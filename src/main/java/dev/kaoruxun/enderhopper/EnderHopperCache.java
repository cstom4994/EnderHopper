package dev.kaoruxun.enderhopper;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnderHopperCache {
    private static final int CACHE_EXPIRE_TICKS = 100; // 100ticks=5s
    private static final Map<BlockPos, SignCacheEntry> signCache = new ConcurrentHashMap<>();
    private static final Map<String, EnderCacheEntry> enderCache = new ConcurrentHashMap<>();

    static class SignCacheEntry {
        final int createdTick;
        final BlockPos signPos;
        final String playerName;

        SignCacheEntry(BlockPos signPos, String playerName, int tick) {
            this.createdTick = tick;
            this.signPos = signPos;
            this.playerName = playerName;
        }

        boolean isValid(World world, int currentTick) {
            return (currentTick - createdTick) <= CACHE_EXPIRE_TICKS &&
                    world.getBlockEntity(signPos) instanceof SignBlockEntity;
        }
    }

    static class EnderCacheEntry {
        final int createdTick;
        final EnderChestInventory inventory;

        EnderCacheEntry(EnderChestInventory inventory, int tick) {
            this.createdTick = tick;
            this.inventory = inventory;
        }

        boolean isValid(int currentTick) {
            return (currentTick - createdTick) <= CACHE_EXPIRE_TICKS;
        }
    }

    public static void cleanCache(MinecraftServer server) {
        int currentTick = server.getTicks();
        signCache.entrySet().removeIf(entry ->
                !entry.getValue().isValid(server.getOverworld(), currentTick)
        );
        enderCache.entrySet().removeIf(entry ->
                !entry.getValue().isValid(currentTick)
        );
    }

    public static SignCacheEntry getSignCache(BlockPos hopperPos) {
        return signCache.get(hopperPos);
    }

    public static void putSignCache(BlockPos hopperPos, SignCacheEntry entry) {
        signCache.put(hopperPos, entry);
    }

    public static EnderCacheEntry getEnderCache(String playerName) {
        return enderCache.get(playerName);
    }

    public static void putEnderCache(String playerName, EnderCacheEntry entry) {
        enderCache.put(playerName, entry);
    }

    public static int getCurrentTick(World world) {
        MinecraftServer server = world.getServer();
        return server != null ? server.getTicks() : 0;
    }
}