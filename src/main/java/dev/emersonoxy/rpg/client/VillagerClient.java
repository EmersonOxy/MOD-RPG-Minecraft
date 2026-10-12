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
    public static RpgNetwork.VillagerDialogue data(){return dialogue;}
    public static void choose(int action,int choice){var mc=Minecraft.getInstance();if(mc.player!=null&&dialogue!=null)mc.player.connection.send(new RpgNetwork.Request(action,dialogue.entityId(),choice));}
    public static int sideWidth(int width){return width>=416?Math.min(220,width-296):0;}
    public static void receive(RpgNetwork.VillagerDialogue data){boolean same=dialogue!=null&&dialogue.entityId()==data.entityId();dialogue=data;switchingUntil=0;var mc=Minecraft.getInstance();if(same&&mc.screen instanceof MerchantScreen)return;if(same&&mc.screen instanceof VillagerDialogueScreen)return;mc.setScreen(new VillagerDialogueScreen(data));}
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
        int side=sideWidth(screen.width);if(side>0){VillagerPanel.init(screen.getGuiLeft()+282,Math.max(4,screen.getGuiTop()-30),side,event::addListener);return;}
        event.addListener(Button.builder(Component.literal("Missões / diálogo"),b->request(303)).bounds(screen.getGuiLeft()+65,Math.max(2,screen.getGuiTop()-23),150,20).build());
    }
    public static void scroll(net.neoforged.neoforge.client.event.ScreenEvent.MouseScrolled.Pre e){if(dialogue!=null&&e.getScreen() instanceof MerchantScreen screen&&sideWidth(screen.width)>0&&e.getMouseX()>=screen.getGuiLeft()+282){VillagerPanel.scroll(e.getScrollDeltaY());e.setCanceled(true);}}
    public static void render(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post e){if(dialogue!=null&&e.getScreen() instanceof MerchantScreen screen){int side=sideWidth(screen.width);if(side>0)VillagerPanel.render(e.getGuiGraphics(),screen.getGuiLeft()+282,Math.max(4,screen.getGuiTop()-30),side);}}
}
