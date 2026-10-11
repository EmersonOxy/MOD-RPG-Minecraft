package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public final class EvasionHud {
    private static RpgNetwork.EvasionState state;private static long received;
    private EvasionHud(){}
    public static void clear(){state=null;received=0;}
    public static void receive(RpgNetwork.EvasionState value){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(state==null||value.maximum()!=state.maximum()||value.energy()<state.energy())DynamicHud.signal(DynamicHud.Part.EVASION);
        state=value;received=mc.level.getGameTime();
    }
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(state==null||mc.player==null||mc.level==null||mc.player.isSpectator()||mc.options.hideGui||mc.screen!=null)return;
        float energy=Math.min(state.maximum(),state.energy()+(mc.level.getGameTime()+mc.getDeltaTracker().getGameTimeDeltaPartialTick(false)-received)/Math.max(1f,state.recharge()));
        if(energy<state.maximum())DynamicHud.signal(DynamicHud.Part.EVASION);
        float alpha=DynamicHud.alpha(DynamicHud.Part.EVASION);if(alpha<.01)return;HudOpacity.begin(alpha);
        int x=g.guiWidth()/2-91,y=g.guiHeight()-25;
        var background=ResourceLocation.withDefaultNamespace("hud/experience_bar_background");var progress=ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
        for(int i=0;i<3;i++){
            int sx=x+i*62;g.blitSprite(RenderPipelines.GUI_TEXTURED,background,sx,y,58,5);
            if(i>=state.maximum()){g.fill(sx,y,sx+58,y+5,0xAA11151D);continue;}
            float fill=Math.clamp(energy-i,0,1);int filled=(int)(fill*58);
            if(filled>0){g.enableScissor(sx,y,sx+filled,y+5);g.blitSprite(RenderPipelines.GUI_TEXTURED,progress,sx,y,58,5);g.disableScissor();}
            if(fill<1)g.fill(sx+27,y+2,sx+31,y+3,0xFF758194);
        }
        HudOpacity.end();
    }
}
