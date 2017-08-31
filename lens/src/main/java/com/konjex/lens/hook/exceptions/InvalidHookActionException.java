package com.konjex.lens.hook.exceptions;

/**
 * Exception thrown when a key hook is given an invalid action.
 */
public class InvalidHookActionException extends Exception {

    public InvalidHookActionException(){
        super("Hook action must be defined");
    }

    public InvalidHookActionException(String action){
        super("Invalid hook action " + action);
    }

}
