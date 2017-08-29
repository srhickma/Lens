package com.jediterm.terminal.model;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by shane on 22/08/17.
 */
public class DefaultTabulator implements Tabulator {

    private static final int TAB_LENGTH = 8;

    private final SortedSet<Integer> myTabStops;

    private int width;

    public DefaultTabulator(int width) {
        myTabStops = new TreeSet<>();
        this.width = width;
        initTabStops(width);
    }

    private void initTabStops(int columns) {
        for(int i = TAB_LENGTH; i < columns; i += TAB_LENGTH){
            myTabStops.add(i);
        }
    }

    public void resize(int columns){
        if(columns > width){
            for(int i = TAB_LENGTH * (width / TAB_LENGTH); i < columns; i += TAB_LENGTH){
                if(i >= width){
                    myTabStops.add(i);
                }
            }
        }
        else{
            myTabStops.removeIf(i -> i > columns);
        }

        width = columns;
    }

    public void clearTabStop(int position) {
        myTabStops.remove(position);
    }

    public void clearAllTabStops() {
        myTabStops.clear();
    }

    public int getNextTabWidth(int position) {
        return nextTab(position) - position;
    }

    public int getPreviousTabWidth(int position) {
        return position - previousTab(position);
    }

    public int nextTab(int position) {
        int tabStop = Integer.MAX_VALUE;

        // Search for the first tab stop after the given position...
        SortedSet<Integer> tailSet = myTabStops.tailSet(position + 1);
        if (!tailSet.isEmpty()) {
            tabStop = tailSet.first();
        }

        // Don't go beyond the end of the line...
        return Math.min(tabStop, (width - 1));
    }

    public int previousTab(int position) {
        int tabStop = 0;

        // Search for the first tab stop before the given position...
        SortedSet<Integer> headSet = myTabStops.headSet(position);
        if (!headSet.isEmpty()) {
            tabStop = headSet.last();
        }

        // Don't go beyond the start of the line...
        return Math.max(0, tabStop);
    }

    public void setTabStop(int position) {
        myTabStops.add(position);
    }

}
