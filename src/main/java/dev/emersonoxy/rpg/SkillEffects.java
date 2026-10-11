package dev.emersonoxy.rpg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
/** Cosmetic messages never alter damage, targeting, resources or terrain. */
public final class SkillEffects {
    private SkillEffects(){}
    public static void cast(net.minecraft.server.level.ServerPlayer player,RpgProfile profile,int slot){
        int duration=profile.rpgClass()==RpgClass.ILUSIONISTA&&slot==2?Math.clamp(profile.abilityDuration(slot),18,600):18;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,new RpgNetwork.CastFx(player.getId(),profile.rpgClass().ordinal(),slot,duration));
    }
    public static void impact(ServerLevel world,Vec3 at,double radius,int kind){PacketDistributor.sendToPlayersNear(world,null,at.x,at.y,at.z,48,new RpgNetwork.ImpactFx(at.x,at.y,at.z,(float)Math.clamp(radius,.5,16),kind));}
}
