package com.konjex.lens.commands.exceptions;

/**
 * Exception thrown when a command is not held by the command provider.
 */
public class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String name){
        super("Command " + name + " not found.");
    }

}
