package me.itsmcb.vexelcore.common.api.utils;

import java.util.Random;

public class MathUtils {

    public static int randomIntBetween(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static boolean isDifferenceGreaterThan(int num1, int num2, int x) {
        int difference = Math.abs(num1 - num2); // Absolute value to handle negative differences
        return difference > x;
    }
}
