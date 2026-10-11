package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.*;
import java.util.*;
import net.minecraft.client.gui.screens.Screen;

public final class AttributesScreen extends GraphScreen {
    private RpgNetwork.Resources state;
    private BitSet unlocked=new BitSet();
    public AttributesScreen(Screen parent,RpgNetwork.Resources state){super(parent,"Árvore de atributos");update(state);}
    @Override protected void loadNodes(){var list=new ArrayList<Node>();for(int id=0;id<AttributeTree.COUNT;id++){int type=AttributeTree.type(minecraft.player.getUUID(),id);list.add(new Node(id,AttributeTree.x(id),AttributeTree.y(id),AttributeTree.parents(id),AttributeStats.label(type),AttributeStats.benefit(type)+".",new String[]{"apple","iron_sword","emerald","iron_chestplate","lapis_lazuli"}[type],false));}nodes=List.copyOf(list);}
    @Override protected boolean owned(int id){return unlocked.get(id);}
    @Override protected boolean available(int id){return state!=null&&state.points()>0&&AttributeTree.available(unlocked,id);}
    @Override protected String budget(){return state==null?"Carregando…":state.points()+" pontos • arraste / roda: zoom";}
    @Override protected String status(int id){if(state==null)return "Carregando…";int type=AttributeTree.type(minecraft.player.getUUID(),id);return (owned(id)?"Aprendido":available(id)?"Disponível por 1 ponto":"Requer conexão e 1 ponto")+" • "+AttributeStats.label(type)+" total: "+state.value(type);}
    @Override protected void purchase(int id){minecraft.player.connection.send(new RpgNetwork.Request(1000+id));}
    public void update(RpgNetwork.Resources value){state=value;unlocked=value==null?new BitSet():AttributeTree.decode(value.nodes());waiting=0;}
}
