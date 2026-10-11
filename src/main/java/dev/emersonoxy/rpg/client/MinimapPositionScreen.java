package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Normalized placement stays inside the viewport when resolution/GUI scale changes. */
public final class MinimapPositionScreen extends Screen {
    private final Screen parent;private boolean dragging;private double grabX,grabY;
    public MinimapPositionScreen(Screen parent){super(Component.literal("Posição do minimapa"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){MinimapOptions.save();minecraft.setScreen(parent);}
    @Override public void removed(){MinimapOptions.save();}
    @Override protected void init(){addRenderableWidget(Button.builder(Component.literal("Salvar posição"),b->onClose()).bounds(width/2-75,height-28,150,20).build());}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        if(super.mouseClicked(event,doubleClick))return true;
        var b=SimpleMinimap.bounds(width,height);if(event.button()==0&&event.x()>=b.x()&&event.x()<b.x()+b.size()+8&&event.y()>=b.y()&&event.y()<b.y()+b.height()){dragging=true;grabX=event.x()-b.x();grabY=event.y()-b.y();return true;}return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){if(!dragging)return super.mouseDragged(event,dx,dy);var b=SimpleMinimap.bounds(width,height);MinimapOptions.manual=true;MinimapOptions.x=Math.clamp((event.x()-grabX)/Math.max(1,width-b.size()-8),0,1);MinimapOptions.y=Math.clamp((event.y()-grabY)/Math.max(1,height-b.height()),0,1);return true;}
    @Override public boolean mouseReleased(MouseButtonEvent event){if(dragging){dragging=false;return true;}return super.mouseReleased(event);}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,height-58,width,height,0xD9111B27);g.drawCenteredString(font,"Arraste o minimapa para escolher a posição",width/2,height-52,RpgUi.GOLD);
        g.drawCenteredString(font,"O espaço dos efeitos é reservado quando necessário",width/2,height-41,RpgUi.MUTED);
        SimpleMinimap.draw(g,SimpleMinimap.bounds(width,height));super.render(g,mx,my,delta);
    }
}
