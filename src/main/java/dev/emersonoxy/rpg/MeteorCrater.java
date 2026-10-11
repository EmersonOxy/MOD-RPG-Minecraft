package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Large impacts are processed over several ticks, with a global block/entity budget. */
public final class MeteorCrater {
    private static final ArrayDeque<Crater> QUEUE=new ArrayDeque<>();
    private static final class Crater {
        final ServerLevel world;final ServerPlayer owner;final Vec3 center;final List<BlockPos> blocks;final double force;final int limit,maxDebris;int cursor,broken,debris;
        Crater(ServerPlayer p,List<BlockPos> positions,double force,boolean capstone){world=p.level();owner=p;center=p.position();blocks=positions;this.force=force;limit=Math.min(1800,100+(int)(force*140));maxDebris=Math.min(160,24+(int)(force*(capstone?14:10)));}
    }
    private MeteorCrater(){}
    public static void enqueue(ServerPlayer p,double radius,double force,boolean capstone){
        int r=(int)Math.ceil(radius),depth=Math.min(7,2+(int)(force*.45));var origin=p.blockPosition().below();var positions=new ArrayList<BlockPos>();
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)for(int y=0;y>=-depth;y--){double edge=(x*x+z*z)/(radius*radius)+(y*y)/(double)(depth*depth);if(edge<=1)positions.add(origin.offset(x,y,z));}
        positions.sort(Comparator.comparingDouble(pos->pos.distSqr(origin)));QUEUE.add(new Crater(p,positions,force,capstone));
    }
    public static void tick(ServerTickEvent.Post event){
        int checks=0,changes=0,entities=0,rounds=QUEUE.size();
        while(rounds-->0&&!QUEUE.isEmpty()&&checks<600&&changes<120){var c=QUEUE.removeFirst();int local=0;
            if(c.world.getServer()!=event.getServer())continue;
            while(c.cursor<c.blocks.size()&&c.broken<c.limit&&checks++<600&&changes<120&&local++<160){
                var pos=c.blocks.get(c.cursor++);if(!c.world.hasChunkAt(pos)||!c.world.mayInteract(c.owner,pos)||!c.owner.mayBuild())continue;var block=c.world.getBlockState(pos);
                if(block.isAir()||block.hasBlockEntity()||!block.getFluidState().isEmpty()||block.getDestroySpeed(c.world,pos)<0||block.getDestroySpeed(c.world,pos)>10)continue;
                if(net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(c.world,c.owner.gameMode.getGameModeForPlayer(),c.owner,pos,block).isCanceled())continue;
                if(c.debris<c.maxDebris&&entities<12&&c.broken%3==0){
                    var debris=FallingBlockEntity.fall(c.world,pos,block);var away=Vec3.atCenterOf(pos).subtract(c.center).multiply(1,0,1).normalize();
                    debris.setDeltaMovement(away.scale(.5+c.force*.08).add(0,.8+c.force*.055,0));c.debris++;entities++;
                }else c.world.destroyBlock(pos,true,c.owner);
                c.broken++;changes++;
            }
            if(c.cursor<c.blocks.size()&&c.broken<c.limit)QUEUE.addLast(c);
        }
    }
    public static void stop(ServerStoppedEvent event){QUEUE.clear();}
}
