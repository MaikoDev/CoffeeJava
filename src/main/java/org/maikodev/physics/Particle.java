package org.maikodev.physics;

import org.maikodev.order.RowMajor;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Particle implements Runnable {
    public Particle(long fixedDeltaTime, byte[] densityMatrix, ConcurrentLinkedQueue<Integer> cullQueue, Integer particleID, int xBounds) {
        FIXED_DELTA_TIME = fixedDeltaTime;
        DENSITY_MAP = densityMatrix;
        CULLING_QUEUE = cullQueue;
        PARTICLE_ID = particleID;
        X_BOUNDS = xBounds;

        isAvailable = true;
    }

    public Particle(long fixedDeltaTime, byte[] densityMatrix, ConcurrentLinkedQueue<Integer> cullQueue, Integer particleID, int xBounds, long lifeTime, float initialX, float initialY, float yVelocity) {
        FIXED_DELTA_TIME = fixedDeltaTime;
        DENSITY_MAP = densityMatrix;
        CULLING_QUEUE = cullQueue;
        PARTICLE_ID = particleID;
        X_BOUNDS = xBounds;

        reset(lifeTime, posX, posY, yVelocity);
    }

    public Particle reset(long lifeTime, float posX, float posY, float yVelocity) {
        this.posX = posX;
        this.posY = posY;
        this.yVelocity = yVelocity;
        this.lifeTime = lifeTime;

        initialX = posX;
        initialY = posY;

        currentLifeTime = 0;
        isAvailable = false;
        oscillationAmplitude = RANDOM_GENERATOR.nextFloat(1f, 2f);

        return this;
    }

    /* Physics update on fixedDeltaTime*/
    @Override
    public void run() {
        currentLifeTime += FIXED_DELTA_TIME;
        if (currentLifeTime >= lifeTime) {
            if (!isAvailable) {
                isAvailable = true;
                CULLING_QUEUE.offer(PARTICLE_ID);
            }

            return;
        }

        posX = initialX + oscillationAmplitude * (float)Math.sin(Math.abs(initialY - posY) + Math.PI);
        posY -= FIXED_DELTA_TIME * 0.01f * yVelocity;

        updateDensity(DENSITY_MAP, RowMajor.getIndex(Math.round(posY), Math.round(posX), X_BOUNDS), (float)(lifeTime - currentLifeTime) / lifeTime);
    }

    private synchronized static void updateDensity(byte[] densityMap, int approxPosition, float particleIntensity) {
        byte previousDensity = densityMap[approxPosition];

        densityMap[approxPosition] = (byte)(previousDensity + DENSITY_RESOLUTION * particleIntensity);
    }

    private float posX;
    private float posY;
    private float initialX;
    private float initialY;
    private float yVelocity;

    private long lifeTime;
    private long currentLifeTime;
    private boolean isAvailable;
    private float oscillationAmplitude;

    private final long FIXED_DELTA_TIME;
    private final byte[] DENSITY_MAP;
    private final Integer PARTICLE_ID;
    private final int X_BOUNDS;

    private final ConcurrentLinkedQueue<Integer> CULLING_QUEUE;

    private static final Random RANDOM_GENERATOR = new Random();
    private static final int DENSITY_RESOLUTION = 8;
}
