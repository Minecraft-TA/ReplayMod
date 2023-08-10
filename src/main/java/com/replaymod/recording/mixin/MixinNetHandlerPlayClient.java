package com.replaymod.recording.mixin;

import com.replaymod.core.versions.MCVer;
import com.replaymod.recording.handler.RecordingEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketRespawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=10800
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.packets.PacketPlayerListEntry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
//#else
//$$ import net.minecraft.network.play.server.S01PacketJoinGame;
//#endif

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    // The stupid name is required as otherwise Mixin treats it as a shadow, seemingly ignoring the lack of @Shadow
    private static Minecraft mcStatic = MCVer.getMinecraft();

    //#if MC>=10800
    @Shadow
    private Map<UUID, NetworkPlayerInfo> playerInfoMap;
    //#endif

    public RecordingEventHandler getRecordingEventHandler() {
        return ((RecordingEventHandler.RecordingEventSender) mcStatic.renderGlobal).getRecordingEventHandler();
    }

    /**
     * Record the own player entity joining the world.
     * We cannot use the {@link net.minecraftforge.event.entity.EntityJoinWorldEvent} because the entity id
     * of the player is set afterwards and the tablist entry might not yet be sent.
     * @param packet The packet
     * @param ci Callback info
     */
    //#if MC>=10800
    //#if FABRIC>=1
    //$$ @Inject(method = "onPlayerList", at=@At("HEAD"))
    //#else
    @Inject(method = "handlePlayerListItem(Lnet/minecraft/network/play/server/SPacketPlayerListItem;)V", at=@At("HEAD"))
    //#endif
    public void recordOwnJoin(SPacketPlayerListItem packet, CallbackInfo ci) {
        if (!mcStatic.isCallingFromMinecraftThread()) return;
        if (mcStatic.player == null) return;

        RecordingEventHandler handler = getRecordingEventHandler();
        //#if MC>=11903
        //$$ if (handler != null && packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
        //#else
        if (handler != null && packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
        //#endif
            // We cannot reference SPacketPlayerListItem.AddPlayerData directly for complicated (and yet to be
            // resolved) reasons (see https://github.com/MinecraftForge/ForgeGradle/issues/472), so we use ReplayStudio
            // to parse it instead.
            ByteBuf byteBuf = Unpooled.buffer();
            try {
                packet.writePacketData(new PacketBuffer(byteBuf));

                byteBuf.readerIndex(0);
                byte[] array = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(array);

                for (PacketPlayerListEntry data : PacketPlayerListEntry.read(new Packet(
                        MCVer.getPacketTypeRegistry(false), 0, PacketType.PlayerListEntry,
                        com.github.steveice10.netty.buffer.Unpooled.wrappedBuffer(array)
                ))) {
                    if (data.getUuid() == null) continue;
                    // Only add spawn packet for our own player and only if he isn't known yet
                    if (data.getUuid().equals(mcStatic.player.getGameProfile().getId())
                            && !this.playerInfoMap.containsKey(data.getUuid())) {
                        handler.spawnRecordingPlayer();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // we just parsed this?
            } finally {
                byteBuf.release();
            }
        }
    }
    //#else
    //$$ @Inject(method = "handleJoinGame", at=@At("RETURN"))
    //$$ public void recordOwnJoin(S01PacketJoinGame packet, CallbackInfo ci) {
    //$$     RecordingEventHandler handler = getRecordingEventHandler();
    //$$     if (handler != null) {
    //$$         handler.spawnRecordingPlayer();
    //$$     }
    //$$ }
    //#endif

    /**
     * Record the own player entity respawning.
     * We cannot use the {@link net.minecraftforge.event.entity.EntityJoinWorldEvent} because that would also include
     * the first spawn which is already handled by the above method.
     * @param packet The packet
     * @param ci Callback info
     */
    //#if FABRIC>=1
    //$$ @Inject(method = "onPlayerRespawn", at=@At("RETURN"))
    //#else
    @Inject(method = "handleRespawn(Lnet/minecraft/network/play/server/SPacketRespawn;)V", at=@At("RETURN"))
    //#endif
    public void recordOwnRespawn(SPacketRespawn packet, CallbackInfo ci) {
        RecordingEventHandler handler = getRecordingEventHandler();
        if (handler != null) {
            handler.spawnRecordingPlayer();
        }
    }
}
