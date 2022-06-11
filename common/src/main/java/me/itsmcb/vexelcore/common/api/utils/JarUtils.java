package me.itsmcb.vexelcore.common.api.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarUtils {

    public static String getAttribute(String file, String attributeKey) {
        try {
            JarFile jarfile = new JarFile(file);
            Manifest manifest = jarfile.getManifest();
            Attributes attrs = manifest.getMainAttributes();
            Optional<Map.Entry<Object, Object>> kvp = attrs.entrySet().stream().filter(entry -> entry.getKey().toString().equals(attributeKey)).findFirst();
            return kvp.map(objectObjectEntry -> objectObjectEntry.getValue().toString()).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMainClassOfJar(String file) {
        return getAttribute(file, "Main-Class");
    }

    public static String getManifestVersion(String file) {
        return getAttribute(file, "Manifest-Version");
    }

    public static Constructor getMainClassConstructorOfJar(URL url, ClassLoader classLoader) {
        String mainClass = getMainClassOfJar(url.getFile());
        if (mainClass == null) {
            System.out.println("Couldn't find main class for " + url.getPath());
            return null;
        }
        try {
            URLClassLoader child = new URLClassLoader(new URL[] {url}, classLoader);
            Class classToLoad = Class.forName(mainClass, true, child);
            return classToLoad.getConstructor();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
