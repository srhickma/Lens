package com.konjex.lens.app;

import com.konjex.lens.app.commands.CommandLoader;
import com.konjex.lens.hook.HookConfigLoader;
import com.konjex.lens.hook.LensKeyboardHook;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LensApplication extends Application {

    private static final Logger log = Logger.getLogger(LensApplication.class);

    @Override
    public void start(Stage stage) throws Exception{
        LensKeyboardHook.init(stage);
        log.info("Launching lens application");
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("LENS");
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();

        log.info("Lens successfully launched");

        CommandLoader.load();
        HookConfigLoader.load();
    }

    public static void start(String[] args){
        launch(args);
    }

    public static void main(String[] args){
        BasicConfigurator.configure();
        start(args);
    }
}
