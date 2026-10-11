package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.Items;

/** Navigation hub; skills are used through the configured gameplay keys. */
public final class RpgScreen extends Screen {
    private RpgNetwork.State state;private Button tree,attributes,classes;private int left,top,w,h,ticks;
    public RpgScreen(){super(Component.literal("Oxy RPG"));state=OxyRpgClient.state();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public boolean keyPressed(KeyEvent e){if(OxyRpgClient.OPEN_MENU.matches(e)){onClose();return true;}return super.keyPressed(e);}
    @Override protected void init(){
        w=Math.min(510,width-16);h=Math.min(290,height-12);left=(width-w)/2;top=(height-h)/2;
        int col=(w-38)/2,gap=7,body=top+76,row=(h-127)/3,tile=Math.max(24,row-gap),right=left+26+col;
        classes=addRenderableWidget(RpgUi.button(left+12,body,col,tile,"Classes","Encontre seu estilo",Items.TOTEM_OF_UNDYING,RpgUi.GOLD,b->{if(state!=null)minecraft.setScreen(new ClassSelectionScreen(this,state));}));
        tree=addRenderableWidget(RpgUi.button(right,body,col,tile,"Árvore de habilidades","Escolha seu caminho",Items.ENCHANTED_BOOK,0xFFB397E9,b->{if(state!=null)minecraft.setScreen(new SkillTreeScreen(this,state));}));
        attributes=addRenderableWidget(RpgUi.button(left+12,body+row,col,tile,"Atributos","Planeje sua evolução",Items.APPLE,0xFFE98C8C,b->minecraft.setScreen(new AttributesScreen(this,OxyRpgClient.resources()))));
        addRenderableWidget(RpgUi.button(right,body+row,col,tile,"Diário de missões","Objetivos e recompensas",Items.WRITABLE_BOOK,0xFFE9C77C,b->{if(state!=null)minecraft.setScreen(new MissionScreen(this,state));}));
        addRenderableWidget(RpgUi.button(left+12,body+row*2,col,tile,"Trabalhos","Mineração e agricultura",Items.IRON_PICKAXE,0xFF88CBAF,b->minecraft.setScreen(new ProfessionsScreen(this,OxyRpgClient.professions()))));
        addRenderableWidget(RpgUi.button(right,body+row*2,col,tile,"Arsenal","Conheça seu equipamento",Items.IRON_SWORD,0xFF93BFEA,b->minecraft.setScreen(new ArsenalScreen(this))));
        int bw=(w-36)/4,y=top+h-30;
        addRenderableWidget(Button.builder(Component.literal("Câmera"),b->minecraft.setScreen(new CameraSettingsScreen(this))).bounds(left+12,y,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Interface"),b->minecraft.setScreen(new HudSettingsScreen(this))).bounds(left+16+bw,y,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Ajustes"),b->net.neoforged.fml.ModList.get().getModContainerById("oxyrpg").ifPresent(mod->minecraft.setScreen(new net.neoforged.neoforge.client.gui.ConfigurationScreen(mod,this)))).bounds(left+20+2*bw,y,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"),b->onClose()).bounds(left+24+3*bw,y,bw,20).build());
        if(minecraft.player!=null)minecraft.player.connection.send(new RpgNetwork.Request(0));updateButtons();
    }
    public void update(RpgNetwork.State value){state=value;updateButtons();}
    private void updateButtons(){if(tree==null)return;classes.active=state!=null;tree.active=state!=null;attributes.active=state!=null&&state.rpgClass()!=0;
        classes.setMessage(Component.literal(state==null||state.rpgClass()==0?"Escolher classe":state.testing()?"Trocar classe":"Sua classe"));
        if(state!=null)tree.setMessage(Component.literal("Habilidades • "+Math.max(0,state.level()/2-Long.bitCount(state.skills()))+" pts"));
        var resources=OxyRpgClient.resources();attributes.setMessage(Component.literal("Atributos"+(resources==null?"":" • "+resources.points()+" pts")));
    }
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){onClose();return;}updateButtons();if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        RpgUi.panel(g,left,top,w,h);g.drawString(font,Component.literal("OXY RPG").withStyle(net.minecraft.ChatFormatting.BOLD),left+16,top+15,RpgUi.GOLD);
        String c=state==null?"Carregando…":RpgClass.values()[state.rpgClass()].label()+" • Nível "+state.level();g.drawString(font,font.plainSubstrByWidth(c,w-32),left+16,top+33,RpgUi.TEXT);
        if(state!=null){String xp=state.level()==100?"Nível máximo":state.xp()+" / "+state.level()*100+" XP";g.drawString(font,xp,left+w-16-font.width(xp),top+15,RpgUi.MUTED);VanillaTheme.progress(g,left+16,top+51,w-32,state.level()==100?1:state.xp(),state.level()==100?1:state.level()*100);}
        g.drawString(font,"SUA JORNADA",left+16,top+63,RpgUi.MUTED);super.render(g,mx,my,partial);
    }
}
