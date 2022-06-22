package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;

public class WorldUtils {

    public static ArrayList<String> getAllWorldsNames() {
        ArrayList<String> worldNames = new ArrayList<>();
        for (File file : Bukkit.getWorldContainer().listFiles()) {
            if (file.isDirectory()) {
                File datFile = new File(file.getPath()+File.separator+"level.dat");
                if (datFile.exists()) {
                    worldNames.add(file.getName());
                }
            }
        }
        return worldNames;
    }

    public static int getAmountAllWorlds() {
        return getAllWorldsNames().size();
    }

    public static ArrayList<World> getLoadedWorlds() {
        return new ArrayList<>(Bukkit.getWorlds());
    }

    public static int getAmountLoadedWorlds() {
        return getLoadedWorlds().size();
    }
}
