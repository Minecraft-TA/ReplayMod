package com.replaymod.replay.mixin;

//#if MC>=10904
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=11600
//$$ import net.minecraft.client.world.ClientWorld;
//#else
import net.minecraft.world.World;
//#endif

import java.util.Queue;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {
    @Final @Shadow
    private Queue<Particle> queueEntityFX;

    /**
     * This method additionally clears the queue of particles to be added when the world is changed.
     * Otherwise particles from the previous world might show up in this one if they were spawned after
     * the last tick in the previous world.
     *
     * @param world The new world
     * @param ci Callback info
     */
    @Inject(method = "clearEffects(Lnet/minecraft/world/World;)V", at = @At("HEAD"))
    public void replayModReplay_clearParticleQueue(
            //#if MC>=11600
            //$$ ClientWorld world,
            //#else
            World world,
            //#endif
            CallbackInfo ci) {
        this.queueEntityFX.clear();
    }
}
//#endif
