package dev.emersonoxy.rpg;

import java.io.IOException;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.LoggerFactory;

public final class VillagerQuests {
    public record Quest(String title,String dialogue,String target,int amount,int area,int emeralds,int kind){}
    private static final Quest[] QUESTS={
        new Quest("Guarda da aldeia","Zumbis rondam nossas casas. Pode nos ajudar?","minecraft:zombie",6,2,6,0),
        new Quest("Arqueiros da noite","Esqueletos assustam os moradores. Afaste-os da região.","minecraft:skeleton",5,2,6,0),
        new Quest("Teias na estrada","Precisamos viajar em segurança. Cuide das aranhas.","minecraft:spider",6,2,5,0),
        new Quest("Pão para todos","Nossa despensa está vazia. Traga trigo para o pão.","minecraft:wheat",16,1,4,1),
        new Quest("Sopa da aldeia","Você consegue trazer algumas cenouras?","minecraft:carrot",12,1,5,1),
        new Quest("Reparos da oficina","Precisamos de ferro para consertar nossas ferramentas.","minecraft:iron_ingot",8,0,8,1),
        new Quest("Colheita cuidadosa","Colha oito pés de trigo maduro depois de aceitar.","minecraft:wheat",8,1,5,2)
    };
    public static final int COUNT=QUESTS.length;
    private record Conversation(UUID npc,long expires){}
    private static final Map<UUID,Conversation> TALKS=new HashMap<>();
    private static final Map<UUID,Map<Integer,Integer>> MARKS=new HashMap<>();
    private VillagerQuests(){}
    public static String journal(ProfessionStore.Data data){
        if(data.quest<0)return "Sem contrato de villager. Converse com um adulto.";
        var q=QUESTS[data.quest];return q.title()+": "+Math.min(q.amount(),data.progress)+"/"+q.amount()+". Volte ao villager contratante; "+q.emeralds()+" esmeraldas.";
    }
    public static String journal(ServerPlayer p,ProfessionStore.Data data){
        return journal(data);
    }
    private static int offer(Villager v,ProfessionStore.Data d){return Math.floorMod(v.getUUID().hashCode()+d.completed,COUNT);}
    private static Item targetItem(Quest q){return BuiltInRegistries.ITEM.getValue(net.minecraft.resources.ResourceLocation.parse(q.target()));}
    public static void interact(PlayerInteractEvent.EntityInteract event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p)||!(event.getTarget() instanceof Villager v)||p.isShiftKeyDown()&&RpgAbilities.profile(p).rpgClass()!=RpgClass.VAMPIRESCO||v.isBaby()||!v.isAlive())return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        if(event.getHand()!=InteractionHand.MAIN_HAND)return;
        if(RpgAbilities.profile(p).rpgClass()==RpgClass.VAMPIRESCO&&VampireHunt.night(p.level())){Professions.notify(p,"Este morador teme sua presença durante a noite.");return;}
        if(v.getTradingPlayer()!=null&&v.getTradingPlayer()!=p){Professions.notify(p,"Este morador já está negociando.");return;}
        TALKS.put(p.getUUID(),new Conversation(v.getUUID(),p.level().getGameTime()+100));
        v.getNavigation().stop();v.setDeltaMovement(0,v.getDeltaMovement().y,0);
        try{
            var d=ProfessionStore.get(p);
            if(d.quest>=0&&v.getUUID().equals(d.villager)&&QUESTS[d.quest].kind()==1){
                var q=QUESTS[d.quest];var held=p.getMainHandItem();int remaining=Math.max(0,q.amount()-d.progress);
                if(remaining>0&&held.is(targetItem(q))){
                    int amount=Math.min(remaining,held.getCount());d.progress+=amount;
                    ProfessionStore.save(p,d);held.shrink(amount);p.getInventory().setChanged();p.inventoryMenu.broadcastFullState();
                    p.displayClientMessage(Component.literal("[Oxy RPG] Entregue: "+amount+" "+targetName(q)+" • "+d.progress+"/"+q.amount()+"."),true);
                }
                if(d.progress>=q.amount()){complete(p,v,d,q);return;}
                Professions.sync(p);
            }
        }catch(IOException e){Professions.notify(p,"Falha ao registrar entrega. Consulte o log.");LoggerFactory.getLogger(VillagerQuests.class).error("Falha na entrega ao villager",e);}
        show(p,v);
    }
    private static void show(ServerPlayer p,Villager v){try{
        var d=ProfessionStore.get(p);boolean mine=d.quest>=0&&v.getUUID().equals(d.villager);int id=mine?d.quest:offer(v,d);var q=QUESTS[id];
        int progress=mine?d.progress:0;
        int status=d.quest<0?0:!mine?3:progress>=q.amount()?2:1;
        String objective=(q.kind()==0?"Derrotar ":q.kind()==1?"Entregar ":"Colher ")+q.amount()+" "+targetName(q)+(mine?" • "+Math.min(progress,q.amount())+"/"+q.amount():"");
        String dialogue=status==3?"Você já tem um compromisso com outro morador. Conclua-o ou abandone pelo diário.":status==2?"Você cumpriu o objetivo? Confirme a conclusão para receber sua recompensa.":status==1?(q.kind()==1?"Segure o item na mão principal e clique em mim para entregar. Aceito entregas parciais.":"Ainda precisamos da sua ajuda. Volte quando terminar."):q.dialogue();
        PacketDistributor.sendToPlayer(p,new RpgNetwork.VillagerDialogue(v.getId(),v.getDisplayName().getString(),q.title(),dialogue,objective,q.emeralds()+" esmeraldas + 50 XP de "+Professions.NAMES[q.area()]+"; bônus a cada 5 contratos",status,!v.getOffers().isEmpty()));
    }catch(IOException e){Professions.notify(p,"Não foi possível ler o contrato. Seus dados foram preservados.");}}
    private static String targetName(Quest q){return q.kind()==0?BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.ResourceLocation.parse(q.target())).getDescription().getString():targetItem(q).getName().getString();}
    public static void frighten(Villager v){TALKS.entrySet().removeIf(e->e.getValue().npc().equals(v.getUUID()));}
    public static boolean conversing(Villager v){
        if(VampireHunt.afraid(v))return false;
        if(!(v.level() instanceof net.minecraft.server.level.ServerLevel level))return false;
        for(var entry:TALKS.entrySet()){
            var talk=entry.getValue();if(!talk.npc().equals(v.getUUID())||talk.expires()<level.getGameTime())continue;
            var p=level.getServer().getPlayerList().getPlayer(entry.getKey());if(p!=null&&p.level()==level&&p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(v)<=36)return true;
        }
        return false;
    }
    public static void action(ServerPlayer p,int action,int entityId){
        if((action==303||action==306)&&RpgAbilities.profile(p).rpgClass()==RpgClass.VAMPIRESCO&&VampireHunt.night(p.level())){Professions.notify(p,"Este morador teme sua presença durante a noite.");return;}
        if(action==302){try{var d=ProfessionStore.get(p);d.quest=-1;d.progress=0;d.villager=null;ProfessionStore.save(p,d);Professions.sync(p);Professions.notify(p,"Contrato abandonado, sem recompensa.");}catch(IOException e){Professions.notify(p,"Falha ao abandonar contrato.");}return;}
        var talk=TALKS.get(p.getUUID());var entity=p.level().getEntity(entityId);
        if(action==304){if(talk!=null&&entity!=null&&talk.npc().equals(entity.getUUID()))TALKS.remove(p.getUUID());return;}
        if((action==303||action==306)&&entity instanceof Villager candidate&&candidate.isAlive()&&!candidate.isBaby()&&p.distanceToSqr(candidate)<=36&&p.hasLineOfSight(candidate)&&p.isAlive()&&!p.isSpectator()){
            talk=new Conversation(candidate.getUUID(),p.level().getGameTime()+100);TALKS.put(p.getUUID(),talk);
        }
        if(talk==null||talk.expires()<p.level().getGameTime()||!(entity instanceof Villager v)||!v.isAlive()||v.isBaby()||!v.getUUID().equals(talk.npc())||p.distanceToSqr(v)>36||!p.hasLineOfSight(v)||!p.isAlive()||p.isSpectator())return;
        TALKS.put(p.getUUID(),new Conversation(v.getUUID(),p.level().getGameTime()+100));
        if(action==305)return;
        if(action==303){p.closeContainer();show(p,v);return;}
        if(action==306){
            if(v.getTradingPlayer()!=null&&v.getTradingPlayer()!=p){Professions.notify(p,"Este morador já está negociando.");return;}
            if(v.getOffers().isEmpty()){Professions.notify(p,"Este morador não tem trocas disponíveis.");show(p,v);return;}
            ((dev.emersonoxy.rpg.mixin.VillagerTradeInvoker)v).oxyStartTrading(p);return;
        }
        try{var d=ProfessionStore.get(p);
            if(action==300&&d.quest<0){d.quest=offer(v,d);d.villager=v.getUUID();d.progress=0;ProfessionStore.save(p,d);}
            else if(action==301&&v.getUUID().equals(d.villager)&&d.quest>=0){
                var q=QUESTS[d.quest];if(q.kind()==1||d.progress<q.amount()){show(p,v);return;}
                complete(p,v,d,q);return;
            }
            Professions.sync(p);show(p,v);
        }catch(IOException e){LoggerFactory.getLogger(VillagerQuests.class).error("Falha no contrato, sem recompensa",e);Professions.notify(p,"Não foi possível salvar o contrato; recompensa não entregue.");}
    }
    private static void complete(ServerPlayer p,Villager v,ProfessionStore.Data d,Quest q)throws IOException{
        // Persist completion before any reward, so repeated interaction cannot pay the same contract twice.
        d.quest=-1;d.progress=0;d.villager=null;d.completed++;ProfessionStore.save(p,d);
        give(p,new ItemStack(Items.EMERALD,q.emeralds()));
        if(d.completed%5==0)give(p,new ItemStack(d.completed%10==0?Items.DIAMOND:Items.GOLD_INGOT,d.completed%10==0?1:3));
        Professions.award(p,q.area(),50);Professions.sync(p);
        var world=p.level();
        world.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,v.getX(),v.getY()+1.4,v.getZ(),24,.6,.7,.6,.08);
        world.sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,v.getX(),v.getY()+1.8,v.getZ(),24,.5,.5,.5,.08);
        world.playSound(null,v.blockPosition(),net.minecraft.sounds.SoundEvents.VILLAGER_YES,net.minecraft.sounds.SoundSource.NEUTRAL,1,1.1f);
        world.playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,net.minecraft.sounds.SoundSource.PLAYERS,.6f,1.3f);
        PacketDistributor.sendToPlayer(p,new RpgNetwork.VillagerDialogue(v.getId(),v.getDisplayName().getString(),q.title(),"Muito obrigado! Seu trabalho ajudou nossa aldeia.","Missão concluída — todos os objetivos entregues/confirmados.",q.emeralds()+" esmeraldas + 50 XP de "+Professions.NAMES[q.area()],4,!v.getOffers().isEmpty()));
    }
    private static void give(ServerPlayer p,ItemStack stack){
        // Explicit survival-style insertion also preserves overflow in creative mode.
        for(int i=0;i<36&&!stack.isEmpty();i++){
            var existing=p.getInventory().getItem(i);
            if(existing.isEmpty()){int amount=Math.min(stack.getCount(),stack.getMaxStackSize());p.getInventory().setItem(i,stack.split(amount));}
            else if(ItemStack.isSameItemSameComponents(existing,stack)){int amount=Math.min(stack.getCount(),existing.getMaxStackSize()-existing.getCount());if(amount>0){existing.grow(amount);stack.shrink(amount);}}
        }
        if(!stack.isEmpty()){var item=p.drop(stack,false);if(item!=null){item.setTarget(p.getUUID());item.setNoPickUpDelay();}}
        p.getInventory().setChanged();p.inventoryMenu.broadcastFullState();
    }
    private static void advance(ServerPlayer p,String target,int kind){try{var d=ProfessionStore.get(p);if(d.quest<0)return;var q=QUESTS[d.quest];if(q.kind()!=kind||!q.target().equals(target)||d.progress>=q.amount())return;d.progress++;ProfessionStore.save(p,d);Professions.sync(p);}catch(IOException e){Professions.notify(p,"Não foi possível salvar o progresso do contrato.");}}
    public static void harvest(ServerPlayer p,BlockState state){advance(p,BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),2);}
    public static void death(LivingDeathEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof Enemy)||RpgNecromancer.isSummon(event.getEntity()))return;
        var p=RpgNecromancer.owner(event.getSource());if(p==null&&event.getEntity().getKillCredit() instanceof ServerPlayer credited)p=credited;
        if(p==null)return;EliteMobs.reward(p,event.getEntity());Professions.award(p,2,15);advance(p,BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString(),0);
    }
    private static int marker(Villager v,ProfessionStore.Data d){
        if(v.isBaby()||!v.isAlive())return 0;
        if(d.quest<0)return 1;
        return v.getUUID().equals(d.villager)?d.progress>=QUESTS[d.quest].amount()?3:2:0;
    }
    private static void mark(ServerPlayer p,Villager v,ProfessionStore.Data d,boolean force){
        int state=marker(v,d);var previous=MARKS.computeIfAbsent(p.getUUID(),id->new HashMap<>());
        if(force||!Objects.equals(previous.get(v.getId()),state)){previous.put(v.getId(),state);PacketDistributor.sendToPlayer(p,new RpgNetwork.VillagerMark(v.getId(),state));}
    }
    public static void syncMarkers(ServerPlayer p,ProfessionStore.Data d){for(var v:p.level().getEntitiesOfClass(Villager.class,p.getBoundingBox().inflate(32)))mark(p,v,d,false);}
    public static void tracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event){
        if(event.getEntity() instanceof ServerPlayer p&&event.getTarget() instanceof Villager v)try{mark(p,v,ProfessionStore.get(p),true);}catch(IOException e){Professions.notify(p,"Falha ao ler marcação da missão.");}
    }
    public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){TALKS.remove(p.getUUID());MARKS.remove(p.getUUID());}}
    public static void clear(){TALKS.clear();MARKS.clear();}
}
