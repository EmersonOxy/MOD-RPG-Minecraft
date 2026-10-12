package dev.emersonoxy.rpg;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@Mod(OxyRpg.MOD_ID)
public final class OxyRpg {
    public static final String MOD_ID = "oxyrpg";
    private static final Logger LOGGER = LoggerFactory.getLogger(OxyRpg.class);

    public OxyRpg(net.neoforged.bus.api.IEventBus bus,net.neoforged.fml.ModContainer container) {
        NeoForge.EVENT_BUS.addListener(ComplementCompat::join);
        NeoForge.EVENT_BUS.addListener(AbilityAim::logout);NeoForge.EVENT_BUS.addListener(AbilityAim::stop);VillageGuards.register(bus);RpgConfig.register(container);PlayerAttachments.register(bus);RpgDataMaps.register(bus);RpgItems.register(bus);RpgParticles.register(bus);Bleeding.register(bus);RpgGameTests.functions(bus);
        bus.addListener(RpgDataGenerator::gather);bus.addListener(RpgGameTests::register);
        NeoForge.EVENT_BUS.addListener(RpgContent::listeners);NeoForge.EVENT_BUS.addListener(RpgContent::sync);
        bus.addListener(RpgContent::configReloaded);
        bus.addListener(RpgNetwork::register);
        VillageLoot.register(bus);
        NeoForge.EVENT_BUS.addListener(EntityMotions::join);NeoForge.EVENT_BUS.addListener(EntityMotions::tracking);NeoForge.EVENT_BUS.addListener(EntityMotions::attack);NeoForge.EVENT_BUS.addListener(RpgVampire::join);NeoForge.EVENT_BUS.addListener(RpgVampire::tick);NeoForge.EVENT_BUS.addListener(VampireHunt::tick);NeoForge.EVENT_BUS.addListener(RpgVampire::damage);NeoForge.EVENT_BUS.addListener(RpgVampire::hit);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH,RpgVampire::incoming);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,RpgVampire::shield);NeoForge.EVENT_BUS.addListener(RpgVampire::applicable);NeoForge.EVENT_BUS.addListener(RpgVampire::useItem);
        NeoForge.EVENT_BUS.addListener(RpgVampire::attack);NeoForge.EVENT_BUS.addListener(RpgVampire::rightItem);NeoForge.EVENT_BUS.addListener(RpgVampire::rightBlock);NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,RpgVampire::interact);NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,RpgVampire::specific);NeoForge.EVENT_BUS.addListener(RpgVampire::breaking);
        NeoForge.EVENT_BUS.addListener(RpgVampire::tracking);NeoForge.EVENT_BUS.addListener(RpgVampire::logout);NeoForge.EVENT_BUS.addListener(RpgVampire::stop);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGH,Bleeding::interact);NeoForge.EVENT_BUS.addListener(Bleeding::hit);NeoForge.EVENT_BUS.addListener(Bleeding::removed);
        NeoForge.EVENT_BUS.addListener(AdventureItems::fall);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,CombatTactics::join);
        NeoForge.EVENT_BUS.addListener(RaiderPatrols::tick);NeoForge.EVENT_BUS.addListener(RaiderPatrols::cleanup);NeoForge.EVENT_BUS.addListener(RaiderPatrols::drops);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,RpgEvasion::incoming);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW,CombatChances::incoming);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,ShieldParry::block);
        NeoForge.EVENT_BUS.addListener(RpgEvasion::tick);
        NeoForge.EVENT_BUS.addListener(RpgEvasion::clonePlayer);
        NeoForge.EVENT_BUS.addListener(RpgEvasion::logout);
        NeoForge.EVENT_BUS.addListener(RpgEvasion::stop);
        NeoForge.EVENT_BUS.addListener(ShieldParry::logout);
        NeoForge.EVENT_BUS.addListener(ShieldParry::stop);
        NeoForge.EVENT_BUS.addListener(CombatChances::stop);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,CombatFeedback::critical);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,CombatFeedback::damage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,CombatParticles::damage);
        NeoForge.EVENT_BUS.addListener(CombatParticles::explosion);
        NeoForge.EVENT_BUS.addListener(CombatParticles::stop);
        NeoForge.EVENT_BUS.addListener(CombatFeedback::logout);
        NeoForge.EVENT_BUS.addListener(CombatFeedback::stop);
        NeoForge.EVENT_BUS.addListener(OxyRpg::registerCommands);
        NeoForge.EVENT_BUS.addListener(RpgDataMaps::resistance);
        NeoForge.EVENT_BUS.addListener(ClassRules::damage);
        NeoForge.EVENT_BUS.addListener(ClassRules::use);
        NeoForge.EVENT_BUS.addListener(ClassRules::rightClick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,Professions::attack);
        NeoForge.EVENT_BUS.addListener(Professions::rightBlock);
        NeoForge.EVENT_BUS.addListener(Professions::leftBlock);
        NeoForge.EVENT_BUS.addListener(Professions::tick);
        NeoForge.EVENT_BUS.addListener(Professions::login);
        NeoForge.EVENT_BUS.addListener(Professions::stop);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH,Professions::breakGuard);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,Professions::broken);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,Professions::placed);
        NeoForge.EVENT_BUS.addListener(VillagerQuests::interact);
        NeoForge.EVENT_BUS.addListener(VillagerQuests::tracking);
        NeoForge.EVENT_BUS.addListener(VillagerQuests::logout);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,VillagerQuests::death);
        NeoForge.EVENT_BUS.addListener(EliteMobs::damage);
        NeoForge.EVENT_BUS.addListener(EliteAffixes::damage);
        NeoForge.EVENT_BUS.addListener(EliteAffixes::drain);
        NeoForge.EVENT_BUS.addListener(EliteMobs::experience);
        NeoForge.EVENT_BUS.addListener(EliteAbilities::tick);
        NeoForge.EVENT_BUS.addListener(EliteAbilities::projectile);
        NeoForge.EVENT_BUS.addListener(EliteAbilities::arrowEffect);
        NeoForge.EVENT_BUS.addListener(AdvancedClasses::tick);NeoForge.EVENT_BUS.addListener(AdvancedClasses::join);NeoForge.EVENT_BUS.addListener(AdvancedClasses::impact);NeoForge.EVENT_BUS.addListener(AdvancedClasses::hit);NeoForge.EVENT_BUS.addListener(AdvancedClasses::damage);NeoForge.EVENT_BUS.addListener(AdvancedClasses::stop);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::tick);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::projectile);
        NeoForge.EVENT_BUS.addListener(FluidPockets::tick);
        NeoForge.EVENT_BUS.addListener(FluidPockets::stop);
        NeoForge.EVENT_BUS.addListener(FluidPockets::placed);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::fall);NeoForge.EVENT_BUS.addListener(RpgSpecialists::incoming);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::damage);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::damagePost);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::targeting);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::tracking);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::attack);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::rightItem);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::rightBlock);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::interact);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::interactSpecific);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::breakBlock);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::join);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::drops);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::experience);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::logout);
        NeoForge.EVENT_BUS.addListener(RpgSpecialists::stop);
        NeoForge.EVENT_BUS.addListener(ClassRelics::drops);
        NeoForge.EVENT_BUS.addListener(ClassRelics::clonePlayer);
        NeoForge.EVENT_BUS.addListener(ClassRelics::tick);
        NeoForge.EVENT_BUS.addListener(ClassRelics::damage);
        NeoForge.EVENT_BUS.addListener(ClassRelics::attack);
        NeoForge.EVENT_BUS.addListener(ClassEquipment::tick);
        NeoForge.EVENT_BUS.addListener(ClassEquipment::damage);
        NeoForge.EVENT_BUS.addListener(ClassEquipment::logout);
        NeoForge.EVENT_BUS.addListener(ClassEquipment::stop);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, OxyRpg::onEnemyDeath);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::onTick);
        NeoForge.EVENT_BUS.addListener(RpgAviary::tick);
        NeoForge.EVENT_BUS.addListener(RpgAviary::fall);
        NeoForge.EVENT_BUS.addListener(RpgAviary::damage);
        NeoForge.EVENT_BUS.addListener(RpgAviary::stop);NeoForge.EVENT_BUS.addListener(AviaryCarry::tick);NeoForge.EVENT_BUS.addListener(AviaryCarry::join);NeoForge.EVENT_BUS.addListener(AviaryCarry::tracking);NeoForge.EVENT_BUS.addListener(AviaryCarry::logout);NeoForge.EVENT_BUS.addListener(AviaryCarry::stop);
        NeoForge.EVENT_BUS.addListener(MeteorCrater::tick);
        NeoForge.EVENT_BUS.addListener(MeteorCrater::stop);
        NeoForge.EVENT_BUS.addListener(MobLevels::damage);
        NeoForge.EVENT_BUS.addListener(MobLoot::drops);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::damagePre);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::damagePost);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::incoming);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::targeting);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::tracking);
        NeoForge.EVENT_BUS.addListener(MobLevels::tracking);
        NeoForge.EVENT_BUS.addListener(RpgResources::damage);
        NeoForge.EVENT_BUS.addListener(RpgResources::clonePlayer);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e)->{if(e.getEntity() instanceof ServerPlayer p&&!e.isEndConquered()){RpgResources.update(p,RpgAbilities.profile(p));p.setHealth(p.getMaxHealth());}});
        NeoForge.EVENT_BUS.addListener(RpgResources::logout);
        NeoForge.EVENT_BUS.addListener(RpgResources::stop);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::join);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::tick);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::targeting);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::damage);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::attack);
        NeoForge.EVENT_BUS.addListener(RpgShaman::attack);
        NeoForge.EVENT_BUS.addListener(RpgShaman::tick);
        NeoForge.EVENT_BUS.addListener(RpgShaman::logout);
        NeoForge.EVENT_BUS.addListener(RpgShaman::stop);
        NeoForge.EVENT_BUS.addListener(TemporaryBlocks::tick);
        NeoForge.EVENT_BUS.addListener(TemporaryBlocks::stop);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,TemporaryBlocks::broken);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,TemporaryBlocks::placed);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::incoming);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::drops);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::experience);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::logout);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::tracking);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,RpgNecromancer::death);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::stop);
        NeoForge.EVENT_BUS.addListener(RpgNecromancer::impact);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MobLevels::join);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::logout);
        NeoForge.EVENT_BUS.addListener(RpgAbilities::stop);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RpgAbilities::onImpact);
        NeoForge.EVENT_BUS.addListener(RpgDebug::onLogin);NeoForge.EVENT_BUS.addListener(RpgDebug::onClone);NeoForge.EVENT_BUS.addListener(RpgDebug::onRespawn);
        NeoForge.EVENT_BUS.addListener(RpgDebug::onLogout);
        NeoForge.EVENT_BUS.addListener(RpgDebug::onStop);
    }

    private static ProfileStore store(ServerPlayer player) {
        return new ProfileStore(player);
    }

    private static void tell(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal("[Oxy RPG] " + message),true);
        sync(player, message);
    }

    static void menuAction(ServerPlayer player, int requested, int direction) {
        switch (requested) {
            case 0 -> {sync(player, "");RpgEvasion.sync(player);}
            case 21 -> {if(direction>0&&direction<RpgClass.values().length)action(player,"classe",RpgClass.values()[direction]);}
            case 12 -> {if(!RpgSpecialists.intangible(player)&&!RpgVampire.bat(player))RpgEvasion.use(player,direction);}
            case 13 -> action(player,"classe",RpgClass.ILUSIONISTA);
            case 14 -> action(player,"classe",RpgClass.HIDROMANTE);
            case 15 -> action(player,"classe",RpgClass.PIROMANTE);
            case 17 -> action(player,"classe",RpgClass.VAMPIRESCO);
            case 16 -> {}
            case 1 -> castAbility(player, 0, direction);
            case 5 -> castAbility(player, 1, direction);
            case 6 -> castAbility(player, 2, direction);
            case 2, 3, 4 -> action(player, "classe", RpgClass.values()[requested - 1]);
            case 7 -> action(player,"classe",RpgClass.NECROMANTE);
            case 11 -> action(player,"classe",RpgClass.AVIARIO);
            case 10 -> action(player,"classe",RpgClass.XAMA);
            case 8,9 -> RpgShaman.potion(player,requested==9);
            case 300,301,302,303,304,305,306,307,308 -> VillagerQuests.action(player,requested,direction,-1);
            default -> { if (requested >= 102 && requested < 100+SkillTree.COUNT) unlockSkill(player, requested - 100);
                else if(requested>=1000&&requested<1000+AttributeTree.COUNT)allocateAttribute(player,requested-1000); }
        }
    }

    static void sync(ServerPlayer player, String notice) {
        try {
            RpgProfile profile = store(player).load(player.getUUID());
            var effective=RpgDebug.effective(player,profile);
            boolean debug=RpgDebug.debugging(player),noCooldown=RpgDebug.cooldownsDisabled(player);
            RpgAbilities.updateProfile(player,effective);
            ClassEquipment.onProfile(player,profile);
            RpgResources.update(player,profile);RpgResources.sync(player);
            Professions.sync(player);
            RpgAbilities.syncGhost(player);
            RpgSpecialists.sync(player);
            RpgAbilities.syncStatus(player);
            RpgDebug.sync(player,profile);
            long cooldown = noCooldown ? 0 : Math.max(0, RpgDebug.readyAt(player,profile,0) - player.level().getGameTime());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new RpgNetwork.State(effective.rpgClass().ordinal(), effective.level(), effective.xp(), profile.kills(), cooldown, noCooldown ? 0 : Math.max(0, RpgDebug.readyAt(player,profile,1) - player.level().getGameTime()), noCooldown ? 0 : Math.max(0, RpgDebug.readyAt(player,profile,2) - player.level().getGameTime()),
                    RpgDebug.effective(player, profile).skills(), debug, RpgDebug.testing(player), profile.questStage(), profile.questFirst(), profile.questSecond(), notice));
        } catch (IOException e) {
            LOGGER.error("Não foi possível atualizar o menu RPG de {}", player.getUUID(), e);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new RpgNetwork.State(0, 1, 0, 0, 0, 0, "Erro ao ler o perfil. Consulte o log; seus dados foram preservados."));
        }
    }

    private static int action(ServerPlayer player, String command, RpgClass selected) {
        try {
            ProfileStore store = store(player);
            RpgProfile profile = store.load(player.getUUID());
            if(!command.equals("classe"))profile=RpgDebug.effective(player,profile);
            switch (command) {
                case "classes" -> tell(player, "Escolha sua classe no menu R, ou /rpg classe <nome>. Classes: guerreiro, mago, assassino, arqueiro, necromante, xama, aviario, ilusionista, hidromante, piromante, vampiresco e deus_trovao. A escolha é definitiva neste mundo.");
                case "classe" -> {
                    if(RpgDebug.testing(player))return testMode(player,selected,false);
                    if(RpgDebug.debugging(player))return DebugControls.choose(player,selected);
                    RpgProfile next = profile.choose(selected);
                    store.save(player.getUUID(), next);
                    RpgDebug.clearSkills(player);
                    RpgAbilities.clear(player);
                    tell(player, "Classe escolhida: " + selected.label() + ". Ative sua habilidade no menu RPG.");
                }
                case "habilidade" -> { return castAbility(player, 0, 0); }
                case "missao" -> tell(player, QuestCatalog.quest(profile.questStage()).title() + ": "
                    + QuestCatalog.progress(profile.questStage(), profile.questFirst(), profile.questSecond())
                    + ". Recompensa: " + QuestCatalog.quest(profile.questStage()).reward() + " XP. Caçada geral: " + (profile.kills() % 10) + "/10.");
                default -> tell(player, profile.rpgClass().label() + " | Nível " + profile.level()
                    + " | XP " + profile.xp() + "/" + profile.xpToNextLevel()
                    + " | /rpg classe <nome> | /rpg habilidade | /rpg missao");
            }
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException e) {
            tell(player, e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Falha no perfil RPG do jogador {}", player.getUUID(), e);
            tell(player, "Não foi possível ler ou salvar seu perfil. Consulte o log do servidor.");
        }
        return 0;
    }

    private static int castAbility(ServerPlayer player, int slot, int direction) {
        try {
            var store = store(player);
            var real = store.load(player.getUUID());
            var effective = RpgDebug.effective(player, real);
            RpgAbilities.updateProfile(player, effective);
            boolean debug=RpgDebug.cooldownsDisabled(player), extra = RpgAbilities.extraDash(player, slot)||RpgAviary.boostAvailable(player,slot)||RpgSpecialists.extra(player,slot)||RpgVampire.extra(player,slot);
            AdvancedClasses.validate(player,effective,slot);RpgVampire.validate(player,slot);RpgSpecialists.validate(player,effective,slot);
            if(effective.rpgClass()==RpgClass.AVIARIO)RpgAviary.validate(player,slot);
            if (!effective.canUseAbility(slot)) { tell(player, "Aprenda o efeito inicial desse ramo na árvore de skills."); return 0; }
            if(effective.rpgClass()==RpgClass.XAMA&&slot==2&&(!player.getMainHandItem().isEmpty()||!player.getOffhandItem().isEmpty())){tell(player,"Especialista exige ambas as mãos vazias.");return 0;}
            long now = player.level().getGameTime();
            if (!debug && !extra && now < RpgDebug.readyAt(player,real,slot)) {
                tell(player, "Aguarde " + ((RpgDebug.readyAt(player,real,slot) - now + 19) / 20) + " segundos para essa habilidade."); return 0;
            }
            int cost=ClassEquipment.discount(player,effective.rpgClass(),RpgResources.cost(effective,slot,extra));
            RpgResources.update(player,real);
            if(!RpgResources.affordable(player,cost)){tell(player,"Mana insuficiente: precisa de "+cost+".");return 0;}
            if (!debug && !extra){if(RpgDebug.enabled(player))RpgDebug.cooldown(player,effective,slot,now);else store.save(player.getUUID(), real.useAbility(now, slot));}
            RpgResources.spend(player,cost);
            RpgAbilities.activate(player, effective, slot, direction);
            SkillEffects.cast(player,effective,slot);
            tell(player, (extra&&effective.rpgClass()==RpgClass.AVIARIO?"Impulso aéreo":SkillTree.abilityName(effective.rpgClass(), slot)) + (effective.mainUnlocked(slot) ? " ativada!" : " — versão inicial ativada!")
                + (RpgDebug.debugging(player) ? " DEBUG: recargas "+(debug?"desligadas":"ativas")+"; recursos "+(RpgDebug.unlimited(player)?"ilimitados":"normais")+"." : " Recarga: " + effective.abilityCooldown(slot) / 20 + " s. Mana: "+cost+" MP."));
            return 1;
        } catch (IllegalStateException e) { tell(player, e.getMessage()); }
        catch (IOException e) { LOGGER.error("Falha ao usar habilidade de {}", player.getUUID(), e); tell(player, "Não foi possível salvar a habilidade."); }
        return 0;
    }

    private static void allocateAttribute(ServerPlayer player,int id){
        if(RpgDebug.enabled(player)){tell(player,"Atributos temporários: use /rpg debug atributos maximos|normais, ou saia do modo de desenvolvimento para gastar pontos reais.");return;}
        try{
            var storage=store(player);var next=storage.load(player.getUUID()).allocateNode(player.getUUID(),id);
            storage.save(player.getUUID(),next);
            tell(player,"Atributo melhorado: "+AttributeStats.label(AttributeTree.type(player.getUUID(),id))+". "+AttributeStats.benefit(AttributeTree.type(player.getUUID(),id))+".");
        }catch(IllegalStateException e){tell(player,e.getMessage());}
        catch(IOException e){LOGGER.error("Falha ao salvar atributo",e);tell(player,"Não foi possível salvar o atributo.");}
    }
    private static void unlockSkill(ServerPlayer player, int id) {
        try {
            ProfileStore store = store(player);
            RpgProfile profile = store.load(player.getUUID());
            boolean debug = RpgDebug.enabled(player);
            RpgProfile next = debug ? RpgDebug.unlock(player, profile, id) : profile.unlockSkill(id);
            if (!debug) store.save(player.getUUID(), next);
            tell(player, (debug ? "DEBUG: skill temporária: " : "Skill aprendida: ") + SkillTree.nodes(next.rpgClass()).get(id).title() + ".");
        } catch (IllegalStateException e) { tell(player, e.getMessage()); }
        catch (IOException e) {
            LOGGER.error("Falha ao salvar skill de {}", player.getUUID(), e);
            tell(player, "Não foi possível salvar a skill. Consulte o log.");
        }
    }

    private static int resetClass(net.minecraft.commands.CommandSourceStack source, ServerPlayer target) {
        if(RpgDebug.enabled(target)){source.sendFailure(Component.literal("Use /rpg debug off antes de resetar o perfil real."));return 0;}
        try {
            ProfileStore store = store(target);
            store.save(target.getUUID(), store.load(target.getUUID()).resetClass());
            RpgDebug.clearSkills(target);
            RpgAbilities.clear(target);
            RpgResources.reset(target);
            tell(target, "Classe, nível, XP e skills resetados. Caçadas e missões preservadas. Escolha uma classe no menu RPG.");
            source.sendSuccess(() -> Component.literal("Classe resetada para " + target.getName().getString() + "."), true);
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            LOGGER.error("Falha ao resetar classe de {}", target.getUUID(), e);
            source.sendFailure(Component.literal("Não foi possível salvar o reset. Consulte o log."));
            return 0;
        }
    }

    private static int testMode(ServerPlayer player,RpgClass choice,boolean off){
        try{
            var real=store(player).load(player.getUUID());
            boolean entering=!RpgDebug.testing(player);
            RpgAbilities.clear(player);
            if(off){RpgDebug.disable(player);RpgEvasion.endTesting(player);}else RpgDebug.test(player,real,choice);
            sync(player,"");
            Professions.refreshModifiers(player);
            if(!off&&entering){player.setHealth(player.getMaxHealth());player.getFoodData().setFoodLevel(20);}
            tell(player,off?"TESTE OFF: classe, níveis, skills, atributos e profissões salvos restaurados."
                :"TESTE ON: "+RpgDebug.effective(player,real).rpgClass().label()+", nível 100, todas as skills e atributos máximos; Mineração, Agricultura e Arsenal 20. Mana e recargas normais. Troque com /rpg teste <classe>; saia com /rpg teste off.");
            return 1;
        }catch(IOException e){LOGGER.error("Falha ao preparar modo teste",e);tell(player,"Não foi possível ler seu perfil para o modo teste.");return 0;}
    }
    static int debug(ServerPlayer player, int mode) {
        try {
            if (mode == 1) RpgDebug.enable(player, store(player).load(player.getUUID()));
            else if (mode == 2) {
                if (!RpgDebug.enabled(player)) { tell(player, "Ative /rpg debug on primeiro."); return 0; }
                if(RpgDebug.testing(player)){tell(player,"No modo teste, a árvore fica completa. Use /rpg debug on para testar skills individualmente.");return 0;}
                RpgDebug.clearSkills(player);
                    RpgAbilities.clear(player);
            } else RpgDebug.disable(player);
            RpgAbilities.clear(player);
            tell(player, mode == 1 ? "DEBUG ON: nível 100, todas as skills liberadas, recursos ilimitados e recargas desligadas. Configure em R > Debug ou /rpg debug. Opções salvas neste mundo; perfil normal preservado."
                : mode == 2 ? "DEBUG: árvore temporária limpa para testar outra combinação."
                : "DEBUG OFF: skills e recarga normais restauradas.");
            sync(player,"");Professions.refreshModifiers(player);
            return 1;
        } catch (IOException e) {
            LOGGER.error("Falha ao preparar debug de {}", player.getUUID(), e);
            tell(player, "Não foi possível ler o perfil para debug.");
            return 0;
        }
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        var root = Commands.literal("rpg")
            .executes(c -> action(c.getSource().getPlayerOrException(), "status", null))
            .then(Commands.literal("kit").executes(c->{ClassEquipment.request(c.getSource().getPlayerOrException());return 1;}))
            .then(Commands.literal("status").executes(c -> action(c.getSource().getPlayerOrException(), "status", null)))
            .then(Commands.literal("habilidade").executes(c -> castAbility(c.getSource().getPlayerOrException(), 0, 0))
                .then(Commands.literal("1").executes(c -> castAbility(c.getSource().getPlayerOrException(), 0, 0)))
                .then(Commands.literal("2").executes(c -> castAbility(c.getSource().getPlayerOrException(), 1, 0)))
                .then(Commands.literal("3").executes(c -> castAbility(c.getSource().getPlayerOrException(), 2, 0))))
            .then(Commands.literal("missao").executes(c -> action(c.getSource().getPlayerOrException(), "missao", null)));
        var classes = Commands.literal("classe")
            .executes(c -> action(c.getSource().getPlayerOrException(), "classes", null));
        for (RpgClass selected : RpgClass.values()) {
            if (selected != RpgClass.NONE) classes.then(Commands.literal(selected.command())
                .executes(c -> action(c.getSource().getPlayerOrException(), "classe", selected)));
        }
        classes.then(Commands.literal("arqueiro_novo").executes(c->action(c.getSource().getPlayerOrException(),"classe",RpgClass.ARQUEIRO_NOVO)));
        root.then(classes);
        root.then(Commands.literal("selo").executes(c->{var p=c.getSource().getPlayerOrException();var item=new net.minecraft.world.item.ItemStack(RpgItems.SIGIL.get());if(!p.addItem(item))p.drop(item,false);return 1;}));
        root.then(Commands.literal("desempenho").requires(Commands.hasPermission(2)).executes(c->{for(var line:RpgProfiler.report())c.getSource().sendSuccess(()->Component.literal(line),false);return 1;})
            .then(Commands.literal("limpar").executes(c->{RpgProfiler.clear();return 1;})));
        root.then(Commands.literal("exportarperfil").requires(Commands.hasPermission(2)).executes(c->{var p=c.getSource().getPlayerOrException();try{var profile=new ProfileStore(p).load(p.getUUID());new ProfileStore(p.level().getServer().getWorldPath(LevelResource.ROOT).resolve("oxyrpg/exports")).save(p.getUUID(),profile);tell(p,"Perfil exportado em oxyrpg/exports.");return 1;}catch(IOException e){c.getSource().sendFailure(Component.literal("Não foi possível exportar o perfil."));return 0;}}));
        root.then(DebugControls.commands());
        var tests=Commands.literal("teste").requires(DebugControls::allowed)
            .executes(c->testMode(c.getSource().getPlayerOrException(),null,false))
            .then(Commands.literal("on").executes(c->testMode(c.getSource().getPlayerOrException(),null,false)))
            .then(Commands.literal("off").executes(c->testMode(c.getSource().getPlayerOrException(),null,true)));
        for(var choice:RpgClass.values())if(choice!=RpgClass.NONE)tests.then(Commands.literal(choice.command())
            .executes(c->testMode(c.getSource().getPlayerOrException(),choice,false)));
        tests.then(Commands.literal("arqueiro_novo").executes(c->testMode(c.getSource().getPlayerOrException(),RpgClass.ARQUEIRO_NOVO,false)));
        root.then(tests);
        root.then(Commands.literal("elite").requires(Commands.hasPermission(2))
            .executes(c->{EliteAbilities.force(c.getSource().getPlayerOrException(),3);return 1;})
            .then(Commands.argument("escala",com.mojang.brigadier.arguments.FloatArgumentType.floatArg(2,4))
                .executes(c->{EliteAbilities.force(c.getSource().getPlayerOrException(),com.mojang.brigadier.arguments.FloatArgumentType.getFloat(c,"escala"));return 1;})));
        root.then(Commands.literal("resetclasse").requires(Commands.hasPermission(2))
            .executes(c -> resetClass(c.getSource(), c.getSource().getPlayerOrException()))
            .then(Commands.argument("jogador", net.minecraft.commands.arguments.EntityArgument.player())
                .executes(c -> resetClass(c.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayer(c, "jogador")))));
        event.getDispatcher().register(root);
    }

    private static void onEnemyDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Enemy)||RpgNecromancer.isSummon(event.getEntity())||RpgSpecialists.temporary(event.getEntity())) return;
        net.minecraft.world.entity.Entity credit = RpgNecromancer.owner(event.getSource());
        if (!(credit instanceof ServerPlayer)) credit = event.getEntity().getKillCredit();
        if (!(credit instanceof ServerPlayer player)) return;
        if(RpgDebug.enabled(player)){RpgAbilities.killed(player);RpgResources.rareDrop(player,event.getEntity());return;}
        try {
            ProfileStore store = store(player);
            RpgProfile previous = store.load(player.getUUID());
            if (previous.rpgClass() == RpgClass.NONE) return;
            String mob = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();
            RpgProfile next = previous.defeatedEnemy(mob,EliteMobs.xp(event.getEntity(),RpgNecromancer.summonKill(event.getSource(),event.getEntity(),player)?12:25));
            store.save(player.getUUID(), next);
            sync(player, "");
            RpgAbilities.killed(player);
            RpgResources.rareDrop(player,event.getEntity());
            if (next.level() > previous.level()) {
                tell(player,"+"+(next.level()-previous.level())+" ponto(s) de atributo! Abra R > Atributos.");
                ProgressionEffects.levelUp(player);
                tell(player, "Você chegou ao nível " + next.level() + "!");
            }
            int points = next.level() / 2 - previous.level() / 2;
            if (points > 0) {
                ProgressionEffects.skillPoint(player);
                tell(player, "+" + points + " ponto(s) de skill! Abra a árvore no menu RPG.");
            }
            if (next.questStage() > previous.questStage()) tell(player,
                "Missão concluída: " + QuestCatalog.quest(previous.questStage()).title() + "! +"
                    + QuestCatalog.quest(previous.questStage()).reward() + " XP. Nova missão: " + QuestCatalog.quest(next.questStage()).title() + ".");
            if (next.kills() % 10 == 0) tell(player, "Caçada concluída! +100 XP extras. Uma nova caçada começou.");
        } catch (IOException | ArithmeticException e) {
            LOGGER.error("Falha ao conceder XP para {}", player.getUUID(), e);
            tell(player, "Seu XP não pôde ser salvo. Consulte o log do servidor.");
        }
    }
}
















