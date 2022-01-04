package me.itsmcb.vexelcore.api.utils;

import java.util.Random;

public class MathUtils {

    public static int getRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
