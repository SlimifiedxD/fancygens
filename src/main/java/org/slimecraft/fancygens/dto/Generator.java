package org.slimecraft.fancygens.dto;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;

public record Generator(String name, Pos one, Pos two, Material type, @SerializedName("respawn_cooldown") int respawnCooldown) {
}
