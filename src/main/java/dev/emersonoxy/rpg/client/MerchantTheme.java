package dev.emersonoxy.rpg.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
public final class MerchantTheme {
    private MerchantTheme(){}
    public static void background(GuiGraphics g,MerchantScreen screen,int selected){
        int side=VillagerClient.data()==null?0:VillagerClient.sideWidth(screen.width);
        int x=screen.getGuiLeft(),y=screen.getGuiTop();RpgUi.panel(g,x-6,y-30,side>0?288+side:288,VillagerPanel.height());
        var font=Minecraft.getInstance().font;g.drawString(font,"COMÉRCIO",x+8,y-17,RpgUi.GOLD);
        if(side>0){g.fill(x+280,y-18,x+281,y-42+VillagerPanel.height(),0xFF364254);VillagerPanel.render(g,x+282,Math.max(4,y-30),side);}
        RpgUi.section(g,x+3,y+3,98,157,RpgUi.GOLD);RpgUi.section(g,x+103,y+3,169,157,0xFF84BCA4);
        for(var slot:screen.getMenu().slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xFF101720);RpgUi.border(g,x+slot.x-1,y+slot.y-1,18,18,0xFF526176);}
        g.drawString(font,"→",x+194,y+40,RpgUi.GOLD);
        var offers=screen.getMenu().getOffers();if(selected>=0&&selected<offers.size()&&offers.get(selected).isOutOfStock())g.drawString(font,"×",x+195,y+40,0xFFFF7777);
    }
}
