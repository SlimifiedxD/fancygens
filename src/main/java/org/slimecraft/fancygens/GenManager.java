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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.slimecraft.bedrock.task.Task;
import org.slimecraft.bedrock.util.Ticks;
import org.slimecraft.fancygens.dto.Generator;
import org.slimecraft.fancygens.dto.Pos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GenManager {
    private final Plugin plugin;
    private final Path gensPath;
    private final List<Generator> generators;
    private final Gson gson = new Gson();

    public GenManager(Plugin plugin) {
        this.plugin = plugin;
        gensPath = plugin.getDataPath().resolve("gens");
        generators = initListFromFiles();
    }

    public void addGenerator(Generator gen) {
        generators.add(gen);
        try (FileWriter writer = new FileWriter(gensPath.resolve(gen.name().toLowerCase() + ".json").toFile())) {
            gson.toJson(gen, writer);
            taskForGen(gen);
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
        generators.remove(optGen.get());
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gens;
    }

    private void taskForGen(Generator gen) {
        Task.builder()
                .expireWhen(t -> !generators.contains(gen))
                .repeat(Ticks.seconds(gen.respawnCooldown()))
                .whenRan(t -> {
                    Pos one = gen.one();
                    Pos two = gen.two();
                    org.bukkit.World bukkitWorld = Bukkit.getWorld(one.world());
                    World world = BukkitAdapter.adapt(bukkitWorld);
                    CuboidRegion region = new CuboidRegion(one.toBlockVector3(), two.toBlockVector3());
                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                    ForwardExtentCopy copy = new ForwardExtentCopy(world, region, clipboard, region.getMinimumPoint());
                    Operations.complete(copy);
                    List<Block> blocks = region.stream().map(v3 -> bukkitWorld.getBlockAt(v3.x(), v3.y(), v3.z())).toList(); // we could just use the BlockVector3 but i prefer Bukkit API
                    for (Block block : blocks) {
                        block.setType(gen.type());
                    }
                })
                .run();
    }
}
