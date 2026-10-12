package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.function.Consumer;

/** One consistent, scrollable contract/conversation panel alongside native commerce. */
public final class VillagerPanel {
    private static final java.util.List<OfferRow> ROWS=new java.util.ArrayList<>();
    private static Button contract,conversation;
    private static boolean talking;
    private static int topic,selectedTopic,scroll,maxScroll;
    private VillagerPanel() {}
    public static int height() { return Math.max(150,Math.min(240,Minecraft.getInstance().getWindow().getGuiScaledHeight()-12)); }
    public static void scroll(double delta) { scroll=Math.clamp(scroll-(int)(delta*14),0,maxScroll); }
    public static void init(int x,int y,int w,Consumer<Button> add) {
        talking=false;scroll=0;ROWS.clear();var data=VillagerClient.data();if(data==null)return;
        var offers=com.google.gson.JsonParser.parseString(data.offers()).getAsJsonArray();int row=0;
        for(var e:offers) {
            var q=e.getAsJsonObject();int id=q.get("id").getAsInt();String title=q.get("title").getAsString();Item icon=Items.PAPER;
            if(q.has("target")&&q.has("kind"))icon=q.get("kind").getAsInt()==0?Items.IRON_SWORD:BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(q.get("target").getAsString()));
            if(icon==null||icon==Items.AIR)icon=Items.PAPER;
            var button=new OfferRow(x+12,y+31+row++*24,w-24,id,title,icon);ROWS.add(button);add.accept(button);
        }
        int bw=(w-28)/2;
        contract=Button.builder(Component.literal("Aceitar"),b->{
            if(talking){talking=false;scroll=0;return;}
            var d=VillagerClient.data();if(d!=null&&(d.status()==0||d.status()==1||d.status()==2))VillagerClient.choose(d.status()==0?300:301,d.selectedQuest());
        }).bounds(x+12,y+height()-30,bw,20).build();add.accept(contract);
        conversation=Button.builder(Component.literal("Conversar"),b->{talking=true;scroll=0;selectedTopic=topic++%3;VillagerClient.choose(308,selectedTopic);}).bounds(x+16+bw,y+height()-30,bw,20).build();add.accept(conversation);
    }
    private static int text(GuiGraphics g,String s,int x,int y,int width,int color) {
        var font=Minecraft.getInstance().font;for(var line:font.split(Component.literal(s),Math.max(1,width))){g.drawString(font,line,x,y,color);y+=11;}return y;
    }
    public static void render(GuiGraphics g,int x,int y,int w) {
        var d=VillagerClient.data();if(d==null)return;var mc=Minecraft.getInstance();var font=mc.font;int h=height();
        if(!(mc.screen instanceof MerchantScreen))RpgUi.panel(g,x,y,w,h);
        g.drawString(font,talking?"CONVERSA":"MISSÕES",x+12,y+12,RpgUi.GOLD);
        for(var row:ROWS){row.visible=!talking;row.active=d.status()==0||d.status()==4;}
        if(contract!=null){contract.active=talking||d.status()==0||d.status()==1||d.status()==2;contract.setMessage(Component.literal(talking?"Missões":d.status()==0?"Aceitar":d.status()==1?"Entregar":d.status()==2?"Receber":"Indisponível"));}
        if(conversation!=null)conversation.setMessage(Component.literal(talking?"Outro assunto":"Conversar"));
        int body=talking?y+35:y+36+ROWS.size()*24;
        g.fill(x+12,body-3,x+w-12,y+h-39,0xFF151D28);
        g.enableScissor(x+14,body,x+w-14,y+h-40);int line=body+4-scroll;
        if(talking) {
            line=text(g,"Você",x+18,line,w-36,RpgUi.MUTED);
            line=text(g,new String[]{"Como é seu trabalho?","Como posso ajudar?","Tem algum conselho?"}[selectedTopic],x+18,line+3,w-36,RpgUi.TEXT);
            line=text(g,d.name(),x+18,line+10,w-36,RpgUi.GOLD);
            line=text(g,d.npcDialogue(),x+18,line+3,w-36,RpgUi.TEXT);
        } else {
            line=text(g,d.objective(),x+18,line,w-36,RpgUi.TEXT);
            if(!d.reward().isBlank())line=text(g,d.reward().split(";",2)[0],x+18,line+7,w-36,0xFF99D6B5);
            String status=switch(d.status()){case 0->"Disponível";case 1->"Em andamento";case 2->"Concluído · recolha sua recompensa";case 3->"Conclua seu contrato ativo no Diário.";case 4->"Contrato concluído";default->"Converse para conhecer este ofício.";};
            line=text(g,status,x+18,line+8,w-36,RpgUi.MUTED);
        }
        maxScroll=Math.max(0,line+scroll-(y+h-40));scroll=Math.min(scroll,maxScroll);g.disableScissor();
        if(maxScroll>0){int track=y+h-40-body;g.fill(x+w-9,body,x+w-7,body+track,0xFF364254);int thumb=Math.max(8,track*track/(track+maxScroll));int at=body+(int)((track-thumb)*scroll/(float)maxScroll);g.fill(x+w-9,at,x+w-7,at+thumb,RpgUi.GOLD);}
    }
    private static final class OfferRow extends Button {
        private final int id;private final ItemStack icon;
        OfferRow(int x,int y,int w,int id,String title,Item item){super(x,y,w,22,Component.literal(title),b->{talking=false;scroll=0;VillagerClient.choose(307,id);},DEFAULT_NARRATION);this.id=id;icon=new ItemStack(item);setTooltip(Tooltip.create(Component.literal(title)));}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
            var d=VillagerClient.data();boolean selected=d!=null&&d.selectedQuest()==id;int x=getX(),y=getY(),w=getWidth();var font=Minecraft.getInstance().font;
            g.fill(x,y,x+w,y+22,selected?0xFF2D3540:isHoveredOrFocused()?0xFF253143:0xFF1B2431);
            g.fill(x,y,x+2,y+22,selected?RpgUi.GOLD:0xFF46566C);g.renderFakeItem(icon,x+7,y+3);
            g.drawString(font,font.plainSubstrByWidth(getMessage().getString(),w-34),x+29,y+7,!active?RpgUi.MUTED:selected?RpgUi.GOLD:RpgUi.TEXT);
        }
    }
}
