package dev.emersonoxy.rpg.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class ExplorationSettingsScreen extends Screen {
    private final Screen parent;private int x,y,w;
    public ExplorationSettingsScreen(Screen parent){super(Component.literal("Exploração"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){ExplorationHud.load();w=Math.min(340,width-16);x=(width-w)/2;y=(height-136)/2;
        addRenderableWidget(Button.builder(Component.literal("Bússola: "+(ExplorationHud.compass?"sim":"não")),b->{ExplorationHud.compass=!ExplorationHud.compass;ExplorationHud.save();rebuildWidgets();}).bounds(x+12,y+32,w-24,20).build());
        addRenderableWidget(Button.builder(Component.literal("Avisos de região: "+(ExplorationHud.regions?"sim":"não")),b->{ExplorationHud.regions=!ExplorationHud.regions;ExplorationHud.save();rebuildWidgets();}).bounds(x+12,y+58,w-24,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(x+12,y+106,w-24,20).build());}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){RpgUi.panel(g,x,y,w,136);g.drawCenteredString(font,title,width/2,y+12,RpgUi.GOLD);g.drawCenteredString(font,"Painéis possuem seu próprio modo de visibilidade.",width/2,y+86,RpgUi.MUTED);super.render(g,mx,my,dt);}
}
