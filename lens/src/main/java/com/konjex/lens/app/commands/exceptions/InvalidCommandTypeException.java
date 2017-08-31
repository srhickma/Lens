package com.konjex.lens.app.commands.exceptions;

/**
 * Exception thrown when a command is given an invalid type.
 */
public class InvalidCommandTypeException extends Exception {

    public InvalidCommandTypeException(){
        super("Command type must be defined");
    }

    public InvalidCommandTypeException(String type){
        super("Invalid command type " + type);
    }

}
