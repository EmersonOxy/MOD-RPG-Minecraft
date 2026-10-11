package dev.emersonoxy.rpg.client;

import java.nio.file.*;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.LoggerFactory;

public final class CombatOptions {
    private static boolean loaded;
    public static boolean numbers=true,marker=true,sound=true,group=true,preview=true;
    public static double size=1;
    private CombatOptions(){}
    public static void load(){
        if(loaded)return;loaded=true;var file=FMLPaths.CONFIGDIR.get().resolve("oxyrpg-combat.properties");
        if(!Files.exists(file))return;
        try(var r=Files.newBufferedReader(file)){var p=new Properties();p.load(r);numbers=Boolean.parseBoolean(p.getProperty("numbers","true"));marker=Boolean.parseBoolean(p.getProperty("marker","true"));sound=Boolean.parseBoolean(p.getProperty("sound","true"));group=Boolean.parseBoolean(p.getProperty("group","true"));preview=Boolean.parseBoolean(p.getProperty("preview","true"));size=CameraMotion.clamp(Double.parseDouble(p.getProperty("size","1")),.6,1.6,1);}
        catch(Exception e){LoggerFactory.getLogger(CombatOptions.class).warn("Não foi possível ler opções de combate",e);}
    }
    public static void save(){
        load();var file=FMLPaths.CONFIGDIR.get().resolve("oxyrpg-combat.properties");Path temp=null;
        try{Files.createDirectories(file.getParent());var p=new Properties();p.setProperty("numbers",""+numbers);p.setProperty("marker",""+marker);p.setProperty("sound",""+sound);p.setProperty("group",""+group);p.setProperty("preview",""+preview);p.setProperty("size",""+size);temp=Files.createTempFile(file.getParent(),"combat-",".tmp");try(var w=Files.newBufferedWriter(temp)){p.store(w,"Oxy RPG combat");}Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(Exception e){LoggerFactory.getLogger(CombatOptions.class).warn("Não foi possível salvar opções de combate",e);}finally{if(temp!=null)try{Files.deleteIfExists(temp);}catch(Exception ignored){}}
    }
}
