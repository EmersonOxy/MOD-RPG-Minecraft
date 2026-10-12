package dev.emersonoxy.rpg;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Optional complements extend the world; the Oxy owns contracts and summoned creatures. */
public final class ComplementCompat {
    private ComplementCompat() {}
    public static void join(EntityJoinLevelEvent e) {
        if(e.getLevel().isClientSide())return;
        var entity=e.getEntity();
        if(RpgNecromancer.isSummon(entity)||RpgSpecialists.temporary(entity)||RpgVampire.proxy(entity))entity.addTag("ts.mm.exclude");
    }
    public static boolean homestead(Villager v) {
        return NpcContracts.role(v).startsWith("homesteads:")
            &&v.getBrain().getMemory(MemoryModuleType.JOB_SITE)
                .filter(site->site.dimension().equals(v.level().dimension())&&site.pos().distSqr(v.blockPosition())<16*16).isPresent();
    }
}
