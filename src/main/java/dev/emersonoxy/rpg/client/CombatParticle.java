package dev.emersonoxy.rpg.client;

import java.util.Optional;
import dev.emersonoxy.rpg.RpgParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.joml.Quaternionf;

/** Reuses native atlas sprites with custom trajectories, fading, collision and surface orientation. */
public final class CombatParticle extends SingleQuadParticle {
    // ParticleLimit is a value record: distinct limits prevent separate categories sharing a counter.
    private static final ParticleLimit SMOKE_LIMIT=new ParticleLimit(128),IMPACT_LIMIT=new ParticleLimit(64);
    private final int kind;
    private final SpriteSet sprites;
    private final float initialSize;
    private CombatParticle(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites,int kind){
        super(level,x,y,z,sprites.get(level.random));this.kind=kind;this.sprites=sprites;
        setParticleSpeed(dx,dy,dz);setSize(.025f,.025f);hasPhysics=kind!=0;
        if(kind==0){lifetime=28;quadSize=.035f+random.nextFloat()*.025f;setColor(.65f,.025f,.035f);gravity=1;friction=.97f;}
        else if(kind==1){lifetime=32+random.nextInt(17);quadSize=.22f+random.nextFloat()*.16f;setColor(.55f,.53f,.50f);gravity=-.025f;friction=.93f;setSpriteFromAge(sprites);}
        else{lifetime=8+random.nextInt(6);quadSize=.065f+random.nextFloat()*.07f;setColor(.88f,.8f,.65f);gravity=.5f;friction=.86f;}
        initialSize=quadSize;roll=oRoll=random.nextFloat()*(float)(Math.PI*2);
    }
    public static void register(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(RpgParticles.BLOOD.get(),sprites->(type,level,x,y,z,dx,dy,dz,random)->enabled(0)?new CombatParticle(level,x,y,z,dx,dy,dz,sprites,0):null);
        event.registerSpriteSet(RpgParticles.SMOKE.get(),sprites->(type,level,x,y,z,dx,dy,dz,random)->enabled(1)?new CombatParticle(level,x,y,z,dx,dy,dz,sprites,1):null);
        event.registerSpriteSet(RpgParticles.IMPACT.get(),sprites->(type,level,x,y,z,dx,dy,dz,random)->enabled(2)?new CombatParticle(level,x,y,z,dx,dy,dz,sprites,2):null);
    }
    private static boolean enabled(int kind){CombatOptions.load();return kind==0?CombatOptions.blood:kind==1?CombatOptions.smoke:CombatOptions.impacts;}
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public Optional<ParticleLimit> getParticleLimit(){return kind==0?Optional.empty():Optional.of(kind==1?SMOKE_LIMIT:IMPACT_LIMIT);}
    @Override public float getQuadSize(float partial){float t=(age+partial)/lifetime;return initialSize*(kind==1?.6f+1.7f*t:kind==2?1-.75f*t:1);}
    @Override public void tick(){
        if(!enabled(kind)||!ParticleFx.visible(level,getPos())||!level.getFluidState(BlockPos.containing(x,y,z)).isEmpty()){remove();return;}
        if(kind==0){
            xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}
            yd-=.04*gravity;Vec3 from=getPos(),to=from.add(xd,yd,zd);
            if(!level.hasChunkAt(BlockPos.containing(to))){remove();return;}
            var hit=level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,CollisionContext.empty()));
            if(hit.getType()!=HitResult.Type.MISS){
                if(CombatOptions.bloodMarks&&level.getFluidState(hit.getBlockPos()).isEmpty())
                    Minecraft.getInstance().particleEngine.add(new BloodMark(level,hit,sprite));
                remove();return;
            }
            setPos(to.x,to.y,to.z);xd*=friction;yd*=friction;zd*=friction;alpha=1-age/(float)lifetime*.35f;
        }else{super.tick();alpha=(kind==1?.65f:1)*(1-age/(float)lifetime);if(kind==1)setSpriteFromAge(sprites);}
    }
    private static final class BloodMark extends SingleQuadParticle {
        private final BlockPos support;
        private final BlockState original;
        private final Quaternionf orientation;
        private final Vec3 normal;
        private final float initialSize;
        BloodMark(ClientLevel level,BlockHitResult hit,TextureAtlasSprite sprite){
            super(level,hit.getLocation().x,hit.getLocation().y,hit.getLocation().z,sprite);
            support=hit.getBlockPos().immutable();original=level.getBlockState(support);
            var face=hit.getDirection();normal=new Vec3(face.getStepX(),face.getStepY(),face.getStepZ());
            var at=hit.getLocation().add(normal.scale(.003));setPos(at.x,at.y,at.z);xo=x;yo=y;zo=z;
            lifetime=CombatOptions.bloodMarkSeconds*20;hasPhysics=false;
            // Keep splashes within the hit block face instead of spanning adjacent blocks.
            var local=hit.getLocation().subtract(Vec3.atLowerCornerOf(support));double margin=1;
            for(var axis:Direction.Axis.values())if(axis!=face.getAxis()){
                double v=local.get(axis);margin=Math.min(margin,Math.min(v,1-v));
            }
            quadSize=(float)Math.max(0,Math.min(.1+random.nextDouble()*.09,margin*.65));initialSize=quadSize;
            if(quadSize<.025)remove();setSize(quadSize*2,quadSize*2);
            setColor(.50f,.012f,.022f);alpha=.9f;
            orientation=new Quaternionf().rotationTo(0,0,1,(float)normal.x,(float)normal.y,(float)normal.z).rotateZ(random.nextFloat()*(float)(Math.PI*2));
            // Verify corners against the same collision surface (slabs/stairs/fences included).
            var shape=original.getCollisionShape(level,support);
            for(int a:new int[]{-1,1})for(int b:new int[]{-1,1}){
                var tangent=new org.joml.Vector3f(a*quadSize,b*quadSize,0).rotate(orientation);
                var corner=hit.getLocation().add(tangent.x,tangent.y,tangent.z);
                var check=shape.clip(corner.add(normal.scale(.03)),corner.subtract(normal.scale(.03)),support);
                if(check==null||check.getDirection()!=face||check.getLocation().distanceToSqr(corner)>.0001)remove();
            }
        }
        @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
        @Override protected int getLightColor(float partial){var p=Minecraft.getInstance().player;return p!=null&&dev.emersonoxy.rpg.RpgVampire.twilight(p)?240:super.getLightColor(partial);}
        @Override public Optional<ParticleLimit> getParticleLimit(){return Optional.empty();}
        @Override public void tick(){
            if(!CombatOptions.blood||!CombatOptions.bloodMarks||Minecraft.getInstance().level!=level||!level.hasChunkAt(support)||age++>=lifetime){remove();return;}
            if(age%10==0&&(level.getBlockState(support)!=original||!level.getFluidState(support).isEmpty()
                ||!level.getFluidState(BlockPos.containing(getPos().add(normal.scale(.05)))).isEmpty()
                ||level.isRainingAt(BlockPos.containing(getPos()).above()))){remove();return;}
            float t=age/(float)lifetime;alpha=.9f*(1-Math.clamp((t-.75f)/.25f,0,1));
            setColor(.50f-.16f*t,.012f,.022f);
        }
        @Override public void extract(QuadParticleRenderState state,Camera camera,float partial){
            if(!CombatOptions.blood||!CombatOptions.bloodMarks)return;
            extractRotatedQuad(state,camera,orientation,partial);
        }
        @Override public float getQuadSize(float partial){return initialSize;}
    }
}
