package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Session-only debug progression and independent resource/cooldown controls. */
public final class RpgDebug {
    private static final Map<UUID,Long> SKILLS=new HashMap<>();
    private static final Map<UUID,RpgClass> TEST_CLASSES=new HashMap<>(),DEBUG_CLASSES=new HashMap<>();
    private static final Map<UUID,Integer> LEVELS=new HashMap<>();
    private static final Set<UUID> ARSENAL=new HashSet<>(),NORMAL_RESOURCES=new HashSet<>(),NORMAL_COOLDOWNS=new HashSet<>(),MAX_STATS=new HashSet<>();
    private static final Map<UUID,long[]> ITEM_COOLDOWNS=new HashMap<>();
    private static final Map<UUID,long[]> COOLDOWNS=new HashMap<>();
    private static final int ALL_ATTRIBUTE_NODES=AttributeTree.COUNT/5;
    private static final AttributeStats MAX_ATTRIBUTES=new AttributeStats(ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES);
    private RpgDebug(){}
    public static boolean enabled(ServerPlayer p){return SKILLS.containsKey(p.getUUID());}
    public static boolean testing(ServerPlayer p){return TEST_CLASSES.containsKey(p.getUUID());}
    public static boolean debugging(ServerPlayer p){return enabled(p)&&!testing(p);}
    public static boolean unlimited(ServerPlayer p){return debugging(p)&&!NORMAL_RESOURCES.contains(p.getUUID());}
    public static boolean cooldownsDisabled(net.minecraft.world.entity.player.Player p){return p instanceof ServerPlayer server?cooldownsDisabled(server):p.getPersistentData().getBooleanOr("oxy_debug_no_cooldown",false);}
    public static boolean cooldownsDisabled(ServerPlayer p){return debugging(p)&&!NORMAL_COOLDOWNS.contains(p.getUUID());}
    public static boolean maxAttributes(ServerPlayer p){return testing(p)||debugging(p)&&MAX_STATS.contains(p.getUUID());}
    public static boolean professionOverride(ServerPlayer p,int area){return testing(p)||area==2&&ARSENAL.contains(p.getUUID());}
    public static boolean equipmentOverride(ServerPlayer p){return testing(p)||ARSENAL.contains(p.getUUID());}
    public static int professionXp(ServerPlayer p,int area,int real){return professionOverride(p,area)?ProfessionStore.MAX_XP:real;}
    public static AttributeStats attributes(ServerPlayer p,AttributeStats real){return maxAttributes(p)?MAX_ATTRIBUTES:real;}
    public static void arsenal(ServerPlayer p){ARSENAL.add(p.getUUID());}
    public static void arsenal(ServerPlayer p,boolean on){if(on)ARSENAL.add(p.getUUID());else ARSENAL.remove(p.getUUID());}
    public static void enable(ServerPlayer p,RpgProfile real){
        disable(p);SKILLS.put(p.getUUID(),SkillTree.PAID_MASK);LEVELS.put(p.getUUID(),100);
        DEBUG_CLASSES.put(p.getUUID(),real.rpgClass()==RpgClass.NONE?RpgClass.GUERREIRO:real.rpgClass());clearItemCooldowns(p);
    }
    public static void test(ServerPlayer p,RpgProfile real,RpgClass choice){
        var selected=choice!=null?choice:TEST_CLASSES.getOrDefault(p.getUUID(),real.rpgClass()==RpgClass.NONE?RpgClass.GUERREIRO:real.rpgClass());
        if(!testing(p))disable(p);TEST_CLASSES.put(p.getUUID(),selected);SKILLS.put(p.getUUID(),SkillTree.PAID_MASK);
    }
    public static void disable(ServerPlayer p){clear(p.getUUID());RpgResources.endTesting(p);RpgEvasion.endTesting(p);}
    private static void clear(UUID id){SKILLS.remove(id);TEST_CLASSES.remove(id);DEBUG_CLASSES.remove(id);LEVELS.remove(id);COOLDOWNS.remove(id);ITEM_COOLDOWNS.remove(id);ARSENAL.remove(id);NORMAL_RESOURCES.remove(id);NORMAL_COOLDOWNS.remove(id);MAX_STATS.remove(id);}
    private static void requireDebug(ServerPlayer p){if(!debugging(p))throw new IllegalStateException("Ative /rpg debug on primeiro. O modo teste mantém seus custos normais.");}
    public static void clearSkills(ServerPlayer p){if(debugging(p))SKILLS.put(p.getUUID(),0L);}
    public static void allSkills(ServerPlayer p){requireDebug(p);SKILLS.put(p.getUUID(),SkillTree.PAID_MASK);}
    public static void level(ServerPlayer p,int value){requireDebug(p);LEVELS.put(p.getUUID(),Math.clamp(value,1,RpgProfile.MAX_LEVEL));}
    public static void choose(ServerPlayer p,RpgClass choice){requireDebug(p);DEBUG_CLASSES.put(p.getUUID(),choice);}
    public static void resources(ServerPlayer p,boolean unlimited){requireDebug(p);if(unlimited)NORMAL_RESOURCES.remove(p.getUUID());else NORMAL_RESOURCES.add(p.getUUID());}
    public static void cooldowns(ServerPlayer p,boolean enabled){requireDebug(p);if(enabled)NORMAL_COOLDOWNS.add(p.getUUID());else NORMAL_COOLDOWNS.remove(p.getUUID());ITEM_COOLDOWNS.remove(p.getUUID());clearItemCooldowns(p);}
    public static void maxAttributes(ServerPlayer p,boolean enabled){requireDebug(p);if(enabled)MAX_STATS.add(p.getUUID());else MAX_STATS.remove(p.getUUID());}
    public static void resetCooldowns(ServerPlayer p){requireDebug(p);COOLDOWNS.remove(p.getUUID());ITEM_COOLDOWNS.remove(p.getUUID());clearItemCooldowns(p);}
    private static void clearItemCooldowns(ServerPlayer p){for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);if(id.getNamespace().equals("oxyrpg"))p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(new net.minecraft.world.item.ItemStack(item)));}}
    public static long itemReady(ServerPlayer p,RpgClass type,long real){return enabled(p)?ITEM_COOLDOWNS.computeIfAbsent(p.getUUID(),id->new long[RpgClass.values().length])[type.ordinal()]:real;}
    public static void itemCooldown(ServerPlayer p,RpgClass type,long until){ITEM_COOLDOWNS.computeIfAbsent(p.getUUID(),id->new long[RpgClass.values().length])[type.ordinal()]=until;}
    public static long readyAt(ServerPlayer p,RpgProfile real,int slot){
        if(cooldownsDisabled(p))return 0;
        return enabled(p)?COOLDOWNS.computeIfAbsent(p.getUUID(),id->new long[RpgClass.values().length*3])[effective(p,real).rpgClass().ordinal()*3+slot]:real.readyAt(slot);
    }
    public static void cooldown(ServerPlayer p,RpgProfile effective,int slot,long now){COOLDOWNS.computeIfAbsent(p.getUUID(),id->new long[RpgClass.values().length*3])[effective.rpgClass().ordinal()*3+slot]=now+effective.abilityCooldown(slot);}
    public static RpgProfile effective(ServerPlayer p,RpgProfile real){
        if(!enabled(p))return real;var selected=testing(p)?TEST_CLASSES.get(p.getUUID()):DEBUG_CLASSES.getOrDefault(p.getUUID(),real.rpgClass());
        int level=testing(p)?100:LEVELS.getOrDefault(p.getUUID(),100);
        return new RpgProfile(selected,level,0,real.kills(),0,SKILLS.get(p.getUUID()),real.questStage(),real.questFirst(),real.questSecond(),0,0,attributes(p,real.attributes()),real.attributeNodes(),true);
    }
    public static RpgProfile unlock(ServerPlayer p,RpgProfile real,int id){
        if(debugging(p)&&id>=2&&id<SkillTree.COUNT){
            long mask=SKILLS.get(p.getUUID())|(1L<<id),old;
            do{old=mask;for(int node=2;node<SkillTree.COUNT;node++)if((mask&(1L<<node))!=0)mask|=SkillTree.parents(node)&SkillTree.PAID_MASK;}while(mask!=old);
            SKILLS.put(p.getUUID(),mask);
        }
        return effective(p,real);
    }
    public static void sync(ServerPlayer p,RpgProfile real){var e=effective(p,real);PacketDistributor.sendToPlayer(p,new RpgNetwork.DebugState(debugging(p)?1:testing(p)?2:0,e.level(),e.rpgClass().ordinal(),(e.skills()&SkillTree.PAID_MASK)==SkillTree.PAID_MASK,!cooldownsDisabled(p),unlimited(p),maxAttributes(p),equipmentOverride(p)));}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e){clear(e.getEntity().getUUID());}
    public static void onStop(ServerStoppedEvent e){SKILLS.clear();TEST_CLASSES.clear();DEBUG_CLASSES.clear();LEVELS.clear();COOLDOWNS.clear();ITEM_COOLDOWNS.clear();ARSENAL.clear();NORMAL_RESOURCES.clear();NORMAL_COOLDOWNS.clear();MAX_STATS.clear();}
}
