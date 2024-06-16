package org.maikodev.physics;

import java.util.HashMap;
import java.util.Queue;
import org.maikodev.order.Position;
import org.maikodev.probability.ExpoDistribution;

public class ParticleEmitter {
    public ParticleEmitter(Position emitterPos, Queue<Integer> availableQueue, HashMap<Integer, Particle> activeQueue, long lifeTime) {
        EMITTER_POS = emitterPos;
        AVAILABLE_QUEUE = availableQueue;
        ACTIVE_QUEUE = activeQueue;
        AVERAGE_LIFETIME = lifeTime;
    }

    public void emit(Particle[] particlePool)  {
        if (AVAILABLE_QUEUE.isEmpty()) return;

        int availableIndex = AVAILABLE_QUEUE.poll();
        ACTIVE_QUEUE.put(availableIndex, particlePool[availableIndex].reset(ExpoDistribution.getRandomLong(AVERAGE_LIFETIME), EMITTER_POS.x, EMITTER_POS.y, 0.2f));
    }

    private final Position EMITTER_POS;

    private final Queue<Integer> AVAILABLE_QUEUE;
    private final HashMap<Integer, Particle> ACTIVE_QUEUE;

    private final long AVERAGE_LIFETIME;
}
