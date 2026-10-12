package dev.emersonoxy.rpg;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
public final class CreativeGroups {
    private static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"oxyrpg");
    private static final DeferredHolder<CreativeModeTab,CreativeModeTab> EQUIPMENT=TABS.register("equipment",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.oxyrpg.equipment")).icon(()->new ItemStack(RpgItems.FROSTBLADE.get())).displayItems((p,out)->{
        for(var item:List.of(RpgItems.FROSTBLADE.get(),RpgItems.EMBERBLADE.get(),RpgItems.TRAIL_BOOTS.get(),RpgItems.FOCUS_RING.get(),RpgItems.VIGOR_RING.get(),RpgItems.WARD_AMULET.get(),RpgItems.WANDERER_AMULET.get(),RpgItems.SIGIL.get(),RpgItems.GUARD_EGG.get()))out.accept(item);RpgItems.relics().forEach(out::accept);
    }).build());
    private static final DeferredHolder<CreativeModeTab,CreativeModeTab> SUPPLIES=TABS.register("supplies",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.oxyrpg.supplies")).icon(()->new ItemStack(RpgItems.MANA_POTION.get())).displayItems((p,out)->{for(var item:List.of(RpgItems.MANA_POTION.get(),RpgItems.GREATER_MANA_POTION.get(),RpgItems.SUPERIOR_MANA_POTION.get(),RpgItems.BANDAGE.get(),RpgItems.ANTIDOTE.get()))out.accept(item);}).build());
    private static final DeferredHolder<CreativeModeTab,CreativeModeTab> EXTRAS=TABS.register("complements",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.oxyrpg.complements")).icon(()->new ItemStack(Items.CHEST)).displayItems((p,out)->BuiltInRegistries.ITEM.stream().filter(item->{var name=BuiltInRegistries.ITEM.getKey(item).getNamespace();return !name.equals("minecraft")&&!name.equals("oxyrpg")&&item.isEnabled(p.enabledFeatures());}).sorted(Comparator.comparing(item->BuiltInRegistries.ITEM.getKey(item).toString())).forEach(out::accept)).build());
    private CreativeGroups(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){TABS.register(bus);bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST,CreativeGroups::vanilla);}
    private static void vanilla(BuildCreativeModeTabContentsEvent e){if(!e.getTabKey().location().getNamespace().equals("minecraft")||e.getTab().getType()!=CreativeModeTab.Type.CATEGORY)return;var all=new ArrayList<>(e.getParentEntries());for(var item:e.getSearchEntries())if(all.stream().noneMatch(s->ItemStack.isSameItemSameComponents(s,item)))all.add(item);for(var item:all)if(!BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace().equals("minecraft"))e.remove(item,CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);}
}
