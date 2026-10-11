package dev.emersonoxy.rpg;

import net.neoforged.neoforge.registries.*;
public final class RpgItems {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(OxyRpg.MOD_ID);
    public static final DeferredItem<AnimatedSigilItem> SIGIL=ITEMS.registerItem("sigil",AnimatedSigilItem::new,properties->properties.stacksTo(1));
    private RpgItems(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){ITEMS.register(bus);}
}
