package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** The camera chooses the target; ordinary reach and the player's clear path still gate interaction. */
public final class CameraAim {
    private CameraAim() {}
    public static void pick(float partial){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.level==null||mc.getCameraEntity()!=p||!ShoulderCamera.active()||mc.options.getCameraType().isMirrored())return;
        var camera=mc.gameRenderer.getMainCamera();var start=camera.getPosition();
        var direction=new Vec3(new org.joml.Vector3f(0,0,-1).rotate(camera.rotation()));
        var eye=p.getEyePosition(partial);double range=Math.max(p.blockInteractionRange(),p.entityInteractionRange())+start.distanceTo(eye)+.25;
        var end=start.add(direction.scale(range));
        var block=mc.level.clip(new ClipContext(start,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
        var mob=ProjectileUtil.getEntityHitResult(p,start,block.getLocation(),new AABB(start,block.getLocation()).inflate(1),
            e->EntitySelector.CAN_BE_PICKED.test(e)&&!EnemyNameplates.ally(e.getId()),start.distanceToSqr(block.getLocation()));
        HitResult hit=mob==null?block:mob;var point=hit.getLocation();
        double reach=hit instanceof EntityHitResult?p.entityInteractionRange():p.blockInteractionRange();
        var obstruction=mc.level.clip(new ClipContext(eye,point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        boolean clear=obstruction.getType()==HitResult.Type.MISS||obstruction.getLocation().distanceToSqr(point)<.01;
        if(!clear||eye.distanceToSqr(point)>reach*reach){
            var delta=point.subtract(eye);hit=BlockHitResult.miss(point,net.minecraft.core.Direction.getApproximateNearest(delta.x,delta.y,delta.z),net.minecraft.core.BlockPos.containing(point));
        }
        mc.hitResult=hit;mc.crosshairPickEntity=hit instanceof EntityHitResult entity?entity.getEntity():null;
    }
}
