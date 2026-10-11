package dev.emersonoxy.rpg.client;
import java.nio.file.*;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;
public final class AmbientOptions {
    public static boolean leaves=true,water=true,vegetation=true;private static boolean loaded;
    private AmbientOptions(){}
    public static void load(){if(loaded)return;loaded=true;var file=FMLPaths.CONFIGDIR.get().resolve("oxyrpg-ambient.properties");if(!Files.exists(file))return;try(var r=Files.newBufferedReader(file)){var p=new Properties();p.load(r);leaves=Boolean.parseBoolean(p.getProperty("leaves","true"));water=Boolean.parseBoolean(p.getProperty("water","true"));vegetation=Boolean.parseBoolean(p.getProperty("vegetation","true"));}catch(Exception e){org.slf4j.LoggerFactory.getLogger(AmbientOptions.class).warn("Falha ao ler ambiente",e);}}
    public static void save(){var file=FMLPaths.CONFIGDIR.get().resolve("oxyrpg-ambient.properties");try{Files.createDirectories(file.getParent());var p=new Properties();p.setProperty("leaves",""+leaves);p.setProperty("water",""+water);p.setProperty("vegetation",""+vegetation);try(var w=Files.newBufferedWriter(file)){p.store(w,"Oxy RPG ambient effects");}}catch(Exception e){org.slf4j.LoggerFactory.getLogger(AmbientOptions.class).warn("Falha ao salvar ambiente",e);}}
}
