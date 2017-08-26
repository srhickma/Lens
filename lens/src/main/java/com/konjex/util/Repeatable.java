package com.konjex.util;

/**
 * Created by shane on 23/08/17.
 */
public class Repeatable {

    public static void invoke(Runnable runnable, int iterations){
        for(int i = 0; i < iterations; i ++){
            runnable.run();
        }
    }

}
