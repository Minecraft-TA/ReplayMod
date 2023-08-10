package com.replaymod.render.mixin;

import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public abstract class Mixin_PreserveDepthDuringHandRendering {
    @ModifyArg(
            //#if MC>=11400
            //$$ method = "updateCameraAndRender(FJ)V",
            //#else
            method = "renderWorldPass",
            //#endif
            //#if MC>=11500
            //$$ at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"),
            //#elseif MC>=11400
            //$$ at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;clear(IZ)V", ordinal = 1),
            //#else
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V", ordinal = 1),
            //#endif
            index = 0
    )
    private int replayModRender_skipClearWhenRecordingDepth(int mask) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) this).replayModRender_getHandler();
        if (handler != null && handler.getSettings().isDepthMap()) {
            mask = mask & ~GL11.GL_DEPTH_BUFFER_BIT;
        }
        return mask;
    }
}
