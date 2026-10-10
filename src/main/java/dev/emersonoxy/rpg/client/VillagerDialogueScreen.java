package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class VillagerDialogueScreen extends Screen {
    private final RpgNetwork.VillagerDialogue data;private int left,top,w,h;
    public VillagerDialogueScreen(RpgNetwork.VillagerDialogue data){super(Component.literal(data.name()));this.data=data;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(410,width-16);h=Math.min(240,height-8);left=(width-w)/2;top=(height-h)/2;
        if(data.status()==0||data.status()==2){int action=data.status()==0?300:301;
            addRenderableWidget(Button.builder(Component.literal(action==300?"Aceitar missão":"Confirmar conclusão"),b->{b.active=false;minecraft.player.connection.send(new RpgNetwork.Request(action,data.entityId()));}).bounds(left+12,top+h-54,w-24,20).build());}
        addRenderableWidget(Button.builder(Component.literal("Até logo"),b->onClose()).bounds(left+12,top+h-28,w-24,20).build());
    }
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}var npc=minecraft.level.getEntity(data.entityId());if(npc==null||!npc.isAlive()||minecraft.player.distanceToSqr(npc)>36)minecraft.setScreen(null);}
    private int text(GuiGraphics g,String text,int y,int color){for(var line:font.split(Component.literal(text),w-48)){g.drawString(font,line,left+24,y,color);y+=11;}return y;}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        VanillaTheme.panel(g,left,top,w,h);g.renderFakeItem(new ItemStack(Items.EMERALD),left+12,top+10);
        g.drawCenteredString(font,title,width/2,top+14,0xFFFFD36B);
        int line=text(g,data.dialogue(),top+40,0xFFFFFFFF);line=text(g,data.title(),line+12,0xFFFFD36B);
        line=text(g,data.objective(),line+8,0xFFFFFFFF);text(g,"Recompensa: "+data.reward(),line+10,0xFF99E890);
        super.render(g,x,y,delta);
    }
}
