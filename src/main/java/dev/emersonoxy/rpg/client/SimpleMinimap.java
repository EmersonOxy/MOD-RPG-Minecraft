package dev.emersonoxy.rpg.client;

import java.util.*;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/** Loaded terrain and cave floors, bounded sampling and one uploaded terrain texture. */
public final class SimpleMinimap {
    private static final int GRID=64;
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("oxyrpg","minimap_surface");
    private record Sample(int color,long updated){}
    public record Bounds(int x,int y,int size,int height){}
    private static final LinkedHashMap<Long,Sample> COLORS=new LinkedHashMap<>();
    private static int cursor,lastStep,lastX=Integer.MIN_VALUE,lastZ;private static long uploaded=-1;private static Object dimension;
    private static DynamicTexture texture;private static boolean uploadedRound,cave,retainPixels;private static int layer=Integer.MIN_VALUE;
    private SimpleMinimap(){}
    public static void clear(){COLORS.clear();cursor=0;uploaded=-1;lastX=Integer.MIN_VALUE;dimension=null;retainPixels=false;if(texture!=null){Minecraft.getInstance().getTextureManager().release(TEXTURE);texture=null;}}
    private static int base(int value,int step){return Math.floorDiv(value,step)*step-GRID*step/2;}
    public static void tick(){dev.emersonoxy.rpg.RpgProfiler.run("minimap",SimpleMinimap::tickBody);}
    private static void tickBody(){
        var mc=Minecraft.getInstance();boolean preview=mc.screen instanceof MinimapSettingsScreen||mc.screen instanceof MinimapPositionScreen;
        if(mc.player==null||mc.level==null||mc.isPaused()||!preview&&(!DynamicHud.enabled(DynamicHud.Part.MINIMAP)||DynamicHud.mode()==DynamicHud.Mode.HIDDEN))return;
        int step=MinimapOptions.step();if(dimension!=mc.level.dimension()||step!=lastStep){clear();dimension=mc.level.dimension();lastStep=step;}
        int surface=mc.level.getHeight(Heightmap.Types.OCEAN_FLOOR,mc.player.getBlockX(),mc.player.getBlockZ());
        boolean underground=mc.level.dimensionType().hasCeiling()||surface>mc.player.getBlockY()+6;int slice=underground?Math.floorDiv(mc.player.getBlockY(),2)*2:Integer.MIN_VALUE;
        if(cave!=underground||layer!=slice){COLORS.clear();cursor=0;uploaded=-1;retainPixels=texture!=null;cave=underground;layer=slice;}
        int bx=base(mc.player.getBlockX(),step),bz=base(mc.player.getBlockZ(),step);long now=mc.level.getGameTime();
        for(int i=0;i<128;i++){
            int pixel=cursor;cursor=(cursor+1)%(GRID*GRID);int x=bx+pixel%GRID*step,z=bz+pixel/GRID*step;long key=BlockPos.asLong(x,0,z);
            if(!mc.level.getChunkSource().hasChunk(x>>4,z>>4)){COLORS.remove(key);continue;}
            var previous=COLORS.get(key);if(previous!=null&&now-previous.updated()<100)continue;
            int y=cave?floor(x,z):mc.level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;
            if(y==Integer.MIN_VALUE){COLORS.put(key,new Sample(0xFF121B24,now));continue;}var pos=new BlockPos(x,y,z);var color=mc.level.getBlockState(pos).getMapColor(mc.level,pos);
            int slope=0;if(mc.level.getChunkSource().hasChunk(x>>4,(z-step)>>4)){int neighbor=cave?floor(x,z-step):mc.level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z-step)-1;if(neighbor!=Integer.MIN_VALUE)slope=y-neighbor;}
            int value=color.calculateARGBColor(slope>0?MapColor.Brightness.HIGH:slope<0?MapColor.Brightness.LOW:MapColor.Brightness.NORMAL);
            if(cave&&mc.level.getBlockState(new BlockPos(x,layer+1,z)).isSolidRender()){int a=value;value=0xFF000000|((a>>16&255)*2/5<<16)|((a>>8&255)*2/5<<8)|(a&255)*2/5;}
            COLORS.put(key,new Sample(value==0?0xFF25313B:value,now));if(COLORS.size()>16384)COLORS.remove(COLORS.keySet().iterator().next());
        }
        if(texture==null){texture=new DynamicTexture(()->"Oxy RPG minimap",new NativeImage(GRID,GRID,false));mc.getTextureManager().register(TEXTURE,texture);}
        if(now-uploaded>=4||bx!=lastX||bz!=lastZ||uploadedRound!=MinimapOptions.round){var image=texture.getPixels();for(int z=0;z<GRID;z++)for(int x=0;x<GRID;x++){var sample=COLORS.get(BlockPos.asLong(bx+x*step,0,bz+z*step));double radius=GRID/2.0,dx=x+.5-radius,dz=z+.5-radius;image.setPixel(x,z,MinimapOptions.round&&dx*dx+dz*dz>radius*radius?0:sample==null?(retainPixels&&bx==lastX&&bz==lastZ&&mc.level.getChunkSource().hasChunk((bx+x*step)>>4,(bz+z*step)>>4)?image.getPixel(x,z):((x/4+z/4)%2==0?0xFF1C2631:0xFF202C37)):sample.color());}texture.upload();uploadedRound=MinimapOptions.round;uploaded=now;lastX=bx;lastZ=bz;}
    }
    private static int floor(int x,int z){
        var world=Minecraft.getInstance().level;int anchor=Math.clamp(layer+1,world.getMinY()+1,world.getMaxY()-2);var pos=new BlockPos.MutableBlockPos(x,anchor,z);
        if(world.getBlockState(pos).isSolidRender())return anchor;
        // Stay in this air/fluid pocket. Its first solid floor is shown; other cave levels are not leaked.
        for(int y=anchor;y>=Math.max(world.getMinY(),anchor-24);y--){pos.setY(y);var state=world.getBlockState(pos);if(!state.isAir()&&(state.isSolidRender()||!state.getFluidState().isEmpty()))return y;}
        return Integer.MIN_VALUE;
    }
    public static Bounds bounds(int width,int height){
        MinimapOptions.load();int size=Math.max(40,Math.min(MinimapOptions.size,Math.min(width-20,height-78))),h=size+(MinimapOptions.coordinates?30:18),w=size+8;
        boolean right=MinimapOptions.corner%2==0,bottom=MinimapOptions.corner>=2;
        int x=MinimapOptions.manual?(int)(MinimapOptions.x*Math.max(0,width-w)):right?width-w-(int)MinimapOptions.offsetX:(int)MinimapOptions.offsetX;
        int y=MinimapOptions.manual?(int)(MinimapOptions.y*Math.max(0,height-h)):bottom?height-h-(int)MinimapOptions.offsetY:(int)MinimapOptions.offsetY;
        x=Math.clamp(x,0,Math.max(0,width-w));y=Math.clamp(y,0,Math.max(0,height-h));
        var mc=Minecraft.getInstance();if(MinimapOptions.avoidEffects&&mc.player!=null&&DynamicHud.enabled(DynamicHud.Part.EFFECTS)){
            int positive=0,negative=0;for(var effect:mc.player.getActiveEffects())if(effect.showIcon()&&net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions.of(effect).isVisibleInGui(effect)){if(effect.getEffect().value().isBeneficial())positive++;else negative++;}
            int rows=negative>0?52:positive>0?26:0,fx=width-25*Math.max(positive,negative),fy=rows+(mc.isDemo()?15:0);
            if(rows>0&&x+w>fx&&y<fy+5){int safe=fy+5;if(safe+h<=height)y=safe;else x=Math.max(0,fx-w-5);}
        }
        return new Bounds(x,y,size,h);
    }
    public static void render(GuiGraphics g){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui||mc.getDebugOverlay().showDebugScreen()||mc.screen!=null||mc.player.isSpectator()||DynamicHud.mode()==DynamicHud.Mode.HIDDEN||!DynamicHud.enabled(DynamicHud.Part.MINIMAP))return;draw(g,bounds(g.guiWidth(),g.guiHeight()));}
    public static void draw(GuiGraphics g,Bounds b){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;int x=b.x(),y=b.y(),size=b.size(),mx=x+4,my=y+14;
        HudOpacity.begin((float)MinimapOptions.opacity);
        if(MinimapOptions.round){SkillGlyphs.disk(g,mx+size/2,my+size/2,size/2+4,0xFF253746);SkillGlyphs.disk(g,mx+size/2,my+size/2,size/2+2,0xFFADC7CE);}
        else{g.fill(x,y,x+size+8,y+b.height(),0xED111B27);RpgUi.border(g,x,y,size+8,b.height(),0xFF657887);g.fill(x+1,y+1,x+size+7,y+2,0xFFADC7CE);}
        g.drawCenteredString(mc.font,"N",x+size/2+4,y+3,RpgUi.GOLD);
        {
            if(texture!=null)g.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,mx,my,0,0,size,size,GRID,GRID,GRID,GRID);
            else g.fill(mx,my,mx+size,my+size,0xFF202C37);
            int step=MinimapOptions.step(),bx=base(mc.player.getBlockX(),step),bz=base(mc.player.getBlockZ(),step);int px=mx+(int)((mc.player.getX()-bx)/(GRID*step)*size),pz=my+(int)((mc.player.getZ()-bz)/(GRID*step)*size);
            SkillGlyphs.marker(g,px,pz,mc.player.getYRot());
            g.drawString(mc.font,"W",mx+2,my+size/2-4,0xFFFFFFFF);g.drawString(mc.font,"E",mx+size-7,my+size/2-4,0xFFFFFFFF);g.drawCenteredString(mc.font,"S",mx+size/2,my+size-10,0xFFFFFFFF);
        }
        if(MinimapOptions.coordinates){if(MinimapOptions.round)g.fill(mx,my+size+3,mx+size,my+size+17,0xDC111B27);String label=(cave?"↓ ":"")+mc.player.getBlockX()+"  "+mc.player.getBlockY()+"  "+mc.player.getBlockZ();g.drawCenteredString(mc.font,mc.font.plainSubstrByWidth(label,size),mx+size/2,my+size+5,RpgUi.TEXT);}
        HudOpacity.end();
    }
}
