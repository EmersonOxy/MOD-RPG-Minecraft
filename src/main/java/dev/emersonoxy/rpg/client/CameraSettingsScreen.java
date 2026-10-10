package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CameraSettingsScreen extends Screen {
    private final Screen parent;private int left,top,panelWidth,panelHeight;
    public CameraSettingsScreen(Screen parent){super(Component.literal("Câmera Oxy RPG"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        panelWidth=Math.min(350,width-16);panelHeight=Math.min(262,height-8);left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        addRenderableWidget(Button.builder(Component.literal("Câmera sobre o ombro: "+(ShoulderCamera.enabled()?"ligada":"desligada")),b->{ShoulderCamera.toggle();rebuildWidgets();}).bounds(left+12,top+30,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Ombro: "+ShoulderCamera.shoulder()),b->{ShoulderCamera.swap();rebuildWidgets();}).bounds(left+12,top+52,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Balanço: "+ShoulderCamera.sway()),b->{ShoulderCamera.cycle();rebuildWidgets();}).bounds(left+12,top+74,panelWidth-24,18).build());
        for(int i=0;i<3;i++){final int axis=i;int y=top+100+i*24;
            addRenderableWidget(Button.builder(Component.literal("−"),b->{ShoulderCamera.adjust(axis,-.1);rebuildWidgets();}).bounds(left+panelWidth-74,y,26,18).build());
            addRenderableWidget(Button.builder(Component.literal("+"),b->{ShoulderCamera.adjust(axis,.1);rebuildWidgets();}).bounds(left+panelWidth-42,y,26,18).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Restaurar padrões"),b->{ShoulderCamera.defaults();rebuildWidgets();}).bounds(left+12,top+174,panelWidth-24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+panelHeight-28,panelWidth-24,20).build());
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        VanillaTheme.panel(g,left,top,panelWidth,panelHeight);g.drawCenteredString(font,title,width/2,top+12,0xFFFFFFFF);
        for(int i=0;i<3;i++)g.drawString(font,(i==0?"Distância":i==1?"Lateral":"Altura")+": "+ShoulderCamera.position(i),left+14,top+105+i*24,0xFFFFFFFF);
        int lineY=top+199;
        String text="F5: perspectiva • J: ombro • Z: trava • N: alvo. Teclado numérico: +/− distância, 4/6 lateral, 2/8 altura. Teclas remapeáveis nos controles.";
        for(var line:font.split(Component.literal(text),panelWidth-24)){if(lineY>top+panelHeight-42)break;g.drawString(font,line,left+12,lineY,0xFFFFFFFF);lineY+=10;}
        super.render(g,x,y,delta);
    }
}
