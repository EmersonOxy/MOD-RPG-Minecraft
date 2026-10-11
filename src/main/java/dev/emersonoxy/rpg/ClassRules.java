package dev.emersonoxy.rpg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.LoggerFactory;

public final class ClassRules {
    private ClassRules(){}
    public static RpgClass selected(ServerPlayer p){return RpgAbilities.profile(p).rpgClass();}
    public static boolean ranged(ItemStack item){return item.getItem() instanceof ProjectileWeaponItem||item.is(Items.TRIDENT);}
    public static boolean meat(ItemStack stack){
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return stack.has(DataComponents.FOOD)&&(id.contains("beef")||id.contains("porkchop")||id.contains("mutton")||id.contains("chicken")||id.contains("rabbit")||id.contains("cod")||id.contains("salmon")||id.contains("tropical_fish")||id.contains("pufferfish")||id.equals("rotten_flesh"));
    }
    public static String denial(ServerPlayer p,ItemStack stack){
        if(ClassEquipment.gear(stack)&&!ClassEquipment.allowed(p,stack))return ClassEquipment.denial(p,stack);
        var c=selected(p);
        if(c==RpgClass.GUERREIRO&&ranged(stack))return "Guerreiros não usam arcos, bestas ou tridentes.";
        if(c==RpgClass.ARQUEIRO&&stack.is(Items.SHIELD))return "Arqueiros não equipam escudos.";
        if(c==RpgClass.XAMA&&meat(stack))return "Xamãs não consomem carne nem peixe.";
        return "";
    }
    public static void use(LivingEntityUseItemEvent.Start event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        String reason=denial(p,event.getItem());if(reason.isEmpty())reason=Professions.denial(p,event.getItem(),true);
        if(!reason.isEmpty()){event.setCanceled(true);Professions.notify(p,reason);}
    }
    public static void rightClick(PlayerInteractEvent.RightClickItem event){
        if(!(event.getEntity() instanceof ServerPlayer p)||event.isCanceled())return;
        String reason=denial(p,event.getItemStack());if(reason.isEmpty())reason=Professions.denial(p,event.getItemStack(),true);
        if(!reason.isEmpty()){event.setCanceled(true);event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);Professions.notify(p,reason);}
    }
    public static void damage(LivingDamageEvent.Pre event){
        var owner=RpgNecromancer.owner(event.getSource());
        if(owner!=null&&selected(owner)==RpgClass.NECROMANTE&&!RpgNecromancer.summonKill(event.getSource(),event.getEntity(),owner))event.setNewDamage(event.getNewDamage()*.8f);
        if(event.getEntity() instanceof ServerPlayer p&&selected(p)==RpgClass.MAGO
            &&event.getSource().getEntity() instanceof Enemy&&event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
            &&!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)&&!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION))event.setNewDamage(event.getNewDamage()*1.2f);
    }
    public static void tick(ServerPlayer p){
        var c=selected(p);
        if(c==RpgClass.GUERREIRO&&!p.isCreative()&&!p.isSpectator())p.causeFoodExhaustion(.2f);
        boolean day=c==RpgClass.NECROMANTE&&p.level().dimensionType().hasSkyLight()&&Math.floorMod(p.level().getDayTime(),24000)<12000;
        Professions.modifier(p,Attributes.MOVEMENT_SPEED,"class_day_slow",day?-.15:0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        Professions.modifier(p,Attributes.MAX_HEALTH,"class_archer_health",c==RpgClass.ARQUEIRO?-4:0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    public static String description(RpgClass c){return switch(c){
        case GUERREIRO->"Sem armas de distância; fome adicional.";
        case ARQUEIRO->"Sem escudos; -2 corações de vida base.";
        case MAGO->"+20% dano recebido de ataques corpo a corpo.";
        case NECROMANTE->"-20% dano próprio; -15% velocidade de dia.";
        case AVIARIO->"Voo temporário, proteção na queda e impacto ao pousar.";
        case XAMA->"Não pode comer carne ou peixe.";
        default->"Escolha uma classe para começar.";
    };}
}
