package dev.emersonoxy.rpg.client;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import java.util.function.Consumer;
/** The same quest/dialogue panel accompanies native trades and residents without offers. */
public final class VillagerPanel {
    private static final java.util.List<Button> ROWS=new java.util.ArrayList<>();private static Button contract;
    private static boolean talking;private static int topic,selectedTopic,scroll,maxScroll;
    public static void scroll(double delta){scroll=Math.clamp(scroll-(int)(delta*14),0,maxScroll);}
    private VillagerPanel(){}
    private static int height(){return Math.max(150,Math.min(220,Minecraft.getInstance().getWindow().getGuiScaledHeight()-8));}
    public static void init(int x,int y,int w,Consumer<Button> add){
        talking=false;scroll=0;ROWS.clear();var data=VillagerClient.data();if(data==null)return;
        var offers=com.google.gson.JsonParser.parseString(data.offers()).getAsJsonArray();int row=0;
        for(var e:offers){var item=e.getAsJsonObject();int id=item.get("id").getAsInt();String title=item.get("title").getAsString();
            var button=RpgUi.button(x+10,y+29+row++*19,w-20,18,title,"",Items.PAPER,RpgUi.GOLD,b->{talking=false;scroll=0;VillagerClient.choose(307,id);});ROWS.add(button);add.accept(button);}
        add.accept(contract=RpgUi.button(x+10,y+height()-29,(w-24)/2,20,"Contrato","",Items.EMERALD,RpgUi.GOLD,b->{var d=VillagerClient.data();if(d!=null&&(d.status()==0||d.status()==1||d.status()==2))VillagerClient.choose(d.status()==0?300:301,d.selectedQuest());}));
        add.accept(RpgUi.button(x+14+(w-24)/2,y+height()-29,(w-24)/2,20,"Diálogo","",Items.WRITABLE_BOOK,RpgUi.GOLD,b->{talking=true;scroll=0;selectedTopic=topic++%3;VillagerClient.choose(308,selectedTopic);}));
    }
    private static int text(GuiGraphics g,String s,int x,int y,int width,int color){var font=Minecraft.getInstance().font;for(var line:font.split(Component.literal(s),width)){g.drawString(font,line,x,y,color);y+=11;}return y;}
    public static void render(GuiGraphics g,int x,int y,int w){var d=VillagerClient.data();if(d==null)return;
        for(var row:ROWS)row.active=d.status()==0||d.status()==4;
        if(contract!=null){contract.active=d.status()==0||d.status()==1||d.status()==2;contract.setMessage(Component.literal(d.status()==0?"Aceitar":d.status()==1?"Entregar / verificar":d.status()==2?"Receber":"Sem contrato"));}
        RpgUi.panel(g,x,y,w,height());var font=Minecraft.getInstance().font;g.drawString(font,"MISSÕES E CONVERSA",x+10,y+12,RpgUi.GOLD);
        g.enableScissor(x+8,y+91,x+w-8,y+height()-37);int line=y+93-scroll;
        if(talking){line=text(g,new String[]{"Você: Como é seu trabalho?","Você: Como posso ajudar?","Você: Tem algum conselho?"}[selectedTopic],x+12,line,w-24,RpgUi.MUTED);line=text(g,d.name()+": "+d.npcDialogue(),x+12,line+8,w-24,0xFFE3E8EF);}
        else {line=text(g,d.title(),x+12,line,w-24,RpgUi.GOLD);line=text(g,d.objective(),x+12,line+6,w-24,0xFFE3E8EF);line=text(g,d.reward(),x+12,line+6,w-24,0xFF99D6B5);line=text(g,d.status()==0?"Contrato: aceitar":d.status()==1?"Contrato: entregar / verificar":d.status()==2?"Contrato: receber recompensa":d.dialogue(),x+12,line+8,w-24,RpgUi.MUTED);}
        maxScroll=Math.max(0,line+scroll-(y+height()-40));g.disableScissor();
    }
}
