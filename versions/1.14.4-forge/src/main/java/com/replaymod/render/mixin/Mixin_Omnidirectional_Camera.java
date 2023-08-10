package com.replaymod.render.mixin;

import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public abstract class Mixin_Omnidirectional_Camera implements EntityRendererHandler.IEntityRenderer {
    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Matrix4f;perspective(DFFF)Lnet/minecraft/client/renderer/Matrix4f;"))
    private Matrix4f replayModRender_perspective$0(double fovY, float aspect, float zNear, float zFar) {
        return replayModRender_perspective((float) fovY, aspect, zNear, zFar);
    }

    @Redirect(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Matrix4f;perspective(DFFF)Lnet/minecraft/client/renderer/Matrix4f;"))
    private Matrix4f replayModRender_perspective$1(double fovY, float aspect, float zNear, float zFar) {
        return replayModRender_perspective((float) fovY, aspect, zNear, zFar);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Matrix4f;perspective(DFFF)Lnet/minecraft/client/renderer/Matrix4f;"))
    private Matrix4f replayModRender_perspective$2(double fovY, float aspect, float zNear, float zFar) {
        return replayModRender_perspective((float) fovY, aspect, zNear, zFar);
    }

    private Matrix4f replayModRender_perspective(float fovY, float aspect, float zNear, float zFar) {
        if (replayModRender_getHandler() != null && replayModRender_getHandler().omnidirectional) {
            fovY = 90;
            aspect = 1;
        }
        return Matrix4f.perspective(fovY, aspect, zNear, zFar);
    }
}
