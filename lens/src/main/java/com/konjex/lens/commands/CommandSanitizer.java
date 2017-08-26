package com.konjex.lens.commands;

import com.konjex.lens.commands.exceptions.InvalidCommandNameException;

/**
 * Class for sanitizing user inputted command strings.
 */
class CommandSanitizer {

    static String sanitize(String input) throws InvalidCommandNameException {
        StringBuilder output = new StringBuilder("");
        int index = 0;
        char[] chars = input.toCharArray();

        while(index < chars.length - 1 && !charIsValidForCommand(chars[index])){
            index ++;
        }

        boolean hitSpace = false;
        for(; index < chars.length; index ++){
            char c = chars[index];
            if(charIsValidForCommand(c)){
                hitSpace = false;
                output.append(c);
            }
            else if(c == ' '){
                if(!hitSpace){
                    hitSpace = true;
                    output.append(c);
                }
            }
            else{
                throw new InvalidCommandNameException(c);
            }
        }

        return output.toString();
    }

    private static boolean charIsValidForCommand(char c){
        return c > 64 && c < 91; // c in [A, Z]
    }

}
