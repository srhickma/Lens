package com.konjex.lens.app.ui;

import javafx.scene.control.Label;
import org.reactfx.EventStreams;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

/**
 * Helper class to automatically update a label with the current date and time.
 */
public class DateTimeProvider {

    private static final Duration updateInterval = Duration.ofSeconds(5);
    private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private Label dateTimeLabel;

    private DateTimeProvider(Label dateTimeLabel){
        this.dateTimeLabel = dateTimeLabel;
    }

    public static void registerTo(Label dateTimeLabel){
        final DateTimeProvider provider = new DateTimeProvider(dateTimeLabel);
        provider.update();
        EventStreams.ticks(updateInterval)
                .subscribe(tick -> provider.update());
    }

    private void update(){
        dateTimeLabel.setText(dateTimeFormat.format(new Date()));
    }

}
