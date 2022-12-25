package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorldUtils {

    public static ArrayList<String> getAllWorldNames() {
        ArrayList<String> worldNames = new ArrayList<>();
        for (File file : Objects.requireNonNull(Bukkit.getWorldContainer().listFiles())) {
            if (file.isDirectory()) {
                File datFile = new File(file.getPath()+File.separator+"uid.dat");
                if (datFile.exists()) {
                    worldNames.add(file.getName());
                }
            }
        }
        return worldNames;
    }

    public static boolean exists(String name) {
        return getAllWorldNames().stream().map(wn -> wn.equalsIgnoreCase(name)).findFirst().isPresent();
    }

    public static boolean kickAllFromWorld(@NotNull World worldNameToKickFrom, String worldNameToSendTo) {
        return kickAllFromWorld(worldNameToKickFrom.getName(), worldNameToSendTo);
    }

    public static boolean kickAllFromWorld(@NotNull String worldNameToKickFrom, String worldNameToSendTo) {
        World kickFrom = Bukkit.getWorld(worldNameToKickFrom);
        List<Player> worldPlayers = kickFrom.getPlayers();
        World sendTo = new WorldCreator(worldNameToSendTo).createWorld();
        if (worldPlayers.size() != 0) {
            Bukkit.getWorld(worldNameToKickFrom).getPlayers().forEach(player -> {
                player.teleport(sendTo.getSpawnLocation());
            });
        }
        if (kickFrom.getPlayers().size() == (0)) {
            return true;
        }
        return false;
    }

    public static boolean isLoaded(String name) {
        return getLoadedWorlds().stream().map(world -> world.getName().equalsIgnoreCase(name)).toList().size() > 0;
    }

    public static boolean unloadWorld(String worldName) {
        return (kickAllFromWorld(worldName, getDefaultWorld()) && Bukkit.unloadWorld(worldName, true));
    }

    public static boolean unloadWorld(World world) {
        return (kickAllFromWorld(world.getName(), getDefaultWorld()) && Bukkit.unloadWorld(world, true));
    }

    public static String getDefaultWorld() {
        // todo Improve later by searching for worlds with normal env type
        return "world";
    }

    public static int getAmountAllWorlds() {
        return getAllWorldNames().size();
    }

    public static ArrayList<World> getAllWorlds() {
        ArrayList<World> allWorlds = new ArrayList<>();
        for (String worldName : getAllWorldNames()) {
            allWorlds.add(Bukkit.getWorld(worldName));
        }
        return allWorlds;
    }

    public static ArrayList<World> getLoadedWorlds() {
        return new ArrayList<>(Bukkit.getWorlds());
    }

    public static List<String> getLoadedWorldNames() {
        return getLoadedWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());
    }

    public static List<String> getUnloadedWorldNames() {
        ArrayList<String> worldNames = getAllWorldNames();
        worldNames.removeAll(getLoadedWorldNames());
        return worldNames;
    }

    public static void deletePlayerData(World world) {
        File playerDataDir = new File(world.getWorldFolder() + File.separator + "playerdata");
        if(playerDataDir.isDirectory()) {
            String[] playerDats = playerDataDir.list();
            for (int i = 0; i < playerDats.length; i++) {
                File datFile = new File(playerDataDir, playerDats[i]);
                datFile.delete();
                System.out.println("Deleted " + datFile.getPath());
            }
        }
    }

}
