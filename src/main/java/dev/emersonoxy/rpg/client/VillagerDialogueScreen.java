package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class VillagerDialogueScreen extends Screen {
    private final RpgNetwork.VillagerDialogue data;private int left,top,w,h;private boolean compact;
    public VillagerDialogueScreen(RpgNetwork.VillagerDialogue data){super(Component.literal(data.name()));this.data=data;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(550,width-16);h=VillagerPanel.height();left=(width-w)/2;top=Math.max(4,(height-h)/2);
        compact=width<480;int side=compact?w:Math.min(260,w-130);VillagerPanel.init(left+w-side,top,side,this::addRenderableWidget);
        if(data.canTrade())addRenderableWidget(Button.builder(Component.literal("Abrir trocas"),b->VillagerClient.request(306)).bounds(compact?left+w-104:left+12,compact?top+8:top+100,compact?92:w-side-32,compact?16:20).build());
        if(!compact)addRenderableWidget(Button.builder(Component.literal("Até logo"),b->onClose()).bounds(left+12,top+h-30,w-side-32,20).build());
    }
    @Override public void onClose(){VillagerClient.close();super.onClose();}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){VillagerPanel.scroll(sy);return true;}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}var npc=minecraft.level.getEntity(data.entityId());boolean night=minecraft.level.dimensionType().hasSkyLight()&&Math.floorMod(minecraft.level.getDayTime(),24000)>=12000;if(minecraft.player.getPersistentData().getBooleanOr("oxy_vampire_class",false)&&night){onClose();return;}if(npc==null||!npc.isAlive()||minecraft.player.distanceToSqr(npc)>36)minecraft.setScreen(null);}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        int side=compact?w:Math.min(260,w-130);if(compact){VillagerPanel.render(g,left,top,w);super.render(g,x,y,delta);return;}RpgUi.panel(g,left,top,w-side-8,h);
        g.drawString(font,data.name(),left+12,top+12,RpgUi.GOLD);
        var current=VillagerClient.data();String info=current!=null&&current.canTrade()?"Produtos da profissão disponíveis nas trocas.":"Este NPC não tem ofertas de troca. Consulte seus contratos e converse ao lado.";
        int line=top+40;for(var text:font.split(Component.literal(info),w-side-32)){g.drawString(font,text,left+12,line,RpgUi.MUTED);line+=11;}
        VillagerPanel.render(g,left+w-side,top,side);super.render(g,x,y,delta);
    }
}
