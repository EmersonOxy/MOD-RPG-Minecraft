package dev.emersonoxy.rpg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A validated aim hint for this cast only. Projectiles still collide normally;
 * there is no homing, automatic attack or direct damage to the chosen target. */
public final class AbilityAim {
    private static final ThreadLocal<Vec3> POINT=new ThreadLocal<>();
    public static Vec3 direction(ServerPlayer p){var point=POINT.get();return point==null?p.getLookAngle():point.subtract(p.getEyePosition()).normalize();}
    private static Vec3 validatedPoint(ServerPlayer p,Vec3 point){
        if(!Double.isFinite(point.x)||!Double.isFinite(point.y)||!Double.isFinite(point.z)||!p.isAlive()||p.isSpectator())return null;
        var delta=point.subtract(p.getEyePosition());
        if(delta.lengthSqr()<.04||delta.lengthSqr()>65*65||delta.normalize().dot(p.getLookAngle())<.45)return null;
        return p.level().clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
    }
    private static final ThreadLocal<Mob> TARGET=new ThreadLocal<>();
    private AbilityAim(){}
    public static void cast(ServerPlayer player,int targetId,Vec3 point,Runnable action){
        var previousPoint=POINT.get();var validatedPoint=validatedPoint(player,point);if(validatedPoint==null)POINT.remove();else POINT.set(validatedPoint);
        var previous=TARGET.get();var target=validated(player,targetId);
        if(target==null)TARGET.remove();else TARGET.set(target);
        try{action.run();}finally{if(previousPoint==null)POINT.remove();else POINT.set(previousPoint);if(previous==null)TARGET.remove();else TARGET.set(previous);}
    }
    private static Mob validated(ServerPlayer p,int id){
        if(id<0||!p.isAlive()||p.isSpectator()||p.isSleeping()||p.isPassenger())return null;
        var entity=p.level().getEntity(id);
        if(!(entity instanceof Mob mob)||!mob.isAlive()||mob.isRemoved()||mob.isInvisible()||p.distanceToSqr(mob)>24*24
            ||mob instanceof Villager||mob instanceof TamableAnimal pet&&pet.isTame()||RpgNecromancer.isSummon(mob)||!p.hasLineOfSight(mob))return null;
        var point=mob.position().add(0,mob.getBbHeight()*.6,0);var delta=point.subtract(p.getEyePosition());
        if(delta.lengthSqr()<1e-8||delta.normalize().dot(p.getLookAngle())<.45)return null;
        var hit=p.level().clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS?mob:null;
    }
    public static Mob target(ServerPlayer p){
        var locked=TARGET.get();if(locked!=null)return locked;
        var direction=direction(p);return p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(24),m->RpgAbilities.attackable(m)&&!(m instanceof Villager)&&!(m instanceof TamableAnimal pet&&pet.isTame())&&p.hasLineOfSight(m))
            .stream().filter(m->{var delta=m.getBoundingBox().getCenter().subtract(p.getEyePosition());return delta.lengthSqr()<=24*24&&delta.normalize().dot(direction)>.985;}).min(java.util.Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
    }
    public static void shoot(Projectile shot,ServerPlayer p,float speed){
        var mob=TARGET.get();
        if(mob==null){
            var point=POINT.get();var aim=point==null?direction(p):point.subtract(shot.position());
            if(!shot.isNoGravity()&&point!=null){double time=Math.min(24/speed,aim.length()/speed);aim=aim.add(0,.025*time*time,0);}
            shot.shoot(aim.x,aim.y,aim.z,speed,0);return;
        }
        Vec3 offset=mob.position().add(0,mob.getBbHeight()*.6,0).subtract(shot.position());
        var velocity=mob.getDeltaMovement();
        // Solve an interception for constant velocity, capped to ordinary lock range.
        double a=velocity.lengthSqr()-speed*speed,b=2*offset.dot(velocity),c=offset.lengthSqr();
        double time=offset.length()/speed,discriminant=b*b-4*a*c;
        if(Math.abs(a)>1e-8&&discriminant>=0){
            double root=Math.sqrt(discriminant),first=(-b-root)/(2*a),second=(-b+root)/(2*a);
            if(first>0&&second>0)time=Math.min(first,second);else if(first>0)time=first;else if(second>0)time=second;
        }
        time=Math.clamp(time,0,24/speed);
        var aim=offset.add(velocity.scale(time));
        if(!shot.isNoGravity())aim=aim.add(0,.025*time*time,0);
        // shootFromRotation adds the caster's velocity, which makes a locked shot
        // drift sideways while strafing. Launch toward the predicted point instead.
        shot.shoot(aim.x,aim.y,aim.z,speed,0);
    }
}
