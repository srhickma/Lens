package com.konjex.lens.app.commands.types;

import com.konjex.lens.app.commands.Command;
import com.konjex.lens.app.commands.exceptions.FailedToRunCommandException;
import com.konjex.lens.app.commands.exceptions.InvalidCommandNameException;

import java.util.List;

/**
 * Command for running a group of other commands.
 */
public class GroupCommand extends Command {

    private final List<Command> commands;

    public GroupCommand(String name, List<Command> commands) throws InvalidCommandNameException {
        super(name, CommandType.GROUP);
        this.commands = commands;
    }

    public void run() throws FailedToRunCommandException {
        for(Command command : commands){
            command.run();
        }
    }

}
