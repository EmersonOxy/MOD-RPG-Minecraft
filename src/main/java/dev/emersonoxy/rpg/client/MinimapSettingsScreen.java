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
        MinimapOptions.load();w=Math.min(470,width-16);h=Math.min(244,height-12);left=(width-w)/2;top=(height-h)/2;cx=left+Math.min(178,w/2);cw=w-(cx-left)-12;
        toggle(28,"Visível",DynamicHud.enabled(DynamicHud.Part.MINIMAP),()->DynamicHud.toggle(DynamicHud.Part.MINIMAP));
        addRenderableWidget(Button.builder(Component.literal(MinimapOptions.cornerLabel()),b->{MinimapOptions.corner=MinimapOptions.manual?0:(MinimapOptions.corner+1)%4;MinimapOptions.manual=false;rebuildWidgets();}).bounds(cx,top+48,cw,18).build());
        addRenderableWidget(new RpgSlider(cx,top+68,cw,18,Component.empty(),(MinimapOptions.size-72)/88.0){
            {updateMessage();}protected void updateMessage(){setMessage(Component.literal("Tamanho: "+MinimapOptions.size+" px"));}protected void applyValue(){MinimapOptions.size=72+(int)Math.round(value*88);}});
        addRenderableWidget(new RpgSlider(cx,top+88,cw,18,Component.empty(),MinimapOptions.zoom/2.0){
            {updateMessage();}protected void updateMessage(){setMessage(Component.literal("Zoom: "+new String[]{"longe","médio","perto"}[MinimapOptions.zoom]));}protected void applyValue(){MinimapOptions.zoom=(int)Math.round(value*2);}});
        addRenderableWidget(new RpgSlider(cx,top+108,cw,18,Component.empty(),(MinimapOptions.opacity-.35)/.65){
            {updateMessage();}protected void updateMessage(){setMessage(Component.literal("Opacidade: "+Math.round(MinimapOptions.opacity*100)+"%"));}protected void applyValue(){MinimapOptions.opacity=.35+value*.65;}});
        toggle(128,"Coordenadas",MinimapOptions.coordinates,()->MinimapOptions.coordinates=!MinimapOptions.coordinates);
        toggle(148,"Evitar efeitos",MinimapOptions.avoidEffects,()->MinimapOptions.avoidEffects=!MinimapOptions.avoidEffects);
        addRenderableWidget(Button.builder(Component.literal("Mover na tela…"),b->minecraft.setScreen(new MinimapPositionScreen(this))).bounds(cx,top+168,cw,18).build());
        int bw=(w-28)/2;addRenderableWidget(Button.builder(Component.literal("Restaurar padrão"),b->{MinimapOptions.defaults();rebuildWidgets();}).bounds(left+10,top+h-26,bw,18).build());
        addRenderableWidget(Button.builder(Component.literal("Salvar e voltar"),b->onClose()).bounds(left+18+bw,top+h-26,bw,18).build());
    }
    private void toggle(int y,String label,boolean value,Runnable action){addRenderableWidget(Button.builder(Component.literal(label+": "+(value?"sim":"não")),b->{action.run();rebuildWidgets();}).bounds(cx,top+y,cw,18).build());}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        RpgUi.panel(g,left,top,w,h);g.drawCenteredString(font,title,width/2,top+10,RpgUi.GOLD);
        int available=cx-left-20,size=Math.min(h-126,Math.min(available-8,MinimapOptions.size));g.drawCenteredString(font,"PRÉVIA",left+available/2+8,top+31,RpgUi.MUTED);
        SimpleMinimap.draw(g,new SimpleMinimap.Bounds(left+8+(available-size-8)/2,top+46,size,size+(MinimapOptions.coordinates?30:18)));
        g.drawCenteredString(font,"Norte fixo",left+available/2+8,top+h-48,RpgUi.MUTED);
        super.render(g,mx,my,partial);
    }
}
