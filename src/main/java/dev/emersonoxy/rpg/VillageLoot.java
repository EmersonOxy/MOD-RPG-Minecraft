package dev.emersonoxy.rpg;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.*;

/** Reduces only unopened vanilla village chest loot; other structures and inventories are untouched. */
public final class VillageLoot extends LootModifier {
    public static final MapCodec<VillageLoot> CODEC=RecordCodecBuilder.mapCodec(inst->codecStart(inst).and(Codec.floatRange(0,1).fieldOf("retention").forGetter(v->v.retention)).apply(inst,VillageLoot::new));
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS=DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,OxyRpg.MOD_ID);
    static {SERIALIZERS.register("village_loot",()->CODEC);}
    private final float retention;
    public VillageLoot(LootItemCondition[] conditions,float retention){super(conditions);this.retention=retention;}
    public static void register(net.neoforged.bus.api.IEventBus bus){SERIALIZERS.register(bus);}
    @Override public MapCodec<VillageLoot> codec(){return CODEC;}
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot,LootContext context){
        var id=context.getQueriedLootTableId();if(!id.getNamespace().equals("minecraft")||!id.getPath().startsWith("chests/village/"))return loot;
        // Thin individual items, not whole rolls: plentiful food is reduced as well as equipment.
        for(var stack:loot){int kept=0;for(int i=0;i<stack.getCount();i++)if(context.getRandom().nextFloat()<Math.clamp(retention*RpgConfig.value(RpgConfig.VILLAGE_LOOT,1),0,1))kept++;stack.setCount(kept);}
        loot.removeIf(ItemStack::isEmpty);return loot;
    }
}
