package com.konjex.lens.conf.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.konjex.lens.commands.Command;
import com.konjex.lens.commands.exceptions.InvalidCommandNameException;

/**
 * Yaml representation of a command.
 */
public class YamlCommand {

    @JsonProperty
    private String name;

    public void setName(String name){
        this.name = name;
    }

    public Command parse() throws InvalidCommandNameException {
        return new Command(name);
    }

}
