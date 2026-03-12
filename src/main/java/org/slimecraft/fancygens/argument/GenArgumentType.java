package org.slimecraft.fancygens.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import org.slimecraft.fancygens.GenManager;
import org.slimecraft.fancygens.dto.Generator;

public class GenArgumentType implements CustomArgumentType.Converted<@NotNull Generator, @NotNull String> {
    private final GenManager manager;

    public GenArgumentType(GenManager manager) {
        this.manager = manager;
    }

    @Override
    public Generator convert(String nativeType) throws CommandSyntaxException {
        return manager.findByName(nativeType).orElseThrow(() -> new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("no generator exists of name: " + nativeType)), new LiteralMessage("no generator exists of name: " + nativeType)));
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
