package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Shared original RPG frames built from vanilla items and GUI primitives. */
public final class RpgUi {
    public static final int GOLD=0xFFE9C77C,TEXT=0xFFE8EAF0,MUTED=0xFF9EAEC0,BG=0xFF11151D;
    private static final java.util.Map<net.minecraft.client.gui.components.AbstractWidget,Hover> HOVERS=new java.util.WeakHashMap<>();
    private static final class Hover {long frame=System.nanoTime();float amount;}
    public interface ConfigTheme {boolean oxyOwned();}
    public static boolean owns(net.minecraft.client.gui.screens.Screen screen){return screen!=null&&(screen instanceof net.minecraft.client.gui.screens.inventory.MerchantScreen||screen.getClass().getName().startsWith("dev.emersonoxy.rpg.client.")||screen instanceof ConfigTheme theme&&theme.oxyOwned());}
    private RpgUi(){}
    /** Rendering only; original click, keyboard, tooltip and narration behavior stays native. */
    public static void action(GuiGraphics g,net.minecraft.client.gui.components.AbstractWidget button){
        int x=button.getX(),y=button.getY(),w=button.getWidth(),h=button.getHeight();var font=Minecraft.getInstance().font;
        var hover=HOVERS.computeIfAbsent(button,b->new Hover());long now=System.nanoTime();float dt=Math.min(.1f,(now-hover.frame)/1000000000f);hover.frame=now;float target=button.active&&button.isHoveredOrFocused()?1:0;hover.amount+=(target-hover.amount)*(1-(float)Math.exp(-18*dt));
        int shade=(int)(hover.amount*15);g.fill(x,y,x+w,y+h,button.active?0xFF202936+(shade<<16)+(shade<<8)+shade:0xFF171C25);
        border(g,x,y,w,h,button.active&&button.isHoveredOrFocused()?GOLD:0xFF435064);g.fill(x,y,x+2,y+h,button.active?GOLD:0xFF556071);
        String label=font.plainSubstrByWidth(button.getMessage().getString(),Math.max(1,w-14));g.drawCenteredString(font,label,x+w/2,y+(h-9)/2,button.active?TEXT:MUTED);
    }
    public static void slider(GuiGraphics g,net.minecraft.client.gui.components.AbstractWidget button,double value){
        int x=button.getX(),y=button.getY(),w=button.getWidth(),h=button.getHeight();g.fill(x,y,x+w,y+h,0xFF1B2330);border(g,x,y,w,h,button.isHoveredOrFocused()?GOLD:0xFF435064);
        int thumb=x+4+(int)(Math.clamp(value,0,1)*(w-8));g.fill(x+4,y+h-5,x+w-4,y+h-3,0xFF435064);g.fill(x+4,y+h-5,thumb,y+h-3,0xFF927649);g.fill(thumb-2,y+h-7,thumb+2,y+h-1,GOLD);
        var font=Minecraft.getInstance().font;g.drawCenteredString(font,font.plainSubstrByWidth(button.getMessage().getString(),w-12),x+w/2,y+3,button.active?TEXT:MUTED);
    }
    public static void panel(GuiGraphics g,int x,int y,int w,int h){
        OutsideClick.panel(x,y,w,h);
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
