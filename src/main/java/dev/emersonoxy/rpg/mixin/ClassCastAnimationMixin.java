package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.ClassCastEffects;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=HumanoidModel.class,remap=false)
abstract class ClassCastAnimationMixin {
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("TAIL"))
    private void oxyCast(HumanoidRenderState state,CallbackInfo ci){if(state instanceof AvatarRenderState avatar)ClassCastEffects.animate((HumanoidModel<?>)(Object)this,avatar);}
}
