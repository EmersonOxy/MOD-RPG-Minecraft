package dev.emersonoxy.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
/** Cosmetic entrance for genuinely new natural mobs and necromancer summons, never chunk reloads. */
public final class EntityMotions {
    private EntityMotions(){}
    public static void join(EntityJoinLevelEvent e){if(e.loadedFromDisk()){e.getEntity().getPersistentData().remove("oxy_entrance_until");e.getEntity().getPersistentData().remove("oxy_entrance_kind");return;}if(e.isCanceled()||e.getLevel().isClientSide()||!(e.getEntity() instanceof Mob m)||RpgSpecialists.temporary(m)||RpgVampire.proxy(m))return;
        var reason=m.getSpawnType();boolean necro=RpgNecromancer.necromancy(m);if(!necro&&(!(m instanceof Enemy)||reason!=EntitySpawnReason.NATURAL&&reason!=EntitySpawnReason.CHUNK_GENERATION))return;
        int kind=m instanceof net.minecraft.world.entity.monster.Phantom||m instanceof net.minecraft.world.entity.monster.Ghast||m instanceof net.minecraft.world.entity.monster.Blaze?5:1;
        m.getPersistentData().putLong("oxy_entrance_until",m.level().getGameTime()+20);m.getPersistentData().putInt("oxy_entrance_kind",kind);
    }
    public static void tracking(PlayerEvent.StartTracking e){if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof Mob m))return;long ticks=m.getPersistentData().getLongOr("oxy_entrance_until",0)-m.level().getGameTime();if(ticks>0&&ticks<=20)PacketDistributor.sendToPlayer(p,new RpgNetwork.EntityMotion(m.getId(),m.getPersistentData().getIntOr("oxy_entrance_kind",1),(int)ticks));}
    public static void attack(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||Bleeding.damage(e.getSource()))return;var source=e.getSource();if(source.getEntity() instanceof LivingEntity actor&&actor.getPersistentData().getLongOr("oxy_entrance_until",0)<=actor.level().getGameTime()&&source.getDirectEntity()==actor&&(source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)||source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)))PacketDistributor.sendToPlayersTrackingEntityAndSelf(actor,new RpgNetwork.EntityMotion(actor.getId(),2,8));}
}
