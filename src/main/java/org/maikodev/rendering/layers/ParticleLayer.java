package org.maikodev.rendering.layers;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.maikodev.order.Position;
import org.maikodev.order.SubdivRange;
import org.maikodev.physics.*;
import org.maikodev.rendering.TerminalDisplayLayer;
import org.maikodev.thread.ThreadPool;
import org.maikodev.thread.task.ClearDensityTask;
import org.maikodev.thread.task.RasterizeParticleTask;

public class ParticleLayer {
    public ParticleLayer(int layerWidth, int layerHeight, byte maxParticles, long averageLifeTime)  {
        LAYER_WIDTH = layerWidth;
        LAYER_HEIGHT = layerHeight;
        MAX_PARTICLES = maxParticles;
        AVERAGE_LIFETIME = averageLifeTime;

        DISPLAY_BUFFER = new TerminalDisplayLayer(layerWidth, layerHeight);
        PARTICLE_DENSITY_MAP = new byte[DISPLAY_BUFFER.getPixelCount()];
        MID_EMITTER_COUNT = (byte)Math.ceil(NUMBER_OF_EMITTERS / 2.0);
        MID_EMITTER_COLUMN = (byte)(layerWidth / 2);

        AVAILABLE_PARTICLES = new LinkedList<>();
        ACTIVE_PARTICLES = new HashMap<>();
        CULLING_QUEUE = new ConcurrentLinkedQueue<>();

        THREAD_POOL = ThreadPool.getPool();
        CLEAR_DENSITY = new ArrayList<>();
        UPDATE_PHYSICS = new ArrayList<>();
        RASTERIZE_PARTICLES = new ArrayList<>();

        PARTICLES = new Particle[MAX_PARTICLES];
        EMITTERS = new ParticleEmitter[NUMBER_OF_EMITTERS];

        NEXT_SPAWN_TIMES = new PriorityQueue<>();
        SUBDIV_RANGES = new PriorityQueue<>(Collections.reverseOrder());

        initParticles();
        initEmitters();
    }

    public void update() throws InterruptedException {
        Instant currentCycleTime = Instant.now();
        if (Duration.between(timeSinceLastFixedUpdate, currentCycleTime).compareTo(FIXED_DELTA_TIME) < 0) return;

        THREAD_POOL.invokeAll(CLEAR_DENSITY);
        THREAD_POOL.invokeAll(UPDATE_PHYSICS);
        THREAD_POOL.invokeAll(RASTERIZE_PARTICLES);

        // CULL DEAD PARTICLES //
        while (!CULLING_QUEUE.isEmpty()) {
            int deadID = CULLING_QUEUE.poll();

            ACTIVE_PARTICLES.remove(deadID);
            AVAILABLE_PARTICLES.add(deadID);
        }

        /* Schedule new particles */
        boolean isScheduleTime = Duration.between(lastScheduleTime, currentCycleTime).compareTo(SPAWN_DELAY_TIME) > 0;
        if (isScheduleTime && !AVAILABLE_PARTICLES.isEmpty()) {
            int spawnCount = -1;

            do {
                spawnCount = (int)RANDOM_GENERATOR.nextGaussian(MEAN_SPAWN_AMOUNT, PARTICLE_STDDEV);
            } while (spawnCount < 0);

            long startRange = 0, endRange = SPAWN_DELAY_TIME.toMillis(), spawnTime;

            /* Subdivide Spawn Delay */
            for (int currentSpawnCount = 0; currentSpawnCount < spawnCount; currentSpawnCount++) {
                spawnTime = RANDOM_GENERATOR.nextLong(startRange, endRange);

                NEXT_SPAWN_TIMES.add(spawnTime);
                SUBDIV_RANGES.add(new SubdivRange(startRange, spawnTime));
                SUBDIV_RANGES.add(new SubdivRange(spawnTime, endRange));

                SubdivRange maxRange = SUBDIV_RANGES.poll();

                startRange = maxRange.startRange;
                endRange = maxRange.endRange;
            }

            SUBDIV_RANGES.clear();
            lastScheduleTime = Instant.now();
        }

        /* Emit particles */
        boolean isSpawnTime = Duration.between(lastScheduleTime, currentCycleTime).compareTo(nextSpawnDuration) > 0;
        if (isSpawnTime && !NEXT_SPAWN_TIMES.isEmpty()) {
            int emitterIndex = -1;
            do {
                emitterIndex = (int)RANDOM_GENERATOR.nextGaussian(MID_EMITTER_COUNT, PARTICLE_STDDEV);

            } while (emitterIndex < 0 || emitterIndex >= NUMBER_OF_EMITTERS);

            EMITTERS[emitterIndex].emit(PARTICLES);
            nextSpawnDuration = Duration.ofMillis(NEXT_SPAWN_TIMES.poll());
        }

        timeSinceLastFixedUpdate = currentCycleTime;
    }

