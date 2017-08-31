package com.konjex.lens.app.commands.types;

import com.konjex.lens.app.events.LensEventManager;
import com.konjex.lens.app.events.ProcessCreatedEvent;
import com.konjex.lens.app.commands.Command;
import com.konjex.lens.app.commands.exceptions.FailedToRunCommandException;
import com.konjex.lens.app.commands.exceptions.InvalidCommandNameException;

import java.io.IOException;

/**
 * Command for running a shell command.
 */
public class ShellCommand extends Command {

    private final String command;

    public ShellCommand(String name, String command) throws InvalidCommandNameException {
        super(name, CommandType.SHELL);
        this.command = command;
    }

    public void run() throws FailedToRunCommandException {
        try{
            Process process = new ProcessBuilder(command).start();
            LensEventManager.fire(new ProcessCreatedEvent(getName(), 0));
        }
        catch(IOException e){
            throw new FailedToRunCommandException(getName(), e);
        }
    }


}
