package org.slimecraft.fancygens.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.block.BlockState;
import org.slimecraft.funmands.api.argument.Argument;

public class BlockArgument implements Argument<ArgumentType<BlockState>> {
    @Override
    public ArgumentType<BlockState> create(Object[] objects) {
        return ArgumentTypes.blockState();
    }
}
