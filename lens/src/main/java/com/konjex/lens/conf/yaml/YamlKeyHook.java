package com.konjex.lens.conf.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.konjex.lens.hook.HookAction;
import com.konjex.lens.hook.exceptions.InvalidHookActionException;
import com.konjex.lens.hook.KeyHook;

/**
 * Yaml representation of a KeyHook.
 */
public class YamlKeyHook {

    @JsonProperty
    private Integer code;

    @JsonProperty
    private String action;

    public void setCode(Integer code){
        this.code = code;
    }

    public void setAction(String action){
        this.action = action.toUpperCase();
    }

    KeyHook parse() throws InvalidHookActionException {
        if(action == null){
            throw new InvalidHookActionException();
        }
        final HookAction hookAction;
        try{
            hookAction = HookAction.valueOf(action);
        }
        catch(IllegalArgumentException e){
            throw new InvalidHookActionException(action);
        }

        return new KeyHook(code, hookAction);
    }

}
