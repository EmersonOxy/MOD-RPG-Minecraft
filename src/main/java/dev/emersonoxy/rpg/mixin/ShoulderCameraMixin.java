package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.ShoulderCamera;
import dev.emersonoxy.rpg.client.TargetLock;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Camera.class,remap=false)
public abstract class ShoulderCameraMixin {
    @Shadow private boolean detached;
    @Shadow protected abstract void setPosition(Vec3 position);
    @Shadow protected abstract void setRotation(float yaw,float pitch,float roll);
    @Inject(method="setup",at=@At("TAIL"))
    private void oxyrpg$shoulder(BlockGetter world,Entity entity,boolean detached,boolean reverse,float partial,CallbackInfo ci){
        var pose=ShoulderCamera.pose(entity,detached,reverse,partial);
        if(pose!=null){setPosition(pose.position());setRotation(pose.yaw(),pose.pitch(),pose.roll());this.detached=pose.detached();}
        else if(!detached&&entity==net.minecraft.client.Minecraft.getInstance().player){
            var rotation=TargetLock.firstPersonRotation(partial);
            if(rotation!=null)setRotation(rotation.x,rotation.y,0);
        }
    }
}
