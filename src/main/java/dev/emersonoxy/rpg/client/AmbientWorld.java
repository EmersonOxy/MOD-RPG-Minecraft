package dev.emersonoxy.rpg.client;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.*;
import net.minecraft.world.level.block.Blocks;
/** Small sampling budgets in already loaded chunks; no terrain generation or network traffic. */
public final class AmbientWorld {
    private AmbientWorld(){}
    public static void tick(){AmbientOptions.load();VegetationMotion.tick();var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.isPaused())return;var level=mc.level;var p=mc.player;long now=level.getGameTime();
        if(AmbientOptions.leaves&&now%4==0&&mc.options.particles().get()!=net.minecraft.server.level.ParticleStatus.MINIMAL){
            for(int sample=0;sample<3;sample++){int x=p.getBlockX()+level.random.nextInt(25)-12,z=p.getBlockZ()+level.random.nextInt(25)-12;
                for(int y=p.getBlockY()+24;y>p.getBlockY()-3;y--){var pos=new BlockPos(x,y,z);if(!level.hasChunkAt(pos))break;var s=level.getBlockState(pos);if(!s.is(BlockTags.LEAVES)||level.getBlockState(pos.below()).is(BlockTags.LEAVES))continue;
                    int color=mc.getBlockColors().getColor(s,level,pos,0);if(s.is(Blocks.CHERRY_LEAVES))color=0xF2AFCB;else if(s.is(Blocks.PALE_OAK_LEAVES))color=0xA5B0A0;else if(color<0)color=0x75A84C;
                    AmbientLeaf.spawn(level,x+level.random.nextDouble(),y-.04,z+level.random.nextDouble(),color,s.is(Blocks.SPRUCE_LEAVES));break;
                }
            }
        }
        if(AmbientOptions.water&&mc.options.particles().get()!=net.minecraft.server.level.ParticleStatus.MINIMAL&&now%5==0&&!p.onGround()&&!p.isInWater()&&p.getDeltaMovement().horizontalDistanceSqr()>.015){
            for(int depth=0;depth<=3;depth++){var pos=p.blockPosition().below(depth);var fluid=level.getFluidState(pos);if(!fluid.is(FluidTags.WATER)||!level.getFluidState(pos.above()).isEmpty())continue;double y=pos.getY()+fluid.getHeight(level,pos)+.015;if(p.getY()-y<0||p.getY()-y>2.5)break;
                WaterWake.spawn(level,p.getX(),y,p.getZ());for(int side:new int[]{-1,1}){double dx=p.getDeltaMovement().z*side,dz=-p.getDeltaMovement().x*side;level.addParticle(ParticleTypes.SPLASH,p.getX()+dx,y+.03,p.getZ()+dz,dx*.2,.08,dz*.2);}break;
            }
        }
    }
}
