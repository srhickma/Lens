package com.konjex.lens.app.events;

import com.google.common.collect.Maps;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

import java.util.Map;

/**
 * Class for handling ui updating events.
 */
public class LensEventManager {

    private static Map<Class<? extends LensEvent>, EventType<? extends LensEvent>> typeMap = Maps.newHashMap();

    private static Node target;

    public static void init(Node targetNode){
        target = targetNode;
    }

    public static void fire(LensEvent event){
        target.fireEvent(event);
    }

    public static void listen(Class<? extends LensEvent> clazz, EventHandler<LensEvent> handler){
        target.addEventHandler(getEventType(clazz), handler);
    }

    @SuppressWarnings("unchecked")
    static <T extends LensEvent> EventType<T> getEventType(Class<? extends LensEvent> clazz){
        return (EventType<T>)typeMap.computeIfAbsent(clazz, (c) -> new EventType<T>(clazz.getName()));
    }

}
