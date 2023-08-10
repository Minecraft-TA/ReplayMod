package com.replaymod.simplepathing.preview;

import net.minecraft.client.renderer.GlStateManager;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.PostRenderWorldCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.pathing.properties.CameraProperties;
import com.replaymod.pathing.properties.SpectatorProperty;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import com.replaymod.replaystudio.util.Location;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import com.replaymod.simplepathing.SPTimeline;
import com.replaymod.simplepathing.gui.GuiPathing;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import de.johni0702.minecraft.gui.versions.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

//#if MC>=11700
//$$ import net.minecraft.client.render.GameRenderer;
//#endif

//#if MC>=11500
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//#endif

import java.util.Comparator;
import java.util.Optional;

import static com.replaymod.core.ReplayMod.TEXTURE;
import static com.replaymod.core.versions.MCVer.bindTexture;
import static com.replaymod.core.versions.MCVer.emitLine;
import static com.replaymod.core.versions.MCVer.popMatrix;
import static com.replaymod.core.versions.MCVer.pushMatrix;

public class PathPreviewRenderer extends EventRegistrations {
    private static final ResourceLocation CAMERA_HEAD = new ResourceLocation("replaymod", "camera_head.png");
    private static final Minecraft mc = MCVer.getMinecraft();

    private static final int SLOW_PATH_COLOR = 0xffcccc;
    private static final int FAST_PATH_COLOR = 0x660000;
    private static final double FASTEST_PATH_SPEED = 0.01;

    private final ReplayModSimplePathing mod;
    private final ReplayHandler replayHandler;

    public PathPreviewRenderer(ReplayModSimplePathing mod, ReplayHandler replayHandler) {
        this.mod = mod;
        this.replayHandler = replayHandler;
    }

    { on(PostRenderWorldCallback.EVENT, this::renderCameraPath); }
    private void renderCameraPath(MatrixStack matrixStack) {
        if (!replayHandler.getReplaySender().isAsyncMode() || mc.gameSettings.hideGUI) return;

        Entity view = mc.getRenderViewEntity();
        if (view == null) return;

        GuiPathing guiPathing = mod.getGuiPathing();
        if (guiPathing == null) return;
        EntityPositionTracker entityTracker = guiPathing.getEntityTracker();

        SPTimeline timeline = mod.getCurrentTimeline();
        if (timeline == null) return;
        Path path = timeline.getPositionPath();
        if (path.getKeyframes().isEmpty()) return;
        Path timePath = timeline.getTimePath();

        path.update();

        int renderDistance = mc.gameSettings.renderDistanceChunks * 16;
        int renderDistanceSquared = renderDistance * renderDistance;

        Vector3f viewPos = new Vector3f(
                (float) view.posX,
                (float) view.posY
                //#if MC>=10800 && MC<11500
                // Eye height is subtracted to make path appear higher (at eye height) than it actually is (at foot height)
                - view.getEyeHeight(),
                //#else
                //$$ ,
                //#endif
                (float) view.posZ
        );

        //#if MC<11700
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        //#endif
        pushMatrix();
        try {
            //#if MC<11700
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            //#endif

            //#if MC>=11700
            //$$ RenderSystem.getModelViewStack().method_34425(matrixStack.peek().getModel());
            //$$ RenderSystem.applyModelViewMatrix();
            //#elseif MC>=11500
            //$$ RenderSystem.multMatrix(matrixStack.peek().getModel());
            //#endif

            for (PathSegment segment : path.getSegments()) {
                Interpolator interpolator = segment.getInterpolator();
                Keyframe start = segment.getStartKeyframe();
                Keyframe end = segment.getEndKeyframe();
                long diff = (int) (end.getTime() - start.getTime());

                boolean spectator = interpolator.getKeyframeProperties().contains(SpectatorProperty.PROPERTY);
                if (spectator && entityTracker == null) {
                    continue; // Cannot render spectator positions when entity tracker is not yet loaded
                }
                // Spectator segments have 20 lines per second (at least 10) whereas normal segments have a fixed 100
                long steps = spectator ? Math.max(diff / 50, 10) : 100;
                Vector3f prevPos = null;
                for (int i = 0; i <= steps; i++) {
                    long time = start.getTime() + diff * i / steps;
                    if (spectator) {
                        Optional<Integer> entityId = path.getValue(SpectatorProperty.PROPERTY, time);
                        Optional<Integer> replayTime = timePath.getValue(TimestampProperty.PROPERTY, time);
                        if (entityId.isPresent() && replayTime.isPresent()) {
                            Location loc = entityTracker.getEntityPositionAtTimestamp(entityId.get(), replayTime.get());
                            if (loc != null) {
                                Vector3f pos = loc2Vec(loc);
                                if (prevPos != null) {
                                    drawConnection(viewPos, prevPos, pos, 0x0000ffff, renderDistanceSquared);
                                }
                                prevPos = pos;
                                continue;
                            }
                        }
                    } else {
                        Optional<Vector3f> optPos = path.getValue(CameraProperties.POSITION, time).map(this::tripleD2Vec);
                        if (optPos.isPresent()) {
                            Vector3f pos = optPos.get();
                            if (prevPos != null) {
                                double distance = Math.sqrt(distanceSquared(prevPos, pos));
                                double speed = Math.min(distance / (diff / steps), FASTEST_PATH_SPEED);
                                double speedFraction = speed / FASTEST_PATH_SPEED;
                                int color = interpolateColor(SLOW_PATH_COLOR, FAST_PATH_COLOR, speedFraction);
                                drawConnection(viewPos, prevPos, pos, (color << 8) | 0xff, renderDistanceSquared);
                            }
                            prevPos = pos;
                            continue;
                        }
                    }
                    prevPos = null;
                }
            }

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            //#if MC<11700
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            //#endif

            path.getKeyframes().stream()
                    .map(k -> Pair.of(k, k.getValue(CameraProperties.POSITION).map(this::tripleD2Vec)))
                    .filter(p -> p.getRight().isPresent())
                    .map(p -> Pair.of(p.getLeft(), p.getRight().get()))
                    .filter(p -> distanceSquared(p.getRight(), viewPos) < renderDistanceSquared)
                    .sorted(new KeyframeComparator(viewPos)) // Need to render the furthest first
                    .forEachOrdered(p -> drawPoint(viewPos, p.getRight(), p.getLeft()));

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            int time = guiPathing.timeline.getCursorPosition();
            Optional<Integer> entityId = path.getValue(SpectatorProperty.PROPERTY, time);
            if (entityId.isPresent()) {
                // Spectating an entity
                if (entityTracker != null) {
                    Optional<Integer> replayTime = timePath.getValue(TimestampProperty.PROPERTY, time);
                    if (replayTime.isPresent()) {
                        Location loc = entityTracker.getEntityPositionAtTimestamp(entityId.get(), replayTime.get());
                        if (loc != null) {
                            drawCamera(viewPos, loc2Vec(loc), new Vector3f(loc.getYaw(), loc.getPitch(), 0f));
                        }
                    }
                }
            } else {
                // Normal camera path
                Optional<Vector3f> cameraPos = path.getValue(CameraProperties.POSITION, time).map(this::tripleD2Vec);
                Optional<Vector3f> cameraRot = path.getValue(CameraProperties.ROTATION, time).map(this::tripleF2Vec);
                if (cameraPos.isPresent() && cameraRot.isPresent()) {
                    drawCamera(viewPos, cameraPos.get(), cameraRot.get());
                }
            }
        } finally {
            popMatrix();
            //#if MC>=11700
            //$$ GL11.glDisable(GL11.GL_BLEND);
            //#else
            GL11.glPopAttrib();
            //#endif
        }
    }

