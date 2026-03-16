package org.slimecraft.fancygens;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.resolver.GlobalTagResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.plugin.java.JavaPlugin;
import org.slimecraft.bedrock.annotation.plugin.Dependency;
import org.slimecraft.bedrock.annotation.plugin.Plugin;
import org.slimecraft.fancygens.argument.BlockArgument;
import org.slimecraft.fancygens.dto.Generator;
import org.slimecraft.funmands.paper.PaperFunmandsManager;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Plugin(value = "fancy-gens", dependencies = {
        @Dependency("FastAsyncWorldEdit"),
        @Dependency("MiniPlaceholders"),
        @Dependency("FancyHolograms")
})
public class FancyGensPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            Files.createDirectories(getDataPath().resolve("gens"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PaperFunmandsManager manager = new PaperFunmandsManager(this.getLifecycleManager());
        manager.getArgumentRegistry().register("block", new BlockArgument());
        GenManager genManager = new GenManager(this);
        manager.registerCommand(new GenCommand(genManager));
        new Listeners().init();
        Expansion.builder("fancygens")
                .globalPlaceholder("timeremaining", (queue, ctx) -> {
                    String name = queue.popOr("gen name argument not provided").value();
                    Optional<Generator> optGen = genManager.findByName(name);
                    if (optGen.isEmpty()) {
                        return Tag.selfClosingInserting(Component.text("gen of name '" + name + "' doesn't exist"));
                    }
                    Generator gen = optGen.get();
                    long timeRemaining = genManager.getTimeUntilRespawn(gen);
                    return Tag.selfClosingInserting(Component.text(GenManager.turnSecondsIntoPrettyString(timeRemaining)));
                }).build().register();
    }
}
