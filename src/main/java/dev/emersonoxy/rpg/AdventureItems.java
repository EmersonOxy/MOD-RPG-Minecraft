package dev.emersonoxy.rpg;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
/** Exploration equipment: modest passive bonuses and deliberate, paid right-click actions. */
public final class AdventureItems {
    private AdventureItems(){}
    public static boolean equipped(LivingEntity entity,Item item){return CuriosApi.getCuriosInventory(entity).flatMap(h->h.findFirstCurio(item)).isPresent();}
    public static int manaBonus(ServerPlayer p){return equipped(p,RpgItems.FOCUS_RING.get())?30:0;}
    public static void tooltip(Consumer<Component> lines,String key){lines.accept(Component.translatable("tooltip.oxyrpg."+key).withStyle(ChatFormatting.AQUA));}
    public static final class Accessory extends Item implements ICurioItem {
        private final String slot,description;private final Holder<Attribute> attribute;private final double amount;private final AttributeModifier.Operation operation;
        public Accessory(Properties p,String slot,String description,Holder<Attribute> attribute,double amount,AttributeModifier.Operation operation){super(p);this.slot=slot;this.description=description;this.attribute=attribute;this.amount=amount;this.operation=operation;}
        @Override public boolean canEquip(SlotContext context,ItemStack stack){return ICurioItem.super.canEquip(context,stack)&&context.identifier().equals(slot)&&!equipped(context.entity(),this);}
        @Override public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack){return attribute==null?CurioAttributeModifiers.EMPTY:CurioAttributeModifiers.EMPTY.withModifierAdded(attribute,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("oxyrpg",description),amount,operation),slot);}
        @Override public void appendHoverText(ItemStack stack,TooltipContext ctx,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag){tooltip(lines,description);}
    }
    public static final class Weapon extends Item {
        private final boolean frost;
        public Weapon(Properties p,boolean frost){super(p);this.frost=frost;}
        @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
            if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
            var stack=p.getItemInHand(hand);if(!RpgDebug.cooldownsDisabled(p)&&p.getCooldowns().isOnCooldown(stack)||!EquipmentAccess.allowed(p,stack)||RpgSpecialists.intangible(p))return InteractionResult.FAIL;
            int cost=frost?18:24;if(!RpgResources.affordable(p,cost)){p.displayClientMessage(Component.translatable("message.oxyrpg.item_mana",cost),true);return InteractionResult.FAIL;}
            RpgResources.spend(p,cost);if(!RpgDebug.cooldownsDisabled(p))p.getCooldowns().addCooldown(stack,frost?160:220);
            var start=p.getEyePosition();var direction=p.getLookAngle();double length=frost?7:10;var end=start.add(direction.scale(length));
            var hit=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));end=hit.getLocation();
            var world=p.level();var bounds=new AABB(start,end).inflate(frost?1.3:.9);int rank=RpgAbilities.profile(p).level();
            for(var mob:level.getEntitiesOfClass(Mob.class,bounds,RpgAbilities::attackable)){
                var point=mob.getBoundingBox().getCenter();double along=point.subtract(start).dot(direction);if(along<0||along>start.distanceTo(end)||point.distanceToSqr(start.add(direction.scale(along)))>(frost?3:2)||!p.hasLineOfSight(mob))continue;
                mob.hurtServer(world,p.damageSources().playerAttack(p),(frost?5:7)+rank*.08f);
                if(frost){mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,1));mob.knockback(.5,p.getX()-mob.getX(),p.getZ()-mob.getZ());}else mob.igniteForSeconds(4);
            }
            var particle=frost?net.minecraft.core.particles.ParticleTypes.SNOWFLAKE:net.minecraft.core.particles.ParticleTypes.FLAME;
            for(double t=.5;t<start.distanceTo(end);t+=.4){var point=start.add(direction.scale(t));world.sendParticles(particle,point.x,point.y,point.z,2,.12,.12,.12,.01);}
            return InteractionResult.SUCCESS;
        }
        @Override public void appendHoverText(ItemStack stack,TooltipContext ctx,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag){tooltip(lines,frost?"frostblade":"emberblade");}
    }
    public static final class Remedy extends Item {
        private final boolean antidote;
        public Remedy(Properties p,boolean antidote){super(p);this.antidote=antidote;}
        @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){if(entity instanceof ServerPlayer p){if(antidote){p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WEAKNESS);}else p.heal(4);}return super.finishUsingItem(stack,level,entity);}
        @Override public void appendHoverText(ItemStack stack,TooltipContext ctx,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag){tooltip(lines,antidote?"antidote":"bandage");}
    }
    public static final class Boots extends Item {
        public Boots(Properties p){super(p);}
        @Override public void appendHoverText(ItemStack stack,TooltipContext ctx,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag){tooltip(lines,"trail_boots");}
    }
    public static void fall(net.neoforged.neoforge.event.entity.living.LivingFallEvent event){if(event.getEntity().getItemBySlot(EquipmentSlot.FEET).is(RpgItems.TRAIL_BOOTS.get()))event.setDamageMultiplier(event.getDamageMultiplier()*.85f);}
}
