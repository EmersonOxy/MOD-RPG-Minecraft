package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class VillagerDialogueScreen extends Screen {
    private final RpgNetwork.VillagerDialogue data;private int left,top,w,h,scroll,maxScroll;private boolean emptyTrades;
    public VillagerDialogueScreen(RpgNetwork.VillagerDialogue data){super(Component.literal(data.name()));this.data=data;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(516,width-16);h=Math.min(228,height-8);left=(width-w)/2;top=Math.max(4,(height-h)/2);
        int side=Math.min(220,w-150);VillagerPanel.init(left+w-side,top,side,this::addRenderableWidget);
        if(data.canTrade())addRenderableWidget(RpgUi.button(left+10,top+64,w-side-24,20,"Abrir trocas","",Items.EMERALD,RpgUi.GOLD,b->VillagerClient.request(306)));
        addRenderableWidget(RpgUi.button(left+10,top+h-37,w-side-24,20,"Até logo","",Items.PAPER,RpgUi.GOLD,b->onClose()));
    }
    @Override public void onClose(){VillagerClient.close();super.onClose();}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){VillagerPanel.scroll(sy);return true;}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}var npc=minecraft.level.getEntity(data.entityId());boolean night=minecraft.level.dimensionType().hasSkyLight()&&Math.floorMod(minecraft.level.getDayTime(),24000)>=12000;if(minecraft.player.getPersistentData().getBooleanOr("oxy_vampire_class",false)&&night){onClose();return;}if(npc==null||!npc.isAlive()||minecraft.player.distanceToSqr(npc)>36)minecraft.setScreen(null);}
    private int text(GuiGraphics g,String text,int y,int color){for(var line:font.split(Component.literal(text),w-48)){g.drawString(font,line,left+24,y,color);y+=11;}return y;}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        int side=Math.min(220,w-150);RpgUi.panel(g,left,top,w-side-8,h-8);
        g.drawString(font,data.name(),left+12,top+12,RpgUi.GOLD);
        var current=VillagerClient.data();String info=current!=null&&current.canTrade()?"Produtos da profissão disponíveis nas trocas.":"Este NPC não tem ofertas de troca. Consulte seus contratos e converse ao lado.";
        int line=top+40;for(var text:font.split(Component.literal(info),w-side-32)){g.drawString(font,text,left+12,line,RpgUi.MUTED);line+=11;}
        VillagerPanel.render(g,left+w-side,top,side);super.render(g,x,y,delta);
    }
}
