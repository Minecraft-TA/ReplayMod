package com.replaymod.core.mixin;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;

//#if MC>=11700
//$$ import net.minecraft.client.gui.Drawable;
//$$ import net.minecraft.client.gui.Element;
//$$ import net.minecraft.client.gui.Selectable;
//#else
//#endif

//#if MC>=11400
//$$ import net.minecraft.client.gui.widget.Widget;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//#else
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
//#endif

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {
    //#if MC>=11700
    //$$ @Invoker("addDrawableChild")
    //$$ <T extends Element & Drawable & Selectable> T invokeAddButton(T drawableElement);
    //#elseif MC>=11400
    //$$ @Invoker
    //$$ <T extends Widget> T invokeAddButton(T button);
    //#else
    @Accessor("buttonList")
    List<GuiButton> getButtons();
    //#endif
}
