package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class VillagerClient {
    private static RpgNetwork.VillagerDialogue dialogue;private static long switchingUntil;
    private VillagerClient(){}
    public static void receive(RpgNetwork.VillagerDialogue data){dialogue=data;switchingUntil=0;Minecraft.getInstance().setScreen(new VillagerDialogueScreen(data));}
    public static void request(int action){var mc=Minecraft.getInstance();if(mc.player==null||dialogue==null)return;switchingUntil=mc.player.tickCount+40;mc.player.connection.send(new RpgNetwork.Request(action,dialogue.entityId()));}
    public static void close(){var mc=Minecraft.getInstance();if(mc.player!=null&&dialogue!=null)mc.player.connection.send(new RpgNetwork.Request(304,dialogue.entityId()));dialogue=null;switchingUntil=0;}
    public static void tick(){var mc=Minecraft.getInstance();if(dialogue==null)return;if(mc.player==null||mc.level==null){dialogue=null;return;}
        boolean active=mc.screen instanceof VillagerDialogueScreen||mc.screen instanceof MerchantScreen;
        if(!active){if(mc.player.tickCount>=switchingUntil)close();return;}
        var npc=mc.level.getEntity(dialogue.entityId());if(npc==null||!npc.isAlive()||mc.player.distanceToSqr(npc)>36){if(mc.screen instanceof MerchantScreen)mc.player.closeContainer();else mc.setScreen(null);close();return;}
        if(mc.player.tickCount%20==0)mc.player.connection.send(new RpgNetwork.Request(305,dialogue.entityId()));
    }
    public static void init(ScreenEvent.Init.Post event){
        if(dialogue==null||!(event.getScreen() instanceof MerchantScreen screen))return;
        int y=Math.max(2,screen.getGuiTop()-23),x=screen.getGuiLeft()+screen.getXSize()/2-82;
        var trade=Button.builder(Component.literal("Trocas"),b->{}).bounds(x,y,80,20).build();trade.active=false;event.addListener(trade);
        event.addListener(Button.builder(Component.literal("Missões"),b->request(303)).bounds(x+84,y,80,20).build());
    }
}
