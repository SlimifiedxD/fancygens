package org.slimecraft.fancygens;

import com.google.gson.Gson;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slimecraft.bedrock.task.Task;
import org.slimecraft.bedrock.util.Ticks;
import org.slimecraft.fancygens.dto.Generator;
import org.slimecraft.fancygens.dto.Pos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GenManager {
    private final Plugin plugin;
    private final Path gensPath;
    private final List<Generator> generators;
    private final Gson gson = new Gson();
    private final Map<Generator, Long> timeUntilRespawn = new HashMap<>();
    private final Map<Generator, List<Block>> genBlocks = new HashMap<>();
    private final Map<Generator, Hologram> genHolos = new HashMap<>();

    public GenManager(Plugin plugin) {
        this.plugin = plugin;
        gensPath = plugin.getDataPath().resolve("gens");
        generators = initListFromFiles();
    }

    public void addGenerator(Generator gen) {
        try {
            generators.add(gen);
            Path filePath = gensPath.resolve(gen.name().toLowerCase() + ".json");
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                gson.toJson(gen, writer);
                taskForGen(gen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param name The name of the generator
     * @return If a generator of that name existed or not
     */
    public boolean deleteGenerator(String name) {
        Optional<Generator> optGen = findByName(name);
        if (optGen.isEmpty()) {
            return false;
        }
        Generator gen = optGen.get();
        generators.remove(gen);
        genBlocks.remove(gen);
        File file = gensPath.resolve(name.toLowerCase() + ".json").toFile();
        file.delete();
        return true;
    }

    public Optional<Generator> findByName(String name) {
        return generators.stream().filter(g -> g.name().equalsIgnoreCase(name)).findFirst();
    }

    public List<Generator> getAllGenerators() {
        return Collections.unmodifiableList(generators);
    }

    private List<Generator> initListFromFiles() {
        final List<Generator> gens = new ArrayList<>();
        for (File file : gensPath.toFile().listFiles()) {
            try (FileReader reader = new FileReader(file)) {
                Generator gen = gson.fromJson(reader, Generator.class);
                gens.add(gen);
                taskForGen(gen);
                if (genHolos.get(gen) != null) {
                    FancyHologramsPlugin.get().getHologramManager().removeHologram(genHolos.get(gen));
                    genHolos.remove(gen);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gens;
    }

    private void taskForGen(Generator gen) {
        timeUntilRespawn.put(gen, gen.respawnCooldown());
        System.out.println(gen.one());
        System.out.println(gen.two());
        CuboidRegion holoRegion = new CuboidRegion(gen.one().toBlockVector3(), gen.two().toBlockVector3());
        holoRegion.setWorld(BukkitAdapter.adapt(Bukkit.getWorld(gen.one().world())));
        TextHologramData holoMeta = new TextHologramData(gen.name(), centerOf(holoRegion));
        holoMeta.setPersistent(false);
        if (gen.hologram()) {
            Hologram holo = FancyHologramsPlugin.get().getHologramManager().create(holoMeta);
            FancyHologramsPlugin.get().getHologramManager().addHologram(holo);
            genHolos.put(gen, holo);
        }

        Task.builder()
                .expireWhen(t -> !generators.contains(gen))
                .repeat(Ticks.seconds(1))
                .whenRan(t -> {
                    holoMeta.setText(List.of("<gray>" + gen.name(), "<dark_gray>" + turnSecondsIntoPrettyString(timeUntilRespawn.get(gen))));
                    timeUntilRespawn.put(gen, timeUntilRespawn.get(gen) - 1);
                    if (timeUntilRespawn.get(gen) < 0) {
                        timeUntilRespawn.put(gen, gen.respawnCooldown());
                        List<Block> blocks = genBlocks.computeIfAbsent(gen, g -> {
                            Pos one = gen.one();
                            Pos two = gen.two();
                            org.bukkit.World bukkitWorld = Bukkit.getWorld(one.world());
                            World world = BukkitAdapter.adapt(bukkitWorld);
                            CuboidRegion region = new CuboidRegion(one.toBlockVector3(), two.toBlockVector3());
                            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                                ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
                                Operations.complete(copy);
                            }
                            return region.stream().map(v3 -> bukkitWorld.getBlockAt(v3.x(), v3.y(), v3.z())).toList(); // we could just use the BlockVector3 but i prefer Bukkit API
                        });
                        for (Block block : blocks) {
                            block.setType(gen.type());
                        }
                    }
                })
                .run();
    }

    private Location centerOf(CuboidRegion region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return new Location(
                BukkitAdapter.adapt(region.getWorld()),
                (min.x() + max.x()) / 2.0,
                max.y() + 1.5,
                (min.z() + max.z()) / 2.0
        );
    }

    public void modifyGeneratorBlockType(String name, @NotNull Material type) {
        findByName(name).ifPresent(generator -> {
            deleteGenerator(name);
            addGenerator(generator.withType(type));
        });
    }

    public void modifyGeneratorRespawnCooldown(String name, int respawnCooldown) {
        findByName(name).ifPresent(generator -> {
            deleteGenerator(name);
            addGenerator(generator.withRespawnCooldown(respawnCooldown));
        });
    }

    public void modifyGeneratorHologram(String name, boolean hologram) {
        findByName(name).ifPresent(generator -> {
            deleteGenerator(name);
            addGenerator(generator.withHologram(hologram));
        });
    }

    public long getTimeUntilRespawn(Generator gen) {
        return timeUntilRespawn.get(gen);
    }

    public static String turnSecondsIntoPrettyString(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return "%02d:%02d:%02d".formatted(hours, minutes, seconds);
    }
}
