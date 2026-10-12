package dev.emersonoxy.rpg.client;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
/** Hold under living bodies, recover after release. Original seed, tint, light and offset remain native. */
public final class VegetationMotion {
    private record Bend(BlockState state,long start,long contact,double dx,double dz){}
    private record Draw(BlockState state,BlockPos pos,List<BlockModelPart> parts,Vec3 relative,float weight,double dx,double dz,boolean upper){}
    private static final ContextKey<List<Draw>> DRAW=new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","vegetation"));
    private static final Map<BlockPos,Bend> BENDS=new ConcurrentHashMap<>();
    private static final ByteBufferBuilder STORAGE=new ByteBufferBuilder(65536);
    private static final MultiBufferSource.BufferSource BUFFER=MultiBufferSource.immediate(STORAGE);
    private static Object world;
    private VegetationMotion(){}
    private static boolean plant(BlockState s){return s.is(Blocks.SHORT_GRASS)||s.is(Blocks.FERN)||s.is(Blocks.TALL_GRASS)||s.is(Blocks.LARGE_FERN);}
    public static boolean dynamic(BlockPos pos,BlockState state){var bend=BENDS.get(pos);return bend!=null&&bend.state.equals(state);}
    private static void dirty(BlockPos p){var mc=Minecraft.getInstance();mc.levelRenderer.setSectionDirty(net.minecraft.core.SectionPos.blockToSectionCoord(p.getX()),net.minecraft.core.SectionPos.blockToSectionCoord(p.getY()),net.minecraft.core.SectionPos.blockToSectionCoord(p.getZ()));}
    private static int press(BlockPos pos,BlockState state,long now,Vec3 direction,int budget){
        var old=BENDS.get(pos);if(old!=null){BENDS.put(pos,new Bend(state,old.start,now,old.dx,old.dz));return 0;}
        if(budget<=0||BENDS.size()>=48)return 0;
        BENDS.put(pos.immutable(),new Bend(state,now,now,direction.x,direction.z));dirty(pos);return 1;
    }
    public static void tick(){var mc=Minecraft.getInstance();if(mc.level!=world){BENDS.clear();world=mc.level;}if(mc.level==null||mc.player==null||mc.isPaused())return;long now=mc.level.getGameTime();
        for(var entry:new ArrayList<>(BENDS.entrySet()))if(!AmbientOptions.vegetation||now-entry.getValue().contact>34||!mc.level.hasChunkAt(entry.getKey())||!mc.level.getBlockState(entry.getKey()).equals(entry.getValue().state)){BENDS.remove(entry.getKey());dirty(entry.getKey());}
        if(!AmbientOptions.vegetation||now%4!=0)return;int added=0,visited=0;
        var actors=mc.level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,mc.player.getBoundingBox().inflate(8),e->e.isAlive());actors.remove(mc.player);actors.addFirst(mc.player);
        for(var e:actors){if(visited++>=64)break;if(e.position().distanceToSqr(mc.player.position())>80)continue;var motion=e.getDeltaMovement();var center=e.blockPosition();
            var direction=motion.horizontalDistanceSqr()>.0001?motion.multiply(1,0,1).normalize():e.getViewVector(1).multiply(1,0,1).normalize();if(direction.lengthSqr()<.0001)direction=new Vec3(0,0,1);
            for(var p:BlockPos.betweenClosed(center.offset(-1,-1,-1),center.offset(1,1,1))){var state=mc.level.getBlockState(p);if(!plant(state))continue;
                boolean tall=state.hasProperty(net.minecraft.world.level.block.DoublePlantBlock.HALF);if(tall&&state.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)continue;
                var volume=new net.minecraft.world.phys.AABB(p).inflate(-.12,0,-.12);if(tall)volume=volume.expandTowards(0,1,0);if(!e.getBoundingBox().intersects(volume))continue;
                added+=press(p,state,now,direction,4-added);
                if(tall){var upper=mc.level.getBlockState(p.above());if(plant(upper))added+=press(p.above(),upper,now,direction,4-added);}
            }
        }
    }
    public static void extract(ExtractLevelRenderStateEvent event){var draws=new ArrayList<Draw>();var camera=event.getCamera().getPosition();double now=event.getLevel().getGameTime()+event.getCamera().getPartialTickTime();var renderer=Minecraft.getInstance().getBlockRenderer();
        for(var entry:BENDS.entrySet()){var pos=entry.getKey();if(pos.distToCenterSqr(camera)>24*24||!event.getLevel().hasChunkAt(pos))continue;var b=entry.getValue();if(!event.getLevel().getBlockState(pos).equals(b.state))continue;
            float press=(float)Math.clamp((now-b.start)/6,0,1),release=(float)Math.clamp((now-b.contact-4)/30,0,1);release=release*release*(3-2*release);float weight=press*(1-release);
            var parts=renderer.getBlockModel(b.state).collectParts(event.getLevel(),pos,b.state,RandomSource.create(b.state.getSeed(pos)));
            draws.add(new Draw(b.state,pos,List.copyOf(parts),Vec3.atLowerCornerOf(pos).subtract(camera),weight,b.dx,b.dz,b.state.hasProperty(net.minecraft.world.level.block.DoublePlantBlock.HALF)&&b.state.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));}
        event.getRenderState().setRenderData(DRAW,List.copyOf(draws));
    }
    public static void render(RenderLevelStageEvent.AfterEntities event){List<Draw> draws=event.getLevelRenderState().getRenderData(DRAW);if(draws==null||draws.isEmpty())return;var mc=Minecraft.getInstance();if(mc.level==null)return;var pose=event.getPoseStack();
        for(var d:draws){pose.pushPose();pose.translate(d.relative.x+.5,d.relative.y-(d.upper?1:0),d.relative.z+.5);pose.mulPose(new org.joml.Quaternionf().rotationAxis(d.weight*.28f,(float)d.dz,0,(float)-d.dx));pose.scale(1,1-d.weight*.82f,1);pose.translate(-.5,d.upper?1:0,-.5);
            mc.getBlockRenderer().getModelRenderer().tesselateBlock(mc.level,d.parts,d.state,d.pos,pose,type->BUFFER.getBuffer(net.neoforged.neoforge.client.RenderTypeHelper.getMovingBlockRenderType(type)),false,OverlayTexture.NO_OVERLAY);pose.popPose();}BUFFER.endBatch();
    }
}
