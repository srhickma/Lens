package com.konjex.lens.hook;

import com.konjex.lens.app.LensApplication;
import com.konjex.util.provide.ProvidableNotFoundException;
import javafx.application.Platform;
import javafx.stage.Stage;
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

    private static Stage focusableStage;

    public static void init(Stage mainStage){
        focusableStage = mainStage;
    }

    private void focusLens() {
        Platform.runLater(() -> {
            focusableStage.requestFocus();
            focusableStage.show();
        });
    }

    public void nativeKeyPressed(NativeKeyEvent event){
        try{
            switch(KeyHookProvider.get(event.getKeyCode()).getAction()){
                case FOCUS:
                    focusLens();
            }
        }
        catch(ProvidableNotFoundException e){

        }
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
        }

        LensApplication.start(args);
    }

    private static void silenceJNativeHook(){
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
    }

}
