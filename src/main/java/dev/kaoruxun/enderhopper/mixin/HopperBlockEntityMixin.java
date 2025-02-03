package dev.kaoruxun.enderhopper.mixin;

import dev.kaoruxun.enderhopper.EnderHopper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Inject(
            method = "getOutputInventory",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void modifyOutputInventory(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Inventory> cir) {
        BlockPos targetPos = pos.offset(Direction.DOWN);
        if (world.getBlockState(targetPos).getBlock() == Blocks.ENDER_CHEST) {
            // Get the Attached sign
            SignBlockEntity sign = EnderHopper.findAttachedSign(world, pos);
            if (sign != null) {
                String targetPlayerName = EnderHopper.getSignPlayerName(sign);
                if (targetPlayerName != null) {
                    // Get the target player's ender chest
                    Inventory targetInventory = EnderHopper.getEnderInventory(world, targetPlayerName);
                    if (targetInventory != null) {
                        cir.setReturnValue(targetInventory);
                        return;
                    }
                }
            }
        }
    }


    @Inject(
            method = "getInputInventory",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void modifyEnderChestInput(
            World world, Hopper hopper, CallbackInfoReturnable<Inventory> cir
    ) {
        if (world.isClient) return;
        BlockPos upperPos = BlockPos.ofFloored(
                hopper.getHopperX(),
                hopper.getHopperY() + 1,
                hopper.getHopperZ()
        );
        if (world.getBlockState(upperPos).getBlock() == Blocks.ENDER_CHEST) {
            processEnderChestHopper(world, hopper, cir, upperPos);
        }
    }

    private static void processEnderChestHopper(
            World world,
            Hopper hopper,
            CallbackInfoReturnable<Inventory> cir,
            BlockPos chestPos
    ) {
        if (!(hopper instanceof HopperBlockEntity)) return;
        BlockPos hopperPos = ((HopperBlockEntity) hopper).getPos();
        SignBlockEntity sign = EnderHopper.findAttachedSign(world, hopperPos);
        if (sign == null) return;
        String targetPlayer = EnderHopper.getSignPlayerName(sign);
        if (targetPlayer == null) return;
        EnderChestInventory enderInventory = EnderHopper.getEnderInventory(world, targetPlayer);

        if (enderInventory != null) {
            enderInventory.markDirty();
            cir.setReturnValue(enderInventory);
            cir.cancel();
        }
    }

//    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
//    private static void onInsert(World world, BlockPos pos, BlockState state, Inventory inventory, CallbackInfoReturnable<Boolean> cir) {
//        HopperBlockEntity hopper = (HopperBlockEntity) world.getBlockEntity(pos);
//        if (hopper.transferCooldown > 0 && isEnderHopper(hopper)) {
//            hopper.transferCooldown = Math.max(hopper.transferCooldown - 2, 1);
//            cir.setReturnValue(false);
//            cir.cancel();
//        }
//    }
//
//    private static boolean isEnderHopper(HopperBlockEntity hopper) {
//        BlockPos upPos = hopper.getPos().up();
//        return hopper.getWorld().getBlockState(upPos).getBlock() == Blocks.ENDER_CHEST;
//    }
}