package org.maikodev.probability;

import java.util.Random;

public class PoissonDistribution {
    public PoissonDistribution(float averageRate, int upperBound) {
        LAMBDA = averageRate;
        UPPER_BOUND = upperBound;
        PROBABILITY_DISTRIBUTION = new float[UPPER_BOUND];
        RNG = new Random();

        /*  */
        for (int i = 0; i < UPPER_BOUND; i++) {
            PROBABILITY_DISTRIBUTION[i] = (float)massProbability(LAMBDA, i);
        }
    }

    public int getRandomX() {
        double selection = RNG.nextDouble();
        double cumulativeProbability = 0;

        for (int i = 0; i < UPPER_BOUND; i++) {
            cumulativeProbability += PROBABILITY_DISTRIBUTION[i];

            if (selection <= cumulativeProbability) {
                return i;
            }
        }

        return -1;
    }

    private static double massProbability(float lambda, int x) {
        return (Math.pow(lambda, x) * Math.pow(Math.E, -1 * lambda)) / factorial(x);
    }

    private static long factorial(int x) {
        if (x == 0) return 1;

        long result = x;
        for (int i = x - 1; i > 0; i--) {
            result *= i;
        }

        return result;
    }

    private final float LAMBDA;
    private final int UPPER_BOUND;
    private float[] PROBABILITY_DISTRIBUTION;

    private final Random RNG;
}
