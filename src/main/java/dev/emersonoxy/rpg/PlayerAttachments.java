package dev.emersonoxy.rpg;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import java.util.function.Supplier;

public final class PlayerAttachments {
    private static final Codec<AttributeStats> ATTRIBUTES=RecordCodecBuilder.create(i->i.group(
        Codec.INT.fieldOf("health").forGetter(AttributeStats::health),Codec.INT.fieldOf("damage").forGetter(AttributeStats::damage),Codec.INT.fieldOf("luck").forGetter(AttributeStats::luck),Codec.INT.fieldOf("armor").forGetter(AttributeStats::armor),Codec.INT.fieldOf("mana").forGetter(AttributeStats::mana)).apply(i,AttributeStats::new));
    public static final Codec<RpgProfile> PROFILE_CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.STRING.xmap(RpgClass::valueOf,RpgClass::name).fieldOf("class").forGetter(RpgProfile::rpgClass),
        Codec.INT.fieldOf("level").forGetter(RpgProfile::level),Codec.INT.fieldOf("xp").forGetter(RpgProfile::xp),Codec.INT.fieldOf("kills").forGetter(RpgProfile::kills),
        Codec.LONG.fieldOf("ready").forGetter(RpgProfile::abilityReadyAt),Codec.LONG.fieldOf("skills").forGetter(RpgProfile::skills),
        Codec.INT.fieldOf("questStage").forGetter(RpgProfile::questStage),Codec.INT.fieldOf("questFirst").forGetter(RpgProfile::questFirst),Codec.INT.fieldOf("questSecond").forGetter(RpgProfile::questSecond),
        Codec.LONG.fieldOf("readySecond").forGetter(RpgProfile::readySecond),Codec.LONG.fieldOf("readyThird").forGetter(RpgProfile::readyThird),
        ATTRIBUTES.fieldOf("attributes").forGetter(RpgProfile::attributes),Codec.STRING.fieldOf("attributeNodes").forGetter(RpgProfile::attributeNodes)).apply(i,RpgProfile::new));
    public record Stored(boolean migrated,RpgProfile profile){
        public static final MapCodec<Stored> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.BOOL.fieldOf("migrated").forGetter(Stored::migrated),PROFILE_CODEC.fieldOf("profile").forGetter(Stored::profile)).apply(i,Stored::new));
    }
    private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,OxyRpg.MOD_ID);
    public static final Supplier<AttachmentType<Stored>> PROFILE=TYPES.register("profile",()->AttachmentType.builder(()->new Stored(false,RpgProfile.NEW)).serialize(Stored.CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<DebugSettings>> DEBUG_SETTINGS=TYPES.register("debug_settings",()->AttachmentType.builder(()->DebugSettings.OFF).serialize(DebugSettings.CODEC).copyOnDeath().build());
    private PlayerAttachments(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){TYPES.register(bus);}
}
