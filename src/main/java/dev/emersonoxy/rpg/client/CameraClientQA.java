package dev.emersonoxy.rpg.client;

import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.LoggerFactory;

/** Opt-in client integration fixture; only runs in a dedicated dev directory and named QA world. */
public final class CameraClientQA {
    private static int tick,samples,collisions,failed;
    private static boolean prepared,locked,cycled,released,aimed,entered,exited,damaged;
    private CameraClientQA(){}
    private static boolean isolated(){var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();
        return Boolean.getBoolean("oxyrpg.cameraQA")&&mc.gameDirectory.toPath().toAbsolutePath().normalize().getFileName().toString().equals("run-camera-qa")&&server!=null
            &&server.getWorldData().getLevelName().equals("Oxy Camera QA");}
    private static void log(String text){LoggerFactory.getLogger(CameraClientQA.class).info("CAMERA_QA {}",text);}
    public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(!isolated()||mc.player==null||mc.level==null)return;
        if(mc.screen!=null||mc.isPaused())return;
        if(!prepared){prepared=true;ShoulderCamera.defaults();mc.options.setCameraType(CameraType.FIRST_PERSON);
            var id=mc.player.getUUID();var server=mc.getSingleplayerServer();server.execute(()->{
                var world=server.overworld();var player=server.getPlayerList().getPlayer(id);if(player==null)return;
                for(int x=-8;x<=12;x++)for(int z=-8;z<=18;z++){
                    world.setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
                    for(int y=81;y<=86;y++)world.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
                }
                player.setGameMode(GameType.SURVIVAL);player.teleportTo(.5,81,.5);player.setYRot(0);player.setXRot(0);
                player.getInventory().setItem(0,new ItemStack(Items.BOW));player.getInventory().setItem(1,new ItemStack(Items.ARROW,64));
                try{var data=dev.emersonoxy.rpg.ProfessionStore.get(player);data.xp[2]=300;dev.emersonoxy.rpg.ProfessionStore.save(player,data);dev.emersonoxy.rpg.Professions.sync(player);}catch(java.io.IOException e){throw new RuntimeException(e);}
                player.setHealth(player.getMaxHealth());
                for(var old:world.getEntitiesOfClass(Mob.class,new AABB(-8,81,-8,13,87,19)))old.discard();
                for(int i=0;i<2;i++){var mob=(Mob)(i==0?EntityType.COW:EntityType.PIG).create(world,EntitySpawnReason.COMMAND);
                    if(mob!=null){mob.setPos(.5+i*3,81,7+i);mob.setNoAi(true);world.addFreshEntity(mob);}}
            });log("START isolated world fixture");return;
        }
        tick++;
        switch(tick){
            case 20 -> {mc.player.setYRot(0);mc.player.setXRot(0);mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);log("ENTER shoulder");}
            case 55 -> {entered=mc.gameRenderer.getMainCamera().isDetached();ShoulderCamera.adjust(0,.1);ShoulderCamera.adjust(1,.1);ShoulderCamera.adjust(2,.1);log("ADJUST distance/lateral/height");}
            case 75 -> {mc.options.keyUp.setDown(true);log("WALK start");}
            case 95 -> {mc.options.keyUp.setDown(false);log("WALK stop");}
            case 105 -> {mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);log("AIM start");}
            case 120 -> {aimed=mc.player.isUsingItem();mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);log("AIM end active="+aimed);}
            case 130 -> {TargetLock.toggle();locked=TargetLock.target()!=null;log("LOCK acquired="+locked);}
            case 155 -> {var old=TargetLock.target();TargetLock.cycle();cycled=TargetLock.target()!=null&&TargetLock.target()!=old;log("LOCK cycled="+cycled);}
            case 175 -> wall(true,true);
            case 195 -> {released=TargetLock.target()==null;log("LOCK occlusion released="+released);wall(true,false);}
            case 205 -> {var server=mc.getSingleplayerServer();var id=mc.player.getUUID();server.execute(()->{var p=server.getPlayerList().getPlayer(id);if(p!=null){p.teleportTo(.5,81,.5);p.hurtServer(server.overworld(),p.damageSources().generic(),2);}});log("DAMAGE and camera wall approach");wall(false,true);}
            case 250 -> {wall(false,false);log("COLLISION recovery");}
            case 230 -> {damaged=mc.player.getHealth()<mc.player.getMaxHealth();log("DAMAGE received="+damaged);}
            case 285 -> {mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);log("FRONT transition");}
            case 315 -> {mc.options.setCameraType(CameraType.FIRST_PERSON);log("EXIT first-person transition");}
            case 355 -> {exited=!mc.gameRenderer.getMainCamera().isDetached();log("EXIT settled="+exited);}
            case 365 -> {mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);log("REENTER shoulder");}
            case 400 -> {log("RESULT samples="+samples+" collisionFrames="+collisions+" failures="+failed+" entered="+entered+" exited="+exited+" locked="+locked+" cycled="+cycled+" released="+released+" aimed="+aimed+" damaged="+damaged);
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("QA câmera concluído; veja CAMERA_QA no log."),false);}
        }
        if(tick>25&&tick<400){
            var camera=mc.gameRenderer.getMainCamera();var position=camera.getPosition();samples++;
            if(!Double.isFinite(position.x)||!Double.isFinite(position.y)||!Double.isFinite(position.z))failed++;
            if(tick>215&&tick<245){
                if(position.distanceTo(mc.player.getEyePosition())<2)collisions++;
                if(mc.level.getBlockCollisions(null,new AABB(position.x-.1,position.y-.1,position.z-.1,position.x+.1,position.y+.1,position.z+.1)).iterator().hasNext())failed++;
            }
        }
    }
    private static void wall(boolean front,boolean present){var server=Minecraft.getInstance().getSingleplayerServer();server.execute(()->{
        var world=server.overworld();int z=front?5:-1;
        for(int x=-4;x<=8;x++)for(int y=81;y<=85;y++)world.setBlock(new BlockPos(x,y,z),(present?Blocks.STONE:Blocks.AIR).defaultBlockState(),3);
        log("WALL "+(front?"occlusion":"camera")+" present="+present);
    });}
}
