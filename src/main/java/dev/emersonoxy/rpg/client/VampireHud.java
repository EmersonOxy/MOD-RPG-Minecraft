package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
/** The vanilla food value remains the server resource; only its presentation changes. */
public final class VampireHud {
    private VampireHud(){}
    public static void receive(RpgNetwork.VampireState state){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;var tag=mc.player.getPersistentData();tag.putBoolean("oxy_vampire_bat",state.bat());tag.putLong("oxy_vampire_twilight",mc.level.getGameTime()+state.twilight());}
    public static void render(GuiGraphics g){var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||DynamicHud.alpha(DynamicHud.Part.FOOD)<=0||!mc.gameMode.canHurtPlayer())return;HudOpacity.begin(DynamicHud.alpha(DynamicHud.Part.FOOD));int right=g.guiWidth()/2+91,y=g.guiHeight()-mc.gui.rightHeight,food=mc.player.getFoodData().getFoodLevel();
        for(int i=0;i<10;i++){int x=right-i*8-8,fill=Math.clamp(food-i*2,0,2),c=fill==2?0xFF358BD7:fill==1?0xFF275D96:0xFF182A40;g.fill(x+3,y,x+5,y+2,c);g.fill(x+2,y+2,x+6,y+4,c);g.fill(x+1,y+4,x+7,y+7,c);g.fill(x+2,y+7,x+6,y+8,c);if(fill>0)g.fill(x+2,y+4,x+3,y+6,0xFF91C9F4);}mc.gui.rightHeight+=10;HudOpacity.end();}
}
