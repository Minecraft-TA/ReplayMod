package com.replaymod.recording.gui;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.recording.Setting;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.versions.callbacks.RenderHudCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

import static com.replaymod.core.ReplayMod.TEXTURE;
import static com.replaymod.core.ReplayMod.TEXTURE_SIZE;
import static net.minecraft.client.renderer.GlStateManager.*;

//#if MC>=12000
//$$ import net.minecraft.client.gui.DrawContext;
//#else
import de.johni0702.minecraft.gui.versions.MatrixStack;
//#endif

/**
 * Renders overlay during recording.
 */
public class GuiRecordingOverlay extends EventRegistrations {
    private final Minecraft mc;
    private final SettingsRegistry settingsRegistry;
    private final GuiRecordingControls guiControls;

    public GuiRecordingOverlay(Minecraft mc, SettingsRegistry settingsRegistry, GuiRecordingControls guiControls) {
        this.mc = mc;
        this.settingsRegistry = settingsRegistry;
        this.guiControls = guiControls;
    }

    /**
     * Render the recording icon and text in the top left corner of the screen.
     */
    { on(RenderHudCallback.EVENT, (stack, partialTicks) -> renderRecordingIndicator(stack)); }
    //#if MC>=12000
    //$$ private void renderRecordingIndicator(DrawContext stack) {
    //#else
    private void renderRecordingIndicator(MatrixStack stack) {
    //#endif
        if (guiControls.isStopped()) return;
        if (settingsRegistry.get(Setting.INDICATOR)) {
            FontRenderer fontRenderer = mc.fontRenderer;
            String text = guiControls.isPaused() ? I18n.format("replaymod.gui.paused") : I18n.format("replaymod.gui.recording");
            MinecraftGuiRenderer renderer = new MinecraftGuiRenderer(stack);
            renderer.drawString(30, 18 - (fontRenderer.FONT_HEIGHT / 2), 0xffffffff, text.toUpperCase());
            renderer.bindTexture(TEXTURE);
            //#if MC<11700
            enableAlpha();
            //#endif
            renderer.drawTexturedRect(10, 10, 58, 20, 16, 16, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
