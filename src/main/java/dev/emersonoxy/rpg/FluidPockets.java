package dev.emersonoxy.rpg;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Drained water is restored; a durable ledger also recovers pockets after a restart. */
public final class FluidPockets {
    private record Entry(int level,long until){}
    private static final Map<ServerLevel,Ledger> WORLDS=new HashMap<>();
    private static final class Ledger {
        final ServerLevel world;final Path file;final Map<BlockPos,Entry> entries=new HashMap<>();boolean failed;
        Ledger(ServerLevel w){world=w;file=w.getServer().getWorldPath(LevelResource.ROOT).resolve("oxyrpg/fluid-"+w.dimension().location().toString().replace(':','_').replace('/','_')+".json");
            if(Files.exists(file))try{for(var element:JsonParser.parseString(Files.readString(file)).getAsJsonArray()){var n=element.getAsJsonObject();int level=n.get("level").getAsInt();if(level<0||level>15)throw new IllegalArgumentException("Água inválida");entries.put(new BlockPos(n.get("x").getAsInt(),n.get("y").getAsInt(),n.get("z").getAsInt()),new Entry(level,n.get("until").getAsLong()));}}catch(Exception e){failed=true;org.slf4j.LoggerFactory.getLogger(FluidPockets.class).error("Registro de água ilegível; preservado",e);}}
        boolean save(){if(failed)return false;Path temp=null;try{Files.createDirectories(file.getParent());var list=new JsonArray();entries.forEach((pos,e)->{var n=new JsonObject();n.addProperty("x",pos.getX());n.addProperty("y",pos.getY());n.addProperty("z",pos.getZ());n.addProperty("level",e.level());n.addProperty("until",e.until());list.add(n);});temp=Files.createTempFile(file.getParent(),"fluid-",".tmp");Files.writeString(temp,list.toString());Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);return true;}catch(Exception e){org.slf4j.LoggerFactory.getLogger(FluidPockets.class).error("Não foi possível registrar água temporária",e);return false;}finally{if(temp!=null)try{Files.deleteIfExists(temp);}catch(Exception ignored){}}}
    }
    private FluidPockets(){}
    public static void drain(ServerPlayer p,BlockPos center,int radius,int duration){
        if(!p.mayBuild())return;var l=WORLDS.computeIfAbsent(p.level(),Ledger::new);if(l.failed)return;var pending=new LinkedHashMap<BlockPos,Entry>();long until=p.level().getGameTime()+duration;
        for(var pos:BlockPos.betweenClosed(center.offset(-radius,-1,-radius),center.offset(radius,1,radius))){var at=pos.immutable();if(!l.world.hasChunkAt(at)||!l.world.mayInteract(p,at)||l.world.getServer().isUnderSpawnProtection(l.world,at,p))continue;var state=l.world.getBlockState(at);if(state.is(Blocks.WATER)&&!l.entries.containsKey(at))pending.put(at,new Entry(state.getValue(LiquidBlock.LEVEL),until));}
        if(pending.isEmpty())return;l.entries.putAll(pending);if(!l.save()){pending.keySet().forEach(l.entries::remove);return;}for(var pos:pending.keySet())l.world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
    }
    public static void tick(ServerTickEvent.Post e){
        for(var world:e.getServer().getAllLevels()){var l=WORLDS.computeIfAbsent(world,Ledger::new);if(l.failed||world.getGameTime()%10!=0)continue;boolean changed=false;var iterator=l.entries.entrySet().iterator();while(iterator.hasNext()){var entry=iterator.next();var pos=entry.getKey();if(!world.hasChunkAt(pos))continue;var state=world.getBlockState(pos);if(world.getGameTime()>=entry.getValue().until()){if(state.isAir())world.setBlock(pos,Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL,entry.getValue().level()),3);iterator.remove();changed=true;}else if(state.is(Blocks.WATER))world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);else if(!state.isAir()){iterator.remove();changed=true;}}if(changed)l.save();}
    }
    public static void placed(BlockEvent.EntityPlaceEvent e){if(e.getLevel() instanceof ServerLevel w){var l=WORLDS.get(w);if(l!=null&&l.entries.remove(e.getPos())!=null)l.save();}}
    public static void stop(ServerStoppedEvent e){WORLDS.clear();}
}
