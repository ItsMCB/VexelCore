package me.itsmcb.vexelcore.common.api.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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

    public static void copyDirectory(Path source, Path target) throws IOException {
        copyDirectory(source,target,List.of());
    }

    public static void copyDirectory(Path source, Path target, List<Path> ignoredFiles) throws IOException {
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
    }

    public static String getLastTextInstanceFromFile(Path path, String textWhere) {
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

    public static List<File> getSpecific(Path path, String endsWith) {
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

    public static long getAvailableDiskSpaceBytes(File file) {
        return file.getUsableSpace();
    }

    public static long getTotalDiskSpaceBytes(File file) {
        return file.getTotalSpace();
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

    private static final String[] SIZE_SUFFIXES = {"B", "KB", "MB", "GB"};

    public static String getRecursiveFileSizeFormatted(File file) {
        return getRecursiveFileSizeFormatted(file, new DecimalFormat("#.##"));
    }

    public static String getRecursiveFileSizeFormatted(File file, DecimalFormat decimalFormat) {
        float size = getRecursiveFileSize(file);
        int suffixIndex = 0;
        while (size >= 1024 && suffixIndex < SIZE_SUFFIXES.length - 1) {
            size /= 1024;
            suffixIndex++;
        }
        return String.format("%s %s", decimalFormat.format(size), SIZE_SUFFIXES[suffixIndex]);
    }

    private static long getRecursiveFileSize(File file) {
        if (file.isDirectory()) {
            long size = 0;
            for (File child : file.listFiles()) {
                size += getRecursiveFileSize(child);
            }
            return size;
        } else {
            return file.length();
        }
    }


}
