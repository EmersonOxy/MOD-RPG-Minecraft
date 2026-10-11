package dev.emersonoxy.rpg.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.render.state.GuiItemRenderState;

/** Opacity is captured when GUI commands are queued, including deferred item icons. */
public final class HudOpacity {
    private static float current=1;
    private static final Map<GuiItemRenderState,Float> items=new WeakHashMap<>();
    private HudOpacity(){}
    public static void begin(float alpha){current=Math.clamp(alpha,0,1);}
    public static void end(){current=1;}
    public static int color(int color){
        int alpha=Math.round((color>>>24)*current);
        // Very low alpha is treated as an unspecified/opaque color by some font paths.
        if(alpha<4)alpha=0;
        return color&0xFFFFFF|alpha<<24;
    }
    public static Integer color(Integer color){return color==null?null:color(color.intValue());}
    public static void capture(GuiItemRenderState state){if(current<1)items.put(state,current);}
    public static int itemColor(GuiItemRenderState state){
        var alpha=items.remove(state);if(alpha==null)return -1;
        // Item atlas uses premultiplied alpha, so fade RGB together with alpha.
        int channel=Math.round(alpha*255);return channel<<24|channel<<16|channel<<8|channel;
    }
}
