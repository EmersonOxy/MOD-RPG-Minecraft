package dev.emersonoxy.rpg.client;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.LoggerFactory;

/** Stable player orientation, with a projected reticle instead of frame-dependent toe-in. */
public final class ShoulderCamera {
    public record Pose(Vec3 position,float yaw,float pitch,float roll,boolean detached){}
    private static boolean loaded,enabled=true,left,rendered,bodyVisible;
    private static int intensity=1;
    private static double distance=2.8,height=.18,lateral=.65;
    private static Entity tracked;
    private static CameraType mode;
    private static long lastFrame;
    private static double depth,side,up,amplitude,orientation;
    private static final CameraMotion.Collision collision=new CameraMotion.Collision();
    private ShoulderCamera(){}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-camera.properties");}
    private static double number(Properties p,String name,double fallback,double min,double max){
        try{return CameraMotion.clamp(Double.parseDouble(p.getProperty(name,""+fallback)),min,max,fallback);}catch(NumberFormatException e){return fallback;}
    }
    private static void load(){
        if(loaded)return;loaded=true;if(!Files.exists(file()))return;
        try(var in=Files.newBufferedReader(file())){var p=new Properties();p.load(in);
            enabled=Boolean.parseBoolean(p.getProperty("enabled","true"));left=Boolean.parseBoolean(p.getProperty("leftShoulder","false"));
            intensity=(int)number(p,"sway",1,0,2);distance=number(p,"distance",2.8,1.2,6);lateral=number(p,"lateral",.65,0,1.6);height=number(p,"height",.18,-.5,1.5);
        }catch(IOException e){LoggerFactory.getLogger(ShoulderCamera.class).warn("Configuração de câmera preservada",e);}
    }
    private static void save(){
        try{Files.createDirectories(file().getParent());var p=new Properties();p.setProperty("enabled",""+enabled);p.setProperty("leftShoulder",""+left);
            p.setProperty("sway",""+intensity);p.setProperty("distance",""+distance);p.setProperty("lateral",""+lateral);p.setProperty("height",""+height);
            var tmp=Files.createTempFile(file().getParent(),"oxyrpg-camera-",".tmp");
            try{try(var out=Files.newBufferedWriter(tmp)){p.store(out,"Oxy RPG camera; distances in blocks; sway 0/1/2");}
                Files.move(tmp,file(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
            }finally{Files.deleteIfExists(tmp);}
        }catch(IOException e){LoggerFactory.getLogger(ShoulderCamera.class).warn("Não foi possível salvar câmera",e);notice("Falha ao salvar opções de câmera.");}
    }
    private static void notice(String text){var p=Minecraft.getInstance().player;if(p!=null)p.displayClientMessage(Component.literal(text),true);}
    public static boolean enabled(){load();return enabled;}
    public static String shoulder(){load();return left?"esquerdo":"direito";}
    public static String sway(){load();return intensity==0?"desligado":intensity==1?"leve":"moderado";}
    public static double value(int axis){load();return axis==0?distance:axis==1?lateral:height;}
    public static String position(int axis){return String.format(java.util.Locale.ROOT,"%.2f",value(axis));}
    public static void adjust(int axis,double amount){load();if(axis==0)distance=Math.clamp(distance+amount,1.2,6);
        else if(axis==1)lateral=Math.clamp(lateral+amount,0,1.6);else height=Math.clamp(height+amount,-.5,1.5);
        save();notice((axis==0?"Distância: ":axis==1?"Deslocamento lateral: ":"Altura: ")+position(axis));}
    public static void defaults(){enabled=true;left=false;intensity=1;distance=2.8;lateral=.65;height=.18;save();}
    public static void toggle(){load();enabled=!enabled;save();}
    public static void swap(){load();left=!left;save();}
    public static void cycle(){load();intensity=(intensity+1)%3;save();}
    public static boolean active(){return rendered&&tracked==Minecraft.getInstance().player;}
    public static boolean hideHands(){var mc=Minecraft.getInstance();return active()&&mc.options.getCameraType().isFirstPerson()&&mc.player!=null&&mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(mc.player.getEyePosition(mc.gameRenderer.getMainCamera().getPartialTickTime()))>.0625;}
    public static void reset(){tracked=null;mode=null;lastFrame=0;depth=side=up=amplitude=orientation=0;rendered=bodyVisible=false;collision.reset();}
    public static Pose pose(Entity entity,boolean detached,boolean reverse,float partial){
        var mc=Minecraft.getInstance();load();
        if(entity!=mc.player||mc.player==null||mc.level==null||mc.player.isSleeping()||mc.player.isPassenger()){reset();return null;}
        var next=mc.options.getCameraType();
        if(tracked!=entity){reset();tracked=entity;mode=next;if(next==CameraType.THIRD_PERSON_FRONT){depth=-4;orientation=180;}}
        long now=System.nanoTime();double dt=lastFrame==0?0:Math.min(.05,(now-lastFrame)/1e9);lastFrame=now;if(mc.isPaused())dt=0;
        if(mode!=next){mode=next;collision.reset();}
        boolean shoulder=enabled&&next==CameraType.THIRD_PERSON_BACK;
        if(!shoulder&&next.isFirstPerson()&&Math.abs(depth)<.015&&Math.abs(side)<.01&&Math.abs(up)<.01){rendered=false;return null;}
        if(!enabled&&Math.abs(side)<.01&&!next.isFirstPerson()){rendered=false;return null;}
        var player=mc.player;var held=player.getUseItem();
        boolean aiming=player.isUsingItem()&&(held.getItem() instanceof BowItem||held.getItem() instanceof CrossbowItem||held.is(Items.TRIDENT)||held.is(Items.SPYGLASS));
        double desiredDepth=shoulder?(aiming?Math.max(1.2,distance*.78):distance):next.isFirstPerson()?0:next.isMirrored()?-4:4;
        depth=CameraMotion.smooth(depth,desiredDepth,10,dt);side=CameraMotion.smooth(side,shoulder?(left?-lateral:lateral):0,10,dt);
        up=CameraMotion.smooth(up,shoulder?height:0,10,dt);orientation=CameraMotion.angle(orientation,next.isMirrored()?180:0,12,dt);
        if(Math.abs(orientation)>359)orientation%=360;
        double bob=player.avatarState().getInterpolatedBob(partial);
        double wanted=shoulder&&intensity>0&&mc.options.bobView().get()&&player.onGround()&&mc.screen==null?(aiming?.12:1)*intensity:0;
        amplitude=CameraMotion.smooth(amplitude,wanted,10,dt);double step=player.avatarState().getBackwardsInterpolatedWalkDistance(partial)*Math.PI;
        // Phase follows distance walked. No free-running oscillation at rest.
        double swayX=Math.sin(step)*bob*.22*amplitude,swayY=-Math.abs(Math.cos(step)*bob)*.3*amplitude;
        float yaw=entity.getViewYRot(partial),pitch=entity.getViewXRot(partial);var forward=entity.getViewVector(partial).normalize();
        double yawRad=Math.toRadians(yaw);var right=new Vec3(-Math.cos(yawRad),0,-Math.sin(yawRad));
        var eye=entity.getEyePosition(partial);double scale=player.getScale();
        var delta=forward.scale(-depth*scale).add(right.scale((side+swayX)*scale)).add(0,(up+swayY)*scale,0);
        var position=eye.add(delta.scale(collision.update(clip(entity,eye,delta),dt)));
        rendered=shoulder||Math.abs(side)>.01||Math.abs(up)>.01||next.isFirstPerson()&&Math.abs(depth)>.015;
        // Native damage reaction remains in GameRenderer.bobHurt.
        float roll=(float)(Math.sin(step)*bob*.8*amplitude),reverseBlend=Math.clamp((float)(Math.abs(orientation)/180),0,1);
        double visibleDistance=eye.distanceTo(position)/scale;
        if(visibleDistance<.9)bodyVisible=false;else if(visibleDistance>1.15)bodyVisible=true;
        return new Pose(position,yaw+(float)orientation,pitch*(1-2*reverseBlend),roll,bodyVisible);
    }
    static double clip(Entity entity,Vec3 eye,Vec3 delta){
        double length=delta.length();if(length<.001)return 1;double fraction=1;
        for(int i=0;i<8;i++){
            var offset=new Vec3((i&1)==0?-.15:.15,(i&2)==0?-.15:.15,(i&4)==0?-.15:.15);var sweep=delta.add(offset);
            var hit=entity.level().clip(new ClipContext(eye,eye.add(sweep),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,entity));
            if(hit.getType()!=HitResult.Type.MISS)fraction=Math.min(fraction,Math.max(0,eye.distanceTo(hit.getLocation())/sweep.length()-.12/length));
        }
        return fraction;
    }
    public static Vec3 aimPoint(float partial){
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return Vec3.ZERO;
        if(mc.hitResult!=null&&mc.hitResult.getType()!=HitResult.Type.MISS)return mc.hitResult.getLocation();
        var eye=p.getEyePosition(partial);var forward=p.getViewVector(partial);var block=p.pick(64,partial,false);
        var hit=ProjectileUtil.getEntityHitResult(p,eye,eye.add(forward.scale(64)),p.getBoundingBox().expandTowards(forward.scale(64)).inflate(1),EntitySelector.CAN_BE_PICKED,eye.distanceToSqr(block.getLocation()));
        return hit==null?block.getLocation():hit.getLocation();
    }
}
