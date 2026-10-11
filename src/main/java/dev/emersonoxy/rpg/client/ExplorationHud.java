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
    private static ResourceLocation dimension,biome,candidate;private static int stable,ticks,announced,shown;private static String heading="",subtitle="";
    private ExplorationHud(){}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-exploration.properties");}
    public static void load(){if(loaded)return;loaded=true;if(Files.exists(file()))try(var in=Files.newBufferedReader(file())){var p=new Properties();p.load(in);compass=Boolean.parseBoolean(p.getProperty("compass","true"));regions=Boolean.parseBoolean(p.getProperty("regions","true"));}catch(java.io.IOException e){org.slf4j.LoggerFactory.getLogger(ExplorationHud.class).warn("Falha ao ler exploração",e);}}
    public static void save(){try{var p=new Properties();p.setProperty("compass",""+compass);p.setProperty("regions",""+regions);try(var out=Files.newBufferedWriter(file())){p.store(out,"Oxy RPG exploration HUD");}}catch(java.io.IOException e){org.slf4j.LoggerFactory.getLogger(ExplorationHud.class).warn("Falha ao salvar exploração",e);}}
    private static String name(ResourceLocation id,String prefix){String key=prefix+"."+id.getNamespace()+"."+id.getPath();return net.minecraft.client.resources.language.I18n.exists(key)?Component.translatable(key).getString():java.util.Arrays.stream(id.getPath().split("[_/]")).map(s->s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1)).collect(java.util.stream.Collectors.joining(" "));}
    public static void tick(){load();var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null){world=null;return;}if(mc.isPaused())return;
        if(world!=mc.level){world=mc.level;dimension=null;biome=candidate=null;stable=ticks=announced=shown=0;}ticks++;
        var d=mc.level.dimension().location();var b=mc.level.getBiome(mc.player.blockPosition()).unwrapKey().map(k->k.location()).orElse(null);if(b==null)return;
        if(!b.equals(candidate)){candidate=b;stable=0;}else stable++;
        if(ticks<40||stable<30||d.equals(dimension)&&b.equals(biome)||ticks-announced<160&&d.equals(dimension))return;
        boolean changed=!d.equals(dimension);dimension=d;biome=b;announced=ticks;shown=regions?120:0;
        heading=changed?name(d,"dimension"):name(b,"biome");subtitle=changed?name(b,"biome"):name(d,"dimension");
    }
    public static void render(GuiGraphics g){load();var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui||mc.screen!=null||mc.getDebugOverlay().showDebugScreen()||DynamicHud.mode()==DynamicHud.Mode.HIDDEN)return;
        if(compass){int w=Math.min(220,g.guiWidth()-40),cx=g.guiWidth()/2,y=12;float yaw=mc.gameRenderer.getMainCamera().getYRot();
            g.fill(cx-w/2-6,y-5,cx+w/2+6,y+20,0x9011151D);g.fill(cx-w/2,y+12,cx+w/2,y+13,0x99796341);g.enableScissor(cx-w/2,y-3,cx+w/2,y+17);
            for(int angle=0;angle<360;angle+=15){float offset=Mth.wrapDegrees(angle-yaw);int x=cx+Math.round(offset*w/150f);if(Math.abs(x-cx)>w/2)continue;g.fill(x,y+9,x+1,y+12,0xFFD4C7A5);
                if(angle%45==0){String label=new String[]{"S","SO","O","NO","N","NE","L","SE"}[angle/45];g.drawCenteredString(mc.font,label,x,y-1,angle%90==0?RpgUi.GOLD:RpgUi.MUTED);}}
            g.disableScissor();g.fill(cx-1,y+14,cx+2,y+18,RpgUi.GOLD);
        }
        int elapsed=ticks-announced;if(regions&&shown>0&&elapsed<shown){float a=Math.min(1,elapsed/12f)*Math.min(1,(shown-elapsed)/24f);HudOpacity.begin(a);int cy=Math.max(48,g.guiHeight()/4);String title=mc.font.plainSubstrByWidth(heading,Math.max(20,(g.guiWidth()-40)*2/3));
            g.pose().pushMatrix();g.pose().translate(g.guiWidth()/2f,cy);g.pose().scale(1.5f,1.5f);g.drawCenteredString(mc.font,title,0,0,RpgUi.GOLD);g.pose().popMatrix();
            int half=Math.min(150,Math.max(45,mc.font.width(title)*3/4));g.fill(g.guiWidth()/2-half,cy+20,g.guiWidth()/2+half,cy+21,0xAAE9C77C);g.drawCenteredString(mc.font,subtitle,g.guiWidth()/2,cy+28,RpgUi.TEXT);HudOpacity.end();}
    }
}
