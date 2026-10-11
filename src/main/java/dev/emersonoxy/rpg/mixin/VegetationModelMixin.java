package dev.emersonoxy.rpg.mixin;
import java.util.List;
import java.util.function.Function;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Cosmetic substitution only; collision, drops, saves and server block states are untouched. */
@Mixin(BlockRenderDispatcher.class)
abstract class VegetationModelMixin {
    @Inject(method="renderBatched",at=@At("HEAD"),cancellable=true)
    private void oxyBend(BlockState state,BlockPos pos,BlockAndTintGetter level,PoseStack pose,Function<ChunkSectionLayer,VertexConsumer> buffer,boolean sides,List<BlockModelPart> parts,CallbackInfo ci){if(dev.emersonoxy.rpg.client.VegetationMotion.dynamic(pos,state))ci.cancel();}
}
