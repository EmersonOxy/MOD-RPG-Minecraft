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
    public static boolean compass=true,regions=true;private static boolean loaded;private static Object world;
    private static final LinkedHashMap<ResourceLocation,Integer> recent=new LinkedHashMap<>();
    private static ResourceLocation dimension,biome,candidate;private static int stable,ticks,announced,shown;private static String heading="",subtitle="";
    private ExplorationHud(){}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-exploration.properties");}
    public static void load(){if(loaded)return;loaded=true;if(Files.exists(file()))try(var in=Files.newBufferedReader(file())){var p=new Properties();p.load(in);compass=Boolean.parseBoolean(p.getProperty("compass","true"));regions=Boolean.parseBoolean(p.getProperty("regions","true"));}catch(java.io.IOException e){org.slf4j.LoggerFactory.getLogger(ExplorationHud.class).warn("Falha ao ler exploração",e);}}
    public static void save(){try{var p=new Properties();p.setProperty("compass",""+compass);p.setProperty("regions",""+regions);try(var out=Files.newBufferedWriter(file())){p.store(out,"Oxy RPG exploration HUD");}}catch(java.io.IOException e){org.slf4j.LoggerFactory.getLogger(ExplorationHud.class).warn("Falha ao salvar exploração",e);}}
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
        if(compass){int w=Math.min(160,g.guiWidth()-40),cx=g.guiWidth()/2,y=10;float yaw=mc.gameRenderer.getMainCamera().getYRot();
            g.enableScissor(cx-w/2,y-2,cx+w/2,y+16);
            for(int angle=0;angle<360;angle+=30){float offset=Mth.wrapDegrees(angle-yaw);int x=cx+Math.round(offset*w/150f);if(Math.abs(x-cx)>w/2)continue;
                int opacity=(int)(220*(1-Math.pow(Math.abs(x-cx)/(w/2.0),2)));int color=opacity<<24|0xD5D0C2;
                if(angle%90==0){String label=new String[]{"S","O","N","L"}[angle/90];g.drawCenteredString(mc.font,label,x,y,color);}else g.fill(x,y+9,x+1,y+11,color);
            }
            g.disableScissor();g.fill(cx,y+13,cx+1,y+16,0xFFE9C77C);
        }
        float elapsed=ticks-announced+mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);if(regions&&shown>0&&elapsed<shown){float a=Math.min(1,elapsed/20f)*Math.min(1,(shown-elapsed)/30f);a=a*a*(3-2*a);if(a<.04f)return;HudOpacity.begin(a);int cy=Math.max(48,g.guiHeight()/4);String title=mc.font.plainSubstrByWidth(heading,Math.max(20,(g.guiWidth()-40)*2/3));
            g.pose().pushMatrix();g.pose().translate(g.guiWidth()/2f,cy);g.pose().scale(1.5f,1.5f);g.drawCenteredString(mc.font,title,0,0,RpgUi.GOLD);g.pose().popMatrix();
            int half=Math.min(150,Math.max(45,mc.font.width(title)*3/4));g.fill(g.guiWidth()/2-half,cy+20,g.guiWidth()/2+half,cy+21,0xAAE9C77C);g.drawCenteredString(mc.font,subtitle,g.guiWidth()/2,cy+28,RpgUi.TEXT);HudOpacity.end();}
    }
}
