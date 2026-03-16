package org.slimecraft.fancygens.dto;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record Generator(String name, Pos one, Pos two, Material type, @SerializedName("respawn_cooldown") long respawnCooldown, boolean hologram) {
    public Generator withType(@NotNull Material type) {
        return new Generator(name, one, two, type, respawnCooldown, hologram);
    }

    public Generator withRespawnCooldown(int respawnCooldown) {
        return new Generator(name, one, two, type, respawnCooldown, hologram);
    }

    public Generator withHologram(boolean hologram) {
        return new Generator(name, one, two, type, respawnCooldown, hologram);
    }
}
