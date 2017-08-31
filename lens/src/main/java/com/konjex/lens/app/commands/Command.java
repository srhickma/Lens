package com.konjex.lens.app.commands;

import com.konjex.lens.app.commands.exceptions.FailedToRunCommandException;
import com.konjex.lens.app.commands.exceptions.InvalidCommandNameException;
import com.konjex.lens.app.commands.types.CommandType;
import com.konjex.util.provide.Providable;

/**
 * Object representing an executable command.
 */
public abstract class Command implements Providable<String> {

    private final String name;
    private final CommandType type;

    protected Command(String name, CommandType type) throws InvalidCommandNameException {
        this.name = CommandSanitizer.sanitize(name.toUpperCase());
        this.type = type;
    }

    public abstract void run() throws FailedToRunCommandException;

    public String getName(){
        return name;
    }

    public CommandType getType(){
        return type;
    }

    public boolean equals(Command command){
        return name.equals(command.name);
    }

    public String getProviderKey(){
        return name;
    }

}
