package dev.emersonoxy.rpg.client;

import java.nio.file.*;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.LoggerFactory;

public final class CombatOptions {
    private static boolean loaded;
    public static boolean numbers=true,marker=true,sound=true,group=true,preview=true,animations=true,effects=true,swarm=true;
    public static boolean blood=true,bloodMarks=true,smoke=true,impacts=true;
    public static double size=1,particleIntensity=1;
    public static int bloodMarkSeconds=60;
    private CombatOptions(){}
    public static void load(){
        if(loaded)return;loaded=true;var file=FMLPaths.CONFIGDIR.get().resolve("oxyrpg-combat.properties");
        if(!Files.exists(file))return;
        try(var r=Files.newBufferedReader(file)){var p=new Properties();p.load(r);blood=Boolean.parseBoolean(p.getProperty("blood","true"));bloodMarks=Boolean.parseBoolean(p.getProperty("bloodMarks","true"));smoke=Boolean.parseBoolean(p.getProperty("smoke","true"));impacts=Boolean.parseBoolean(p.getProperty("impacts","true"));particleIntensity=CameraMotion.clamp(Double.parseDouble(p.getProperty("particleIntensity","1")),.5,1.5,1);bloodMarkSeconds=Math.clamp(Integer.parseInt(p.getProperty("bloodMarkSeconds","60")),15,120);animations=Boolean.parseBoolean(p.getProperty("animations","true"));effects=Boolean.parseBoolean(p.getProperty("effects","true"));swarm=Boolean.parseBoolean(p.getProperty("swarm","true"));numbers=Boolean.parseBoolean(p.getProperty("numbers","true"));marker=Boolean.parseBoolean(p.getProperty("marker","true"));sound=Boolean.parseBoolean(p.getProperty("sound","true"));group=Boolean.parseBoolean(p.getProperty("group","true"));preview=Boolean.parseBoolean(p.getProperty("preview","true"));size=CameraMotion.clamp(Double.parseDouble(p.getProperty("size","1")),.6,1.6,1);}
        catch(Exception e){LoggerFactory.getLogger(CombatOptions.class).warn("Não foi possível ler opções de combate",e);}
    }
    public static void save(){
        load();var file=FMLPaths.CONFIGDIR.get().resolve("oxyrpg-combat.properties");Path temp=null;
        try{Files.createDirectories(file.getParent());var p=new Properties();p.setProperty("blood",""+blood);p.setProperty("bloodMarks",""+bloodMarks);p.setProperty("smoke",""+smoke);p.setProperty("impacts",""+impacts);p.setProperty("particleIntensity",""+particleIntensity);p.setProperty("bloodMarkSeconds",""+bloodMarkSeconds);p.setProperty("animations",""+animations);p.setProperty("effects",""+effects);p.setProperty("swarm",""+swarm);p.setProperty("numbers",""+numbers);p.setProperty("marker",""+marker);p.setProperty("sound",""+sound);p.setProperty("group",""+group);p.setProperty("preview",""+preview);p.setProperty("size",""+size);temp=Files.createTempFile(file.getParent(),"combat-",".tmp");try(var w=Files.newBufferedWriter(temp)){p.store(w,"Oxy RPG combat");}Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(Exception e){LoggerFactory.getLogger(CombatOptions.class).warn("Não foi possível salvar opções de combate",e);}finally{if(temp!=null)try{Files.deleteIfExists(temp);}catch(Exception ignored){}}
    }
}
