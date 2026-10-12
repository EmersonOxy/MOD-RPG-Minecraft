package dev.emersonoxy.rpg.client;

import java.util.*;
import com.google.common.reflect.TypeToken;
import dev.emersonoxy.rpg.RpgClass;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.context.ContextKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

/** Event-driven vanilla model gestures and bounded, client-only class cosmetics. */
public final class ClassCastEffects {
    private record Cast(int entity,int kind,int slot,int duration,long born){}
    public record Pose(int kind,int slot,float weight){}
    private static final Map<Integer,Cast> CASTS=new LinkedHashMap<>();
    private static final int[] COLORS={0,0xE5B768,0x72CFF5,0xADD86A,0xB388E9,0x64D9AE,0xD7EFF8,0xA878DB,0x54BEDF,0xF58A40,0xB81F43};
    private static final DustParticleOptions[] DUST=Arrays.stream(COLORS).mapToObj(c->new DustParticleOptions(c,.8f)).toArray(DustParticleOptions[]::new);
    private static final ContextKey<Pose> POSE=new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","cast_pose"));
    private static Object world;
    private ClassCastEffects(){}
    public static void clear(){CASTS.clear();world=null;}
    public static void cancel(int entity){CASTS.remove(entity);}
    public static void receive(RpgNetwork.CastFx packet){
        CombatOptions.load();
        var mc=Minecraft.getInstance();if(mc.level==null||packet.kind()<1||packet.kind()>=COLORS.length||packet.slot()<0||packet.slot()>2||packet.duration()<1||packet.duration()>600)return;
        if(world!=mc.level){clear();world=mc.level;}
        CASTS.remove(packet.entity());if(CASTS.size()>=32)CASTS.remove(CASTS.keySet().iterator().next());
        CASTS.put(packet.entity(),new Cast(packet.entity(),packet.kind(),packet.slot(),packet.duration(),mc.level.getGameTime()));
    }
    public static void tick(){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null){clear();return;}if(world!=mc.level){clear();world=mc.level;}if(mc.isPaused())return;
        long now=mc.level.getGameTime();int budget=24;
        var iterator=CASTS.values().iterator();while(iterator.hasNext()){
            var c=iterator.next();var entity=mc.level.getEntity(c.entity());long age=now-c.born();
            if(entity==null||!entity.isAlive()||age>=c.duration()){iterator.remove();continue;}
            if(!CombatOptions.effects||entity.distanceToSqr(mc.player)>32*32||age%2!=0||budget<2)continue;
            // Respect the native particle preference. No particles are sent every tick by the server.
            if(mc.options.particles().get()==net.minecraft.server.level.ParticleStatus.MINIMAL||mc.options.particles().get()==net.minecraft.server.level.ParticleStatus.DECREASED&&age%4!=0)continue;
            if(c.kind()==RpgClass.MAGO.ordinal()&&c.slot()==1||c.kind()==RpgClass.HIDROMANTE.ordinal()&&c.slot()==1)continue;
            for(int i=0;i<2;i++){double angle=age*.32+i*Math.PI;double r=c.kind()==6?1.1:.65;
                var at=entity.position().add(Math.cos(angle)*r,.6+Math.sin(age*.2)*.25,Math.sin(angle)*r);
                mc.level.addParticle(DUST[c.kind()],at.x,at.y,at.z,0,.025,0);budget--;
            }
            if(age<18&&budget>0){
                ParticleOptions motif=switch(RpgClass.values()[c.kind()]){
                    case MAGO->c.slot()==0?ParticleTypes.SNOWFLAKE:c.slot()==1?ParticleTypes.SMALL_FLAME:ParticleTypes.ENCHANT;
                    case GUERREIRO->ParticleTypes.CRIT;case ARQUEIRO->ParticleTypes.ENCHANT;case NECROMANTE->ParticleTypes.SOUL;
                    case XAMA->ParticleTypes.HAPPY_VILLAGER;case AVIARIO->ParticleTypes.CLOUD;case ILUSIONISTA->ParticleTypes.PORTAL;
                    case HIDROMANTE->entity.isInWater()?ParticleTypes.BUBBLE:ParticleTypes.FALLING_WATER;case PIROMANTE->ParticleTypes.SMALL_FLAME;
                    default->DUST[c.kind()];
                };
                var look=entity.getLookAngle();var at=entity.position().add(0,1.1,0).add(look.scale(c.kind()==6?-.5:.65));
                mc.level.addParticle(motif,at.x,at.y,at.z,look.x*.025,.015,look.z*.025);budget--;
            }
        }
    }
    public static void register(RegisterRenderStateModifiersEvent event){
        event.registerEntityModifier(new TypeToken<EntityRenderer<Entity,EntityRenderState>>() {},(entity,state)->{
            if(!(state instanceof AvatarRenderState))return;state.setRenderData(POSE,null);var mc=Minecraft.getInstance();var cast=CASTS.get(entity.getId());if(cast==null||mc.level==null)return;
            float age=mc.level.getGameTime()-cast.born()+mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            if(age<0||age>=18)return;float t=age/18f;float weight=(float)Math.sin(Math.PI*t);weight*=weight;
            state.setRenderData(POSE,new Pose(cast.kind(),cast.slot(),weight));
        });
    }
    public static void animate(HumanoidModel<?> model,AvatarRenderState state){
        CombatOptions.load();if(!CombatOptions.animations)return;Pose pose=state.getRenderData(POSE);if(pose==null||state.isSpectator||state.isUsingItem||state.isPassenger||state.isFallFlying||state.swimAmount>0)return;
        float right=-1.35f,left=-1.35f,rz=.15f,lz=-.15f;
        switch(RpgClass.values()[pose.kind()]){
            case GUERREIRO->{right=-1.9f;left=-.65f;rz=.25f;lz=-.3f;}
            case ARQUEIRO->{right=-1.45f;left=-1.45f;rz=.1f;lz=-.5f;}
            case NECROMANTE->{right=-.7f;left=-.7f;rz=.65f;lz=-.65f;}
            case XAMA->{right=-1.0f;left=-1.0f;rz=.55f;lz=-.55f;}
            case AVIARIO->{right=-.15f;left=-.15f;rz=1.1f;lz=-1.1f;}
            case ILUSIONISTA->{right=-.8f;left=-.8f;rz=.75f;lz=-.75f;}
            case PIROMANTE->{right=-1.5f;left=-.75f;rz=.2f;lz=-.45f;}
            default->{}
        }
        // Blend instead of freezing the running pose. Held-item use and gliding keep their vanilla animations.
        if(state.mainArm==net.minecraft.world.entity.HumanoidArm.LEFT){arm(model.leftArm,right,-rz,pose.weight());arm(model.rightArm,left,-lz,pose.weight());}
        else{arm(model.rightArm,right,rz,pose.weight());arm(model.leftArm,left,lz,pose.weight());}
    }
    private static void arm(ModelPart part,float x,float z,float weight){part.xRot+=(x-part.xRot)*weight;part.zRot+=(z-part.zRot)*weight;}

    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(!CombatOptions.effects||!CombatOptions.swarm||mc.level==null||mc.player==null||mc.screen!=null||mc.options.hideGui)return;int budget=8;
        double now=mc.level.getGameTime()+mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for(var c:CASTS.values()){
            if(c.kind()!=7||c.slot()!=2||budget<=0)continue;var entity=mc.level.getEntity(c.entity());if(entity==null||entity.distanceToSqr(mc.player)>24*24)continue;
            for(int i=0;i<4&&budget-->0;i++){
                double age=now-c.born(),a=age*.11+i*Math.PI/2;Vec3 at=entity.getPosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false)).add(Math.cos(a)*1.1,.9+Math.sin(a*2)*.45,Math.sin(a)*1.1);
                var p=CameraProjection.project(at);if(!WorldLines.onScreen(p)||!WorldLines.visible(at))continue;
                int x=(int)((p.x+1)*g.guiWidth()/2),y=(int)((1-p.y)*g.guiHeight()/2),wing=2+(int)(2*(.5+.5*Math.sin(age*.9+i)));
                int color=0xDB372744;g.fill(x-1,y-1,x+1,y+2,color);g.fill(x-wing-2,y-2,x-1,y,color);g.fill(x+1,y-2,x+wing+2,y,color);
                g.fill(x-wing-2,y-3,x-wing,y-1,color);g.fill(x+wing,y-3,x+wing+2,y-1,color);
            }
        }
    }
}
