package dev.emersonoxy.rpg;

import java.util.*;
import java.nio.file.*;
import java.io.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.LoggerFactory;

public final class RpgResources {
    private static final Map<UUID, Pool> POOLS=new HashMap<>();
    private static final class Pool {float mana;long cast;RpgProfile profile;Pool(RpgProfile p){profile=p;mana=p.attributes().maxMana();}}
    private RpgResources(){}
    private static long now(ServerPlayer p){return p.level().getGameTime();}
    private static Path file(ServerPlayer p){return p.level().getServer().getWorldPath(LevelResource.ROOT).resolve("oxyrpg/resources/"+p.getUUID()+".properties");}
    private static Pool pool(ServerPlayer p){
        return POOLS.computeIfAbsent(p.getUUID(),id->{
            var value=new Pool(RpgProfile.NEW);
            if(Files.exists(file(p)))try(var in=Files.newBufferedReader(file(p))){
                var data=new Properties();data.load(in);
                value.mana=Math.max(0,Float.parseFloat(data.getProperty("mana","100")));
                long saved=Long.parseLong(data.getProperty("time","0"));value.cast=Long.parseLong(data.getProperty("cast","0"));
                value.mana+=Math.max(0,now(p)-Math.max(saved,value.cast+60))*.1f;
            }catch(Exception e){LoggerFactory.getLogger(RpgResources.class).warn("Falha ao ler recurso de mana, usando padrão",e);}
            return value;
        });
    }
    public static void update(ServerPlayer p,RpgProfile profile){
        var a=pool(p);a.profile=profile;a.mana=Math.min(a.mana,maximum(p,profile));
        modifier(p,Attributes.MAX_HEALTH,"attribute_health",profile.attributes().health()*2);
        modifier(p,Attributes.ARMOR,"attribute_armor",profile.attributes().armor()*.5);
        modifier(p,Attributes.LUCK,"attribute_luck",profile.attributes().luck()*.25);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attribute,String name,double amount){
        var value=p.getAttribute(attribute);if(value==null)return;
        var id=ResourceLocation.fromNamespaceAndPath("oxyrpg",name);var old=value.getModifier(id);
        if(old!=null&&old.amount()==amount)return;
        if(old!=null)value.removeModifier(id);
        if(amount!=0)value.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
    }
    public static int cost(RpgProfile profile,int slot,boolean extra){
        if(extra)return 5;
        return switch(profile.rpgClass()){
            case MAGO->profile.mainUnlocked(slot)?new int[]{20,30,35}[slot]:new int[]{8,8,10}[slot];
            case GUERREIRO->slot==0&&!profile.mainUnlocked(0)?0:new int[]{12,20,25}[slot];
            case ARQUEIRO->profile.mainUnlocked(slot)?new int[]{12,20,15}[slot]:new int[]{5,8,5}[slot];
            case NECROMANTE->profile.mainUnlocked(slot)?new int[]{20,40,35}[slot]:new int[]{10,15,15}[slot];
            case XAMA->profile.mainUnlocked(slot)?new int[]{20,35,20}[slot]:new int[]{8,15,10}[slot];
            default->0;
        };
    }
    public static boolean affordable(ServerPlayer p,int cost){return RpgDebug.enabled(p)||pool(p).mana>=cost;}
    public static void spend(ServerPlayer p,int cost){
        var a=pool(p);if(!RpgDebug.enabled(p)){a.mana=Math.max(0,a.mana-cost);if(cost>0)a.cast=now(p);save(p);}
    }
    public static void tick(ServerPlayer p){
        var a=POOLS.get(p.getUUID());if(a==null)return;
        if(!p.isAlive())return;
        if(now(p)>=a.cast+60)a.mana=Math.min(maximum(p,a.profile),a.mana+.1f);
        if(p.tickCount%20==0)sync(p);
    }
    public static void sync(ServerPlayer p){
        var a=pool(p);var s=a.profile.attributes();
        a.mana=Math.min(a.mana,maximum(p,a.profile));
        PacketDistributor.sendToPlayer(p,new RpgNetwork.Resources(a.profile.attributePoints(),s.health(),s.damage(),s.luck(),s.armor(),s.mana(),(int)a.mana,maximum(p,a.profile)));
    }
    public static void damage(LivingDamageEvent.Pre event){
        var p=RpgNecromancer.owner(event.getSource());
        if(p!=null){
            var a=POOLS.get(p.getUUID());if(a!=null)event.setNewDamage(event.getNewDamage()*(1+a.profile.attributes().damage()*.02f));
        }
    }
    public static void rareDrop(ServerPlayer p,LivingEntity mob){
        var a=pool(p);if(a.profile.attributes().luck()==0||!p.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))return;
        if(p.getRandom().nextFloat()>=a.profile.attributes().luck()*.002f)return;
        int roll=p.getRandom().nextInt(100);
        var item=roll<65?Items.IRON_INGOT:roll<90?Items.GOLD_INGOT:roll<99?Items.EMERALD:Items.DIAMOND;
        mob.level().addFreshEntity(new ItemEntity(mob.level(),mob.getX(),mob.getY()+.3,mob.getZ(),new ItemStack(item)));
    }
    private static void save(ServerPlayer p){
        var a=POOLS.get(p.getUUID());if(a==null)return;Path temp=null;
        try{
            Files.createDirectories(file(p).getParent());var data=new Properties();
            data.setProperty("mana",Float.toString(a.mana));data.setProperty("time",Long.toString(now(p)));data.setProperty("cast",Long.toString(a.cast));
            temp=Files.createTempFile(file(p).getParent(),p.getUUID().toString(),".tmp");
            try(var writer=Files.newBufferedWriter(temp)){data.store(writer,"Oxy RPG mana");}
            Files.move(temp,file(p),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){LoggerFactory.getLogger(RpgResources.class).error("Falha ao salvar mana",e);}
        finally{if(temp!=null)try{Files.deleteIfExists(temp);}catch(IOException ignored){}}
    }
    public static void reset(ServerPlayer p){var a=pool(p);a.mana=100;a.cast=0;save(p);}
    private static int maximum(ServerPlayer p,RpgProfile profile){return profile.attributes().maxMana()+ClassEquipment.manaBonus(p);}
    public static void clonePlayer(PlayerEvent.Clone event){if(event.getEntity() instanceof ServerPlayer p){var a=POOLS.get(p.getUUID());if(a!=null){update(p,a.profile);if(event.isWasDeath()){a.mana=maximum(p,a.profile);a.cast=0;}sync(p);}}}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){save(p);POOLS.remove(p.getUUID());}}
    public static void stop(ServerStoppedEvent event){POOLS.clear();}
}



