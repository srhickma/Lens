package com.konjex.lens.commands;

import com.google.common.collect.Maps;
import com.konjex.lens.commands.exceptions.CommandNotFoundException;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Class for storing custom and pre-defined command mappings.
 */
class CommandProvider {

    private static final Logger log = Logger.getLogger(CommandProvider.class);

    private static Map<String, Command> commandMap = Maps.newHashMap();

    static void addCommands(Stream<Command> commands){
        commands.forEach(CommandProvider::addCommand);
    }

    static void addCommand(Command command){
        log.info("Adding command definition -> " + command.getName());
        commandMap.put(command.getName(), command);
    }

    static Command get(String name) throws CommandNotFoundException {
        if(commandMap.containsKey(name)){
            return commandMap.get(name);
        }
        throw new CommandNotFoundException(name);
    }

}
