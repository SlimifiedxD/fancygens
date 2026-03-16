package org.slimecraft.fancygens;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.slimecraft.bedrock.internal.Bedrock;
import org.slimecraft.fancygens.dto.Generator;
import org.slimecraft.fancygens.dto.Pos;
import org.slimecraft.funmands.api.Suggestion;
import org.slimecraft.funmands.paper.PaperContext;
import org.slimecraft.funmands.paper.PaperPreContext;
import org.slimecraft.funmands.paper.command.PaperCommand;

import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GenCommand extends PaperCommand {
    private final GenManager manager;

    public GenCommand(GenManager manager) {
        super("gen");
        this.manager = manager;

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
            manager.addGenerator(new Generator(ctx.get("name"), one, two, Material.DIRT, 0, false));
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Created generator!"));
        }, pre -> {
            setPermsOnPre(pre);
            pre.addOptions("name", StringArgumentType.StringType.SINGLE_WORD);
        });

        addFormat("delete name:string", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            boolean didGenOfNameExist = manager.deleteGenerator(name);
            if (!didGenOfNameExist) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No gen of that name exists!"));
                return;
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Gen was deleted!"));
        }, this::setPermsAndAutoGenCompleteOnPre);

        addPreFormat("wand", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            player.getInventory().addItem(Items.selectionWand().get());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Gave you the wand!"));
        });

        addFormat("edit name:string block block:block", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            BlockState state = ctx.get("block");
            manager.modifyGeneratorBlockType(name, state.getType());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Modified block of the gen!"));
        }, this::setPermsAndAutoGenCompleteOnPre);

        addFormat("edit name:string respawn_cooldown respawn_cooldown:int", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            int respawnCooldown = ctx.get("respawn_cooldown");
            manager.modifyGeneratorRespawnCooldown(name, respawnCooldown);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Modified respawn cooldown of the gen!"));
        }, this::setPermsAndAutoGenCompleteOnPre);

        addFormat("edit name:string hologram hologram:bool", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            boolean hologram = ctx.get("hologram");
            manager.modifyGeneratorHologram(name, hologram);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Modified hologram of the gen!"));
        }, this::setPermsAndAutoGenCompleteOnPre);

        addFormat("list", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            for (int i = 0; i < manager.getAllGenerators().size(); i++) {
                Generator gen = manager.getAllGenerators().get(i);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green><pos> <name>", TagResolver.resolver("pos", Tag.selfClosingInserting(Component.text(i + 1))), TagResolver.resolver("name", Tag.selfClosingInserting(Component.text(gen.name())))));
            }
        }, this::setPermsOnPre);

        addFormat("info name:string", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            manager.findByName(name).ifPresentOrElse(gen -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("Name: <name>", TagResolver.resolver("name", Tag.selfClosingInserting(Component.text(name)))));
                player.sendMessage(MiniMessage.miniMessage().deserialize("Respawn Cooldown: <cooldown>", TagResolver.resolver("cooldown", Tag.selfClosingInserting(Component.text(gen.respawnCooldown())))));
                player.sendMessage(MiniMessage.miniMessage().deserialize("Time Until Respawn: <time>", TagResolver.resolver("time", Tag.selfClosingInserting(Component.text(GenManager.turnSecondsIntoPrettyString(manager.getTimeUntilRespawn(gen)))))));
            }, () -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No generator of that name exists!"));
            });
        }, this::setPermsAndAutoGenCompleteOnPre);

        addFormat("reset name:string", ctx -> {
            if (!(ctx.getSource().getSender() instanceof final Player player)) return;
            String name = ctx.get("name");
            manager.findByName(name).ifPresentOrElse(gen -> {
                manager.modifyGeneratorBlockType(name, gen.type());
            }, () -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No generator of that name exists!"));
            });
        }, this::setPermsAndAutoGenCompleteOnPre);
    }

    private void addPreFormat(String identifier, Consumer<PaperContext> ctxConsumer) {
        addFormat(identifier, ctxConsumer, this::setPermsOnPre);
    }

    private void setPermsAndAutoGenCompleteOnPre(PaperPreContext preContext) {
        setPermsOnPre(preContext);
        preContext.addOptions("name", StringArgumentType.StringType.SINGLE_WORD);
        preContext.addSuggestions("name", source -> {
            return manager.getAllGenerators().stream().map(Generator::name).map(Suggestion::new).collect(Collectors.toSet());
        });
    }

    private void setPermsOnPre(PaperPreContext preContext) {
        preContext.setPredicate(s ->
                s.getSender() instanceof Player player &&
                player.hasPermission("fancygens.commands"));
    }
}
