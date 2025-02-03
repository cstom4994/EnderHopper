package dev.kaoruxun.enderhopper.mixin;

import dev.kaoruxun.enderhopper.EnderHopperCache;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow
    private int ticks;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onServerTick(CallbackInfo ci) {
        if (ticks % 20 == 0) {
            EnderHopperCache.cleanCache((MinecraftServer) (Object) this);
        }
    }
}
