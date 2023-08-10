//#if MC>=11400
//$$ package com.replaymod.core.mixin;
//$$
//$$ import com.replaymod.core.events.PostRenderWorldCallback;
//$$ import com.replaymod.core.events.PreRenderHandCallback;
//$$ import net.minecraft.client.renderer.ActiveRenderInfo;
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$ import de.johni0702.minecraft.gui.versions.MatrixStack;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ @Mixin(GameRenderer.class)
//$$ public class MixinGameRenderer {
//$$     @Inject(
//$$             method = "updateCameraAndRender(FJ)V",
//$$             at = @At(
//$$                     value = "FIELD",
//$$                     target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"
//$$             )
//$$     )
//$$     private void postRenderWorld(
//$$             float partialTicks,
//$$             long nanoTime,
            //#if MC>=11500
            //$$ MatrixStack matrixStack,
            //#endif
//$$             CallbackInfo ci) {
        //#if MC<11500
        //$$ MatrixStack matrixStack = new MatrixStack();
        //#endif
//$$         PostRenderWorldCallback.EVENT.invoker().postRenderWorld(matrixStack);
//$$     }
//$$
//$$     @Inject(method = "func_215308_a", at = @At("HEAD"), cancellable = true)
//$$     private void preRenderHand(
            //#if MC>=11500
            //$$ MatrixStack matrixStack,
            //#endif
//$$             ActiveRenderInfo camera,
//$$             float partialTicks,
//$$             CallbackInfo ci) {
//$$         if (PreRenderHandCallback.EVENT.invoker().preRenderHand()) {
//$$             ci.cancel();
//$$         }
//$$     }
//$$ }
//#endif
