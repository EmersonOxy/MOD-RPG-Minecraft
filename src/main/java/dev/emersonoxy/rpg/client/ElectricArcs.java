package dev.emersonoxy.rpg.client;
import java.util.*;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
/** Jagged world-space lightning, using vanilla's lightning shader and depth test. */
public final class ElectricArcs {
    private record Arc(Vec3 from,Vec3 to,long until){}
    private record Segment(Vec3 a,Vec3 b,float alpha){}
    private static final ArrayDeque<Arc> ARCS=new ArrayDeque<>();
    private static final ContextKey<List<Segment>> DRAW=new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","electric_bolts"));
    private static final MultiBufferSource.BufferSource BUFFER=MultiBufferSource.immediate(new ByteBufferBuilder(65536));
    private static Object world;
    private ElectricArcs(){}
    public static void receive(RpgNetwork.ElectricArc v){
        var mc=Minecraft.getInstance();if(mc.level!=world){ARCS.clear();world=mc.level;}if(mc.level==null||mc.player==null)return;
        var start=new Vec3(v.x(),v.y(),v.z());var end=new Vec3(v.tx(),v.ty(),v.tz());
        if(!Double.isFinite(start.lengthSqr())||!Double.isFinite(end.lengthSqr())||start.distanceToSqr(end)>65*65||mc.player.position().distanceToSqr(start)>96*96)return;
        if(ARCS.size()>=64)ARCS.removeFirst();ARCS.addLast(new Arc(start,end,mc.level.getGameTime()+6));
    }
    public static void extract(ExtractLevelRenderStateEvent event){
        var mc=Minecraft.getInstance();if(world!=mc.level){ARCS.clear();world=mc.level;}
        var draws=new ArrayList<Segment>();long now=event.getLevel().getGameTime();ARCS.removeIf(a->a.until<=now);
        var camera=event.getCamera().getPosition();
        for(var arc:ARCS){
            var delta=arc.to.subtract(arc.from);var unit=delta.normalize();var normal=unit.cross(Math.abs(unit.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0)).normalize();var other=unit.cross(normal);
            int n=Math.clamp((int)Math.ceil(delta.length()*2),2,64);var previous=arc.from;
            for(int i=1;i<=n;i++){
                double t=(double)i/n,noise=i==n?0:Math.sin(i*2.31)*.13;var point=arc.from.add(delta.scale(t)).add(normal.scale(noise)).add(other.scale(i==n?0:Math.cos(i*3.7)*.08));
                // Nothing inside the camera near-plane: no giant first-person sparks.
                if(previous.distanceToSqr(camera)>1.2*1.2&&point.distanceToSqr(camera)>1.2*1.2)draws.add(new Segment(previous.subtract(camera),point.subtract(camera),(float)Math.min(1,(arc.until-now)/2.0)));
                previous=point;
            }
        }
        event.getRenderState().setRenderData(DRAW,List.copyOf(draws));
    }
    public static void render(RenderLevelStageEvent.AfterEntities event){
        var draws=event.getLevelRenderState().getRenderData(DRAW);if(draws==null||draws.isEmpty())return;
        var buffer=BUFFER.getBuffer(RenderType.lightning());var matrix=event.getPoseStack().last().pose();
        for(var s:draws){var dir=s.b.subtract(s.a).normalize();var normal=dir.cross(Math.abs(dir.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0)).normalize();var side=dir.cross(normal);
            quad(buffer,matrix,s,normal,.025f,.85f,.95f,1f,.85f);quad(buffer,matrix,s,side,.025f,.85f,.95f,1f,.85f);
            quad(buffer,matrix,s,normal,.09f,.25f,.5f,1f,.2f);quad(buffer,matrix,s,side,.09f,.25f,.5f,1f,.2f);
        }BUFFER.endBatch();
    }
    private static void quad(VertexConsumer b,org.joml.Matrix4f pose,Segment s,Vec3 side,float width,float r,float g,float blue,float alpha){
        var offset=side.scale(width);vertex(b,pose,s.a.add(offset),r,g,blue,alpha*s.alpha);vertex(b,pose,s.b.add(offset),r,g,blue,alpha*s.alpha);vertex(b,pose,s.b.subtract(offset),r,g,blue,alpha*s.alpha);vertex(b,pose,s.a.subtract(offset),r,g,blue,alpha*s.alpha);
    }
    private static void vertex(VertexConsumer b,org.joml.Matrix4f pose,Vec3 v,float r,float g,float blue,float alpha){b.addVertex(pose,(float)v.x,(float)v.y,(float)v.z).setColor(r,g,blue,alpha);}
}
