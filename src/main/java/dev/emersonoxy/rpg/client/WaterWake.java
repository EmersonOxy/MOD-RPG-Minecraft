package dev.emersonoxy.rpg.client;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
/** A horizontal ripple built from native atlas quads, independent of HUD visibility. */
public final class WaterWake extends SingleQuadParticle {
    private static SpriteSet sprites;private static final ParticleLimit LIMIT=new ParticleLimit(64);
    private static final org.joml.Quaternionf FLAT=new org.joml.Quaternionf().rotationX((float)-Math.PI/2);
    private final boolean[] wet=new boolean[64];
    private WaterWake(ClientLevel level,double x,double y,double z){super(level,x,y,z,sprites.get(level.random));lifetime=80;hasPhysics=false;setColor(.72f,.9f,1);}
    public static void register(RegisterParticleProvidersEvent e){e.registerSpriteSet(dev.emersonoxy.rpg.RpgParticles.WAKE.get(),s->{sprites=s;return (type,level,x,y,z,dx,dy,dz,random)->new WaterWake(level,x,y,z);});}
    public static void spawn(ClientLevel level,double x,double y,double z){if(sprites!=null)net.minecraft.client.Minecraft.getInstance().particleEngine.add(new WaterWake(level,x,y,z));}
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public Optional<ParticleLimit> getParticleLimit(){return Optional.of(LIMIT);}
    @Override public void tick(){if(!AmbientOptions.water||age++>=lifetime){remove();return;}double radius=.22+age*.032;if(age==1||age%4==0)for(int i=0;i<64;i++){double a=i*Math.PI/32;wet[i]=level.getFluidState(BlockPos.containing(x+Math.cos(a)*radius,y-.02,z+Math.sin(a)*radius)).is(FluidTags.WATER);}alpha=.7f*(1-age/(float)lifetime);}
    @Override public void extract(QuadParticleRenderState state,Camera camera,float partial){if(!AmbientOptions.water)return;double radius=.22+(age+partial)*.032;var at=camera.getPosition();int color=net.minecraft.util.ARGB.colorFromFloat(alpha,rCol,gCol,bCol),light=getLightColor(partial);
        for(int i=0;i<64;i++)if(wet[i]){double a=i*Math.PI/32;state.add(getLayer(),(float)(x+Math.cos(a)*radius-at.x),(float)(y-at.y),(float)(z+Math.sin(a)*radius-at.z),FLAT.x,FLAT.y,FLAT.z,FLAT.w,(float)(Math.max(.035,radius*.045)),getU0(),getU1(),getV0(),getV1(),color,light);}}
}
