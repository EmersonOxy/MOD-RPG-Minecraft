package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
/** One arc packet generates cosmetic particles locally instead of dozens of server packets. */
public final class ElectricArcs {
    private ElectricArcs(){}
    public static void receive(RpgNetwork.ElectricArc v){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;var start=new Vec3(v.x(),v.y(),v.z());var end=new Vec3(v.tx(),v.ty(),v.tz());if(!Double.isFinite(start.lengthSqr())||!Double.isFinite(end.lengthSqr())||start.distanceToSqr(end)>65*65||mc.player.position().distanceToSqr(start)>96*96)return;
        var delta=end.subtract(start);int steps=Math.clamp((int)Math.ceil(delta.length()*3),2,48);for(int i=0;i<=steps;i++){var at=start.add(delta.scale((double)i/steps));double jitter=Math.sin(i*2.4+mc.level.getGameTime())*.09;mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK,at.x,at.y+jitter,at.z-jitter,0,0,0);}
    }
}
