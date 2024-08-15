package me.itsmcb.vexelcore.common.api.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class FileUtils {

    public static boolean delete(@NotNull File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return false;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    delete(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    public static boolean copyDirectory(@NotNull Path source, @NotNull Path target) throws IOException {
        return copyDirectory(source,target,List.of());
    }

    public static boolean copyDirectory(@NotNull Path source, @NotNull Path target, @NotNull List<Path> ignoredFiles) throws IOException {
        if (!(source.toFile().exists())) {
            return false;
        }
        Files.walk(source)
            .forEach(sourcePath -> {
                Path relativePath = source.relativize(sourcePath);
                Path targetPath = target.resolve(relativePath);
                if (!(ignoredFiles.contains(relativePath))) {
                    try {
                        Files.copy(sourcePath,targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        return true;
    }

    public static String getLastTextInstanceFromFile(@NotNull Path path, @NotNull String textWhere) {
        AtomicReference<String> foundText = null;
        try (Stream<String> stream = Files.lines(path)) {
            stream.filter(lines -> lines.contains(textWhere)).forEach(line -> {
                foundText.set(line.replace(textWhere, "").trim());
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return foundText.get();
    }

    public static List<File> getSpecific(@NotNull Path path, @NotNull String endsWith) {
        List<File> fileList = new ArrayList<>();

        File modulesFolder = path.toFile();
        if (!modulesFolder.exists()) {
            return null;
        }
        File[] modulesFolderItems = modulesFolder.listFiles();
        if (modulesFolderItems == null) {
            return fileList;
        }
        for (final File fileEntry : modulesFolderItems) {
            if (fileEntry.getPath().endsWith(endsWith)) {
                fileList.add(fileEntry);
            }
        }
        return fileList;
    }

    public static long getSizeBytes(@NotNull File file) {
        long size = 0;
        try (Stream<Path> walk = Files.walk(Path.of(file.getPath()))) {
            size = walk
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    public static long getDiskUsedBytes() {
        return getOSRoot().getTotalSpace();
    }

    public static long getDiskAvailableBytes() {
        return getOSRoot().getUsableSpace();
    }

    public static long getDiskTotalBytes() {
        return getOSRoot().getTotalSpace();
    }

    public static File getOSRoot() {
        File root = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            root = new File("C:");
        } else {
            root = new File("/");
        }
        return root;
    }
    private static final String[] SIZE_SUFFIXES = {"B", "KB", "MB", "GB","TB","PB"};

    public static String getRecursiveFileSizeFormatted(@NotNull File file) {
        return getRecursiveFileSizeFormatted(getSizeBytes(file), null);
    }

    public static String getRecursiveFileSizeFormatted(long bytes) {
        return getRecursiveFileSizeFormatted(bytes, null);
    }

    public static String getRecursiveFileSizeFormatted(long bytes, DecimalFormat df) {
        if (df == null) {
            df = new DecimalFormat("#.##");
        }
        int suffixIndex = 0;
        double size = bytes;

        while (size >= 1000 && suffixIndex < SIZE_SUFFIXES.length - 1) {
            size /= 1000;
            suffixIndex++;
        }

        return df.format(size) + " " + SIZE_SUFFIXES[suffixIndex];
    }
}
