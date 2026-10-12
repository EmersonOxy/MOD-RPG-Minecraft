package dev.emersonoxy.rpg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
public final class VillageGuards {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,"oxyrpg");
    public static final DeferredHolder<EntityType<?>,EntityType<VillageGuard>> GUARD=TYPES.register("village_guard",()->EntityType.Builder.of(VillageGuard::new,MobCategory.CREATURE).sized(.6f,1.95f).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("oxyrpg","village_guard"))));
    private VillageGuards(){}
    public static void register(net.neoforged.bus.api.IEventBus bus){TYPES.register(bus);bus.addListener(VillageGuards::attributes);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(VillageGuards::tick);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(GUARD.get(),Villager.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,40).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,6).build());}
    private static void tick(EntityTickEvent.Post e){
        if(!(e.getEntity() instanceof Villager v)||v instanceof VillageGuard||v.isBaby()||!v.isAlive()||!(v.level() instanceof ServerLevel w)||v.tickCount%200!=0||!w.isVillage(v.blockPosition()))return;
        var tag=v.getPersistentData();if(w.getGameTime()<tag.getLongOr("oxy_guard_check",0))return;tag.putLong("oxy_guard_check",w.getGameTime()+1200);
        if(w.getEntitiesOfClass(VillageGuard.class,v.getBoundingBox().inflate(40)).size()>=2)return;
        var at=net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(v,6,2);if(at==null)return;
        var guard=GUARD.get().create(w,EntitySpawnReason.TRIGGERED);if(guard==null)return;guard.setPos(at);if(!w.noCollision(guard))return;
        guard.getPersistentData().putDouble("oxy_guard_x",v.getX());guard.getPersistentData().putDouble("oxy_guard_y",v.getY());guard.getPersistentData().putDouble("oxy_guard_z",v.getZ());
        guard.setCustomName(net.minecraft.network.chat.Component.literal("Guarda da aldeia"));guard.setCustomNameVisible(true);guard.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));w.addFreshEntity(guard);
    }
}
