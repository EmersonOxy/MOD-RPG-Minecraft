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
    private record Hint(Vec3 point,long tick,net.minecraft.server.level.ServerLevel level){}
    private static final java.util.Map<java.util.UUID,Hint> LIVE=new java.util.HashMap<>();
    public static void receive(ServerPlayer p,Vec3 point){var valid=validatedPoint(p,point);if(valid!=null)LIVE.put(p.getUUID(),new Hint(valid,p.level().getGameTime(),p.level()));}
    private static Vec3 live(ServerPlayer p){var hint=LIVE.get(p.getUUID());return hint!=null&&hint.level()==p.level()&&p.level().getGameTime()-hint.tick()<=10?hint.point():null;}
    public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){LIVE.remove(e.getEntity().getUUID());}
    public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){LIVE.clear();}
    public static boolean nativeShot(Projectile shot,ServerPlayer p,float speed,float spread,float yawOffset){
        var point=live(p);if(point==null||!(shot instanceof net.minecraft.world.entity.projectile.AbstractArrow)||speed<=0)return false;
        var aim=point.subtract(shot.position());if(aim.lengthSqr()<.001)return false;
        if(!shot.isNoGravity()){double time=Math.min(24/speed,aim.length()/speed);aim=aim.add(0,.025*time*time,0);}
        aim=aim.normalize().yRot((float)Math.toRadians(-yawOffset));shot.shoot(aim.x,aim.y,aim.z,speed,spread);return true;
    }
    public static Vec3 point(ServerPlayer p,double range){var hint=POINT.get();if(hint==null)hint=live(p);var end=hint!=null&&hint.distanceToSqr(p.getEyePosition())<=range*range?hint:p.getEyePosition().add(direction(p).scale(range));return p.level().clip(new ClipContext(p.getEyePosition(),end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();}
    public static Vec3 direction(ServerPlayer p){var point=POINT.get();if(point==null)point=live(p);return point==null?p.getLookAngle():point.subtract(p.getEyePosition()).normalize();}
    private static Vec3 validatedPoint(ServerPlayer p,Vec3 point){
        if(!Double.isFinite(point.x)||!Double.isFinite(point.y)||!Double.isFinite(point.z)||!p.isAlive()||p.isSpectator())return null;
        var delta=point.subtract(p.getEyePosition());
        if(delta.lengthSqr()<.04||delta.lengthSqr()>Math.pow(Math.max(65,TargetingRange.of(p)+8),2))return null;
        return p.level().clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
    }
    private static final ThreadLocal<Mob> TARGET=new ThreadLocal<>();
    private AbilityAim(){}
    public static void cast(ServerPlayer player,int targetId,Vec3 point,Runnable action){
        var previousPoint=POINT.get();var validatedPoint=validatedPoint(player,point);if(validatedPoint==null)POINT.remove();else{POINT.set(validatedPoint);LIVE.put(player.getUUID(),new Hint(validatedPoint,player.level().getGameTime(),player.level()));}
        var previous=TARGET.get();var target=validated(player,targetId);
        if(target==null)TARGET.remove();else TARGET.set(target);
        try{action.run();}finally{if(previousPoint==null)POINT.remove();else POINT.set(previousPoint);if(previous==null)TARGET.remove();else TARGET.set(previous);}
    }
    private static Mob validated(ServerPlayer p,int id){
        if(id<0||!p.isAlive()||p.isSpectator()||p.isSleeping()||p.isPassenger())return null;
        var entity=p.level().getEntity(id);
        if(!(entity instanceof Mob mob)||!mob.isAlive()||mob.isRemoved()||mob.isInvisible()||p.distanceToSqr(mob)>Math.pow(TargetingRange.of(p),2)
            ||mob instanceof TamableAnimal pet&&pet.isTame()||RpgNecromancer.isSummon(mob)||!p.hasLineOfSight(mob))return null;
        var point=mob.position().add(0,mob.getBbHeight()*.6,0);var delta=point.subtract(p.getEyePosition());
        if(delta.lengthSqr()<1e-8)return null;
        var hit=p.level().clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS?mob:null;
    }
    public static Mob target(ServerPlayer p){
        var locked=TARGET.get();if(locked!=null)return locked;
        var start=p.getEyePosition();var end=start.add(direction(p).scale(TargetingRange.of(p)));
        end=p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();final var rayEnd=end;
        return p.level().getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(start,end).inflate(.3),m->RpgAbilities.attackable(m)&&!(m instanceof TamableAnimal pet&&pet.isTame()))
            .stream().filter(m->m.getBoundingBox().inflate(.2).contains(start)||m.getBoundingBox().inflate(.2).clip(start,rayEnd).isPresent()).min(java.util.Comparator.comparingDouble(m->m.getBoundingBox().inflate(.2).clip(start,rayEnd).map(start::distanceToSqr).orElse(0.0))).orElse(null);
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
