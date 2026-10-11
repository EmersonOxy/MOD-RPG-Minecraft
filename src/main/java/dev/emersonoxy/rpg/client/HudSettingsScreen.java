package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudSettingsScreen extends Screen {
    private final Screen parent;private int left,top,panelWidth,panelHeight;
    public HudSettingsScreen(Screen parent){super(Component.literal("HUD Oxy RPG"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        panelWidth=Math.min(370,width-16);panelHeight=Math.min(250,height-8);left=(width-panelWidth)/2;top=Math.max(4,(height-panelHeight)/2);
        addRenderableWidget(Button.builder(Component.literal("Modo: "+DynamicHud.modeLabel()),b->{DynamicHud.cycle();rebuildWidgets();})
            .bounds(left+12,top+row(28),panelWidth-24,20).build());
        var parts=DynamicHud.Part.values();int column=(panelWidth-28)/2;
        for(int i=0;i<parts.length;i++){
            var part=parts[i];int x=left+12+(i%2)*(column+4),y=top+row(54+(i/2)*20);
            addRenderableWidget(Button.builder(Component.literal(part.label+": "+(DynamicHud.enabled(part)?"sim":"não")),b->{DynamicHud.toggle(part);rebuildWidgets();})
                .bounds(x,y,column,18).build());
        }
        int footer=(panelWidth-32)/3;
        addRenderableWidget(Button.builder(Component.literal("Padrões"),b->{DynamicHud.defaults();rebuildWidgets();}).bounds(left+12,top+row(218),footer,20).build());
        addRenderableWidget(Button.builder(Component.literal("Combate"),b->minecraft.setScreen(new CombatSettingsScreen(this))).bounds(left+16+footer,top+row(218),footer,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+20+footer*2,top+row(218),footer,20).build());
    }
    private int row(int y){return y*panelHeight/250;}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        RpgUi.panel(g,left,top,panelWidth,panelHeight);g.drawCenteredString(font,title,width/2,top+10,RpgUi.GOLD);
        g.drawString(font,"Dinâmica: 4 s visível + 1,2 s para esmaecer.",left+12,top+row(197),0xFFB7C8D7);
        g.drawString(font,"Vida crítica e falta de ar continuam visíveis.",left+12,top+row(207),0xFFB7C8D7);
        super.render(g,x,y,delta);
    }
}
