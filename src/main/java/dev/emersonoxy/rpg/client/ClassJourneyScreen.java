package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClassJourneyScreen extends Screen {
    private final Screen parent;private int left,top,w,h;private double scroll;
    public ClassJourneyScreen(Screen parent){super(Component.literal("Treinamento de classe"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){w=Math.min(420,width-16);left=(width-w)/2;h=Math.min(282,height-8);top=(height-h)/2;addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+h-28,w-24,20).build());if(minecraft.player!=null)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(x>=left&&x<=left+w&&y>=top+46&&y<top+h-60){scroll=Math.clamp(scroll-vertical*20,0,Math.max(0,186-(h-106)));return true;}return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        RpgUi.panel(g,left,top,w,h);g.drawCenteredString(font,title,width/2,top+12,RpgUi.GOLD);var state=OxyRpgClient.journey();var c=state==null?RpgClass.NONE:RpgClass.values()[state.rpgClass()];int stage=state==null?0:state.stage();
        g.drawCenteredString(font,c.label()+" • "+Math.min(3,stage)+"/3 lições",width/2,top+30,0xFFFFD36B);
        scroll=Math.clamp(scroll,0,Math.max(0,186-(h-106)));
        g.enableScissor(left+8,top+46,left+w-8,top+h-60);
        for(int i=0;i<3;i++){
            int row=top+52+i*62-(int)scroll;int cardHeight=58;int color=i<stage?0xFF53D5BF:i==stage?0xFFFFFFFF:0xFF909BAA;
            RpgUi.section(g,left+10,row-5,w-20,cardHeight,color);
            g.drawString(font,(i<stage?"✓ ":i==stage?"▶ ":"• ")+"Lição "+(i+1)+" • +"+ClassJourney.reward(i)+" XP Arsenal",left+12,row,color);
            int lineY=row+12;for(var line:font.split(Component.literal(ClassJourney.objective(c,i)),w-24)){g.drawString(font,line,left+12,lineY,color);lineY+=10;}
            if(i==stage&&state!=null&&c!=RpgClass.NONE)g.drawString(font,state.progress()+"/"+ClassJourney.goal(c,i),left+12,row+33,color);
        }
        g.disableScissor();
        g.drawString(font,font.plainSubstrByWidth("Aprenda os ramos na árvore de skills para avançar.",w-24),left+12,top+h-54,0xFFB7C8D7);
        g.drawString(font,font.plainSubstrByWidth("Role para ler • teste/debug não dão progresso ou recompensas.",w-24),left+12,top+h-44,RpgUi.MUTED);super.render(g,x,y,delta);
    }
}
