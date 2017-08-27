package com.konjex.lens.app.events;

import javafx.event.Event;

/**
 * Object representing a lens ui event.
 */
public abstract class LensEvent extends Event {

    LensEvent(Class<? extends LensEvent> subClass){
        super(LensEventManager.getEventType(subClass));
    }

}
