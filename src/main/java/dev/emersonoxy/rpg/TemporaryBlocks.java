package dev.emersonoxy.rpg;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.LoggerFactory;

/** Only places in air. Durable expiry ledger prevents temporary blocks surviving a restart. */
public final class TemporaryBlocks {
    private record Entry(Block block,long until){}
    private static final Map<ServerLevel,Ledger> WORLDS=new HashMap<>();
    private static final class Ledger {
        final ServerLevel world;final Path file;final Map<BlockPos,Entry> entries=new HashMap<>();boolean failed;
        Ledger(ServerLevel world){
            this.world=world;
            file=world.getServer().getWorldPath(LevelResource.ROOT).resolve("oxyrpg/temporary-"+world.dimension().location().toString().replace(':','_').replace('/','_')+".properties");
            if(Files.exists(file))try(var in=Files.newBufferedReader(file)){
                var data=new Properties();data.load(in);
                for(String key:data.stringPropertyNames()){
                    var xyz=key.split(",");var value=data.getProperty(key).split("\\|");
                    var block=BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(value[0]));
                    if(block!=Blocks.COBWEB&&block!=Blocks.PACKED_ICE)throw new IOException("Bloco temporário inválido");
                    entries.put(new BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2])),new Entry(block,Long.parseLong(value[1])));
                }
            }catch(Exception e){failed=true;LoggerFactory.getLogger(TemporaryBlocks.class).error("Registro temporário ilegível, arquivo preservado",e);}
        }
        boolean save(){
            if(failed)return false;Path temp=null;
            try{
                Files.createDirectories(file.getParent());var data=new Properties();
                entries.forEach((pos,e)->data.setProperty(pos.getX()+","+pos.getY()+","+pos.getZ(),BuiltInRegistries.BLOCK.getKey(e.block()).toString()+"|"+e.until()));
                temp=Files.createTempFile(file.getParent(),"temporary-",".tmp");
                try(var out=Files.newBufferedWriter(temp)){data.store(out,"Oxy RPG temporary blocks");}
                Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);return true;
            }catch(IOException e){LoggerFactory.getLogger(TemporaryBlocks.class).error("Falha ao salvar registro temporário",e);return false;}
            finally{if(temp!=null)try{Files.deleteIfExists(temp);}catch(IOException ignored){}}
        }
    }
    private TemporaryBlocks(){}
    static boolean temporary(ServerLevel world,BlockPos pos){var ledger=WORLDS.computeIfAbsent(world,Ledger::new);return ledger.entries.containsKey(pos);}
    public static void web(ServerPlayer player,LivingEntity target,int ticks){
        var center=target.blockPosition();place(player,List.of(center,center.above()),Blocks.COBWEB,ticks);
    }
    public static void ice(ServerPlayer player,BlockPos center,boolean large,int ticks){
        var positions=new ArrayList<BlockPos>();
        if(large)for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++){
            if(Math.abs(x)==1||Math.abs(y)==1||Math.abs(z)==1)positions.add(center.offset(x,y,z));
        }else{
            positions.add(center);positions.add(center.east());positions.add(center.west());positions.add(center.north());positions.add(center.south());
        }
        place(player,positions,Blocks.PACKED_ICE,ticks);
    }
    private static void place(ServerPlayer p,List<BlockPos> positions,Block block,int ticks){
        var world=(ServerLevel)p.level();var ledger=WORLDS.computeIfAbsent(world,Ledger::new);
        if(ledger.failed)return;var added=new ArrayList<BlockPos>();
        for(var pos:positions){
            if(ledger.entries.size()>=1024||!world.hasChunkAt(pos)||!world.isInWorldBounds(pos)||!world.mayInteract(p,pos)||!world.getBlockState(pos).isAir())continue;
            var box=new AABB(pos);boolean overlap=!world.getEntitiesOfClass(Player.class,box).isEmpty();
            if(block==Blocks.PACKED_ICE)overlap|=!world.getEntitiesOfClass(LivingEntity.class,box).isEmpty();
            if(overlap)continue;
            ledger.entries.put(pos.immutable(),new Entry(block,world.getGameTime()+ticks));added.add(pos.immutable());
            if(block==Blocks.PACKED_ICE&&positions.size()<=5)break;
        }
        if(added.isEmpty())return;
        // Record before mutation; if interrupted, a harmless ledger entry may remain, never an unrecorded block.
        if(!ledger.save()){for(var pos:added)ledger.entries.remove(pos);return;}
        for(var pos:added)world.setBlock(pos,block.defaultBlockState(),3);
    }
    public static void tick(ServerTickEvent.Post event){
        if(event.getServer().getTickCount()%20!=0)return;
        for(var world:event.getServer().getAllLevels()){
            var ledger=WORLDS.computeIfAbsent(world,Ledger::new);if(ledger.failed)continue;boolean changed=false;
            var iterator=ledger.entries.entrySet().iterator();
            while(iterator.hasNext()){
                var next=iterator.next();if(next.getValue().until()>world.getGameTime()||!world.hasChunkAt(next.getKey()))continue;
                if(world.getBlockState(next.getKey()).is(next.getValue().block()))world.removeBlock(next.getKey(),false);
                iterator.remove();changed=true;
            }
            if(changed)ledger.save();
        }
    }
    public static void broken(BlockEvent.BreakEvent event){
        if(!(event.getLevel() instanceof ServerLevel world)||event.isCanceled())return;
        var ledger=WORLDS.computeIfAbsent(world,Ledger::new);var entry=ledger.entries.get(event.getPos());
        if(entry==null||!event.getState().is(entry.block()))return;
        event.setCanceled(true);world.removeBlock(event.getPos(),false);ledger.entries.remove(event.getPos());ledger.save();
    }
    public static void placed(BlockEvent.EntityPlaceEvent event){
        if(!(event.getEntity() instanceof Player)||!(event.getLevel() instanceof ServerLevel world)||event.isCanceled())return;
        var ledger=WORLDS.computeIfAbsent(world,Ledger::new);
        if(ledger.entries.remove(event.getPos())!=null)ledger.save();
    }
    public static void stop(ServerStoppedEvent event){WORLDS.clear();}
}
