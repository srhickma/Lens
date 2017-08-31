package com.konjex.lens.app.commands.types;

import com.konjex.lens.app.commands.Command;
import com.konjex.lens.app.commands.exceptions.FailedToRunCommandException;
import com.konjex.lens.app.commands.exceptions.InvalidCommandNameException;

import java.io.IOException;

/**
 * Command for running an shell command with optional arguments.
 */
public class RunCommand extends Command {

    private final String[] command;

    public RunCommand(String name, String[] command) throws InvalidCommandNameException {
        super(name, CommandType.RUN);
        this.command = command;
    }

    public void run() throws FailedToRunCommandException {
        try{
            new ProcessBuilder(command).start();
        }
        catch(IOException e){
            throw new FailedToRunCommandException(getName(), e);
        }
    }

}
