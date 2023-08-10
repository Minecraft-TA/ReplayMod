package com.replaymod.replay.camera;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.events.RenderHotbarCallback;
import com.replaymod.replay.events.RenderSpectatorCrosshairCallback;
import com.replaymod.replay.mixin.EntityPlayerAccessor;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.mixin.FirstPersonRendererAccessor;
import com.replaymod.replaystudio.util.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

//#if FABRIC>=1
//#else
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//#endif

//#if MC>=11400
//$$ import net.minecraft.client.world.ClientWorld;
//$$ import net.minecraft.fluid.Fluid;
//#if MC>=11802
//$$ import net.minecraft.tag.TagKey;
//#else
//$$ import net.minecraft.tags.Tag;
//#endif
//$$ import net.minecraft.util.math.BlockRayTraceResult;
//$$ import net.minecraft.util.math.RayTraceResult;
//#else
import com.replaymod.replay.events.ReplayChatMessageEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

//#if MC>=11400
//$$ import net.minecraft.util.math.RayTraceFluidMode;
//#else
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
//#endif
//#endif

//#if MC>=10904
import net.minecraft.inventory.EntityEquipmentSlot;
//#if MC>=11200
//#if MC>=11400
//$$ import net.minecraft.client.util.ClientRecipeBook;
//#else
import net.minecraft.stats.RecipeBook;
//#endif
//#endif
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumHand;
//#endif

//#if MC>=10800
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EnumPlayerModelParts;
//#else
//$$ import net.minecraft.client.entity.EntityClientPlayerMP;
//$$ import net.minecraft.util.Session;
//#endif

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.replaymod.core.versions.MCVer.*;

/**
 * The camera entity used as the main player entity during replay viewing.
 * During a replay the player should be an instance of this class.
 * Camera movement is controlled by a separate {@link CameraController}.
 */
