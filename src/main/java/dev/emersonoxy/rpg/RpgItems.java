package dev.emersonoxy.rpg;

import net.neoforged.neoforge.registries.*;
public final class RpgItems {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(OxyRpg.MOD_ID);
    public static final DeferredItem<net.minecraft.world.item.SpawnEggItem> GUARD_EGG=ITEMS.registerItem("village_guard_spawn_egg",net.minecraft.world.item.SpawnEggItem::new,p->p.spawnEgg(VillageGuards.GUARD.get()));
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
    public static final DeferredItem<AdventureItems.Weapon> FROSTBLADE=ITEMS.registerItem("frostblade",p->new AdventureItems.Weapon(p,true),p->p.sword(net.minecraft.world.item.ToolMaterial.IRON,3,-2.4f));
    public static final DeferredItem<AdventureItems.Weapon> EMBERBLADE=ITEMS.registerItem("emberblade",p->new AdventureItems.Weapon(p,false),p->p.sword(net.minecraft.world.item.ToolMaterial.DIAMOND,3,-2.6f));
    public static final DeferredItem<AdventureItems.Boots> TRAIL_BOOTS=ITEMS.registerItem("trail_boots",AdventureItems.Boots::new,p->p.humanoidArmor(net.minecraft.world.item.equipment.ArmorMaterials.IRON,net.minecraft.world.item.equipment.ArmorType.BOOTS));
    public static final DeferredItem<AdventureItems.Accessory> FOCUS_RING=accessory("focus_ring","ring",null,0,false);
    public static final DeferredItem<AdventureItems.Accessory> VIGOR_RING=accessory("vigor_ring","ring",net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,.08,true);
    public static final DeferredItem<AdventureItems.Accessory> WARD_AMULET=accessory("ward_amulet","necklace",net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,3,false);
    public static final DeferredItem<AdventureItems.Accessory> WANDERER_AMULET=accessory("wanderer_amulet","necklace",net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.08,true);
    public static final DeferredItem<AdventureItems.Remedy> BANDAGE=remedy("bandage",false);
    public static final DeferredItem<AdventureItems.Remedy> ANTIDOTE=remedy("antidote",true);
    private static DeferredItem<AdventureItems.Accessory> accessory(String name,String slot,net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,double amount,boolean multiplied){return ITEMS.registerItem(name,p->new AdventureItems.Accessory(p,slot,name,attribute,amount,multiplied?net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL:net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),p->p.stacksTo(1));}
    private static DeferredItem<AdventureItems.Remedy> remedy(String name,boolean antidote){return ITEMS.registerItem(name,p->new AdventureItems.Remedy(p,antidote),p->p.stacksTo(16).component(net.minecraft.core.component.DataComponents.CONSUMABLE,net.minecraft.world.item.component.Consumables.DEFAULT_DRINK).component(net.minecraft.core.component.DataComponents.USE_COOLDOWN,new net.minecraft.world.item.component.UseCooldown(8f,java.util.Optional.of(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oxyrpg","remedies")))));}
    private static final java.util.Map<RpgClass,DeferredItem<? extends net.minecraft.world.item.Item>> RELICS=new java.util.EnumMap<>(RpgClass.class);
    static {for(var c:RpgClass.values())if(c!=RpgClass.NONE){String name="relic_"+c.name().toLowerCase(java.util.Locale.ROOT);RELICS.put(c,c==RpgClass.ARQUEIRO_NOVO?ITEMS.registerItem(name,ClassRelicItem.HunterBow::new,p->p.stacksTo(1).durability(384)):ITEMS.registerItem(name,ClassRelicItem::new,p->p.stacksTo(1)));}}
    public static net.minecraft.world.item.Item relic(RpgClass c){return RELICS.get(c).get();}
    public static java.util.List<net.minecraft.world.item.Item> relics(){return RELICS.values().stream().map(v->(net.minecraft.world.item.Item)v.get()).toList();}
    private RpgItems(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){ITEMS.register(bus);CreativeGroups.register(bus);}
}
