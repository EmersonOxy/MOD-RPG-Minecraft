package dev.emersonoxy.rpg.client;

import java.util.HashMap;
import java.util.Map;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/** Client-only models never join the world, tick AI, collide or move the player. */
public final class DisguiseVisuals {
    private record Form(EntityType<?> type,int source,long until,Mob model) {}
    private record Draw(EntityRenderer<Mob,EntityRenderState> renderer,EntityRenderState state) {}
    private static final Map<Integer,Form> FORMS=new HashMap<>();
    private static final ContextKey<Draw> DRAW=new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","disguise_model"));
    private static Object world;
    private DisguiseVisuals() {}
    private static void world(){var next=Minecraft.getInstance().level;if(world!=next){world=next;FORMS.clear();}}
    public static void receive(RpgNetwork.DisguiseState p){
        world();var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.ticks()<=0){FORMS.remove(p.entity());return;}
        var key=ResourceLocation.tryParse(p.mob());if(key==null||!BuiltInRegistries.ENTITY_TYPE.containsKey(key))return;
        var type=BuiltInRegistries.ENTITY_TYPE.getValue(key);var entity=type.create(mc.level,EntitySpawnReason.TRIGGERED);
        if(!(entity instanceof Mob model))return;
        if(FORMS.size()>=128)FORMS.clear();
        var source=mc.level.getEntity(p.source());
        if(source instanceof Mob mob&&mob.getType()==type){
            dev.emersonoxy.rpg.MimicAppearance.copy(mob,model);
        }
        model.setInvisible(false);model.setNoAi(true);
        FORMS.put(p.entity(),new Form(type,p.source(),mc.level.getGameTime()+Math.clamp(p.ticks(),1,12000),model));
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static void extract(Entity entity,EntityRenderState state){
        world();state.setRenderData(DRAW,null);if(!(entity instanceof Player player))return;
        var mc=Minecraft.getInstance();var form=FORMS.get(entity.getId());if(form==null||mc.level==null)return;
        if(!player.isAlive()||form.until<=mc.level.getGameTime()){FORMS.remove(entity.getId());return;}
        var model=form.model;model.setPos(player.position());model.xo=player.xo;model.yo=player.yo;model.zo=player.zo;
        model.xOld=player.xOld;model.yOld=player.yOld;model.zOld=player.zOld;model.setDeltaMovement(player.getDeltaMovement());
        model.tickCount=player.tickCount;model.setYRot(player.getYRot());model.yRotO=player.yRotO;
        model.setXRot(player.getXRot());model.xRotO=player.xRotO;model.setOnGround(player.onGround());
        model.yBodyRot=player.yBodyRot;model.yBodyRotO=player.yBodyRotO;model.yHeadRot=player.yHeadRot;model.yHeadRotO=player.yHeadRotO;
        if(model instanceof net.minecraft.world.entity.ambient.Bat bat){bat.setResting(false);bat.flyAnimationState.startIfStopped(0);}
        var renderer=(EntityRenderer<Mob,EntityRenderState>)(EntityRenderer)mc.getEntityRenderDispatcher().getRenderer(model);
        var draw=renderer.createRenderState(model,state.partialTick);draw.x=state.x;draw.y=state.y;draw.z=state.z;
        draw.ageInTicks=state.ageInTicks;draw.lightCoords=state.lightCoords;draw.distanceToCameraSq=state.distanceToCameraSq;
        draw.isInvisible=false;draw.nameTag=null;
        if(draw instanceof LivingEntityRenderState living&&state instanceof LivingEntityRenderState avatar){
            living.bodyRot=avatar.bodyRot;living.yRot=avatar.yRot;living.xRot=avatar.xRot;
            living.walkAnimationPos=avatar.walkAnimationPos;living.walkAnimationSpeed=avatar.walkAnimationSpeed;
            living.isInvisibleToPlayer=false;
        }
        state.setRenderData(DRAW,new Draw(renderer,draw));state.shadowRadius=draw.shadowRadius;
    }
    public static boolean render(RenderPlayerEvent.Pre event){
        var draw=event.getRenderState().getRenderData(DRAW);if(draw==null)return false;
        draw.renderer.submit(draw.state,event.getPoseStack(),event.getSubmitNodeCollector(),new net.minecraft.client.renderer.state.CameraRenderState());
        event.setCanceled(true);return true;
    }
}
