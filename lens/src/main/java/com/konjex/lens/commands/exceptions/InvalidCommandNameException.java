package com.konjex.lens.commands.exceptions;

/**
 * Exception thrown when a command is given an invalid name.
 */
public class InvalidCommandNameException extends Exception {

    public InvalidCommandNameException(char badChar){
        super("Character " + badChar + " with code " + (int)badChar + " is not a valid command name character.");
    }

}
