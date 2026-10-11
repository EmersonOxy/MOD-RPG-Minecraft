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
    private static final int[] PIXELS=java.util.stream.IntStream.range(0,GRID*GRID).boxed().sorted(java.util.Comparator.comparingInt(i->{int x=i%GRID-GRID/2,z=i/GRID-GRID/2;return x*x+z*z;})).mapToInt(Integer::intValue).toArray();
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("oxyrpg","minimap_surface");
    private record Sample(int color,long updated,int height){}
    public record Bounds(int x,int y,int size,int height){}
    private record LayerKey(boolean cave,int floor){}
    private static final LinkedHashMap<LayerKey,LinkedHashMap<Long,Sample>> LAYERS=new LinkedHashMap<>(8,.75f,true);
    private static LinkedHashMap<Long,Sample> COLORS=new LinkedHashMap<>();
    private static boolean dirty;private static int warmup;
    private static long layerCheck=-1,candidateSince;
    private static boolean candidateCave,layerInitialized;
    private static int candidateLayer=Integer.MIN_VALUE,selectedMode=-1;
    private static int cursor,lastStep,lastX=Integer.MIN_VALUE,lastZ;private static long uploaded=-1;private static Object dimension;
    private static DynamicTexture texture;private static boolean uploadedRound,cave,retainPixels;private static int layer=Integer.MIN_VALUE;
    private SimpleMinimap(){}
    public static void clear(){LAYERS.clear();COLORS=new LinkedHashMap<>();cursor=warmup=0;dirty=true;uploaded=-1;lastX=Integer.MIN_VALUE;dimension=null;retainPixels=false;cave=false;layer=Integer.MIN_VALUE;layerCheck=-1;selectedMode=-1;candidateLayer=Integer.MIN_VALUE;layerInitialized=false;if(texture!=null){Minecraft.getInstance().getTextureManager().release(TEXTURE);texture=null;}}
    private static int base(int value,int step){return Math.floorDiv(value,step)*step-GRID*step/2;}
    public static void tick(){dev.emersonoxy.rpg.RpgProfiler.run("minimap",SimpleMinimap::tickBody);}
    private static void tickBody(){
        var mc=Minecraft.getInstance();boolean preview=mc.screen instanceof MinimapSettingsScreen||mc.screen instanceof MinimapPositionScreen;
        if(mc.player==null||mc.level==null||mc.isPaused()||!preview&&(!DynamicHud.enabled(DynamicHud.Part.MINIMAP)||DynamicHud.mode()==DynamicHud.Mode.HIDDEN))return;
        int step=MinimapOptions.step();if(dimension!=mc.level.dimension()||step!=lastStep){clear();dimension=mc.level.dimension();lastStep=step;}
        if(!mc.level.getChunkSource().hasChunk(mc.player.getBlockX()>>4,mc.player.getBlockZ()>>4))return;
        warmup++;
        updateLayer();
        int bx=base(mc.player.getBlockX(),step),bz=base(mc.player.getBlockZ(),step);long now=mc.level.getGameTime();
        // Leave time for chunk building when entering a world. Bound both samples and wall time.
        long deadline=System.nanoTime()+1_000_000;int budget=warmup<60?32:64;
        for(int i=0;i<budget;i++){
            if(i>0&&System.nanoTime()>=deadline)break;
            int pixel=PIXELS[cursor];cursor=(cursor+1)%(GRID*GRID);int x=bx+pixel%GRID*step,z=bz+pixel/GRID*step;long key=BlockPos.asLong(x,0,z);
            if(!mc.level.getChunkSource().hasChunk(x>>4,z>>4)){if(COLORS.remove(key)!=null)dirty=true;continue;}
            var previous=COLORS.get(key);if(previous!=null&&now-previous.updated()<100)continue;
            int y=cave?floor(x,z):mc.level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;
            if(y==Integer.MIN_VALUE){put(key,new Sample(0xFF121B24,now,y));continue;}var pos=new BlockPos(x,y,z);var color=mc.level.getBlockState(pos).getMapColor(mc.level,pos);
            int slope=0;if(mc.level.getChunkSource().hasChunk(x>>4,(z-step)>>4)){int neighbor=cave?y:mc.level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z-step)-1;if(neighbor!=Integer.MIN_VALUE)slope=y-neighbor;}
            int value=color.calculateARGBColor(slope>0?MapColor.Brightness.HIGH:slope<0?MapColor.Brightness.LOW:MapColor.Brightness.NORMAL);
            if(cave&&mc.level.getBlockState(new BlockPos(x,layer,z)).isSolidRender()){int a=value;value=0xFF000000|((a>>16&255)*2/5<<16)|((a>>8&255)*2/5<<8)|(a&255)*2/5;}
            put(key,new Sample(value==0?0xFF25313B:value,now,y));
        }
        if(texture==null){texture=new DynamicTexture(()->"Oxy RPG minimap",new NativeImage(GRID,GRID,false));mc.getTextureManager().register(TEXTURE,texture);}
        if(dirty&&now-uploaded>=4||bx!=lastX||bz!=lastZ||uploadedRound!=MinimapOptions.round){var image=texture.getPixels();for(int z=0;z<GRID;z++)for(int x=0;x<GRID;x++){var sample=COLORS.get(BlockPos.asLong(bx+x*step,0,bz+z*step));double radius=GRID/2.0,dx=x+.5-radius,dz=z+.5-radius;image.setPixel(x,z,MinimapOptions.round&&dx*dx+dz*dz>radius*radius?0:sample==null?(retainPixels&&bx==lastX&&bz==lastZ&&mc.level.getChunkSource().hasChunk((bx+x*step)>>4,(bz+z*step)>>4)?image.getPixel(x,z):((x/4+z/4)%2==0?0xFF1C2631:0xFF202C37)):sample.color());}texture.upload();dirty=false;uploadedRound=MinimapOptions.round;uploaded=now;lastX=bx;lastZ=bz;}
    }
    /** Keep one cave pocket across jumps, slopes and flight. Only a sustained floor change selects a new slice. */
    private static void updateLayer(){
        var mc=Minecraft.getInstance();long now=mc.level.getGameTime();int mode=MinimapOptions.layerMode;
        boolean forced=selectedMode!=mode;selectedMode=mode;
        if(!forced&&layerCheck>=0&&now-layerCheck<10)return;layerCheck=now;
        boolean next=mode==2||mode==0&&(mc.level.dimensionType().hasCeiling()&&mc.player.getBlockY()<mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING,mc.player.getBlockX(),mc.player.getBlockZ())||covered());
        int floor=playerFloor(),anchor=floor==Integer.MIN_VALUE?mc.player.getBlockY():floor+1;
        // Airborne motion keeps the previous slice; gradual slopes tolerate five blocks.
        int slice=next?(cave&&layer!=Integer.MIN_VALUE&&(floor==Integer.MIN_VALUE||Math.abs(anchor-layer)<=5)?layer:anchor):Integer.MIN_VALUE;
        if(!forced&&cave==next&&layer==slice){candidateLayer=slice;candidateCave=next;candidateSince=now;return;}
        if(!forced&&layerInitialized){
            if(candidateCave!=next||next&&Math.abs((long)candidateLayer-slice)>5){candidateCave=next;candidateLayer=slice;candidateSince=now;return;}
            if(now-candidateSince<20)return;
        }else if(!forced&&!next&&cave==next)return;
        // Switching a floor never erases the surface or a previously visited floor.
        COLORS=LAYERS.computeIfAbsent(new LayerKey(next,slice),key->new LinkedHashMap<>());
        if(LAYERS.size()>4)LAYERS.remove(LAYERS.keySet().iterator().next());
        cursor=0;dirty=true;retainPixels=cave==next&&texture!=null;
        cave=next;layer=slice;candidateLayer=slice;candidateCave=next;candidateSince=now;layerInitialized=true;
    }
    private static boolean covered(){
        var mc=Minecraft.getInstance();var head=mc.player.blockPosition().above(2);
        if(mc.level.canSeeSky(head))return false;
        // Canopy height alone must not switch a surface map to a cave map.
        var pos=new BlockPos.MutableBlockPos(head.getX(),head.getY(),head.getZ());
        int thickness=0;
        for(int y=head.getY();y<Math.min(mc.level.getMaxY(),head.getY()+64);y++){
            pos.setY(y);var state=mc.level.getBlockState(pos);
            boolean roof=!state.is(net.minecraft.tags.BlockTags.LEAVES)&&!state.is(net.minecraft.tags.BlockTags.LOGS)&&state.isSolidRender();
            thickness=roof?thickness+1:0;
            // A thin building roof or tree canopy should not turn surface exploration into cave mode.
            if(thickness>=3)return true;
        }
        return false;
    }
    private static int playerFloor(){
        var mc=Minecraft.getInstance();var pos=mc.player.blockPosition().mutable();
        for(int y=mc.player.getBlockY();y>=Math.max(mc.level.getMinY(),mc.player.getBlockY()-24);y--){
            pos.setY(y);var state=mc.level.getBlockState(pos);
            if(!state.getCollisionShape(mc.level,pos).isEmpty()||!state.getFluidState().isEmpty())return y;
        }
        return Integer.MIN_VALUE;
    }
    private static void put(long key,Sample sample){var old=COLORS.put(key,sample);if(old==null||old.color()!=sample.color())dirty=true;if(COLORS.size()>16384)COLORS.remove(COLORS.keySet().iterator().next());}
    private static int floor(int x,int z){
        var world=Minecraft.getInstance().level;int anchor=Math.clamp(layer,world.getMinY()+1,world.getMaxY()-2);var pos=new BlockPos.MutableBlockPos(x,anchor,z);
        if(world.getBlockState(pos).isSolidRender()){
            // Follow small rises inside the same pocket rather than drawing a flat slab of rock.
            int air=Integer.MIN_VALUE;
            for(int y=anchor+1;y<=Math.min(anchor+6,world.getMaxY()-2);y++){
                pos.setY(y);if(!world.getBlockState(pos).isSolidRender()&&!world.getBlockState(pos.above()).isSolidRender()){air=y;break;}
            }
            if(air==Integer.MIN_VALUE)return anchor;anchor=air;
        }
        // Stay in this air/fluid pocket. Its first solid floor is shown; other cave levels are not leaked.
        for(int y=anchor;y>=Math.max(world.getMinY(),anchor-24);y--){pos.setY(y);var state=world.getBlockState(pos);if(!state.isAir()&&(!state.getCollisionShape(world,pos).isEmpty()||!state.getFluidState().isEmpty()))return y;}
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
