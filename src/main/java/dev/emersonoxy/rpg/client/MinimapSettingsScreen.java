package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MinimapSettingsScreen extends Screen {
    private final Screen parent;private int left,top,w,h,cx,cw;
    public MinimapSettingsScreen(Screen parent){super(Component.literal("Minimapa"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){MinimapOptions.save();minecraft.setScreen(parent);}
    @Override public void removed(){MinimapOptions.save();}
    @Override protected void init(){
        MinimapOptions.load();w=Math.min(470,width-16);h=Math.min(274,height-12);left=(width-w)/2;top=(height-h)/2;cx=left+Math.min(186,w/2);cw=w-(cx-left)-12;
        toggle(28,"Visível",DynamicHud.enabled(DynamicHud.Part.MINIMAP),()->DynamicHud.toggle(DynamicHud.Part.MINIMAP));
        addRenderableWidget(Button.builder(Component.literal(MinimapOptions.cornerLabel()),b->{MinimapOptions.corner=MinimapOptions.manual?0:(MinimapOptions.corner+1)%4;MinimapOptions.manual=false;rebuildWidgets();}).bounds(cx,top+row(48),cw,18).build());
        addRenderableWidget(new RpgSlider(cx,top+row(68),cw,18,Component.empty(),(MinimapOptions.size-72)/88.0){
            {updateMessage();}protected void updateMessage(){setMessage(Component.literal("Tamanho: "+MinimapOptions.size+" px"));}protected void applyValue(){MinimapOptions.size=72+(int)Math.round(value*88);}});
        addRenderableWidget(new RpgSlider(cx,top+row(88),cw,18,Component.empty(),MinimapOptions.zoom/2.0){
            {updateMessage();}protected void updateMessage(){setMessage(Component.literal("Zoom: "+new String[]{"longe","médio","perto"}[MinimapOptions.zoom]));}protected void applyValue(){MinimapOptions.zoom=(int)Math.round(value*2);}});
        addRenderableWidget(new RpgSlider(cx,top+row(108),cw,18,Component.empty(),(MinimapOptions.opacity-.35)/.65){
            {updateMessage();}protected void updateMessage(){setMessage(Component.literal("Opacidade: "+Math.round(MinimapOptions.opacity*100)+"%"));}protected void applyValue(){MinimapOptions.opacity=.35+value*.65;}});
        toggle(128,"Coordenadas",MinimapOptions.coordinates,()->MinimapOptions.coordinates=!MinimapOptions.coordinates);
        toggle(148,"Evitar efeitos",MinimapOptions.avoidEffects,()->MinimapOptions.avoidEffects=!MinimapOptions.avoidEffects);
        addRenderableWidget(Button.builder(Component.literal("Mover na tela…"),b->minecraft.setScreen(new MinimapPositionScreen(this))).bounds(cx,top+row(168),cw,18).build());
        addRenderableWidget(Button.builder(Component.literal("Formato: "+(MinimapOptions.round?"redondo":"quadrado")),b->{MinimapOptions.round=!MinimapOptions.round;rebuildWidgets();}).bounds(cx,top+row(188),cw,18).build());
        int bw=(w-28)/2;addRenderableWidget(Button.builder(Component.literal("Restaurar padrão"),b->{MinimapOptions.defaults();rebuildWidgets();}).bounds(left+10,top+h-26,bw,18).build());
        addRenderableWidget(Button.builder(Component.literal("Salvar e voltar"),b->onClose()).bounds(left+18+bw,top+h-26,bw,18).build());
    }
    private int row(int y){return h>=250?y:(int)Math.round(y*(h-40)/210.0);}
    private void toggle(int y,String label,boolean value,Runnable action){addRenderableWidget(Button.builder(Component.literal(label+": "+(value?"sim":"não")),b->{action.run();rebuildWidgets();}).bounds(cx,top+row(y),cw,18).build());}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        RpgUi.panel(g,left,top,w,h);g.drawCenteredString(font,title,width/2,top+10,RpgUi.GOLD);
        int size=MinimapOptions.size,previewX=left+8+(178-size-8)/2,previewY=top+36;
        // Display exact size. A detached preview avoids the footer on compact GUI scales.
        if(h<size+98){if(left>=size+16){previewX=left-size-14;previewY=Math.max(20,(height-size-30)/2);}else previewY=Math.max(4,top+h-30-(size+(MinimapOptions.coordinates?30:18)));}
        g.drawCenteredString(font,"PRÉVIA • "+size+" px",previewX+size/2+4,previewY-13,RpgUi.MUTED);
        SimpleMinimap.draw(g,new SimpleMinimap.Bounds(previewX,previewY,size,size+(MinimapOptions.coordinates?30:18)));
        super.render(g,mx,my,partial);
    }
}
