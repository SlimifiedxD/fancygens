package org.slimecraft.fancygens;

import com.mojang.brigadier.arguments.StringArgumentType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.slimecraft.bedrock.internal.Bedrock;
import org.slimecraft.fancygens.dto.Generator;
import org.slimecraft.fancygens.dto.Pos;
import org.slimecraft.funmands.paper.command.PaperCommand;

import java.util.Optional;

public class GenCommand extends PaperCommand {
    public GenCommand(GenManager manager) {
        super("gen");

        addFormat("create string:name", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            Optional<Pos> optOne = PlayerManager.getLeftClickPos(player);
            if (optOne.isEmpty()) {
                player.sendMessage(Bedrock.bedrock().getMiniMessage().deserialize("<red>You did not set position 1!"));
                return;
            }
            Optional<Pos> optTwo = PlayerManager.getRightClickPos(player);
            if (optTwo.isEmpty()) {
                player.sendMessage(Bedrock.bedrock().getMiniMessage().deserialize("<red>You did not set position 2!"));
                return;
            }
            Pos one = optOne.get();
            Pos two = optTwo.get();
            if (!one.world().equals(two.world())) {
                player.sendMessage(Bedrock.bedrock().getMiniMessage().deserialize("<red>The two locations must be within the same world!"));
                return;
            }
            manager.addGenerator(new Generator(ctx.get("name"), one, two, Material.DIRT, 0));
        }, pre -> {
            pre.addOptions("name", StringArgumentType.StringType.SINGLE_WORD);
        });

        addFormat("delete gen:gen", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            Generator gen = ctx.get("gen");
            boolean didGenOfNameExist = manager.deleteGenerator(gen.name());
            if (!didGenOfNameExist) {
                player.sendMessage(Bedrock.bedrock().getMiniMessage().deserialize("<red>No gen of that name exists!"));
                return;
            }
            player.sendMessage(Bedrock.bedrock().getMiniMessage().deserialize("<green>Gen was deleted!"));
        });

        addFormat("wand", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            player.getInventory().addItem(Items.selectionWand().get());
            player.sendMessage(Bedrock.bedrock().getMiniMessage().deserialize("<green>Gave you the wand!"));
        });
    }
}
