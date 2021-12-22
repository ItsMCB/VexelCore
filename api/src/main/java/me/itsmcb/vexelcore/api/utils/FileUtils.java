package me.itsmcb.vexelcore.api.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
