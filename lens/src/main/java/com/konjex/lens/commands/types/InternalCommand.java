package com.konjex.lens.commands.types;

import com.konjex.lens.commands.Command;
import com.konjex.lens.commands.exceptions.InvalidCommandNameException;

/**
 * Command internal to lens.
 */
public class InternalCommand extends Command {

    public InternalCommand(String name) throws InvalidCommandNameException {
        super(name, CommandType.INTERNAL);
    }

    public void run(){

    }

}
