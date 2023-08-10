package com.replaymod.recording.mixin;

import com.replaymod.core.versions.MCVer;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.recording.handler.RecordingEventHandler.RecordingEventSender;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.IPacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLoginNetHandler.class)
public abstract class MixinNetHandlerLoginClient {

    @Final @Shadow
    private NetworkManager networkManager;

    @Inject(method = "handleCustomPayloadLogin", at=@At("HEAD"))
    private void earlyInitiateRecording(SCustomPayloadLoginPacket packet, CallbackInfo ci) {
        initiateRecording(packet);
    }

    @Inject(method = "handleLoginSuccess", at=@At("HEAD"))
    private void lateInitiateRecording(SLoginSuccessPacket packet, CallbackInfo ci) {
        initiateRecording(packet);
    }

    private void initiateRecording(IPacket<?> packet) {
        RecordingEventSender eventSender = (RecordingEventSender) MCVer.getMinecraft().worldRenderer;
        if (eventSender.getRecordingEventHandler() != null) {
            return; // already recording
        }
        ReplayModRecording.instance.initiateRecording(this.networkManager);
        if (eventSender.getRecordingEventHandler() != null) {
            eventSender.getRecordingEventHandler().onPacket(packet);
        }
    }
}
