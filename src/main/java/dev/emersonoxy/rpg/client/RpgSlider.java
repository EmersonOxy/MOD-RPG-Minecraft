package dev.emersonoxy.rpg.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
/** Shared RPG slider appearance, preserving native pointer and keyboard handling. */
public abstract class RpgSlider extends AbstractSliderButton {
    public RpgSlider(int x,int y,int width,int height,Component label,double value){super(x,y,width,height,label,value);}
    @Override public void renderWidget(GuiGraphics graphics,int mouseX,int mouseY,float partial){RpgUi.slider(graphics,this,value);}
}
