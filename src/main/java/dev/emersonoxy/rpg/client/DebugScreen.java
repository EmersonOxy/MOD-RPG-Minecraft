package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgClass;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Uses the same operator-validated commands as chat; no client-side authority over debug. */
public final class DebugScreen extends Screen {
    private final Screen parent;private int x,y,w,h,ticks;private Button mode,skills,resources,cooldowns,attributes,arsenal;
    private final java.util.List<Button> controls=new java.util.ArrayList<>();
    public DebugScreen(Screen parent){super(Component.literal("Debug Oxy RPG"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    private RpgNetwork.DebugState state(){return OxyRpgClient.debugState();}
    private void command(String suffix){if(minecraft.player!=null)minecraft.player.connection.sendCommand("rpg debug "+suffix);}
    private int row(int n){return n*h/276;}
    private Button button(int column,int offset,String text,Runnable action){int cw=(w-28)/2;return addRenderableWidget(Button.builder(Component.literal(text),b->action.run()).bounds(x+12+column*(cw+4),y+row(offset),cw,Math.max(14,Math.min(20,row(22)))).build());}
    private Button control(int column,int offset,String text,Runnable action){var b=button(column,offset,text,action);controls.add(b);return b;}
    @Override protected void init(){
        controls.clear();w=Math.min(420,width-16);h=Math.min(276,height-12);x=(width-w)/2;y=(height-h)/2;
        int cw=(w-36)/4;String[] labels={"−10","−1","+1","+10"};int[] amounts={-10,-1,1,10};
        for(int i=0;i<4;i++){final int value=amounts[i];controls.add(addRenderableWidget(Button.builder(Component.literal(labels[i]),b->command("nivel "+(value<0?"descer ":"subir ")+Math.abs(value))).bounds(x+12+i*(cw+4),y+row(66),cw,Math.max(14,Math.min(20,row(22)))).build()));}
        mode=button(0,94,"Debug",()->command(state()!=null&&state().mode()==1?"off":"on"));
        skills=control(1,94,"Liberar todas as skills",()->command("skills todas"));
        resources=control(0,120,"Recursos",()->command("recursos "+(state()!=null&&state().resources()?"normais":"ilimitados")));
        cooldowns=control(1,120,"Recargas",()->command("cooldowns "+(state()!=null&&state().cooldowns()?"off":"on")));
        attributes=control(0,146,"Atributos",()->command("atributos "+(state()!=null&&state().attributes()?"normais":"maximos")));
        arsenal=control(1,146,"Arsenal",()->command("arsenal "+(state()!=null&&state().arsenal()?"off":"on")));
        control(0,172,"Trocar de classe",()->{var s=state();if(s!=null){int next=s.rpgClass()%9+1;command("classe "+RpgClass.values()[next].name().toLowerCase(java.util.Locale.ROOT));}});
        control(1,172,"Limpar recargas das skills",()->command("cooldowns limpar"));
        control(0,198,"Maximizar tudo",()->command("tudo"));control(1,198,"Recuperar vida / mana",()->command("recuperar"));
        control(0,224,"Limpar skills temporárias",()->command("skills limpar"));button(1,224,"Voltar",this::onClose);
        if(minecraft.player!=null)minecraft.player.connection.send(new RpgNetwork.Request(0));tick();
    }
    @Override public void tick(){
        if(minecraft.player==null||minecraft.level==null){onClose();return;}var s=state();boolean active=s!=null&&s.mode()==1;
        controls.forEach(b->b.active=active);mode.active=minecraft.player.hasPermissions(2);mode.setMessage(Component.literal(active?"Desativar debug":"Ativar debug completo"));
        skills.setMessage(Component.literal(s!=null&&s.allSkills()?"Skills: todas liberadas":"Liberar todas as skills"));
        resources.setMessage(Component.literal("Recursos: "+(s!=null&&s.resources()?"ilimitados":"normais")));
        cooldowns.setMessage(Component.literal("Recargas: "+(s!=null&&s.cooldowns()?"ativas":"desligadas")));
        attributes.setMessage(Component.literal("Atributos: "+(s!=null&&s.attributes()?"máximos":"salvos")));
        arsenal.setMessage(Component.literal("Arsenal: "+(s!=null&&s.arsenal()?"máximo":"salvo")));
        if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        RpgUi.panel(g,x,y,w,h);g.drawCenteredString(font,title,width/2,y+row(12),RpgUi.GOLD);var s=state();
        String text=s==null?"Carregando…":(s.mode()==1?"DEBUG":s.mode()==2?"TESTE":"NORMAL")+" • "+RpgClass.values()[s.rpgClass()].label()+" • Nível "+s.level();
        g.drawCenteredString(font,font.plainSubstrByWidth(text,w-24),width/2,y+row(40),RpgUi.TEXT);
        g.drawCenteredString(font,"Progressão temporária; sair restaura o perfil salvo.",width/2,y+row(255),RpgUi.MUTED);super.render(g,mx,my,dt);
    }
}
