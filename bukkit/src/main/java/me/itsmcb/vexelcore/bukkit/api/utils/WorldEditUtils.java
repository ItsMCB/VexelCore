package me.itsmcb.vexelcore.bukkit.api.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.itsmcb.vexelcore.bukkit.api.config.BukkitCoordinates;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class WorldEditUtils {

    public static File getSchematicFile(String fileName) {
        return new File(
                Bukkit.getPluginsFolder() +
                        File.separator + "FastAsyncWorldEdit" +
                        File.separator + "schematics" +
                        File.separator + fileName + ".schem"
        );
    }

    public static CompletableFuture<Void> pasteSchematic(File file, World world, BukkitCoordinates location, boolean ignoreAirBlocks) {
        return CompletableFuture.runAsync(() -> {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                throw new IllegalArgumentException("Unrecognized schematic format: " + file.getName());
            }

            try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
                Clipboard clipboard = reader.read();

                Actor actor = BukkitAdapter.adapt(Bukkit.getConsoleSender());

                try (EditSession editSession = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(BukkitAdapter.adapt(world))
                        .actor(actor)
                        .maxBlocks(-1)
                        .build()) {

                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                            .ignoreAirBlocks(ignoreAirBlocks)
                            .build();

                    Operations.complete(operation);
                }
            } catch (IOException | RuntimeException e) {
                throw new RuntimeException("Failed to paste schematic: " + file.getName(), e);
            }
        });
    }


    public static void pasteSchematic(File file, Location location, boolean ignoreAirBlocks) {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            Actor actor = BukkitAdapter.adapt(Bukkit.getConsoleSender()); // For plugins to check (such as LibreProperty ignoring world border limitations if the actor is console)
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(location.getWorld())).actor(actor).maxBlocks(-1).build()) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                        .ignoreAirBlocks(ignoreAirBlocks)
                        .build();
                Operations.complete(operation);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static boolean createAndSaveSchematic(BlockVector3 max, BlockVector3 min, World world, File file) {
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        forwardExtentCopy.setCopyingEntities(true);
        forwardExtentCopy.setRemovingEntities(false);
        forwardExtentCopy.setCopyingBiomes(true);
        Operations.complete(forwardExtentCopy);

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CuboidRegion expandCuboidRegionWalls(BlockVector3 max, BlockVector3 min, World world, int expansionAmount) {
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), min, max);
        BlockVector3 newMin = region.getMinimumPoint().subtract(expansionAmount,0,expansionAmount);
        BlockVector3 newMax = region.getMaximumPoint().add(expansionAmount,0,expansionAmount);
        region.setPos1(newMin);
        region.setPos2(newMax);
        return region;
    }

    public static CuboidRegion expandCuboidRegionAllSides(BlockVector3 max, BlockVector3 min, World world, int expansionAmount) {
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), min, max);
        BlockVector3 newMin = region.getMinimumPoint().subtract(expansionAmount,expansionAmount,expansionAmount);
        BlockVector3 newMax = region.getMaximumPoint().add(expansionAmount,expansionAmount,expansionAmount);
        region.setPos1(newMin);
        region.setPos2(newMax);
        return region;
    }
}
