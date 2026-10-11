package dev.emersonoxy.rpg.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/** Surface-only map. Samples loaded chunks in small batches and never requests new chunks. */
public final class SimpleMinimap {
    private static final int GRID=32,STEP=4;
    private static final LinkedHashMap<Long,Integer> COLORS=new LinkedHashMap<>();
    private static final Map<Long,Long> UPDATED=new HashMap<>();
    private static int cursor;
    private SimpleMinimap(){}
    public static void clear(){COLORS.clear();UPDATED.clear();cursor=0;}
    private static int base(int value){return Math.floorDiv(value,STEP)*STEP-GRID*STEP/2;}
    public static void tick(){dev.emersonoxy.rpg.RpgProfiler.run("minimap",SimpleMinimap::tickBody);}
    private static void tickBody(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.isPaused()||!DynamicHud.enabled(DynamicHud.Part.MINIMAP)||DynamicHud.mode()==DynamicHud.Mode.HIDDEN)return;
        if(mc.level.dimensionType().hasCeiling())return;
        int bx=base(mc.player.getBlockX()),bz=base(mc.player.getBlockZ());long now=mc.level.getGameTime();
        for(int i=0;i<64;i++){
            int pixel=cursor++%(GRID*GRID),x=bx+(pixel%GRID)*STEP,z=bz+(pixel/GRID)*STEP;long key=BlockPos.asLong(x,0,z);
            if(!mc.level.getChunkSource().hasChunk(x>>4,z>>4)){COLORS.remove(key);UPDATED.remove(key);continue;}
            if(now-UPDATED.getOrDefault(key,-1000L)<100)continue;
            int y=mc.level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;var pos=new BlockPos(x,y,z);
            var color=mc.level.getBlockState(pos).getMapColor(mc.level,pos);int value=color.calculateARGBColor(MapColor.Brightness.NORMAL);
            COLORS.put(key,value==0?0xFF263344:value);UPDATED.put(key,now);
            if(COLORS.size()>4096){long old=COLORS.keySet().iterator().next();COLORS.remove(old);UPDATED.remove(old);}
        }
    }
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui||mc.screen!=null||mc.player.isSpectator()||DynamicHud.mode()==DynamicHud.Mode.HIDDEN||!DynamicHud.enabled(DynamicHud.Part.MINIMAP))return;
        int x=g.guiWidth()-84,y=8,bx=base(mc.player.getBlockX()),bz=base(mc.player.getBlockZ());
        RpgUi.panel(g,x,y,76,94);g.drawCenteredString(mc.font,"N",x+38,y+5,RpgUi.GOLD);
        if(mc.level.dimensionType().hasCeiling()){g.drawCenteredString(mc.font,"Sem",x+38,y+33,RpgUi.MUTED);g.drawCenteredString(mc.font,"superfície",x+38,y+44,RpgUi.MUTED);return;}
        // Merge runs of identical pixels, rather than submit thousands of individual draws.
        for(int row=0;row<GRID;row++)for(int col=0;col<GRID;){
            int color=COLORS.getOrDefault(BlockPos.asLong(bx+col*STEP,0,bz+row*STEP),0xFF18202B),end=col+1;
            while(end<GRID&&COLORS.getOrDefault(BlockPos.asLong(bx+end*STEP,0,bz+row*STEP),0xFF18202B)==color)end++;
            g.fill(x+6+col*2,y+17+row*2,x+6+end*2,y+19+row*2,color);col=end;
        }
        int px=x+6+(int)((mc.player.getX()-bx)/STEP*2),pz=y+17+(int)((mc.player.getZ()-bz)/STEP*2);
        double angle=Math.toRadians(mc.player.getYRot());int dx=(int)Math.round(-Math.sin(angle)*5),dz=(int)Math.round(Math.cos(angle)*5);
        for(int i=1;i<=5;i++)g.fill(px+dx*i/5,pz+dz*i/5,px+dx*i/5+1,pz+dz*i/5+1,RpgUi.GOLD);
        g.fill(px-1,pz-1,px+2,pz+2,0xFFFFFFFF);
        String coordinates=mc.player.getBlockX()+", "+mc.player.getBlockZ();g.drawCenteredString(mc.font,mc.font.plainSubstrByWidth(coordinates,68),x+38,y+82,RpgUi.MUTED);
    }
}
