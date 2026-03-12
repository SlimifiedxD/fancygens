package org.slimecraft.fancygens;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.slimecraft.bedrock.event.EventNode;
import org.slimecraft.fancygens.dto.Pos;

public class Listeners {
    public void init() {
        selectionWandHandler();
    }

    private void selectionWandHandler() {
        EventNode.global().addListener(PlayerInteractEvent.class, event -> {
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!item.getPersistentDataContainer().has(Items.SELECTION_WAND_KEY)) return;
            Block block = event.getClickedBlock();
            if (block == null) return;
            Player player = event.getPlayer();
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                PlayerManager.setLeftClickPos(player, Pos.fromLocation(block.getLocation()));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Set pos 1!"));
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                PlayerManager.setRightClickPos(player, Pos.fromLocation(block.getLocation()));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Set pos 2!"));
            }
        });
    }
}
