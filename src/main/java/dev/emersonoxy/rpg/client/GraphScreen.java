package dev.emersonoxy.rpg.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Advancement-style canvas with pointer-anchored zoom and drag-to-pan. */
public abstract class GraphScreen extends Screen {
    protected record Node(int id,double x,double y,int[] parents,String title,String description,String icon,boolean major){}
    protected final Screen parent;
    protected List<Node> nodes=List.of();
    protected int selected,left,top,panelWidth,panelHeight,ticks,waiting;
    private double zoom=.85,panX,panY,downX,downY;
    private boolean dragging,moved;
    private Button learn;
    private final Map<String,ItemStack> icons=new HashMap<>();
    protected GraphScreen(Screen parent,String title){super(Component.literal(title));this.parent=parent;}
    protected abstract void loadNodes();
    protected abstract boolean owned(int id);
    protected abstract boolean available(int id);
    protected abstract String budget();
    protected abstract String status(int id);
    protected abstract void purchase(int id);
    protected String purchaseLabel(){return "Aprender • 1 ponto";}
    protected void tabs(){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        left=8;top=4;panelWidth=width-16;panelHeight=height-8;loadNodes();tabs();
        int w=(panelWidth-32)/3;
        learn=addRenderableWidget(Button.builder(Component.literal("Aprender • 1 ponto"),b->{if(waiting==0&&available(selected)){waiting=100;purchase(selected);}}).bounds(left+10,top+panelHeight-27,w,20).build());
        addRenderableWidget(Button.builder(Component.literal("Centralizar"),b->center()).bounds(left+16+w,top+panelHeight-27,w,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+22+2*w,top+panelHeight-27,w,20).build());
        center();if(minecraft.player!=null)minecraft.player.connection.send(new dev.emersonoxy.rpg.RpgNetwork.Request(0));
    }
    protected void center(){zoom=.85;var node=nodes.get(selected);panX=left+panelWidth/2.-node.x*zoom;panY=(top+53+bottom())/2.-node.y*zoom;}
    private int bottom(){return top+panelHeight-98;}
    private boolean canvas(double x,double y){return x>left+6&&x<left+panelWidth-6&&y>top+53&&y<bottom();}
    private int hit(double x,double y){double wx=(x-panX)/zoom,wy=(y-panY)/zoom;for(var n:nodes)if(Math.abs(n.x-wx)<=14&&Math.abs(n.y-wy)<=14)return n.id;return -1;}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){if(e.button()==0&&canvas(e.x(),e.y())){dragging=true;moved=false;downX=e.x();downY=e.y();return true;}return super.mouseClicked(e,twice);}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging){if(Math.hypot(e.x()-downX,e.y()-downY)>3)moved=true;if(moved){panX+=dx;panY+=dy;}return true;}return super.mouseDragged(e,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){if(dragging&&e.button()==0){dragging=false;if(!moved){int id=hit(e.x(),e.y());if(id>=0)selected=id;}return true;}return super.mouseReleased(e);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(canvas(x,y)){double next=Math.clamp(zoom*Math.pow(1.15,vertical),.3,2);panX=x-(x-panX)*next/zoom;panY=y-(y-panY)*next/zoom;zoom=next;return true;}return super.mouseScrolled(x,y,horizontal,vertical);}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){onClose();return;}if(waiting>0)waiting--;if(++ticks%40==0)minecraft.player.connection.send(new dev.emersonoxy.rpg.RpgNetwork.Request(0));}
    private static void line(GuiGraphics g,double ax,double ay,double bx,double by,int color){float dx=(float)(bx-ax),dy=(float)(by-ay);g.pose().pushMatrix();g.pose().translate((float)ax,(float)ay);g.pose().rotate((float)Math.atan2(dy,dx));g.fill(0,-1,(int)Math.hypot(dx,dy),1,color);g.pose().popMatrix();}
    protected void drawNodeIcon(GuiGraphics g,Node n){var icon=icons.computeIfAbsent(n.icon,k->new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(k))));g.renderFakeItem(icon,(int)n.x-8,(int)n.y-8);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        RpgUi.panel(g,left,top,panelWidth,panelHeight);
        g.fill(left+5,top+5,left+panelWidth-5,top+panelHeight-5,0xFF151D28);
        g.drawString(font,title,left+12,top+10,0xFFFFD580);g.drawString(font,font.plainSubstrByWidth(budget(),Math.max(30,panelWidth-font.width(title)-36)),left+panelWidth-12-Math.min(font.width(budget()),Math.max(30,panelWidth-font.width(title)-36)),top+10,0xFFD5E0EB);
        g.enableScissor(left+6,top+53,left+panelWidth-6,bottom());g.fill(left+6,top+53,left+panelWidth-6,bottom(),0xFF20252C);
        g.pose().pushMatrix();g.pose().translate((float)panX,(float)panY);g.pose().scale((float)zoom,(float)zoom);
        for(var n:nodes)for(int id:n.parents){var p=nodes.get(id);line(g,p.x,p.y,n.x,n.y,owned(n.id)?0xFFFFC66B:available(n.id)?0xFF73A89A:0xFF505966);}
        for(var n:nodes){
            double sx=panX+n.x*zoom,sy=panY+n.y*zoom;if(sx<left-30||sx>left+panelWidth+30||sy<top+23||sy>bottom()+30)continue;
            int color=owned(n.id)?n.major?RpgUi.GOLD:0xFF7AB7A6:available(n.id)?0xFF93BFEA:0xFF576477;
            if(n.id==selected)RpgUi.medallion(g,(int)n.x,(int)n.y,n.major?22:19,RpgUi.GOLD);
            RpgUi.medallion(g,(int)n.x,(int)n.y,n.major?18:14,color);
            drawNodeIcon(g,n);
        }
        g.pose().popMatrix();g.disableScissor();
        var n=nodes.get(selected);g.drawString(font,font.plainSubstrByWidth(n.title,panelWidth-20),left+10,bottom()+6,0xFFFFD580);
        var description=font.split(Component.literal(n.description),panelWidth-20);for(int i=0;i<Math.min(3,description.size());i++)g.drawString(font,description.get(i),left+10,bottom()+18+i*10,0xFFE2E8EC);
        g.drawString(font,font.plainSubstrByWidth(waiting>0?"Aguardando servidor…":status(selected),panelWidth-20),left+10,top+panelHeight-42,0xFFAACCCC);
        learn.setMessage(Component.literal(purchaseLabel()));learn.active=waiting==0&&available(selected);super.render(g,mx,my,partial);
        int hovered=canvas(mx,my)&&!dragging?hit(mx,my):-1;if(hovered>=0){var h=nodes.get(hovered);var lines=new java.util.ArrayList<net.minecraft.util.FormattedCharSequence>();int wrap=Math.min(260,width-32);
            lines.addAll(font.split(Component.literal(h.title).withStyle(net.minecraft.ChatFormatting.GOLD,net.minecraft.ChatFormatting.BOLD),wrap));
            lines.add(net.minecraft.util.FormattedCharSequence.EMPTY);lines.addAll(font.split(Component.literal(h.description),wrap));
            lines.add(net.minecraft.util.FormattedCharSequence.EMPTY);lines.addAll(font.split(Component.literal(status(hovered)).withStyle(net.minecraft.ChatFormatting.AQUA),wrap));
            g.setTooltipForNextFrame(font,lines,mx,my);}
    }
}
