package com.konjex.lens.commands;


import com.konjex.util.DoublyLinkedList;
import com.konjex.util.LinkedListNode;
import org.apache.log4j.Logger;

/**
 * Created by shane on 23/08/17.
 */
public class CommandRunner {

    private static Logger log = Logger.getLogger(CommandRunner.class);

    private static final int MAX_HISTORY_SIZE = 64;
    private DoublyLinkedList<String> history = new DoublyLinkedList<>();
    private LinkedListNode<String> currentCommand;

    public CommandRunner(){
        history.addFirst("");
        currentCommand = history.getFirst();
    }

    public void run(String inputText){
        log.info("Parsing command from: " + inputText);
        history.getFirst().setValue(inputText);
        history.addFirst("");
        if(history.size() > MAX_HISTORY_SIZE){
            history.removeLast();
        }
    }

    public String shiftToFirst(){
        currentCommand = history.getFirst();
        return history.getFirst().getValue();
    }

    public String shiftDown(){
        if(currentCommand.hasPrev()){
            currentCommand = currentCommand.getPrev();
        }
        return currentCommand.getValue();
    }

    public String shiftUp(){
        if(currentCommand.hasNext()){
            currentCommand = currentCommand.getNext();
        }
        return currentCommand.getValue();
    }


}
