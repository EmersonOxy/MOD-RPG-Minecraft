package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Ground contours projected with the world camera; walls occlude each segment. */
public final class WorldLines {
    private WorldLines(){}
    public static boolean onScreen(Vec3 p){return Double.isFinite(p.x)&&Double.isFinite(p.y)&&p.z>=-1&&p.z<=1&&Math.abs(p.x)<1.1&&Math.abs(p.y)<1.1;}
    public static boolean visible(Vec3 point){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return false;var hit=mc.level.clip(new ClipContext(mc.gameRenderer.getMainCamera().getPosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(point)<.012;}
    public static Vec3 ground(Vec3 point){var mc=Minecraft.getInstance();if(!mc.level.hasChunkAt(net.minecraft.core.BlockPos.containing(point)))return null;var hit=mc.level.clip(new ClipContext(point.add(0,3,0),point.add(0,-6,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));return hit.getType()==HitResult.Type.MISS?null:hit.getLocation().add(0,.035,0);}
    public static void line(GuiGraphics g,Vec3 a,Vec3 b,int color){
        if(a==null||b==null||Math.abs(a.y-b.y)>1.5||!visible(a)||!visible(b))return;
        projectedLine(g,a,b,color);
    }
    /** Adjacent segments share the same visibility result instead of raycasting each point twice. */
    public static void contour(GuiGraphics g,Vec3[] points,int color){
        Vec3 previous=null;boolean previousVisible=false;
        for(var point:points){boolean currentVisible=point!=null&&visible(point);
            if(previousVisible&&currentVisible&&Math.abs(previous.y-point.y)<=1.5)projectedLine(g,previous,point,color);
            previous=point;previousVisible=currentVisible;
        }
    }
    private static void projectedLine(GuiGraphics g,Vec3 a,Vec3 b,int color){
        var x=CameraProjection.project(a);var y=CameraProjection.project(b);if(!onScreen(x)||!onScreen(y))return;
        float ax=(float)((x.x+1)*g.guiWidth()/2),ay=(float)((1-x.y)*g.guiHeight()/2),bx=(float)((y.x+1)*g.guiWidth()/2),by=(float)((1-y.y)*g.guiHeight()/2);
        double length=Math.hypot(bx-ax,by-ay);if(length<.2||length>g.guiWidth()/2.0)return;
        g.pose().pushMatrix();g.pose().translate(ax,ay);g.pose().rotate((float)Math.atan2(by-ay,bx-ax));g.fill(0,0,(int)Math.ceil(length),1,color);g.pose().popMatrix();
    }
}
