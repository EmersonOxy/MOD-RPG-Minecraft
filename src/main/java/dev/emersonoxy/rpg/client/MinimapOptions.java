package dev.emersonoxy.rpg.client;

import java.nio.file.*;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.LoggerFactory;

public final class MinimapOptions {
    public static int size=112,zoom=1,corner=0,layerMode=0;
    public static double opacity=.94,offsetX=8,offsetY=8,x=.85,y=.05;
    public static boolean coordinates=true,avoidEffects=true,manual,round;
    private static boolean loaded;
    private MinimapOptions(){}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-minimap.properties");}
    private static double number(Properties p,String name,double fallback,double min,double max){try{double n=Double.parseDouble(p.getProperty(name,""+fallback));return Double.isFinite(n)?Math.clamp(n,min,max):fallback;}catch(NumberFormatException e){return fallback;}}
    public static void load(){if(loaded)return;loaded=true;if(!Files.exists(file()))return;
        try(var in=Files.newBufferedReader(file())){var p=new Properties();p.load(in);layerMode=(int)number(p,"layerMode",0,0,2);size=(int)number(p,"size",112,72,160);zoom=(int)number(p,"zoom",1,0,2);corner=(int)number(p,"corner",0,0,3);opacity=number(p,"opacity",.94,.35,1);offsetX=number(p,"offsetX",8,0,80);offsetY=number(p,"offsetY",8,0,80);x=number(p,"x",.85,0,1);y=number(p,"y",.05,0,1);round=Boolean.parseBoolean(p.getProperty("round","false"));coordinates=Boolean.parseBoolean(p.getProperty("coordinates","true"));avoidEffects=Boolean.parseBoolean(p.getProperty("avoidEffects","true"));manual=Boolean.parseBoolean(p.getProperty("manual","false"));}
        catch(java.io.IOException e){LoggerFactory.getLogger(MinimapOptions.class).warn("Falha ao ler minimapa",e);}}
    public static void save(){Path temp=null;try{Files.createDirectories(file().getParent());var p=new Properties();p.setProperty("layerMode",""+layerMode);p.setProperty("size",""+size);p.setProperty("zoom",""+zoom);p.setProperty("corner",""+corner);p.setProperty("opacity",""+opacity);p.setProperty("offsetX",""+offsetX);p.setProperty("offsetY",""+offsetY);p.setProperty("x",""+x);p.setProperty("y",""+y);p.setProperty("round",""+round);p.setProperty("coordinates",""+coordinates);p.setProperty("avoidEffects",""+avoidEffects);p.setProperty("manual",""+manual);temp=Files.createTempFile(file().getParent(),"oxyrpg-minimap-",".tmp");try(var out=Files.newBufferedWriter(temp)){p.store(out,"Oxy RPG minimap");}try{Files.move(temp,file(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file(),StandardCopyOption.REPLACE_EXISTING);}}
        catch(java.io.IOException e){LoggerFactory.getLogger(MinimapOptions.class).warn("Falha ao salvar minimapa",e);}finally{if(temp!=null)try{Files.deleteIfExists(temp);}catch(java.io.IOException ignored){}}}
    public static int step(){load();return zoom==0?4:zoom==1?2:1;}
    public static String cornerLabel(){load();return manual?"Personalizado":new String[]{"Superior direito","Superior esquerdo","Inferior direito","Inferior esquerdo"}[corner];}
    public static void defaults(){size=112;zoom=1;corner=layerMode=0;opacity=.94;offsetX=offsetY=8;coordinates=avoidEffects=true;manual=round=false;SimpleMinimap.clear();save();}
}
