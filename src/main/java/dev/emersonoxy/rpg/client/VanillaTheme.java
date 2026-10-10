package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

/** Nine-slice of vanilla inventory frame, with a blank center rather than stretched slots. */
public final class VanillaTheme {
    private static final ResourceLocation INVENTORY=ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
    private VanillaTheme(){}
    private static void part(GuiGraphics g,int x,int y,int w,int h,int u,int v,int sw,int sh){if(w>0&&h>0)g.blit(RenderPipelines.GUI_TEXTURED,INVENTORY,x,y,u,v,w,h,sw,sh,256,256,0xFF777777);}
    public static void panel(GuiGraphics g,int x,int y,int w,int h){
        part(g,x+4,y+4,w-8,h-8,166,6,2,2);
        part(g,x,y,4,4,0,0,4,4);part(g,x+w-4,y,4,4,172,0,4,4);
        part(g,x,y+h-4,4,4,0,162,4,4);part(g,x+w-4,y+h-4,4,4,172,162,4,4);
        part(g,x+4,y,w-8,4,4,0,168,4);part(g,x+4,y+h-4,w-8,4,4,162,168,4);
        part(g,x,y+4,4,h-8,0,4,4,158);part(g,x+w-4,y+4,4,h-8,172,4,4,158);
    }
    public static void progress(GuiGraphics g,int x,int y,int w,int amount,int total){
        g.blitSprite(RenderPipelines.GUI_TEXTURED,ResourceLocation.withDefaultNamespace("hud/experience_bar_background"),x,y,w,5);
        int filled=Math.clamp(w*amount/Math.max(1,total),0,w);
        if(filled>0){g.enableScissor(x,y,x+filled,y+5);g.blitSprite(RenderPipelines.GUI_TEXTURED,ResourceLocation.withDefaultNamespace("hud/experience_bar_progress"),x,y,w,5);g.disableScissor();}
    }
}
