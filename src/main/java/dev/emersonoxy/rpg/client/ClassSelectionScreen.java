package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Class identity with an embedded owo component selector and native detail cards. */
public final class ClassSelectionScreen extends Screen {
    private final RpgScreen parent;private RpgNetwork.State state;private RpgClass selected;
    private int left,top,w,h,scroll,ticks,pending,contentHeight;private Button confirm;
    private io.wispforest.owo.ui.core.OwoUIAdapter<io.wispforest.owo.ui.container.FlowLayout> selector;
    private long opened,changed;
    public ClassSelectionScreen(RpgScreen parent,RpgNetwork.State state){super(Component.literal("Escolha seu caminho"));this.parent=parent;this.state=state;selected=state.rpgClass()==0?RpgClass.GUERREIRO:RpgClass.values()[state.rpgClass()];}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void init(){
        w=Math.min(740,width-16);h=Math.min(410,height-12);left=(width-w)/2;top=(height-h)/2;
        if(selector!=null)selector.dispose();
        opened=changed=System.nanoTime();
        selector=io.wispforest.owo.ui.core.OwoUIAdapter.createWithoutScreen(0,0,width,height,io.wispforest.owo.ui.container.Containers::verticalFlow);
        var row=io.wispforest.owo.ui.container.Containers.horizontalFlow(io.wispforest.owo.ui.core.Sizing.fixed(w-28),io.wispforest.owo.ui.core.Sizing.fixed(43));
        row.positioning(io.wispforest.owo.ui.core.Positioning.absolute(left+14,top+53));
        selector.rootComponent.child(row);
        int count=RpgClass.values().length-1,tabW=(w-28)/count;
        for(int i=1;i<RpgClass.values().length;i++){
            var c=RpgClass.values()[i];
            var tab=io.wispforest.owo.ui.component.Components.button(Component.empty(),b->{selected=c;scroll=0;changed=System.nanoTime();});
            tab.sizing(io.wispforest.owo.ui.core.Sizing.fixed(tabW),io.wispforest.owo.ui.core.Sizing.fixed(43));
            tab.setTooltip(Tooltip.create(Component.literal(c.label()+": "+role(c))));
            tab.renderer((g,b,delta)->{int color=accent(c);boolean chosen=selected==c;int x=b.getX(),y=b.getY(),tw=b.getWidth()-4;g.fill(x,y,x+tw,y+43,chosen?0xFF303C4D:b.isHoveredOrFocused()?0xFF263142:0xFF1B2330);RpgUi.border(g,x,y,tw,43,chosen||b.isHoveredOrFocused()?color:0xFF3B4658);g.renderFakeItem(new ItemStack(icon(c)),x+tw/2-8,y+5);g.drawCenteredString(font,font.plainSubstrByWidth(c.label(),tw-6),x+tw/2,y+29,chosen?color:RpgUi.TEXT);});
            row.child(tab);
        }
        selector.inflateAndMount();
        int bw=(w-36)/3;
        confirm=addRenderableWidget(RpgUi.button(left+12,top+h-32,bw,20,"Escolher classe","",Items.EMERALD,RpgUi.GOLD,v->{if(canChoose()){pending=100;minecraft.player.connection.send(new RpgNetwork.Request(selected==RpgClass.PIROMANTE?15:selected==RpgClass.HIDROMANTE?14:selected==RpgClass.ILUSIONISTA?13:selected==RpgClass.AVIARIO?11:selected==RpgClass.XAMA?10:selected==RpgClass.NECROMANTE?7:selected.ordinal()+1));}}));
        addRenderableWidget(Button.builder(Component.literal("Ver árvore"),v->minecraft.setScreen(new SkillTreeScreen(this,state,selected))).bounds(left+18+bw,top+h-32,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),v->onClose()).bounds(left+24+2*bw,top+h-32,bw,20).build());
        // The full-screen adapter must follow the footer in the native hit-test order.
        // getChildAt chooses the first overlapping child even if it declines the click.
        addRenderableWidget(selector);
    }
    private boolean canChoose(){return pending==0&&(state.rpgClass()==0||state.testing()||state.debug())&&selected.ordinal()!=state.rpgClass();}
    public void update(RpgNetwork.State value){boolean done=pending>0&&value.rpgClass()==selected.ordinal();state=value;pending=0;parent.update(value);if(done)onClose();}
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){onClose();return;}if(pending>0)pending--;if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    private int viewport(){return h-154;}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(x>left+12&&x<left+w-12&&y>top+107&&y<top+h-43){scroll=Math.clamp(scroll-(int)(sy*24),0,Math.max(0,contentHeight-viewport()));return true;}return super.mouseScrolled(x,y,sx,sy);}
    private int text(GuiGraphics g,String message,int x,int y,int width,int color){for(var line:font.split(Component.literal(message),width)){g.drawString(font,line,x,y,color);y+=11;}return y;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        HudOpacity.begin(UiTransitions.opacity(opened,260));
        RpgUi.panel(g,left,top,w,h);g.drawCenteredString(font,Component.literal("ESCOLHA SEU CAMINHO").withStyle(net.minecraft.ChatFormatting.BOLD),width/2,top+14,RpgUi.GOLD);
        String mode=(state.testing()||state.debug())?"Modo de desenvolvimento • troque livremente":state.rpgClass()==0?"Cada classe tem sua identidade, vantagens e limitações":"Explore as classes • seu personagem já tem uma classe";
        g.drawCenteredString(font,font.plainSubstrByWidth(mode,w-32),width/2,top+32,RpgUi.MUTED);
        int body=top+111,leftW=Math.min(215,(w-40)/3),lx=left+16,rx=lx+leftW+18,rw=w-leftW-50,accent=accent(selected);
        RpgUi.section(g,lx-3,body-3,leftW+6,viewport(),accent);RpgUi.section(g,rx-4,body-3,rw+8,viewport(),accent);
        HudOpacity.end();HudOpacity.begin(UiTransitions.opacity(changed,180));
        g.enableScissor(left+12,body-2,left+w-12,top+h-43);int y=body+10-scroll;
        float scale=h<300?2:3;RpgUi.medallion(g,lx+leftW/2,y+(int)(8*scale),(int)(10*scale),accent);g.pose().pushMatrix();g.pose().translate(lx+leftW/2f-8*scale,y);g.pose().scale(scale,scale);g.renderFakeItem(new ItemStack(icon(selected)),0,0);g.pose().popMatrix();
        y+=(int)(16*scale)+14;g.drawCenteredString(font,Component.literal(selected.label()).withStyle(net.minecraft.ChatFormatting.BOLD),lx+leftW/2,y,accent);y+=18;
        y=text(g,role(selected),lx+8,y,leftW-16,RpgUi.TEXT)+15;
        g.drawString(font,"CONSIDERE",lx+8,y,RpgUi.GOLD);y+=14;
        String limits=ClassRules.description(selected);
        y=text(g,limits,lx+8,y,leftW-16,RpgUi.MUTED)+12;
        int heroHeight=y+scroll-body;
        int sy=body+10-scroll;g.drawString(font,"HABILIDADES DA CLASSE",rx+8,sy,RpgUi.GOLD);sy+=20;
        var keys=new net.minecraft.client.KeyMapping[]{OxyRpgClient.USE_ABILITY,OxyRpgClient.SECOND_ABILITY,OxyRpgClient.THIRD_ABILITY};
        for(int slot=0;slot<3;slot++){
            var skill=SkillTree.nodes(selected).get(slot*7+3);int wrap=Math.max(50,rw-48);
            String label=(selected==RpgClass.AVIARIO&&slot==1?"Passiva":keys[slot].getTranslatedKeyMessage().getString())+" • "+skill.title();
            var titleLines=font.split(Component.literal(label),wrap);var lines=font.split(Component.literal(summary(selected,slot)),wrap);int cardH=24+(titleLines.size()+lines.size())*11;
            g.fill(rx+4,sy,rx+rw-4,sy+cardH,0xFF242F3E);g.fill(rx+4,sy,rx+6,sy+cardH,accent);
            SkillGlyphs.draw(g,selected,slot,rx+10,sy+9,20,accent);
            int ty=sy+10;for(var line:titleLines){g.drawString(font,line,rx+34,ty,accent);ty+=11;}ty+=4;for(var line:lines){g.drawString(font,line,rx+34,ty,RpgUi.TEXT);ty+=11;}sy+=cardH+8;
        }
        contentHeight=Math.max(heroHeight,sy+scroll-body);g.disableScissor();
        if(contentHeight>viewport())g.drawString(font,"Role para ler mais",left+w-12-font.width("Role para ler mais"),top+h-43,RpgUi.MUTED);
        HudOpacity.end();HudOpacity.begin(UiTransitions.opacity(opened,260));confirm.active=canChoose();confirm.setMessage(Component.literal(pending>0?"Aguardando…":(state.testing()||state.debug())?"Testar "+selected.label():state.rpgClass()!=0?"Classe definida":"Escolher "+selected.label()));super.render(g,mx,my,partial);HudOpacity.end();
    }
    @Override public void removed(){if(selector!=null)selector.dispose();super.removed();}
    static Item icon(RpgClass c){return switch(c){case GUERREIRO->Items.IRON_SWORD;case MAGO->Items.BLAZE_ROD;case ARQUEIRO->Items.BOW;case NECROMANTE->Items.BOOK;case XAMA->Items.AMETHYST_SHARD;case AVIARIO->Items.FEATHER;case ILUSIONISTA->Items.ENDER_EYE;case HIDROMANTE->Items.HEART_OF_THE_SEA;case PIROMANTE->Items.MAGMA_CREAM;default->Items.PAPER;};}
    static int accent(RpgClass c){return switch(c){case GUERREIRO->0xFFE49A7D;case MAGO->0xFF99C9F5;case ARQUEIRO->0xFFB7D48A;case NECROMANTE->0xFFC6A0E4;case XAMA->0xFF8DD8B5;case AVIARIO->0xFFF0D79F;case ILUSIONISTA->0xFFCF9AF2;case HIDROMANTE->0xFF70DCEB;case PIROMANTE->0xFFFFAA62;default->RpgUi.GOLD;};}
    public void contentChanged(){rebuildWidgets();}
    public static String role(RpgClass c){return switch(c){case GUERREIRO->"A linha de frente. Domine o combate próximo, sustente-se com roubo de vida e resista aos inimigos.";case MAGO->"Controle o campo de batalha. Combine gelo, fogo e tremores para enfrentar grupos.";case ARQUEIRO->"Precisão e mobilidade. Ataque à distância, desapareça e reposicione-se rapidamente.";case NECROMANTE->"Comande suas invocações. Aranhas, mortos-vivos e phantoms lutam ao seu lado.";case XAMA->"Natureza e suporte. Combine raios, animais protetores, cura e poções.";case ILUSIONISTA->"Engane seus inimigos. Copie aparências, suspenda alvos e fuja como um enxame intangível.";case HIDROMANTE->"Domine a água. Explore as profundezas, lance jatos e caminhe sobre a superfície.";case PIROMANTE->"Prepare o terreno. Combine fogos, núcleos de magma e calor para assumir a forma da Fênix.";case AVIARIO->"Domine os céus. Voe, controle sua descida e transforme o pouso em um impacto devastador.";default->"Escolha uma classe para começar sua jornada.";};}
    private static String summary(RpgClass c,int slot){return switch(c){
        case GUERREIRO->new String[]{"Atinge inimigos ao redor e fortalece seus ataques.","Recupera vida ao causar dano, consumindo fome durante a ativação.","Assume uma postura defensiva com resistência e retaliação."}[slot];
        case MAGO->new String[]{"Projétil de gelo que paralisa e desacelera inimigos em área.","Canaliza um jato de fogo na direção da mira.","Cria ondas sísmicas que causam dano e desgastam o solo."}[slot];
        case ARQUEIRO->new String[]{"Flecha que atravessa inimigos, destaca os alvos e os desacelera.","Oculta o jogador e seus equipamentos; atacar encerra o efeito.","Avança rapidamente e fortalece ataques com sangramento."}[slot];
        case NECROMANTE->new String[]{"Lança teia e invoca aranhas para atacar o alvo.","Convoca esqueletos e zumbis que acompanham seus ataques.","Cega inimigos próximos e convoca phantoms para mergulhar sobre eles."}[slot];
        case XAMA->new String[]{"Ataques de punho chamam raios durante a ativação.","Invoca animais protetores e uma aura de cura e limpeza.","Com as mãos vazias, lança poções positivas ou negativas."}[slot];
        case ILUSIONISTA->new String[]{"Copia o mob da mira: ilusões atraem ataques enquanto você assume sua aparência.","Suspende o alvo, enfraquece e causa dano; inimigos próximos ficam lentos e desorientados.","Vira um enxame imune a ataques comuns. Voa baixo e atravessa blocos comuns, sem atacar ou interagir."}[slot];
        case HIDROMANTE->new String[]{"Respira e trabalha debaixo da água. Reative para um impulso ou um salto para fora dela.","Jato giratório na mira com dano e empurrão; submerso, cria manifestações marinhas temporárias.","Caminha sobre a água. Ao ativar em contato com água, abre um espaço drenado e recupera vida."}[slot];
        case PIROMANTE->new String[]{"Marca alvos com fogos encantados. Reative para detonar uma sequência de explosões.","Lança magma que adere ao alvo ou ao chão. Reative agachado para detonar núcleos e formar linhas ardentes.","Consome calor para assumir a Fênix: saltos explosivos, brasas e mobilidade na lava; termina em explosão."}[slot];
        case AVIARIO->new String[]{"Decole sem elytra e planeie. Reative para impulsos cada vez mais rápidos.","Receba 30% menos dano de queda. Agache ao cair para descer lentamente e reduzir dano de inimigos.","Voando, avance na direção da mira e exploda ao atingir um alvo ou obstáculo. Caindo, mergulhe até o solo. Altura e velocidade ampliam o impacto."}[slot];default->"";};}
}
