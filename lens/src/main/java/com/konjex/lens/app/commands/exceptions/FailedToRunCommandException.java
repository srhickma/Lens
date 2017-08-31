package com.konjex.lens.app.commands.exceptions;

/**
 * Exception thrown when an error occurs running a command.
 */
public class FailedToRunCommandException extends Exception {

    public FailedToRunCommandException(String name, Exception e){
        super("Command " + name + " failed with exception " + e);
    }

}
