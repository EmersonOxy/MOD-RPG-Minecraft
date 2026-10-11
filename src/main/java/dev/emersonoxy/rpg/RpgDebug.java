package dev.emersonoxy.rpg;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Session-only skill overrides. Real points, class and cooldown are never overwritten. */
public final class RpgDebug {
    private static final Map<UUID, Long> SKILLS = new HashMap<>();
    private static final Map<UUID,RpgClass> TEST_CLASSES=new HashMap<>();
    private static final java.util.Set<UUID> ARSENAL=new java.util.HashSet<>();
    private static final Map<UUID,long[]> TEST_COOLDOWNS=new HashMap<>();
    public static boolean unlimited(ServerPlayer p){return enabled(p)&&!testing(p);}
    public static long readyAt(ServerPlayer p,RpgProfile real,int slot){return testing(p)?TEST_COOLDOWNS.computeIfAbsent(p.getUUID(),id->new long[RpgClass.values().length*3])[TEST_CLASSES.get(p.getUUID()).ordinal()*3+slot]:real.readyAt(slot);}
    public static void cooldown(ServerPlayer p,RpgProfile effective,int slot,long now){TEST_COOLDOWNS.computeIfAbsent(p.getUUID(),id->new long[RpgClass.values().length*3])[effective.rpgClass().ordinal()*3+slot]=now+effective.abilityCooldown(slot);}
    private static final int ALL_ATTRIBUTE_NODES=AttributeTree.COUNT/5;
    private static final AttributeStats MAX_ATTRIBUTES=new AttributeStats(ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES,ALL_ATTRIBUTE_NODES);
    private RpgDebug() {}
    public static boolean enabled(ServerPlayer player) { return SKILLS.containsKey(player.getUUID()); }
    public static void enable(ServerPlayer player, RpgProfile profile) { TEST_CLASSES.remove(player.getUUID());TEST_COOLDOWNS.remove(player.getUUID());RpgResources.endTesting(player);RpgEvasion.endTesting(player);SKILLS.put(player.getUUID(), profile.skills()); }
    public static boolean testing(ServerPlayer p){return TEST_CLASSES.containsKey(p.getUUID());}
    public static boolean professionOverride(ServerPlayer p,int area){return testing(p)||area==2&&ARSENAL.contains(p.getUUID());}
    public static boolean equipmentOverride(ServerPlayer p){return testing(p)||ARSENAL.contains(p.getUUID());}
    public static int professionXp(ServerPlayer p,int area,int real){return professionOverride(p,area)?ProfessionStore.MAX_XP:real;}
    public static AttributeStats attributes(ServerPlayer p,AttributeStats real){return testing(p)?MAX_ATTRIBUTES:real;}
    public static void arsenal(ServerPlayer p){ARSENAL.add(p.getUUID());}
    public static void test(ServerPlayer p,RpgProfile real,RpgClass choice){
        var selected=choice!=null?choice:TEST_CLASSES.getOrDefault(p.getUUID(),real.rpgClass()==RpgClass.NONE?RpgClass.GUERREIRO:real.rpgClass());
        TEST_CLASSES.put(p.getUUID(),selected);SKILLS.put(p.getUUID(),SkillTree.PAID_MASK);
    }
    public static void disable(ServerPlayer player) {var id=player.getUUID();SKILLS.remove(id);TEST_CLASSES.remove(id);TEST_COOLDOWNS.remove(id);ARSENAL.remove(id);RpgResources.endTesting(player);RpgEvasion.endTesting(player);}
    public static void clearSkills(ServerPlayer player) { if (enabled(player)&&!testing(player)) SKILLS.put(player.getUUID(), 0L); }
    public static RpgProfile effective(ServerPlayer player, RpgProfile real) {
        var selected=TEST_CLASSES.getOrDefault(player.getUUID(),real.rpgClass());
        if (!enabled(player) || selected == RpgClass.NONE) return real;
        return new RpgProfile(selected, 100, 0, real.kills(), 0, SKILLS.get(player.getUUID()), real.questStage(), real.questFirst(), real.questSecond(), 0, 0, real.attributes());
    }
    public static RpgProfile unlock(ServerPlayer player, RpgProfile real, int id) {
        if(testing(player))return effective(player,real);
        var next = effective(player, real).unlockSkill(id);
        SKILLS.put(player.getUUID(), next.skills());
        return next;
    }
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {var id=event.getEntity().getUUID();SKILLS.remove(id);TEST_CLASSES.remove(id);TEST_COOLDOWNS.remove(id);ARSENAL.remove(id);}
    public static void onStop(ServerStoppedEvent event) { SKILLS.clear();TEST_CLASSES.clear();TEST_COOLDOWNS.clear();ARSENAL.clear(); }
}

