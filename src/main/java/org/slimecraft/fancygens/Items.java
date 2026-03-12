package org.slimecraft.fancygens;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.slimecraft.bedrock.util.item.ItemBuilder;

import java.util.function.Supplier;

public class Items {
    public static final NamespacedKey SELECTION_WAND_KEY = new NamespacedKey("fancy_gens", "selection_wand");
    private Items() {

    }

    public static Supplier<ItemStack> selectionWand() {
        return () -> ItemBuilder.create()
                .material(Material.GOLDEN_AXE)
                .name("Selection Wand")
                .pdc(SELECTION_WAND_KEY, PersistentDataType.BOOLEAN, true)
                .build();
    }
}
