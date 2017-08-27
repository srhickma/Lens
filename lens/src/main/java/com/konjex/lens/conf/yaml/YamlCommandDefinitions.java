package com.konjex.lens.conf.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.konjex.lens.commands.Command;
import com.konjex.lens.commands.exceptions.InvalidCommandNameException;
import com.konjex.lens.commands.exceptions.InvalidCommandTypeException;

import java.util.List;
import java.util.stream.Stream;

/**
 * Yaml representation of a command definitions file.
 */
public class YamlCommandDefinitions {

    @JsonProperty
    private List<YamlCommand> commands;

    public void setCommands(List<YamlCommand> commands){
        this.commands = commands;
    }

    public Stream<Command> parseToStream() throws RuntimeException {
        return commands.stream().map(this::parseCommand);
    }

    private Command parseCommand(YamlCommand yamlCommand){
        try{
            return yamlCommand.parse();
        }
        catch(InvalidCommandNameException | InvalidCommandTypeException e){
            throw new RuntimeException(e);
        }
    }

}
