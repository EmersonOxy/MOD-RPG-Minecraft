package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** One server roll per direct hit, before armor, shield durability or health are spent. */
public final class CombatChances {
    private record Hit(UUID target,long tick,boolean critical){}
    private static final Map<DamageSource,Hit> HITS=new IdentityHashMap<>();
    private CombatChances(){}
    public static void incoming(LivingIncomingDamageEvent event){
        if(event.isCanceled()||event.getAmount()<=0||RpgAbilities.secondaryHit())return;
        var source=event.getSource();
        if(event.getEntity() instanceof ServerPlayer victim&&source.getEntity() instanceof net.minecraft.world.entity.Mob mob&&!RpgNecromancer.isSummon(mob)&&!RpgSpecialists.temporary(mob)&&(source.is(DamageTypes.MOB_ATTACK)||source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)||source.is(DamageTypeTags.IS_PROJECTILE))){
            long now=victim.level().getGameTime();HITS.values().removeIf(h->now-h.tick()>2);
            if(mob.getRandom().nextFloat()<RpgConfig.value(RpgConfig.ENEMY_MISS,.05)){event.setCanceled(true);CombatFeedback.text(victim,mob,"Errou",0x75BFFF);return;}
            boolean critical=mob.getRandom().nextFloat()<RpgConfig.value(RpgConfig.ENEMY_CRITICAL,.10);
            if(critical)event.setAmount(event.getAmount()*(float)RpgConfig.value(RpgConfig.ENEMY_CRITICAL_DAMAGE,1.5));
            HITS.put(source,new Hit(victim.getUUID(),now,critical));return;
        }
        if(!(source.getEntity() instanceof ServerPlayer p)||event.getEntity()==p)return;
        if(!source.is(DamageTypes.PLAYER_ATTACK)&&!source.is(DamageTypeTags.IS_PROJECTILE)&&!source.is(DamageTypes.MAGIC)&&!source.is(DamageTypes.INDIRECT_MAGIC)&&!source.is(DamageTypes.LIGHTNING_BOLT))return;
        if(RpgNecromancer.isSummon(event.getEntity()))return;
        long now=p.level().getGameTime();HITS.values().removeIf(h->now-h.tick()>2);
        int luck=RpgDebug.attributes(p,RpgAbilities.profile(p).attributes()).luck();
        boolean counter=source.is(DamageTypes.PLAYER_ATTACK)&&source.getDirectEntity()==p&&ShieldParry.counter(p);
        if(!counter&&p.getRandom().nextFloat()<RpgConfig.miss(luck)){
            event.setCanceled(true);CombatFeedback.text(p,event.getEntity(),"Errou",0x75BFFF);return;
        }
        boolean vanilla=CombatFeedback.vanillaCritical(p,event.getEntity(),source);
        boolean critical=counter||vanilla||p.getRandom().nextFloat()<Math.min(1,RpgConfig.critical(luck)+RpgDataMaps.weapon(p.getMainHandItem()).criticalBonus());
        if(critical&&!vanilla)event.setAmount(event.getAmount()*(float)RpgConfig.value(RpgConfig.CRITICAL_DAMAGE,1.5));
        HITS.put(source,new Hit(event.getEntity().getUUID(),now,critical));
    }
    public static boolean take(DamageSource source,LivingEntity target){var value=HITS.remove(source);return value!=null&&value.target().equals(target.getUUID())&&value.tick()==target.level().getGameTime()&&value.critical();}
    public static void stop(ServerStoppedEvent event){HITS.clear();}
}
