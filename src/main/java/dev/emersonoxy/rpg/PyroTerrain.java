package dev.emersonoxy.rpg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
/** Small real craters with nether scars; immutable/protected/container blocks are excluded. */
public final class PyroTerrain {
    private PyroTerrain(){}
    private static boolean allowed(ServerPlayer p,BlockPos pos){var w=p.level();if(!p.mayBuild()||!w.hasChunkAt(pos)||!w.mayInteract(p,pos))return false;var s=w.getBlockState(pos);float strength=s.getDestroySpeed(w,pos);return !s.isAir()&&!s.hasBlockEntity()&&s.getFluidState().isEmpty()&&strength>=0&&strength<=3&&!net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(w,p.gameMode.getGameModeForPlayer(),p,pos,s).isCanceled();}
    public static void impact(ServerPlayer p,Vec3 at,boolean main){if(!p.mayBuild())return;var w=p.level();var center=BlockPos.containing(at);double radius=main?1.8:1.25;int broken=0;
        for(var pos:BlockPos.betweenClosed(center.offset(-2,-2,-2),center.offset(2,2,2))){if(broken>=32)break;if(pos.distToCenterSqr(at)>radius*radius||!allowed(p,pos))continue;w.destroyBlock(pos,true,p);broken++;}
        int r=main?3:2,scarred=0;
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){if(x*x+z*z>r*r||scarred>=32)continue;
            for(int y=2;y>=-3;y--){var pos=center.offset(x,y,z);if(!w.hasChunkAt(pos))break;var s=w.getBlockState(pos);if(s.isAir()||!w.getBlockState(pos.above()).isAir())continue;
                boolean natural=s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(BlockTags.DIRT)||s.is(BlockTags.SAND)||s.is(Blocks.GRAVEL)||s.is(Blocks.NETHERRACK);
                if(natural&&allowed(p,pos)){w.setBlock(pos,(w.random.nextFloat()<.6f?Blocks.MAGMA_BLOCK:Blocks.NETHERRACK).defaultBlockState(),3);scarred++;}break;
            }
        }
    }
}
