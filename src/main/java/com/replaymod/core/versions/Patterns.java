package com.replaymod.core.versions;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.core.mixin.MinecraftAccessor;
import com.replaymod.gradle.remap.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.ReportedException;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

//#if MC>=11700
//#else
import org.lwjgl.opengl.GL11;
//#endif

//#if MC>=11600
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.client.util.math.Vector3f;
//$$ import net.minecraft.util.math.Matrix4f;
//$$ import net.minecraft.util.math.Quaternion;
//#else
//#endif

//#if MC>=11400
//$$ import net.minecraft.client.gui.widget.Widget;
//$$ import net.minecraft.client.MainWindow;
//$$ import net.minecraft.util.registry.Registry;
//#else
import net.minecraft.client.gui.GuiButton;
//#endif

//#if MC>=11100
import net.minecraft.util.NonNullList;
//#endif

//#if MC>=10904
import net.minecraft.util.SoundEvent;
import net.minecraft.crash.ICrashReportDetail;
//#else
//$$ import java.util.concurrent.Callable;
//#endif

//#if MC>=10809
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//#else
//#endif

//#if MC>=10800
import net.minecraft.client.renderer.BufferBuilder;
//#else
//$$ import net.minecraft.entity.EntityLivingBase;
//#endif

import java.io.IOException;
import java.util.Collection;
import java.util.List;

class Patterns {
    //#if MC>=10904
    @Pattern
    private static void addCrashCallable(CrashReportCategory category, String name, ICrashReportDetail<String> callable) {
        //#if MC>=11200
        category.addDetail(name, callable);
        //#else
        //$$ category.setDetail(name, callable);
        //#endif
    }
    //#else
    //$$ @Pattern
    //$$ private static void addCrashCallable(CrashReportCategory category, String name, Callable<String> callable) {
    //$$     category.addCrashSectionCallable(name, callable);
    //$$ }
    //#endif

    @Pattern
    private static double Entity_getX(Entity entity) {
        //#if MC>=11500
        //$$ return entity.getX();
        //#else
        return entity.posX;
        //#endif
    }

    @Pattern
    private static double Entity_getY(Entity entity) {
        //#if MC>=11500
        //$$ return entity.getY();
        //#else
        return entity.posY;
        //#endif
    }

    @Pattern
    private static double Entity_getZ(Entity entity) {
        //#if MC>=11500
        //$$ return entity.getZ();
        //#else
        return entity.posZ;
        //#endif
    }

    @Pattern
    private static void Entity_setYaw(Entity entity, float value) {
        //#if MC>=11700
        //$$ entity.setYaw(value);
        //#else
        entity.rotationYaw = value;
        //#endif
    }

    @Pattern
    private static float Entity_getYaw(Entity entity) {
        //#if MC>=11700
        //$$ return entity.getYaw();
        //#else
        return entity.rotationYaw;
        //#endif
    }

    @Pattern
    private static void Entity_setPitch(Entity entity, float value) {
        //#if MC>=11700
        //$$ entity.setPitch(value);
        //#else
        entity.rotationPitch = value;
        //#endif
    }

    @Pattern
    private static float Entity_getPitch(Entity entity) {
        //#if MC>=11700
        //$$ return entity.getPitch();
        //#else
        return entity.rotationPitch;
        //#endif
    }

    @Pattern
    private static void Entity_setPos(Entity entity, double x, double y, double z) {
        //#if MC>=11500
        //$$ entity.setPos(x, y, z);
        //#else
        { net.minecraft.entity.Entity self = entity; self.posX = x; self.posY = y; self.posZ = z; }
        //#endif
    }

    @Pattern
    private static int getX(GuiButton button) {
        //#if MC>=11903
        //$$ return button.getX();
        //#else
        return button.x;
        //#endif
    }

    @Pattern
    private static int getY(GuiButton button) {
        //#if MC>=11903
        //$$ return button.getY();
        //#else
        return button.y;
        //#endif
    }

    @Pattern
    private static void setX(GuiButton button, int value) {
        //#if MC>=11903
        //$$ button.setX(value);
        //#else
        button.x = value;
        //#endif
    }

    @Pattern
    private static void setY(GuiButton button, int value) {
        //#if MC>=11903
        //$$ button.setY(value);
        //#else
        button.y = value;
        //#endif
    }

