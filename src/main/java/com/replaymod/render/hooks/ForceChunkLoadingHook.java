package com.replaymod.render.hooks;

import com.replaymod.render.mixin.ChunkRenderDispatcherAccessor;
import com.replaymod.render.mixin.WorldRendererAccessor;
import com.replaymod.render.utils.JailingQueue;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.client.renderer.chunk.RenderChunk;

import java.util.Iterator;

//#if MC>=10904
import java.util.concurrent.PriorityBlockingQueue;
//#else
//$$ import java.util.concurrent.BlockingQueue;
//#endif

import static com.replaymod.core.versions.MCVer.ChunkRenderWorkerAccessor;

public class ForceChunkLoadingHook {

    private final RenderGlobal hooked;

    private ChunkRenderDispatcher renderDispatcher;
    //#if MC>=11400
    //$$ private JailingQueue<ChunkRenderTask> workerJailingQueue;
    //#else
    private JailingQueue<ChunkCompileTaskGenerator> workerJailingQueue;
    //#endif
    private ChunkRenderWorkerAccessor renderWorker;
    private int frame;

    public ForceChunkLoadingHook(RenderGlobal renderGlobal) {
        this.hooked = renderGlobal;

        setup(((WorldRendererAccessor) renderGlobal).getRenderDispatcher());
        IForceChunkLoading.from(renderGlobal).replayModRender_setHook(this);
    }

    public void updateRenderDispatcher(ChunkRenderDispatcher renderDispatcher) {
        if (this.renderDispatcher != null) {
            workerJailingQueue.freeAll();
            this.renderDispatcher = null;
        }
        if (renderDispatcher != null) {
            setup(renderDispatcher);
        }
    }

    private void setup(ChunkRenderDispatcher renderDispatcher) {
        this.renderDispatcher = renderDispatcher;
        this.renderWorker = (ChunkRenderWorkerAccessor) new ChunkRenderWorker(renderDispatcher, new RegionRenderCacheBuilder());
        ChunkRenderDispatcherAccessor renderDispatcherAcc = (ChunkRenderDispatcherAccessor) renderDispatcher;

        int workerThreads = renderDispatcherAcc.getListThreadedWorkers().size();
        //#if MC>=10904
        PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates = renderDispatcherAcc.getQueueChunkUpdates();
        //#else
        //$$ BlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates = renderDispatcherAcc.getQueueChunkUpdates();
        //#endif
        workerJailingQueue = new JailingQueue<>(queueChunkUpdates);
        renderDispatcherAcc.setQueueChunkUpdates(workerJailingQueue);
        //#if MC>=10904
        ChunkCompileTaskGenerator element = new ChunkCompileTaskGenerator(
                null,
                null,
                0
                //#if MC>=11400
                //$$ , null
                //#endif
        );
        //#else
        //$$ ChunkCompileTaskGenerator element = new ChunkCompileTaskGenerator(null, null);
        //#endif
        element.finish();
        for (int i = 0; i < workerThreads; i++) {
            queueChunkUpdates.add(element);
        }

        // Temporary workaround for dead lock, will be replaced by a new (ShaderMod compatible) mechanism later
        //noinspection StatementWithEmptyBody
        while (renderDispatcher.runChunkUploads(0)) {}

        workerJailingQueue.jail(workerThreads);
        renderDispatcherAcc.setQueueChunkUpdates(queueChunkUpdates);
    }

    public void updateChunks() {
        while (renderDispatcher.runChunkUploads(0)) {
            ((WorldRendererAccessor) hooked).setDisplayListEntitiesDirty(true);
        }

        //#if MC>=10904
        PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates;
        //#else
        //$$ BlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates;
        //#endif
        queueChunkUpdates = ((ChunkRenderDispatcherAccessor) renderDispatcher).getQueueChunkUpdates();
        while (!queueChunkUpdates.isEmpty()) {
            try {
                renderWorker.doRunTask(queueChunkUpdates.poll());
            } catch (InterruptedException ignored) { }
        }

        Iterator<RenderChunk> iterator = ((WorldRendererAccessor) hooked).getChunksToUpdate().iterator();
        while (iterator.hasNext()) {
            RenderChunk renderchunk = iterator.next();

            renderDispatcher.updateChunkNow(renderchunk);

            //#if MC>=10904
            renderchunk.clearNeedsUpdate();
            //#else
            //$$ renderchunk.setNeedsUpdate(false);
            //#endif
            iterator.remove();
        }
    }

    public int nextFrameId() {
        return frame++;
    }

    public void uninstall() {
        workerJailingQueue.freeAll();
        IForceChunkLoading.from(hooked).replayModRender_setHook(null);
    }
}
