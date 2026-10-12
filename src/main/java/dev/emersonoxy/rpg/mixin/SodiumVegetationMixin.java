package dev.emersonoxy.rpg.mixin;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Sodium builds its own terrain mesh; omit the static plant replaced by Oxy. */
@Pseudo
@Mixin(targets="net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer",remap=false)
abstract class SodiumVegetationMixin {
    @Inject(method="renderModel",at=@At("HEAD"),cancellable=true,require=0)
    private void oxyPlant(BlockStateModel model,BlockState state,BlockPos pos,BlockPos origin,CallbackInfo ci){
        if(dev.emersonoxy.rpg.client.VegetationMotion.dynamic(pos,state))ci.cancel();
    }
}
