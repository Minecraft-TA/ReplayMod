//#if MC>=11400
//$$ package com.replaymod.replay.mixin;
//$$
//$$ import com.replaymod.replay.camera.CameraEntity;
//$$ import net.minecraft.client.entity.player.ClientPlayerEntity;
//$$ import net.minecraft.client.multiplayer.PlayerController;
//$$ import net.minecraft.world.GameType;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Pseudo;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Redirect;
//$$
//$$ import static com.replaymod.core.versions.MCVer.getMinecraft;
//$$
//$$ @Pseudo
//$$ @Mixin(targets = "net.coderbot.iris.pipeline.HandRenderer", remap = false)
//$$ public abstract class Mixin_ShowSpectatedHand_Iris {
//$$     @Redirect(
//$$             method = "*",
//$$             at = @At(
//$$                     value = "INVOKE",
//$$                     target = "Lnet/minecraft/client/multiplayer/PlayerController;getCurrentGameType()Lnet/minecraft/world/GameType;",
//$$                     remap = true
//$$             )
//$$     )
//$$     private GameType getGameMode(PlayerController interactionManager) {
//$$         ClientPlayerEntity camera = getMinecraft().player;
//$$         if (camera instanceof CameraEntity) {
//$$             // alternative doesn't really matter, the caller only checks for equality to SPECTATOR
//$$             return camera.isSpectator() ? GameType.SPECTATOR : GameType.SURVIVAL;
//$$         }
//$$         return interactionManager.getCurrentGameType();
//$$     }
//$$ }
//#endif
