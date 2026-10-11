package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** Cached ground contours; ranged/buff preparations are labels, not misleading damage areas. */
public final class EliteWarnings {
    private record Warning(int entity,Vec3 center,Vec3[] ring,long until,int duration,int kind){}
    private static final List<Warning> ACTIVE=new ArrayList<>();
    private EliteWarnings(){}
    public static void clear(){ACTIVE.clear();}
    public static void receive(RpgNetwork.EliteWarning packet){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        if(!Double.isFinite(packet.x())||!Double.isFinite(packet.y())||!Double.isFinite(packet.z())
            ||!Float.isFinite(packet.radius())||packet.radius()<=0||packet.radius()>16||packet.ticks()<1||packet.ticks()>1200)return;
        var center=new Vec3(packet.x(),packet.y(),packet.z());if(center.distanceToSqr(mc.player.position())>64*64)return;
        var ring=new Vec3[packet.kind()==12||packet.kind()==13?0:25];
        for(int i=0;i<ring.length;i++){double a=i*Math.PI/12;ring[i]=WorldLines.ground(center.add(Math.cos(a)*packet.radius(),0,Math.sin(a)*packet.radius()));}
        ACTIVE.removeIf(w->w.entity()==packet.entity()&&w.kind()==packet.kind());
        if(ACTIVE.size()>=16)ACTIVE.removeFirst();
        ACTIVE.add(new Warning(packet.entity(),WorldLines.ground(center),ring,mc.level.getGameTime()+packet.ticks(),packet.ticks(),packet.kind()));
    }
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double now=mc.level.getGameTime()+mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        ACTIVE.removeIf(w->now>=w.until()||mc.level.getEntity(w.entity())==null||!mc.level.getEntity(w.entity()).isAlive());
        if(mc.options.hideGui||mc.screen!=null)return;int budget=8;
        for(var w:ACTIVE){
            if(budget--<=0)break;
            float progress=(float)Math.clamp(1-(w.until()-now)/w.duration(),0,1);
            boolean defensive=w.kind()==3||w.kind()==13;int rgb=defensive?0x68CFF0:0xFF8058;
            int color=((int)(170+85*progress)<<24)|rgb;
            WorldLines.contour(g,w.ring(),color);
            if(w.center()==null||!WorldLines.visible(w.center()))continue;
            var p=CameraProjection.project(w.center());if(!WorldLines.onScreen(p))continue;
            String key=switch(w.kind()){case 3->"barrier";case 12->"volley";case 13->"empower";default->"strike";};
            String label=Component.translatable("warning.oxyrpg."+key).getString();
            int x=(int)((p.x+1)*g.guiWidth()/2),y=(int)((1-p.y)*g.guiHeight()/2)-16;
            g.drawCenteredString(mc.font,label,x,y,color);g.fill(x-15,y+11,x+15,y+13,0xA5222930);g.fill(x-15,y+11,x-15+(int)(30*progress),y+13,color);
        }
    }
}
