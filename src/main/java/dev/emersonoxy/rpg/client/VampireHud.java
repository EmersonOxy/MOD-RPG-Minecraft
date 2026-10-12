package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
/** The vanilla food value remains the server resource; only its presentation changes. */
public final class VampireHud {
    private VampireHud(){}
    public static void receive(RpgNetwork.VampireState state){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;var tag=mc.player.getPersistentData();boolean changed=dev.emersonoxy.rpg.RpgVampire.bat(mc.player)!=state.bat();tag.putBoolean("oxy_vampire_bat",state.bat());if(changed)mc.player.refreshDimensions();tag.putInt("oxy_vampire_meals",state.meals());tag.putLong("oxy_vampire_twilight",mc.level.getGameTime()+state.twilight());}
    public static void prey(GuiGraphics g){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||mc.options.hideGui||DynamicHud.mode()==DynamicHud.Mode.HIDDEN||!mc.player.getPersistentData().getBooleanOr("oxy_vampire_class",false)||dev.emersonoxy.rpg.RpgVampire.bat(mc.player)||mc.player.getFoodData().getFoodLevel()>=20||mc.player.isShiftKeyDown()||!(mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit)||!dev.emersonoxy.rpg.RpgVampire.prey(hit.getEntity())||mc.player.distanceToSqr(hit.getEntity())>9)return;
        var projected=CameraProjection.project(hit.getEntity().getEyePosition().add(0,.35,0));if(!WorldLines.onScreen(projected))return;
        int x=(int)((projected.x+1)*g.guiWidth()/2),y=(int)((1-projected.y)*g.guiHeight()/2);
        SkillGlyphs.fangs(g,x-8,y-8,16,0xFFFF526B);
        if(mc.player.getPersistentData().getIntOr("oxy_vampire_meals",0)>=3)return;
        String label=mc.options.keyUse.getTranslatedKeyMessage().getString()+" · Beber sangue";
        g.pose().pushMatrix();g.pose().translate(x,y+11);g.pose().scale(.8f,.8f);g.drawString(mc.font,label,-mc.font.width(label)/2,0,0xFFFFA3AB);g.pose().popMatrix();
    }
    public static void render(GuiGraphics g){var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||DynamicHud.alpha(DynamicHud.Part.FOOD)<=0||!mc.gameMode.canHurtPlayer())return;HudOpacity.begin(DynamicHud.alpha(DynamicHud.Part.FOOD));int right=g.guiWidth()/2+91,y=g.guiHeight()-mc.gui.rightHeight,food=mc.player.getFoodData().getFoodLevel();
        for(int i=0;i<10;i++){int x=right-i*8-8,fill=Math.clamp(food-i*2,0,2),c=fill==2?0xFFCF3047:fill==1?0xFF862337:0xFF341923;g.fill(x+3,y,x+5,y+2,c);g.fill(x+2,y+2,x+6,y+4,c);g.fill(x+1,y+4,x+7,y+7,c);g.fill(x+2,y+7,x+6,y+8,c);if(fill>0)g.fill(x+2,y+4,x+3,y+6,0xFFFF8993);}mc.gui.rightHeight+=10;HudOpacity.end();}
}
