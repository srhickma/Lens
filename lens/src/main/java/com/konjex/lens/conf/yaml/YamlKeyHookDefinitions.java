package com.konjex.lens.conf.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.konjex.lens.hook.KeyHook;
import com.konjex.lens.hook.exceptions.InvalidHookActionException;

import java.util.List;
import java.util.stream.Stream;

/**
 * Yaml representation of a key hook definitions file.
 */
public class YamlKeyHookDefinitions {

    @JsonProperty
    private List<YamlKeyHook> hooks;

    public void setHooks(List<YamlKeyHook> hooks){
        this.hooks = hooks;
    }

    public Stream<KeyHook> parseToStream() throws RuntimeException {
        return hooks.stream().map(this::parseKeyHook);
    }

    private KeyHook parseKeyHook(YamlKeyHook yamlKeyHook){
        try{
            return yamlKeyHook.parse();
        }
        catch(InvalidHookActionException e){
            throw new RuntimeException(e);
        }
    }

}
