package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;

/** Quest journal: objectives, progress and reward are presented on a readable page. */
public final class MissionScreen extends Screen {
    private final Screen parent;private RpgNetwork.State state;private int left,top,w,h,ticks,tab,scroll,contentHeight;private final Button[] tabs=new Button[2];
    public MissionScreen(Screen parent,RpgNetwork.State state){super(Component.literal("Diário de missões"));this.parent=parent;this.state=state;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    public void update(RpgNetwork.State value){state=value;}
    @Override protected void init(){
        w=Math.min(590,width-16);h=Math.min(330,height-12);left=(width-w)/2;top=(height-h)/2;int side=Math.min(155,w/3);
        tabs[0]=addRenderableWidget(RpgUi.button(left+12,top+52,side-10,38,"Caçadas","Sua missão atual",Items.IRON_SWORD,RpgUi.GOLD,b->{tab=0;scroll=0;}));
        tabs[1]=addRenderableWidget(RpgUi.button(left+12,top+97,side-10,38,"Contratos","Missões dos aldeões",Items.EMERALD,0xFF8DD8B5,b->{tab=1;scroll=0;}));
        addRenderableWidget(RpgUi.button(left+12,top+142,side-10,38,"Treinamento","Pratique sua classe",Items.ENCHANTED_BOOK,0xFFBEA2E5,b->minecraft.setScreen(new ClassJourneyScreen(this))));
        addRenderableWidget(Button.builder(Component.literal("Voltar"),b->onClose()).bounds(left+12,top+h-30,w-24,20).build());
        if(minecraft.player!=null)minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void tick(){if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}if(++ticks%40==0)minecraft.player.connection.send(new RpgNetwork.Request(0));}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){int side=Math.min(155,w/3);if(x>left+side+14&&x<left+w-12&&y>top+45&&y<top+h-42){scroll=Math.clamp(scroll-(int)(dy*22),0,Math.max(0,contentHeight-(h-107)));return true;}return super.mouseScrolled(x,y,dx,dy);}
    private int text(GuiGraphics g,String msg,int x,int y,int width,int color){for(var line:font.split(Component.literal(msg),width)){g.drawString(font,line,x,y,color,false);y+=12;}return y;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        RpgUi.panel(g,left,top,w,h);g.renderFakeItem(new ItemStack(Items.WRITABLE_BOOK),left+14,top+12);g.drawString(font,Component.literal("DIÁRIO DE MISSÕES").withStyle(net.minecraft.ChatFormatting.BOLD),left+38,top+16,RpgUi.GOLD);
        int side=Math.min(155,w/3),x=left+side+16,pw=w-side-30,body=top+49;
        g.fill(x-5,body-5,x+pw+5,top+h-42,0xFFD8C7A3);RpgUi.border(g,x-5,body-5,pw+10,h-86,0xFF8F7754);g.fill(x-2,body-2,x+1,top+h-45,0xFFBFA77F);
        g.enableScissor(x+2,body,x+pw-4,top+h-47);int y=body+10-scroll;
        if(tab==0){
            var quest=QuestCatalog.quest(state.questStage());y=text(g,quest.title(),x+10,y,pw-24,0xFF4C321D)+8;
            y=text(g,"OBJETIVOS",x+10,y,pw-24,0xFF725434)+8;
            for(int i=0;i<quest.targets().size();i++){
                var target=quest.targets().get(i);int progress=i==0?state.questFirst():state.questSecond();boolean done=progress>=target.amount();
                var item=BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(target.icon()));g.renderFakeItem(new ItemStack(item==null?Items.PAPER:item),x+10,y);
                y=text(g,target.label()+" • "+progress+" / "+target.amount(),x+34,y+2,pw-48,done?0xFF376247:0xFF3B3024);
                g.fill(x+34,y+3,x+pw-14,y+8,0xFFB09B78);int filled=(pw-48)*progress/Math.max(1,target.amount());g.fill(x+34,y+3,x+34+filled,y+8,done?0xFF658968:0xFF9B7338);y+=24;
            }
            y=text(g,"RECOMPENSA",x+10,y+5,pw-24,0xFF725434)+7;y=text(g,"+"+quest.reward()+" XP RPG",x+10,y,pw-24,0xFF3C6047)+12;
            y=text(g,state.rpgClass()==0?"Escolha uma classe para iniciar a caçada.":"Conclua todos os objetivos para receber a recompensa automaticamente.",x+10,y,pw-24,0xFF685B49)+12;
            y=text(g,"Próxima caçada: "+QuestCatalog.quest(state.questStage()+1).title(),x+10,y,pw-24,0xFF685B49);
        }else{
            y=text(g,"Contratos de aldeões",x+10,y,pw-24,0xFF4C321D)+14;var professions=OxyRpgClient.professions();
            y=text(g,professions==null?"Carregando contrato…":professions.journal(),x+10,y,pw-24,0xFF3B3024)+18;
            y=text(g,"Converse com um aldeão adulto para receber uma tarefa. Depois de concluir, volte ao mesmo aldeão para entregar e receber sua recompensa.",x+10,y,pw-24,0xFF685B49);
        }
        contentHeight=y+scroll-body+12;g.disableScissor();g.fill(left+10,top+52+tab*45,left+12,top+90+tab*45,RpgUi.GOLD);
        if(contentHeight>h-107)g.drawString(font,"Role para ler a página",x+8,top+h-42,RpgUi.MUTED);super.render(g,mx,my,partial);
    }
}
