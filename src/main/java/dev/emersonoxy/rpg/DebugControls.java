package dev.emersonoxy.rpg;

import java.io.IOException;
import java.util.function.Consumer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Operator-only controls. All progression overrides live exclusively in the debug session. */
public final class DebugControls {
    private DebugControls(){}
    public static LiteralArgumentBuilder<CommandSourceStack> commands(){
        var root=Commands.literal("debug").requires(Commands.hasPermission(2)).executes(c->status(c.getSource().getPlayerOrException()));
        root.then(Commands.literal("on").executes(c->OxyRpg.debug(c.getSource().getPlayerOrException(),1)));
        root.then(Commands.literal("off").executes(c->OxyRpg.debug(c.getSource().getPlayerOrException(),0)));
        root.then(Commands.literal("limpar").executes(c->OxyRpg.debug(c.getSource().getPlayerOrException(),2)));
        root.then(Commands.literal("skills")
            .then(Commands.literal("todas").executes(c->modify(c.getSource().getPlayerOrException(),RpgDebug::allSkills,true)))
            .then(Commands.literal("limpar").executes(c->modify(c.getSource().getPlayerOrException(),RpgDebug::clearSkills,true))));
        root.then(Commands.literal("nivel").then(Commands.argument("valor",IntegerArgumentType.integer(1,100))
            .executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.level(p,IntegerArgumentType.getInteger(c,"valor")),true)))
            .then(Commands.literal("subir").then(Commands.argument("quantidade",IntegerArgumentType.integer(1,99)).executes(c->changeLevel(c.getSource().getPlayerOrException(),IntegerArgumentType.getInteger(c,"quantidade")))))
            .then(Commands.literal("descer").then(Commands.argument("quantidade",IntegerArgumentType.integer(1,99)).executes(c->changeLevel(c.getSource().getPlayerOrException(),-IntegerArgumentType.getInteger(c,"quantidade"))))));
        var classes=Commands.literal("classe");for(var type:RpgClass.values())if(type!=RpgClass.NONE)
            classes.then(Commands.literal(type.name().toLowerCase(java.util.Locale.ROOT)).executes(c->choose(c.getSource().getPlayerOrException(),type)));
        root.then(classes);
        root.then(Commands.literal("cooldowns")
            .then(Commands.literal("on").executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.cooldowns(p,true),false)))
            .then(Commands.literal("off").executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.cooldowns(p,false),false)))
            .then(Commands.literal("limpar").executes(c->modify(c.getSource().getPlayerOrException(),p->{RpgDebug.resetCooldowns(p);RpgEvasion.refill(p);},false))));
        root.then(Commands.literal("recursos")
            .then(Commands.literal("ilimitados").executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.resources(p,true),false)))
            .then(Commands.literal("normais").executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.resources(p,false),false))));
        root.then(Commands.literal("atributos")
            .then(Commands.literal("maximos").executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.maxAttributes(p,true),true)))
            .then(Commands.literal("normais").executes(c->modify(c.getSource().getPlayerOrException(),p->RpgDebug.maxAttributes(p,false),true))));
        root.then(Commands.literal("arsenal").executes(c->arsenal(c.getSource().getPlayerOrException(),true))
            .then(Commands.literal("on").executes(c->arsenal(c.getSource().getPlayerOrException(),true)))
            .then(Commands.literal("off").executes(c->arsenal(c.getSource().getPlayerOrException(),false))));
        root.then(Commands.literal("tudo").executes(c->modify(c.getSource().getPlayerOrException(),p->{RpgDebug.allSkills(p);RpgDebug.level(p,100);RpgDebug.maxAttributes(p,true);RpgDebug.arsenal(p,true);},true)));
        root.then(Commands.literal("recuperar").executes(c->{var p=c.getSource().getPlayerOrException();int result=modify(p,x->{},false);if(result>0){p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.getFoodData().setSaturation(20);RpgResources.restoreMana(p,Integer.MAX_VALUE);RpgEvasion.refill(p);}return result;}));
        return root;
    }
    private static int changeLevel(ServerPlayer p,int amount){return modify(p,x->RpgDebug.level(x,RpgAbilities.profile(x).level()+amount),true);}
    public static int choose(ServerPlayer p,RpgClass type){return modify(p,x->RpgDebug.choose(x,type),true);}
    private static int modify(ServerPlayer p,Consumer<ServerPlayer> action,boolean clear){
        if(!RpgDebug.debugging(p)){p.sendSystemMessage(Component.literal("[Oxy RPG] Ative /rpg debug on primeiro. /rpg teste mantém custos normais."));return 0;}
        try{new ProfileStore(p).load(p.getUUID());action.accept(p);if(clear)RpgAbilities.clear(p);OxyRpg.sync(p,"");Professions.refreshModifiers(p);return status(p);}
        catch(IOException|IllegalStateException e){p.sendSystemMessage(Component.literal("[Oxy RPG] "+e.getMessage()));return 0;}
    }
    private static int arsenal(ServerPlayer p,boolean on){RpgDebug.arsenal(p,on);OxyRpg.sync(p,"");Professions.refreshModifiers(p);p.sendSystemMessage(Component.literal("[Oxy RPG] Arsenal temporário: "+(on?"nível 20":"nível salvo")+"."));return 1;}
    private static int status(ServerPlayer p){
        OxyRpg.sync(p,"");String mode=RpgDebug.testing(p)?"TESTE":RpgDebug.debugging(p)?"DEBUG ON":"DEBUG OFF";
        p.sendSystemMessage(Component.literal("[Oxy RPG] "+mode+" | Nível "+RpgAbilities.profile(p).level()+" | Recargas "+(RpgDebug.cooldownsDisabled(p)?"OFF":"ON")+" | Recursos "+(RpgDebug.unlimited(p)?"ilimitados":"normais")+". Painel: R > Debug."));
        p.sendSystemMessage(Component.literal("/rpg debug skills todas | nivel 1–100 | nivel subir/descer N | classe <nome> | cooldowns on/off/limpar | recursos normais/ilimitados | atributos maximos/normais | tudo | recuperar | off"));return 1;
    }
}
