package org.slimecraft.fancygens;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
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
            EquipmentSlot hand = event.getHand();
            if (hand == EquipmentSlot.HAND) {
                PlayerManager.setLeftClickPos(player, Pos.fromLocation(block.getLocation()));
            } else if (hand == EquipmentSlot.OFF_HAND) {
                PlayerManager.setRightClickPos(player, Pos.fromLocation(block.getLocation()));
            }
        });
    }
}
