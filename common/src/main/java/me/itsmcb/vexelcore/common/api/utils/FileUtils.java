package me.itsmcb.vexelcore.common.api.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class FileUtils {

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

}
