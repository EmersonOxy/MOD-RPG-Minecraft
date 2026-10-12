package dev.emersonoxy.rpg.client;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLPaths;
/** Camera-relative compass and debounced region transitions; no server packets or chunk loads. */
public final class ExplorationHud {
    public static boolean compass=true,regions=true,degrees=true;public static int compassY=32;private static boolean loaded;private static Object world;
    private static ResourceLocation markerDimension;private static net.minecraft.core.BlockPos marker;private static String markerWorld="";
    private static final LinkedHashMap<ResourceLocation,Integer> recent=new LinkedHashMap<>();
    private static ResourceLocation dimension,biome,candidate;private static int stable,ticks,announced,shown;private static String heading="",subtitle="";
    private ExplorationHud(){}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-exploration.properties");}
    public static void load(){if(loaded)return;loaded=true;if(Files.exists(file()))try(var in=Files.newBufferedReader(file())){var p=new Properties();p.load(in);compass=Boolean.parseBoolean(p.getProperty("compass","true"));regions=Boolean.parseBoolean(p.getProperty("regions","true"));degrees=Boolean.parseBoolean(p.getProperty("degrees","true"));
        compassY=Math.clamp(Integer.parseInt(p.getProperty("compass_y","32")),24,56);markerDimension=ResourceLocation.tryParse(p.getProperty("marker_dimension",""));
        markerWorld=p.getProperty("marker_world","");if(markerDimension!=null)marker=new net.minecraft.core.BlockPos(Integer.parseInt(p.getProperty("marker_x","0")),Integer.parseInt(p.getProperty("marker_y","0")),Integer.parseInt(p.getProperty("marker_z","0")));
    }catch(java.io.IOException|NumberFormatException e){org.slf4j.LoggerFactory.getLogger(ExplorationHud.class).warn("Falha ao ler exploração",e);}}
    public static void save(){try{var p=new Properties();p.setProperty("compass",""+compass);p.setProperty("regions",""+regions);p.setProperty("degrees",""+degrees);p.setProperty("compass_y",""+compassY);
        if(marker!=null&&markerDimension!=null){p.setProperty("marker_world",markerWorld);p.setProperty("marker_dimension",markerDimension.toString());p.setProperty("marker_x",""+marker.getX());p.setProperty("marker_y",""+marker.getY());p.setProperty("marker_z",""+marker.getZ());}
        try(var out=Files.newBufferedWriter(file())){p.store(out,"Oxy RPG exploration HUD");}}catch(java.io.IOException e){org.slf4j.LoggerFactory.getLogger(ExplorationHud.class).warn("Falha ao salvar exploração",e);}}
    private static String worldId(){var mc=Minecraft.getInstance();var local=mc.getSingleplayerServer();if(local!=null)return local.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize().toString();var remote=mc.getCurrentServer();return remote==null?"":remote.ip;}
    public static void markHere(){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;marker=mc.player.blockPosition().immutable();markerDimension=mc.level.dimension().location();markerWorld=worldId();save();}
    public static void clearMarker(){marker=null;markerDimension=null;markerWorld="";save();}
    public static boolean hasMarker(){return marker!=null;}
    private static void compass(GuiGraphics g,Minecraft mc){
        int w=Math.min(196,g.guiWidth()-40),cx=g.guiWidth()/2,y=compassY;float yaw=mc.gameRenderer.getMainCamera().getYRot();
        g.enableScissor(cx-w/2,y-3,cx+w/2,y+16);
        String[] labels={"S","SO","O","NO","N","NE","L","SE"};
        for(int angle=0;angle<360;angle+=15){float offset=Mth.wrapDegrees(angle-yaw);int x=cx+Math.round(offset*w/150f);if(Math.abs(x-cx)>w/2)continue;
            int opacity=(int)(235*(1-Math.pow(Math.abs(x-cx)/(w/2.0),2)));int color=opacity<<24|0xD5D0C2;
            if(angle%45==0)g.drawCenteredString(mc.font,labels[angle/45],x,y,color);else g.fill(x,y+9,x+1,y+11,color);
        }
        g.disableScissor();g.fill(cx-2,y-3,cx+3,y-2,0xFFE9C77C);g.fill(cx-1,y-2,cx+2,y-1,0xFFE9C77C);g.fill(cx,y-1,cx+1,y,0xFFE9C77C);
        if(degrees)g.drawCenteredString(mc.font,Math.floorMod(Math.round(yaw)+180,360)+"°",cx,y+15,0xFFD5D0C2);
        var mob=TargetLock.target();if(mob!=null&&mob.isAlive()&&!dev.emersonoxy.rpg.AviaryCarry.holding(mc.player,mob))notch(g,mc,mob.position(),yaw,cx,y,w,0xFF78DCEC);
        if(marker!=null&&markerWorld.equals(worldId())&&markerDimension.equals(mc.level.dimension().location())){
            var point=net.minecraft.world.phys.Vec3.atCenterOf(marker);notch(g,mc,point,yaw,cx,y,w,0xFFE9C77C);
            g.drawCenteredString(mc.font,"Local marcado · "+Math.round(mc.player.position().distanceTo(point))+" m",cx,y+(degrees?27:15),0xFFE9C77C);
        }
    }
    private static void notch(GuiGraphics g,Minecraft mc,net.minecraft.world.phys.Vec3 point,float yaw,int cx,int y,int width,int color){
        var delta=point.subtract(mc.player.position());float angle=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));float offset=Mth.wrapDegrees(angle-yaw);
        int x=cx+Math.clamp(Math.round(offset*width/150f),-width/2+3,width/2-3);
        g.fill(x-2,y+10,x+3,y+11,color);g.fill(x-1,y+11,x+2,y+13,color);
        if(Math.abs(offset)>75){int side=offset>0?1:-1;g.fill(x+side*2,y+11,x+side*2+1,y+12,color);}
    }
    private static String name(ResourceLocation id,String prefix){String key=prefix+"."+id.getNamespace()+"."+id.getPath();return net.minecraft.client.resources.language.I18n.exists(key)?Component.translatable(key).getString():java.util.Arrays.stream(id.getPath().split("[_/]")).map(s->s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1)).collect(java.util.stream.Collectors.joining(" "));}
    public static void tick(){load();var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null){world=null;return;}if(mc.isPaused())return;
        if(world!=mc.level){world=mc.level;dimension=null;biome=candidate=null;stable=ticks=announced=shown=0;recent.clear();}ticks++;
        // Biome lookup needs only two checks per second, in an already loaded chunk.
        if(!regions||ticks%10!=0||!mc.level.getChunkSource().hasChunk(mc.player.getBlockX()>>4,mc.player.getBlockZ()>>4))return;
        var d=mc.level.dimension().location();var b=mc.level.getBiome(mc.player.blockPosition()).unwrapKey().map(k->k.location()).orElse(null);if(b==null)return;
        if(!b.equals(candidate)){candidate=b;stable=0;}else stable+=10;
        if(ticks<40||stable<30||d.equals(dimension)&&b.equals(biome)||ticks-announced<160&&d.equals(dimension))return;
        boolean changed=!d.equals(dimension);recent.entrySet().removeIf(e->ticks-e.getValue()>=1200);
        dimension=d;biome=b;
        if(!changed&&recent.containsKey(b))return;
        recent.remove(b);recent.put(b,ticks);while(recent.size()>3)recent.remove(recent.keySet().iterator().next());
        announced=ticks;shown=120;
        heading=changed?name(d,"dimension"):name(b,"biome");subtitle=changed?name(b,"biome"):name(d,"dimension");
    }
    public static void render(GuiGraphics g){load();var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui||mc.screen!=null||mc.getDebugOverlay().showDebugScreen()||DynamicHud.mode()==DynamicHud.Mode.HIDDEN)return;
        if(compass)compass(g,mc);
        float elapsed=ticks-announced+mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);if(regions&&shown>0&&elapsed<shown){float a=Math.min(1,elapsed/20f)*Math.min(1,(shown-elapsed)/30f);a=a*a*(3-2*a);if(a<.04f)return;HudOpacity.begin(a);int cy=Math.max(48,g.guiHeight()/4);String title=mc.font.plainSubstrByWidth(heading,Math.max(20,(g.guiWidth()-40)*2/3));
            g.pose().pushMatrix();g.pose().translate(g.guiWidth()/2f,cy);g.pose().scale(1.5f,1.5f);g.drawCenteredString(mc.font,title,0,0,RpgUi.GOLD);g.pose().popMatrix();
            int half=Math.min(150,Math.max(45,mc.font.width(title)*3/4));g.fill(g.guiWidth()/2-half,cy+20,g.guiWidth()/2+half,cy+21,0xAAE9C77C);g.drawCenteredString(mc.font,subtitle,g.guiWidth()/2,cy+28,RpgUi.TEXT);HudOpacity.end();}
    }
}
