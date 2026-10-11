package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class SpecialistHud {
    private static float heat;private static long overheated;private static net.minecraft.client.multiplayer.ClientLevel world;
    private SpecialistHud(){}
    public static void receive(RpgNetwork.SpecialState state){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
        world=mc.level;heat=state.heat();overheated=mc.level.getGameTime()+state.overheated();
        var tag=mc.player.getPersistentData();
        if(state.phase()>0){tag.putLong("oxy_eclipse",mc.level.getGameTime()+state.ticks());tag.putInt("oxy_phase_tier",state.phase());}else{tag.remove("oxy_eclipse");tag.remove("oxy_phase_tier");}
        if(state.waterwalk())tag.putLong("oxy_waterwalk",mc.level.getGameTime()+state.ticks());else tag.remove("oxy_waterwalk");
        if(state.heat()>0||state.overheated()>0)DynamicHud.signal(DynamicHud.Part.RPG);
    }
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(world!=mc.level||mc.player==null||EquipmentAccess.clientSelected()!=RpgClass.PIROMANTE)return;
        float alpha=DynamicHud.alpha(DynamicHud.Part.RPG);if(alpha<=0)return;HudOpacity.begin(alpha);
        int x=g.guiWidth()/2-91,y=g.guiHeight()-Math.max(106,mc.gui.leftHeight+60);boolean hot=overheated>mc.level.getGameTime();
        g.fill(x,y,x+182,y+7,0xC01A2029);for(int i=0;i<10;i++){int sx=x+2+i*18;g.fill(sx,y+2,sx+16,y+5,0xFF553329);int fill=(int)(16*Math.clamp(heat/10-i,0,1));if(fill>0)g.fill(sx,y+2,sx+fill,y+5,heat>80?0xFFFF6843:0xFFFFB35D);}
        String label=hot?"Superaquecido • "+((overheated-mc.level.getGameTime()+19)/20)+" s":"Calor "+(int)heat+"/100";g.drawCenteredString(mc.font,label,g.guiWidth()/2,y-11,hot?0xFFFF7760:0xFFFFCC8D);HudOpacity.end();
    }
}