    //#if MC>=11400
    //$$ @Pattern
    //$$ private static void setWidth(Widget button, int value) {
    //$$     button.setWidth(value);
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static int getWidth(Widget button) {
    //$$     return button.getWidth();
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static int getHeight(Widget button) {
        //#if MC>=11600
        //$$ return button.getHeight();
        //#else
        //$$ return ((com.replaymod.core.mixin.AbstractButtonWidgetAccessor) button).getHeight();
        //#endif
    //$$ }
    //#else
    @Pattern
    private static void setWidth(GuiButton button, int value) {
        button.width = value;
    }

    @Pattern
    private static int getWidth(GuiButton button) {
        return button.width;
    }

    @Pattern
    private static int getHeight(GuiButton button) {
        return button.height;
    }
    //#endif

    @Pattern
    private static String readString(PacketBuffer buffer, int max) {
        //#if MC>=10800
        return buffer.readString(max);
        //#else
        //$$ return com.replaymod.core.versions.MCVer.tryReadString(buffer, max);
        //#endif
    }

    @Pattern
    //#if MC>=10800
    private static Entity getRenderViewEntity(Minecraft mc) {
        return mc.getRenderViewEntity();
    }
    //#else
    //$$ private static EntityLivingBase getRenderViewEntity(Minecraft mc) {
    //$$     return mc.renderViewEntity;
    //$$ }
    //#endif

    @Pattern
    //#if MC>=10800
    private static void setRenderViewEntity(Minecraft mc, Entity entity) {
        mc.setRenderViewEntity(entity);
    }
    //#else
    //$$ private static void setRenderViewEntity(Minecraft mc, EntityLivingBase entity) {
    //$$     mc.renderViewEntity = entity;
    //$$ }
    //#endif

    @Pattern
    private static Entity getVehicle(Entity passenger) {
        //#if MC>=10904
        return passenger.getRidingEntity();
        //#else
        //$$ return passenger.ridingEntity;
        //#endif
    }

    @Pattern
    private static InventoryPlayer getInventory(EntityPlayer entity) {
        //#if MC>=11700
        //$$ return entity.getInventory();
        //#else
        return entity.inventory;
        //#endif
    }

    @Pattern
    private static Iterable<Entity> loadedEntityList(WorldClient world) {
        //#if MC>=11400
        //$$ return world.getAllEntities();
        //#else
        //#if MC>=10809
        return world.loadedEntityList;
        //#else
        //$$ return ((java.util.List<net.minecraft.entity.Entity>) world.loadedEntityList);
        //#endif
        //#endif
    }

    @Pattern
    //#if MC>=11700
    //$$ private static void getEntitySectionArray() {}
    //#else
    private static Collection<Entity>[] getEntitySectionArray(Chunk chunk) {
        //#if MC>=11700
        //$$ return obsolete(chunk);
        //#elseif MC>=10800
        return chunk.getEntityLists();
        //#else
        //$$ return chunk.entityLists;
        //#endif
    }
    //#endif

    @Pattern
    private static List<? extends EntityPlayer> playerEntities(World world) {
        //#if MC>=11400
        //$$ return world.getPlayers();
        //#elseif MC>=10809
        return world.playerEntities;
        //#else
        //$$ return ((List<? extends net.minecraft.entity.player.EntityPlayer>) world.playerEntities);
        //#endif
    }

    @Pattern
    private static boolean isOnMainThread(Minecraft mc) {
        //#if MC>=11400
        //$$ return mc.isOnExecutionThread();
        //#else
        return mc.isCallingFromMinecraftThread();
        //#endif
    }

    @Pattern
    private static void scheduleOnMainThread(Minecraft mc, Runnable runnable) {
        //#if MC>=11400
        //$$ mc.enqueue(runnable);
        //#else
        mc.addScheduledTask(runnable);
        //#endif
    }

    @Pattern
    private static Window getWindow(Minecraft mc) {
        //#if MC>=11500
        //$$ return mc.getWindow();
        //#elseif MC>=11400
        //$$ return mc.mainWindow;
        //#else
        return new com.replaymod.core.versions.Window(mc);
        //#endif
    }

    @Pattern
    private static BufferBuilder Tessellator_getBuffer(Tessellator tessellator) {
        //#if MC>=10800
        return tessellator.getBuffer();
        //#else
        //$$ return new BufferBuilder(tessellator);
        //#endif
    }

    //#if MC<11700
    @Pattern
    private static void BufferBuilder_beginPosCol(BufferBuilder buffer, int mode) {
        //#if MC>=10809
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR);
        //#else
        //$$ buffer.startDrawing(mode /* POSITION_COLOR */);
        //#endif
    }

    @Pattern
    private static void BufferBuilder_addPosCol(BufferBuilder buffer, double x, double y, double z, int r, int g, int b, int a) {
        //#if MC>=10809
        buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        //#else
        //$$ { WorldRenderer $buffer = buffer; double $x = x; double $y = y; double $z = z; $buffer.setColorRGBA(r, g, b, a); $buffer.addVertex($x, $y, $z); }
        //#endif
    }

    @Pattern
    private static void BufferBuilder_beginPosTex(BufferBuilder buffer, int mode) {
        //#if MC>=10809
        buffer.begin(mode, DefaultVertexFormats.POSITION_TEX);
        //#else
        //$$ buffer.startDrawing(mode /* POSITION_TEXTURE */);
        //#endif
    }

    @Pattern
    private static void BufferBuilder_addPosTex(BufferBuilder buffer, double x, double y, double z, float u, float v) {
        //#if MC>=10809
        buffer.pos(x, y, z).tex(u, v).endVertex();
        //#else
        //$$ buffer.addVertexWithUV(x, y, z, u, v);
        //#endif
    }

    @Pattern
    private static void BufferBuilder_beginPosTexCol(BufferBuilder buffer, int mode) {
        //#if MC>=10809
        buffer.begin(mode, DefaultVertexFormats.POSITION_TEX_COLOR);
        //#else
        //$$ buffer.startDrawing(mode /* POSITION_TEXTURE_COLOR */);
        //#endif
    }

    @Pattern
    private static void BufferBuilder_addPosTexCol(BufferBuilder buffer, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
        //#if MC>=10809
        buffer.pos(x, y, z).tex(u, v).color(r, g, b, a).endVertex();
        //#else
        //$$ { WorldRenderer $buffer = buffer; double $x = x; double $y = y; double $z = z; float $u = u; float $v = v; $buffer.setColorRGBA(r, g, b, a); $buffer.addVertexWithUV($x, $y, $z, $u, $v); }
        //#endif
    }
    //#else
    //$$ @Pattern private static void BufferBuilder_beginPosCol() {}
    //$$ @Pattern private static void BufferBuilder_addPosCol() {}
    //$$ @Pattern private static void BufferBuilder_beginPosTex() {}
    //$$ @Pattern private static void BufferBuilder_addPosTex() {}
    //$$ @Pattern private static void BufferBuilder_beginPosTexCol() {}
    //$$ @Pattern private static void BufferBuilder_addPosTexCol() {}
    //#endif

    @Pattern
    private static Tessellator Tessellator_getInstance() {
        //#if MC>=10800
        return Tessellator.getInstance();
        //#else
        //$$ return Tessellator.instance;
        //#endif
    }

    @Pattern
    private static RenderManager getEntityRenderDispatcher(Minecraft mc) {
        //#if MC>=10800
        return mc.getRenderManager();
        //#else
        //$$ return com.replaymod.core.versions.MCVer.getRenderManager(mc);
        //#endif
    }

    @Pattern
    private static float getCameraYaw(RenderManager dispatcher) {
        //#if MC>=11500
        //$$ return dispatcher.camera.getYaw();
        //#else
        return dispatcher.playerViewY;
        //#endif
    }

    @Pattern
    private static float getCameraPitch(RenderManager dispatcher) {
        //#if MC>=11500
        //$$ return dispatcher.camera.getPitch();
        //#else
        return dispatcher.playerViewX;
        //#endif
    }

    @Pattern
    private static float getRenderPartialTicks(Minecraft mc) {
        //#if MC>=10900
        return mc.getRenderPartialTicks();
        //#else
        //$$ return ((com.replaymod.core.mixin.MinecraftAccessor) mc).getTimer().renderPartialTicks;
        //#endif
    }

    @Pattern
    private static TextureManager getTextureManager(Minecraft mc) {
        //#if MC>=11400
        //$$ return mc.getTextureManager();
        //#else
        return mc.renderEngine;
        //#endif
    }

    @Pattern
    private static String getBoundKeyName(KeyBinding keyBinding) {
        //#if MC>=11600
        //$$ return keyBinding.getBoundKeyLocalizedText().getString();
        //#elseif MC>=11400
        //$$ return keyBinding.getLocalizedName();
        //#else
        return org.lwjgl.input.Keyboard.getKeyName(keyBinding.getKeyCode());
        //#endif
    }

    @Pattern
    private static PositionedSoundRecord master(ResourceLocation sound, float pitch) {
        //#if MC>=10900
        return PositionedSoundRecord.getMasterRecord(new SoundEvent(sound), pitch);
        //#elseif MC>=10800
        //$$ return PositionedSoundRecord.create(sound, pitch);
        //#else
        //$$ return PositionedSoundRecord.createPositionedSoundRecord(sound, pitch);
        //#endif
    }

    @Pattern
    private static boolean isKeyBindingConflicting(KeyBinding a, KeyBinding b) {
        //#if MC>=10900
        return a.conflicts(b);
        //#else
        //$$ return (a.getKeyCode() == b.getKeyCode());
        //#endif
    }

    //#if MC>=11600
    //$$ @Pattern
    //$$ private static void BufferBuilder_beginLineStrip(BufferBuilder buffer, VertexFormat vertexFormat) {
        //#if MC>=11700
        //$$ buffer.begin(net.minecraft.client.render.VertexFormat.DrawMode.LINE_STRIP, VertexFormats.LINES);
        //#else
        //$$ buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static void BufferBuilder_beginLines(BufferBuilder buffer) {
        //#if MC>=11700
        //$$ buffer.begin(net.minecraft.client.render.VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        //#else
        //$$ buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static void BufferBuilder_beginQuads(BufferBuilder buffer, VertexFormat vertexFormat) {
        //#if MC>=11700
        //$$ buffer.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, vertexFormat);
        //#else
        //$$ buffer.begin(GL11.GL_QUADS, vertexFormat);
        //#endif
    //$$ }
    //#else
    @Pattern private static void BufferBuilder_beginLineStrip() {}
    @Pattern private static void BufferBuilder_beginLines() {}
    @Pattern private static void BufferBuilder_beginQuads() {}
    //#endif

    @Pattern
    private static void GL11_glLineWidth(float width) {
        //#if MC>=11700
        //$$ com.mojang.blaze3d.systems.RenderSystem.lineWidth(width);
        //#else
        GL11.glLineWidth(width);
        //#endif
    }

    @Pattern
    private static void GL11_glTranslatef(float x, float y, float z) {
        //#if MC>=11700
        //$$ com.mojang.blaze3d.systems.RenderSystem.getModelViewStack().translate(x, y, z);
        //#else
        GL11.glTranslatef(x, y, z);
        //#endif
    }

    @Pattern
    private static void GL11_glRotatef(float angle, float x, float y, float z) {
        //#if MC>=11700
        //$$ com.mojang.blaze3d.systems.RenderSystem.getModelViewStack().multiply(com.replaymod.core.versions.MCVer.quaternion(angle, new net.minecraft.util.math.Vec3f(x, y, z)));
        //#else
        GL11.glRotatef(angle, x, y, z);
        //#endif
    }

    // FIXME preprocessor bug: there are mappings for this, not sure why it doesn't remap by itself
    //#if MC>=11600
    //$$ @Pattern
    //$$ private static Matrix4f getPositionMatrix(MatrixStack.Entry stack) {
        //#if MC>=11800
        //$$ return stack.getPositionMatrix();
        //#else
        //$$ return stack.getModel();
        //#endif
    //$$ }
    //#else
    private static void getPositionMatrix() {}
    //#endif

    @SuppressWarnings("rawtypes") // preprocessor bug: doesn't work with generics
    @Pattern
    private static void Futures_addCallback(ListenableFuture future, FutureCallback callback) {
        //#if MC>=11800
        //$$ Futures.addCallback(future, callback, Runnable::run);
        //#else
        Futures.addCallback(future, callback);
        //#endif
    }

    @Pattern
    private static void setCrashReport(Minecraft mc, CrashReport report) {
        //#if MC>=11900
        //$$ mc.setCrashReportSupplier(report);
        //#elseif MC>=11800
        //$$ mc.setCrashReportSupplier(() -> report);
        //#else
        mc.crashed(report);
        //#endif
    }

    @Pattern
    private static ReportedException crashReportToException(Minecraft mc) {
        //#if MC>=11800
        //$$ return new CrashException(((MinecraftAccessor) mc).getCrashReporter().get());
        //#else
        return new ReportedException(((MinecraftAccessor) mc).getCrashReporter());
        //#endif
    }

    @Pattern
    private static Vec3d getTrackedPosition(Entity entity) {
        //#if MC>=11604
        //$$ return entity.getTrackedPosition();
        //#else
        return com.replaymod.core.versions.MCVer.getTrackedPosition(entity);
        //#endif
    }

    @Pattern
    private static ITextComponent newTextLiteral(String str) {
        //#if MC>=11900
        //$$ return net.minecraft.text.Text.literal(str);
        //#else
        return new TextComponentString(str);
        //#endif
    }

    @Pattern
    private static ITextComponent newTextTranslatable(String key, Object...args) {
        //#if MC>=11900
        //$$ return net.minecraft.text.Text.translatable(key, args);
        //#else
        return new TextComponentTranslation(key, args);
        //#endif
    }

    //#if MC>=11500
    //$$ @Pattern
    //$$ private static Vec3d getTrackedPos(Entity entity) {
        //#if MC>=11900
        //$$ return entity.getTrackedPosition().withDelta(0, 0, 0);
        //#else
        //$$ return com.replaymod.core.versions.MCVer.getTrackedPosition(entity);
        //#endif
    //$$ }
    //#else
    @Pattern private static void getTrackedPos() {}
    //#endif

    @Pattern
    private static void setGamma(GameSettings options, double value) {
        //#if MC>=11900
        //$$ ((com.replaymod.core.mixin.SimpleOptionAccessor<Double>) (Object) options.getGamma()).setRawValue(value);
        //#elseif MC>=11400
        //$$ options.gamma = value;
        //#else
        options.gammaSetting = (float) value;
        //#endif
    }

    @Pattern
    private static double getGamma(GameSettings options) {
        //#if MC>=11900
        //$$ return options.getGamma().getValue();
        //#else
        return options.gammaSetting;
        //#endif
    }

    @Pattern
    private static int getViewDistance(GameSettings options) {
        //#if MC>=11900
        //$$ return options.getViewDistance().getValue();
        //#else
        return options.renderDistanceChunks;
        //#endif
    }

    @Pattern
    private static double getFov(GameSettings options) {
        //#if MC>=11900
        //$$ return options.getFov().getValue();
        //#else
        return options.fovSetting;
        //#endif
    }

    @Pattern
    private static int getGuiScale(GameSettings options) {
        //#if MC>=11900
        //$$ return options.getGuiScale().getValue();
        //#else
        return options.guiScale;
        //#endif
    }

    @Pattern
    private static IResource getResource(IResourceManager manager, ResourceLocation id) throws IOException {
        //#if MC>=11900
        //$$ return manager.getResourceOrThrow(id);
        //#else
        return manager.getResource(id);
        //#endif
    }

    @Pattern
    private static List<ItemStack> DefaultedList_ofSize_ItemStack_Empty(int size) {
        //#if MC>=11100
        return NonNullList.withSize(size, ItemStack.EMPTY);
        //#else
        //$$ return java.util.Arrays.asList(new ItemStack[size]);
        //#endif
    }

    @Pattern
    private static void setSoundVolume(GameSettings options, SoundCategory category, float value) {
        //#if MC>=11903
        //$$ options.getSoundVolumeOption(category).setValue((double) value);
        //#else
        options.setSoundLevel(category, value);
        //#endif
    }

    //#if MC>=10900
    @Pattern
    private static SoundEvent SoundEvent_of(ResourceLocation identifier) {
        //#if MC>=11903
        //$$ return SoundEvent.of(identifier);
        //#else
        return new SoundEvent(identifier);
        //#endif
    }
    //#else
    //$$ @Pattern private static void SoundEvent_of() {}
    //#endif

    //#if MC>=11600
    //$$ @Pattern
    //$$ private static Vector3f POSITIVE_X() {
        //#if MC>=11903
        //$$ return new org.joml.Vector3f(1, 0, 0);
        //#else
        //$$ return Vector3f.POSITIVE_X;
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static Vector3f POSITIVE_Y() {
        //#if MC>=11903
        //$$ return new org.joml.Vector3f(0, 1, 0);
        //#else
        //$$ return Vector3f.POSITIVE_Y;
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static Vector3f POSITIVE_Z() {
        //#if MC>=11903
        //$$ return new org.joml.Vector3f(0, 0, 1);
        //#else
        //$$ return Vector3f.POSITIVE_Z;
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static Quaternion getDegreesQuaternion(Vector3f axis, float angle) {
        //#if MC>=11903
        //$$ return new org.joml.Quaternionf().fromAxisAngleDeg(axis, angle);
        //#else
        //$$ return axis.getDegreesQuaternion(angle);
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static void Quaternion_mul(Quaternion left, Quaternion right) {
        //#if MC>=11903
        //$$ left.mul(right);
        //#else
        //$$ left.hamiltonProduct(right);
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static float Quaternion_getX(Quaternion q) {
        //#if MC>=11903
        //$$ return q.x;
        //#else
        //$$ return q.getX();
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static float Quaternion_getY(Quaternion q) {
        //#if MC>=11903
        //$$ return q.y;
        //#else
        //$$ return q.getY();
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static float Quaternion_getZ(Quaternion q) {
        //#if MC>=11903
        //$$ return q.z;
        //#else
        //$$ return q.getZ();
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static float Quaternion_getW(Quaternion q) {
        //#if MC>=11903
        //$$ return q.w;
        //#else
        //$$ return q.getW();
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static Quaternion Quaternion_copy(Quaternion source) {
        //#if MC>=11903
        //$$ return new org.joml.Quaternionf(source);
        //#else
        //$$ return source.copy();
        //#endif
    //$$ }
    //#else
    @Pattern private static void POSITIVE_X() {}
    @Pattern private static void POSITIVE_Y() {}
    @Pattern private static void POSITIVE_Z() {}
    @Pattern private static void getDegreesQuaternion() {}
    @Pattern private static void Quaternion_mul() {}
    @Pattern private static void Quaternion_getX() {}
    @Pattern private static void Quaternion_getY() {}
    @Pattern private static void Quaternion_getZ() {}
    @Pattern private static void Quaternion_getW() {}
    @Pattern private static void Quaternion_copy() {}
    //#endif

    //#if MC>=11600
    //$$ @Pattern
    //$$ private static void Matrix4f_multiply(Matrix4f left, Matrix4f right) {
        //#if MC>=11903
        //$$ left.mul(right);
        //#else
        //$$ left.multiply(right);
        //#endif
    //$$ }
    //$$
    //$$ @Pattern
    //$$ private static Matrix4f Matrix4f_translate(float x, float y, float z) {
        //#if MC>=11903
        //$$ return new Matrix4f().translation(x, y, z);
        //#else
        //$$ return Matrix4f.translate(x, y, z);
        //#endif
    //$$ }
    //#else
    @Pattern private static void Matrix4f_multiply() {}
    @Pattern private static void Matrix4f_translate() {}
    //#endif

    //#if MC>=11700
    //$$ @Pattern
    //$$ private static Matrix4f Matrix4f_perspectiveMatrix(float left, float right, float top, float bottom, float zNear, float zFar) {
        //#if MC>=11903
        //$$ return com.replaymod.core.versions.MCVer.ortho(left, right, top, bottom, zNear, zFar);
        //#else
        //$$ return Matrix4f.projectionMatrix(left, right, top, bottom, zNear, zFar);
        //#endif
    //$$ }
    //#else
    @Pattern private static void Matrix4f_perspectiveMatrix() {}
    //#endif

    //#if MC>=11400
    //$$ @Pattern
    //$$ private static Registry<? extends Registry<?>> REGISTRIES() {
        //#if MC>=11903
        //$$ return net.minecraft.registry.Registries.REGISTRIES;
        //#else
        //$$ return Registry.REGISTRY;
        //#endif
    //$$ }
    //#else
    @Pattern private static void REGISTRIES() {}
    //#endif

    @Pattern
    public World getWorld(Entity entity) {
        //#if MC>=12000
        //$$ return entity.getWorld();
        //#else
        return entity.world;
        //#endif
    }
}
