package dev.emersonoxy.rpg.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class AmbientSettingsScreen extends Screen {
    private final Screen parent;private int x,y,w;public AmbientSettingsScreen(Screen parent){super(Component.literal("Ambiente"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){AmbientOptions.save();minecraft.setScreen(parent);}
    @Override protected void init(){AmbientOptions.load();w=Math.min(340,width-16);x=(width-w)/2;y=Math.max(4,(height-166)/2);toggle(34,"Folhas e vento",AmbientOptions.leaves,()->AmbientOptions.leaves=!AmbientOptions.leaves);toggle(62,"Rastro de voo na água",AmbientOptions.water,()->AmbientOptions.water=!AmbientOptions.water);toggle(90,"Vegetação reativa",AmbientOptions.vegetation,()->AmbientOptions.vegetation=!AmbientOptions.vegetation);addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(x+12,y+130,w-24,20).build());}
    private void toggle(int row,String text,boolean value,Runnable change){addRenderableWidget(Button.builder(Component.literal(text+": "+(value?"sim":"não")),b->{change.run();AmbientOptions.save();rebuildWidgets();}).bounds(x+12,y+row,w-24,20).build());}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){RpgUi.panel(g,x,y,w,166);g.drawCenteredString(font,title,width/2,y+12,RpgUi.GOLD);super.render(g,mx,my,partial);}
}
