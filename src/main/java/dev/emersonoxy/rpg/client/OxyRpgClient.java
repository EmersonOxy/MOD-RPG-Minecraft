package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.OxyRpg;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import dev.emersonoxy.rpg.RpgClass;
import dev.emersonoxy.rpg.QuestCatalog;

@Mod(value = OxyRpg.MOD_ID, dist = Dist.CLIENT)
public final class OxyRpgClient {
    private static RpgNetwork.DebugState debugState;
    public static RpgNetwork.DebugState debugState(){return debugState;}
    private static final KeyMapping.Category CATEGORY=new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath("oxyrpg","controls"));
    private static final java.util.List<KeyMapping> ALL_KEYS=new java.util.ArrayList<>();
    /** Declare every present and future shortcut through this factory; registration is automatic. */
    private static KeyMapping key(String name,int code){
        return key(name,code,net.neoforged.neoforge.client.settings.KeyConflictContext.IN_GAME);
    }
    private static KeyMapping key(String name,int code,net.neoforged.neoforge.client.settings.IKeyConflictContext context){
        var mapping=new KeyMapping("key.oxyrpg."+name,context,
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,code,CATEGORY);
        ALL_KEYS.add(mapping);return mapping;
    }
    public static final KeyMapping COMPARE_EQUIPMENT=key("compare_equipment",GLFW.GLFW_KEY_LEFT_SHIFT,net.neoforged.neoforge.client.settings.KeyConflictContext.GUI);
    public static final KeyMapping DODGE=key("dodge",GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping MINIMAP=key("minimap",GLFW.GLFW_KEY_M);
    public static final KeyMapping MINIMAP_OPTIONS=key("minimap_options",GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping TOGGLE_PANELS=key("panels",GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping HUD_OPTIONS=key("hud_options",GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping REVEAL_HUD=key("reveal_hud",GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping MOVEMENT_MODE=key("movement_mode",GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping OPEN_MENU = key("menu",GLFW.GLFW_KEY_R);
    public static final KeyMapping TOGGLE_HUD = key("hud",GLFW.GLFW_KEY_H);
    public static final KeyMapping SECOND_ABILITY = key("ability2",GLFW.GLFW_KEY_V);
    public static final KeyMapping THIRD_ABILITY = key("ability3",GLFW.GLFW_KEY_B);
    public static final KeyMapping USE_ABILITY = key("ability",GLFW.GLFW_KEY_G);
    public static final KeyMapping CAMERA_OPTIONS = key("camera",GLFW.GLFW_KEY_O); public static final KeyMapping SWAP_SHOULDER = key("shoulder",GLFW.GLFW_KEY_J); public static final KeyMapping LOCK_TARGET=key("lock",GLFW.GLFW_KEY_Z);
    public static final KeyMapping NEXT_TARGET=key("next_target",GLFW.GLFW_KEY_N);
    public static final KeyMapping[] CAMERA_POSITION={
        key("camera_far",GLFW.GLFW_KEY_KP_ADD),
        key("camera_near",GLFW.GLFW_KEY_KP_SUBTRACT),
        key("camera_side_out",GLFW.GLFW_KEY_KP_6),
        key("camera_side_in",GLFW.GLFW_KEY_KP_4),
        key("camera_up",GLFW.GLFW_KEY_KP_8),
        key("camera_down",GLFW.GLFW_KEY_KP_2)};
    private static RpgNetwork.State hudState;
    private static RpgNetwork.Resources resourceState;
    private static RpgNetwork.ProfessionState professionState;
    public static RpgNetwork.ProfessionState professions(){return professionState;}
    private static RpgNetwork.JourneyState journeyState;
    public static RpgNetwork.JourneyState journey(){return journeyState;}
    public static RpgNetwork.State state(){return hudState;}
    public static long cooldown(int slot){var mc=Minecraft.getInstance();return mc.level==null?0:Math.max(0,hudReadyAt[slot]-mc.level.getGameTime());}
    public static dev.emersonoxy.rpg.RpgProfile profile(){return hudState==null||hudState.rpgClass()==0?null:new dev.emersonoxy.rpg.RpgProfile(RpgClass.values()[hudState.rpgClass()],hudState.level(),hudState.xp(),hudState.kills(),0,hudState.skills(),0,0,0,0,0,dev.emersonoxy.rpg.AttributeStats.EMPTY,"",hudState.debug()||hudState.testing());}
    public static RpgNetwork.Resources resources(){return resourceState;}
    private static final long[] hudReadyAt = new long[3];
    private static final long[] effectUntil = new long[3];
    private static final int[] effectTotal = new int[3], effectPhase = new int[3], cooldownTotal = new int[3];
    private static final net.minecraft.world.item.ItemStack[] skillIcons = new net.minecraft.world.item.ItemStack[3];
    private static LocalPlayer trackedPlayer;
    private static ClientLevel trackedLevel;
    private static int refreshTicks;

    public OxyRpgClient(IEventBus bus,net.neoforged.fml.ModContainer container) {
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,net.neoforged.neoforge.client.gui.ConfigurationScreen::new);
        if(Boolean.getBoolean("oxyrpg.cameraQA"))NeoForge.EVENT_BUS.addListener(CameraClientQA::tick);
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->e.registerEntityRenderer(dev.emersonoxy.rpg.VillageGuards.GUARD.get(),VillageGuardRenderer::new));
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e)->e.enqueueWork(()->{if(net.neoforged.fml.ModList.get().isLoaded("player_animation_library")){DodgeAnimation.register();if(net.neoforged.fml.ModList.get().isLoaded("entity_model_features")&&net.neoforged.fml.ModList.get().isLoaded("entity_texture_features"))EmfCombatBridge.register();}}));
        bus.addListener(OxyRpgClient::registerKeys);
        bus.addListener(OxyRpgClient::registerPayloads);
        bus.addListener(OxyRpgClient::registerHud);bus.addListener(CombatParticle::register);bus.addListener(AmbientLeaf::register);bus.addListener(WaterWake::register);bus.addListener(WaterPulse::register);
        NeoForge.EVENT_BUS.addListener(VegetationMotion::extract);NeoForge.EVENT_BUS.addListener(VegetationMotion::render);
        NeoForge.EVENT_BUS.addListener(OutsideClick::frame);NeoForge.EVENT_BUS.addListener(OutsideClick::click);
        NeoForge.EVENT_BUS.addListener(OxyRpgClient::tick);NeoForge.EVENT_BUS.addListener(VillagerClient::init);NeoForge.EVENT_BUS.addListener(VillagerClient::scroll);NeoForge.EVENT_BUS.addListener(InventoryMovement::opening); NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderHandEvent event)->{if(ShoulderCamera.hideHands())event.setCanceled(true);});
        NeoForge.EVENT_BUS.addListener(DynamicHud::beforeLayer);
        NeoForge.EVENT_BUS.addListener(DynamicHud::afterLayer);
        NeoForge.EVENT_BUS.addListener(OxyRpgClient::potionInput);
        NeoForge.EVENT_BUS.addListener(EquipmentTooltip::gather);
        NeoForge.EVENT_BUS.addListener(EquipmentTooltip::render);
        NeoForge.EVENT_BUS.addListener(EquipmentTooltip::pressed);
        NeoForge.EVENT_BUS.addListener(EquipmentTooltip::released);
        bus.addListener(EnemyNameplates::register);
        bus.addListener(MovementFacing::register);bus.addListener(ClassCastEffects::register);
        NeoForge.EVENT_BUS.addListener(EnemyNameplates::player);
        NeoForge.EVENT_BUS.addListener(EnemyNameplates::hand);
        NeoForge.EVENT_BUS.addListener(EnemyNameplates::plate);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent e)->{if(dev.emersonoxy.rpg.RpgSpecialists.intangible(e.getPlayer()))e.setCanceled(true);});
    }
    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        ALL_KEYS.forEach(event::register);
    }
    private static void registerHud(RegisterGuiLayersEvent event) {
        event.registerBelow(net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,ResourceLocation.fromNamespaceAndPath("oxyrpg","mana_space"),(g,d)->{
            var mc=Minecraft.getInstance();if(resourceState!=null&&hudState!=null&&hudState.rpgClass()!=0&&DynamicHud.alpha(DynamicHud.Part.MANA)>0&&!mc.options.hideGui&&mc.gameMode.canHurtPlayer())mc.gui.leftHeight+=10;
        });
        event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,ResourceLocation.fromNamespaceAndPath("oxyrpg","mana"),(g,d)->renderMana(g));
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("oxyrpg","exploration"),(g,d)->{EvasionHud.render(g);SimpleMinimap.render(g);ExplorationHud.render(g);});
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("oxyrpg", "hud"), (graphics, delta) -> renderHud(graphics));
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("oxyrpg","combat_feedback"),(g,d)->{AbilityPreview.render(g);EliteWarnings.render(g);DamageNumbers.render(g);SkillEffectsRenderer.render(g);ClassCastEffects.render(g);});
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("oxyrpg", "shoulder_crosshair"),(g,d)->{
            var mc=Minecraft.getInstance();if(!ShoulderCamera.active()||mc.options.hideGui||DynamicHud.mode()==DynamicHud.Mode.HIDDEN||mc.screen!=null||mc.player==null||mc.player.isSpectator())return;
            var pipeline=net.minecraft.client.renderer.RenderPipelines.CROSSHAIR;
            int cx=g.guiWidth()/2,cy=g.guiHeight()/2;
            g.blitSprite(pipeline,ResourceLocation.withDefaultNamespace("hud/crosshair"),cx-7,cy-7,15,15);
            var target=TargetLock.target();if(target!=null){
                var targetScreen=CameraProjection.project(TargetLock.point(target,mc.gameRenderer.getMainCamera().getPartialTickTime()));
                if(targetScreen.z>=-1&&targetScreen.z<=1){
                    int tx=(int)((targetScreen.x+1)*g.guiWidth()/2),ty=(int)((1-targetScreen.y)*g.guiHeight()/2);
                    g.drawCenteredString(mc.font,"[ "+target.getName().getString()+" ]",tx,ty+20,0xFFFFD36B);
                }
            }
            if(mc.options.attackIndicator().get()==net.minecraft.client.AttackIndicatorStatus.CROSSHAIR){
                float strength=mc.player.getAttackStrengthScale(0);int x=cx-8,y=cy+9;
                if(strength<1){g.blitSprite(pipeline,ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background"),x,y,16,4);
                    g.blitSprite(pipeline,ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress"),16,4,0,0,x,y,(int)(strength*17),4);}
            }
        });
        
    }
    private static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(RpgNetwork.ContentState.TYPE,(value,context)->{dev.emersonoxy.rpg.RpgContent.receive(value.json());var screen=Minecraft.getInstance().screen;if(screen instanceof SkillTreeScreen tree)tree.loadNodes();if(screen instanceof ClassSelectionScreen classes)classes.contentChanged();});
        event.register(RpgNetwork.EvasionState.TYPE,(value,context)->EvasionHud.receive(value));
        event.register(RpgNetwork.CombatText.TYPE,(value,context)->DamageNumbers.receiveText(value));
        event.register(RpgNetwork.EliteWarning.TYPE,(value,context)->EliteWarnings.receive(value));
        event.register(RpgNetwork.FlightState.TYPE,(value,context)->{
            var p=Minecraft.getInstance().player;if(p==null)return;var tag=p.getPersistentData();
            if(value.remaining()>0&&(tag.getIntOr("oxy_flight_until",0)<=p.tickCount||value.gliding()&&!tag.getBooleanOr("oxy_flight_gliding",false)))BarrelRollCompat.resetFlight();
            tag.putBoolean("oxy_flight_gliding",value.gliding());tag.putInt("oxy_flight_until",p.tickCount+value.remaining());tag.putInt("oxy_boost_until",p.tickCount+value.boost());tag.putInt("oxy_meteor_until",p.tickCount+value.meteor());tag.putBoolean("oxy_fast_flight",value.fast());tag.putFloat("oxy_flight_thrust",value.thrust());tag.putBoolean("oxy_meteor_forward",value.forward());tag.putFloat("oxy_meteor_x",value.meteorX());tag.putFloat("oxy_meteor_y",value.meteorY());tag.putFloat("oxy_meteor_z",value.meteorZ());
        });
        event.register(RpgNetwork.DamageNumber.TYPE,(value,context)->DamageNumbers.receive(value));
        event.register(RpgNetwork.JourneyState.TYPE,(value,context)->journeyState=value);
        event.register(RpgNetwork.ProfessionState.TYPE,(value,context)->{professionState=value;dev.emersonoxy.rpg.EquipmentAccess.clientXp(value.mining(),value.farming(),value.arsenal());var mc=Minecraft.getInstance();if(mc.screen instanceof ProfessionsScreen screen)screen.update(value);});
        event.register(RpgNetwork.VillagerMark.TYPE,(value,context)->EnemyNameplates.villagerMark(value.entityId(),value.state()));
        event.register(RpgNetwork.EliteInfo.TYPE,(value,context)->EnemyNameplates.eliteTitle(value.entityId(),value.title()));
        event.register(RpgNetwork.VillagerDialogue.TYPE,(value,context)->VillagerClient.receive(value));
        event.register(RpgNetwork.Resources.TYPE,(value,context)->{
            if(resourceState==null||resourceState.mana()!=value.mana()||resourceState.maxMana()!=value.maxMana()){DynamicHud.signal(DynamicHud.Part.MANA);}
            if(resourceState==null||resourceState.points()!=value.points())DynamicHud.signal(DynamicHud.Part.RPG);
            resourceState=value;var mc=Minecraft.getInstance();
            if(mc.screen instanceof AttributesScreen screen)screen.update(value);
        });
        event.register(RpgNetwork.MobLevel.TYPE, (value, context) -> EnemyNameplates.mobLevel(value.entityId(), value.level()));
        event.register(RpgNetwork.AbilityStatus.TYPE, (value, context) -> {
            var mc=Minecraft.getInstance();long time=mc.level==null?0:mc.level.getGameTime();
            for(int slot=0;slot<3;slot++){effectUntil[slot]=value.remaining(slot)==-1?Long.MAX_VALUE:time+value.remaining(slot);effectTotal[slot]=value.total(slot);effectPhase[slot]=value.phase(slot);}
        });
        event.register(RpgNetwork.DebugState.TYPE,(value,context)->{debugState=value;var player=Minecraft.getInstance().player;if(player!=null)player.getPersistentData().putBoolean("oxy_debug_no_cooldown",value.mode()==1&&!value.cooldowns());});
        event.register(RpgNetwork.CastFx.TYPE,(value,context)->ClassCastEffects.receive(value));
        event.register(RpgNetwork.BloodFx.TYPE,(value,context)->ParticleFx.blood(value));
        event.register(RpgNetwork.ImpactFx.TYPE,(value,context)->SkillEffectsRenderer.receive(value));
        event.register(RpgNetwork.DisguiseState.TYPE,(value,context)->DisguiseVisuals.receive(value));
        event.register(RpgNetwork.EntityMotion.TYPE,(value,context)->EntityMotionVisuals.receive(value));
        event.register(RpgNetwork.VampireState.TYPE,(value,context)->VampireHud.receive(value));
        event.register(RpgNetwork.SpecialState.TYPE,(value,context)->SpecialistHud.receive(value));
        event.register(RpgNetwork.Ghost.TYPE, (ghost, context) -> {EnemyNameplates.ghost(ghost.entityId(), ghost.ticks());if(ghost.ticks()<=0)ClassCastEffects.cancel(ghost.entityId());});
        event.register(RpgNetwork.State.TYPE, (state, context) -> {
            var mc = Minecraft.getInstance();
            if(hudState==null||state.xp()!=hudState.xp()||state.level()!=hudState.level()||state.rpgClass()!=hudState.rpgClass()||state.skills()!=hudState.skills())DynamicHud.signal(DynamicHud.Part.RPG);
            if(hudState==null||state.kills()!=hudState.kills()||state.questStage()!=hudState.questStage()||state.questFirst()!=hudState.questFirst()||state.questSecond()!=hudState.questSecond())DynamicHud.signal(DynamicHud.Part.QUEST);
            hudState = state;
            if(mc.player!=null){mc.player.getPersistentData().putBoolean("oxy_vampire_class",state.rpgClass()==RpgClass.VAMPIRESCO.ordinal());mc.player.getPersistentData().putBoolean("oxy_feather_ready",state.rpgClass()==RpgClass.AVIARIO.ordinal()&&state.hasSkill(8));mc.player.getPersistentData().putBoolean("oxy_feather_enhanced",state.hasSkill(11));}
            dev.emersonoxy.rpg.EquipmentAccess.clientClass(state.rpgClass());
            dev.emersonoxy.rpg.EquipmentAccess.clientLevel(state.level());
            if(state.rpgClass()!=0)for(int slot=0;slot<3;slot++){
                cooldownTotal[slot]=profile().abilityCooldown(slot);
                int node=slot*7+(state.hasSkill(slot*7+3)?3:1);
                var icon=dev.emersonoxy.rpg.SkillTree.nodes(RpgClass.values()[state.rpgClass()]).get(node).icon();
                var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(icon));
                skillIcons[slot]=new net.minecraft.world.item.ItemStack(item==null?net.minecraft.world.item.Items.BOOK:item);
            }
            for (int slot = 0; slot < 3; slot++) hudReadyAt[slot] = (mc.level == null ? 0 : mc.level.getGameTime()) + state.cooldown(slot);
            if (mc.screen instanceof RpgScreen screen) screen.update(state);
            if (mc.screen instanceof ClassSelectionScreen screen) screen.update(state);
            if (mc.screen instanceof SkillTreeScreen screen) screen.update(state);
            if (mc.screen instanceof MissionScreen screen) screen.update(state);
        });
    }
    private static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player != trackedPlayer || mc.level != trackedLevel) {
            EvasionHud.clear();SimpleMinimap.clear();DamageNumbers.clear();SkillEffectsRenderer.clear();ParticleFx.clear();ClassCastEffects.clear();debugState=null;if(mc.player!=null)mc.player.getPersistentData().remove("oxy_debug_no_cooldown");EliteWarnings.clear();AbilityPreview.clear();journeyState=null;ShoulderCamera.reset();TargetLock.clear();MovementFacing.reset();DynamicHud.reset(); trackedPlayer = mc.player;
            trackedLevel = mc.level;
            hudState = null;resourceState=null;professionState=null;
            dev.emersonoxy.rpg.EquipmentAccess.resetClient();
            java.util.Arrays.fill(hudReadyAt, 0);
            EnemyNameplates.world(mc.level);
            EnemyNameplates.clearGhosts();
            java.util.Arrays.fill(effectUntil,0);java.util.Arrays.fill(effectPhase,0);
            refreshTicks = 0;
            if (mc.player != null && mc.level != null) mc.player.connection.send(new RpgNetwork.Request(0));
        }
        if (mc.player != null && mc.level != null && mc.screen == null && ++refreshTicks >= 200) {
            refreshTicks = 0;
            mc.player.connection.send(new RpgNetwork.Request(0));
        }
        while(LOCK_TARGET.consumeClick())if(mc.player!=null&&mc.screen==null)TargetLock.toggle();
        while(NEXT_TARGET.consumeClick())if(mc.player!=null&&mc.screen==null)TargetLock.cycle();
        for(int i=0;i<CAMERA_POSITION.length;i++)while(CAMERA_POSITION[i].consumeClick())if(mc.player!=null&&mc.screen==null)ShoulderCamera.adjust(i/2,i%2==0?.1:-.1);
        while(DODGE.consumeClick())if(mc.player!=null&&mc.screen==null){
            int side=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0),forward=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
            int direction=side==0&&forward==0?0:Math.floorMod((int)Math.round(Math.atan2(side,forward)/(Math.PI/4)),8);
            mc.player.connection.send(new RpgNetwork.Request(12,direction));DynamicHud.signal(DynamicHud.Part.EVASION);
        }
        while(MINIMAP.consumeClick())if(mc.player!=null&&mc.screen==null)DynamicHud.toggle(DynamicHud.Part.MINIMAP);
        while(MINIMAP_OPTIONS.consumeClick())if(mc.player!=null&&mc.screen==null)mc.setScreen(new MinimapSettingsScreen(null));
        AmbientWorld.tick();SimpleMinimap.tick();ParticleFx.tick();VillagerClient.tick();ExplorationHud.tick();ClassCastEffects.tick();
        TargetLock.tick();
        DynamicHud.tick();
        while(TOGGLE_PANELS.consumeClick())if(mc.player!=null&&mc.screen==null)DynamicHud.cyclePanels();
        while(HUD_OPTIONS.consumeClick())if(mc.player!=null&&mc.screen==null)mc.setScreen(new HudSettingsScreen(null));
        while(MOVEMENT_MODE.consumeClick())if(mc.player!=null&&mc.screen==null)ShoulderCamera.toggleMovement();
        while(CAMERA_OPTIONS.consumeClick())if(mc.player!=null&&mc.screen==null)mc.setScreen(new CameraSettingsScreen(null)); while(SWAP_SHOULDER.consumeClick())if(mc.player!=null&&mc.screen==null)ShoulderCamera.swap(); while (TOGGLE_HUD.consumeClick()) {
            if (mc.player != null && mc.screen == null) DynamicHud.cycle();
        }
        while (USE_ABILITY.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.screen == null) AbilityPreview.press(0);
        }
        while (SECOND_ABILITY.consumeClick()) if (mc.player != null && mc.screen == null) AbilityPreview.press(1);
        while (THIRD_ABILITY.consumeClick()) if (mc.player != null && mc.screen == null) AbilityPreview.press(2);
        while (OPEN_MENU.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new RpgScreen());
        }
        BarrelRollCompat.tick();LiveAim.tick();SpecialistHud.tick();
        AbilityPreview.tick();
    }

    public static boolean liveChannel(){var mc=Minecraft.getInstance();return mc.level!=null&&hudState!=null&&(hudState.rpgClass()==dev.emersonoxy.rpg.RpgClass.HIDROMANTE.ordinal()||hudState.rpgClass()==dev.emersonoxy.rpg.RpgClass.MAGO.ordinal())&&effectUntil[1]>mc.level.getGameTime();}
    private static void renderHud(GuiGraphics g) {
        var mc=Minecraft.getInstance();
        if(mc.options.hideGui||mc.getDebugOverlay().showDebugScreen()||mc.player==null||mc.level==null||mc.screen!=null||hudState==null)return;
        SpecialistHud.render(g);VampireHud.prey(g);
        renderAbilityIcons(g);
        renderDynamicPanels(g);
    }
    private static void renderMana(GuiGraphics g){
        var mc=Minecraft.getInstance();var r=resourceState;
        if(DynamicHud.alpha(DynamicHud.Part.MANA)<=0||mc.options.hideGui||mc.screen!=null||mc.player==null||r==null||hudState==null||hudState.rpgClass()==0)return;
        HudOpacity.begin(DynamicHud.alpha(DynamicHud.Part.MANA));
        int x=g.guiWidth()/2-91,y=g.guiHeight()-39;
        // Ten drops represent the fraction of total mana, so the row stays compact at high levels.
        float fraction=(float)r.mana()/Math.max(1,r.maxMana());
        for(int i=0;i<10;i++){
            int px=x+i*8;float fill=Math.clamp(fraction*10-i,0,1);
            int color=fill>=1?0xFF65BBFF:fill>0?0xFF367AB0:0xFF23364C;
            g.fill(px+3,y,px+5,y+2,color);g.fill(px+2,y+2,px+6,y+4,color);
            g.fill(px+1,y+4,px+7,y+7,color);g.fill(px+2,y+7,px+6,y+8,color);
            if(fill>0)g.fill(px+2,y+4,px+3,y+6,0xFFB7E6FF);
        }
        HudOpacity.end();
    }
    private static void renderAbilityIcons(GuiGraphics g) {
        var mc=Minecraft.getInstance();var state=hudState;if(state==null||state.rpgClass()==0||mc.level==null)return;
        boolean dock=g.guiWidth()>=390;int start=dock?g.guiWidth()/2-190:g.guiWidth()/2-44,y=dock?g.guiHeight()-37:g.guiHeight()-Math.max(102,mc.gui.leftHeight+52);var keys=new KeyMapping[]{USE_ABILITY,SECOND_ABILITY,THIRD_ABILITY};
        for(int slot=0;slot<3;slot++){float alpha=DynamicHud.alpha(DynamicHud.ability(slot));if(alpha<=0)continue;HudOpacity.begin(alpha);int x=start+slot*30;
            long remaining=effectUntil[slot]==Long.MAX_VALUE?Long.MAX_VALUE:Math.max(0,effectUntil[slot]-mc.level.getGameTime()),cooldown=Math.max(0,hudReadyAt[slot]-mc.level.getGameTime());boolean available=state.canUse(slot),active=remaining>0;
            int accent=ClassSelectionScreen.accent(RpgClass.values()[state.rpgClass()]),ink=!available?0xFF586473:active?0xFFF6E4AA:cooldown>0?0xFF8392A2:accent;
            g.fill(x-1,y-1,x+27,y+27,0xAA080C12);g.fill(x,y,x+26,y+26,0xE8202935);RpgUi.border(g,x,y,26,26,0xFF566173);g.fill(x+1,y+1,x+25,y+2,ink);g.fill(x+1,y+24,x+25,y+25,0xFF111823);
            if(skillIcons[slot]!=null&&!skillIcons[slot].isEmpty())g.renderFakeItem(skillIcons[slot],x+5,y+5);
            if(!available)g.fill(x+2,y+2,x+24,y+24,0xBA101720);
            else if(cooldown>0&&!active){int cover=(int)(22*Math.clamp((float)cooldown/Math.max(1,cooldownTotal[slot]),0,1));g.fill(x+2,y+24-cover,x+24,y+24,0xB0081018);}
            if(active){g.fill(x+1,y+24,x+25,y+26,ink);}else if(available&&cooldown==0){g.fill(x+2,y+25,x+24,y+26,accent);}
            String key=mc.font.plainSubstrByWidth(keys[slot].getTranslatedKeyMessage().getString(),26);g.pose().pushMatrix();g.pose().translate(x+13,y+29);g.pose().scale(.75f,.75f);g.drawString(mc.font,key,-mc.font.width(key)/2,0,0xFFEADCC0);g.pose().popMatrix();
            String time=!available?"×":cooldown>0&&!active?((cooldown+19)/20)+"":active&&remaining!=Long.MAX_VALUE?((remaining+19)/20)+"":"";
            if(!time.isEmpty()){g.pose().pushMatrix();g.pose().translate(x+13,y-10);g.pose().scale(.85f,.85f);g.drawString(mc.font,time,-mc.font.width(time)/2,0,ink);g.pose().popMatrix();}
            HudOpacity.end();
        }
    }

    public static boolean abilityActive(int slot){
        var mc=Minecraft.getInstance();return mc.level!=null&&hudState!=null&&hudState.rpgClass()!=0
            &&(hudReadyAt[slot]>mc.level.getGameTime()||effectUntil[slot]>mc.level.getGameTime());
    }
    private static void renderDynamicPanels(GuiGraphics g){
        var mc=Minecraft.getInstance();var s=hudState;int width=Math.min(174,g.guiWidth()/3),x=10,base=Math.max(28,g.guiHeight()/2-70);
        float rpg=DynamicHud.alpha(DynamicHud.Part.RPG),quest=DynamicHud.alpha(DynamicHud.Part.QUEST);
        if(rpg>0){
            HudOpacity.begin(rpg);int accent=ClassSelectionScreen.accent(RpgClass.values()[s.rpgClass()]);
            g.fill(x,base,x+width,base+57,0xDA14202C);g.fill(x,base,x+2,base+57,accent);
            SkillGlyphs.draw(g,RpgClass.values()[s.rpgClass()],0,x+8,base+8,20,accent);
            g.drawString(mc.font,mc.font.plainSubstrByWidth(RpgClass.values()[s.rpgClass()].label(),width-65),x+35,base+8,accent);
            g.drawString(mc.font,"Nv. "+s.level()+(s.debug()?" • DEBUG":s.testing()?" • TESTE":""),x+35,base+20,RpgUi.TEXT);
            double fraction=s.level()>=100?1:Math.clamp(s.xp()/(double)Math.max(100,s.level()*100),0,1);
            g.fill(x+9,base+35,x+width-9,base+38,0xFF2A3B4B);g.fill(x+9,base+35,x+9+(int)((width-18)*fraction),base+38,accent);
            String xp=s.level()>=100?"Nível máximo":"XP "+s.xp()+" / "+(s.level()*100);
            g.drawString(mc.font,xp,x+9,base+44,RpgUi.MUTED);
            if(resourceState!=null&&resourceState.points()>0){String points="+"+resourceState.points()+" pts";g.drawString(mc.font,points,x+width-9-mc.font.width(points),base+20,RpgUi.GOLD);}
            HudOpacity.end();
        }
        if(quest>0){
            HudOpacity.begin(quest);int y=base+65;var q=QuestCatalog.quest(s.questStage());var first=q.targets().getFirst();
            g.fill(x,y,x+width,y+58,0xDA14202C);g.fill(x,y,x+2,y+58,RpgUi.GOLD);
            g.drawString(mc.font,"OBJETIVO",x+9,y+8,RpgUi.GOLD);g.drawString(mc.font,"Caçada "+s.kills()%10+"/10",x+9,y+22,RpgUi.TEXT);
            String target=first.label()+" "+s.questFirst()+"/"+first.amount();g.drawString(mc.font,mc.font.plainSubstrByWidth(target,width-18),x+9,y+34,RpgUi.MUTED);
            String second=q.targets().size()>1?q.targets().get(1).label()+" "+s.questSecond()+"/"+q.targets().get(1).amount():"+"+q.reward()+" XP";
            g.drawString(mc.font,mc.font.plainSubstrByWidth(second,width-18),x+9,y+46,RpgUi.MUTED);HudOpacity.end();
        }
    }
    private static void potionInput(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||hudState==null||hudState.rpgClass()!=RpgClass.XAMA.ordinal()
            ||effectUntil[2]<=mc.level.getGameTime()||!mc.player.getMainHandItem().isEmpty()||!mc.player.getOffhandItem().isEmpty())return;
        if(!event.isAttack()&&!event.isUseItem())return;
        event.setCanceled(true);event.setSwingHand(false);
        if(event.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND)mc.player.connection.send(aimedRequest(event.isUseItem()?9:8,0));
    }
    public static void cast(int slot) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        DynamicHud.signal(DynamicHud.ability(slot));DynamicHud.signal(DynamicHud.Part.MANA);
        int side = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);
        int forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        int direction = side == 0 && forward == 0 ? 0 : Math.floorMod((int)Math.round(Math.atan2(side, forward) / (Math.PI / 4)), 8);
        mc.player.connection.send(aimedRequest(slot == 0 ? 1 : slot == 1 ? 5 : 6,direction));
    }
    private static RpgNetwork.Request aimedRequest(int action,int direction){
        var mc=Minecraft.getInstance();var target=TargetLock.mouseLocked()?TargetLock.target():null;
        var point=ShoulderCamera.abilityPoint(mc.gameRenderer.getMainCamera().getPartialTickTime());
        return new RpgNetwork.Request(action,direction,target==null?-1:target.getId(),point.x,point.y,point.z);
    }
}
