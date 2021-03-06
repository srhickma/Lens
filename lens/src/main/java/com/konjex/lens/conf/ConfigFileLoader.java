package com.konjex.lens.conf;

import java.io.File;
import java.util.function.Consumer;

/**
 * Class for loading lens configuration files by name.
 */
public abstract class ConfigFileLoader {

    private static final String CMD_CONFIG_PATH = System.getProperty("user.home") + File.separator + ".lens" + File.separator;

    public static String getConfigPath(String configName){
        return CMD_CONFIG_PATH + configName;
    }

    public static ConfigLoadResult load(String configName){
        ConfigFile config = new ConfigFile(getConfigPath(configName));
        if(!config.exists()){
            return ConfigLoadResult.miss();
        }
        return ConfigLoadResult.hit(config);
    }

    public static class ConfigLoadResult {

        private final ConfigFile file;
        private final boolean loaded;

        private ConfigLoadResult(ConfigFile file, boolean loaded){
            this.file = file;
            this.loaded = loaded;
        }

        private static ConfigLoadResult hit(ConfigFile file){
            return new ConfigLoadResult(file, true);
        }

        private static ConfigLoadResult miss(){
            return new ConfigLoadResult(null, false);
        }

        public ConfigLoadResult onHit(Consumer<ConfigFile> consumer){
            if(loaded){
                consumer.accept(file);
            }
            return this;
        }

        public ConfigLoadResult onMiss(Runnable runnable){
            if(!loaded){
                runnable.run();
            }
            return this;
        }

    }

}
