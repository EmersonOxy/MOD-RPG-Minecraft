package dev.emersonoxy.rpg;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Copy visible synchronized traits, never position, UUID, navigation or ownership. */
public final class MimicAppearance {
    private MimicAppearance() {}
    public static void copy(Mob source,Mob copy){
        if(source.getType()!=copy.getType())return;
        var traits=source.getEntityData().getNonDefaultValues();
        if(traits!=null)copy.getEntityData().assignValues(traits);
        for(var slot:EquipmentSlot.values())copy.setItemSlot(slot,source.getItemBySlot(slot).copy());
        var health=copy.getAttribute(Attributes.MAX_HEALTH);if(health!=null)health.setBaseValue(source.getMaxHealth());
        copy.setHealth(Math.min(copy.getMaxHealth(),source.getHealth()));
        copy.setCustomName(source.getCustomName());copy.setCustomNameVisible(source.isCustomNameVisible());
        if(!copy.level().isClientSide()){
            copy.setNoAi(false);copy.noPhysics=false;
            for(var slot:EquipmentSlot.values())copy.setDropChance(slot,0);
        }
    }
}
