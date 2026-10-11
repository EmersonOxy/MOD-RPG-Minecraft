package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Shared original RPG frames built from vanilla items and GUI primitives. */
public final class RpgUi {
    public static final int GOLD=0xFFE9C77C,TEXT=0xFFE8EAF0,MUTED=0xFF9EAEC0,BG=0xFF11151D;
    private RpgUi(){}
    public static void panel(GuiGraphics g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,0xF511151D);border(g,x,y,w,h,0xFF796341);border(g,x+3,y+3,w-6,h-6,0xFF333E50);
        for(int dx:new int[]{0,w-9})for(int dy:new int[]{0,h-9}){g.fill(x+dx,y+dy,x+dx+9,y+dy+2,GOLD);g.fill(x+dx,y+dy,x+dx+2,y+dy+9,GOLD);}
    }
    public static void border(GuiGraphics g,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+1,color);g.fill(x,y+h-1,x+w,y+h,color);g.fill(x,y,x+1,y+h,color);g.fill(x+w-1,y,x+w,y+h,color);}
    public static void section(GuiGraphics g,int x,int y,int w,int h,int accent){g.fill(x,y,x+w,y+h,0xFF1B2330);border(g,x,y,w,h,0xFF364254);g.fill(x,y,x+2,y+h,accent);}
    public static void medallion(GuiGraphics g,int x,int y,int radius,int color){circle(g,x,y,radius,color);circle(g,x,y,radius-2,0xFF101722);circle(g,x,y,radius-4,0xFF263344);}
    private static void circle(GuiGraphics g,int x,int y,int r,int color){for(int dy=-r;dy<=r;dy++){int dx=(int)Math.sqrt(r*r-dy*dy);g.fill(x-dx,y+dy,x+dx+1,y+dy+1,color);}}
    public static Button button(int x,int y,int w,int h,String title,String subtitle,Item item,int accent,Button.OnPress press){return new Tile(x,y,w,h,title,subtitle,item,accent,press);}
    private static final class Tile extends Button {
        private final String subtitle;private final ItemStack icon;private final int accent;
        Tile(int x,int y,int w,int h,String title,String subtitle,Item item,int accent,OnPress press){super(x,y,w,h,Component.literal(title),press,DEFAULT_NARRATION);this.subtitle=subtitle;icon=new ItemStack(item);this.accent=accent;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
            var font=Minecraft.getInstance().font;int x=getX(),y=getY(),w=getWidth(),h=getHeight();
            g.fill(x,y,x+w,y+h,!active?0xFF171C25:isHoveredOrFocused()?0xFF303D4E:0xFF202936);border(g,x,y,w,h,isHoveredOrFocused()?accent:0xFF435064);g.fill(x,y,x+2,y+h,active?accent:0xFF556071);
            g.renderFakeItem(icon,x+10,y+(h-16)/2);int tx=x+34,tw=w-42;
            int ty=y+(h>=38&&!subtitle.isEmpty()?9:(h-9)/2);g.drawString(font,font.plainSubstrByWidth(getMessage().getString(),tw),tx,ty,active?TEXT:MUTED);
            if(h>=38&&!subtitle.isEmpty())g.drawString(font,font.plainSubstrByWidth(subtitle,tw),tx,ty+13,MUTED);
        }
    }
}
