package me.itsmcb.vexelcore.bukkit.api.utils;

import java.io.File;

public class FileUtils {

    public static boolean deleteFile(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFile(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

}
