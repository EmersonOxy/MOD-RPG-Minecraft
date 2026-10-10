package dev.emersonoxy.rpg.mixin;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value=Creeper.class,remap=false)
public interface CreeperRadiusAccessor {
    @Accessor("explosionRadius") int oxyrpg$getExplosionRadius();
    @Accessor("explosionRadius") void oxyrpg$setExplosionRadius(int radius);
}