    private Vector3f loc2Vec(Location loc) {
        return new Vector3f((float) loc.getX(), (float) loc.getY(), (float) loc.getZ());
    }

    private Vector3f tripleD2Vec(Triple<Double, Double, Double> loc) {
        return new Vector3f(loc.getLeft().floatValue(), loc.getMiddle().floatValue(), loc.getRight().floatValue());
    }

    private Vector3f tripleF2Vec(Triple<Float, Float, Float> loc) {
        return new Vector3f(loc.getLeft(), loc.getMiddle(), loc.getRight());
    }

    private static int interpolateColor(int c1, int c2, double weight) {
        return (interpolateColorComponent((c1 >> 16) & 0xff, (c2 >> 16) & 0xff, weight) << 16)
                | (interpolateColorComponent((c1 >> 8) & 0xff, (c2 >> 8) & 0xff, weight) << 8)
                | interpolateColorComponent(c1 & 0xff, c2 & 0xff, weight);
    }

    private static int interpolateColorComponent(int c1, int c2, double weight) {
        return (int) (c1 + (1 - Math.pow(Math.E, -4 * weight)) * (c2 - c1)) & 0xff;
    }

    private static double distanceSquared(Vector3f p1, Vector3f p2) {
        return Vector3f.sub(p1, p2, null).lengthSquared();
    }

    private void drawConnection(Vector3f view, Vector3f pos1, Vector3f pos2, int color, int renderDistanceSquared) {
        if (distanceSquared(view, pos1) > renderDistanceSquared) return;
        if (distanceSquared(view, pos2) > renderDistanceSquared) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        emitLine(buffer, Vector3f.sub(pos1, view, null), Vector3f.sub(pos2, view, null), color);

        //#if MC>=11700
        //$$ RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        //$$ RenderSystem.disableCull();
        //#endif
        GL11.glLineWidth(3);
        tessellator.draw();
        //#if MC>=11700
        //$$ RenderSystem.enableCull();
        //#endif
    }

