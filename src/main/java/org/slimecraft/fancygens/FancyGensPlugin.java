package org.slimecraft.fancygens;

import org.bukkit.plugin.java.JavaPlugin;
import org.slimecraft.bedrock.annotation.plugin.Plugin;
import org.slimecraft.fancygens.argument.BlockArgument;
import org.slimecraft.fancygens.argument.GenArgument;
import org.slimecraft.funmands.paper.PaperFunmandsManager;

@Plugin("fancy-gens")
public class FancyGensPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        PaperFunmandsManager manager = new PaperFunmandsManager(this.getLifecycleManager());
        manager.getArgumentRegistry().register("block", new BlockArgument());
        GenManager genManager = new GenManager(this);
        manager.registerCommand(new GenCommand(genManager));
        manager.getArgumentRegistry().register("gen", new GenArgument(genManager));
        new Listeners().init();
    }
}