    public TerminalDisplayLayer getDisplay() { return DISPLAY_BUFFER; }

    //region Private Methods
    private void initParticles() {
        char[] pixelBuffer = DISPLAY_BUFFER.getPixelBuffer();
        boolean[] transparencyBuffer = DISPLAY_BUFFER.getTransparencyBuffer();

        /* Initialize Density and Rasterize tasks */
        for (int i = 0; i < LAYER_HEIGHT; i++) {
            CLEAR_DENSITY.add(Executors.callable(new ClearDensityTask(PARTICLE_DENSITY_MAP, i, LAYER_WIDTH)));
            RASTERIZE_PARTICLES.add(Executors.callable(new RasterizeParticleTask(pixelBuffer, transparencyBuffer, PARTICLE_DENSITY_MAP, i, LAYER_WIDTH)));
        }

        /* Pre-allocate and create pool of particles and its physics task */
        for (int i = 0; i < MAX_PARTICLES; i++) {
            Particle particle = new Particle(FIXED_DELTA_TIME.toMillis(), PARTICLE_DENSITY_MAP, CULLING_QUEUE, i, LAYER_WIDTH);
            PARTICLES[i] = particle;

            AVAILABLE_PARTICLES.add(i);
            UPDATE_PHYSICS.add(Executors.callable(particle));
        }
    }

    /* Create and assign emitters at their positions */
    private void initEmitters() {
        int outerBeginColumn = MID_EMITTER_COLUMN - MID_EMITTER_COUNT, outerEndColumn = MID_EMITTER_COLUMN + MID_EMITTER_COUNT - 1;
        int innerBeginColumn = outerBeginColumn + 2, innerEndColumn = outerEndColumn - 3;

        byte emitterRow = 13;
        for (int layerColumn = outerBeginColumn; layerColumn < outerEndColumn; layerColumn++) {
            if (layerColumn > innerBeginColumn && layerColumn < innerEndColumn) {
                EMITTERS[layerColumn - outerBeginColumn] = new ParticleEmitter(new Position(layerColumn, emitterRow), AVAILABLE_PARTICLES, ACTIVE_PARTICLES, AVERAGE_LIFETIME);
            } else {
                EMITTERS[layerColumn - outerBeginColumn] = new ParticleEmitter(new Position(layerColumn, emitterRow - 1), AVAILABLE_PARTICLES, ACTIVE_PARTICLES, AVERAGE_LIFETIME);
            }
        }
    }
    //endregion

    //region Instance Variables
    private Instant timeSinceLastFixedUpdate = Instant.now();
    private Instant lastScheduleTime = Instant.now();
    private Duration nextSpawnDuration = Duration.ZERO;

    private final PriorityQueue<Long> NEXT_SPAWN_TIMES;
    private final PriorityQueue<SubdivRange> SUBDIV_RANGES;

    private final int LAYER_WIDTH;
    private final int LAYER_HEIGHT;

    private final Queue<Integer> AVAILABLE_PARTICLES;
    private final HashMap<Integer, Particle> ACTIVE_PARTICLES;
    private final ConcurrentLinkedQueue<Integer> CULLING_QUEUE;
    private final Particle[] PARTICLES;

    private final ParticleEmitter[] EMITTERS;
    private final byte MID_EMITTER_COUNT;
    private final byte MID_EMITTER_COLUMN;

    private final byte MAX_PARTICLES;
    private final long AVERAGE_LIFETIME;

    private final TerminalDisplayLayer DISPLAY_BUFFER;
    private final byte[] PARTICLE_DENSITY_MAP;

    private final ExecutorService THREAD_POOL;
    private final List<Callable<Object>> CLEAR_DENSITY;
    private final List<Callable<Object>> UPDATE_PHYSICS;
    private final List<Callable<Object>> RASTERIZE_PARTICLES;

    private final static Duration FIXED_DELTA_TIME = Duration.ofMillis(20);
    private final static Duration SPAWN_DELAY_TIME = Duration.ofMillis(200);
    private final static double MEAN_SPAWN_AMOUNT = 20.0;
    private final static double PARTICLE_STDDEV = 4.0;
    private final static byte NUMBER_OF_EMITTERS = 29;

    private final static Random RANDOM_GENERATOR = new Random();
    //endregion
}
