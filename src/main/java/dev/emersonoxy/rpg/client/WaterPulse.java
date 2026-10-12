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
    private final SpriteSet sprites;
    private WaterPulse(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites,boolean fire){super(level,x,y,z,sprites.get(0,1));this.sprites=sprites;lifetime=16;hasPhysics=false;quadSize=1.5f;if(fire){boolean orange=level.random.nextBoolean();setColor(1,orange?.38f:.1f,.02f);}else setColor(.12f,.65f,1);setSpriteFromAge(sprites);}
    public static void register(RegisterParticleProvidersEvent e){e.registerSpriteSet(dev.emersonoxy.rpg.RpgParticles.WATER_PULSE.get(),s->(type,level,x,y,z,dx,dy,dz,random)->CombatOptions.effects?new WaterPulse(level,x,y,z,dx,dy,dz,s,false):null);e.registerSpriteSet(dev.emersonoxy.rpg.RpgParticles.FIRE_PULSE.get(),s->(type,level,x,y,z,dx,dy,dz,random)->CombatOptions.effects?new WaterPulse(level,x,y,z,dx,dy,dz,s,true):null);}
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public Optional<ParticleLimit> getParticleLimit(){return Optional.of(LIMIT);}
    @Override public void tick(){if(age++>=lifetime||!CombatOptions.effects){remove();return;}setSpriteFromAge(sprites);alpha=.85f*(1-age/(float)lifetime);quadSize=1.5f;}
    @Override public void extract(QuadParticleRenderState state,Camera camera,float partial){super.extract(state,camera,partial);}
}
