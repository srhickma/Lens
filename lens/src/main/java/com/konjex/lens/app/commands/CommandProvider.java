package com.konjex.lens.app.commands;

import com.konjex.lens.app.commands.exceptions.CommandNotFoundException;
import com.konjex.util.provide.ProvidableNotFoundException;
import com.konjex.util.provide.Provider;
import org.apache.log4j.Logger;

import java.util.stream.Stream;

/**
 * Class for storing custom and pre-defined command mappings.
 */
class CommandProvider {

    private static final Logger log = Logger.getLogger(CommandProvider.class);

    private static Provider<String, Command> commandProvider = new Provider<>();

    static void addCommands(Stream<Command> commands){
        commandProvider.store(commands);
    }

    static void addCommand(Command command){
        log.info("Adding command definition -> " + command.getName());
        commandProvider.store(command);
    }

    static Command get(String name) throws CommandNotFoundException {
        try{
            return commandProvider.request(name);
        }
        catch(ProvidableNotFoundException e){
            throw new CommandNotFoundException(name);
        }
    }

}
