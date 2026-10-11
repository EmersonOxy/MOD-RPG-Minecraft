package dev.emersonoxy.rpg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
/** Cosmetic messages never alter damage, targeting, resources or terrain. */
public final class SkillEffects {
    private SkillEffects(){}
    public static void impact(ServerLevel world,Vec3 at,double radius,int kind){PacketDistributor.sendToPlayersNear(world,null,at.x,at.y,at.z,48,new RpgNetwork.ImpactFx(at.x,at.y,at.z,(float)Math.clamp(radius,.5,16),kind));}
}
