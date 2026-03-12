package org.slimecraft.fancygens.dto;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;

import java.util.UUID;

public record Pos(UUID world, int x, int y, int z) {
    public static Pos fromLocation(Location location) {
        return new Pos(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockVector3 toBlockVector3() {
        return BlockVector3.at(x, y, z);
    }
}
