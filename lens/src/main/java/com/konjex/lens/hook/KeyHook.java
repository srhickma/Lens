package com.konjex.lens.hook;

import com.konjex.util.provide.Providable;

/**
 * Object representing a key to be hooked.
 */
public class KeyHook implements Providable<Integer> {

    private final int code;
    private final HookAction action;

    public KeyHook(int code, HookAction action){
        this.code = code;
        this.action = action;
    }

    public int getCode(){
        return code;
    }

    public HookAction getAction(){
        return action;
    }

    public Integer getProviderKey(){
        return code;
    }

}
