package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.*;

/** Read-only comparison uses the same slot and modifier operations as equipment. */
public final class EquipmentComparison {
    private EquipmentComparison(){}
    public static EquipmentSlot slot(ItemStack stack){
        if(stack.isEmpty())return null;
        var equippable=stack.get(DataComponents.EQUIPPABLE);
        if(equippable!=null){var slot=equippable.slot();if(slot==EquipmentSlot.HEAD||slot==EquipmentSlot.CHEST||slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET)return slot;}
        if(stack.is(Items.SHIELD))return EquipmentSlot.OFFHAND;
        if(stack.getItem() instanceof ProjectileWeaponItem||stack.is(Items.TRIDENT)||stack.is(Items.MACE)||ClassEquipment.gear(stack)||stack.is(net.minecraft.tags.ItemTags.SWORDS)||stack.is(net.minecraft.tags.ItemTags.AXES)||stack.is(net.minecraft.tags.ItemTags.PICKAXES)||stack.is(net.minecraft.tags.ItemTags.SHOVELS)||stack.is(net.minecraft.tags.ItemTags.HOES))return EquipmentSlot.MAINHAND;
        var modifiers=stack.get(DataComponents.ATTRIBUTE_MODIFIERS);return modifiers!=null&&!modifiers.modifiers().isEmpty()?EquipmentSlot.MAINHAND:null;
    }
    public static double stat(ItemStack stack,EquipmentSlot slot,Holder<Attribute> attribute){
        var player=Minecraft.getInstance().player;double base=player.getAttributeBaseValue(attribute);double[] amounts={0,0,1};
        stack.forEachModifier(slot,(a,modifier)->{if(a.equals(attribute))switch(modifier.operation()){case ADD_VALUE->amounts[0]+=modifier.amount();case ADD_MULTIPLIED_BASE->amounts[1]+=modifier.amount();case ADD_MULTIPLIED_TOTAL->amounts[2]*=1+modifier.amount();}});
        double value=base+amounts[0];return (value+value*amounts[1])*amounts[2];
    }
    public static String restriction(ItemStack stack){
        var p=Minecraft.getInstance().player;var c=EquipmentAccess.clientSelected();
        if(stack.isEmpty())return "Slot vazio.";
        if(ClassEquipment.gear(stack)){var data=ClassEquipment.identity(stack);if(!data.getStringOr("owner","").equals(p.getUUID().toString()))return "Bloqueado: pertence a outro jogador.";if(data.getIntOr("class",0)!=c.ordinal())return "Bloqueado: pertence a outra classe.";int stage=data.getIntOr("stage",1);if(stage>ClassEquipment.stage(p))return stage==2?"Requer nível RPG 20 + Arsenal 8.":"Requer nível RPG 50 + Arsenal 16.";}
        if(c==RpgClass.GUERREIRO&&ClassRules.ranged(stack))return "Bloqueado: Guerreiro não usa armas de distância.";
        if(c==RpgClass.ARQUEIRO_NOVO&&stack.is(Items.SHIELD))return "Bloqueado: Arqueiro não usa escudos.";
        if(c==RpgClass.ARQUEIRO&&!ClassRules.lightArmor(stack))return "Bloqueado: Assassino usa armaduras leves.";
        var r=Professions.requirement(stack);if(r.area()>=0&&EquipmentAccess.clientProfessionLevel(r.area())<r.level())return "Requer "+Professions.NAMES[r.area()]+" nível "+r.level()+".";
        return EquipmentAccess.allowed(p,stack)?"Liberado para sua classe e nível.":"Bloqueado para sua classe.";
    }
}
