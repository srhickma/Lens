package com.konjex.lens.app.controllers;

import com.konjex.lens.commands.CommandRunner;
import com.konjex.lens.cores.terminal.LensTerminal;
import com.konjex.lens.ui.DateTimeProvider;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final Logger log = Logger.getLogger(Controller.class);

    private final CommandRunner commandRunner = new CommandRunner();

    @FXML
    private TextField lens;

    @FXML
    private SwingNode terminal;

    @FXML
    private Label dateTime;

    @FXML
    private void setFocus(){
        terminal.requestFocus();
    }

    @FXML
    private void globalKeyPressed(KeyEvent event){
        if(event.getCode() == KeyCode.ESCAPE) {
            lens.requestFocus();
            lens.end();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        initializeLens();
        initializeJediTerm();

        DateTimeProvider.registerTo(dateTime);
    }

    private void initializeLens(){
        lens.textProperty().addListener((ov, old, newValue) -> lens.setText(newValue.toUpperCase()));
        lens.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            boolean matched = true;
            switch(event.getCode()){
                case BACK_SLASH:
                    terminal.requestFocus();
                    break;
                case ENTER:
                    commandRunner.run(lens.getText());
                    lens.setText(commandRunner.shiftToFirst());
                    break;
                case UP:
                    lens.setText(commandRunner.shiftUp());
                    lens.end();
                    break;
                case DOWN:
                    lens.setText(commandRunner.shiftDown());
                    lens.end();
                    break;
                case TAB:
                    lens.end();
                    break;
                default:
                    matched = false;
            }
            if(matched){
                event.consume();
            }
        });
        lens.requestFocus();
    }

    private void initializeJediTerm(){
        SwingUtilities.invokeLater(() -> {
            LensTerminal lensTerminal = new LensTerminal();
            lensTerminal.getTerminalPanel().setTerminalPanelListener((pixelDimension, origin) -> {});
            lensTerminal.openSession();

            terminal.setContent(lensTerminal.getTerminalPanel());
        });
    }

}
