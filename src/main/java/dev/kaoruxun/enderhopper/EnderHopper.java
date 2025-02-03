package dev.kaoruxun.enderhopper;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnderHopper implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("enderhopper");

    public static SignBlockEntity findAttachedSign(World world, BlockPos hopperPos) {
        int currentTick = EnderHopperCache.getCurrentTick(world);
        EnderHopperCache.SignCacheEntry cached = EnderHopperCache.getSignCache(hopperPos);

        if (cached != null && cached.isValid(world, currentTick)) {
            BlockEntity be = world.getBlockEntity(cached.signPos);
            return (SignBlockEntity) be;
        }

        for (Direction direction : Direction.values()) {
            BlockPos checkPos = hopperPos.offset(direction);
            Block block = world.getBlockState(checkPos).getBlock();
            if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
                BlockEntity be = world.getBlockEntity(checkPos);
                if (be instanceof SignBlockEntity sign) {
                    String playerName = getSignPlayerName(sign);
                    if (playerName != null) {
                        EnderHopperCache.putSignCache(hopperPos,
                                new EnderHopperCache.SignCacheEntry(checkPos, playerName, currentTick)
                        );
                    }
                    return sign;
                }
            }
        }
        return null;
    }

    public static String getSignPlayerName(SignBlockEntity sign) {
        Text[] texts = sign.getFrontText().getMessages(false);
        if (texts.length == 0) return null;
        return texts[0].getString().trim().replaceAll("§[0-9a-fk-or]", "");
    }

    public static EnderChestInventory getEnderInventory(World world, String playerName) {
        int currentTick = EnderHopperCache.getCurrentTick(world);
        EnderHopperCache.EnderCacheEntry cached = EnderHopperCache.getEnderCache(playerName);
        if (cached != null && cached.isValid(currentTick)) {
            return cached.inventory;
        }

        MinecraftServer server = world.getServer();
        if (server == null) return null;

        ServerPlayerEntity target = world.getServer()
                .getPlayerManager()
                .getPlayer(playerName);
        if (target == null) return null;

        // Force sync of ender chest data
        EnderChestInventory inventory = target.getEnderChestInventory();
        inventory.markDirty();
        target.currentScreenHandler.sendContentUpdates();

        EnderHopperCache.putEnderCache(playerName,
                new EnderHopperCache.EnderCacheEntry(inventory, currentTick)
        );
        return inventory;
    }

    @Override
    public void onInitialize() {
    }
}
