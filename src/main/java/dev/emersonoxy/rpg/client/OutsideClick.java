package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Close through the screen's native lifecycle so held stacks and NPC sessions are returned. */
public final class OutsideClick {
    private static Screen screen;
    private static int left,top,right,bottom;
    private static boolean drawn;
    private OutsideClick() {}
    public static void frame(ScreenEvent.Render.Pre event){screen=event.getScreen();drawn=false;}
    public static void panel(int x,int y,int w,int h){
        if(screen!=Minecraft.getInstance().screen||!RpgUi.owns(screen)||w<=0||h<=0)return;
        if(!drawn){left=x;top=y;right=x+w;bottom=y+h;drawn=true;}
        else{left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+w);bottom=Math.max(bottom,y+h);}
    }
    public static void click(ScreenEvent.MouseButtonPressed.Pre event){
        var current=event.getScreen();var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||!(current instanceof AbstractContainerScreen<?>)&&!current.getClass().getName().startsWith("dev.emersonoxy.rpg.client."))return;
        int x1,y1,x2,y2;
        if(drawn&&screen==current){x1=left;y1=top;x2=right;y2=bottom;}
        else if(current instanceof AbstractContainerScreen<?> container){
            x1=container.getGuiLeft();y1=container.getGuiTop();x2=x1+container.getXSize();y2=y1+container.getYSize();
            if(current instanceof CreativeModeInventoryScreen){y1-=32;y2+=32;x1-=12;x2+=12;}
        }else return;
        double x=event.getMouseX(),y=event.getMouseY();
        if(x>=x1&&x<x2&&y>=y1&&y<y2)return;
        for(var child:current.children()){
            if(child.isMouseOver(x,y))return;
            if(child instanceof net.minecraft.client.gui.screens.recipebook.RecipeBookComponent<?> book&&book.isVisible()){
                int bx=(current.width-147)/2-(current.width<379?0:86),by=(current.height-166)/2;
                if(x>=bx-30&&x<bx+147&&y>=by&&y<by+166)return;
            }
        }
        event.setCanceled(true);current.onClose();
    }
}
