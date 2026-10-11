package dev.emersonoxy.rpg;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.living.*;

/** One persistent secondary identity per elite, plus a low-health combat phase. */
public final class EliteAffixes {
    private static final String[] NAMES={"Erupção","Carcereiro","Vampírico","Baluarte","Tempestade","Predador"};
    private EliteAffixes(){}
    public static void initialize(Mob mob){if(!mob.getPersistentData().contains("oxy_affix"))mob.getPersistentData().putInt("oxy_affix",mob.getRandom().nextInt(NAMES.length));}
    public static String title(Mob mob){initialize(mob);return NAMES[Math.clamp(mob.getPersistentData().getIntOr("oxy_affix",0),0,NAMES.length-1)];}
    public static void tick(ServerLevel world,Mob mob,LivingEntity target){
        initialize(mob);var data=mob.getPersistentData();long now=world.getGameTime();
        boolean enraged=mob.getHealth()<mob.getMaxHealth()*.4f&&target!=null&&target.isAlive();
        MobLevels.modifier(mob,net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,"elite_enrage",enraged?.25:0);
        if(target==null||!target.isAlive()||target instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator())){data.putLong("oxy_affix_cast",0);return;}
        int kind=data.getIntOr("oxy_affix",0);if(kind==2)return;
        if(mob.distanceToSqr(target)>400||!mob.hasLineOfSight(target)){data.putLong("oxy_affix_cast",0);return;}
        long cast=data.getLongOr("oxy_affix_cast",0);
        if(cast>0){if(now>=cast){data.putLong("oxy_affix_cast",0);data.putLong("oxy_elite_recover_until",now+16);apply(world,mob,target,kind,new Vec3(data.getDoubleOr("oxy_affix_x",mob.getX()),data.getDoubleOr("oxy_affix_y",mob.getY()),data.getDoubleOr("oxy_affix_z",mob.getZ())));}return;}
        if(now<data.getLongOr("oxy_affix_ready",0)||now<data.getLongOr("oxy_elite_cast_until",0)||now<data.getLongOr("oxy_elite_recover_until",0))return;
        Vec3 point=kind==3?mob.position():target.position();int windup=kind==3?20:32;
        data.putDouble("oxy_affix_x",point.x);data.putDouble("oxy_affix_y",point.y);data.putDouble("oxy_affix_z",point.z);
        mob.getNavigation().stop();data.putLong("oxy_affix_cast",now+windup);data.putLong("oxy_affix_ready",now+(enraged?140:220));
        PacketDistributor.sendToPlayersTrackingEntity(mob,new RpgNetwork.EliteWarning(mob.getId(),point.x,point.y,point.z,kind==3?2:3.5f,windup,kind));
    }
    private static void apply(ServerLevel world,Mob mob,LivingEntity target,int kind,Vec3 center){
        if(kind==3){mob.getPersistentData().putLong("oxy_ward_until",world.getGameTime()+70);return;}
        if(kind==5){var delta=center.subtract(mob.position()).multiply(1,0,1).normalize();mob.setDeltaMovement(delta.scale(1.2).add(0,.3,0));mob.hasImpulse=true;}
        if(kind==4){var bolt=EntityType.LIGHTNING_BOLT.create(world,EntitySpawnReason.TRIGGERED);if(bolt!=null){bolt.setPos(center);bolt.setVisualOnly(true);world.addFreshEntity(bolt);}}
        for(var victim:world.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(3.5),e->e.isAlive()&&(e==target||e instanceof net.minecraft.world.entity.player.Player||RpgNecromancer.isSummon(e)))){
            if(victim instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator())||victim.distanceToSqr(center)>12.25||!mob.hasLineOfSight(victim))continue;
            float base=kind==1?1.5f:kind==4?3.5f:3;
            float damage=(float)(base*MobLevels.damageFactor(MobLevels.level(mob))*(1.5+mob.getPersistentData().getDoubleOr("oxy_elite_scale",2)*.5));
            victim.hurtServer(world,mob.damageSources().mobAttack(mob),damage);
            if(kind==1){victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,2));victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0));}
            if(kind==0){victim.igniteForSeconds(3);victim.push(0,.45,0);victim.hurtMarked=true;}
        }
    }
    public static void damage(LivingDamageEvent.Pre event){
        if(event.getEntity() instanceof Mob victim&&EliteMobs.elite(victim)&&victim.getPersistentData().getLongOr("oxy_ward_until",0)>victim.level().getGameTime())event.setNewDamage(event.getNewDamage()*.6f);
        if(event.getSource().getEntity() instanceof Mob attacker&&EliteMobs.elite(attacker)&&attacker.getHealth()<attacker.getMaxHealth()*.4f)event.setNewDamage(event.getNewDamage()*1.25f);
    }
    public static void drain(LivingDamageEvent.Post event){
        if(event.getNewDamage()<=0||!(event.getSource().getEntity() instanceof Mob mob)||!EliteMobs.elite(mob)||mob.getPersistentData().getIntOr("oxy_affix",-1)!=2)return;
        long now=mob.level().getGameTime();if(now<mob.getPersistentData().getLongOr("oxy_drain_ready",0))return;
        mob.heal(Math.min(mob.getMaxHealth()*.02f,event.getNewDamage()*.3f));mob.getPersistentData().putLong("oxy_drain_ready",now+20);
    }
}
