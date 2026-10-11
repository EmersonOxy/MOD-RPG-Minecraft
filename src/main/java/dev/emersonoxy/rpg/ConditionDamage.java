package dev.emersonoxy.rpg;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.*;
/** Exact native effect context distinguishes poison ticks from ordinary magic. */
public final class ConditionDamage {
    private static final ThreadLocal<Integer> CONTEXT=ThreadLocal.withInitial(()->0);
    private ConditionDamage(){}
    public static void enter(int color){CONTEXT.set(color);}
    public static void leave(){CONTEXT.remove();}
    public static int color(DamageSource source){int effect=CONTEXT.get();if(effect!=0)return effect;if(Bleeding.damage(source))return 0xEF4655;String type=source.typeHolder().unwrapKey().map(k->k.location().getPath()).orElse("");if(type.equals("pressure"))return 0x68CAF3;if(type.equals("illusion"))return 0xC88CF2;if(source.is(DamageTypeTags.IS_FIRE))return 0xFF9D45;if(source.is(DamageTypeTags.IS_FREEZING))return 0x77DDEE;if(source.is(DamageTypeTags.IS_DROWNING))return 0x669EF4;if(source.is(DamageTypes.WITHER))return 0xA986B5;if(source.is(DamageTypes.MAGIC)||source.is(DamageTypes.INDIRECT_MAGIC))return 0xC88CF2;if(source.is(DamageTypeTags.IS_FALL))return 0xBCC3D0;if(source.is(DamageTypeTags.IS_LIGHTNING))return 0xF4DC6E;return 0;}
}
