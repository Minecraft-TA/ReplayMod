package com.replaymod.render.mixin;

//#if MC>=10800
import com.replaymod.compat.shaders.ShaderReflection;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.hooks.IForceChunkLoading;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=11400
//$$ import net.minecraft.client.renderer.ActiveRenderInfo;
//#else
import net.minecraft.entity.Entity;
//#endif

//#if MC<10904
//$$ import net.minecraft.client.renderer.chunk.RenderChunk;
//$$ import net.minecraft.util.BlockPos;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

@Mixin(RenderGlobal.class)
public abstract class Mixin_ForceChunkLoading implements IForceChunkLoading {
    private ForceChunkLoadingHook replayModRender_hook;

    @Override
    public void replayModRender_setHook(ForceChunkLoadingHook hook) {
        this.replayModRender_hook = hook;
    }

    private boolean replayModRender_passThroughSetupTerrain;

    @Shadow
    private boolean displayListEntitiesDirty;

    @Shadow
    private ChunkRenderDispatcher renderDispatcher;

    @Shadow
    public abstract void setupTerrain(
            //#if MC>=11400
            //$$ ActiveRenderInfo viewEntity,
            //#else
            Entity viewEntity,
            double partialTicks,
            //#endif
            ICamera camera,
            int frameCount,
            boolean playerSpectator
    );

    @Inject(method = "setupTerrain", at = @At("HEAD"), cancellable = true)
    private void replayModRender_setupTerrain(
            //#if MC>=11400
            //$$ ActiveRenderInfo viewEntity,
            //#else
            Entity viewEntity,
            double partialTicks,
            //#endif
            ICamera camera,
            int frameCount,
            boolean playerSpectator,
            CallbackInfo ci
    ) throws IllegalAccessException {
        if (ShaderReflection.shaders_isShadowPass != null && (boolean) ShaderReflection.shaders_isShadowPass.get(null)) {
            return;
        }
        if (replayModRender_hook != null && !replayModRender_passThroughSetupTerrain) {
            replayModRender_passThroughSetupTerrain = true;

            do {
                setupTerrain(
                        viewEntity,
                        //#if MC<11400
                        partialTicks,
                        //#endif
                        camera,
                        replayModRender_hook.nextFrameId(),
                        playerSpectator
                );
                replayModRender_hook.updateChunks();
            } while (this.displayListEntitiesDirty);

            this.displayListEntitiesDirty = true;

            replayModRender_passThroughSetupTerrain = false;
            ci.cancel();
        }
    }

    //#if MC<10904
    //$$ @Inject(method = "isPositionInRenderChunk", at = @At("HEAD"), cancellable = true)
    //$$ public void replayModRender_isPositionInRenderChunk(BlockPos pos, RenderChunk chunk, CallbackInfoReturnable<Boolean> ci) {
    //$$     if (replayModRender_hook != null) {
    //$$         ci.setReturnValue(true);
    //$$     }
    //$$ }
    //#endif

    @Inject(method = "updateChunks", at = @At("HEAD"), cancellable = true)
    public void replayModRender_updateChunks(long finishTimeNano, CallbackInfo ci) {
        if (replayModRender_hook != null) {
            replayModRender_hook.updateChunks();
            ci.cancel();
        }
    }

    // Prior to 1.9.4, MC always uses the same ChunkRenderDispatcher instance
    //#if MC>=10904
    @Inject(method = "setWorldAndLoadRenderers(Lnet/minecraft/client/multiplayer/WorldClient;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;stopWorkerThreads()V"))
    private void stopWorkerThreadsAndChunkLoadingRenderGlobal(CallbackInfo ci) {
        if (replayModRender_hook != null) {
            replayModRender_hook.updateRenderDispatcher(null);
        }
    }
    //#endif

    @Inject(method = "loadRenderers", at = @At(value = "RETURN"))
    private void setupChunkLoadingRenderGlobal(CallbackInfo ci) {
        if (replayModRender_hook != null) {
            replayModRender_hook.updateRenderDispatcher(this.renderDispatcher);
        }
    }
}
//#endif