@SuppressWarnings("EntityConstructor")
public class CameraEntity
        //#if MC>=10800
        extends EntityPlayerSP
        //#else
        //$$ extends EntityClientPlayerMP
        //#endif
{
    private static final UUID CAMERA_UUID = UUID.nameUUIDFromBytes("ReplayModCamera".getBytes(StandardCharsets.UTF_8));

    /**
     * Roll of this camera in degrees.
     */
    public float roll;

    private CameraController cameraController;

    private long lastControllerUpdate = System.currentTimeMillis();

    /**
     * The entity whose hand was the last one rendered.
     */
    private Entity lastHandRendered = null;

    /**
     * The hashCode and equals methods of Entity are not stable.
     * Therefore we cannot register any event handlers directly in the CameraEntity class and
     * instead have this inner class.
     */
    private EventHandler eventHandler = new EventHandler();

    public CameraEntity(
            Minecraft mcIn,
            //#if MC>=11400
            //$$ ClientWorld worldIn,
            //#else
            World worldIn,
            //#endif
            //#if MC<10800
            //$$ Session session,
            //#endif
            NetHandlerPlayClient netHandlerPlayClient,
            StatisticsManager statisticsManager
            //#if MC>=11200
            //#if MC>=11400
            //$$ , ClientRecipeBook recipeBook
            //#else
            , RecipeBook recipeBook
            //#endif
            //#endif
    ) {
        super(mcIn,
                worldIn,
                //#if MC<10800
                //$$ session,
                //#endif
                netHandlerPlayClient,
                statisticsManager
                //#if MC>=11200
                , recipeBook
                //#endif
                //#if MC>=11600
                //$$ , false
                //$$ , false
                //#endif
        );
        //#if MC>=10900
        setUniqueId(CAMERA_UUID);
        //#else
        //$$ entityUniqueID = CAMERA_UUID;
        //#endif
        eventHandler.register();
        if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
            cameraController = ReplayModReplay.instance.createCameraController(this);
        } else {
            cameraController = new SpectatorCameraController(this);
        }
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public void setCameraController(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    /**
     * Moves the camera by the specified delta.
     * @param x Delta in X direction
     * @param y Delta in Y direction
     * @param z Delta in Z direction
     */
    public void moveCamera(double x, double y, double z) {
        setCameraPosition(this.posX + x, this.posY + y, this.posZ + z);
    }

    /**
     * Set the camera position.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public void setCameraPosition(double x, double y, double z) {
        this.lastTickPosX = this.prevPosX = x;
        this.lastTickPosY = this.prevPosY = y;
        this.lastTickPosZ = this.prevPosZ = z;
        { net.minecraft.entity.Entity self = this; self.posX = x; self.posY = y; self.posZ = z; }
        updateBoundingBox();
    }

    /**
     * Sets the camera rotation.
     * @param yaw Yaw in degrees
     * @param pitch Pitch in degrees
     * @param roll Roll in degrees
     */
    public void setCameraRotation(float yaw, float pitch, float roll) {
        this.prevRotationYaw = yaw;
        this.prevRotationPitch = pitch;
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        this.roll = roll;
    }

    /**
     * Sets the camera position and rotation to that of the specified AdvancedPosition
     * @param pos The position and rotation to set
     */
    public void setCameraPosRot(Location pos) {
        setCameraRotation(pos.getYaw(), pos.getPitch(), roll);
        setCameraPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Sets the camera position and rotation to that of the specified entity.
     * @param to The entity whose position to copy
     */
    public void setCameraPosRot(Entity to) {
        if (to == this) return;
        //#if MC>=10800
        float yOffset = 0;
        //#else
        //$$ float yOffset = 1.62f; // Magic value (eye height) from EntityRenderer#orientCamera
        //#endif
        this.prevPosX = to.prevPosX;
        this.prevPosY = to.prevPosY + yOffset;
        this.prevPosZ = to.prevPosZ;
        this.prevRotationYaw = to.prevRotationYaw;
        this.prevRotationPitch = to.prevRotationPitch;
        { net.minecraft.entity.Entity self = this; self.posX = to.posX; self.posY = to.posY; self.posZ = to.posZ; }
        this.rotationYaw = to.rotationYaw;
        this.rotationPitch = to.rotationPitch;
        this.lastTickPosX = to.lastTickPosX;
        this.lastTickPosY = to.lastTickPosY + yOffset;
        this.lastTickPosZ = to.lastTickPosZ;
        this.wrapArmYaw();
        updateBoundingBox();
    }

    //#if MC>=11400
    //$$ @Override
    //$$ public float getYaw(float tickDelta) {
    //$$     Entity view = this.mc.getRenderViewEntity();
    //$$     if (view != null && view != this) {
    //$$         return this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * tickDelta;
    //$$     }
    //$$     return super.getYaw(tickDelta);
    //$$ }
    //$$
    //$$ @Override
    //$$ public float getPitch(float tickDelta) {
    //$$     Entity view = this.mc.getRenderViewEntity();
    //$$     if (view != null && view != this) {
    //$$         return this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * tickDelta;
    //$$     }
    //$$     return super.getPitch(tickDelta);
    //$$ }
    //#endif

    private void updateBoundingBox() {
        //#if MC>=11400
        //$$ float width = getWidth();
        //$$ float height = getHeight();
        //#endif
        //#if MC>=10800
        setEntityBoundingBox(new AxisAlignedBB(
        //#else
        //$$ this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(
        //#endif
                this.posX - width / 2, this.posY, this.posZ - width / 2,
                this.posX + width / 2, this.posY + height, this.posZ + width / 2));
    }

    @Override
    public void onUpdate() {
        //#if MC>=10800
        Entity view =
        //#else
        //$$ EntityLivingBase view =
        //#endif
            this.mc.getRenderViewEntity();
        if (view != null) {
            // Make sure we're always spectating the right entity
            // This is important if the spectated player respawns as their
            // entity is recreated and we have to spectate a new entity
            UUID spectating = ReplayModReplay.instance.getReplayHandler().getSpectatedUUID();
            if (spectating != null && (view.getUniqueID() != spectating
                    || view.world != this.world)
                    || this.world.getEntityByID(view.getEntityId()) != view) {
                if (spectating == null) {
                    // Entity (non-player) died, stop spectating
                    ReplayModReplay.instance.getReplayHandler().spectateEntity(this);
                    return;
                }
                view = this.world.getPlayerEntityByUUID(spectating);
                if (view != null) {
                    this.mc.setRenderViewEntity(view);
                } else {
                    this.mc.setRenderViewEntity(this);
                    return;
                }
            }
            // Move cmera to their position so when we exit the first person view
            // we don't jump back to where we entered it
            if (view != this) {
                setCameraPosRot(view);
            }
        }
    }

    @Override
    public void preparePlayerToSpawn() {
        // Make sure our world is up-to-date in case of world changes
        if (this.mc.world != null) {
            // FIXME cannot use Patters because `setWorld` is `protected` in 1.20
            //#if MC>=12000
            //$$ this.setWorld(this.client.world);
            //#else
            this.world = this.mc.world;
            //#endif
        }
        super.preparePlayerToSpawn();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        if (this.mc.getRenderViewEntity() == this) {
            // Only update camera rotation when the camera is the view
            super.setRotation(yaw, pitch);
        }
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return falseUnlessSpectating(Entity::isEntityInsideOpaqueBlock); // Make sure no suffocation overlay is rendered
    }

    //#if MC<11400
    @Override
    public boolean isInsideOfMaterial(Material materialIn) {
        return falseUnlessSpectating(e -> e.isInsideOfMaterial(materialIn)); // Make sure no overlays are rendered
    }
    //#endif

    //#if MC>=11400
    //$$ @Override
    //$$ public boolean areEyesInFluid(
            //#if MC>=11802
            //$$ TagKey<Fluid> fluid
            //#else
            //$$ Tag<Fluid> fluid
            //#endif
    //$$ ) {
    //$$     return falseUnlessSpectating(entity -> entity.areEyesInFluid(fluid));
    //$$ }
    //$$
    //$$ @Override
    //$$ public float getWaterBrightness() {
    //$$     return falseUnlessSpectating(__ -> true) ? super.getWaterBrightness() : 1f;
    //$$ }
    //#else
    //#if MC>=10800
    @Override
    public boolean isInLava() {
        return falseUnlessSpectating(Entity::isInLava); // Make sure no lava overlay is rendered
    }
    //#else
    //$$ @Override
    //$$ public boolean handleLavaMovement() {
    //$$     return falseUnlessSpectating(Entity::handleLavaMovement); // Make sure no lava overlay is rendered
    //$$ }
    //#endif

    @Override
    public boolean isInWater() {
        return falseUnlessSpectating(Entity::isInWater); // Make sure no water overlay is rendered
    }
    //#endif

    @Override
    public boolean isBurning() {
        return falseUnlessSpectating(Entity::isBurning); // Make sure no fire overlay is rendered
    }

    private boolean falseUnlessSpectating(Function<Entity, Boolean> property) {
        Entity view = this.mc.getRenderViewEntity();
        if (view != null && view != this) {
            return property.apply(view);
        }
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false; // We are in full control of ourselves
    }

    //#if MC>=10800
    @Override
    protected void createRunningParticles() {
        // We do not produce any particles, we are a camera
    }
    //#endif

    @Override
    public boolean canBeCollidedWith() {
        return false; // We are a camera, we cannot collide
    }

    //#if MC>=10800
    @Override
    public boolean isSpectator() {
        ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
        return replayHandler == null || replayHandler.isCameraView(); // Make sure we're treated as spectator
    }
    //#endif

    //#if MC>=11400
    //$$ @Override
    //$$ public boolean isInRangeToRender3d(double double_1, double double_2, double double_3) {
    //$$     return false; // never render the camera otherwise it'd be visible e.g. in 3rd-person or with shaders
    //$$ }
    //#else
    @Override
    public boolean shouldRenderInPass(int pass) {
        // Never render the camera
        // This is necessary to hide the player head in third person mode and to not
        // cause any unwanted shadows when rendering with shaders.
        return false;
    }
    //#endif

    //#if MC>=10800
    @Override
    public float getFovModifier() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) view).getFovModifier();
        }
        return 1;
    }
    //#else
    //$$ @Override
    //$$ public float getFOVMultiplier() {
    //$$     return 1;
    //$$ }
    //#endif

    @Override
    public boolean isInvisible() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this) {
            return view.isInvisible();
        }
        return super.isInvisible();
    }

    @Override
    public ResourceLocation getLocationSkin() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) view).getLocationSkin();
        }
        return super.getLocationSkin();
    }

    //#if MC>=10800
    @Override
    public String getSkinType() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) view).getSkinType();
        }
        return super.getSkinType();
    }

    @Override
    public boolean isWearing(EnumPlayerModelParts modelPart) {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).isWearing(modelPart);
        }
        return super.isWearing(modelPart);
    }
    //#endif

    //#if MC>=10904
    @Override
    public EnumHandSide getPrimaryHand() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getPrimaryHand();
        }
        return super.getPrimaryHand();
    }
    //#endif

    @Override
    public float getSwingProgress(float renderPartialTicks) {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getSwingProgress(renderPartialTicks);
        }
        return 0;
    }

    //#if MC>=10904
    @Override
    public float getCooldownPeriod() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getCooldownPeriod();
        }
        return 1;
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getCooledAttackStrength(adjustTicks);
        }
        // Default to 1 as to not render the cooldown indicator (renders for < 1)
        return 1;
    }

    @Override
    public EnumHand getActiveHand() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getActiveHand();
        }
        return super.getActiveHand();
    }

    @Override
    public boolean isHandActive() {
        Entity view = this.mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).isHandActive();
        }
        return super.isHandActive();
    }

    //#if MC>=11400
    //$$ @Override
    //#if MC>=11900
    //$$ public void onEquipStack(EquipmentSlot slot, ItemStack stack, ItemStack itemStack) {
    //#else
    //$$ protected void playEquipSound(ItemStack itemStack_1) {
    //#endif
    //$$     // Suppress equip sounds
    //$$ }
    //#endif

    //#if MC>=11400
    //$$ @Override
    //$$ public RayTraceResult func_213324_a(double maxDistance, float tickDelta, boolean fluids) {
    //$$     RayTraceResult result = super.func_213324_a(maxDistance, tickDelta, fluids);
    //$$
    //$$     // Make sure we can never look at blocks (-> no outline)
    //$$     if (result instanceof BlockRayTraceResult) {
    //$$         BlockRayTraceResult blockResult = (BlockRayTraceResult) result;
    //$$         result = BlockRayTraceResult.createMiss(result.getHitVec(), blockResult.getFace(), blockResult.getPos());
    //$$     }
    //$$
    //$$     return result;
    //$$ }
    //#else
    //#if MC>=11400
    //$$ @Override
    //$$ public RayTraceResult rayTrace(double blockReachDistance, float partialTicks, RayTraceFluidMode p_174822_4_) {
    //$$     RayTraceResult pos = super.rayTrace(blockReachDistance, partialTicks, p_174822_4_);
    //$$
    //$$     // Make sure we can never look at blocks (-> no outline)
    //$$     if(pos != null && pos.type == RayTraceResult.Type.BLOCK) {
    //$$         pos.type = RayTraceResult.Type.MISS;
    //$$     }
    //$$
    //$$     return pos;
    //$$ }
    //#else
    @Override
    public RayTraceResult rayTrace(double p_174822_1_, float p_174822_3_) {
        RayTraceResult pos = super.rayTrace(p_174822_1_, 1f);

        // Make sure we can never look at blocks (-> no outline)
        if(pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
            pos.typeOfHit = RayTraceResult.Type.MISS;
        }

        return pos;
    }
    //#endif
    //#endif
    //#else
    //$$ @Override
    //$$ public MovingObjectPosition rayTrace(double p_174822_1_, float p_174822_3_) {
    //$$     MovingObjectPosition pos = super.rayTrace(p_174822_1_, 1f);
    //$$
    //$$     // Make sure we can never look at blocks (-> no outline)
    //$$     if(pos != null && pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
    //$$         pos.typeOfHit = MovingObjectPosition.MovingObjectType.MISS;
    //$$     }
    //$$
    //$$     return pos;
    //$$ }
    //#endif

    //#if MC<11400
    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        // Do not open any block GUIs for the camera entities
        // Note: Vanilla GUIs are filtered out on a packet level, this only applies to mod GUIs
    }
    //#endif

    @Override
    //#if MC>=11700
    //$$ public void remove(RemovalReason reason) {
    //$$     super.remove(reason);
    //#else
    public void setDead() {
        super.setDead();
    //#endif
        if (eventHandler != null) {
            eventHandler.unregister();
            eventHandler = null;
        }
    }

    private void update() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != this.world) {
            if (eventHandler != null) {
                eventHandler.unregister();
                eventHandler = null;
            }
            return;
        }

        long now = System.currentTimeMillis();
        long timePassed = now - lastControllerUpdate;
        cameraController.update(timePassed / 50f);
        lastControllerUpdate = now;

        handleInputEvents();

        Map<String, KeyBindingRegistry.Binding> keyBindings = ReplayMod.instance.getKeyBindingRegistry().getBindings();
        if (keyBindings.get("replaymod.input.rollclockwise").keyBinding.isKeyDown()) {
            roll += Utils.isCtrlDown() ? 0.2 : 1;
        }
        if (keyBindings.get("replaymod.input.rollcounterclockwise").keyBinding.isKeyDown()) {
            roll -= Utils.isCtrlDown() ? 0.2 : 1;
        }

        //#if MC>=10800
        this.noClip = this.isSpectator();
        //#endif

        syncInventory();
    }

    private final InventoryPlayer originalInventory = this.inventory;

    // If we are spectating a player, "steal" its inventory so the rendering code knows what item(s) to render
    // and if we aren't, then reset ours.
    private void syncInventory() {
        Entity view = this.mc.getRenderViewEntity();
        EntityPlayer viewPlayer = view != this && view instanceof EntityPlayer ? (EntityPlayer) view : null;
        EntityPlayerAccessor cameraA = (EntityPlayerAccessor) this;
        EntityPlayerAccessor viewPlayerA = (EntityPlayerAccessor) viewPlayer;

        //#if MC>=11100
        ItemStack empty = ItemStack.EMPTY;
        //#else
        //$$ ItemStack empty = null;
        //#endif

        // TODO switch to replacing the entire inventory for 1.14+ as well, should be easier and faster
        //#if MC>=11400
        //$$ this.setItemStackToSlot(EquipmentSlotType.HEAD, viewPlayer != null ? viewPlayer.getItemStackFromSlot(EquipmentSlotType.HEAD) : empty);
        //$$ this.setItemStackToSlot(EquipmentSlotType.MAINHAND, viewPlayer != null ? viewPlayer.getItemStackFromSlot(EquipmentSlotType.MAINHAND) : empty);
        //$$ this.setItemStackToSlot(EquipmentSlotType.OFFHAND, viewPlayer != null ? viewPlayer.getItemStackFromSlot(EquipmentSlotType.OFFHAND) : empty);
        //#else
        this.inventory = viewPlayer != null ? viewPlayer.inventory : originalInventory;
        //#endif

        //#if MC>=10904
        cameraA.setItemStackMainHand(viewPlayerA != null ? viewPlayerA.getItemStackMainHand() : empty);
        this.swingingHand = viewPlayer != null ? viewPlayer.swingingHand : EnumHand.MAIN_HAND;
        this.activeItemStack = viewPlayer != null ? viewPlayer.getActiveItemStack() : empty;
        cameraA.setActiveItemStackUseCount(viewPlayerA != null ? viewPlayerA.getActiveItemStackUseCount() : 0);
        //#else
        //$$ cameraA.setItemInUse(viewPlayerA != null ? viewPlayerA.getItemInUse() : empty);
        //$$ cameraA.setItemInUseCount(viewPlayerA != null ? viewPlayerA.getItemInUseCount() : 0);
        //#endif
    }

    private void handleInputEvents() {
        if (this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindUseItem.isPressed()) {
            if (this.mc.currentScreen == null && canSpectate(this.mc.pointedEntity)) {
                ReplayModReplay.instance.getReplayHandler().spectateEntity(
                        //#if MC<=10710
                        //$$ (EntityLivingBase)
                        //#endif
                        this.mc.pointedEntity);
                // Make sure we don't exit right away
                //noinspection StatementWithEmptyBody
                while (this.mc.gameSettings.keyBindSneak.isPressed());
            }
        }
    }

    private void updateArmYawAndPitch() {
        this.prevRenderArmYaw = this.renderArmYaw;
        this.prevRenderArmPitch = this.renderArmPitch;
        this.renderArmPitch = this.renderArmPitch +  (this.rotationPitch - this.renderArmPitch) * 0.5f;
        this.renderArmYaw = this.renderArmYaw + wrapDegrees(this.rotationYaw - this.renderArmYaw) * 0.5f;
        this.wrapArmYaw();
    }

    /**
     * Minecraft renders the arm offset based on the difference between {@link #yaw} and {@link #renderYaw}. It does not
     * wrap around the difference though, so if {@link #yaw} just wrapped around from 350 to 10 but {@link #renderYaw}
     * is still at 355, then the difference will be inappropriately large. To fix this, we always wrap the
     * {@link #renderYaw} such that it is no more than 180 degrees away from {@link #yaw}, even if that requires going
     * outside the normal range.
     */
    private void wrapArmYaw() {
        this.renderArmYaw = wrapDegreesTo(this.renderArmYaw, this.rotationYaw);
        this.prevRenderArmYaw = wrapDegreesTo(this.prevRenderArmYaw, this.renderArmYaw);
    }

    private static float wrapDegreesTo(float value, float towardsValue) {
        while (towardsValue - value < -180) {
            value -= 360;
        }
        while (towardsValue - value >= 180) {
            value += 360;
        }
        return value;
    }

    private static float wrapDegrees(float value) {
        value %= 360;
        return wrapDegreesTo(value, 0);
    }

    public boolean canSpectate(Entity e) {
        return e != null
                //#if MC<10800
                //$$ && e instanceof EntityPlayer // cannot be more generic since 1.7.10 has no concept of eye height
                //#endif
                && !e.isInvisible();
    }

    //#if MC<11400
    //#if MC>=11102
    @Override
    public void sendMessage(ITextComponent message) {
        if (MinecraftForge.EVENT_BUS.post(new ReplayChatMessageEvent(this))) return;
        super.sendMessage(message);
    }
    //#else
    //$$ @Override
    //$$ public void addChatMessage(ITextComponent message) {
    //$$     if (MinecraftForge.EVENT_BUS.post(new ReplayChatMessageEvent(this))) return;
    //$$     super.addChatMessage(message);
    //$$ }
    //#endif
    //#endif

    //#if MC>=10800
    private
    //#else
    //$$ public // All event handlers need to be public in 1.7.10
    //#endif
    class EventHandler extends EventRegistrations {
        private final Minecraft mc = getMinecraft();

        private EventHandler() {}

        { on(PreTickCallback.EVENT, this::onPreClientTick); }
        private void onPreClientTick() {
            updateArmYawAndPitch();
        }

        { on(PreRenderCallback.EVENT, this::onRenderUpdate); }
        private void onRenderUpdate() {
            update();
        }

        { on(KeyBindingEventCallback.EVENT, CameraEntity.this::handleInputEvents); }

        { on(RenderSpectatorCrosshairCallback.EVENT, this::shouldRenderSpectatorCrosshair); }
        private Boolean shouldRenderSpectatorCrosshair() {
            return canSpectate(mc.pointedEntity);
        }

        { on(RenderHotbarCallback.EVENT, this::shouldRenderHotbar); }
        private Boolean shouldRenderHotbar() {
            return false;
        }

        { on(SettingsChangedCallback.EVENT, this::onSettingsChanged); }
        private void onSettingsChanged(SettingsRegistry registry, SettingsRegistry.SettingKey<?> key) {
            if (key == Setting.CAMERA) {
                if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
                    cameraController = ReplayModReplay.instance.createCameraController(CameraEntity.this);
                } else {
                    cameraController = new SpectatorCameraController(CameraEntity.this);
                }
            }
        }

        { on(PreRenderHandCallback.EVENT, this::onRenderHand); }
        private boolean onRenderHand() {
            // Unless we are spectating another player, don't render our hand
            Entity view = mc.getRenderViewEntity();
            if (view == CameraEntity.this || !(view instanceof EntityPlayer)) {
                return true; // cancel hand rendering
            } else {
                EntityPlayer player = (EntityPlayer) view;
                // When the spectated player has changed, force equip their items to prevent the equip animation
                if (lastHandRendered != player) {
                    lastHandRendered = player;

                    FirstPersonRendererAccessor acc = (FirstPersonRendererAccessor) mc.entityRenderer.itemRenderer;
                    //#if MC>=10904
                    acc.setPrevEquippedProgressMainHand(1);
                    acc.setPrevEquippedProgressOffHand(1);
                    acc.setEquippedProgressMainHand(1);
                    acc.setEquippedProgressOffHand(1);
                    acc.setItemStackMainHand(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
                    acc.setItemStackOffHand(player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND));
                    //#else
                    //$$ acc.setPrevEquippedProgress(1);
                    //$$ acc.setEquippedProgress(1);
                    //$$ acc.setItemToRender(player.inventory.getCurrentItem());
                    //$$ acc.setEquippedItemSlot(player.inventory.currentItem);
                    //#endif


                    mc.player.renderArmYaw = mc.player.prevRenderArmYaw = player.rotationYaw;
                    mc.player.renderArmPitch = mc.player.prevRenderArmPitch = player.rotationPitch;
                }
                return false;
            }
        }

        //#if MC>=11400
        //$$ // Moved to MixinCamera
        //#else
        //#if MC>=10800
        @SubscribeEvent
        public void onEntityViewRenderEvent(EntityViewRenderEvent.CameraSetup event) {
            if (mc.getRenderViewEntity() == CameraEntity.this) {
                //#if MC>=10904
                event.setRoll(roll);
                //#else
                //$$ event.roll = roll;
                //#endif
            }
        }
        //#endif
        //#endif

        private boolean heldItemTooltipsWasTrue;

        //#if FABRIC>=1
        //$$ // FIXME fabric
        //#else
        @SubscribeEvent
        public void preRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
            switch (event.getType()) {
                case ALL:
                    heldItemTooltipsWasTrue = mc.gameSettings.heldItemTooltips;
                    mc.gameSettings.heldItemTooltips = false;
                    break;
                case ARMOR:
                case HEALTH:
                case FOOD:
                case AIR:
                case HOTBAR:
                case EXPERIENCE:
                case HEALTHMOUNT:
                case JUMPBAR:
                //#if MC>=10904
                case POTION_ICONS:
                //#endif
                    event.setCanceled(true);
                    break;
                case HELMET:
                case PORTAL:
                case CROSSHAIRS:
                case BOSSHEALTH:
                //#if MC>=10904
                case BOSSINFO:
                case SUBTITLES:
                //#endif
                case TEXT:
                case CHAT:
                case PLAYER_LIST:
                case DEBUG:
                    break;
            }
        }

        @SubscribeEvent
        public void postRenderGameOverlay(RenderGameOverlayEvent.Post event) {
            if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
            mc.gameSettings.heldItemTooltips = heldItemTooltipsWasTrue;
        }
        //#endif
    }
}
