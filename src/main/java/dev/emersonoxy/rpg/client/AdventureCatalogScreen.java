package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
/** Discoverable descriptions, never grants items or changes the player's inventory. */
public final class AdventureCatalogScreen extends Screen {
    private final Screen parent;private int x,y,w,h,page,selected,rows;private List<Item> items;
    public AdventureCatalogScreen(Screen parent){super(Component.literal("Achados de exploração"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){items=List.of(RpgItems.FROSTBLADE.get(),RpgItems.EMBERBLADE.get(),RpgItems.FOCUS_RING.get(),RpgItems.VIGOR_RING.get(),RpgItems.WARD_AMULET.get(),RpgItems.WANDERER_AMULET.get(),RpgItems.TRAIL_BOOTS.get(),RpgItems.BANDAGE.get(),RpgItems.ANTIDOTE.get());w=Math.min(470,width-16);h=Math.min(290,height-8);x=(width-w)/2;y=(height-h)/2;rows=Math.max(1,(h-84)/38);int col=Math.max(100,w*2/5);
        for(int row=0;row<rows&&page*rows+row<items.size();row++){int index=page*rows+row;var item=items.get(index);addRenderableWidget(RpgUi.button(x+12,y+34+row*38,col,34,item.getName().getString(),"",item,index==selected?RpgUi.GOLD:0xFF61758A,b->{selected=index;rebuildWidgets();}));}
        addRenderableWidget(Button.builder(Component.literal("◀"),b->{page--;rebuildWidgets();}).bounds(x+12,y+h-45,28,18).build()).active=page>0;
        addRenderableWidget(Button.builder(Component.literal("▶"),b->{page++;rebuildWidgets();}).bounds(x+44,y+h-45,28,18).build()).active=(page+1)*rows<items.size();
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(x+78,y+h-45,w-90,20).build());}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){RpgUi.panel(g,x,y,w,h);g.drawCenteredString(font,title,width/2,y+12,RpgUi.GOLD);int cx=x+24+Math.max(100,w*2/5),cw=x+w-12-cx;RpgUi.section(g,cx-6,y+34,cw+6,h-87,RpgUi.GOLD);var item=items.get(selected);g.renderFakeItem(new ItemStack(item),cx+4,y+44);g.drawString(font,font.plainSubstrByWidth(item.getName().getString(),cw-28),cx+26,y+48,RpgUi.GOLD);
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();int ty=y+74;for(var line:font.split(Component.translatable("tooltip.oxyrpg."+id),cw-10)){g.drawString(font,line,cx+4,ty,RpgUi.TEXT);ty+=11;}
        String source=selected<2?"Capitães saqueadores • Nv. "+(selected==0?20:40)+"+":selected<6?"Recompensa de capitães saqueadores.":"Fabricação; botas também podem vir de capitães.";
        for(var line:font.split(Component.literal(source),cw-10)){if(ty>y+h-66)break;g.drawString(font,line,cx+4,ty+12,RpgUi.MUTED);ty+=11;}
        super.render(g,mx,my,dt);}
}
