package com.replaymod.replay.camera;

import com.replaymod.replay.ReplayModReplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;

//#if MC>=11400
//#else
import org.lwjgl.input.Mouse;
//#endif

import java.util.Arrays;

import static com.replaymod.core.versions.MCVer.*;

public class SpectatorCameraController implements CameraController {
    private final CameraEntity camera;

    public SpectatorCameraController(CameraEntity camera) {
        this.camera = camera;
    }

    @Override
    public void update(float partialTicksPassed) {
        Minecraft mc = getMinecraft();
        if (mc.gameSettings.keyBindSneak.isPressed()) {
            ReplayModReplay.instance.getReplayHandler().spectateCamera();
        }

        // Soak up all remaining key presses
        for (KeyBinding binding : Arrays.asList(mc.gameSettings.keyBindAttack, mc.gameSettings.keyBindUseItem,
                mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight)) {
            //noinspection StatementWithEmptyBody
            while (binding.isPressed());
        }

        // Prevent mouse movement
        //#if MC>=11400
        //$$ // No longer needed
        //#else
        Mouse.updateCursor();
        //#endif

        // Always make sure the camera is in the exact same spot as the spectated entity
        // This is necessary as some rendering code for the hand doesn't respect the view entity
        // and always uses mc.thePlayer
        Entity view = mc.getRenderViewEntity();
        if (view != null && view != camera) {
            camera.setCameraPosRot(mc.getRenderViewEntity());
        }
    }

    @Override
    public void increaseSpeed() {

    }

    @Override
    public void decreaseSpeed() {

    }
}
