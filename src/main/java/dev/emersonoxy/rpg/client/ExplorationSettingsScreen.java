package dev.emersonoxy.rpg.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class ExplorationSettingsScreen extends Screen {
    private final Screen parent;private int x,y,w;
    public ExplorationSettingsScreen(Screen parent){super(Component.literal("Bússola e exploração"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){ExplorationHud.load();w=Math.min(340,width-16);x=(width-w)/2;y=Math.max(4,(height-216)/2);
        addRenderableWidget(Button.builder(Component.literal("Bússola: "+(ExplorationHud.compass?"ligada":"desligada")),b->{ExplorationHud.compass=!ExplorationHud.compass;ExplorationHud.save();rebuildWidgets();}).bounds(x+12,y+30,w-24,20).build());
        addRenderableWidget(new HeightSlider(x+12,y+56,w-24));
        addRenderableWidget(Button.builder(Component.literal("Graus: "+(ExplorationHud.degrees?"mostrar":"ocultar")),b->{ExplorationHud.degrees=!ExplorationHud.degrees;ExplorationHud.save();rebuildWidgets();}).bounds(x+12,y+82,w-24,20).build());
        int half=(w-28)/2;
        addRenderableWidget(Button.builder(Component.literal("Marcar local atual"),b->{ExplorationHud.markHere();rebuildWidgets();}).bounds(x+12,y+108,half,20).build());
        var clear=addRenderableWidget(Button.builder(Component.literal("Limpar marcador"),b->{ExplorationHud.clearMarker();rebuildWidgets();}).bounds(x+16+half,y+108,half,20).build());clear.active=ExplorationHud.hasMarker();
        addRenderableWidget(Button.builder(Component.literal("Avisos de região: "+(ExplorationHud.regions?"sim":"não")),b->{ExplorationHud.regions=!ExplorationHud.regions;ExplorationHud.save();rebuildWidgets();}).bounds(x+12,y+134,w-24,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(x+12,y+186,w-24,20).build());}
    private static final class HeightSlider extends net.minecraft.client.gui.components.AbstractSliderButton {
        HeightSlider(int x,int y,int width){super(x,y,width,20,Component.empty(),(ExplorationHud.compassY-24)/32.0);updateMessage();}
        @Override protected void updateMessage(){setMessage(Component.literal("Altura da bússola: "+ExplorationHud.compassY));}
        @Override protected void applyValue(){ExplorationHud.compassY=24+(int)Math.round(value*32);ExplorationHud.save();}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){RpgUi.panel(g,x,y,w,216);g.drawCenteredString(font,title,width/2,y+12,RpgUi.GOLD);g.drawCenteredString(font,"Marcador dourado: local · azul: alvo travado",width/2,y+164,RpgUi.MUTED);super.render(g,mx,my,dt);}
}
