package dev.emersonoxy.rpg.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class InventorySettingsScreen extends Screen {
    private final Screen parent;private int left,top,w;
    public InventorySettingsScreen(Screen parent){super(Component.literal("Inventário"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){InventoryMovement.load();w=Math.min(340,width-16);left=(width-w)/2;top=(height-140)/2;addRenderableWidget(Button.builder(Component.literal("Mover em inventários, baús e Esc: "+(InventoryMovement.enabled?"sim":"não")),b->{InventoryMovement.enabled=!InventoryMovement.enabled;InventoryMovement.save();rebuildWidgets();}).bounds(left+12,top+38,w-24,20).build());addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+110,w-24,20).build());}
    @Override public void render(GuiGraphics g,int x,int y,float delta){RpgUi.panel(g,left,top,w,140);g.drawCenteredString(font,title,width/2,top+12,RpgUi.GOLD);g.drawCenteredString(font,"Usa suas teclas de movimento configuradas.",width/2,top+72,RpgUi.TEXT);g.drawCenteredString(font,"Pausa ao digitar em campos de pesquisa.",width/2,top+87,RpgUi.MUTED);super.render(g,x,y,delta);}
}
