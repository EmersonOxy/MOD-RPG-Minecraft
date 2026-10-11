package dev.emersonoxy.rpg;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
/** Small server-owned patrols in loaded outdoor terrain, separated from village raids. */
public final class RaiderPatrols {
    private RaiderPatrols(){}
    private static CompoundTag saved(ServerPlayer p){var root=p.getPersistentData();var tag=root.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);root.put(Player.PERSISTED_NBT_TAG,tag);return tag;}
    public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%200!=0||!RpgConfig.patrols()||!p.isAlive()||p.isSpectator()||p.isCreative()||p.isSleeping()||!p.level().dimension().equals(Level.OVERWORLD))return;
        var world=p.level();if(world.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING))return;
        long now=world.getGameTime();var data=saved(p);long next=data.getLongOr("oxy_raiders_next",0);
        if(next==0){data.putLong("oxy_raiders_next",now+3600+world.random.nextInt(2400));return;}if(now<next)return;
        data.putLong("oxy_raiders_next",now+1200);
        if(!world.canSeeSky(p.blockPosition())||!world.getEntitiesOfClass(Raider.class,p.getBoundingBox().inflate(128),m->m.getPersistentData().getBooleanOr("oxy_patrol",false)).isEmpty())return;
        // At most twelve already-loaded columns are considered per attempt. No worldgen is triggered.
        for(int attempt=0;attempt<12;attempt++){
            double angle=world.random.nextDouble()*Math.PI*2,radius=44+world.random.nextInt(25);int x=p.getBlockX()+(int)(Math.cos(angle)*radius),z=p.getBlockZ()+(int)(Math.sin(angle)*radius);
            if(!world.getChunkSource().hasChunk(x>>4,z>>4))continue;var pos=new BlockPos(x,world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
            if(!safe(world,pos)||world.isVillage(pos)||world.players().stream().anyMatch(other->other.distanceToSqr(pos.getX()+.5,pos.getY(),pos.getZ()+.5)<32*32))continue;
            int level=RpgAbilities.profile(p).level(),count=Math.min(5,3+level/40);boolean spawned=false;
            for(int i=0;i<count;i++){
                var spot=pos.offset((i%3)*2,0,(i/3)*2);if(!safe(world,spot))continue;
                Raider mob=(i==count-1&&level>=35?EntityType.VINDICATOR:EntityType.PILLAGER).create(world,EntitySpawnReason.PATROL);if(mob==null)continue;
                mob.setPos(spot.getX()+.5,spot.getY(),spot.getZ()+.5);if(!world.noCollision(mob))continue;
                mob.setCanJoinRaid(false);mob.setPatrolTarget(pos.offset(32,0,32));mob.getPersistentData().putBoolean("oxy_patrol",true);mob.getPersistentData().putLong("oxy_patrol_birth",now);
                mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(mob instanceof Pillager?Items.CROSSBOW:Items.IRON_AXE));mob.setDropChance(EquipmentSlot.MAINHAND,.03f);
                if(i==0){mob.setPatrolLeader(true);mob.getPersistentData().putBoolean("oxy_patrol_captain",true);mob.setCustomName(Component.translatable("entity.oxyrpg.patrol_captain"));}
                if(world.addFreshEntity(mob)){if(i==0)EliteMobs.promote(mob,2);spawned=true;}
            }
            if(spawned){data.putLong("oxy_raiders_next",now+12000);return;}
        }
    }
    private static boolean safe(ServerLevel world,BlockPos pos){
        if(!world.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4)||!world.getBlockState(pos).isAir()||!world.getBlockState(pos.above()).isAir())return false;
        var ground=world.getBlockState(pos.below());return ground.getFluidState().isEmpty()&&(ground.is(BlockTags.DIRT)||ground.is(BlockTags.SAND)||ground.is(BlockTags.BASE_STONE_OVERWORLD)||ground.is(net.minecraft.world.level.block.Blocks.GRAVEL)||ground.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK));
    }
    public static void cleanup(EntityTickEvent.Post event){if(!(event.getEntity() instanceof Raider mob)||!(mob.level() instanceof ServerLevel world)||mob.tickCount%200!=0||!mob.getPersistentData().getBooleanOr("oxy_patrol",false))return;
        if(world.getGameTime()-mob.getPersistentData().getLongOr("oxy_patrol_birth",world.getGameTime())>24000&&!world.hasNearbyAlivePlayer(mob.getX(),mob.getY(),mob.getZ(),96))mob.discard();}
    public static void drops(LivingDropsEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof Raider mob)||!(mob.level() instanceof ServerLevel world)||!mob.getPersistentData().getBooleanOr("oxy_patrol",false)||!world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)||RpgNecromancer.owner(event.getSource())==null)return;
        int level=MobLevels.level(mob);boolean captain=mob.getPersistentData().getBooleanOr("oxy_patrol_captain",false);
        reward(event,mob,new ItemStack(Items.EMERALD,(captain?4:1)+level/20));
        if(!captain)return;
        reward(event,mob,new ItemStack(level>=60?RpgItems.SUPERIOR_MANA_POTION.get():level>=25?RpgItems.GREATER_MANA_POTION.get():RpgItems.MANA_POTION.get(),2));
        Item prize;
        if(level>=40&&mob.getRandom().nextFloat()<.25)prize=RpgItems.EMBERBLADE.get();else if(level>=20&&mob.getRandom().nextFloat()<.3)prize=RpgItems.FROSTBLADE.get();else{
            Item[] accessories={RpgItems.FOCUS_RING.get(),RpgItems.VIGOR_RING.get(),RpgItems.WARD_AMULET.get(),RpgItems.WANDERER_AMULET.get(),RpgItems.TRAIL_BOOTS.get()};prize=accessories[mob.getRandom().nextInt(accessories.length)];}
        reward(event,mob,new ItemStack(prize));
    }
    private static void reward(LivingDropsEvent event,Raider mob,ItemStack stack){event.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY()+.4,mob.getZ(),stack));}
}
