package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
public final class ArcherAimHud {
    private ArcherAimHud(){}
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();var p=OxyRpgClient.profile();
        if(mc.player==null||p==null||p.rpgClass()!=RpgClass.ARQUEIRO_NOVO)return;
        var status=OxyRpgClient.effectRemaining(2);if(status<=0||!p.mainUnlocked(2))return;
        var target=TargetLock.target();if(target==null&&mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit&&hit.getEntity() instanceof net.minecraft.world.entity.Mob m)target=m;
        String text=target!=null?"Pontaria guiada · "+target.getName().getString():"Pontaria ativa · mire numa criatura ou trave o alvo";
        g.drawCenteredString(mc.font,text,g.guiWidth()/2,g.guiHeight()/2+26,target!=null?0xFF8FE8C0:0xFFC9D4E3);
    }
}
