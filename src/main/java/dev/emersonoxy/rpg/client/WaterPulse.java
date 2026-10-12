package dev.emersonoxy.rpg.client;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleLimit;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
/** Sonic-style expanding rings oriented across the water jet, using native atlas animation. */
public final class WaterPulse extends SingleQuadParticle {
    private static final ParticleLimit LIMIT=new ParticleLimit(160);
    private final SpriteSet sprites;private final org.joml.Quaternionf orientation;
    private WaterPulse(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites){super(level,x,y,z,sprites.get(0,1));this.sprites=sprites;lifetime=12;hasPhysics=false;quadSize=.5f;setColor(.12f,.65f,1);var dir=new org.joml.Vector3f((float)dx,(float)dy,(float)dz);if(dir.lengthSquared()<.0001)dir.set(0,0,1);dir.normalize();orientation=new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0,0,1),dir);setSpriteFromAge(sprites);}
    public static void register(RegisterParticleProvidersEvent e){e.registerSpriteSet(dev.emersonoxy.rpg.RpgParticles.WATER_PULSE.get(),s->(type,level,x,y,z,dx,dy,dz,random)->CombatOptions.effects?new WaterPulse(level,x,y,z,dx,dy,dz,s):null);}
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public Optional<ParticleLimit> getParticleLimit(){return Optional.of(LIMIT);}
    @Override public void tick(){if(age++>=lifetime||!CombatOptions.effects){remove();return;}setSpriteFromAge(sprites);alpha=.85f*(1-age/(float)lifetime);quadSize=.5f+age*.035f;}
    @Override public void extract(QuadParticleRenderState state,Camera camera,float partial){extractRotatedQuad(state,camera,orientation,partial);}
}
