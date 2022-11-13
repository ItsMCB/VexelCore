package me.itsmcb.vexelcore.bukkit.api.utils;

import java.io.File;

public class FileUtils {

    public static boolean deleteFile(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFile(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}
