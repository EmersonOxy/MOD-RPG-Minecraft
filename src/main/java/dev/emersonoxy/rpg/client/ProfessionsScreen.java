package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class ProfessionsScreen extends Screen {
    private final Screen parent;private RpgNetwork.ProfessionState state;private int left,top,panelWidth,panelHeight,ticks;private double scroll;
    public ProfessionsScreen(Screen parent,RpgNetwork.ProfessionState state){super(Component.literal("Profissões e contratos"));this.parent=parent;this.state=state;}
    public void update(RpgNetwork.ProfessionState state){this.state=state;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        panelWidth=Math.min(440,width-16);panelHeight=Math.min(330,height-8);left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        addRenderableWidget(Button.builder(Component.literal("Comparar armas"),b->minecraft.setScreen(new ArsenalScreen(this))).bounds(left+panelWidth-116,top+28,104,18).build());
        addRenderableWidget(Button.builder(Component.literal("Abandonar contrato de villager"),b->minecraft.player.connection.send(new RpgNetwork.Request(302))).bounds(left+12,top+panelHeight-52,panelWidth-24,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+panelHeight-28,panelWidth-24,20).build());
        minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void tick(){if(minecraft.player==null){minecraft.setScreen(null);return;}if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    private int contentHeight(){return 180+font.split(Component.literal(state==null?"Carregando contrato...":state.journal()),panelWidth-24).size()*10;}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(x>=left&&x<=left+panelWidth&&y>=top+48&&y<top+panelHeight-62){scroll=Math.clamp(scroll-vertical*20,0,Math.max(0,contentHeight()-(panelHeight-110)));return true;}return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        RpgUi.panel(g,left,top,panelWidth,panelHeight);g.drawCenteredString(font,title,width/2,top+10,RpgUi.GOLD);
        scroll=Math.clamp(scroll,0,Math.max(0,contentHeight()-(panelHeight-110)));
        g.drawString(font,"Profissões • role para ler",left+12,top+32,RpgUi.MUTED);
        g.enableScissor(left+8,top+48,left+panelWidth-8,top+panelHeight-62);
        var icons=new Item[]{Items.IRON_PICKAXE,Items.WHEAT,Items.IRON_SWORD};
        for(int i=0;i<3;i++){
            int y=top+54+i*48-(int)scroll;int xp=state==null?0:state.xp(i),level=ProfessionStore.level(xp);
            RpgUi.section(g,left+10,y-4,panelWidth-20,44,RpgUi.GOLD);
            g.renderFakeItem(new ItemStack(icons[i]),left+14,y);g.drawString(font,Professions.NAMES[i]+" • Nível "+level+"/20",left+38,y,0xFFFFD36B);
            g.drawString(font,level==20?"Máximo! Passivo: "+(i==0?"+10% velocidade de mineração":i==1?"+1 coração de vida":"+5% dano corpo a corpo"):ProfessionStore.remainder(xp)+" / "+ProfessionStore.nextXp(level)+" XP",left+38,y+12,0xFFFFFFFF);
            VanillaTheme.progress(g,left+38,y+25,panelWidth-52,level==20?1:ProfessionStore.remainder(xp),level==20?1:ProfessionStore.nextXp(level));
        }
        g.drawString(font,font.plainSubstrByWidth("Etapas: inicial 1 • pedra 4 • ferro 8 • diamante 12 • netherite 16",panelWidth-24),left+12,top+204-(int)scroll,0xFFD8DBDD);
        int y=top+220-(int)scroll;
        var text=Component.literal(state==null?"Carregando contrato...":state.journal());
        for(var line:font.split(text,panelWidth-24)){g.drawString(font,line,left+12,y,0xFFFFFFFF);y+=10;}
        g.disableScissor();
        super.render(g,mouseX,mouseY,delta);
    }
}
