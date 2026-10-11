package dev.emersonoxy.rpg;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.LoggerFactory;

public final class Professions {
    public static final String[] NAMES={"Mineração","Agricultura","Arsenal"};
    private static final Map<UUID,Long> NOTICES=new HashMap<>();
    private static final Map<ServerLevel,Set<BlockPos>> PLACED=new HashMap<>();
    private static final Set<ServerLevel> FAILED=new HashSet<>();
    private Professions(){}
    public static void notify(ServerPlayer p,String text){long now=p.level().getGameTime();if(now<NOTICES.getOrDefault(p.getUUID(),0L))return;NOTICES.put(p.getUUID(),now+40);p.sendSystemMessage(Component.literal("[Oxy RPG] "+text));}
    public static int level(ServerPlayer p,int profession){if(RpgDebug.professionOverride(p,profession))return 20;try{return ProfessionStore.level(ProfessionStore.get(p).xp[profession]);}catch(IOException e){notify(p,"Falha ao ler profissões; arquivo preservado.");return 1;}}
    public static int tier(ItemStack stack){
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if(id.startsWith("netherite_"))return 16;if(id.startsWith("diamond_"))return 12;
        if(id.startsWith("iron_"))return 8;if(id.startsWith("stone_")||id.startsWith("chainmail_")||id.startsWith("copper_"))return 4;
        if(id.startsWith("golden_")||id.startsWith("gold_"))return 8;return 1;
    }
    public static int cropLevel(String id){return switch(id){
        case "beetroots","beetroot_seeds","sugar_cane","cactus"->4;case "carrots","potatoes","carrot","potato","bamboo"->8;
        case "melon","pumpkin","melon_seeds","pumpkin_seeds","melon_stem","pumpkin_stem","attached_melon_stem","attached_pumpkin_stem","brown_mushroom","red_mushroom"->12;
        case "nether_wart","cocoa","cocoa_beans","sweet_berries","sweet_berry_bush","torchflower_seeds","pitcher_pod","torchflower_crop","pitcher_crop"->16;default->1;
    };}
    public record Requirement(int area,int level){}
    public static Requirement requirement(ItemStack stack){
        if(stack.isEmpty())return new Requirement(-1,1);
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());if(!id.getNamespace().equals("minecraft"))return new Requirement(-1,1);
        String name=id.getPath();int profession=-1,required=1;
        if(stack.is(net.minecraft.tags.ItemTags.PICKAXES)||stack.is(net.minecraft.tags.ItemTags.SHOVELS)){profession=0;required=tier(stack);}
        else if(stack.is(net.minecraft.tags.ItemTags.HOES)){profession=1;required=tier(stack);}
        else if(stack.is(net.minecraft.tags.ItemTags.SWORDS)||stack.is(net.minecraft.tags.ItemTags.AXES)||name.endsWith("_helmet")||name.endsWith("_chestplate")||name.endsWith("_leggings")||name.endsWith("_boots")){profession=2;required=tier(stack);}
        else if(stack.is(Items.BOW)||stack.is(Items.SHIELD)){profession=2;required=4;}
        else if(stack.is(Items.CROSSBOW)){profession=2;required=8;}
        else if(stack.is(Items.TRIDENT)||stack.is(Items.MACE)){profession=2;required=16;}
        return new Requirement(profession,required);
    }
    public static String denial(ServerPlayer p,ItemStack stack,boolean use){
        if(ClassEquipment.gear(stack))return ClassEquipment.denial(p,stack);
        var required=requirement(stack);
        if(required.area()>=0&&level(p,required.area())<required.level())return NAMES[required.area()]+" nível "+required.level()+" necessário para usar "+stack.getHoverName().getString()+".";
        return "";
    }
    public static void award(ServerPlayer p,int area,int amount){
        if(p.isCreative()||p.isSpectator()||amount<=0||RpgDebug.professionOverride(p,area))return;
        try{var d=ProfessionStore.get(p);int old=ProfessionStore.level(d.xp[area]);if(old==20)return;
            d.xp[area]=Math.min(ProfessionStore.MAX_XP,d.xp[area]+amount);ProfessionStore.save(p,d);
            int next=ProfessionStore.level(d.xp[area]);if(next>old)p.sendSystemMessage(Component.literal("[Oxy RPG] "+NAMES[area]+" chegou ao nível "+next+(next==20?"! Bônus passivo desbloqueado.":"!")));
            sync(p);
        }catch(IOException e){LoggerFactory.getLogger(Professions.class).error("Progresso de profissão não foi salvo",e);notify(p,"Não foi possível salvar XP de profissão.");}
    }
    public static void sync(ServerPlayer p){try{var d=ProfessionStore.get(p);PacketDistributor.sendToPlayer(p,new RpgNetwork.ProfessionState(RpgDebug.professionXp(p,0,d.xp[0]),RpgDebug.professionXp(p,1,d.xp[1]),RpgDebug.professionXp(p,2,d.xp[2]),VillagerQuests.journal(p,d)));VillagerQuests.syncMarkers(p,d);ClassJourney.sync(p);}catch(IOException e){notify(p,"Não foi possível ler profissões.");}}
    private static Path placedFile(ServerLevel world){return world.getServer().getWorldPath(LevelResource.ROOT).resolve("oxyrpg/placed-"+world.dimension().location().toString().replace(':','_').replace('/','_')+".txt");}
    private static Set<BlockPos> placed(ServerLevel world){return PLACED.computeIfAbsent(world,w->{var set=new HashSet<BlockPos>();var file=placedFile(w);
        if(Files.exists(file))try{for(var line:Files.readAllLines(file)){String[] xyz=line.split(",");set.add(new BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2])));}}
        catch(Exception e){FAILED.add(w);LoggerFactory.getLogger(Professions.class).error("Registro de blocos colocados preservado",e);}return set;});}
    private static void savePlaced(ServerLevel world)throws IOException{
        var file=placedFile(world);Files.createDirectories(file.getParent());var tmp=Files.createTempFile(file.getParent(),"placed-",".tmp");
        try{Files.write(tmp,placed(world).stream().map(p->p.getX()+","+p.getY()+","+p.getZ()).toList());Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}
    }
    private static boolean mineable(net.minecraft.world.level.block.state.BlockState state){return state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)||state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL);}
    public static void placed(BlockEvent.EntityPlaceEvent event){
        if(event.isCanceled()||!(event.getLevel() instanceof ServerLevel world)||!(event.getEntity() instanceof ServerPlayer))return;
        if(!mineable(event.getPlacedBlock())&&event.getPlacedBlock().getBlock()!=Blocks.MELON&&event.getPlacedBlock().getBlock()!=Blocks.PUMPKIN)return;
        placed(world).add(event.getPos().immutable());if(FAILED.contains(world))return;
        try{savePlaced(world);}catch(IOException e){FAILED.add(world);LoggerFactory.getLogger(Professions.class).error("XP de mineração suspenso para preservar registro",e);}
    }
    public static void breakGuard(BlockEvent.BreakEvent event){
        if(event.isCanceled()||!(event.getPlayer() instanceof ServerPlayer p))return;
        String reason=denial(p,p.getMainHandItem(),false);if(!reason.isEmpty()){event.setCanceled(true);notify(p,reason);}
    }
    public static void leftBlock(PlayerInteractEvent.LeftClickBlock event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p))return;
        String reason=denial(p,p.getMainHandItem(),false);if(!reason.isEmpty()){event.setCanceled(true);notify(p,reason);}
    }
    public static void broken(BlockEvent.BreakEvent event){
        if(event.isCanceled()||!(event.getPlayer() instanceof ServerPlayer p)||!(event.getLevel() instanceof ServerLevel world))return;
        var state=event.getState();var block=state.getBlock();var pos=event.getPos().immutable();if(TemporaryBlocks.temporary(world,pos))return;var registry=placed(world);
        if(registry.remove(pos)){if(!FAILED.contains(world))try{savePlaced(world);}catch(IOException e){FAILED.add(world);}return;}
        if(FAILED.contains(world))return;
        // Maturity is checked before awarding harvest XP; planting itself never yields XP.
        if(block instanceof CropBlock crop&&crop.isMaxAge(state)||block==Blocks.NETHER_WART&&state.getValue(NetherWartBlock.AGE)==3
            ||block==Blocks.COCOA&&state.getValue(CocoaBlock.AGE)==2||block==Blocks.MELON||block==Blocks.PUMPKIN){award(p,1,15);VillagerQuests.harvest(p,state);}
        else if(mineable(state)&&!state.hasBlockEntity()){
            String id=BuiltInRegistries.BLOCK.getKey(block).getPath();award(p,0,id.contains("ore")?15:3);
        }
    }
    public static void rightBlock(PlayerInteractEvent.RightClickBlock event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p))return;
        var stack=event.getItemStack();String reason=ClassRules.denial(p,stack);if(reason.isEmpty())reason=denial(p,stack,true);
        if(stack.getItem() instanceof BlockItem){String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();int req=cropLevel(id);
            if(level(p,1)<req)reason="Agricultura nível "+req+" necessário para plantar "+stack.getHoverName().getString()+".";
        }
        if(!reason.isEmpty()){event.setCanceled(true);event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);notify(p,reason);}
    }
    public static void attack(AttackEntityEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p))return;
        String reason=ClassRules.denial(p,p.getMainHandItem());if(reason.isEmpty())reason=denial(p,p.getMainHandItem(),false);
        if(!reason.isEmpty()){event.setCanceled(true);notify(p,reason);}
    }
    public static void modifier(ServerPlayer p,Holder<Attribute> attribute,String name,double amount,AttributeModifier.Operation operation){
        var value=p.getAttribute(attribute);if(value==null)return;var id=ResourceLocation.fromNamespaceAndPath("oxyrpg",name);var old=value.getModifier(id);
        if(old!=null&&old.amount()==amount)return;if(old!=null)value.removeModifier(id);
        if(amount!=0)value.addTransientModifier(new AttributeModifier(id,amount,operation));
    }
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        refreshModifiers(p);
        if(ClassRules.selected(p)==RpgClass.ARQUEIRO&&p.getMainHandItem().is(Items.SHIELD))returnEquipment(p,EquipmentSlot.MAINHAND,"Arqueiros não equipam escudos.");
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.OFFHAND}){
            var stack=p.getItemBySlot(slot);String reason=ClassRules.denial(p,stack);if(reason.isEmpty())reason=denial(p,stack,true);
            if(!EquipmentAccess.allowed(p,stack)){
                returnEquipment(p,slot,reason);
            }
        }
        if(p.tickCount%100==0)sync(p);
    }
    public static void refreshModifiers(ServerPlayer p){
        ClassRules.tick(p);
        modifier(p,Attributes.MAX_HEALTH,"profession_health",level(p,1)==20?2:0,AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.BLOCK_BREAK_SPEED,"profession_mining",level(p,0)==20?.1:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.ATTACK_DAMAGE,"profession_arsenal",level(p,2)==20?.05:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    private static void returnEquipment(ServerPlayer p,EquipmentSlot slot,String reason){
        // Move the original stack exactly once into a specific empty main-inventory slot.
        // Inventory.add can route back into offhand and discards overflow in creative mode.
        var stack=p.getItemBySlot(slot);if(stack.isEmpty())return;
        int destination=-1;
        for(int i=0;i<36;i++){
            if(stack.is(Items.SHIELD)&&ClassRules.selected(p)==RpgClass.ARQUEIRO&&i==p.getInventory().getSelectedSlot())continue;
            if(p.getInventory().getItem(i).isEmpty()){destination=i;break;}
        }
        if(destination<0){notify(p,reason+" Libere espaço no inventário para retirar o equipamento; o item foi preservado.");return;}
        p.setItemSlot(slot,ItemStack.EMPTY);p.getInventory().setItem(destination,stack);p.getInventory().setChanged();
        p.inventoryMenu.broadcastFullState();if(p.containerMenu!=p.inventoryMenu)p.containerMenu.broadcastFullState();
        notify(p,reason+" Item devolvido ao inventário.");
    }
    public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p){OxyRpg.sync(p,"");sync(p);}}
    public static void stop(ServerStoppedEvent event){ProfessionStore.clear();PLACED.clear();FAILED.clear();NOTICES.clear();VillagerQuests.clear();}
}
