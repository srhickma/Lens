package com.konjex.lens.conf.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.konjex.lens.commands.Command;
import com.konjex.lens.commands.exceptions.InvalidCommandNameException;
import com.konjex.lens.commands.exceptions.InvalidCommandTypeException;
import com.konjex.lens.commands.types.CommandType;
import com.konjex.lens.commands.types.RunCommand;
import com.konjex.lens.commands.types.ShellCommand;

/**
 * Yaml representation of a command.
 */
public class YamlCommand {

    @JsonProperty
    private String name;

    @JsonProperty
    private String type;

    @JsonProperty
    private Object params;

    public void setName(String name){
        this.name = name;
    }

    public void setType(String type){
        this.type = type.toUpperCase();
    }

    public void setParams(Object params){
        this.params = params;
    }

    Command parse() throws InvalidCommandNameException, InvalidCommandTypeException {
        if(type == null){
            throw new InvalidCommandTypeException();
        }
        final CommandType commandType;
        try{
            commandType = CommandType.valueOf(type);
        }
        catch(IllegalArgumentException e){
            throw new InvalidCommandTypeException();
        }

        switch(commandType){
            case GROUP:
                return null;
            case RUN:
                String[] commandAndArgs = (String[])params;
                return new RunCommand(name, commandAndArgs);
            case SHELL:
                String command = (String)params;
                return new ShellCommand(name, command);
            default:
                return null;
        }
    }

}
