package dev.emersonoxy.rpg.client;

import com.google.common.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

public final class EnemyNameplates {
    public static boolean ally(int id){return ALLIES.contains(id);}
    private static final Map<Integer, Long> GHOSTS = new HashMap<>();
    private static final Map<Integer, Integer> LEVELS = new HashMap<>();
    private static final Map<Integer,Integer> VILLAGERS=new HashMap<>();
    private static final Map<Integer,String> TRAITS=new HashMap<>();
    private static final java.util.Set<Integer> COSMETIC=new java.util.HashSet<>();
    private static final java.util.Set<Integer> ALLIES=new java.util.HashSet<>();
    private static final java.util.Set<Integer> ELITES=new java.util.HashSet<>();
    private record Sight(long tick,Vec3 camera,Vec3 target,boolean clear){}
    private static final Map<Mob,Sight> SIGHTS=new java.util.WeakHashMap<>();
    private static ClientLevel trackedWorld;
    private static final ContextKey<Boolean> HIDDEN = new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "ghost"));
    private static final ContextKey<Plate> PLATE = new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "plate"));
    private record Plate(Component name, float health, float max, int level, boolean hostile,boolean ally,boolean elite,int marker) {}
    private EnemyNameplates() {}
    public static void world(ClientLevel level) { if(trackedWorld!=level){trackedWorld=level;LEVELS.clear();GHOSTS.clear();ALLIES.clear();COSMETIC.clear();ELITES.clear();VILLAGERS.clear();TRAITS.clear();SIGHTS.clear();} }
    public static void eliteTitle(int id,String title){world(Minecraft.getInstance().level);TRAITS.put(id,title);}
    public static void villagerMark(int id,int marker){world(Minecraft.getInstance().level);if(marker==0)VILLAGERS.remove(id);else VILLAGERS.put(id,marker);}
    public static void mobLevel(int id,int level) { world(Minecraft.getInstance().level);if(level==-2)COSMETIC.add(id);else COSMETIC.remove(id);if(level<0)ALLIES.add(id);else ALLIES.remove(id);if(level>=1000){ELITES.add(id);level-=1000;}else ELITES.remove(id);LEVELS.put(id, Math.clamp(Math.abs(level),1,100)); }
    public static void ghost(int id, int ticks) {
        var mc = Minecraft.getInstance(); world(mc.level);
        if (mc.level == null) return;
        if (ticks <= 0) GHOSTS.remove(id); else GHOSTS.put(id, mc.level.getGameTime() + ticks);
    }
    public static void clearGhosts() { GHOSTS.clear(); }
    public static boolean hidden(int id) {
        var mc = Minecraft.getInstance();
        return mc.level != null && GHOSTS.getOrDefault(id, 0L) > mc.level.getGameTime();
    }
    private static boolean visible(Mob mob) {
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||mc.options.hideGui||DynamicHud.mode()==DynamicHud.Mode.HIDDEN||!mob.isAlive()||mob.isInvisible()||COSMETIC.contains(mob.getId()))return false;
        var camera=mc.gameRenderer.getMainCamera();
        var start=camera.getPosition();var end=mob.getEyePosition();
        var relative=end.subtract(start);
        if(relative.lengthSqr()>24*24)return false;
        double front=relative.dot(new Vec3(camera.getLookVector()));
        if(front<=0)return false;
        double tangent=Math.tan(Math.toRadians(mc.options.fov().get()/2.0));
        double aspect=(double)mc.getWindow().getWidth()/Math.max(1,mc.getWindow().getHeight());
        if(Math.abs(relative.dot(new Vec3(camera.getUpVector())))>front*tangent
            ||Math.abs(relative.dot(new Vec3(camera.getLeftVector())))>front*tangent*aspect)return false;
        // Reuse stationary sight lines for at most 100 ms; view/frustum checks remain per frame.
        long tick=mc.level.getGameTime();var old=SIGHTS.get(mob);
        if(old!=null&&tick-old.tick()<2&&start.distanceToSqr(old.camera())<.0625&&end.distanceToSqr(old.target())<.0625)return old.clear();
        boolean clear=mc.level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mob)).getType()==HitResult.Type.MISS;
        SIGHTS.put(mob,new Sight(tick,start,end,clear));return clear;
    }
    public static void register(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<EntityRenderer<Entity, EntityRenderState>>() {}, (entity, state) -> {
            if(entity instanceof net.minecraft.world.entity.ambient.Bat&&entity.getCustomName()!=null&&entity.getCustomName().getString().equals("oxy_vampire_form")&&state instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState living)living.scale*=2.2f;
            state.setRenderData(HIDDEN, entity instanceof Player && hidden(entity.getId()));
            state.setRenderData(PLATE, null);
            if (!(entity instanceof Mob mob)) return;
            if (!visible(mob)) {state.nameTag=null;return;}
            var name=mob.getDisplayName();if(ELITES.contains(mob.getId())&&TRAITS.containsKey(mob.getId()))name=name.copy().append(" • "+TRAITS.get(mob.getId()));
            state.setRenderData(PLATE,new Plate(name,mob.getHealth(),mob.getMaxHealth(),LEVELS.getOrDefault(mob.getId(),1),mob instanceof Enemy&&!ALLIES.contains(mob.getId()),ALLIES.contains(mob.getId()),ELITES.contains(mob.getId()),VILLAGERS.getOrDefault(mob.getId(),0)));
            // Non-null tag enables the native submit hook. The custom renderer uses depth testing.
            state.nameTag = Component.empty();
            state.nameTagAttachment = new Vec3(0,mob.getBbHeight()+.7,0);
            state.isDiscrete = true;
        });
    }
    public static void plate(RenderNameTagEvent.DoRender event) {
        var state=event.getEntityRenderState();var plate=state.getRenderData(PLATE);
        if(plate==null)return;
        event.setCanceled(true);
        var pose=event.getPoseStack();var nodes=event.getSubmitNodeCollector();var font=Minecraft.getInstance().font;
        pose.pushPose();
        pose.translate(state.nameTagAttachment.x,state.nameTagAttachment.y,state.nameTagAttachment.z);
        pose.mulPose(event.getCameraRenderState().orientation);
        pose.scale(.018f,-.018f,.018f);
        if(plate.marker>0){
            var marker=Component.literal(plate.marker==2?"?":"!").withStyle(net.minecraft.ChatFormatting.BOLD);
            nodes.submitText(pose,-font.width(marker)/2f,-27,marker.getVisualOrderText(),true,Font.DisplayMode.NORMAL,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,plate.marker==3?0xFF78F091:0xFFFFD54F,0,0);
        }
        nodes.submitText(pose,-font.width(plate.name)/2f,-12,plate.name.getVisualOrderText(),true,Font.DisplayMode.NORMAL,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,plate.elite?0xFFFFA133:0xFFFFFFFF,0,0);
        if(plate.hostile||plate.ally) {
            var level=Component.literal(plate.ally?"Aliado":(plate.elite?"Elite • ":"")+"Lv. "+plate.level);
            nodes.submitText(pose,36,-2,level.getVisualOrderText(),true,Font.DisplayMode.NORMAL,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,0xFFFFD36B,0,0);
        }
        int filled=Math.clamp(Math.round(64*plate.health/Math.max(1,plate.max)),0,64);
        int color=plate.elite?0xFFFF982B:plate.health<=plate.max*.3f?0xFFEF5350:0xFF68D391;
        nodes.submitCustomGeometry(pose,RenderType.debugQuads(),(matrix,consumer)->{
            quad(matrix,consumer,-34,-2,34,6,0,0xFF151A20);
            quad(matrix,consumer,-32,0,32,4,.02f,0xFF424B56);
            if(filled>0)quad(matrix,consumer,-32,0,-32+filled,4,.04f,color);
        });
        var hp=Component.literal(String.format(java.util.Locale.ROOT,"%.1f / %.1f",plate.health,plate.max));
        nodes.submitText(pose,-font.width(hp)/2f,8,hp.getVisualOrderText(),true,Font.DisplayMode.NORMAL,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,0xFFE6EDF3,0,0);
        pose.popPose();
    }
    private static void quad(com.mojang.blaze3d.vertex.PoseStack.Pose pose,com.mojang.blaze3d.vertex.VertexConsumer out,float x1,float y1,float x2,float y2,float z,int color) {
        out.addVertex(pose.pose(),x1,y1,z).setColor(color);
        out.addVertex(pose.pose(),x1,y2,z).setColor(color);
        out.addVertex(pose.pose(),x2,y2,z).setColor(color);
        out.addVertex(pose.pose(),x2,y1,z).setColor(color);
    }
    public static void player(RenderPlayerEvent.Pre event) {
        if (Boolean.TRUE.equals(event.getRenderState().getRenderData(HIDDEN))) event.setCanceled(true);
    }
    public static void hand(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.player != null && hidden(mc.player.getId())) event.setCanceled(true);
    }
}




