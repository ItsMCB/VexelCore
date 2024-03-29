package me.itsmcb.vexelcore.common.api.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.serialization.YamlSerializer;
import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BoostedConfig {

    private final String fileName;

    private final File customConfigFile;
    private YamlDocument customConfig;
    private final InputStream inputStream;

    private YamlSerializer serializer = new StandardSerializer("==");

    public BoostedConfig(File dataFolder, String fileName, InputStream inputStream) {
        this.inputStream = inputStream;
        this.customConfigFile = new File(dataFolder, fileName+".yml");
        this.fileName = fileName;
        saveDefaultConfig();
    }

    public BoostedConfig(File dataFolder, String fileName, InputStream inputStream, YamlSerializer serializer) {
        this.serializer = serializer;
        if (inputStream == null) {
            String defaultConfigData = "file-version: 1";
            this.inputStream = new ByteArrayInputStream(defaultConfigData.getBytes());
        } else {
            this.inputStream = inputStream;
        }
        this.customConfigFile = new File(dataFolder, fileName+".yml");
        this.fileName = fileName;
        saveDefaultConfig();
    }

    public void saveDefaultConfig() {
        try {
            customConfig = YamlDocument.create(
                    customConfigFile,
                    inputStream,
                    GeneralSettings.builder().setDefaultNumber(-1).setUseDefaults(false).setSerializer(serializer).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public YamlDocument get() {
        return customConfig;
    }

    public boolean exists() {
        try {
            return customConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean save() {
        try {
         return customConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean reload() {
        try {
            return customConfig.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static YamlDocument getUnsavedYAML(InputStream inputStream) {
        try {
            return YamlDocument.create(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileName() {
        return fileName;
    }
}
