package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgNetwork;
import dev.emersonoxy.rpg.RpgParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.phys.Vec3;

/** One server event expands locally; emission, raycasts, lifetime and distance are bounded. */
public final class ParticleFx {
    private static ClientLevel tracked;
    private static long tick=Long.MIN_VALUE;
    private static int emitted,rays;
    private ParticleFx(){}
    public static void clear(){tracked=null;tick=Long.MIN_VALUE;emitted=rays=0;}
    private static void refresh(ClientLevel level){
        if(tracked!=level||tick!=level.getGameTime()){tracked=level;tick=level.getGameTime();emitted=rays=0;}
    }
    static boolean ray(ClientLevel level){refresh(level);return rays++<48;}
    static boolean visible(ClientLevel level,Vec3 at){
        var mc=Minecraft.getInstance();CombatOptions.load();
        return mc.level==level&&mc.options.particles().get()!=ParticleStatus.MINIMAL
            &&mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(at)<=32*32
            &&level.hasChunkAt(BlockPos.containing(at));
    }
    private static int count(int base){
        var mc=Minecraft.getInstance();double scale=CombatOptions.particleIntensity*(mc.options.particles().get()==ParticleStatus.DECREASED?.5:1);
        return Math.max(1,(int)Math.round(base*scale));
    }
    private static void spawn(SimpleParticleType type,Vec3 at,Vec3 velocity){
        var mc=Minecraft.getInstance();if(mc.level==null||!visible(mc.level,at))return;
        refresh(mc.level);if(emitted>=64)return;emitted++;
        mc.particleEngine.createParticle(type,at.x,at.y,at.z,velocity.x,velocity.y,velocity.z);
    }
    public static void blood(RpgNetwork.BloodFx packet){
        var mc=Minecraft.getInstance();CombatOptions.load();if(mc.level==null||!CombatOptions.blood)return;
        Vec3 at=new Vec3(packet.x(),packet.y(),packet.z());
        if(!finite(at)||!Float.isFinite(packet.damage())||packet.damage()<=0||packet.damage()>40||!visible(mc.level,at))return;
        Vec3 direction=new Vec3(packet.dx(),packet.dy(),packet.dz());if(!finite(direction)||direction.lengthSqr()>1.1)return;
        var random=mc.level.random;int count=count(Math.clamp((int)Math.ceil(packet.damage()*1.5),1,40));
        for(int i=0;i<count;i++){
            var velocity=direction.scale(.12+Math.min(20,packet.damage())*.006)
                .add((random.nextDouble()-.5)*.24,.04+random.nextDouble()*.17,(random.nextDouble()-.5)*.24);
            if(i==0)velocity=new Vec3(velocity.x,-.2,velocity.z);
            spawn(RpgParticles.BLOOD.get(),at.add((random.nextDouble()-.5)*.25,(random.nextDouble()-.5)*.3,(random.nextDouble()-.5)*.25),velocity);
        }
    }
    public static void impact(RpgNetwork.ImpactFx packet){
        var mc=Minecraft.getInstance();CombatOptions.load();if(mc.level==null)return;
        Vec3 at=new Vec3(packet.x(),packet.y(),packet.z());
        if(!finite(at)||!Float.isFinite(packet.radius())||packet.radius()<=0||packet.radius()>16||!visible(mc.level,at))return;
        var random=mc.level.random;double radius=Math.min(4,packet.radius());boolean takeoff=packet.kind()==7;
        if(CombatOptions.smoke){int n=count(Math.clamp(6+(int)(radius*3),6,18));
            for(int i=0;i<n;i++){
                double angle=i*Math.PI*2/n,spread=takeoff?.7:Math.min(radius*.4,1.2);
                var offset=new Vec3(Math.cos(angle)*spread,.12+random.nextDouble()*.15,Math.sin(angle)*spread);
                var speed=new Vec3(Math.cos(angle)*(.05+radius*.025),takeoff?.025:.045+random.nextDouble()*.035,Math.sin(angle)*(.05+radius*.025));
                spawn(RpgParticles.SMOKE.get(),at.add(offset),speed);
            }
        }
        if(!takeoff&&CombatOptions.impacts){int n=count(Math.clamp(5+(int)(radius*2),5,12));
            for(int i=0;i<n;i++){
                double angle=random.nextDouble()*Math.PI*2;
                spawn(RpgParticles.IMPACT.get(),at.add(0,.1,0),new Vec3(Math.cos(angle)*.17,.08+random.nextDouble()*.14,Math.sin(angle)*.17));
            }
        }
    }
    private static boolean finite(Vec3 v){return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z);}
}
