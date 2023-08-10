package com.replaymod.recording.mixin;

import com.replaymod.recording.ReplayModRecording;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerLoginClient.class)
public abstract class MixinNetHandlerLoginClient {

    @Final @Shadow
    private NetworkManager networkManager;

    /**
     * Starts the recording right before switching into PLAY state.
     * We cannot use the {@link FMLNetworkEvent.ClientConnectedToServerEvent}
     * as it only fires after the forge handshake.
     */
    @Inject(method = "handleLoginSuccess", at=@At("HEAD"))
    public void replayModRecording_initiateRecording(CallbackInfo cb) {
        ReplayModRecording.instance.initiateRecording(this.networkManager);
    }

    // Race condition in Forge's networking (not sure if still present in 1.13)
    //#if MC>=11200 && MC<11400
    @Inject(method = "handleLoginSuccess", at=@At("RETURN"))
    public void replayModRecording_raceConditionWorkAround(CallbackInfo cb) {
        ((NetworkManagerAccessor) this.networkManager).getChannel().config().setAutoRead(true);
    }
    //#endif
}
