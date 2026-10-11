package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CameraSettingsScreen extends Screen {
    private final Screen parent;private int left,top,panelWidth,panelHeight;
    public CameraSettingsScreen(Screen parent){super(Component.literal("Câmera Oxy RPG"));this.parent=parent;}
    @Override public void removed(){ShoulderCamera.savePositions();}
    private static final class PositionSlider extends RpgSlider {
        private final int axis;
        private final double limit;
        PositionSlider(int x,int y,int width,int axis){
            super(x,y,width,20,Component.empty(),(ShoulderCamera.value(axis)+(axis==0?6:3))/(axis==0?12:6));
            this.axis=axis;this.limit=axis==0?6:3;updateMessage();
        }
        @Override protected void updateMessage(){setMessage(Component.literal((axis==0?"Distância":axis==1?"Lateral":"Altura")+": "+ShoulderCamera.position(axis)));}
        @Override protected void applyValue(){ShoulderCamera.setPosition(axis,Math.round((value*2*limit-limit)*100)/100.0);}
        @Override public void onRelease(MouseButtonEvent event){super.onRelease(event);ShoulderCamera.savePositions();}
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        panelWidth=Math.min(350,width-16);panelHeight=Math.min(262,height-8);left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        addRenderableWidget(Button.builder(Component.literal("Câmera sobre o ombro: "+(ShoulderCamera.enabled()?"ligada":"desligada")),b->{ShoulderCamera.toggle();rebuildWidgets();}).bounds(left+12,top+30,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Ombro: "+ShoulderCamera.shoulder()),b->{ShoulderCamera.swap();rebuildWidgets();}).bounds(left+12,top+52,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Balanço: "+ShoulderCamera.sway()),b->{ShoulderCamera.cycle();rebuildWidgets();}).bounds(left+12,top+74,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Movimento: "+(ShoulderCamera.independentMovement()?"independente da mira":"padrão")),b->{ShoulderCamera.toggleMovement();rebuildWidgets();}).bounds(left+12,top+96,panelWidth-24,18).build());
        for(int i=0;i<3;i++)addRenderableWidget(new PositionSlider(left+12,top+120+i*24,panelWidth-24,i));
        addRenderableWidget(Button.builder(Component.literal("Restaurar padrões"),b->{ShoulderCamera.defaults();rebuildWidgets();}).bounds(left+12,top+194,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+panelHeight-28,panelWidth-24,20).build());
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        RpgUi.panel(g,left,top,panelWidth,panelHeight);g.drawCenteredString(font,title,width/2,top+12,RpgUi.GOLD);
        int lineY=top+216;
        String text="Movimento independente: corpo segue WASD em 3ª pessoa.";
        for(var line:font.split(Component.literal(text),panelWidth-24)){if(lineY>top+panelHeight-42)break;g.drawString(font,line,left+12,lineY,0xFFFFFFFF);lineY+=10;}
        super.render(g,x,y,delta);
    }
}
