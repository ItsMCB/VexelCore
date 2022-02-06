package me.itsmcb.vexelcore.api.modules;

import me.itsmcb.vexelcore.api.utils.JarUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleHandler {

    private VexelCorePlatform platform;
    private List<VexelCoreModule> moduleList = new ArrayList<>();
    private File dataFolder;

    public ModuleHandler(VexelCorePlatform platform, File dataFolder) {
        this.platform = platform;
        this.dataFolder = dataFolder;
    }

    public VexelCorePlatform getPlatform() { return this.platform; }

    public List<VexelCoreModule> getModuleList() { return this.moduleList; }

    public void addModule(VexelCoreModule vexelCoreModule) {
        if (vexelCoreModule.getPlatform().equals(platform)) {
            System.out.println("Registering " + vexelCoreModule);
            if (moduleList.contains(vexelCoreModule)) {
                System.out.println("Not re-registering " + vexelCoreModule);
                return;
            }
            moduleList.add(vexelCoreModule);
        } else {
            System.out.println("Can't load " + vexelCoreModule);
        }
    }

    public Optional<VexelCoreModule> getModule(String developer, String name) {
        return moduleList.stream().filter(vexelCoreModule -> vexelCoreModule.getId().equalsIgnoreCase(developer + ":" + name)).findFirst();
    }

    // To be overridden by platform extension
    public ModuleLoadStatus enableModule(String developer, String name) { return ModuleLoadStatus.UNKNOWN; }

    public void disableModule(String developer, String name) { }

    public void loadLocalModules() {
        System.out.println("Enabling experimental local module support...");
        try {
            File modulesFolder = Paths.get(dataFolder + File.separator + "Modules").toFile();
            if (!modulesFolder.exists()) {
                modulesFolder.mkdirs();
            }
            File[] modulesFolderItems = modulesFolder.listFiles();
            if (modulesFolderItems == null) {
                return;
            }
            for (final File fileEntry : modulesFolderItems) {
                if (!fileEntry.isDirectory()) {
                    loadModuleFromURL(fileEntry.toURI().toURL());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void loadModuleFromURL(URL url) {
        String mainClass = JarUtils.getMainClassOfJar(url.getFile());
        if (mainClass == null) {
            return;
        }
        try {
            URLClassLoader child = new URLClassLoader(new URL[] {url}, this.getClass().getClassLoader());
            Class classToLoad = Class.forName(mainClass, true, child);
            Constructor c = classToLoad.getConstructor();
            System.out.println("Loading module with main class of: " + c.getName());
            VexelCoreModule vexelCoreModule = (VexelCoreModule) c.newInstance();
            addModule(vexelCoreModule);
            enableModule(vexelCoreModule.getDeveloper(),vexelCoreModule.getName());
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
        }
    }
}
