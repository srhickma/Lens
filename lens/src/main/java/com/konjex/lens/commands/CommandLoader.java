package com.konjex.lens.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.konjex.lens.commands.exceptions.InvalidCommandNameException;
import com.konjex.lens.conf.yaml.YamlCommandDefinitions;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Class for loading command definitions on launch and request.
 */
public class CommandLoader {

    private static final Logger log = Logger.getLogger(CommandLoader.class);

    private static final String CMD_CONFIG_PATH = System.getProperty("user.home") + File.separator + ".lens" + File.separator + "cmd-conf.yml";
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void load(){
        loadDefaults();
        log.info("Loading command definitions from " + CMD_CONFIG_PATH);
        File configFile = new File(CMD_CONFIG_PATH);
        if(!configFile.exists()){
            log.info("No command yml found, using default commands");
            return;
        }

        try{
            final YamlCommandDefinitions commandDefinitions =
                    yamlMapper.readValue(configFile, YamlCommandDefinitions.class);

            CommandProvider.addCommands(commandDefinitions.parseToStream());
        }
        catch(IOException e){
            log.error("Failed to load command definitions", e);
        }
        catch(RuntimeException e){
            log.error(e);
        }
    }

    private static void loadDefaults(){
        log.info("Constructing default commands definitions");
        try{
            CommandProvider.addCommand(new Command("EXIT"));
        }
        catch(InvalidCommandNameException e){
            log.error(e);
        }
    }

}
