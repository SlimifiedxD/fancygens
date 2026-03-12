package org.slimecraft.fancygens;

import org.bukkit.plugin.java.JavaPlugin;
import org.slimecraft.bedrock.annotation.plugin.Dependency;
import org.slimecraft.bedrock.annotation.plugin.Plugin;
import org.slimecraft.fancygens.argument.BlockArgument;
import org.slimecraft.funmands.paper.PaperFunmandsManager;

import java.io.IOException;
import java.nio.file.Files;

@Plugin(value = "fancy-gens", dependencies = {
        @Dependency("FastAsyncWorldEdit")
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
    }
}
