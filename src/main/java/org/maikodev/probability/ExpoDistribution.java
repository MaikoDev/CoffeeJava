package org.maikodev.probability;

import java.util.Random;

public class ExpoDistribution {
    public static int getRandomInt(int averageRate) {
        return (int)(-1 * Math.log(1 - RANDOM_GENERATOR.nextFloat()) / (1.0f / averageRate));
    }

    public static long getRandomLong(long averageRate) {
        return (long)(-1 * Math.log(1 - RANDOM_GENERATOR.nextFloat()) / (1.0f / averageRate));
    }

    public static float getRandomFloat(float averageRate) {
        return (float)(-1 * Math.log(1 - RANDOM_GENERATOR.nextFloat()) / (1 / averageRate));
    }

    public static double getRandomDouble(double averageRate) {
        return (-1 * Math.log(1 - RANDOM_GENERATOR.nextFloat()) / (1 / averageRate));
    }

    private static final Random RANDOM_GENERATOR = new Random();
}
