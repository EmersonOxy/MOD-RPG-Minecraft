package dev.emersonoxy.rpg;

import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.registries.*;

/** Persistent wounds; particles use the existing blood provider and surface marks. */
public final class Bleeding {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,OxyRpg.MOD_ID);
    public static final DeferredHolder<MobEffect,Wound> EFFECT=EFFECTS.register("bleeding",Wound::new);
    public static final ResourceKey<DamageType> DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(OxyRpg.MOD_ID,"bleeding"));
    private static final String OWNER="oxy_bleeding_owner";
    private Bleeding(){}
    public static void register(IEventBus bus){EFFECTS.register(bus);}
    public static boolean damage(DamageSource source){return source.is(DAMAGE);}
    public static void apply(LivingEntity target,LivingEntity attacker,int severity){
        if(!(target.level() instanceof ServerLevel)||!target.isAlive()||target instanceof Player p&&(p.isCreative()||p.isSpectator()))return;
        var old=target.getEffect(EFFECT);int rank=Math.max(0,Math.min(2,severity));
        if(old!=null&&old.getAmplifier()>rank)return;
        if(target.addEffect(new MobEffectInstance(EFFECT,MobEffectInstance.INFINITE_DURATION,rank,false,false,true),attacker))
            target.getPersistentData().putString(OWNER,attacker.getUUID().toString());
    }
    public static void stop(LivingEntity entity){entity.removeEffect(EFFECT);entity.getPersistentData().remove(OWNER);}
    public static void hit(LivingDamageEvent.Post event){
        if(event.getNewDamage()<=0||damage(event.getSource())||!(event.getSource().getDirectEntity() instanceof LivingEntity attacker)||event.getSource().getEntity()!=attacker)return;
        var source=event.getSource();if(!source.is(DamageTypes.PLAYER_ATTACK)&&!source.is(DamageTypes.MOB_ATTACK)&&!source.is(DamageTypes.MOB_ATTACK_NO_AGGRO))return;
        if(RpgAbilities.secondaryHit())return;
        var weapon=RpgDataMaps.weapon(attacker.getMainHandItem());
        if(weapon.bleedChance()>0&&attacker.getRandom().nextFloat()<weapon.bleedChance())apply(event.getEntity(),attacker,weapon.bleedSeverity());
    }
    public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer)||!(event.getTarget() instanceof LivingEntity target)||!target.hasEffect(EFFECT))return;
        var stack=event.getEntity().getItemInHand(event.getHand());if(!stack.is(RpgItems.BANDAGE.get()))return;
        var result=RpgItems.BANDAGE.get().interactLivingEntity(stack,event.getEntity(),target,event.getHand());
        event.setCanceled(true);event.setCancellationResult(result);
    }
    // Milk still cures ordinary effects; wounds require a bandage. Commands can clear them for administration.
    public static void removed(MobEffectEvent.Remove event){
        if(event.getEffect().is(EFFECT.getKey())&&event.getEntity().getUseItem().is(Items.MILK_BUCKET))event.setCanceled(true);
    }
    public static final class Wound extends MobEffect {
        private Wound(){super(MobEffectCategory.HARMFUL,0xA61624);}
        @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
        @Override public boolean applyEffectTick(ServerLevel level,LivingEntity entity,int amplifier){
            if(!entity.isAlive()||entity instanceof Player p&&(p.isCreative()||p.isSpectator()))return true;
            int rank=Math.max(0,Math.min(2,amplifier));
            if(entity.tickCount%4==0){
                double spread=Math.max(.08,Math.min(.3,entity.getBbWidth()*.3));
                for(int i=0;i<=rank;i++){
                    double x=entity.getX()+(level.random.nextDouble()-.5)*spread*2,z=entity.getZ()+(level.random.nextDouble()-.5)*spread*2;
                    // Count zero preserves a downward velocity; drops collide and become native blood marks.
                    level.sendParticles(RpgParticles.BLOOD.get(),x,entity.getY()+.16,z,0,0,-.07,0,1);
                }
            }
            if(entity.tickCount%20==0){
                net.minecraft.world.entity.Entity owner=null;
                try{UUID id=UUID.fromString(entity.getPersistentData().getStringOr(OWNER,""));owner=level.getEntity(id);if(owner==null)owner=level.getServer().getPlayerList().getPlayer(id);}catch(IllegalArgumentException ignored){}
                entity.hurtServer(level,new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE),null,owner),.5f*(rank+1));
            }
            return true;
        }
    }
}
