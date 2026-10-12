package dev.emersonoxy.rpg.mixin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(AbstractArrow.class)
public interface GuidedArrowInvoker {
    @Invoker("onHitEntity") void oxyGuidedHit(EntityHitResult hit);
}
