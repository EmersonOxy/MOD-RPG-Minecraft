package dev.emersonoxy.rpg;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Only particle types are common-side; simulation and marks stay on the client. */
public final class RpgParticles {
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,OxyRpg.MOD_ID);
    public static final Supplier<SimpleParticleType> BLOOD=TYPES.register("blood",()->new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SMOKE=TYPES.register("smoke",()->new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> IMPACT=TYPES.register("impact",()->new SimpleParticleType(false));
    private RpgParticles(){}
    public static void register(IEventBus bus){TYPES.register(bus);}
}
