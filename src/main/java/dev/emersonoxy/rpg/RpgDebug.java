package dev.emersonoxy.rpg;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Session-only skill overrides. Real points, class and cooldown are never overwritten. */
public final class RpgDebug {
    private static final Map<UUID, Integer> SKILLS = new HashMap<>();
    private RpgDebug() {}
    public static boolean enabled(ServerPlayer player) { return SKILLS.containsKey(player.getUUID()); }
    public static void enable(ServerPlayer player, RpgProfile profile) { SKILLS.put(player.getUUID(), profile.skills()); }
    public static void disable(ServerPlayer player) { SKILLS.remove(player.getUUID()); }
    public static void clearSkills(ServerPlayer player) { if (enabled(player)) SKILLS.put(player.getUUID(), 0); }
    public static RpgProfile effective(ServerPlayer player, RpgProfile real) {
        if (!enabled(player) || real.rpgClass() == RpgClass.NONE) return real;
        return new RpgProfile(real.rpgClass(), 100, 0, real.kills(), 0, SKILLS.get(player.getUUID()), real.questStage(), real.questFirst(), real.questSecond(), 0, 0, real.attributes());
    }
    public static RpgProfile unlock(ServerPlayer player, RpgProfile real, int id) {
        var next = effective(player, real).unlockSkill(id);
        SKILLS.put(player.getUUID(), next.skills());
        return next;
    }
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { SKILLS.remove(event.getEntity().getUUID()); }
    public static void onStop(ServerStoppedEvent event) { SKILLS.clear(); }
}

