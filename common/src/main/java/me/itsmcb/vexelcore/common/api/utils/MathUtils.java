package me.itsmcb.vexelcore.common.api.utils;

import java.util.Random;

public class MathUtils {

    public static int randomIntBetween(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
