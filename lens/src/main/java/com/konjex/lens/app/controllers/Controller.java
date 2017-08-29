package com.konjex.lens.app.controllers;

import com.konjex.lens.app.events.LensEventManager;
import com.konjex.lens.app.events.ProcessCreatedEvent;
import com.konjex.lens.commands.LensCommandRunner;
import com.konjex.lens.cores.terminal.LensTerminal;
import com.konjex.lens.ui.DateTimeProvider;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final Logger log = Logger.getLogger(Controller.class);

    private final LensCommandRunner lensCommandRunner = new LensCommandRunner();

    @FXML
    private BorderPane frame;

    @FXML
    private TextField lens;

    @FXML
    private SwingNode terminal;

    @FXML
    private Label dateTime;

    @FXML
    private ToolBar processList;

    @FXML
    private void setFocus(){
        terminal.requestFocus();
    }

    @FXML
    private void globalKeyPressed(KeyEvent event){
        if(event.getCode() == KeyCode.ESCAPE){
            lens.requestFocus();
            lens.end();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        initializeLens();
        initializeJediTerm();
        initializeEventHandlers();

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
                    lensCommandRunner.run(lens.getText());
                    lens.setText(lensCommandRunner.shiftToFirst());
                    break;
                case UP:
                    lens.setText(lensCommandRunner.shiftUp());
                    lens.end();
                    break;
                case DOWN:
                    lens.setText(lensCommandRunner.shiftDown());
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

    private void initializeEventHandlers(){
        LensEventManager.init(this.frame);

        LensEventManager.listen(ProcessCreatedEvent.class, event -> {
            ProcessCreatedEvent pce = (ProcessCreatedEvent)event;
            Label description = new Label(pce.getDescription());
            description.getStyleClass().add("process-label");
            processList.getItems().add(description);
        });
    }

}
