package me.itsmcb.vexelcore.common.api.utils;

import java.util.Arrays;

public class ArgUtils {

    /**
     * Removes the first element
     */
    public static String[] shift(String[] args) {
        if (2 > args.length) {
            return new String[] {};
        }
        return Arrays.copyOfRange(args,1,args.length);
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
}
