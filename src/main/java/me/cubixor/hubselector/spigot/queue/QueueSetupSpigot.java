package me.cubixor.hubselector.spigot.queue;

import me.cubixor.hubselector.spigot.HubSelector;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class QueueSetupSpigot {

    HubSelector plugin;

    public QueueSetupSpigot() {
        plugin = HubSelector.getInstance();
    }

    public void registerMethods() {
        plugin.getServer().getPluginManager().registerEvents(new QueueListenerSpigot(), plugin);
        createQueueWorld();
    }

    @SuppressWarnings("deprecation")
    public void createQueueWorld() {
        WorldCreator worldCreator = new WorldCreator("queue");
        worldCreator.environment(World.Environment.THE_END).generateStructures(false).generator(new EmptyChunkGenerator());
        World world = plugin.getServer().createWorld(worldCreator);
        world.setGameRuleValue("announceAchievements", "false");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("spectatorsGenerateChunks", "false");
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        world.setDifficulty(Difficulty.PEACEFUL);
    }

    public void disable() {
        World world = Bukkit.getWorld("queue");
        if (world != null) {
            for (Chunk chunk : world.getLoadedChunks()) {
                chunk.unload(false);
            }
            Bukkit.unloadWorld("queue", false);
        }
    }

    @SuppressWarnings("deprecation")
    static class EmptyChunkGenerator extends ChunkGenerator {

        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world);
        }
    }

}
