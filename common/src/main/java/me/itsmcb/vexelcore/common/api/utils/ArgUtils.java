package me.itsmcb.vexelcore.common.api.utils;

import java.util.Arrays;

public class ArgUtils {

    /**
     * Removes by the amount provided
     */
    public static String[] shift(String[] args, int amount) {
        return Arrays.copyOfRange(args,amount,args.length);
    }

    /**
     * Removes the first element
     */
    public static String[] shift(String[] args) {
        return shift(args,1);
    }


    /**
     * Removes the last element
     */
    public static String[] pop(String[] args) {
        if (args.length == 0) {
            return new String[] {};
        }
        return Arrays.copyOfRange(args,0,args.length-1);
    }

    /**
     * Get a string from the arguments
     */
    public static String mergeWithSpace(String[] args, int startIndex) {
        return String.join(" ",shift(args, startIndex));
    }
}
