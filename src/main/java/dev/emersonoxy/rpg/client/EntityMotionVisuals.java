package dev.emersonoxy.rpg.client;
import java.util.*;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.world.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
/** Extracted visual state: native bat renderer replaces the avatar, not a second world entity. */
public final class EntityMotionVisuals {
    private static final Set<Integer> FORMS=new HashSet<>();
    private record Motion(long until,int duration,int kind,int direction){}
    private static final Map<Integer,Motion> MOTIONS=new HashMap<>();
    private record BatDraw(net.minecraft.client.renderer.entity.BatRenderer renderer,BatRenderState state){}
    private static final ContextKey<BatDraw> BAT=new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","bat_form"));
    private static Object world;private static final Map<Integer,net.minecraft.world.entity.ambient.Bat> DUMMIES=new HashMap<>();
    private EntityMotionVisuals(){}
    private static void world(){var w=Minecraft.getInstance().level;if(w!=world){world=w;FORMS.clear();MOTIONS.clear();DUMMIES.clear();}}
    public static void receive(RpgNetwork.EntityMotion p){world();var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.kind()==3||p.kind()==4){if(p.kind()==3)FORMS.add(p.entity());else{FORMS.remove(p.entity());DUMMIES.remove(p.entity());}var entity=mc.level.getEntity(p.entity());if(entity!=null){entity.getPersistentData().putBoolean("oxy_vampire_bat",p.kind()==3);entity.refreshDimensions();}return;}
        if(p.kind()==6){var entity=mc.level.getEntity(p.entity());
            if(entity==mc.player){var dir=new net.minecraft.world.phys.Vec3(0,0,1).yRot((float)Math.toRadians(-entity.getYRot())).yRot((float)(-Math.floorMod(p.ticks(),8)*Math.PI/4));entity.getPersistentData().putDouble("oxy_dodge_dx",dir.x);entity.getPersistentData().putDouble("oxy_dodge_dz",dir.z);}if(entity instanceof net.minecraft.world.entity.Avatar avatar&&net.neoforged.fml.ModList.get().isLoaded("player_animation_library"))DodgeAnimation.play(avatar);MOTIONS.put(p.entity(),new Motion(mc.level.getGameTime()+13,13,6,Math.floorMod(p.ticks(),8)));return;}
        var active=MOTIONS.get(p.entity());if(p.kind()==2&&active!=null&&(active.kind==1||active.kind==5)&&active.until>mc.level.getGameTime())return;
        if(MOTIONS.size()>=2048)MOTIONS.remove(MOTIONS.keySet().iterator().next());
        MOTIONS.put(p.entity(),new Motion(mc.level.getGameTime()+Math.clamp(p.ticks(),0,40),Math.clamp(p.ticks(),1,40),p.kind(),0));
    }
    public static void tick(){world();var mc=Minecraft.getInstance();if(mc.level!=null)MOTIONS.values().removeIf(m->m.until<=mc.level.getGameTime());}
    public static void extract(Entity entity,EntityRenderState state){world();state.setRenderData(BAT,null);var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(entity instanceof net.minecraft.world.entity.player.Player&&(FORMS.contains(entity.getId())||entity.getPersistentData().getBooleanOr("oxy_vampire_bat",false))){
            var dummy=DUMMIES.computeIfAbsent(entity.getId(),id->EntityType.BAT.create(mc.level,EntitySpawnReason.TRIGGERED));if(dummy==null)return;dummy.setPos(entity.position());dummy.xo=entity.xo;dummy.yo=entity.yo;dummy.zo=entity.zo;dummy.setYRot(entity.getYRot());dummy.setXRot(entity.getXRot());dummy.tickCount=entity.tickCount;dummy.setResting(false);dummy.flyAnimationState.start(0);dummy.getPersistentData().putBoolean("oxy_vampire_proxy",true);
            var renderer=(net.minecraft.client.renderer.entity.BatRenderer)mc.getEntityRenderDispatcher().getRenderer(dummy);var b=renderer.createRenderState(dummy,state.partialTick);b.ageInTicks=state.ageInTicks;b.lightCoords=state.lightCoords;b.scale=1.1f;b.bodyRot=entity.getYRot();if(entity==mc.player){var input=mc.player.input.keyPresses;int forward=(input.forward()?1:0)-(input.backward()?1:0),side=(input.right()?1:0)-(input.left()?1:0);if(forward!=0||side!=0){b.bodyRot+= (float)Math.toDegrees(Math.atan2(side,forward));b.xRot=0;}}b.nameTag=null;b.x=state.x;b.y=state.y;b.z=state.z;b.distanceToCameraSq=state.distanceToCameraSq;state.setRenderData(BAT,new BatDraw(renderer,b));state.shadowRadius=.25f;return;}
        var motion=MOTIONS.get(entity.getId());if(motion==null)return;double left=motion.until-mc.level.getGameTime()-state.partialTick;if(left<=0){MOTIONS.remove(entity.getId());return;}if(!CombatOptions.animations)return;
        float t=(float)Math.clamp(1-left/motion.duration,0,1);
        if(motion.kind==1){float ease=t*t*(3-2*t);state.y-=(1-ease)*Math.min(1.6,state.boundingBoxHeight*.85);if(state instanceof LivingEntityRenderState living){living.xRot+=(1-ease)*20;}}
        else if(motion.kind==5&&state instanceof LivingEntityRenderState living){living.scale*=.15f+.85f*(t*t*(3-2*t));}
        else if(motion.kind==6&&state instanceof LivingEntityRenderState living){living.bodyRot+=motion.direction*45;}
        else if(motion.kind==2&&state instanceof LivingEntityRenderState living){living.bodyRot+=(float)Math.sin(t*Math.PI)*12;state.y+=(float)Math.sin(t*Math.PI)*.06;}
    }
    public static boolean morph(RenderPlayerEvent.Pre event){var draw=event.getRenderState().getRenderData(BAT);if(draw==null)return DisguiseVisuals.render(event);draw.renderer.submit(draw.state,event.getPoseStack(),event.getSubmitNodeCollector(),new net.minecraft.client.renderer.state.CameraRenderState());event.setCanceled(true);return true;}
}
