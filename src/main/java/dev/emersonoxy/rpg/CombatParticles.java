package dev.emersonoxy.rpg;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Post-damage cosmetics: misses, invulnerability and fully blocked hits never bleed. */
public final class CombatParticles {
    private record Budget(long tick,int count){}
    private static final Map<ServerLevel,Budget> BUDGET=new WeakHashMap<>();
    private CombatParticles(){}
    private static boolean reserve(ServerLevel level){
        long now=level.getGameTime();var old=BUDGET.get(level);
        int count=old!=null&&old.tick==now?old.count:0;
        if(count>=64)return false;BUDGET.put(level,new Budget(now,count+1));return true;
    }
    public static void damage(LivingDamageEvent.Post event){
        var target=event.getEntity();var source=event.getSource();float damage=event.getNewDamage();
        if(!(target.level() instanceof ServerLevel level)||!(target instanceof Player||target instanceof Mob)||!Float.isFinite(damage)||damage<=0)return;
        if(source.getEntity()==null&&!source.is(DamageTypeTags.IS_EXPLOSION)&&!source.is(DamageTypeTags.IS_FALL))return;
        Vec3 at=target.position().add(0,target.getBbHeight()*.55,0);
        var origin=source.getSourcePosition();Vec3 direction=origin==null?Vec3.ZERO:at.subtract(origin).normalize();
        PacketDistributor.sendToPlayersNear(level,null,at.x,at.y,at.z,48,
            new RpgNetwork.BloodFx(at.x,at.y,at.z,(float)direction.x,(float)direction.y,(float)direction.z,damage));

    }
    public static void explosion(ExplosionEvent.Detonate event){
        if(event.getLevel() instanceof ServerLevel level&&reserve(level))
            SkillEffects.impact(level,event.getExplosion().center(),event.getExplosion().radius(),1);
    }
    public static void stop(ServerStoppedEvent event){BUDGET.clear();}
}
