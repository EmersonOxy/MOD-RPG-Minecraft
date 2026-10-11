package dev.emersonoxy.rpg.client;
import java.util.*;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
/** Short depth-occluded impact waves; bounded and cleared on world changes. */
public final class SkillEffectsRenderer {
    private record Wave(Vec3 center,float radius,int kind,long born){}
    private static final List<Wave> WAVES=new ArrayList<>();
    private SkillEffectsRenderer(){}
    public static void clear(){WAVES.clear();}
    public static void receive(RpgNetwork.ImpactFx p){var mc=Minecraft.getInstance();if(mc.level==null||!Float.isFinite(p.radius())||p.radius()<=0||p.radius()>16||!Double.isFinite(p.x())||!Double.isFinite(p.y())||!Double.isFinite(p.z()))return;if(WAVES.size()>=12)WAVES.removeFirst();WAVES.add(new Wave(new Vec3(p.x(),p.y(),p.z()),p.radius(),p.kind(),mc.level.getGameTime()));}
    public static void render(GuiGraphics g){var mc=Minecraft.getInstance();if(mc.level==null)return;double now=mc.level.getGameTime()+mc.gameRenderer.getMainCamera().getPartialTickTime();WAVES.removeIf(w->now-w.born>14);CombatOptions.load();if(!CombatOptions.effects||mc.options.hideGui||mc.screen!=null||mc.getDebugOverlay().showDebugScreen()||DynamicHud.mode()==DynamicHud.Mode.HIDDEN)return;
        for(var w:WAVES){float t=(float)Math.clamp((now-w.born)/14,0,1);double radius=w.radius*(.2+.8*Math.sqrt(t));int rgb=w.kind==6?0xD7EFF8:w.kind==2?0x70DCEB:w.kind==3?0xCF9AF2:w.kind==4?0xF0D79F:0xFFAA62;int color=((int)((1-t)*210)<<24)|rgb;
            var points=new Vec3[17];for(int i=0;i<points.length;i++){double a=i*Math.PI/8;points[i]=w.center.add(Math.cos(a)*radius,.15,Math.sin(a)*radius);}WorldLines.contour(g,points,color);
        }
    }
}
