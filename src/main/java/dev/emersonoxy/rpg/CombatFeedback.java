package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Feedback comes from completed server damage, never from a client damage estimate. */
public final class CombatFeedback {
    private record Critical(UUID target,long tick,boolean critical){}
    private static final Map<UUID,Critical> CRITICAL=new HashMap<>();
    private CombatFeedback(){}
    public static void critical(CriticalHitEvent event){if(event.getEntity() instanceof ServerPlayer p)CRITICAL.put(p.getUUID(),new Critical(event.getTarget().getUUID(),p.level().getGameTime(),event.isCriticalHit()));}
    public static void damage(LivingDamageEvent.Post event){
        var target=event.getEntity();var source=event.getSource();var p=RpgNecromancer.owner(source);
        if(target instanceof ServerPlayer victim&&event.getBlockedDamage()>0&&RpgAbilities.profile(victim).rpgClass()==RpgClass.GUERREIRO)ClassJourney.advance(victim,2,1);
        if(p==null||target==p||RpgNecromancer.isSummon(target))return;
        boolean blocked=event.getNewDamage()<=0&&event.getBlockedDamage()>0;
        if(!blocked&&event.getNewDamage()<=0)return;
        var mark=CRITICAL.get(p.getUUID());boolean crit=false;
        if(source.getDirectEntity()==p&&mark!=null&&mark.tick()==p.level().getGameTime()&&mark.target().equals(target.getUUID())){crit=mark.critical();CRITICAL.remove(p.getUUID());}
        if(source.getDirectEntity() instanceof AbstractArrow arrow)crit=arrow.isCritArrow();
        PacketDistributor.sendToPlayer(p,new RpgNetwork.DamageNumber(target.getId(),target.getX(),target.getY()+target.getBbHeight()*.75,target.getZ(),event.getNewDamage(),crit,blocked));
        if(!blocked)ClassJourney.damage(p,event);
    }
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){CRITICAL.remove(event.getEntity().getUUID());}
    public static void stop(ServerStoppedEvent event){CRITICAL.clear();}
}
