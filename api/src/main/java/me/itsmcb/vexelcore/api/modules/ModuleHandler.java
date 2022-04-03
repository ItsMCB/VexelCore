package me.itsmcb.vexelcore.api.modules;

import me.itsmcb.vexelcore.api.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.itsmcb.vexelcore.api.utils.JarUtils.getMainClassConstructorOfJar;

public class ModuleHandler {

    private VexelCorePlatform platform;
    private List<VexelCoreModule> moduleList = new ArrayList<>();
    private File dataFolder;
    // Messages
    private String loadingFailedAlert = "Failed to load module. Please ensure the VexelCore API and all dependencies are bundled inside the JAR.";
    private String registeringAlert = "Registering ";
    private String registeringCancelledAlert = "Registering has been cancelled for ";
    private String registeringStoppedIncorrectPlatform = "Can't load incompatible module ";

    public ModuleHandler(VexelCorePlatform platform, File dataFolder) {
        this.platform = platform;
        this.dataFolder = dataFolder;
    }

    public VexelCorePlatform getPlatform() { return this.platform; }

    public List<VexelCoreModule> getModuleList() { return this.moduleList; }

    public void removeModule(VexelCoreModule module) {
        this.moduleList.remove(module);
    }

    public void addModule(VexelCoreModule vexelCoreModule) {
        if (vexelCoreModule.getPlatform().equals(platform)) {
            System.out.println(registeringAlert + vexelCoreModule);
            if (moduleList.contains(vexelCoreModule)) {
                System.out.println(registeringCancelledAlert + vexelCoreModule);
                return;
            }
            moduleList.add(vexelCoreModule);
        } else {
            System.out.println(registeringStoppedIncorrectPlatform + vexelCoreModule);
        }
    }

    public Optional<VexelCoreModule> getModule(String developer, String name) {
        return moduleList.stream().filter(vexelCoreModule -> vexelCoreModule.getId().equalsIgnoreCase(developer + ":" + name)).findFirst();
    }

    // To be overridden by platform extension
    public ModuleLoadStatus enableModule(String developer, String name) { return ModuleLoadStatus.UNKNOWN; }

    public void unloadModule(String developer, String name) { }

    public void disableAllModules() { }

    public void loadLocalModules() {
        List<File> fileList = FileUtils.getSpecific(Paths.get(dataFolder + File.separator + "Modules"), "jar");
        for (File file : fileList) {
            try {
                loadModule(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadModule(URL url) {
        Constructor c = getMainClassConstructorOfJar(url, this.getClass().getClassLoader());
        if (c == null) {
            System.out.println(loadingFailedAlert);
            return;
        }
        System.out.println("Loading module with main class of: " + c.getName());
        try {
            VexelCoreModule vexelCoreModule = (VexelCoreModule) c.newInstance();
            addModule(vexelCoreModule);
            enableModule(vexelCoreModule.getDeveloper(),vexelCoreModule.getName());
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
