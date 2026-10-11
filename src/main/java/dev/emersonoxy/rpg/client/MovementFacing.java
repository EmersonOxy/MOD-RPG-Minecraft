package dev.emersonoxy.rpg.client;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

/** Separate the visible character's facing from camera/interaction aim. Vanilla
 * WASD remains camera-relative and the server keeps the ordinary interaction direction. */
public final class MovementFacing {
    private static Entity tracked;
    private static long frame;
    private static double yaw;
    private MovementFacing(){}
    public static boolean freeMovement(){var mc=Minecraft.getInstance();return mc.player!=null&&ShoulderCamera.independentMovement()&&mc.options.getCameraType()!=CameraType.FIRST_PERSON&&!mc.player.isFallFlying()&&!mc.player.isPassenger()&&!mc.player.isInWater();}
    public static boolean sprintImpulse(net.minecraft.client.player.ClientInput input){return freeMovement()?input.getMoveVector().lengthSquared()>.01f:input.hasForwardImpulse();}
    public static void reset(){tracked=null;frame=0;}
    public static void register(RegisterRenderStateModifiersEvent event){
        event.registerEntityModifier(new TypeToken<EntityRenderer<Entity,EntityRenderState>>() {},(entity,state)->{
            var mc=Minecraft.getInstance();
            if(entity!=mc.player||!(state instanceof AvatarRenderState avatar))return;
            if(!ShoulderCamera.independentMovement()||mc.options.getCameraType()==CameraType.FIRST_PERSON
                ||mc.player.isPassenger()||mc.player.isSleeping()||mc.player.isFallFlying()||!mc.player.isAlive()){reset();return;}
            long now=System.nanoTime();
            if(tracked!=entity){tracked=entity;yaw=avatar.bodyRot;frame=now;}
            double dt=mc.isPaused()?0:Math.min(.1,(now-frame)/1e9);frame=now;
            boolean aiming=mc.player.isUsingItem()||mc.player.swinging||mc.options.keyAttack.isDown()||mc.options.keyUse.isDown();
            var input=mc.player.input.keyPresses;double desired=yaw;
            int forward=(input.forward()?1:0)-(input.backward()?1:0),side=(input.right()?1:0)-(input.left()?1:0);
            if(aiming)desired=entity.getYRot();
            else if(forward!=0||side!=0)desired=entity.getYRot()+Math.toDegrees(Math.atan2(side,forward));
            yaw=CameraMotion.angle(yaw,desired,aiming?24:14,dt);
            avatar.bodyRot=(float)yaw;avatar.yRot=0;
            if(!aiming)avatar.xRot=0;
        });
    }
}
