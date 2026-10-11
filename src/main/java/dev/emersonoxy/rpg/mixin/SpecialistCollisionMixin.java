package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.RpgSpecialists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.tags.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class SpecialistCollisionMixin {
    @Inject(method="getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",at=@At("HEAD"),cancellable=true)
    private void oxyShape(BlockGetter level,BlockPos pos,CollisionContext context,CallbackInfoReturnable<VoxelShape> ci){
        if(!(context instanceof EntityCollisionContext ec)||!(ec.getEntity() instanceof Player p))return;
        var state=(BlockState)(Object)this;
        if(RpgSpecialists.intangible(p)){
            int tier=RpgSpecialists.phaseTier(p);
            var tag=TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oxyrpg",tier>=3?"phase_glass":tier>=2?"phase_barriers":"phase_light"));
            if(state.is(tag)){ci.setReturnValue(Shapes.empty());return;}
        }
        if(RpgSpecialists.waterWalking(p)&&state.getFluidState().is(FluidTags.WATER)&&p.getBoundingBox().minY>=pos.getY()+.85)ci.setReturnValue(Shapes.block());
    }
}
