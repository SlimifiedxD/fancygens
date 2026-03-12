package org.slimecraft.fancygens;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.slimecraft.bedrock.internal.Bedrock;
import org.slimecraft.fancygens.dto.Generator;
import org.slimecraft.fancygens.dto.Pos;
import org.slimecraft.funmands.paper.PaperContext;
import org.slimecraft.funmands.paper.PaperPreContext;
import org.slimecraft.funmands.paper.command.PaperCommand;

import java.util.Optional;
import java.util.function.Consumer;

public class GenCommand extends PaperCommand {
    public GenCommand(GenManager manager) {
        super("gen");

        addFormat("create name:string", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            Optional<Pos> optOne = PlayerManager.getLeftClickPos(player);
            if (optOne.isEmpty()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You did not set position 1!"));
                return;
            }
            Optional<Pos> optTwo = PlayerManager.getRightClickPos(player);
            if (optTwo.isEmpty()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You did not set position 2!"));
                return;
            }
            Pos one = optOne.get();
            Pos two = optTwo.get();
            if (!one.world().equals(two.world())) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The two locations must be within the same world!"));
                return;
            }
            manager.addGenerator(new Generator(ctx.get("name"), one, two, Material.DIRT, 0));
        }, pre -> {
            setPermsOnPre(pre);
            pre.addOptions("name", StringArgumentType.StringType.SINGLE_WORD);
        });

        addPreFormat("delete name:string", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            boolean didGenOfNameExist = manager.deleteGenerator(name);
            if (!didGenOfNameExist) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No gen of that name exists!"));
                return;
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Gen was deleted!"));
        });

        addPreFormat("wand", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            player.getInventory().addItem(Items.selectionWand().get());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Gave you the wand!"));
        });

        addFormat("edit name:string block:block", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            BlockState state = ctx.get("block");
            manager.modifyGeneratorBlockType(name, state.getType());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Modified block of the gen!"));
        }, pre -> {
            setPermsOnPre(pre);
            pre.addOptions("name", StringArgumentType.StringType.SINGLE_WORD);
        });

        addFormat("edit name:string respawn_cooldown:int", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            int respawnCooldown = ctx.get("respawn_cooldown");
            manager.modifyGeneratorRespawnCooldown(name, respawnCooldown);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Modified respawn cooldown of the gen!"));
        }, pre -> {
            setPermsOnPre(pre);
            pre.addOptions("name", StringArgumentType.StringType.SINGLE_WORD);
        });
    }

    private void addPreFormat(String identifier, Consumer<PaperContext> ctxConsumer) {
        addFormat(identifier, ctxConsumer, this::setPermsOnPre);
    }

    private void setPermsOnPre(PaperPreContext preContext) {
        preContext.setPredicate(s ->
                s.getSender() instanceof Player player &&
                player.hasPermission("fancygens.commands"));
    }
}
