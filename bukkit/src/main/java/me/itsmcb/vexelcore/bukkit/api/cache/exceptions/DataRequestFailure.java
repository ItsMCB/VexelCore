package me.itsmcb.vexelcore.bukkit.api.cache.exceptions;

public class DataRequestFailure extends Exception {

    public DataRequestFailure(Exception e) {
        super(e);
    }

    public DataRequestFailure(String s) {
        super(s);
    }

}