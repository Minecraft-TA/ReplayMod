package com.replaymod.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Queue;

//#if MC>=11800
//$$ import java.util.function.Supplier;
//#endif

//#if MC>=11400
//$$ import java.util.concurrent.CompletableFuture;
//#endif

//#if MC<11400
import java.util.concurrent.FutureTask;
//#endif

//#if MC<11400
import net.minecraft.client.resources.IResourcePack;
import java.util.List;
//#endif

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor
    Timer getTimer();
    @Accessor
    //#if MC>=11200
    @Mutable
    //#endif
    void setTimer(Timer value);

    //#if MC>=11400
    //$$ @Accessor("field_213276_aV")
    //$$ CompletableFuture<Void> getResourceReloadFuture();
    //$$ @Accessor("field_213276_aV")
    //$$ void setResourceReloadFuture(CompletableFuture<Void> value);
    //#endif

    //#if MC>=11400
    //$$ @Accessor("field_213275_aU")
    //$$ Queue<Runnable> getRenderTaskQueue();
    //#else
    @Accessor
    Queue<FutureTask<?>> getScheduledTasks();
    //#endif

    @Accessor
    //#if MC>=11800
    //$$ Supplier<CrashReport> getCrashReporter();
    //#else
    CrashReport getCrashReporter();
    //#endif

    //#if MC<11400
    @Accessor
    List<IResourcePack> getDefaultResourcePacks();
    //#endif

    //#if MC>=11400
    //$$ @Accessor("networkManager")
    //$$ void setConnection(NetworkManager connection);
    //#endif
}
