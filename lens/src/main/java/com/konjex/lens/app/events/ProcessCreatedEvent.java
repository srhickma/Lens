package com.konjex.lens.app.events;

/**
 * Event fired when a shell process is launched.
 */
public class ProcessCreatedEvent extends LensEvent {

    private final String name;
    private final int num;

    public ProcessCreatedEvent(String name, int num){
        super(ProcessCreatedEvent.class);
        this.name = name;
        this.num = num;
    }

    public String getDescription(){
        return name + " " + num;
    }

}
