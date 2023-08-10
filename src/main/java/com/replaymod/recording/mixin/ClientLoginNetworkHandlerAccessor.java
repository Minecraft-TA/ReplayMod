package com.replaymod.recording.mixin;

import net.minecraft.client.network.NetHandlerLoginClient;
import org.spongepowered.asm.mixin.Mixin;

//#if MC>=11903
//$$ import net.minecraft.client.network.ServerInfo;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//#endif

@Mixin(NetHandlerLoginClient.class)
public interface ClientLoginNetworkHandlerAccessor {
    //#if MC>=11903
    //$$ @Accessor
    //$$ ServerInfo getServerInfo();
    //#endif
}
