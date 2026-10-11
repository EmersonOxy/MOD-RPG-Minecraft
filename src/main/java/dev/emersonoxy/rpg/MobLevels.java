package dev.emersonoxy.rpg;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.LoggerFactory;

/** Level is assigned once, after vanilla spawn setup, and saved with the mob. */
public final class MobLevels {
    private static final String KEY = "oxy_level";
    private MobLevels() {}
    public static int level(Mob mob) { return Math.clamp(mob.getPersistentData().getIntOr(KEY, 1), 1, 100); }
    public static void join(EntityJoinLevelEvent event) {
        if (event.isCanceled() || RpgSpecialists.temporary(event.getEntity()) || RpgNecromancer.isSummon(event.getEntity()) || !(event.getLevel() instanceof ServerLevel world)
            || !(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy)) return;
        if (mob.getPersistentData().contains(KEY)) {balance(mob);EliteMobs.slow(mob);return;}
        int assigned = 1;
        if (!event.loadedFromDisk()) {
            ServerPlayer closest = null;
            double distance = 128 * 128;
            for (var player : world.players()) {
                if (!player.isAlive() || player.isSpectator()) continue;
                double d = player.distanceToSqr(mob);
                if (d < distance) { distance = d; closest = player; }
            }
            if (closest != null) try {
                var profile = new ProfileStore(closest).load(closest.getUUID());
                int playerLevel = RpgDebug.effective(closest,profile).level();
                assigned = Math.clamp(playerLevel + mob.getRandom().nextInt(5) - 2, 1, 100);
            } catch (IOException e) { LoggerFactory.getLogger(MobLevels.class).warn("Não foi possível ler o nível para spawn RPG; usando nível 1", e); }
        }
        mob.getPersistentData().putInt(KEY, assigned);
        if(!event.loadedFromDisk())equip(mob,assigned,world);
        balance(mob);
        if(!event.loadedFromDisk())EliteMobs.roll(mob);
    }
    public static double healthFactor(int level){double n=level-1;return (1+n*.07+n*n*.0007)*RpgConfig.value(RpgConfig.MOB_HEALTH,1);}
    public static double damageFactor(int level){double n=level-1;return (1+n*.045+n*n*.00035)*RpgConfig.value(RpgConfig.MOB_DAMAGE,1);}
    static void balance(Mob mob){
        float ratio=mob.getHealth()/Math.max(1,mob.getMaxHealth());
        modifier(mob,Attributes.MAX_HEALTH,"mob_health",healthFactor(level(mob))*RpgDataMaps.mob(mob).health()-1);
        modifier(mob,Attributes.ATTACK_DAMAGE,"mob_damage",damageFactor(level(mob))*RpgDataMaps.mob(mob).damage()-1);
        mob.setHealth(Math.min(mob.getMaxHealth(),ratio*mob.getMaxHealth()));
    }
    static void modifier(Mob mob,net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> key,String name,double amount){
        var attr=mob.getAttribute(key);if(attr==null)return;var id=ResourceLocation.fromNamespaceAndPath("oxyrpg",name);var old=attr.getModifier(id);
        if(old!=null&&old.amount()==amount)return;if(old!=null)attr.removeModifier(id);if(amount!=0)attr.addPermanentModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    public static void damage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event){
        if(event.getSource().getEntity() instanceof Mob mob&&mob instanceof Enemy&&!RpgNecromancer.isSummon(mob)&&!RpgSpecialists.temporary(mob)
            &&(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)||event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)))event.setNewDamage(event.getNewDamage()*(float)(damageFactor(level(mob))*RpgDataMaps.mob(mob).damage()));
    }
    private static void equip(Mob mob,int level,ServerLevel world){
        if(level<10)return;
        boolean humanoid=mob instanceof net.minecraft.world.entity.monster.Zombie
            ||mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton
            ||mob instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin;
        if(!humanoid)return;
        int tier=level>=80?4:level>=60?3:level>=40?2:level>=25?1:0;
        net.minecraft.world.item.Item[][] armor={
            {net.minecraft.world.item.Items.LEATHER_HELMET,net.minecraft.world.item.Items.LEATHER_CHESTPLATE,net.minecraft.world.item.Items.LEATHER_LEGGINGS,net.minecraft.world.item.Items.LEATHER_BOOTS},
            {net.minecraft.world.item.Items.CHAINMAIL_HELMET,net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE,net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS,net.minecraft.world.item.Items.CHAINMAIL_BOOTS},
            {net.minecraft.world.item.Items.IRON_HELMET,net.minecraft.world.item.Items.IRON_CHESTPLATE,net.minecraft.world.item.Items.IRON_LEGGINGS,net.minecraft.world.item.Items.IRON_BOOTS},
            {net.minecraft.world.item.Items.DIAMOND_HELMET,net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,net.minecraft.world.item.Items.DIAMOND_LEGGINGS,net.minecraft.world.item.Items.DIAMOND_BOOTS},
            {net.minecraft.world.item.Items.NETHERITE_HELMET,net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,net.minecraft.world.item.Items.NETHERITE_LEGGINGS,net.minecraft.world.item.Items.NETHERITE_BOOTS}
        };
        var slots=new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET};
        for(int i=0;i<4;i++)if(mob.getRandom().nextFloat()<.2f+level*.008f){
            var stack=enhance(new net.minecraft.world.item.ItemStack(armor[tier][i]),level,world,mob);
            // Preserve named/special equipment supplied by spawn commands and other mods.
            var current=mob.getItemBySlot(slots[i]);
            if(current.isEmpty()||rank(current)<=tier&&!current.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)&&!current.isEnchanted()){
                mob.setItemSlot(slots[i],stack);mob.setDropChance(slots[i],.015f+level*.0004f);
            }
        }
        var held=mob.getMainHandItem();net.minecraft.world.item.ItemStack weapon;
        if(held.is(net.minecraft.world.item.Items.BOW)||held.is(net.minecraft.world.item.Items.CROSSBOW))weapon=held.copy();
        else {
            var swords=new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.WOODEN_SWORD,net.minecraft.world.item.Items.STONE_SWORD,net.minecraft.world.item.Items.IRON_SWORD,net.minecraft.world.item.Items.DIAMOND_SWORD,net.minecraft.world.item.Items.NETHERITE_SWORD};
            weapon=new net.minecraft.world.item.ItemStack(swords[tier]);
        }
        if(held.isEmpty()||rank(held)<=tier&&!held.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)&&!held.isEnchanted()){
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,enhance(weapon,level,world,mob));
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND,.015f+level*.0004f);
        }
    }
    private static int rank(net.minecraft.world.item.ItemStack item){
        if(item.isEmpty())return -1;
        var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem());
        if(!id.getNamespace().equals("minecraft"))return 5;
        String name=id.getPath();
        if(name.startsWith("netherite_"))return 4;
        if(name.startsWith("diamond_"))return 3;
        if(name.startsWith("iron_"))return 2;
        if(name.startsWith("chainmail_")||name.startsWith("stone_"))return 1;
        return 0;
    }
    private static net.minecraft.world.item.ItemStack enhance(net.minecraft.world.item.ItemStack item,int level,ServerLevel world,Mob mob){
        if(level<30||mob.getRandom().nextFloat()>Math.min(.9f,.25f+(level-30)*.009f))return item;
        var allowed=world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE);
        return net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(mob.getRandom(),item,Math.min(30,5+level/3),world.registryAccess(),java.util.Optional.of(allowed));
    }
    public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Mob mob && mob instanceof Enemy&&!RpgNecromancer.isSummon(mob)&&!RpgSpecialists.temporary(mob)){
            PacketDistributor.sendToPlayer(player, new RpgNetwork.MobLevel(mob.getId(), level(mob)+(EliteMobs.elite(mob)?1000:0)));
            if(EliteMobs.elite(mob))PacketDistributor.sendToPlayer(player,new RpgNetwork.EliteInfo(mob.getId(),EliteAbilities.title(mob)));
        }
    }
}




