package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Class-bound variants of native items. Receipt and pending inventory share the player's saved NBT. */
public final class ClassEquipment {
    private static final String KEY="oxy_class_equipment",RECEIPT="oxyrpg_kits";
    private static final Map<UUID,RpgProfile> REAL=new HashMap<>();
    private ClassEquipment(){}
    private static CompoundTag tag(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);}
    public static CompoundTag identity(ItemStack stack){return tag(stack);}
    public static boolean gear(ItemStack stack){var data=tag(stack);String part=data.getStringOr("part","");return part.equals("weapon")||part.equals("armor");}
    private static RpgClass selected(Player p){return p instanceof ServerPlayer s?(RpgDebug.testing(s)?RpgDebug.effective(s,REAL.getOrDefault(s.getUUID(),RpgAbilities.profile(s))):REAL.getOrDefault(s.getUUID(),RpgAbilities.profile(s))).rpgClass():EquipmentAccess.clientSelected();}
    private static int level(Player p){return p instanceof ServerPlayer s?(RpgDebug.testing(s)?100:REAL.getOrDefault(s.getUUID(),RpgAbilities.profile(s)).level()):EquipmentAccess.clientLevel();}
    public static int stage(Player p){int arsenal=p instanceof ServerPlayer s?Professions.level(s,2):EquipmentAccess.clientProfessionLevel(2);return level(p)>=50&&arsenal>=16?3:level(p)>=20&&arsenal>=8?2:1;}
    public static boolean allowed(Player p,ItemStack stack){
        if(!gear(stack))return true;var data=tag(stack);int rank=data.getIntOr("stage",1);
        return data.getStringOr("owner","").equals(p.getUUID().toString())&&data.getIntOr("class",0)==selected(p).ordinal()&&rank>=1&&rank<=stage(p);
    }
    public static String denial(ServerPlayer p,ItemStack stack){return allowed(p,stack)?"":"Equipamento exclusivo do dono/classe; etapas exigem nível 20 + Arsenal 8 ou nível 50 + Arsenal 16.";}
    public static int active(Player p,RpgClass c){
        if(selected(p)!=c)return 0;int result=0;
        for(var slot:new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.FEET}){
            var stack=p.getItemBySlot(slot);var identity=tag(stack);boolean held=slot==EquipmentSlot.MAINHAND||slot==EquipmentSlot.OFFHAND;
            boolean activePart=identity.getStringOr("part","").equals(held?"weapon":"armor");
            if(activePart&&gear(stack)&&allowed(p,stack))result=Math.max(result,identity.getIntOr("stage",1));
        }return result;
    }
    public static int discount(Player p,RpgClass c,int base){int grade=active(p,c);double reduction=c==RpgClass.MAGO?grade==3?.2:grade*.05:c==RpgClass.XAMA&&grade==3?.1:0;return base==0?0:Math.max(1,(int)Math.floor(base*(1-reduction)));}
    public static int frost(ServerPlayer p){int grade=active(p,RpgClass.MAGO);return grade>=2?(grade-1)*20:0;}
    public static float summonHealth(ServerPlayer p){return 1+active(p,RpgClass.NECROMANTE)*.1f;}
    public static float summonDamage(ServerPlayer p){return 1+Math.max(0,active(p,RpgClass.NECROMANTE)-1)*.05f;}
    public static int manaBonus(ServerPlayer p){var head=p.getItemBySlot(EquipmentSlot.HEAD);return gear(head)&&allowed(p,head)&&tag(head).getStringOr("part","").equals("armor")&&selected(p)==RpgClass.MAGO?20:0;}
    public static void onProfile(ServerPlayer p,RpgProfile real){REAL.put(p.getUUID(),real);if(real.rpgClass()!=RpgClass.NONE&&p.isAlive())progress(p,real);}
    private static CompoundTag receipt(ServerPlayer p){var root=p.getPersistentData();var persisted=root.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);var receipt=persisted.getCompoundOrEmpty(RECEIPT);persisted.put(RECEIPT,receipt);root.put(Player.PERSISTED_NBT_TAG,persisted);return receipt;}
    private static void enqueue(ListTag queue,RpgClass c,int stage,String part,int amount){var entry=new CompoundTag();entry.putInt("class",c.ordinal());entry.putInt("stage",stage);entry.putString("part",part);entry.putInt("amount",amount);queue.add(entry);}
    private static void progress(ServerPlayer p,RpgProfile real){
        if(RpgDebug.equipmentOverride(p)){boolean changed=false;for(int i=0;i<p.getInventory().getContainerSize();i++){var old=p.getInventory().getItem(i);var id=tag(old);int cls=id.getIntOr("class",0);if(gear(old)&&id.getIntOr("schema",0)<3&&id.getStringOr("owner","").equals(p.getUUID().toString())&&cls>0&&cls<RpgClass.values().length){p.getInventory().setItem(i,build(p,RpgClass.values()[cls],id.getIntOr("stage",1),id.getStringOr("part","weapon"),old));changed=true;}}if(changed){p.getInventory().setChanged();p.inventoryMenu.broadcastFullState();}return;}
        RpgClass c=real.rpgClass();int stage=stage(p);var receipt=receipt(p);String key=c.name();int done=receipt.getIntOr(key,0);
        var queue=receipt.getListOrEmpty("pending");
        if((done&(1<<(stage-1)))==0){
            if(done==0)enqueue(queue,c,stage,"weapon",1);
            if(stage==3)enqueue(queue,c,stage,"armor",1);
            enqueue(queue,c,stage,"food",stage==1?8:stage==2?12:4);
            if(stage==3)enqueue(queue,c,stage,"travel_food",8);
            if(c==RpgClass.ARQUEIRO){enqueue(queue,c,stage,"ammo",stage==1?32:stage==2?48:32);if(stage==3)enqueue(queue,c,stage,"normal_arrow",1);}
            // Existing high-level profiles receive the eligible kit once, rather than every earlier package.
            receipt.putInt(key,done|((1<<stage)-1));receipt.put("pending",queue);
            p.displayClientMessage(Component.literal("[Oxy RPG] Kit "+c.label()+" — "+stageName(stage)+"! Libere espaço para receber os itens pendentes."),true);
        }
        boolean changed=false;
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            var old=p.getInventory().getItem(i);var data=tag(old);
            if(old.isEmpty()||!data.getStringOr("owner","").equals(p.getUUID().toString())||data.getIntOr("class",0)!=c.ordinal()||data.getIntOr("stage",1)>=stage&&data.getIntOr("schema",0)>=3)continue;
            String part=data.getStringOr("part","");if(!Set.of("weapon","armor","food","ammo","travel_food","normal_arrow").contains(part))continue;
            p.getInventory().setItem(i,build(p,c,stage,part,old));changed=true;
        }
        // Insert only into the 36 ordinary inventory slots, never into equipment or the cursor.
        for(int i=0;i<queue.size();){
            var entry=queue.getCompoundOrEmpty(i);if(entry.getIntOr("class",0)!=c.ordinal()||entry.getIntOr("stage",1)>stage){i++;continue;}
            int amount=entry.getIntOr("amount",0);String part=entry.getStringOr("part","");
            var stack=build(p,c,stage,part,new ItemStack(base(c,stage,part),Math.min(64,amount)));
            int before=stack.getCount();insert(p,stack);int delivered=before-stack.getCount();
            if(delivered>0){amount-=delivered;entry.putInt("amount",amount);changed=true;}
            if(amount<=0)queue.remove(i);else i++;
        }
        receipt.put("pending",queue);
        if(changed){p.getInventory().setChanged();p.inventoryMenu.broadcastFullState();if(p.containerMenu!=p.inventoryMenu)p.containerMenu.broadcastFullState();}
    }
    private static void insert(ServerPlayer p,ItemStack stack){
        for(int i=0;i<36&&!stack.isEmpty();i++){
            var existing=p.getInventory().getItem(i);
            if(existing.isEmpty())p.getInventory().setItem(i,stack.split(Math.min(stack.getCount(),stack.getMaxStackSize())));
            else if(ItemStack.isSameItemSameComponents(existing,stack)){int amount=Math.min(stack.getCount(),existing.getMaxStackSize()-existing.getCount());if(amount>0){existing.grow(amount);stack.shrink(amount);}}
        }
    }
    private static Item base(RpgClass c,int stage,String part){
        return switch(part){
            case "food"->stage==3?Items.GOLDEN_APPLE:stage==1?Items.BREAD:c==RpgClass.XAMA?Items.BAKED_POTATO:Items.COOKED_BEEF;
            case "travel_food"->Items.GOLDEN_CARROT;
            case "ammo"->stage==1?Items.ARROW:stage==2?Items.SPECTRAL_ARROW:Items.TIPPED_ARROW;
            case "normal_arrow"->Items.ARROW;
            case "armor"->switch(c){case GUERREIRO->Items.DIAMOND_CHESTPLATE;case ARQUEIRO->Items.DIAMOND_BOOTS;case MAGO,NECROMANTE->Items.LEATHER_HELMET;default->Items.LEATHER_CHESTPLATE;};
            default->switch(c){case GUERREIRO->stage==1?Items.WOODEN_SWORD:stage==2?Items.IRON_SWORD:Items.DIAMOND_SWORD;case ARQUEIRO->Items.BOW;case MAGO->Items.BLAZE_ROD;case NECROMANTE->Items.BOOK;case AVIARIO->stage==1?Items.FEATHER:stage==2?Items.BREEZE_ROD:Items.MACE;case ILUSIONISTA->Items.AMETHYST_SHARD;case HIDROMANTE->Items.PRISMARINE_SHARD;case PIROMANTE->Items.MAGMA_CREAM;default->Items.AMETHYST_SHARD;};
        };
    }
    public static String stageName(int stage){return stage==1?"Básico":stage==2?"Intermediário":"Avançado";}
    private static ItemStack build(ServerPlayer p,RpgClass c,int stage,String part,ItemStack old){
        double wear=old.getMaxDamage()>0?(double)old.getDamageValue()/old.getMaxDamage():0;
        var nativeItem=base(c,stage,part);
        var stack=old.transmuteCopy(part.equals("weapon")?RpgItems.relic(c):nativeItem,old.getCount());
        if(part.equals("weapon")){var attributes=new ItemStack(nativeItem).get(DataComponents.ATTRIBUTE_MODIFIERS);if(attributes!=null)stack.set(DataComponents.ATTRIBUTE_MODIFIERS,attributes);stack.set(DataComponents.ITEM_MODEL,ResourceLocation.withDefaultNamespace(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(nativeItem).getPath()));}
        if(stack.getMaxDamage()>0)stack.setDamageValue(Math.min(stack.getMaxDamage()-1,(int)(stack.getMaxDamage()*wear)));
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var identity=new CompoundTag();identity.putString("owner",p.getUUID().toString());identity.putInt("class",c.ordinal());identity.putInt("stage",stage);identity.putInt("schema",3);identity.putString("part",part);data.put(KEY,identity);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        String name=part.equals("weapon")?switch(c){case GUERREIRO->"Espada do Gladiador";case ARQUEIRO->"Arco do Caçador";case MAGO->"Cajado Glacial";case NECROMANTE->"Grimório dos Mortos";case AVIARIO->stage==1?"Pluma de Resgate":stage==2?"Bastão das Correntes":"Martelo dos Céus";case ILUSIONISTA->"Espelho de Bolso";case HIDROMANTE->"Gota do Abismo";case PIROMANTE->"Brasa da Caldeira";default->"Totem da Tempestade";}:part.equals("armor")?switch(c){case GUERREIRO->"Peitoral do Gladiador";case ARQUEIRO->"Botas do Vento";case MAGO->"Capuz Glacial";case NECROMANTE->"Coroa dos Mortos";case AVIARIO->"Manto das Alturas";default->"Manto da Tempestade";}:part.equals("ammo")?"Flechas do Caçador":part.equals("normal_arrow")?"Flecha de Infinidade":"Provisões de "+c.label();
        stack.set(DataComponents.CUSTOM_NAME,Component.literal(name+" • "+stageName(stage)).withStyle(stage==3?ChatFormatting.LIGHT_PURPLE:stage==2?ChatFormatting.AQUA:ChatFormatting.WHITE));
        var lore=new ArrayList<Component>();
        if(part.equals("weapon")||part.equals("armor")){
            stack.set(DataComponents.MAX_STACK_SIZE,1);
            lore.add(Component.literal("Exclusivo de "+p.getName().getString()+" • "+c.label()));
            lore.add(Component.literal(stage==1?"Etapa inicial da classe":stage==2?"Nível 20 + Arsenal 8":"Nível 50 + Arsenal 16"));
            lore.add(Component.literal("Vinculado: retorna após a morte; efeito recupera em 60 s."));
            if(part.equals("weapon")){
                lore.add(Component.literal(ClassRelics.effect(c)));
                lore.add(Component.literal("Botão direito: 10 MP, recarga "+(40-stage*5)+" s."));
                stack.set(DataComponents.USE_COOLDOWN,new UseCooldown(.05f,Optional.of(ResourceLocation.fromNamespaceAndPath("oxyrpg","class_item_"+c.name().toLowerCase(Locale.ROOT)))));stack.set(DataComponents.UNBREAKABLE,Unit.INSTANCE);stack.set(DataComponents.DAMAGE_RESISTANT,new DamageResistant(net.minecraft.tags.DamageTypeTags.IS_FIRE));
                if(c==RpgClass.GUERREIRO){lore.add(Component.literal("Dano do kit +"+(stage*5)+"%; Corte Circular ganha alcance."));if(stage>=2)enchant(p,stack,Enchantments.SHARPNESS,stage==2?2:4);if(stage==3)enchant(p,stack,Enchantments.LOOTING,2);}
                if(c==RpgClass.ARQUEIRO){if(stage>=2){enchant(p,stack,Enchantments.POWER,stage==2?2:4);enchant(p,stack,Enchantments.PUNCH,1);}if(stage==3)enchant(p,stack,Enchantments.INFINITY,1);lore.add(Component.literal("Flechas e Flecha Espectral: dano +"+(stage*5)+"%."));if(stage==3)lore.add(Component.literal("Infinidade funciona com flechas normais; especiais são consumidas."));}
                if(c==RpgClass.MAGO){stack.set(DataComponents.ITEM_MODEL,ResourceLocation.withDefaultNamespace(stage==1?"blaze_rod":stage==2?"breeze_rod":"end_rod"));lore.add(Component.literal("Mana -"+(stage==3?20:stage*5)+"%; gelo +"+(Math.max(0,stage-1))+" s."));}
                if(c==RpgClass.NECROMANTE){lore.add(Component.literal("Vida das invocações +"+(stage*10)+"%; dano +"+(Math.max(0,stage-1)*5)+"%."));stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,stage>=2);}
                if(c==RpgClass.AVIARIO&&stage==3){lore.add(Component.literal("Dano do kit +15%."));}
                if(c==RpgClass.XAMA){stack.set(DataComponents.ITEM_MODEL,ResourceLocation.withDefaultNamespace("totem_of_undying"));lore.add(Component.literal("Raios e cura +"+(stage*5)+"%. Use na mão secundária para atacar com punhos."));lore.add(Component.literal("Totem visual: não ressuscita nem substitui mãos livres do Especialista."));}
            }else{
                lore.add(Component.literal("Mantém bônus do kit quando equipado."));
                enchant(p,stack,Enchantments.UNBREAKING,3);enchant(p,stack,Enchantments.PROTECTION,2);
                if(c==RpgClass.ARQUEIRO){enchant(p,stack,Enchantments.FEATHER_FALLING,4);lore.add(Component.literal("Queda lenta ao cair."));}
                if(c==RpgClass.MAGO){stack.set(DataComponents.DYED_COLOR,new DyedItemColor(0x83D9ED));lore.add(Component.literal("+20 de mana máxima quando equipado."));}
                if(c==RpgClass.AVIARIO)stack.set(DataComponents.DYED_COLOR,new DyedItemColor(0xC9DDE8));
                if(c==RpgClass.NECROMANTE)stack.set(DataComponents.DYED_COLOR,new DyedItemColor(0x653399));
                if(c==RpgClass.XAMA){stack.set(DataComponents.DYED_COLOR,new DyedItemColor(0x4F985B));lore.add(Component.literal("Bônus do kit mesmo com mãos vazias; mana -10%."));}
            }
        }else lore.add(Component.literal("Provisão de etapa; não é renovada ao relogar ou resetar."));
        if(part.equals("ammo")&&stage==3)stack.set(DataComponents.POTION_CONTENTS,new PotionContents(Optional.empty(),Optional.empty(),List.of(new MobEffectInstance(MobEffects.SLOWNESS,100,0)),Optional.empty()));
        stack.set(DataComponents.LORE,new ItemLore(lore));return stack;
    }
    private static void enchant(ServerPlayer p,ItemStack stack,net.minecraft.resources.ResourceKey<Enchantment> key,int amount){var holder=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);var old=stack.getOrDefault(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY);stack.enchant(holder,Math.max(amount,old.getLevel(holder)));}
    public static void damage(LivingDamageEvent.Pre event){if(Bleeding.damage(event.getSource()))return;
        var owner=RpgNecromancer.owner(event.getSource());if(owner==null||RpgNecromancer.summonKill(event.getSource(),event.getEntity(),owner))return;
        RpgClass c=selected(owner);int grade=active(owner,c);
        if(c==RpgClass.GUERREIRO||c==RpgClass.AVIARIO||c==RpgClass.ARQUEIRO&&event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow)event.setNewDamage(event.getNewDamage()*(1+grade*.05f));
    }
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.tickCount%20!=0)return;var real=REAL.get(p.getUUID());if(real==null)return;if(real.rpgClass()==RpgClass.NONE&&!RpgDebug.equipmentOverride(p))return;progress(p,real);
        var boots=p.getItemBySlot(EquipmentSlot.FEET);if(real.rpgClass()==RpgClass.ARQUEIRO&&gear(boots)&&allowed(p,boots)&&tag(boots).getStringOr("part","").equals("armor")&&!p.onGround()&&p.getDeltaMovement().y<-.05&&!p.isCreative())p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0,false,false));
    }
    public static void request(ServerPlayer p){if(RpgDebug.testing(p)){var choice=RpgAbilities.profile(p).rpgClass();for(int i=0;i<36;i++){var item=p.getInventory().getItem(i);var id=tag(item);if(gear(item)&&id.getIntOr("class",0)==choice.ordinal()&&id.getStringOr("part","").equals("weapon")){p.displayClientMessage(Component.literal("[Oxy RPG] Seu item de teste já está no inventário."),true);return;}}var stack=build(p,choice,3,"weapon",new ItemStack(base(choice,3,"weapon")));insert(p,stack);if(!stack.isEmpty())p.displayClientMessage(Component.literal("[Oxy RPG] Abra um espaço e solicite o kit novamente."),true);else{p.getInventory().setChanged();p.inventoryMenu.broadcastFullState();p.displayClientMessage(Component.literal("[Oxy RPG] Item de teste recebido. Continua exclusivo dessa classe."),true);}return;}var real=REAL.get(p.getUUID());if(real!=null&&real.rpgClass()!=RpgClass.NONE){progress(p,real);p.displayClientMessage(Component.literal("[Oxy RPG] Kit "+stageName(stage(p))+". Etapas: nível 20/Arsenal 8; nível 50/Arsenal 16. Itens pendentes aguardam espaço. Provisões não são repetidas."),true);}}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p)REAL.remove(p.getUUID());}
    public static void stop(ServerStoppedEvent event){REAL.clear();}
}
