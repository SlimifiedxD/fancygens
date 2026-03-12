package org.slimecraft.fancygens;

import org.bukkit.entity.Player;
import org.slimecraft.fancygens.dto.Pos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerManager {
    private static final Map<UUID, Pos> LEFT_CLICK_POS = new HashMap<>();
    private static final Map<UUID, Pos> RIGHT_CLICK_POS = new HashMap<>();

    private PlayerManager() {

    }

    public static void setLeftClickPos(Player player, Pos pos) {
        LEFT_CLICK_POS.put(player.getUniqueId(), pos);
    }

    public static void setRightClickPos(Player player, Pos pos) {
        RIGHT_CLICK_POS.put(player.getUniqueId(), pos);
    }

    public static Optional<Pos> getLeftClickPos(Player player) {
        return Optional.ofNullable(LEFT_CLICK_POS.get(player.getUniqueId()));
    }

    public static Optional<Pos> getRightClickPos(Player player) {
        return Optional.ofNullable(RIGHT_CLICK_POS.get(player.getUniqueId()));
    }
}
