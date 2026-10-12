package dev.emersonoxy.rpg.mixin;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Use the bat dimensions for collision/pose checks as well as bounding box refresh. */
@Mixin(Avatar.class)
abstract class VampireDimensionsMixin {
    @Inject(method="getDefaultDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;",at=@At("HEAD"),cancellable=true)
    private void oxyBat(Pose pose,CallbackInfoReturnable<EntityDimensions> ci){var avatar=(Avatar)(Object)this;if(avatar.getPersistentData().getBooleanOr("oxy_vampire_bat",false))ci.setReturnValue(EntityDimensions.scalable(.7f,.85f).withEyeHeight(.55f));}
}
