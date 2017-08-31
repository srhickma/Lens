package com.konjex.lens.app.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.konjex.lens.app.commands.exceptions.InvalidCommandNameException;
import com.konjex.lens.app.commands.types.InternalCommand;
import com.konjex.lens.conf.ConfigFileLoader;
import com.konjex.lens.conf.yaml.YamlCommandDefinitions;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Class for loading command definitions on launch and request.
 */
public class CommandLoader {

    private static final Logger log = Logger.getLogger(CommandLoader.class);

    private static final String CONFIG_FILE_NAME = "cmd-conf.yml";
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void load(){
        loadDefaults();

        log.info("Loading command definitions from " + ConfigFileLoader.getConfigPath(CONFIG_FILE_NAME));
        ConfigFileLoader.load(CONFIG_FILE_NAME)
                .onHit(configFile -> {
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
                })
                .onMiss(() -> log.info("No command yml found, using default commands"));
    }

    private static void loadDefaults(){
        log.info("Constructing default commands definitions");
        try{
            CommandProvider.addCommand(new InternalCommand("EXIT", () -> System.exit(0)));
        }
        catch(InvalidCommandNameException e){
            log.error(e);
        }
    }

}
