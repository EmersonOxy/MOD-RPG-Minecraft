package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class ShieldParry {
    private static final Map<UUID,Long> USED=new HashMap<>(),COUNTER=new HashMap<>();
    private ShieldParry(){}
    public static void block(LivingShieldBlockEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!event.getBlocked()||event.getBlockedDamage()<=0)return;
        var shield=p.getUseItem();if(!(shield.getItem() instanceof ShieldItem)&&!shield.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_SHIELD))return;
        int ticks=p.getTicksUsingItem();long now=p.level().getGameTime();
        // The normal shield raise delay remains; a precise block has a short window afterwards.
        if(ticks<5||ticks>=5+RpgConfig.value(RpgConfig.PARRY_WINDOW,6)||now-USED.getOrDefault(p.getUUID(),-100L)<20)return;
        USED.put(p.getUUID(),now);COUNTER.put(p.getUUID(),now+30);
        event.setShieldDamage(0);event.setBlockedDamage(event.getDamageContainer().getNewDamage());
        if(event.getDamageSource().getEntity() instanceof LivingEntity attacker&&attacker!=p){
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,20,3));
            attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,20,1));
            attacker.knockback(.9,p.getX()-attacker.getX(),p.getZ()-attacker.getZ());
            CombatFeedback.text(p,attacker,"Parry",0xE9C77C);
        }else CombatFeedback.text(p,p,"Parry",0xE9C77C);
        ((net.minecraft.server.level.ServerLevel)p.level()).playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.SHIELD_BLOCK.value(),net.minecraft.sounds.SoundSource.PLAYERS,.8f,1.5f);
    }
    public static boolean counter(ServerPlayer p){return COUNTER.getOrDefault(p.getUUID(),0L)>p.level().getGameTime();}
    public static void consume(ServerPlayer p){COUNTER.remove(p.getUUID());}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){USED.remove(event.getEntity().getUUID());COUNTER.remove(event.getEntity().getUUID());}
    public static void stop(ServerStoppedEvent event){USED.clear();COUNTER.clear();}
}
