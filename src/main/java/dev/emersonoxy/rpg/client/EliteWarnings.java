package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.RpgNetwork;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

public final class EliteWarnings {
    private record Warning(int entity,Vec3 center,Vec3[] ring,long until,int kind){}
    private static final List<Warning> ACTIVE=new ArrayList<>();
    private EliteWarnings(){}
    public static void clear(){ACTIVE.clear();}
    public static void receive(RpgNetwork.EliteWarning packet){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;var center=new Vec3(packet.x(),packet.y(),packet.z());var ring=new Vec3[49];for(int i=0;i<ring.length;i++){double a=i*Math.PI/24;ring[i]=WorldLines.ground(center.add(Math.cos(a)*packet.radius(),0,Math.sin(a)*packet.radius()));}if(ACTIVE.size()>=32)ACTIVE.removeFirst();ACTIVE.add(new Warning(packet.entity(),WorldLines.ground(center),ring,mc.level.getGameTime()+packet.ticks(),packet.kind()));}
    public static void render(GuiGraphics g){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;long now=mc.level.getGameTime();ACTIVE.removeIf(w->now>=w.until||mc.level.getEntity(w.entity)==null||!mc.level.getEntity(w.entity).isAlive());if(mc.options.hideGui||mc.screen!=null)return;
        for(var w:ACTIVE){int color=w.kind==3?0xFF68CFF0:0xFFFF8058;for(int i=0;i<w.ring.length-1;i++)WorldLines.line(g,w.ring[i],w.ring[i+1],color);if(w.center!=null&&WorldLines.visible(w.center)){var p=CameraProjection.project(w.center);if(WorldLines.onScreen(p))g.drawCenteredString(mc.font,w.kind==3?"Barreira":"! "+String.format(java.util.Locale.ROOT,"%.1f s",(w.until-now)/20.0),(int)((p.x+1)*g.guiWidth()/2),(int)((1-p.y)*g.guiHeight()/2)-12,color);}}
    }
}
