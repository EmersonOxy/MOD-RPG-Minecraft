package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Cosmetics are independent of gameplay and enemy attack warnings. */
public final class CombatVisualSettingsScreen extends Screen {
    private final Screen parent;private int x,y,w,h;
    public CombatVisualSettingsScreen(Screen parent){super(Component.translatable("screen.oxyrpg.combat_visuals"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){CombatOptions.save();minecraft.setScreen(parent);}
    @Override protected void init(){
        CombatOptions.load();w=Math.min(420,width-16);h=Math.min(244,height-8);x=(width-w)/2;y=(height-h)/2;
        toggle(30,0,"option.oxyrpg.cast_animations",CombatOptions.animations,()->CombatOptions.animations=!CombatOptions.animations);
        toggle(56,0,"option.oxyrpg.class_effects",CombatOptions.effects,()->CombatOptions.effects=!CombatOptions.effects);
        toggle(82,0,"option.oxyrpg.swarm_visuals",CombatOptions.swarm,()->CombatOptions.swarm=!CombatOptions.swarm);
        toggle(108,0,"option.oxyrpg.blood_marks",CombatOptions.bloodMarks,()->CombatOptions.bloodMarks=!CombatOptions.bloodMarks);
        toggle(30,1,"option.oxyrpg.blood",CombatOptions.blood,()->CombatOptions.blood=!CombatOptions.blood);
        toggle(56,1,"option.oxyrpg.smoke",CombatOptions.smoke,()->CombatOptions.smoke=!CombatOptions.smoke);
        toggle(82,1,"option.oxyrpg.impacts",CombatOptions.impacts,()->CombatOptions.impacts=!CombatOptions.impacts);
        addRenderableWidget(Button.builder(Component.translatable("option.oxyrpg.particle_intensity").append(": "+Math.round(CombatOptions.particleIntensity*100)+"%"),b->{CombatOptions.particleIntensity=CombatOptions.particleIntensity>=1.5?.5:CombatOptions.particleIntensity+.5;CombatOptions.save();rebuildWidgets();}).bounds(x+12,y+row(134),w-24,buttonHeight()).build());
        addRenderableWidget(Button.builder(Component.translatable("option.oxyrpg.blood_duration").append(": "+CombatOptions.bloodMarkSeconds+" s"),b->{int n=CombatOptions.bloodMarkSeconds;CombatOptions.bloodMarkSeconds=n<30?30:n<60?60:n<120?120:15;CombatOptions.save();rebuildWidgets();}).bounds(x+12,y+row(160),w-24,buttonHeight()).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(x+12,y+row(213),w-24,buttonHeight()).build());
    }
    private int row(int n){return n*h/244;}
    private int buttonHeight(){return Math.min(20,Math.max(12,24*h/244));}
    private void toggle(int offset,int column,String key,boolean value,Runnable action){
        int columnWidth=(w-30)/2;
        addRenderableWidget(Button.builder(Component.translatable(key).append(": ").append(Component.translatable(value?"options.on":"options.off")),b->{action.run();CombatOptions.save();rebuildWidgets();}).bounds(x+12+column*(columnWidth+6),y+row(offset),columnWidth,buttonHeight()).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){RpgUi.panel(g,x,y,w,h);g.drawCenteredString(font,title,width/2,y+8,RpgUi.GOLD);g.drawCenteredString(font,Component.translatable("option.oxyrpg.particles_hint"),width/2,y+row(188),RpgUi.MUTED);super.render(g,mx,my,dt);}
}
