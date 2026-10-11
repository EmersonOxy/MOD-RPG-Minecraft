package dev.emersonoxy.rpg.client;

import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

/** Two tooltips while the configured comparison key is held, without changing inventory input. */
public final class EquipmentTooltip {
    private static boolean drawing,scanDown;
    private static Screen scanScreen;
    private EquipmentTooltip(){}
    public static void pressed(ScreenEvent.KeyPressed.Pre event){if(OxyRpgClient.COMPARE_EQUIPMENT.matches(event.getKeyEvent())){scanDown=true;scanScreen=event.getScreen();}}
    public static void released(ScreenEvent.KeyReleased.Pre event){if(OxyRpgClient.COMPARE_EQUIPMENT.matches(event.getKeyEvent()))scanDown=false;}
    private static boolean held(){
        var mc=Minecraft.getInstance();var binding=OxyRpgClient.COMPARE_EQUIPMENT;var key=binding.getKey();
        if(mc.screen==null||!binding.getKeyConflictContext().isActive()||key.getValue()<0)return false;
        // A modifier used as the primary key (e.g. Shift) must not reject itself via NONE.isActive(GUI).
        if(binding.getKeyModifier()!=net.neoforged.neoforge.client.settings.KeyModifier.NONE
            &&!binding.getKeyModifier().isActive(binding.getKeyConflictContext()))return false;
        return switch(key.getType()){
            case KEYSYM->InputConstants.isKeyDown(mc.getWindow(),key.getValue());
            case MOUSE->GLFW.glfwGetMouseButton(mc.getWindow().handle(),key.getValue())==GLFW.GLFW_PRESS;
            case SCANCODE->scanDown&&scanScreen==mc.screen;
        };
    }
    public static void gather(RenderTooltipEvent.GatherComponents event){
        var wound=dev.emersonoxy.rpg.RpgDataMaps.weapon(event.getItemStack());
        if(!drawing&&wound.bleedChance()>0)event.getTooltipElements().add(Either.left(Component.translatable("tooltip.oxyrpg.bleeding_weapon",Math.round(wound.bleedChance()*100),String.format(Locale.ROOT,"%.1f",.5f*(wound.bleedSeverity()+1))).withStyle(ChatFormatting.RED)));
        var mc=Minecraft.getInstance();if(drawing||mc.player==null||mc.screen==null||EquipmentComparison.slot(event.getItemStack())==null||held()||OxyRpgClient.COMPARE_EQUIPMENT.getKey().getValue()<0)return;
        event.getTooltipElements().add(Either.left(Component.translatable("tooltip.oxyrpg.compare_hint",OxyRpgClient.COMPARE_EQUIPMENT.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW)));
    }
    private static void row(List<Component> lines,String label,ItemStack stack,ItemStack equipped,EquipmentSlot slot,Holder<Attribute> attribute,boolean differences){
        double value=EquipmentComparison.stat(stack,slot,attribute),delta=value-EquipmentComparison.stat(equipped,slot,attribute);
        var text=Component.literal(String.format(Locale.ROOT,"%s: %.1f",label,value));
        if(differences)text.append(Component.literal(String.format(Locale.ROOT,"  %+.1f",delta)).withStyle(Math.abs(delta)<.001?ChatFormatting.GRAY:delta>0?ChatFormatting.GREEN:ChatFormatting.RED));lines.add(text);
    }
    private static List<ClientTooltipComponent> content(Font font,ItemStack stack,ItemStack equipped,EquipmentSlot slot,boolean chosen,int width,int maxLines){
        var lines=new ArrayList<Component>();lines.add(Component.translatable(chosen?"tooltip.oxyrpg.hovered":"tooltip.oxyrpg.equipped").withStyle(chosen?ChatFormatting.AQUA:ChatFormatting.GOLD,ChatFormatting.BOLD));
        lines.add(stack.isEmpty()?Component.literal("Sem item equipado").withStyle(ChatFormatting.GRAY):Component.literal(font.plainSubstrByWidth(stack.getHoverName().getString(),width)).setStyle(stack.getHoverName().getStyle()));
        if(slot==EquipmentSlot.MAINHAND){row(lines,"Dano",stack,equipped,slot,Attributes.ATTACK_DAMAGE,chosen);row(lines,"Ataques/s",stack,equipped,slot,Attributes.ATTACK_SPEED,chosen);row(lines,"Alcance",stack,equipped,slot,Attributes.ENTITY_INTERACTION_RANGE,chosen);if(dev.emersonoxy.rpg.ClassRules.ranged(stack))lines.add(Component.literal("Dano de projétil varia com carga e encantamentos.").withStyle(ChatFormatting.GRAY));}
        else if(slot!=EquipmentSlot.OFFHAND){row(lines,"Armadura",stack,equipped,slot,Attributes.ARMOR,chosen);row(lines,"Robustez",stack,equipped,slot,Attributes.ARMOR_TOUGHNESS,chosen);row(lines,"Antiempurrão",stack,equipped,slot,Attributes.KNOCKBACK_RESISTANCE,chosen);}
        if(stack.isDamageableItem())lines.add(Component.literal("Durabilidade: "+(stack.getMaxDamage()-stack.getDamageValue())+"/"+stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        if(!stack.isEmpty())lines.add(Component.literal(EquipmentComparison.restriction(stack)).withStyle(dev.emersonoxy.rpg.EquipmentAccess.allowed(Minecraft.getInstance().player,stack)?ChatFormatting.GREEN:ChatFormatting.RED));
        if(chosen)lines.add(Component.literal("Verde: ganho • Vermelho: perda").withStyle(ChatFormatting.GRAY));
        var enchantments=stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        if(!enchantments.isEmpty()){lines.add(Component.literal("Encantamentos").withStyle(ChatFormatting.LIGHT_PURPLE));for(var entry:enchantments.entrySet())lines.add(net.minecraft.world.item.enchantment.Enchantment.getFullname(entry.getKey(),entry.getIntValue()));}
        lines.add(Component.literal("Solte a tecla para detalhes completos.").withStyle(ChatFormatting.GRAY));
        var result=new ArrayList<ClientTooltipComponent>();
        for(var line:lines)for(var wrapped:font.split(line,width)){if(result.size()>=maxLines-1){result.add(ClientTooltipComponent.create(Component.literal(font.plainSubstrByWidth("… Solte a tecla para ver todos os detalhes.",width)).withStyle(ChatFormatting.GRAY).getVisualOrderText()));return result;}result.add(ClientTooltipComponent.create(wrapped));}
        return result;
    }
    private static int width(Font font,List<ClientTooltipComponent> lines){return lines.stream().mapToInt(line->line.getWidth(font)).max().orElse(1);}
    private static int height(Font font,List<ClientTooltipComponent> lines){return 2+lines.stream().mapToInt(line->line.getHeight(font)).sum();}
    public static void render(RenderTooltipEvent.Pre event){
        var mc=Minecraft.getInstance();if(drawing||event.isCanceled()||mc.player==null||mc.screen==null||!held())return;
        var hovered=event.getItemStack();var slot=EquipmentComparison.slot(hovered);if(slot==null)return;var equipped=mc.player.getItemBySlot(slot);
        int column=Math.min(240,Math.max(40,(event.getScreenWidth()-40)/2)),maxLines=Math.max(3,(event.getScreenHeight()-24)/10);var font=event.getFont();
        var left=content(font,equipped,equipped,slot,false,column,maxLines);var right=content(font,hovered,equipped,slot,true,column,maxLines);
        int lw=Math.max(width(font,left),width(font,right)),rw=lw,total=lw+rw+24,h=Math.max(height(font,left),height(font,right));
        int x=event.getX()+12;if(x+total>event.getScreenWidth()-6)x=event.getX()-total-12;x=Math.clamp(x,6,Math.max(6,event.getScreenWidth()-total-6));
        int y=Math.clamp(event.getY()-12,8,Math.max(8,event.getScreenHeight()-h-8));final int fx=x,fy=y,rx=x+lw+24;
        event.setCanceled(true);drawing=true;
        try{var g=event.getGraphics();g.renderTooltip(font,left,fx,fy,(sw,sh,mx,my,tw,th)->new Vector2i(fx,fy),null);g.renderTooltip(font,right,rx,fy,(sw,sh,mx,my,tw,th)->new Vector2i(rx,fy),hovered.get(net.minecraft.core.component.DataComponents.TOOLTIP_STYLE));}finally{drawing=false;}
    }
}
