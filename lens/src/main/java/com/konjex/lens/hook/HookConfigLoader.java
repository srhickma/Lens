package com.konjex.lens.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.konjex.lens.conf.ConfigFile;
import com.konjex.lens.conf.ConfigFileLoader;
import com.konjex.lens.conf.yaml.YamlKeyHookDefinitions;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class for loading keyboard hook config file.
 */
public class HookConfigLoader {

    private static final Logger log = Logger.getLogger(HookConfigLoader.class);

    private static final String CONFIG_FILE_NAME = "hook-conf.yml";
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void load(){
        log.info("Loading key hooks from " + ConfigFileLoader.getConfigPath(CONFIG_FILE_NAME));
        ConfigFileLoader.load(CONFIG_FILE_NAME)
                .onHit(HookConfigLoader::load)
                .onMiss(HookConfigLoader::loadAndCopyDefaultConfig);
    }

    private static void load(ConfigFile configFile){
        try{
            load(new FileInputStream(configFile));
        }
        catch(IOException e){
            log.error("Failed to load key hook definitions", e);
        }
    }

    private static void load(InputStream inputStream){
        try{
            final YamlKeyHookDefinitions keyHookDefinitions =
                    yamlMapper.readValue(inputStream, YamlKeyHookDefinitions.class);

            KeyHookProvider.addKeyHooks(keyHookDefinitions.parseToStream());
        }
        catch(IOException e){
            log.error("Failed to load key hook definitions", e);
        }
        catch(RuntimeException e){
            log.error(e);
        }
    }

    private static void loadAndCopyDefaultConfig(){
        log.info("No hook yml found, using default key hooks");
        load(HookConfigLoader.class.getResourceAsStream(CONFIG_FILE_NAME));
        //Copy over to where it should be
    }

}
