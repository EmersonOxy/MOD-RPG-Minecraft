package dev.emersonoxy.rpg;

import net.neoforged.neoforge.registries.*;
public final class RpgItems {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(OxyRpg.MOD_ID);
    public static final DeferredItem<AnimatedSigilItem> SIGIL=ITEMS.registerItem("sigil",AnimatedSigilItem::new,properties->properties.stacksTo(1));
    public static final DeferredItem<ManaPotionItem> MANA_POTION=manaPotion("mana_potion",35);
    public static final DeferredItem<ManaPotionItem> GREATER_MANA_POTION=manaPotion("greater_mana_potion",80);
    public static final DeferredItem<ManaPotionItem> SUPERIOR_MANA_POTION=manaPotion("superior_mana_potion",160);
    private static DeferredItem<ManaPotionItem> manaPotion(String name,int mana){
        return ITEMS.registerItem(name,p->new ManaPotionItem(p,mana),p->p.stacksTo(16)
            .component(net.minecraft.core.component.DataComponents.CONSUMABLE,net.minecraft.world.item.component.Consumables.DEFAULT_DRINK)
            .usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE)
            .component(net.minecraft.core.component.DataComponents.USE_COOLDOWN,new net.minecraft.world.item.component.UseCooldown(2f,java.util.Optional.of(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oxyrpg","mana_potions")))));
    }
    private static final java.util.Map<RpgClass,DeferredItem<? extends net.minecraft.world.item.Item>> RELICS=new java.util.EnumMap<>(RpgClass.class);
    static {for(var c:RpgClass.values())if(c!=RpgClass.NONE){String name="relic_"+c.name().toLowerCase(java.util.Locale.ROOT);RELICS.put(c,c==RpgClass.ARQUEIRO?ITEMS.registerItem(name,ClassRelicItem.HunterBow::new,p->p.stacksTo(1).durability(384)):ITEMS.registerItem(name,ClassRelicItem::new,p->p.stacksTo(1)));}}
    public static net.minecraft.world.item.Item relic(RpgClass c){return RELICS.get(c).get();}
    private static void creative(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey()==net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS){event.accept(MANA_POTION);event.accept(GREATER_MANA_POTION);event.accept(SUPERIOR_MANA_POTION);}
    }
    private RpgItems(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){ITEMS.register(bus);bus.addListener(RpgItems::creative);}
}
