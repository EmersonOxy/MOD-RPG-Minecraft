package dev.emersonoxy.rpg.client;

import java.util.*;
import dev.emersonoxy.rpg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

public final class AbilityPreview {
    private static int slot=-1,selectedClass;
    private static final List<Vec3[]> LINES=new ArrayList<>();
    private static long built=-1;
    private AbilityPreview(){}
    public static void clear(){slot=-1;LINES.clear();built=-1;}
    public static void press(int index){var mc=Minecraft.getInstance();CombatOptions.load();if(!CombatOptions.preview||EquipmentAccess.clientSelected()==RpgClass.AVIARIO){OxyRpgClient.cast(index);return;}slot=index;selectedClass=EquipmentAccess.clientSelected().ordinal();built=-1;}
    public static void tick(){
        var mc=Minecraft.getInstance();if(slot<0)return;
        if(mc.player==null||mc.level==null||mc.screen!=null||!mc.player.isAlive()||mc.player.isSpectator()||selectedClass!=EquipmentAccess.clientSelected().ordinal()){clear();return;}
        KeyMapping key=slot==0?OxyRpgClient.USE_ABILITY:slot==1?OxyRpgClient.SECOND_ABILITY:OxyRpgClient.THIRD_ABILITY;
        if(!key.isDown()){int release=slot;clear();OxyRpgClient.cast(release);}
    }
    private static void edge(Vec3 a,Vec3 b){LINES.add(new Vec3[]{WorldLines.ground(a),WorldLines.ground(b)});}
    private static void circle(Vec3 center,double radius){for(int i=0;i<64;i++){double a=i*Math.PI/32,b=(i+1)*Math.PI/32;edge(center.add(Math.cos(a)*radius,0,Math.sin(a)*radius),center.add(Math.cos(b)*radius,0,Math.sin(b)*radius));}}
    private static void path(Vec3 start,Vec3 end){double distance=start.distanceTo(end);int steps=Math.max(1,(int)Math.ceil(distance*2));for(int i=0;i<steps;i++)edge(start.lerp(end,i/(double)steps),start.lerp(end,(i+1)/(double)steps));}
    private static void build(RpgProfile p){
        var mc=Minecraft.getInstance();var player=mc.player;LINES.clear();var center=player.position();var point=ShoulderCamera.aimPoint(mc.gameRenderer.getMainCamera().getPartialTickTime());
        var target=TargetLock.target();if(target!=null&&TargetLock.mouseLocked())point=TargetLock.point(target,mc.gameRenderer.getMainCamera().getPartialTickTime());
        var direction=point.subtract(player.getEyePosition()).normalize();
        if(p.rpgClass()==RpgClass.MAGO&&slot==2){circle(center,p.mainUnlocked(2)?4+(p.hasSkill(18)?1:0):1.5+(p.hasSkill(16)?.5:0));return;}
        if(p.rpgClass()==RpgClass.GUERREIRO){circle(center,slot==0?3+(p.hasSkill(4)?.5:0)+Math.max(0,ClassEquipment.active(player,RpgClass.GUERREIRO)-1)*.5:1);return;}
        if(p.rpgClass()==RpgClass.MAGO&&slot==1&&p.mainUnlocked(1)){
            double range=6+(p.hasSkill(11)?1:0)+(p.hasSkill(13)?1:0);var forward=direction.multiply(1,0,1).normalize();var side=new Vec3(-forward.z,0,forward.x);double width=.4+range*(p.hasSkill(13)?.18:.1);
            var end=mc.level.clip(new ClipContext(player.getEyePosition(),player.getEyePosition().add(direction.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getLocation();range=Math.min(range,player.getEyePosition().distanceTo(end));
            var left=center.add(forward.scale(range)).add(side.scale(width));var right=center.add(forward.scale(range)).subtract(side.scale(width));path(center,left);path(center,right);path(left,right);return;
        }
        if(p.rpgClass()==RpgClass.ARQUEIRO&&slot==2){
            var forward=direction.multiply(1,0,1).normalize();int side=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0),f=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);if(side!=0||f!=0)forward=forward.yRot((float)-Math.atan2(side,f));double range=p.mainUnlocked(2)?5+(p.hasSkill(18)?1:0):2;path(center,center.add(forward.scale(range)));circle(center.add(forward.scale(range)),.35);return;
        }
        if(slot>0&&p.rpgClass()!=RpgClass.MAGO){circle(center,p.rpgClass()==RpgClass.NECROMANTE?slot==2?6:3:p.rpgClass()==RpgClass.XAMA?slot==1?4:1:2);return;}
        var end=mc.level.clip(new ClipContext(player.getEyePosition(),player.getEyePosition().add(direction.scale(24)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getLocation();
        path(center,end);if(p.rpgClass()==RpgClass.MAGO&&slot==0)circle(end,p.mainUnlocked(0)?3+(p.hasSkill(4)?.5:0)+(p.hasSkill(6)?1:0):.6);else circle(end,.35);
    }
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(slot<0||mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui)return;var profile=OxyRpgClient.profile();if(profile==null)return;
        if(built!=mc.level.getGameTime()){build(profile);built=mc.level.getGameTime();}
        int cost=ClassEquipment.discount(mc.player,profile.rpgClass(),RpgResources.cost(profile,slot,false));var resource=OxyRpgClient.resources();var state=OxyRpgClient.state();
        String status=!state.canUse(slot)?"Habilidade bloqueada":!state.debug()&&OxyRpgClient.cooldown(slot)>0?"Em recarga":!state.debug()&&resource!=null&&resource.mana()<cost?"Mana insuficiente":"Solte para lançar";
        int color=status.equals("Solte para lançar")?0xD953D5BF:status.equals("Em recarga")?0xD9A1AAB8:0xD9FF7070;
        for(var line:LINES)WorldLines.line(g,line[0],line[1],color);
        g.drawCenteredString(mc.font,SkillTree.abilityName(profile.rpgClass(),slot)+" • "+status+" • "+(state.debug()?0:cost)+" MP",g.guiWidth()/2,g.guiHeight()/2+30,0xFF000000|(color&0xFFFFFF));
    }
}
