package dev.emersonoxy.rpg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CombatSettingsScreen extends Screen {
    private final Screen parent;private int left,top,w;
    public CombatSettingsScreen(Screen parent){super(Component.literal("Combate Oxy RPG"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){CombatOptions.save();minecraft.setScreen(parent);}
    @Override public void removed(){CombatOptions.save();}
    @Override protected void init(){
        CombatOptions.load();w=Math.min(370,width-16);left=(width-w)/2;top=Math.max(4,(height-234)/2);
        toggle(30,"Números de dano",CombatOptions.numbers,()->CombatOptions.numbers=!CombatOptions.numbers);
        toggle(54,"Confirmação na mira",CombatOptions.marker,()->CombatOptions.marker=!CombatOptions.marker);
        toggle(78,"Som de acerto",CombatOptions.sound,()->CombatOptions.sound=!CombatOptions.sound);
        toggle(102,"Agrupar ataques rápidos",CombatOptions.group,()->CombatOptions.group=!CombatOptions.group);
        addRenderableWidget(new RpgSlider(left+12,top+126,w-24,20,Component.empty(),CombatOptions.size-.6){
            {updateMessage();}
            @Override protected void updateMessage(){setMessage(Component.literal("Tamanho do dano: "+Math.round(CombatOptions.size*100)+"%"));}
            @Override protected void applyValue(){CombatOptions.size=.6+value;}
        });
        toggle(150,"Marcações no chão antes de lançar",CombatOptions.preview,()->{CombatOptions.preview=!CombatOptions.preview;AbilityPreview.clear();});
        addRenderableWidget(Button.builder(Component.translatable("screen.oxyrpg.combat_visuals"),b->minecraft.setScreen(new CombatVisualSettingsScreen(this))).bounds(left+12,top+174,w-24,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+206,w-24,20).build());
    }
    private void toggle(int y,String label,boolean value,Runnable change){addRenderableWidget(Button.builder(Component.literal(label+": "+(value?"sim":"não")),b->{change.run();CombatOptions.save();rebuildWidgets();}).bounds(left+12,top+y,w-24,20).build());}
    @Override public void render(GuiGraphics g,int x,int y,float delta){RpgUi.panel(g,left,top,w,234);g.drawCenteredString(font,title,width/2,top+12,RpgUi.GOLD);super.render(g,x,y,delta);}
}
