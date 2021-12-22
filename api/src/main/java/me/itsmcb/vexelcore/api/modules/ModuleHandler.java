package me.itsmcb.vexelcore.api.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleHandler {

    private VexelCorePlatform platform;
    private List<VexelCoreModule> moduleList = new ArrayList<>();

    public ModuleHandler(VexelCorePlatform platform) {
        this.platform = platform;
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
    public void enableModule(String developer, String name) { }

    public void disableModule(String developer, String name) { }
}
