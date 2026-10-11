package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.*;
import java.util.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SkillTreeScreen extends GraphScreen {
    private RpgNetwork.State state;
    private RpgClass viewed;
    public SkillTreeScreen(Screen parent,RpgNetwork.State state){super(parent,"Árvore de habilidades");this.state=state;viewed=state.rpgClass()==0?RpgClass.GUERREIRO:RpgClass.values()[state.rpgClass()];}
    public SkillTreeScreen(Screen parent,RpgNetwork.State state,RpgClass viewed){this(parent,state);this.viewed=viewed;}
    @Override protected void tabs(){int count=RpgClass.values().length-1,w=(panelWidth-20)/count;for(int i=0;i<count;i++){var c=RpgClass.values()[i+1];var b=addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth(c.label(),w-8)),v->{viewed=c;selected=0;rebuildWidgets();}).bounds(left+10+i*w,top+29,w-2,20).build());b.active=viewed!=c;}}
    @Override protected void loadNodes(){var list=new ArrayList<Node>();for(var n:SkillTree.nodes(viewed)){
        int id=n.id(),step=id<21?id%7:(id-21)%10;
        double[] position=RpgContent.position(viewed,id,SkillTreeLayouts.nodePosition(viewed,id));
        double x=position[0],y=position[1];
        var parents=new ArrayList<Integer>();for(int p=0;p<SkillTree.COUNT;p++)if((n.parents()&(1L<<p))!=0)parents.add(p);
        list.add(new Node(id,x,y,parents.stream().mapToInt(Integer::intValue).toArray(),n.title()+" • Nv. "+n.level(),n.description(),n.icon(),id<21&&(step==3||step==6)));
    }nodes=List.copyOf(list);}
    private boolean own(){return state.rpgClass()==viewed.ordinal();}
    @Override protected boolean owned(int id){return own()&&state.hasSkill(id);}
    private int points(){return Math.max(0,state.level()/2-Long.bitCount(state.skills()));}
    @Override protected boolean available(int id){return own()&&id>=2&&!owned(id)&&(state.debug()||points()>0&&state.level()>=SkillTree.requiredLevel(id))&&((state.skills()|SkillTree.FREE_MASK)&SkillTree.parents(id))==SkillTree.parents(id);}
    @Override protected String budget(){return state.testing()?"TESTE • tudo liberado":state.debug()?"DEBUG":points()+" pontos • arraste / roda: zoom";}
    @Override protected String status(int id){return !own()?"Prévia de outra classe.":owned(id)?"Aprendido.":available(id)?"Disponível por 1 ponto.":"Requer conexão • Nv. "+SkillTree.requiredLevel(id)+" • 1 ponto";}
    @Override protected void purchase(int id){minecraft.player.connection.send(new RpgNetwork.Request(100+id));}
    public void update(RpgNetwork.State value){state=value;waiting=0;}
}
