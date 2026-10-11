package dev.emersonoxy.rpg.client;

import java.util.*;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public final class DamageNumbers {
    private static final class Number {
        int entity;Vec3 point;float damage;boolean critical,blocked;double born,last;double side;String text;int textColor;
        Number(RpgNetwork.DamageNumber n,double time){entity=n.entity();point=new Vec3(n.x(),n.y(),n.z());damage=n.damage();critical=n.critical();blocked=n.blocked();born=last=time;side=(SEQUENCE++%2==0?1:-1)*(.55+(SEQUENCE%3)*.10);}
    }
    private static final List<Number> NUMBERS=new ArrayList<>();
    private static int SEQUENCE;
    private static double hitUntil,soundAt;private static boolean lastCritical,lastBlocked;
    private DamageNumbers(){}
    public static void clear(){NUMBERS.clear();hitUntil=soundAt=0;}
    public static void receive(RpgNetwork.DamageNumber value){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;CombatOptions.load();double now=mc.level.getGameTime();
        hitUntil=now+5;lastCritical=value.critical();lastBlocked=value.blocked();
        boolean merged=false;
        if(CombatOptions.group&&!value.blocked())for(int i=NUMBERS.size()-1;i>=0;i--){var n=NUMBERS.get(i);if(n.text==null&&n.entity==value.entity()&&n.critical==value.critical()&&!n.blocked&&now-n.born<=4){n.damage+=value.damage();n.last=now;merged=true;break;}}
        if(!merged){if(NUMBERS.size()>=96)NUMBERS.removeFirst();NUMBERS.add(new Number(value,now));}
        if(CombatOptions.sound&&now>=soundAt){mc.player.playSound(value.blocked()?SoundEvents.SHIELD_BLOCK.value():SoundEvents.EXPERIENCE_ORB_PICKUP,.12f,value.critical()?1.35f:1.8f);soundAt=now+3;}
    }
    public static void receiveText(RpgNetwork.CombatText value){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        var n=new Number(new RpgNetwork.DamageNumber(value.entity(),value.x(),value.y(),value.z(),0,false,false),mc.level.getGameTime());n.text=value.text();n.textColor=value.color();
        if(NUMBERS.size()>=96)NUMBERS.removeFirst();NUMBERS.add(n);
    }
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double now=mc.level.getGameTime()+mc.gameRenderer.getMainCamera().getPartialTickTime();NUMBERS.removeIf(n->now-n.last>32);CombatOptions.load();
        if(mc.options.hideGui||mc.screen!=null||DynamicHud.mode()==DynamicHud.Mode.HIDDEN)return;
        if(CombatOptions.numbers)for(var n:NUMBERS){
            double age=Math.max(0,now-n.born);var camera=mc.gameRenderer.getMainCamera();var right=new org.joml.Vector3f(1,0,0).rotate(camera.rotation());
            var point=n.point.add(right.x*n.side,.25+right.y*n.side,right.z*n.side);
            if(!WorldLines.visible(point))continue;var p=CameraProjection.project(point);if(!WorldLines.onScreen(p))continue;
            float fade=(float)Math.clamp((32-(now-n.last))/10,0,1);
            float alpha=fade*fade*(3-2*fade)*(float)Math.clamp(age/1.5,0,1);if(alpha<=.03)continue;
            int color=((int)(alpha*255)<<24)|(n.text!=null?n.textColor:n.blocked?0x75BFFF:n.critical?0xFFD36B:0xFFFFFF);
            String text=n.text!=null?n.text:n.blocked?"Bloqueado":String.format(Locale.ROOT,n.damage==Math.rint(n.damage)?"%.0f":"%.1f",n.damage)+(n.critical?" Crítico":"");
            double pop=age<3?.55+.8*(1-Math.pow(1-age/3,2)):age<8?1+.35*Math.pow(1-(age-3)/5,2):1;
            float scale=(float)(CombatOptions.size*(n.critical?1.75:1.45)*pop);
            double hop=18*(1-Math.pow(1-Math.min(age,10)/10,2))+age*.4;
            g.pose().pushMatrix();g.pose().translate((float)((p.x+1)*g.guiWidth()/2),(float)((1-p.y)*g.guiHeight()/2-hop));g.pose().scale(scale,scale);var label=net.minecraft.network.chat.Component.literal(text).withStyle(style->style.withBold(true).withItalic(n.critical));
            int lx=-mc.font.width(label)/2;int outline=((int)(alpha*255)<<24)|0x10101B;
            g.drawString(mc.font,label,lx-1,-1,outline,false);g.drawString(mc.font,label,lx+1,-1,outline,false);g.drawString(mc.font,label,lx-1,1,outline,false);g.drawString(mc.font,label,lx+1,1,outline,false);
            g.drawString(mc.font,label,lx-1,0,outline,false);g.drawString(mc.font,label,lx+1,0,outline,false);g.drawString(mc.font,label,lx,-1,outline,false);g.drawString(mc.font,label,lx,1,outline,false);g.drawString(mc.font,label,lx,0,color,false);g.pose().popMatrix();
        }
        if(CombatOptions.marker&&now<hitUntil){
            var point=ShoulderCamera.aimPoint(mc.gameRenderer.getMainCamera().getPartialTickTime());var p=CameraProjection.project(point);if(!WorldLines.onScreen(p))return;
            int x=ShoulderCamera.active()?(int)((p.x+1)*g.guiWidth()/2):g.guiWidth()/2,y=ShoulderCamera.active()?(int)((1-p.y)*g.guiHeight()/2):g.guiHeight()/2;
            int color=lastBlocked?0xFF75BFFF:lastCritical?0xFFFFD36B:0xFFFFFFFF;
            for(int i=4;i<=7;i++){g.fill(x-i,y-i,x-i+1,y-i+1,color);g.fill(x+i,y-i,x+i+1,y-i+1,color);g.fill(x-i,y+i,x-i+1,y+i+1,color);g.fill(x+i,y+i,x+i+1,y+i+1,color);}
        }
    }
}
