package dev.emersonoxy.rpg.client;

import java.util.*;
import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;

/** Read-only inventory comparison. Nothing is equipped, spent or transferred by this screen. */
public final class ArsenalScreen extends Screen {
    private final Screen parent;private int left,top,w,h,page,selected=-1,ticks,rows;
    private final List<Integer> slots=new ArrayList<>();
    public ArsenalScreen(Screen parent){super(Component.literal("Arsenal • comparar armas"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    private boolean weapon(ItemStack s){return !s.isEmpty()&&(s.is(net.minecraft.tags.ItemTags.SWORDS)||s.is(net.minecraft.tags.ItemTags.AXES)||s.getItem() instanceof ProjectileWeaponItem||s.is(Items.TRIDENT)||s.is(Items.MACE)||ClassEquipment.gear(s)&&ClassEquipment.identity(s).getStringOr("part","").equals("weapon"));}
    @Override protected void init(){
        w=Math.min(480,width-16);h=Math.min(300,height-8);left=(width-w)/2;top=(height-h)/2;rows=Math.max(1,(h-94)/22);slots.clear();if(minecraft.player==null)return;
        for(int i=0;i<36;i++)if(weapon(minecraft.player.getInventory().getItem(i)))slots.add(i);
        page=Math.clamp(page,0,Math.max(0,(slots.size()-1)/rows));if(!slots.contains(selected))selected=slots.isEmpty()?-1:slots.getFirst();
        int listWidth=Math.max(90,w/3);
        for(int i=0;i<rows&&page*rows+i<slots.size();i++){int id=slots.get(page*rows+i);var stack=minecraft.player.getInventory().getItem(id);addRenderableWidget(RpgUi.button(left+12,top+44+i*22,listWidth,20,stack.getHoverName().getString(),"",stack.getItem(),id==selected?RpgUi.GOLD:0xFF61758D,button->{selected=id;rebuildWidgets();})).setTooltip(Tooltip.create(details(stack)));}
        addRenderableWidget(Button.builder(Component.literal("◀"),button->{page--;rebuildWidgets();}).bounds(left+12,top+h-52,28,18).build()).active=page>0;
        addRenderableWidget(Button.builder(Component.literal("▶"),button->{page++;rebuildWidgets();}).bounds(left+44,top+h-52,28,18).build()).active=(page+1)*rows<slots.size();
        addRenderableWidget(Button.builder(Component.literal("Achados de exploração"),b->minecraft.setScreen(new AdventureCatalogScreen(this))).bounds(left+80,top+h-52,w-92,18).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"),button->onClose()).bounds(left+12,top+h-28,w-24,20).build());
        minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void tick(){if(minecraft.player==null){minecraft.setScreen(null);return;}if(++ticks%40==0)rebuildWidgets();}
    private Component details(ItemStack stack){var text=Component.literal(stack.getHoverName().getString()+"\n"+restriction(stack));var lore=stack.get(DataComponents.LORE);if(lore!=null)for(var line:lore.lines())text.append("\n").append(line);return text;}
    private double stat(ItemStack stack,Holder<Attribute> attribute){
        return EquipmentComparison.stat(stack,EquipmentSlot.MAINHAND,attribute);
    }
    private void comparison(GuiGraphics g,String label,ItemStack chosen,ItemStack held,Holder<Attribute> attribute,int x,int y){double a=stat(chosen,attribute),b=stat(held,attribute),difference=a-b;String text=String.format(Locale.ROOT,"%s: %.2f → %.2f (%+.2f)",label,b,a,difference);g.drawString(font,font.plainSubstrByWidth(text,left+w-12-x),x,y,Math.abs(difference)<.001?0xFFE1EAF0:difference>0?0xFF53D5BF:0xFFFF8B8B);}
    private String restriction(ItemStack stack){
        return EquipmentComparison.restriction(stack);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        RpgUi.panel(g,left,top,w,h);g.drawCenteredString(font,title,width/2,top+12,RpgUi.GOLD);g.drawString(font,"Arsenal nível "+EquipmentAccess.clientProfessionLevel(2)+"/20",left+12,top+30,0xFFFFD36B);
        if(selected<0){RpgUi.section(g,left+12,top+52,w-24,h-116,RpgUi.GOLD);RpgUi.medallion(g,width/2,top+88,22,RpgUi.GOLD);g.renderFakeItem(new ItemStack(Items.IRON_SWORD),width/2-8,top+80);g.drawCenteredString(font,"Seu arsenal começa aqui",width/2,top+122,RpgUi.GOLD);g.drawCenteredString(font,"Coloque armas no inventário para comparar.",width/2,top+141,RpgUi.TEXT);g.drawCenteredString(font,"Selecione uma arma para ver as diferenças da equipada.",width/2,top+156,RpgUi.MUTED);super.render(g,mx,my,delta);return;}
        RpgUi.section(g,left+20+Math.max(90,w/3),top+40,w-32-Math.max(90,w/3),h-110,RpgUi.GOLD);
        var chosen=minecraft.player.getInventory().getItem(selected);var held=minecraft.player.getMainHandItem();int x=left+24+Math.max(90,w/3),available=left+w-12-x,y=top+44;
        g.renderFakeItem(chosen,x,y);g.drawString(font,font.plainSubstrByWidth(chosen.getHoverName().getString(),available-22),x+22,y+4,0xFFFFFFFF);y+=26;
        comparison(g,"Dano melee",chosen,held,Attributes.ATTACK_DAMAGE,x,y);comparison(g,"Ataques/s",chosen,held,Attributes.ATTACK_SPEED,x,y+13);comparison(g,"Alcance",chosen,held,Attributes.ENTITY_INTERACTION_RANGE,x,y+26);y+=44;
        for(var line:font.split(Component.literal(restriction(chosen)),available)){g.drawString(font,line,x,y,EquipmentAccess.allowed(minecraft.player,chosen)?0xFF53D5BF:0xFFFF8B8B);y+=10;}y+=5;
        String role=ClassRules.ranged(chosen)?"Projéteis: dano depende da carga e dos encantamentos.":stat(chosen,Attributes.ATTACK_SPEED)>=2.4?"Perfil: ataques rápidos.":"Perfil: golpes com maior intervalo.";
        for(var line:font.split(Component.literal(role),available)){if(y>top+h-68)break;g.drawString(font,line,x,y,0xFFB7C8D7);y+=10;}
        y+=5;var resource=OxyRpgClient.resources();var c=EquipmentAccess.clientSelected();String bonuses="Personagem ativo: atributos +"+(resource==null?0:resource.damage()*2)+"% dano; Arsenal "+(EquipmentAccess.clientProfessionLevel(2)==20?"+5% melee":"sem passivo máximo")+"; kit etapa "+ClassEquipment.active(minecraft.player,c)+".";
        for(var line:font.split(Component.literal(bonuses),available)){if(y>top+h-68)break;g.drawString(font,line,x,y,0xFFFFD36B);y+=10;}
        var lore=chosen.get(DataComponents.LORE);if(lore!=null)for(var component:lore.lines().stream().skip(2).toList())for(var line:font.split(component,available)){if(y>top+h-68)break;g.drawString(font,line,x,y,0xFFB7C8D7);y+=10;}
        g.drawString(font,font.plainSubstrByWidth("Equipada → selecionada. Valores base; efeitos situacionais variam.",w-24),left+12,top+h-63,0xFF909BAA);super.render(g,mx,my,delta);
    }
}
