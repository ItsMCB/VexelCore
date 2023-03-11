package me.itsmcb.vexelcore.bukkit;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import me.itsmcb.vexelcore.common.api.utils.JarUtils;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class VexelCoreBukkitPluginManager {

    private File pluginsFolder;

    public VexelCoreBukkitPluginManager(File pluginsFolder) {
        this.pluginsFolder = pluginsFolder;
    }

    ArrayList<VexelCoreBukkitPlugin> plugins = new ArrayList<>();

    public void registerPlugin(VexelCoreBukkitPlugin plugin) {
        plugins.add(plugin);
    }

    public void enablePlugins() {
        plugins.forEach(plugin -> {
            plugin.onEnable();
        });
    }

    public void loadPluginsFromFolder() {
        if (!pluginsFolder.isDirectory()) {
            System.out.println("VexelCore Plugins folder doesn't exist!");
            return;
        }
        for (File plugin : pluginsFolder.listFiles()) {
            YamlDocument pluginYML = BoostedConfig.getUnsavedYAML(JarUtils.getJarResource(plugin, "plugin.yml", PluginClassLoader.class));
            String mainClass = pluginYML.get("main").toString();
            System.out.println("Found main class BETTER: " + mainClass);

            try {
                URLClassLoader child = new URLClassLoader(new URL[] {plugin.toURI().toURL()}, PluginClassLoader.class.getClassLoader());
                Class classToLoad = Class.forName(mainClass, true, child).asSubclass(VexelCoreBukkitPlugin.class);
                Constructor c = classToLoad.getConstructor();
                System.out.println("Loading VC plugin with main class of: " + c.getName());
                VexelCoreBukkitPlugin vexelCoreBukkitPlugin = (VexelCoreBukkitPlugin) c.newInstance();
                registerPlugin(vexelCoreBukkitPlugin);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoClassDefFoundError e ) {
                System.out.println("Failed to load module. Please ensure the VexelCore API and all dependencies are bundled inside the JAR.");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                // Something is wrong with the URL Classloader
                throw new RuntimeException(e);
            }
        }
    }

}