    private void drawPoint(Vector3f view, Vector3f pos, Keyframe keyframe) {

        bindTexture(TEXTURE);

        float posX = 80f / ReplayMod.TEXTURE_SIZE;
        float posY = 0f;
        float size = 10f / ReplayMod.TEXTURE_SIZE;

        if (mod.isSelected(keyframe)) {
            posY += size;
        }

        if (keyframe.getValue(SpectatorProperty.PROPERTY).isPresent()) {
            posX += size;
        }

        float minX = -0.5f;
        float minY = -0.5f;
        float maxX = 0.5f;
        float maxY = 0.5f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.pos(minX, minY, 0).tex(posX + size, posY + size).endVertex();
        buffer.pos(minX, maxY, 0).tex(posX + size, posY).endVertex();
        buffer.pos(maxX, maxY, 0).tex(posX, posY).endVertex();
        buffer.pos(maxX, minY, 0).tex(posX, posY + size).endVertex();

        pushMatrix();

        Vector3f t = Vector3f.sub(pos, view, null);
        GL11.glTranslatef(t.x, t.y, t.z);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0, 1, 0);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1, 0, 0);

        //#if MC>=11700
        //$$ RenderSystem.applyModelViewMatrix();
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //#endif
        tessellator.draw();

        popMatrix();
    }

    private void drawCamera(Vector3f view, Vector3f pos, Vector3f rot) {

        bindTexture(CAMERA_HEAD);

        pushMatrix();

        Vector3f t = Vector3f.sub(pos, view, null);
        GL11.glTranslatef(t.x, t.y, t.z);
        GL11.glRotatef(-rot.x, 0, 1, 0); // Yaw
        GL11.glRotatef(rot.y, 1, 0, 0); // Pitch
        GL11.glRotatef(rot.z, 0, 0, 1); // Roll

        //draw the position line
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        emitLine(buffer, new Vector3f(0, 0, 0), new Vector3f(0, 0, 2), 0x00ff00aa);

        //#if MC>=11700
        //$$ RenderSystem.applyModelViewMatrix();
        //$$ RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        //#else
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //#endif

        tessellator.draw();

        //#if MC<11700
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //#endif

        // draw camera cube

        float cubeSize = 0.5f;

        double r = -cubeSize/2;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        //back
        buffer.pos(r, r + cubeSize, r).tex(3 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r + cubeSize, r).tex(4*8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r, r).tex(4*8/64f, 2*8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r, r).tex(3*8/64f, 2*8/64f).color(255, 255, 255, 200).endVertex();

        //front
        buffer.pos(r + cubeSize, r, r + cubeSize).tex(2 * 8 / 64f, 2*8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r + cubeSize, r + cubeSize).tex(2 * 8 / 64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r + cubeSize, r + cubeSize).tex(8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r, r + cubeSize).tex(8 / 64f, 2*8/64f).color(255, 255, 255, 200).endVertex();

        //left
        buffer.pos(r + cubeSize, r + cubeSize, r).tex(0, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r + cubeSize, r + cubeSize).tex(8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r, r + cubeSize).tex(8/64f, 2*8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r+cubeSize, r, r).tex(0, 2*8/64f).color(255, 255, 255, 200).endVertex();

        //right
        buffer.pos(r, r + cubeSize, r + cubeSize).tex(2*8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r + cubeSize, r).tex(3*8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r, r).tex(3*8/64f, 2*8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r, r + cubeSize).tex(2 * 8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();

        //bottom
        buffer.pos(r + cubeSize, r, r).tex(3*8/64f, 0).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r, r + cubeSize).tex(3*8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r, r + cubeSize).tex(2*8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r, r).tex(2 * 8 / 64f, 0).color(255, 255, 255, 200).endVertex();

        //top
        buffer.pos(r, r + cubeSize, r).tex(8/64f, 0).color(255, 255, 255, 200).endVertex();
        buffer.pos(r, r + cubeSize, r + cubeSize).tex(8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r + cubeSize, r + cubeSize).tex(2*8/64f, 8/64f).color(255, 255, 255, 200).endVertex();
        buffer.pos(r + cubeSize, r + cubeSize, r).tex(2 * 8 / 64f, 0).color(255, 255, 255, 200).endVertex();

        //#if MC>=11700
        //$$ RenderSystem.applyModelViewMatrix();
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        //#endif
        tessellator.draw();

        popMatrix();
    }

    private class KeyframeComparator implements Comparator<Pair<Keyframe, Vector3f>> {
        private final Vector3f viewPos;

        public KeyframeComparator(Vector3f viewPos) {
            this.viewPos = viewPos;
        }

        @Override
        public int compare(Pair<Keyframe, Vector3f> o1, Pair<Keyframe, Vector3f> o2) {
            return -Double.compare(distanceSquared(o1.getRight(), viewPos), distanceSquared(o2.getRight(), viewPos));
        }
    }
}
