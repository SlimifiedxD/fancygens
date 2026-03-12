package org.slimecraft.fancygens.argument;

import org.slimecraft.fancygens.GenManager;
import org.slimecraft.funmands.api.argument.Argument;

public class GenArgument implements Argument<GenArgumentType> {
    private final GenManager manager;

    public GenArgument(GenManager manager) {
        this.manager = manager;
    }

    @Override
    public GenArgumentType create(Object[] objects) {
        return new GenArgumentType(manager);
    }
}
