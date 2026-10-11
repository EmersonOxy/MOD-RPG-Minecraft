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
    private static int emitted;
    private static final java.util.ArrayDeque<BloodBurst> BLOOD=new java.util.ArrayDeque<>();
    private static final class BloodBurst {
        final Vec3 at,direction;final double power;long remaining;int index;
        BloodBurst(Vec3 at,Vec3 direction,double damage,long count){this.at=at;this.direction=direction;this.power=Math.sqrt(damage)*.025;remaining=count;}
    }
    private ParticleFx(){}
    public static void clear(){tracked=null;tick=Long.MIN_VALUE;emitted=0;BLOOD.clear();}
    private static void refresh(ClientLevel level){
        if(tracked!=level||tick!=level.getGameTime()){tracked=level;tick=level.getGameTime();emitted=0;}
    }
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
        if(!finite(at)||!Float.isFinite(packet.damage())||packet.damage()<=0||!visible(mc.level,at))return;
        Vec3 direction=new Vec3(packet.dx(),packet.dy(),packet.dz());if(!finite(direction)||direction.lengthSqr()>1.1)return;
        double scale=CombatOptions.particleIntensity*(mc.options.particles().get()==ParticleStatus.DECREASED?.5:1);
        // No artistic damage/drop ceiling: batches spread computation without cutting the requested total.
        long total=Math.max(1,(long)Math.ceil(packet.damage()*1.5*scale));
        if(tracked!=mc.level){clear();tracked=mc.level;}
        BLOOD.addLast(new BloodBurst(at,direction,packet.damage(),total));
    }
    public static void tick(){
        var mc=Minecraft.getInstance();if(mc.level==null){clear();return;}
        if(tracked!=mc.level){clear();tracked=mc.level;}
        CombatOptions.load();if(!CombatOptions.blood){BLOOD.clear();return;}if(mc.isPaused())return;
        int budget=128;var random=mc.level.random;
        while(budget>0&&!BLOOD.isEmpty()){
            var burst=BLOOD.removeFirst();if(!visible(mc.level,burst.at))continue;
            int n=(int)Math.min(Math.min(budget,16),burst.remaining);
            for(int i=0;i<n;i++){
                double spread=.12+burst.power;
                var velocity=burst.direction.scale(.12+burst.power)
                    .add((random.nextDouble()-.5)*spread,.04+random.nextDouble()*(.17+burst.power*.4),(random.nextDouble()-.5)*spread);
                // Mix downward splashes with the outward spray so hard hits soak nearby floors too.
                if(burst.index++%4==0)velocity=new Vec3(velocity.x,-.2-random.nextDouble()*burst.power,velocity.z);
                var at=burst.at.add((random.nextDouble()-.5)*.25,(random.nextDouble()-.5)*.3,(random.nextDouble()-.5)*.25);
                mc.particleEngine.createParticle(RpgParticles.BLOOD.get(),at.x,at.y,at.z,velocity.x,velocity.y,velocity.z);
            }
            burst.remaining-=n;budget-=n;if(burst.remaining>0)BLOOD.addLast(burst);
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
