package dev.emersonoxy.rpg;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.living.*;

public final class EliteMobs {
    private static final String KEY="oxy_elite";
    private EliteMobs(){}
    public static boolean elite(LivingEntity mob){return mob.getPersistentData().getBooleanOr(KEY,false);}
    public static void roll(Mob mob){
        if(mob.getPersistentData().getBooleanOr("oxy_elite_child",false)||mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss||mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon||mob.getRandom().nextFloat()>=.05f)return;
        double scale=2+mob.getRandom().nextFloat()*2;
        promote(mob,scale);
    }
    public static void promote(Mob mob,double scale){
        if(elite(mob)){slow(mob);return;}
        scale=Math.clamp(scale,2,4);
        mob.getPersistentData().putBoolean(KEY,true);mob.getPersistentData().putDouble("oxy_elite_scale",scale);
        add(mob,Attributes.SCALE,"elite_scale",scale-1);
        add(mob,Attributes.MAX_HEALTH,"elite_health",4+scale*1.5); // ×8 to ×11
        add(mob,Attributes.ATTACK_DAMAGE,"elite_damage",.5+scale*.5); // ×2.5 to ×3.5
        slow(mob);
        mob.setHealth(mob.getMaxHealth());
    }
    private static void add(Mob mob,net.minecraft.core.Holder<Attribute> key,String name,double amount){var value=mob.getAttribute(key);if(value!=null)value.addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("oxyrpg",name),amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
    public static void slow(Mob mob){
        if(!elite(mob))return;double scale=Math.clamp(mob.getPersistentData().getDoubleOr("oxy_elite_scale",2),2,4);
        float ratio=mob.getHealth()/Math.max(1,mob.getMaxHealth());
        MobLevels.modifier(mob,Attributes.MAX_HEALTH,"elite_health",4+scale*1.5);
        MobLevels.modifier(mob,Attributes.ATTACK_DAMAGE,"elite_damage",.5+scale*.5);
        mob.setHealth(Math.min(mob.getMaxHealth(),ratio*mob.getMaxHealth()));
        double amount=-(.08+(scale-2)*.06);
        for(var attribute:java.util.List.of(Attributes.MOVEMENT_SPEED,Attributes.FLYING_SPEED)){
            var value=mob.getAttribute(attribute);if(value==null)continue;
            var id=ResourceLocation.fromNamespaceAndPath("oxyrpg","elite_speed");var old=value.getModifier(id);
            if(old!=null&&old.amount()==amount)continue;if(old!=null)value.removeModifier(id);
            value.addPermanentModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        EliteAbilities.initialize(mob);
        if(mob instanceof net.minecraft.world.entity.monster.Creeper&&mob instanceof dev.emersonoxy.rpg.mixin.CreeperRadiusAccessor accessor){
            var data=mob.getPersistentData();if(!data.contains("oxy_elite_original_blast"))data.putInt("oxy_elite_original_blast",accessor.oxyrpg$getExplosionRadius());
            accessor.oxyrpg$setExplosionRadius((int)Math.round(data.getIntOr("oxy_elite_original_blast",3)*scale));
        }
    }
    public static int xp(LivingEntity mob,int base){return elite(mob)?base*4:mob.getPersistentData().getBooleanOr("oxy_elite_child",false)?Math.max(1,base/2):base;}
    public static void damage(LivingDamageEvent.Pre event){
        if(event.getSource().getEntity() instanceof LivingEntity mob&&elite(mob)
            &&(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)||event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)))
            event.setNewDamage(event.getNewDamage()*(float)(1.5+mob.getPersistentData().getDoubleOr("oxy_elite_scale",2)*.5));
    }
    public static void experience(LivingExperienceDropEvent event){if(elite(event.getEntity()))event.setDroppedExperience(event.getDroppedExperience()*3);}
    public static void reward(net.minecraft.server.level.ServerPlayer p,LivingEntity mob){
        if(!elite(mob)||!p.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))return;
        var stack=new ItemStack(Items.EMERALD,2+mob.getRandom().nextInt(4));drop(mob,stack);
        drop(mob,new ItemStack(mob.getRandom().nextFloat()<.25f?Items.DIAMOND:Items.IRON_INGOT,mob.getRandom().nextInt(3)+1));
    }
    private static void drop(LivingEntity mob,ItemStack stack){mob.level().addFreshEntity(new ItemEntity(mob.level(),mob.getX(),mob.getY()+.3,mob.getZ(),stack));}
}
