package com.konjex.lens.hook;

import com.konjex.util.provide.ProvidableNotFoundException;
import com.konjex.util.provide.Provider;
import org.apache.log4j.Logger;

import java.util.stream.Stream;

/**
 * Class for storing and providing
 */
class KeyHookProvider {

    private static final Logger log = Logger.getLogger(KeyHookProvider.class);

    private static Provider<Integer, KeyHook> keyHookProvider = new Provider<>();

    static void addKeyHooks(Stream<KeyHook> keyHooks){
        keyHookProvider.store(keyHooks);
    }

    static void addKeyHook(KeyHook keyHook){
        log.info("Adding key hook for " + keyHook.getAction() + " with keyCode " + keyHook.getCode());
        keyHookProvider.store(keyHook);
    }

    static KeyHook get(int keyCode) throws ProvidableNotFoundException {
        return keyHookProvider.request(keyCode);
    }

}
