package dev.emersonoxy.rpg.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
public final class MerchantTheme {
    private MerchantTheme(){}
    public static void background(GuiGraphics g,MerchantScreen screen,int selected){
        int side=VillagerClient.sideWidth(screen.width);if(side>0&&VillagerClient.data()!=null)VillagerPanel.render(g,screen.getGuiLeft()+282,Math.max(4,screen.getGuiTop()-30),side);
        int x=screen.getGuiLeft(),y=screen.getGuiTop();RpgUi.panel(g,x-6,y-30,288,202);
        RpgUi.section(g,x+3,y+3,98,157,RpgUi.GOLD);RpgUi.section(g,x+103,y+3,169,157,0xFF84BCA4);
        for(var slot:screen.getMenu().slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xFF101720);RpgUi.border(g,x+slot.x-1,y+slot.y-1,18,18,0xFF526176);}
        var font=Minecraft.getInstance().font;g.drawString(font,"→",x+194,y+40,RpgUi.GOLD);
        var offers=screen.getMenu().getOffers();if(selected>=0&&selected<offers.size()&&offers.get(selected).isOutOfStock())g.drawString(font,"×",x+195,y+40,0xFFFF7777);
    }
}
