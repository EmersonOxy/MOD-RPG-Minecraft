package dev.emersonoxy.rpg.client;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
/** Native models temporarily leave their cached section and bend in an extracted render state. */
public final class VegetationMotion {
    private record Bend(BlockState state,long start,double dx,double dz){}
    private record Draw(BlockState state,Vec3 relative,int light,float weight,double dx,double dz,boolean upper){}
    private static final ContextKey<List<Draw>> DRAW=new ContextKey<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","vegetation"));
    private static final Map<BlockPos,Bend> BENDS=new ConcurrentHashMap<>();
    private static final ByteBufferBuilder STORAGE=new ByteBufferBuilder(65536);
    private static final MultiBufferSource.BufferSource BUFFER=MultiBufferSource.immediate(STORAGE);
    private static Object world;
    private VegetationMotion(){}
    private static boolean plant(BlockState s){return s.is(Blocks.SHORT_GRASS)||s.is(Blocks.FERN)||s.is(Blocks.TALL_GRASS)||s.is(Blocks.LARGE_FERN);}
    public static boolean dynamic(BlockPos pos,BlockState state){if(BENDS.isEmpty())return false;var bend=BENDS.get(pos);return bend!=null&&bend.state.equals(state);}
    private static void dirty(BlockPos p){var mc=Minecraft.getInstance();mc.levelRenderer.setSectionDirty(net.minecraft.core.SectionPos.blockToSectionCoord(p.getX()),net.minecraft.core.SectionPos.blockToSectionCoord(p.getY()),net.minecraft.core.SectionPos.blockToSectionCoord(p.getZ()));}
    public static void tick(){var mc=Minecraft.getInstance();if(mc.level!=world){BENDS.clear();world=mc.level;}if(mc.level==null||mc.player==null)return;long now=mc.level.getGameTime();
        for(var entry:new ArrayList<>(BENDS.entrySet()))if(!AmbientOptions.vegetation||now-entry.getValue().start>48||!mc.level.hasChunkAt(entry.getKey())||!mc.level.getBlockState(entry.getKey()).equals(entry.getValue().state)){BENDS.remove(entry.getKey());dirty(entry.getKey());}
        if(!AmbientOptions.vegetation||now%4!=0)return;int added=0;
        var actors=mc.level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,mc.player.getBoundingBox().inflate(8),e->e.isAlive()&&e.getDeltaMovement().horizontalDistanceSqr()>.0001);if(!actors.contains(mc.player))actors.add(mc.player);
        for(var e:actors){if(e.position().distanceToSqr(mc.player.position())>80)continue;var motion=e.getDeltaMovement();if(motion.horizontalDistanceSqr()<.0001)continue;var center=e.blockPosition();
            for(var p:BlockPos.betweenClosed(center.offset(-1,0,-1),center.offset(1,1,1))){if(BENDS.size()>=48||added>=4)return;var s=mc.level.getBlockState(p);if(!plant(s)||Vec3.atCenterOf(p).multiply(1,0,1).distanceToSqr(e.position().multiply(1,0,1))>.9)continue;
                boolean tall=s.hasProperty(net.minecraft.world.level.block.DoublePlantBlock.HALF);if(tall&&s.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)continue;
                var at=p.immutable();if(!BENDS.containsKey(at)){var dir=motion.multiply(1,0,1).normalize();BENDS.put(at,new Bend(s,now,dir.x,dir.z));dirty(at);added++;if(tall&&BENDS.size()<48){var up=at.above();var upper=mc.level.getBlockState(up);if(plant(upper)){BENDS.put(up,new Bend(upper,now,dir.x,dir.z));dirty(up);added++;}}}}
        }
    }
    public static void extract(ExtractLevelRenderStateEvent event){var draws=new ArrayList<Draw>();var camera=event.getCamera().getPosition();long now=event.getLevel().getGameTime();
        for(var entry:BENDS.entrySet()){var pos=entry.getKey();if(pos.distToCenterSqr(camera)>24*24||!event.getLevel().hasChunkAt(pos))continue;var b=entry.getValue();if(!event.getLevel().getBlockState(pos).equals(b.state))continue;float age=now-b.start,weight=age<5?age/5:Math.max(0,1-(age-5)/43);draws.add(new Draw(b.state,Vec3.atLowerCornerOf(pos).subtract(camera),LevelRenderer.getLightColor(event.getLevel(),pos),weight,b.dx,b.dz,b.state.hasProperty(net.minecraft.world.level.block.DoublePlantBlock.HALF)&&b.state.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));}
        event.getRenderState().setRenderData(DRAW,List.copyOf(draws));
    }
    public static void render(RenderLevelStageEvent.AfterEntities event){List<Draw> draws=event.getLevelRenderState().getRenderData(DRAW);if(draws==null||draws.isEmpty())return;var mc=Minecraft.getInstance();var pose=event.getPoseStack();
        for(var d:draws){pose.pushPose();pose.translate(d.relative.x+.5,d.relative.y-(d.upper?1:0),d.relative.z+.5);pose.mulPose(new org.joml.Quaternionf().rotationAxis(d.weight*.65f,(float)d.dz,0,(float)-d.dx));pose.scale(1,1-d.weight*.35f,1);pose.translate(-.5,d.upper?1:0,-.5);mc.getBlockRenderer().renderSingleBlock(d.state,pose,BUFFER,d.light,OverlayTexture.NO_OVERLAY);pose.popPose();}BUFFER.endBatch();
    }
}
