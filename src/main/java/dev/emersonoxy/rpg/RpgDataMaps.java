package dev.emersonoxy.rpg;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.datamaps.*;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class RpgDataMaps {
    public record MobTuning(float health,float damage,float xp,float fireResistance,float frostResistance,float lightningResistance){
        public static final MobTuning DEFAULT=new MobTuning(1,1,1,0,0,0);
        public static final Codec<MobTuning> CODEC=RecordCodecBuilder.create(i->i.group(
            Codec.floatRange(.01f,20).optionalFieldOf("health",1f).forGetter(MobTuning::health),Codec.floatRange(.01f,20).optionalFieldOf("damage",1f).forGetter(MobTuning::damage),Codec.floatRange(0,20).optionalFieldOf("xp",1f).forGetter(MobTuning::xp),
            Codec.floatRange(0,1).optionalFieldOf("fire_resistance",0f).forGetter(MobTuning::fireResistance),Codec.floatRange(0,1).optionalFieldOf("frost_resistance",0f).forGetter(MobTuning::frostResistance),Codec.floatRange(0,1).optionalFieldOf("lightning_resistance",0f).forGetter(MobTuning::lightningResistance)).apply(i,MobTuning::new));
    }
    public record WeaponTuning(float damage,float criticalBonus){
        public static final WeaponTuning DEFAULT=new WeaponTuning(1,0);
        public static final Codec<WeaponTuning> CODEC=RecordCodecBuilder.create(i->i.group(Codec.floatRange(.01f,20).optionalFieldOf("damage",1f).forGetter(WeaponTuning::damage),Codec.floatRange(0,1).optionalFieldOf("critical_bonus",0f).forGetter(WeaponTuning::criticalBonus)).apply(i,WeaponTuning::new));
    }
    public static final DataMapType<EntityType<?>,MobTuning> MOBS=DataMapType.builder(ResourceLocation.fromNamespaceAndPath("oxyrpg","mob_tuning"),Registries.ENTITY_TYPE,MobTuning.CODEC).build();
    public static final DataMapType<Item,WeaponTuning> WEAPONS=DataMapType.builder(ResourceLocation.fromNamespaceAndPath("oxyrpg","weapon_tuning"),Registries.ITEM,WeaponTuning.CODEC).synced(WeaponTuning.CODEC,false).build();
    private RpgDataMaps(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){bus.addListener(RpgDataMaps::types);}
    private static void types(RegisterDataMapTypesEvent event){event.register(MOBS);event.register(WEAPONS);}
    public static MobTuning mob(LivingEntity e){var data=BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(e.getType()).getData(MOBS);return data==null?MobTuning.DEFAULT:data;}
    public static WeaponTuning weapon(ItemStack stack){var data=BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).getData(WEAPONS);return data==null?WeaponTuning.DEFAULT:data;}
    public static void resistance(LivingDamageEvent.Pre event){
        if(!(event.getEntity() instanceof Mob))return;var data=mob(event.getEntity());var source=event.getSource();
        float resistance=source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)?data.fireResistance():source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE)?data.frostResistance():source.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)?data.lightningResistance():0;
        event.setNewDamage(event.getNewDamage()*(1-resistance));
    }
}
