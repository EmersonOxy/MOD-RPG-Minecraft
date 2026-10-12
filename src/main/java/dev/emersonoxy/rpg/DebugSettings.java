package dev.emersonoxy.rpg;

import java.util.List;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Saved development settings, deliberately separate from the real progression attachment. */
public record DebugSettings(int mode,int level,RpgClass rpgClass,long skills,boolean resources,
                            boolean cooldowns,boolean attributes,boolean arsenal,List<Long> ready,List<Long> items) {
    public static final DebugSettings OFF=new DebugSettings(0,1,RpgClass.NONE,0,false,true,false,false,List.of(),List.of());
    public DebugSettings {
        ready=List.copyOf(ready);items=List.copyOf(items);
        if(mode<0||mode>2||level<1||level>100||rpgClass==null||mode!=0&&rpgClass==RpgClass.NONE
            ||(skills&~SkillTree.PAID_MASK)!=0||ready.size()>192||items.size()>64
            ||ready.stream().anyMatch(n->n<0)||items.stream().anyMatch(n->n<0))throw new IllegalArgumentException("Configuração de debug inválida");
    }
    public static final MapCodec<DebugSettings> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Codec.INT.optionalFieldOf("mode",0).forGetter(DebugSettings::mode),
        Codec.INT.optionalFieldOf("level",1).forGetter(DebugSettings::level),
        Codec.STRING.xmap(RpgClass::valueOf,RpgClass::name).optionalFieldOf("class",RpgClass.NONE).forGetter(DebugSettings::rpgClass),
        Codec.LONG.optionalFieldOf("skills",0L).forGetter(DebugSettings::skills),
        Codec.BOOL.optionalFieldOf("resources",false).forGetter(DebugSettings::resources),
        Codec.BOOL.optionalFieldOf("cooldowns",true).forGetter(DebugSettings::cooldowns),
        Codec.BOOL.optionalFieldOf("attributes",false).forGetter(DebugSettings::attributes),
        Codec.BOOL.optionalFieldOf("arsenal",false).forGetter(DebugSettings::arsenal),
        Codec.LONG.listOf().optionalFieldOf("ready",List.of()).forGetter(DebugSettings::ready),
        Codec.LONG.listOf().optionalFieldOf("items",List.of()).forGetter(DebugSettings::items)
    ).apply(i,DebugSettings::new));
}
