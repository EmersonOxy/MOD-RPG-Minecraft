package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
public final class LiveAim {
    private LiveAim(){}
    public static void send(){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||!mc.player.isAlive()||mc.player.isSpectator()||mc.options.getCameraType().isMirrored())return;var point=ShoulderCamera.abilityPoint(mc.gameRenderer.getMainCamera().getPartialTickTime());mc.player.connection.send(new RpgNetwork.Request(18,0,-1,point.x,point.y,point.z));}
    public static void tick(){var mc=Minecraft.getInstance();if(mc.player!=null&&mc.player.tickCount%2==0&&(mc.options.keyUse.isDown()||mc.player.isUsingItem()||OxyRpgClient.liveChannel()))send();}
}
