package dev.emersonoxy.rpg.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

/** Look assistance uses ordinary rotation packets, without bypassing server reach. */
public final class TargetLock {
    private static Mob target;
    private static Mob firstPersonTarget;
    private static long firstPersonFrame;
    private static double firstPersonYaw,firstPersonPitch;
    private TargetLock(){}
    public static Mob target(){return target;}
    public static void clear(){target=null;firstPersonTarget=null;firstPersonFrame=0;}
    public static boolean mouseLocked(){var mc=Minecraft.getInstance();return !BarrelRollCompat.active()&&target!=null&&mc.screen==null&&valid(target);}
    private static boolean valid(Mob mob){
        var mc=Minecraft.getInstance();var p=mc.player;
        return p!=null&&mc.level!=null&&mob.level()==mc.level&&mob.isAlive()&&!mob.isRemoved()&&!mob.isInvisible()
            &&!p.isSpectator()&&p.isAlive()&&p.distanceToSqr(mob)<=24*24&&p.hasLineOfSight(mob)
            &&mc.level.clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),point(mob,1),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()==net.minecraft.world.phys.HitResult.Type.MISS
            &&!(mob instanceof TamableAnimal pet&&pet.isTame())&&!EnemyNameplates.ally(mob.getId());
    }
    private static List<Mob> candidates(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return List.of();var p=mc.player;var eye=p.getEyePosition();var look=p.getLookAngle();
        return mc.level.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(24),TargetLock::valid).stream()
            .filter(m->point(m,1).subtract(eye).normalize().dot(look)>.45)
            .filter(m->{var screen=mc.gameRenderer.projectPointToScreen(point(m,1));return screen.z>=-1&&screen.z<=1&&Math.abs(screen.x)<.98&&Math.abs(screen.y)<.98;})
            .sorted(Comparator.comparingDouble(m->(1-point(m,1).subtract(eye).normalize().dot(look))*10+p.distanceTo(m)*.01)).toList();
    }
    public static void toggle(){if(target!=null){clear();message("Trava de mira desligada.");return;}var list=candidates();target=list.isEmpty()?null:list.getFirst();message(target==null?"Nenhuma criatura visível a até 24 blocos.":"Mira travada: "+target.getName().getString());}
    public static void cycle(){var list=candidates();if(list.isEmpty()){clear();return;}int index=list.indexOf(target);target=list.get((index+1)%list.size());message("Mira travada: "+target.getName().getString());}
    private static void message(String text){var p=Minecraft.getInstance().player;if(p!=null)p.displayClientMessage(Component.literal(text),true);}
    public static Vec3 point(Mob mob,float partial){return mob.getPosition(partial).add(0,mob.getBbHeight()*.6,0);}
    /** Render from interpolated positions, rather than interpolating two tick-rate aim angles.
     * This keeps strafing and close targets from pulling the camera behind the player. */
    public static Vec3 viewDirection(float partial){
        var mc=Minecraft.getInstance();
        if(!mouseLocked()||mc.isPaused()||mc.player.isSleeping()||mc.player.isPassenger())return null;
        var delta=point(target,partial).subtract(mc.player.getEyePosition(partial));
        return delta.lengthSqr()<1e-8?null:delta.normalize();
    }
    /** LocalPlayer does not interpolate view rotation in first person. Render aim
     * at frame rate here, while the server continues receiving tick-rate rotation. */
    public static net.minecraft.world.phys.Vec2 firstPersonRotation(float partial){
        var direction=viewDirection(partial);if(direction==null){firstPersonFrame=0;return null;}
        var mc=Minecraft.getInstance();long now=System.nanoTime();
        if(firstPersonTarget!=target){
            firstPersonTarget=target;firstPersonYaw=mc.player.getYRot();firstPersonPitch=mc.player.getXRot();firstPersonFrame=now;
        }
        double dt=firstPersonFrame==0?0:Math.min(.1,(now-firstPersonFrame)/1e9);firstPersonFrame=now;
        double yaw=Math.toDegrees(Math.atan2(-direction.x,direction.z));
        double pitch=-Math.toDegrees(Math.atan2(direction.y,direction.horizontalDistance()));
        firstPersonYaw=CameraMotion.angle(firstPersonYaw,yaw,24,dt);
        firstPersonPitch=CameraMotion.smooth(firstPersonPitch,pitch,24,dt);
        return new net.minecraft.world.phys.Vec2((float)firstPersonYaw,(float)firstPersonPitch);
    }
    public static void tick(){
        var mc=Minecraft.getInstance();if(target==null)return;
        if(!valid(target)||mc.player.isSleeping()||mc.player.isPassenger()||mc.options.getCameraType().isMirrored()){clear();return;}
        if(mc.screen!=null||mc.isPaused()||BarrelRollCompat.active())return;var p=mc.player;var look=point(target,1).subtract(p.getEyePosition());
        float yaw=(float)Math.toDegrees(Math.atan2(-look.x,look.z)),pitch=(float)-Math.toDegrees(Math.atan2(look.y,look.horizontalDistance()));
        p.setYRot(p.getYRot()+Mth.wrapDegrees(yaw-p.getYRot()));p.setXRot(Mth.clamp(pitch,-89,89));
    }
}
