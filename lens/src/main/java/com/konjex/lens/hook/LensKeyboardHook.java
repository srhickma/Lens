package com.konjex.lens.hook;

import com.konjex.lens.app.LensApplication;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.LogManager;

/**
 * Global keyboard hook for launching and focusing the LensApplication.
 */
public class LensKeyboardHook implements NativeKeyListener {

    private static final Logger log = Logger.getLogger(LensKeyboardHook.class);

    public void nativeKeyPressed(NativeKeyEvent event){

    }

    public void nativeKeyReleased(NativeKeyEvent event){

    }

    public void nativeKeyTyped(NativeKeyEvent event){

    }


    public static void main(String[] args){
        silenceJNativeHook();

        BasicConfigurator.configure();
        try{
            GlobalScreen.registerNativeHook();
            log.info("Successfully registered native hook.");
            GlobalScreen.addNativeKeyListener(new LensKeyboardHook());
        }
        catch(NativeHookException e){
            log.error("There was a problem registering the native hook. Launching lens anyways.", e);
            System.exit(1);
        }

        LensApplication.start(args);
    }

    public static void silenceJNativeHook(){
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
    }

}
