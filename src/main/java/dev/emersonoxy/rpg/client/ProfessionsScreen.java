package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class ProfessionsScreen extends Screen {
    private final Screen parent;private RpgNetwork.ProfessionState state;private int left,top,panelWidth,panelHeight,ticks;
    public ProfessionsScreen(Screen parent,RpgNetwork.ProfessionState state){super(Component.literal("Profissões e contratos"));this.parent=parent;this.state=state;}
    public void update(RpgNetwork.ProfessionState state){this.state=state;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        panelWidth=Math.min(440,width-16);panelHeight=Math.min(300,height-8);left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        addRenderableWidget(Button.builder(Component.literal("Abandonar contrato de villager"),b->minecraft.player.connection.send(new RpgNetwork.Request(302))).bounds(left+12,top+panelHeight-52,panelWidth-24,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+panelHeight-28,panelWidth-24,20).build());
        minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void tick(){if(minecraft.player==null){minecraft.setScreen(null);return;}if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        VanillaTheme.panel(g,left,top,panelWidth,panelHeight);g.drawCenteredString(font,title,width/2,top+10,0xFFFFFFFF);
        var icons=new Item[]{Items.IRON_PICKAXE,Items.WHEAT,Items.IRON_SWORD};
        for(int i=0;i<3;i++){
            int y=top+32+i*48;int xp=state==null?0:state.xp(i),level=ProfessionStore.level(xp);
            g.renderFakeItem(new ItemStack(icons[i]),left+14,y);g.drawString(font,Professions.NAMES[i]+" • Nível "+level+"/20",left+38,y,0xFFFFD36B);
            g.drawString(font,level==20?"Máximo! Passivo: "+(i==0?"+10% velocidade de mineração":i==1?"+1 coração de vida":"+5% dano corpo a corpo"):ProfessionStore.remainder(xp)+" / "+ProfessionStore.nextXp(level)+" XP",left+38,y+12,0xFFFFFFFF);
            VanillaTheme.progress(g,left+38,y+25,panelWidth-52,level==20?1:ProfessionStore.remainder(xp),level==20?1:ProfessionStore.nextXp(level));
        }
        g.drawString(font,font.plainSubstrByWidth("Etapas: inicial 1 • pedra 4 • ferro 8 • diamante 12 • netherite 16",panelWidth-24),left+12,top+178,0xFFD8DBDD);
        int y=top+193;
        var text=Component.literal(state==null?"Carregando contrato...":state.journal());
        for(var line:font.split(text,panelWidth-24)){if(y>top+panelHeight-66)break;g.drawString(font,line,left+12,y,0xFFFFFFFF);y+=10;}
        super.render(g,mouseX,mouseY,delta);
    }
}
