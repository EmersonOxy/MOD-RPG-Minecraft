package dev.emersonoxy.rpg;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

/** Separate world/UUID record: class resets do not destroy profession or villager progress. */
public final class ProfessionStore {
    public static final class Data {
        public final int[] xp=new int[3];
        public UUID villager;public int quest=-1,progress,completed;
        public Data copy(){var n=new Data();System.arraycopy(xp,0,n.xp,0,3);n.villager=villager;n.quest=quest;n.progress=progress;n.completed=completed;return n;}
    }
    private record Key(Path file){}
    private static final Map<Key,Data> CACHE=new HashMap<>();
    private ProfessionStore(){}
    private static Key key(ServerPlayer p){return new Key(p.level().getServer().getWorldPath(LevelResource.ROOT).resolve("oxyrpg/professions/"+p.getUUID()+".properties"));}
    public static Data get(ServerPlayer p)throws IOException{
        var key=key(p);var cached=CACHE.get(key);if(cached!=null)return cached.copy();
        var value=new Data();
        if(Files.exists(key.file()))try(var in=Files.newBufferedReader(key.file())){
            var props=new Properties();props.load(in);
            for(int i=0;i<3;i++)value.xp[i]=Math.clamp(Integer.parseInt(props.getProperty("xp"+i,"0")),0,19000);
            value.quest=Integer.parseInt(props.getProperty("quest","-1"));
            if(value.quest>=0){if(value.quest>=VillagerQuests.COUNT)throw new IOException("Missão inválida");value.villager=UUID.fromString(props.getProperty("villager"));}
            value.progress=Math.max(0,Integer.parseInt(props.getProperty("progress","0")));
            value.completed=Math.max(0,Integer.parseInt(props.getProperty("completed","0")));
        }catch(RuntimeException e){throw new IOException("Registro de profissão inválido, preservado",e);}
        CACHE.put(key,value);return value.copy();
    }
    public static void save(ServerPlayer p,Data value)throws IOException{
        var key=key(p);Files.createDirectories(key.file().getParent());var props=new Properties();
        for(int i=0;i<3;i++)props.setProperty("xp"+i,Integer.toString(value.xp[i]));
        props.setProperty("quest",Integer.toString(value.quest));props.setProperty("progress",Integer.toString(value.progress));props.setProperty("completed",Integer.toString(value.completed));
        if(value.villager!=null)props.setProperty("villager",value.villager.toString());
        var temp=Files.createTempFile(key.file().getParent(),"profession-",".tmp");
        try{
            try(var out=Files.newBufferedWriter(temp)){props.store(out,"Oxy RPG professions and villager contract");}
            Files.move(temp,key.file(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);CACHE.put(key,value.copy());
        }finally{Files.deleteIfExists(temp);}
    }
    public static final int MAX_XP=17500; public static int nextXp(int level){return level>=20?0:level*100*(level<=5?50:level<=9?75:100)/100;} public static int level(int xp){int level=1;while(level<20&&xp>=nextXp(level)){xp-=nextXp(level);level++;}return level;}
    public static int remainder(int xp){int level=1;while(level<20&&xp>=nextXp(level)){xp-=nextXp(level);level++;}return level==20?0:xp;}
    public static void clear(){CACHE.clear();}
}
