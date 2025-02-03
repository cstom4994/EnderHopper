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
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = hopperPos.offset(direction);
            Block block = world.getBlockState(checkPos).getBlock();
            if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
                BlockEntity be = world.getBlockEntity(checkPos);
                if (be instanceof SignBlockEntity) {
                    return (SignBlockEntity) be;
                }
            }
        }
        return null;
    }

    public static String getSignPlayerName(SignBlockEntity sign) {
        Text[] texts = sign.getFrontText().getMessages(false);
        return texts.length > 0 ? texts[0].getString().trim() : null;
    }

    public static EnderChestInventory getEnderInventory(World world, String playerName) {
        MinecraftServer server = world.getServer();
        if (server == null) return null;

        ServerPlayerEntity target = world.getServer()
                .getPlayerManager()
                .getPlayer(playerName);

        if (target != null) {
            // Force sync of ender chest data
            target.currentScreenHandler.sendContentUpdates();
            target.getEnderChestInventory().markDirty();
            return target.getEnderChestInventory();
        }
        return null;
    }

    @Override
    public void onInitialize() {
    }
}
