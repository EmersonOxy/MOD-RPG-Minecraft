package dev.emersonoxy.rpg.client;
import java.util.Optional;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleLimit;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
/** Native leaf sprites, biome tint and coherent gusts; no imported textures. */
public final class AmbientLeaf extends SingleQuadParticle {
    private static SpriteSet sprites;
    private static final ParticleLimit LIMIT=new ParticleLimit(192);
    private final double phase;
    private AmbientLeaf(ClientLevel level,double x,double y,double z,int color,boolean needle){super(level,x,y,z,sprites.get(level.random));setColor((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);quadSize=needle?.055f:.10f+random.nextFloat()*.055f;setSize(.025f,.025f);lifetime=140;gravity=.075f;friction=.98f;phase=random.nextDouble()*Math.PI*2;roll=oRoll=random.nextFloat()*6.28f;}
    public static void register(RegisterParticleProvidersEvent e){e.registerSpriteSet(dev.emersonoxy.rpg.RpgParticles.LEAF.get(),s->{sprites=s;return (type,level,x,y,z,dx,dy,dz,random)->new AmbientLeaf(level,x,y,z,0x70A349,false);});}
    public static void spawn(ClientLevel level,double x,double y,double z,int color,boolean needle){if(sprites!=null)net.minecraft.client.Minecraft.getInstance().particleEngine.add(new AmbientLeaf(level,x,y,z,color,needle));}
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public Optional<ParticleLimit> getParticleLimit(){return Optional.of(LIMIT);}
    @Override public void tick(){if(!AmbientOptions.leaves){remove();return;}double t=level.getGameTime()*.012,wind=.02+.018*Math.sin(t*.4);xd=wind*Math.cos(t)+Math.sin(age*.08+phase)*.015;zd=wind*Math.sin(t)+Math.cos(age*.07+phase)*.015;yd=Math.max(yd,-.04);oRoll=roll;roll+=.025;super.tick();alpha=Math.min(1,(lifetime-age)/25f);if(onGround)remove();}
}
