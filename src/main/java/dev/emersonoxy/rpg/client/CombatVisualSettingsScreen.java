package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Independent cosmetic controls: attack warnings remain available for combat readability. */
public final class CombatVisualSettingsScreen extends Screen {
    private final Screen parent;private int x,y,w,h;
    public CombatVisualSettingsScreen(Screen parent){super(Component.translatable("screen.oxyrpg.combat_visuals"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){CombatOptions.save();minecraft.setScreen(parent);}
    @Override protected void init(){
        CombatOptions.load();w=Math.min(340,width-16);h=Math.min(160,height-8);x=(width-w)/2;y=(height-h)/2;
        toggle(30,"option.oxyrpg.cast_animations",CombatOptions.animations,()->CombatOptions.animations=!CombatOptions.animations);
        toggle(56,"option.oxyrpg.class_effects",CombatOptions.effects,()->CombatOptions.effects=!CombatOptions.effects);
        toggle(82,"option.oxyrpg.swarm_visuals",CombatOptions.swarm,()->CombatOptions.swarm=!CombatOptions.swarm);
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(x+12,y+row(130),w-24,20).build());
    }
    private int row(int n){return n*h/160;}
    private void toggle(int offset,String key,boolean value,Runnable action){
        addRenderableWidget(Button.builder(Component.translatable(key).append(": ").append(Component.translatable(value?"options.on":"options.off")),b->{action.run();CombatOptions.save();rebuildWidgets();}).bounds(x+12,y+row(offset),w-24,Math.min(20,h/8)).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){RpgUi.panel(g,x,y,w,h);g.drawCenteredString(font,title,width/2,y+8,RpgUi.GOLD);g.drawCenteredString(font,Component.translatable("option.oxyrpg.visuals_hint"),width/2,y+row(111),RpgUi.MUTED);super.render(g,mx,my,dt);}
}
