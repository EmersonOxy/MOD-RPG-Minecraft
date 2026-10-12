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
        w=Math.min(288,width-16);h=Math.min(202,height-8);left=(width-w)/2;top=(height-h)/2;
        var quests=addRenderableWidget(Button.builder(Component.literal("Missões"),b->{emptyTrades=false;scroll=0;rebuildWidgets();}).bounds(left+12,top+32,(w-28)/2,20).build());quests.active=emptyTrades;
        var trades=addRenderableWidget(Button.builder(Component.literal("Trocas"),b->{if(data.canTrade())VillagerClient.request(306);else{emptyTrades=true;scroll=0;rebuildWidgets();}}).bounds(left+16+(w-28)/2,top+32,(w-28)/2,20).build());trades.active=!emptyTrades;trades.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(data.canTrade()?"Abrir as trocas deste morador":"Consultar por que este morador não tem ofertas")));
        if(!emptyTrades&&(data.status()==0||data.status()==2)){int action=data.status()==0?300:301;
            addRenderableWidget(Button.builder(Component.literal(action==300?"Aceitar missão":"Confirmar conclusão"),b->{b.active=false;minecraft.player.connection.send(new RpgNetwork.Request(action,data.entityId()));}).bounds(left+12,top+h-54,w-24,20).build());}
        addRenderableWidget(Button.builder(Component.literal("Até logo"),b->onClose()).bounds(left+12,top+h-28,w-24,20).build());
    }
    @Override public void onClose(){VillagerClient.close();super.onClose();}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){scroll=Math.clamp(scroll-(int)(sy*16),0,maxScroll);return true;}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}var npc=minecraft.level.getEntity(data.entityId());boolean night=minecraft.level.dimensionType().hasSkyLight()&&Math.floorMod(minecraft.level.getDayTime(),24000)>=12000;if(minecraft.player.getPersistentData().getBooleanOr("oxy_vampire_class",false)&&night){onClose();return;}if(npc==null||!npc.isAlive()||minecraft.player.distanceToSqr(npc)>36)minecraft.setScreen(null);}
    private int text(GuiGraphics g,String text,int y,int color){for(var line:font.split(Component.literal(text),w-48)){g.drawString(font,line,left+24,y,color);y+=11;}return y;}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        RpgUi.panel(g,left,top,w,h);g.renderFakeItem(new ItemStack(Items.EMERALD),left+12,top+10);
        g.drawCenteredString(font,title,width/2,top+14,0xFFFFD36B);
        g.enableScissor(left+12,top+60,left+w-12,top+h-60);int line=text(g,emptyTrades?"Este morador ainda não tem trocas. Um adulto sem profissão precisa de um bloco de trabalho acessível; aldeões simplórios não negociam.":data.dialogue(),top+64-scroll,0xFFFFFFFF);line=text(g,emptyTrades?"Sem ofertas":data.title(),line+12,0xFFFFD36B);
        line=text(g,emptyTrades?"Você ainda pode consultar suas missões na outra aba.":data.objective(),line+8,0xFFFFFFFF);int end=text(g,emptyTrades?"":"Recompensa: "+data.reward(),line+10,0xFF99E890);maxScroll=Math.max(0,end+scroll-(top+h-64));
        g.disableScissor();super.render(g,x,y,delta);
    }
}
