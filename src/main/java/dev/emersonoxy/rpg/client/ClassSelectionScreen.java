package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Class identity, strengths and limitations, with a compact six-icon selector. */
public final class ClassSelectionScreen extends Screen {
    private final RpgScreen parent;private RpgNetwork.State state;private RpgClass selected;
    private int left,top,w,h,scroll,ticks,pending,contentHeight;private Button confirm;
    public ClassSelectionScreen(RpgScreen parent,RpgNetwork.State state){super(Component.literal("Escolha seu caminho"));this.parent=parent;this.state=state;selected=state.rpgClass()==0?RpgClass.GUERREIRO:RpgClass.values()[state.rpgClass()];}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        w=Math.min(740,width-16);h=Math.min(410,height-12);left=(width-w)/2;top=(height-h)/2;
        int count=RpgClass.values().length-1,tabW=(w-28)/count;
        for(int i=1;i<RpgClass.values().length;i++)addRenderableWidget(new ClassTab(RpgClass.values()[i],left+14+(i-1)*tabW,top+53,tabW-4));
        int bw=(w-36)/3;
        confirm=addRenderableWidget(RpgUi.button(left+12,top+h-32,bw,20,"Escolher classe","",Items.EMERALD,RpgUi.GOLD,v->{if(canChoose()){pending=100;minecraft.player.connection.send(new RpgNetwork.Request(selected==RpgClass.AVIARIO?11:selected==RpgClass.XAMA?10:selected==RpgClass.NECROMANTE?7:selected.ordinal()+1));}}));
        addRenderableWidget(Button.builder(Component.literal("Ver árvore"),v->minecraft.setScreen(new SkillTreeScreen(this,state,selected))).bounds(left+18+bw,top+h-32,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),v->onClose()).bounds(left+24+2*bw,top+h-32,bw,20).build());
    }
    private boolean canChoose(){return pending==0&&(state.rpgClass()==0||state.testing())&&selected.ordinal()!=state.rpgClass();}
    public void update(RpgNetwork.State value){boolean done=pending>0&&value.rpgClass()==selected.ordinal();state=value;pending=0;parent.update(value);if(done)onClose();}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){onClose();return;}if(pending>0)pending--;if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    private int viewport(){return h-154;}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(x>left+12&&x<left+w-12&&y>top+107&&y<top+h-43){scroll=Math.clamp(scroll-(int)(sy*24),0,Math.max(0,contentHeight-viewport()));return true;}return super.mouseScrolled(x,y,sx,sy);}
    private int text(GuiGraphics g,String message,int x,int y,int width,int color){for(var line:font.split(Component.literal(message),width)){g.drawString(font,line,x,y,color);y+=11;}return y;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        RpgUi.panel(g,left,top,w,h);g.drawCenteredString(font,Component.literal("ESCOLHA SEU CAMINHO").withStyle(net.minecraft.ChatFormatting.BOLD),width/2,top+14,RpgUi.GOLD);
        String mode=state.testing()?"Modo de teste • troque livremente":state.rpgClass()==0?"Cada classe tem sua identidade, vantagens e limitações":"Explore as classes • seu personagem já tem uma classe";
        g.drawCenteredString(font,font.plainSubstrByWidth(mode,w-32),width/2,top+32,RpgUi.MUTED);
        int body=top+111,leftW=Math.min(215,(w-40)/3),lx=left+16,rx=lx+leftW+18,rw=w-leftW-50,accent=accent(selected);
        RpgUi.section(g,lx-3,body-3,leftW+6,viewport(),accent);RpgUi.section(g,rx-4,body-3,rw+8,viewport(),accent);
        g.enableScissor(left+12,body-2,left+w-12,top+h-43);int y=body+10-scroll;
        float scale=h<300?2:3;RpgUi.medallion(g,lx+leftW/2,y+(int)(8*scale),(int)(10*scale),accent);g.pose().pushMatrix();g.pose().translate(lx+leftW/2f-8*scale,y);g.pose().scale(scale,scale);g.renderFakeItem(new ItemStack(icon(selected)),0,0);g.pose().popMatrix();
        y+=(int)(16*scale)+14;g.drawCenteredString(font,Component.literal(selected.label()).withStyle(net.minecraft.ChatFormatting.BOLD),lx+leftW/2,y,accent);y+=18;
        y=text(g,role(selected),lx+8,y,leftW-16,RpgUi.TEXT)+15;
        g.drawString(font,"CONSIDERE",lx+8,y,RpgUi.GOLD);y+=14;
        String limits=selected==RpgClass.AVIARIO?"Voo temporário. Impulsos gastam mana. Quedas ainda causam dano.":ClassRules.description(selected);
        y=text(g,limits,lx+8,y,leftW-16,RpgUi.MUTED)+12;
        int heroHeight=y+scroll-body;
        int sy=body+10-scroll;g.drawString(font,"HABILIDADES DA CLASSE",rx+8,sy,RpgUi.GOLD);sy+=20;
        var keys=new net.minecraft.client.KeyMapping[]{OxyRpgClient.USE_ABILITY,OxyRpgClient.SECOND_ABILITY,OxyRpgClient.THIRD_ABILITY};
        for(int slot=0;slot<3;slot++){
            var skill=SkillTree.nodes(selected).get(slot*7+3);int wrap=Math.max(50,rw-48);
            String label=(selected==RpgClass.AVIARIO&&slot==1?"Passiva":keys[slot].getTranslatedKeyMessage().getString())+" • "+skill.title();
            var titleLines=font.split(Component.literal(label),wrap);var lines=font.split(Component.literal(summary(selected,slot)),wrap);int cardH=24+(titleLines.size()+lines.size())*11;
            g.fill(rx+4,sy,rx+rw-4,sy+cardH,0xFF242F3E);g.fill(rx+4,sy,rx+6,sy+cardH,accent);
            var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.ResourceLocation.withDefaultNamespace(skill.icon()));g.renderFakeItem(new ItemStack(item),rx+12,sy+11);
            int ty=sy+10;for(var line:titleLines){g.drawString(font,line,rx+34,ty,accent);ty+=11;}ty+=4;for(var line:lines){g.drawString(font,line,rx+34,ty,RpgUi.TEXT);ty+=11;}sy+=cardH+8;
        }
        contentHeight=Math.max(heroHeight,sy+scroll-body);g.disableScissor();
        if(contentHeight>viewport())g.drawString(font,"Role para ler mais",left+w-12-font.width("Role para ler mais"),top+h-43,RpgUi.MUTED);
        confirm.active=canChoose();confirm.setMessage(Component.literal(pending>0?"Aguardando…":state.testing()?"Testar "+selected.label():state.rpgClass()!=0?"Classe definida":"Escolher "+selected.label()));super.render(g,mx,my,partial);
    }
    private final class ClassTab extends Button {
        private final RpgClass c;private final ItemStack item;
        ClassTab(RpgClass c,int x,int y,int w){super(x,y,w,43,Component.literal(c.label()),v->{selected=c;scroll=0;},DEFAULT_NARRATION);this.c=c;item=new ItemStack(icon(c));setTooltip(Tooltip.create(Component.literal(c.label()+": "+role(c))));}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){int color=accent(c);boolean chosen=c==selected;g.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),chosen?0xFF303C4D:isHoveredOrFocused()?0xFF263142:0xFF1B2330);RpgUi.border(g,getX(),getY(),getWidth(),getHeight(),chosen||isHoveredOrFocused()?color:0xFF3B4658);g.renderFakeItem(item,getX()+getWidth()/2-8,getY()+5);g.drawCenteredString(font,font.plainSubstrByWidth(c.label(),getWidth()-6),getX()+getWidth()/2,getY()+29,chosen?color:RpgUi.TEXT);}
    }
    static Item icon(RpgClass c){return switch(c){case GUERREIRO->Items.IRON_SWORD;case MAGO->Items.BLAZE_ROD;case ARQUEIRO->Items.BOW;case NECROMANTE->Items.BOOK;case XAMA->Items.AMETHYST_SHARD;case AVIARIO->Items.FEATHER;default->Items.PAPER;};}
    static int accent(RpgClass c){return switch(c){case GUERREIRO->0xFFE49A7D;case MAGO->0xFF99C9F5;case ARQUEIRO->0xFFB7D48A;case NECROMANTE->0xFFC6A0E4;case XAMA->0xFF8DD8B5;case AVIARIO->0xFFF0D79F;default->RpgUi.GOLD;};}
    public static String role(RpgClass c){return switch(c){case GUERREIRO->"A linha de frente. Domine o combate próximo, sustente-se com roubo de vida e resista aos inimigos.";case MAGO->"Controle o campo de batalha. Combine gelo, fogo e tremores para enfrentar grupos.";case ARQUEIRO->"Precisão e mobilidade. Ataque à distância, desapareça e reposicione-se rapidamente.";case NECROMANTE->"Comande suas invocações. Aranhas, mortos-vivos e phantoms lutam ao seu lado.";case XAMA->"Natureza e suporte. Combine raios, animais protetores, cura e poções.";case AVIARIO->"Domine os céus. Voe, controle sua descida e transforme o pouso em um impacto devastador.";default->"Escolha uma classe para começar sua jornada.";};}
    private static String summary(RpgClass c,int slot){return switch(c){
        case GUERREIRO->new String[]{"Atinge inimigos ao redor e fortalece seus ataques.","Recupera vida ao causar dano, consumindo fome durante a ativação.","Assume uma postura defensiva com resistência e retaliação."}[slot];
        case MAGO->new String[]{"Projétil de gelo que paralisa e desacelera inimigos em área.","Canaliza um jato de fogo na direção da mira.","Cria ondas sísmicas que causam dano e desgastam o solo."}[slot];
        case ARQUEIRO->new String[]{"Flecha que atravessa inimigos, destaca os alvos e os desacelera.","Oculta o jogador e seus equipamentos; atacar encerra o efeito.","Avança rapidamente e fortalece ataques com sangramento."}[slot];
        case NECROMANTE->new String[]{"Lança teia e invoca aranhas para atacar o alvo.","Convoca esqueletos e zumbis que acompanham seus ataques.","Cega inimigos próximos e convoca phantoms para mergulhar sobre eles."}[slot];
        case XAMA->new String[]{"Ataques de punho chamam raios durante a ativação.","Invoca animais protetores e uma aura de cura e limpeza.","Com as mãos vazias, lança poções positivas ou negativas."}[slot];
        case AVIARIO->new String[]{"Decole sem elytra e planeie. Reative para impulsos cada vez mais rápidos.","Receba 30% menos dano de queda. Agache ao cair para descer lentamente e reduzir dano de inimigos.","No ar, prepare um mergulho. Altura e velocidade ampliam a cratera e o dano ao pousar."}[slot];default->"";};}
}
