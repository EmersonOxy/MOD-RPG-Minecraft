package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AttributesScreen extends Screen {
    private final Screen parent;
    private RpgNetwork.Resources state;
    private final Button[] buttons=new Button[5];
    private int left,top,panelWidth,ticks;
    public AttributesScreen(Screen parent,RpgNetwork.Resources state){super(Component.literal("Atributos"));this.parent=parent;this.state=state;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        panelWidth=Math.min(370,width-16);left=(width-panelWidth)/2;top=Math.max(4,(height-230)/2);
        for(int i=0;i<5;i++){final int id=i;
            buttons[i]=addRenderableWidget(Button.builder(Component.literal("+"),b->{
                if(state!=null&&state.points()>0){minecraft.player.connection.send(new RpgNetwork.Request(200+id));buttons[id].active=false;}
            }).bounds(left+panelWidth-55,top+54+i*27,40,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+201,panelWidth-24,20).build());
        update(state);
        if(minecraft.player!=null)minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    public void update(RpgNetwork.Resources value){state=value;for(var b:buttons)if(b!=null)b.active=state!=null&&state.points()>0;}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        VanillaTheme.panel(g,left,top,panelWidth,230);
        g.drawCenteredString(font,title,width/2,top+12,0xFF53D5BF);
        g.drawCenteredString(font,state==null?"Carregando...":state.points()+" pontos disponíveis • 1 por nível",width/2,top+30,0xFFFFFFFF);
        for(int i=0;i<5;i++){
            g.drawString(font,AttributeStats.label(i)+(state==null?"":" • "+state.value(i)),left+14,top+55+i*27,0xFFFFFFFF);
            g.drawString(font,AttributeStats.benefit(i),left+14,top+66+i*27,0xFFB7C8D7);
        }
        g.drawString(font,state==null?"":"Mana: "+state.mana()+"/"+state.maxMana(),left+14,top+188,0xFF70BAFF);
        super.render(g,x,y,delta);
    }
}

