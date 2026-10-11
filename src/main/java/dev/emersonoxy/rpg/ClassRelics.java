package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Soulbound equipment recovery uses saved player NBT, not a session-only item cache. */
public final class ClassRelics {
    private static final String KEY="oxy_soulbound",QUEUE="items";
    private ClassRelics(){}
    private static CompoundTag data(ServerPlayer p){var root=p.getPersistentData();var persisted=root.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);var data=persisted.getCompoundOrEmpty(KEY);persisted.put(KEY,data);root.put(Player.PERSISTED_NBT_TAG,persisted);return data;}
    public static String effect(RpgClass c){return switch(c){
        case GUERREIRO->"Guarda do Gladiador: resistência breve e empurrão ao redor.";
        case MAGO->"Toque Glacial: seu próximo ataque desacelera o alvo.";
        case ARQUEIRO->"Olho do Caçador: o próximo alvo atingido fica destacado.";
        case NECROMANTE->"Pacto Menor: o próximo ataque recupera 1 coração.";
        case XAMA->"Sopro Vital: limpa veneno e fraqueza e recupera 1 coração.";
        case AVIARIO->"Pluma de Resgate: impulso curto e queda lenta por 3 s.";
        case ILUSIONISTA->"Véu de Espelhos: desorienta inimigos próximos por 3 s.";
        case HIDROMANTE->"Gota Vital: cura na água e concede impulso de nado.";
        case VAMPIRESCO->"Presa Noturna: a próxima mordida recupera um coração.";
        case PIROMANTE->"Fagulha Guardada: queima inimigos próximos por 3 s.";
        default->"Escolha uma classe.";
    };}
    public static void use(ServerPlayer p){var stack=p.getMainHandItem();if(!weapon(p,stack))stack=p.getOffhandItem();use(p,stack);}
    public static void use(ServerPlayer p,ItemStack stack){
        if(RpgVampire.bat(p))return;
        var s=RpgAbilities.profile(p);if(s.rpgClass()==RpgClass.NONE||!p.isAlive()||p.isSpectator())return;
        if(RpgSpecialists.intangible(p)){p.displayClientMessage(Component.literal("[Oxy RPG] O enxame não pode usar itens."),true);return;}
        if(!weapon(p,stack)||recovering(p,stack)){p.displayClientMessage(Component.literal("[Oxy RPG] Segure seu item de classe em uma das mãos."),true);return;}
        if(stack.getItem() instanceof BowItem&&!p.hasInfiniteMaterials()&&p.getProjectile(stack).isEmpty()){Professions.notify(p,"Você precisa de flechas para usar o arco.");return;}
        var d=data(p);long now=p.level().getGameTime(),ready=RpgDebug.itemReady(p,s.rpgClass(),d.getLongOr(s.rpgClass().name(),0));
        if(!RpgDebug.cooldownsDisabled(p)&&ready>now){Professions.notify(p,"Item em recuperação: "+((ready-now+19)/20)+" s.");return;}
        if(s.rpgClass()==RpgClass.ARQUEIRO&&d.getLongOr("charged",0)>now&&d.getIntOr("charged_class",0)==s.rpgClass().ordinal())return;
        if(!RpgResources.affordable(p,10)){p.displayClientMessage(Component.literal("[Oxy RPG] O efeito precisa de 10 MP."),true);return;}
        RpgResources.spend(p,10);int rank=ClassEquipment.identity(stack).getIntOr("stage",1);int cooldown=(40-rank*5)*20;
        if(!RpgDebug.cooldownsDisabled(p)){if(RpgDebug.enabled(p))RpgDebug.itemCooldown(p,s.rpgClass(),now+cooldown);else d.putLong(s.rpgClass().name(),now+cooldown);}
        switch(s.rpgClass()){
            case GUERREIRO->{p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,60,0));for(var m:p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(3),RpgAbilities::attackable))m.knockback(.6,p.getX()-m.getX(),p.getZ()-m.getZ());}
            case MAGO,ARQUEIRO,NECROMANTE,VAMPIRESCO->{d.putLong("charged",now+200);d.putInt("charged_class",s.rpgClass().ordinal());}
            case XAMA->{p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WEAKNESS);p.heal(2);}
            case AVIARIO->{p.setDeltaMovement(p.getDeltaMovement().add(0,.45,0));p.hurtMarked=true;p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,60,0));}
            case ILUSIONISTA->{for(var m:p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(3),RpgAbilities::attackable)){m.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0));m.setTarget(null);}}
            case HIDROMANTE->{if(p.isInWater()){p.heal(2);p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE,60,0));}else p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,100,0));}
            case PIROMANTE->{for(var m:p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(3),RpgAbilities::attackable))m.igniteForSeconds(3);}
            default->{}
        }
        p.displayClientMessage(Component.literal("[Oxy RPG] "+effect(s.rpgClass())),true);
    }
    private static boolean weapon(ServerPlayer p,ItemStack stack){return ClassEquipment.gear(stack)&&ClassEquipment.allowed(p,stack)&&ClassEquipment.identity(stack).getStringOr("part","").equals("weapon");}
    public static void damage(LivingDamageEvent.Post e){if(Bleeding.damage(e.getSource()))return;if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!RpgAbilities.attackable(e.getEntity()))return;
        var d=data(p);long now=p.level().getGameTime();var c=RpgAbilities.profile(p).rpgClass();if(d.getLongOr("charged",0)<=now||d.getIntOr("charged_class",0)!=c.ordinal())return;
        d.remove("charged");if(c==RpgClass.MAGO)e.getEntity().addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,1));else if(c==RpgClass.ARQUEIRO)e.getEntity().addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0));else if(c==RpgClass.NECROMANTE||c==RpgClass.VAMPIRESCO)p.heal(2);
    }
    public static boolean recovering(ServerPlayer p,ItemStack stack){if(RpgDebug.cooldownsDisabled(p)||!ClassEquipment.gear(stack))return false;int id=ClassEquipment.identity(stack).getIntOr("class",0);return id>0&&id<RpgClass.values().length&&data(p).getLongOr("death_"+RpgClass.values()[id].name(),0)>p.level().getGameTime();}
    public static void attack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&recovering(p,p.getMainHandItem())){e.setCanceled(true);p.displayClientMessage(Component.literal("[Oxy RPG] O item ainda está se recuperando da morte."),true);}}
    public static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var d=data(p);var queue=d.getListOrEmpty(QUEUE);var ops=p.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var iterator=e.getDrops().iterator();while(iterator.hasNext()){var entity=iterator.next();var stack=entity.getItem();var identity=ClassEquipment.identity(stack);if(!ClassEquipment.gear(stack)||!identity.getStringOr("owner","").equals(p.getUUID().toString()))continue;
            var encoded=ItemStack.CODEC.encodeStart(ops,stack).result();if(encoded.isEmpty())continue;queue.add(encoded.get());iterator.remove();int id=identity.getIntOr("class",0);if(id>0&&id<RpgClass.values().length){d.putLong(RpgClass.values()[id].name(),p.level().getGameTime()+1200);d.putLong("death_"+RpgClass.values()[id].name(),p.level().getGameTime()+1200);}
        }d.put(QUEUE,queue);
    }
    public static void clonePlayer(PlayerEvent.Clone e){
        if(!(e.getOriginal() instanceof ServerPlayer original)||!(e.getEntity() instanceof ServerPlayer p))return;
        var saved=data(original).copy();var persisted=p.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);persisted.put(KEY,saved);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        if(e.isWasDeath()){long ready=p.level().getGameTime()+1200;for(var c:RpgClass.values())if(c!=RpgClass.NONE){saved.putLong(c.name(),Math.max(ready,saved.getLongOr(c.name(),0)));saved.putLong("death_"+c.name(),ready);}saved.remove("charged");}
    }
    public static void tick(EntityTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.tickCount%20!=0)return;var d=data(p);var queue=d.getListOrEmpty(QUEUE);var ops=p.registryAccess().createSerializationContext(NbtOps.INSTANCE);boolean changed=false;
        for(int i=0;i<queue.size();){var decoded=ItemStack.CODEC.parse(ops,queue.get(i)).result();if(decoded.isEmpty()){i++;continue;}var stack=decoded.get();int slot=-1;for(int j=0;j<36;j++)if(p.getInventory().getItem(j).isEmpty()){slot=j;break;}if(slot<0)break;p.getInventory().setItem(slot,stack);queue.remove(i);changed=true;}
        d.put(QUEUE,queue);if(changed){p.getInventory().setChanged();p.inventoryMenu.broadcastFullState();p.displayClientMessage(Component.literal("[Oxy RPG] Equipamento vinculado recuperado. Efeito especial em recarga após a morte."),true);}
        for(var stack:List.of(p.getMainHandItem(),p.getOffhandItem()))if(weapon(p,stack)){long remaining=d.getLongOr("death_"+RpgAbilities.profile(p).rpgClass().name(),0)-p.level().getGameTime();if(RpgDebug.cooldownsDisabled(p))p.getCooldowns().removeCooldown(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oxyrpg","class_item_"+RpgAbilities.profile(p).rpgClass().name().toLowerCase(java.util.Locale.ROOT)));else if(remaining>0&&!p.getCooldowns().isOnCooldown(stack))p.getCooldowns().addCooldown(stack,(int)Math.min(1200,remaining));}
    }
}
