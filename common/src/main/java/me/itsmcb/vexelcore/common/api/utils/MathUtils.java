package me.itsmcb.vexelcore.common.api.utils;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {

    public static int randomIntBetween(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean isDifferenceGreaterThan(int num1, int num2, int x) {
        int difference = Math.abs(num1 - num2); // Absolute value to handle negative differences
        return difference > x;
    }
}