package com.konjex.lens.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.konjex.lens.commands.exceptions.InvalidCommandNameException;

/**
 * Object representing an executable command.
 */
public class Command {

    @JsonProperty
    private final String name;

    public Command(String name) throws InvalidCommandNameException {
        this.name = CommandSanitizer.sanitize(name.toUpperCase());
    }

    void run(){
        //execute the command
    }

    public String getName(){
        return name;
    }

    public boolean equals(Command command){
        return name.equals(command.name);
    }

}
