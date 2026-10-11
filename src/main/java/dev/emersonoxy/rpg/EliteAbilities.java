package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Short telegraphs, line-of-sight attacks and bounded native enemy reinforcements. */
public final class EliteAbilities {
    private EliteAbilities(){}
    public static void initialize(Mob mob){
        EliteAffixes.initialize(mob);var data=mob.getPersistentData();if(data.contains("oxy_elite_trait"))return;
        String id=BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
        String trait=switch(id){
            case "creeper"->"demolisher";
            case "zombie","husk","drowned","zombie_villager"->"crusher";
            case "skeleton","stray","bogged"->"frost_archer";
            case "wither_skeleton"->"reaper";
            case "spider","cave_spider"->"venom";
            case "enderman"->"distorter";
            case "witch"->"alchemist";
            case "blaze","magma_cube"->"inferno";
            case "slime"->"unstable";
            case "phantom"->"terror";
            case "pillager"->"marksman";
            case "evoker"->"summoner";
            case "vindicator","piglin","piglin_brute","hoglin","zoglin","zombified_piglin"->"berserker";
            case "guardian","elder_guardian"->"tide";
            case "breeze"->"gale";
            case "warden"->"sentinel";
            default->List.of("sentinel","storm","hunter").get(mob.getRandom().nextInt(3));
        };
        data.putString("oxy_elite_trait",trait);
    }
    public static String title(Mob mob){return primaryTitle(mob)+" • "+EliteAffixes.title(mob);}
    private static String primaryTitle(Mob mob){return switch(mob.getPersistentData().getStringOr("oxy_elite_trait","")){
        case "demolisher"->"Demolidor";case "crusher"->"Esmagador";case "frost_archer"->"Atirador Glacial";case "reaper"->"Ceifador";
        case "venom"->"Caçador Venenoso";case "distorter"->"Distorcedor";case "alchemist"->"Alquimista";case "inferno"->"Incendiário";
        case "unstable"->"Instável";case "terror"->"Grito Sombrio";case "marksman"->"Balestrista";case "summoner"->"Invocador";
        case "berserker"->"Berserker";case "tide"->"Maré Sombria";case "gale"->"Vendaval";case "storm"->"Tempestade";case "hunter"->"Predador";default->"Sentinela";
    };}
    private static boolean target(LivingEntity e){return e!=null&&e.isAlive()&&(!(e instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob)||!(mob.level() instanceof ServerLevel))return;
        if(!mob.getPersistentData().getBooleanOr("oxy_elite_child",false)
            &&(!EliteMobs.elite(mob)||!mob.isAlive()||RpgNecromancer.isSummon(mob)||mob.tickCount%4!=0))return;
        RpgProfiler.run("elites",()->tickBody(event));
    }
    private static void tickBody(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob)||!(mob.level() instanceof ServerLevel world))return;var data=mob.getPersistentData();long now=world.getGameTime();
        if(data.getBooleanOr("oxy_elite_child",false)){
            if(now>=data.getLongOr("oxy_elite_child_expiry",0))mob.discard();return;
        }
        if(!EliteMobs.elite(mob)||!mob.isAlive()||RpgNecromancer.isSummon(mob)||mob.tickCount%4!=0)return;
        initialize(mob);EliteAffixes.tick(world,mob,mob.getTarget());String trait=data.getStringOr("oxy_elite_trait","");
        if(trait.equals("demolisher"))return; // Creepers retain their real vanilla explosion, scaled by size.
        var target=mob.getTarget();if(!target(target)||mob.distanceToSqr(target)>144||!mob.hasLineOfSight(target)){data.putLong("oxy_elite_cast_until",0);return;}
        long cast=data.getLongOr("oxy_elite_cast_until",0);
        if(cast>0){
            if(now<cast){ring(world,mob,3,ParticleTypes.ENCHANT);return;}
            data.putLong("oxy_elite_cast_until",0);cast(world,mob,target,trait);data.putLong("oxy_elite_recover_until",now+16);return;
        }
        if(now<data.getLongOr("oxy_elite_ready",0)||now<data.getLongOr("oxy_elite_recover_until",0)||now<data.getLongOr("oxy_affix_cast",0))return;
        if(!Set.of("frost_archer","marksman","summoner","storm","sentinel","berserker").contains(trait)&&mob.distanceToSqr(target)>64)return;
        data.putLong("oxy_elite_cast_until",now+20);data.putLong("oxy_elite_ready",now+(trait.equals("summoner")?220:trait.equals("sentinel")?200:130));
        mob.getNavigation().stop();
        double radius=switch(trait){case "distorter","terror"->6;case "alchemist"->5;default->3+data.getDoubleOr("oxy_elite_scale",2)*.5;};
        int kind=Set.of("frost_archer","marksman").contains(trait)?12:Set.of("berserker","sentinel","summoner").contains(trait)?13:10;
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(mob,new RpgNetwork.EliteWarning(mob.getId(),mob.getX(),mob.getY(),mob.getZ(),(float)Math.clamp(radius,1,16),20,kind));
        world.sendParticles(ParticleTypes.ENCHANT,mob.getX(),mob.getY()+mob.getBbHeight(),mob.getZ(),6,.4,.4,.4,.04);
    }
    private static List<LivingEntity> near(ServerLevel w,Mob mob,LivingEntity target,double radius){
        return w.getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(radius),e->target(e)&&(e==target||e instanceof Player||RpgNecromancer.isSummon(e))&&mob.distanceToSqr(e)<=radius*radius&&mob.hasLineOfSight(e));
    }
    private static void hit(ServerLevel world,Mob mob,LivingEntity target,float base){
        hit(world,mob,target,base,mob.damageSources().mobAttack(mob));
    }
    private static void hit(ServerLevel world,Mob mob,LivingEntity target,float base,net.minecraft.world.damagesource.DamageSource source){
        float scale=(float)mob.getPersistentData().getDoubleOr("oxy_elite_scale",2);
        float damage=base*(float)MobLevels.damageFactor(MobLevels.level(mob))*(1.5f+scale*.5f);
        target.hurtServer(world,source,damage);
    }
    private static void impulse(Mob mob,LivingEntity e,double force,boolean pull){
        Vec3 direction=e.position().subtract(mob.position()).multiply(1,0,1).normalize();if(pull)direction=direction.scale(-1);
        e.push(direction.x*force,.2,direction.z*force);e.hurtMarked=true;
    }
    private static void cast(ServerLevel world,Mob mob,LivingEntity target,String trait){
        double radius=3+mob.getPersistentData().getDoubleOr("oxy_elite_scale",2)*.5;
        switch(trait){
            case "crusher"->{for(var e:near(world,mob,target,radius)){hit(world,mob,e,4);e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0));impulse(mob,e,1,false);}ring(world,mob,radius,ParticleTypes.CRIT);}
            case "frost_archer"->volley(world,mob,target,true);
            case "marksman"->volley(world,mob,target,false);
            case "venom"->{jump(mob,target);for(var e:near(world,mob,target,3)){hit(world,mob,e,2);e.addEffect(new MobEffectInstance(MobEffects.POISON,80,0));e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,0));}ring(world,mob,3,ParticleTypes.SNEEZE);}
            case "distorter"->{for(var e:near(world,mob,target,6)){e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,60,0));impulse(mob,e,.7,true);}ring(world,mob,6,ParticleTypes.PORTAL);}
            case "alchemist"->{for(var e:near(world,mob,target,5)){e.addEffect(new MobEffectInstance(MobEffects.POISON,80,0));e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));}ring(world,mob,5,ParticleTypes.WITCH);}
            case "inferno"->{for(var e:near(world,mob,target,radius)){hit(world,mob,e,3,new net.minecraft.world.damagesource.DamageSource(mob.damageSources().inFire().typeHolder(),mob,mob));e.igniteForSeconds(2);}ring(world,mob,radius,ParticleTypes.FLAME);}
            case "unstable"->{mob.setDeltaMovement(mob.getDeltaMovement().add(0,.6,0));for(var e:near(world,mob,target,radius)){hit(world,mob,e,3);impulse(mob,e,.9,false);e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0));}ring(world,mob,radius,ParticleTypes.ITEM_SLIME);}
            case "terror"->{for(var e:near(world,mob,target,6)){e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,80,0));e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0));}ring(world,mob,6,ParticleTypes.SMOKE);}
            case "reaper"->{for(var e:near(world,mob,target,radius)){hit(world,mob,e,3);e.addEffect(new MobEffectInstance(MobEffects.WITHER,60,0));}ring(world,mob,radius,ParticleTypes.SOUL);}
            case "berserker"->{mob.addEffect(new MobEffectInstance(MobEffects.STRENGTH,100,0));mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,100,0));ring(world,mob,2,ParticleTypes.CRIT);}
            case "tide"->{for(var e:near(world,mob,target,6)){e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100,1));impulse(mob,e,.6,true);}ring(world,mob,6,ParticleTypes.BUBBLE);}
            case "gale"->{for(var e:near(world,mob,target,6)){hit(world,mob,e,2);impulse(mob,e,1.3,false);}ring(world,mob,6,ParticleTypes.CLOUD);}
            case "summoner"->reinforce(world,mob,target);
            case "storm"->{var bolt=EntityType.LIGHTNING_BOLT.create(world,EntitySpawnReason.TRIGGERED);if(bolt!=null){bolt.setPos(target.position());bolt.setVisualOnly(true);world.addFreshEntity(bolt);}hit(world,mob,target,4,new net.minecraft.world.damagesource.DamageSource(mob.damageSources().lightningBolt().typeHolder(),mob,mob));}
            case "hunter"->{jump(mob,target);if(mob.distanceToSqr(target)<16){hit(world,mob,target,3);target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0));}ring(world,mob,2,ParticleTypes.CRIT);}
            default->{mob.heal(mob.getMaxHealth()*.05f);mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,100,0));ring(world,mob,2,ParticleTypes.HEART);}
        }
    }
    private static void jump(Mob mob,LivingEntity target){var dir=target.position().subtract(mob.position()).multiply(1,0,1).normalize();mob.setDeltaMovement(dir.scale(.45).add(0,.5,0));mob.hasImpulse=true;}
    private static void ring(ServerLevel world,Mob mob,double radius,SimpleParticleType type){for(int i=0;i<20;i++){double a=i*Math.PI/10;world.sendParticles(type,mob.getX()+Math.cos(a)*radius,mob.getY()+.2,mob.getZ()+Math.sin(a)*radius,1,.02,.02,.02,.01);}}
    private static void volley(ServerLevel world,Mob mob,LivingEntity target,boolean frost){
        for(int i=-1;i<=1;i++){
            var weapon=new ItemStack(Items.CROSSBOW);
            if(!frost)weapon.enchant(world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PIERCING),1);
            AbstractArrow arrow=frost?new SpectralArrow(world,mob,new ItemStack(Items.SPECTRAL_ARROW),null):new Arrow(world,mob,new ItemStack(Items.ARROW),weapon);
            arrow.pickup=AbstractArrow.Pickup.DISALLOWED;arrow.setBaseDamage(2);
            arrow.getPersistentData().putBoolean("oxy_elite_frost",frost);arrow.getPersistentData().putLong("oxy_elite_shot_expiry",world.getGameTime()+120);
            var delta=target.getEyePosition().subtract(arrow.position()).yRot((float)Math.toRadians(i*7));arrow.shoot(delta.x,delta.y,delta.z,2,.5f);world.addFreshEntity(arrow);
        }
    }
    private static void reinforce(ServerLevel world,Mob mob,LivingEntity target){
        String owner=mob.getUUID().toString();long alive=world.getEntitiesOfClass(Mob.class,mob.getBoundingBox().inflate(24),e->e.isAlive()&&e.getPersistentData().getStringOr("oxy_elite_parent","").equals(owner)).size();
        for(int i=(int)alive;i<2;i++){
            var child=EntityType.SILVERFISH.create(world,EntitySpawnReason.MOB_SUMMONED);if(child==null)continue;
            child.getPersistentData().putBoolean("oxy_elite_child",true);child.getPersistentData().putString("oxy_elite_parent",owner);child.getPersistentData().putLong("oxy_elite_child_expiry",world.getGameTime()+240);
            child.setPos(mob.getX()+(i==0?1:-1),mob.getY(),mob.getZ()+1);if(!world.noCollision(child))continue;child.setTarget(target);world.addFreshEntity(child);
        }
    }
    public static void arrowEffect(LivingDamageEvent.Post event){if(event.getNewDamage()>0&&event.getSource().getDirectEntity() instanceof AbstractArrow arrow&&arrow.getPersistentData().getBooleanOr("oxy_elite_frost",false))event.getEntity().addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0));}
    public static void projectile(EntityTickEvent.Post event){var entity=event.getEntity();if(entity instanceof Projectile&&entity.level() instanceof ServerLevel world){long expiry=entity.getPersistentData().getLongOr("oxy_elite_shot_expiry",0);if(expiry>0&&world.getGameTime()>=expiry)entity.discard();}}
    public static void force(ServerPlayer p,float size){
        Mob found=null;double distance=64;
        for(var mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(8),m->m instanceof Enemy&&!RpgNecromancer.isSummon(m)&&!m.getPersistentData().getBooleanOr("oxy_elite_child",false)&&!(m instanceof net.minecraft.world.entity.boss.wither.WitherBoss)&&!(m instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon))){
            double d=p.distanceToSqr(mob);if(d<distance&&p.hasLineOfSight(mob)&&mob.getBoundingBox().getCenter().subtract(p.getEyePosition()).normalize().dot(p.getLookAngle())>.85){distance=d;found=mob;}
        }
        if(found==null){p.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Oxy RPG] Mire num hostil a até 8 blocos."));return;}
        EliteMobs.promote(found,size);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(found,new RpgNetwork.MobLevel(found.getId(),MobLevels.level(found)+1000));
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(found,new RpgNetwork.EliteInfo(found.getId(),title(found)));
        p.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Oxy RPG] Elite: "+title(found)+". Elites existentes preservam sua escala."));
    }
}
