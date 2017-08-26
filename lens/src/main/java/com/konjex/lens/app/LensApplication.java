package com.konjex.lens.app;

import javafx.application.Application;
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
        log.info("Launching lens application" + getClass().getPackage().toString());
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("LENS");
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();

        log.info("Lens successfully launched");
    }


    public static void main(String[] args){
        BasicConfigurator.configure();
        launch(args);
    }
}
