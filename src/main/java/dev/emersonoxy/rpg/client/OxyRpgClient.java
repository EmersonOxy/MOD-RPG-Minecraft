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
    public static final KeyMapping OPEN_MENU = new KeyMapping("key.oxyrpg.menu", GLFW.GLFW_KEY_R, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping TOGGLE_HUD = new KeyMapping("key.oxyrpg.hud", GLFW.GLFW_KEY_H, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping SECOND_ABILITY = new KeyMapping("key.oxyrpg.ability2", GLFW.GLFW_KEY_V, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping THIRD_ABILITY = new KeyMapping("key.oxyrpg.ability3", GLFW.GLFW_KEY_B, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping USE_ABILITY = new KeyMapping("key.oxyrpg.ability", GLFW.GLFW_KEY_G, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping CAMERA_OPTIONS = new KeyMapping("key.oxyrpg.camera", GLFW.GLFW_KEY_O, KeyMapping.Category.GAMEPLAY); public static final KeyMapping SWAP_SHOULDER = new KeyMapping("key.oxyrpg.shoulder", GLFW.GLFW_KEY_J, KeyMapping.Category.GAMEPLAY); public static final KeyMapping LOCK_TARGET=new KeyMapping("key.oxyrpg.lock",GLFW.GLFW_KEY_Z,KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping NEXT_TARGET=new KeyMapping("key.oxyrpg.next_target",GLFW.GLFW_KEY_N,KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping[] CAMERA_POSITION={
        new KeyMapping("key.oxyrpg.camera_far",GLFW.GLFW_KEY_KP_ADD,KeyMapping.Category.GAMEPLAY),
        new KeyMapping("key.oxyrpg.camera_near",GLFW.GLFW_KEY_KP_SUBTRACT,KeyMapping.Category.GAMEPLAY),
        new KeyMapping("key.oxyrpg.camera_side_out",GLFW.GLFW_KEY_KP_6,KeyMapping.Category.GAMEPLAY),
        new KeyMapping("key.oxyrpg.camera_side_in",GLFW.GLFW_KEY_KP_4,KeyMapping.Category.GAMEPLAY),
        new KeyMapping("key.oxyrpg.camera_up",GLFW.GLFW_KEY_KP_8,KeyMapping.Category.GAMEPLAY),
        new KeyMapping("key.oxyrpg.camera_down",GLFW.GLFW_KEY_KP_2,KeyMapping.Category.GAMEPLAY)};
    private static RpgNetwork.State hudState;
    private static RpgNetwork.Resources resourceState;
    private static RpgNetwork.ProfessionState professionState;
    public static RpgNetwork.ProfessionState professions(){return professionState;}
    public static RpgNetwork.Resources resources(){return resourceState;}
    private static final long[] hudReadyAt = new long[3];
    private static final long[] effectUntil = new long[3];
    private static final int[] effectTotal = new int[3], effectPhase = new int[3], cooldownTotal = new int[3];
    private static final net.minecraft.world.item.ItemStack[] skillIcons = new net.minecraft.world.item.ItemStack[3];
    private static LocalPlayer trackedPlayer;
    private static ClientLevel trackedLevel;
    private static boolean showHud = true;
    private static int refreshTicks;

    public OxyRpgClient(IEventBus bus) {
        if(Boolean.getBoolean("oxyrpg.cameraQA"))NeoForge.EVENT_BUS.addListener(CameraClientQA::tick);
        bus.addListener(OxyRpgClient::registerKeys);
        bus.addListener(OxyRpgClient::registerPayloads);
        bus.addListener(OxyRpgClient::registerHud);
        NeoForge.EVENT_BUS.addListener(OxyRpgClient::tick); NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderHandEvent event)->{if(ShoulderCamera.hideHands())event.setCanceled(true);});
        NeoForge.EVENT_BUS.addListener(OxyRpgClient::potionInput);
        bus.addListener(EnemyNameplates::register);
        NeoForge.EVENT_BUS.addListener(EnemyNameplates::player);
        NeoForge.EVENT_BUS.addListener(EnemyNameplates::hand);
        NeoForge.EVENT_BUS.addListener(EnemyNameplates::plate);
    }
    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(LOCK_TARGET);event.register(NEXT_TARGET);for(var key:CAMERA_POSITION)event.register(key); event.register(OPEN_MENU); event.register(CAMERA_OPTIONS); event.register(SWAP_SHOULDER);
        event.register(TOGGLE_HUD);
        event.register(USE_ABILITY);
        event.register(SECOND_ABILITY);
        event.register(THIRD_ABILITY);
    }
    private static void registerHud(RegisterGuiLayersEvent event) {
        event.registerBelow(net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,ResourceLocation.fromNamespaceAndPath("oxyrpg","mana_space"),(g,d)->{
            var mc=Minecraft.getInstance();if(resourceState!=null&&hudState!=null&&hudState.rpgClass()!=0&&showHud&&!mc.options.hideGui&&mc.gameMode.canHurtPlayer())mc.gui.leftHeight+=10;
        });
        event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,ResourceLocation.fromNamespaceAndPath("oxyrpg","mana"),(g,d)->renderMana(g));
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("oxyrpg", "hud"), (graphics, delta) -> renderHud(graphics));
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("oxyrpg", "shoulder_crosshair"),(g,d)->{
            var mc=Minecraft.getInstance();if(!ShoulderCamera.active()||mc.options.hideGui||mc.screen!=null||mc.player.isSpectator())return;
            var pipeline=net.minecraft.client.renderer.RenderPipelines.CROSSHAIR;
            var point=ShoulderCamera.aimPoint(mc.gameRenderer.getMainCamera().getPartialTickTime());
            var projection=mc.gameRenderer.projectPointToScreen(point);
            if(!Double.isFinite(projection.x)||!Double.isFinite(projection.y)||projection.z<-1||projection.z>1)return;
            int cx=(int)((projection.x+1)*g.guiWidth()/2),cy=(int)((1-projection.y)*g.guiHeight()/2);
            g.blitSprite(pipeline,ResourceLocation.withDefaultNamespace("hud/crosshair"),cx-7,cy-7,15,15);
            var target=TargetLock.target();if(target!=null){
                g.drawString(mc.font,"[ "+target.getName().getString()+" ]",cx-6,cy+20,0xFFFFD36B);
            }
            if(mc.options.attackIndicator().get()==net.minecraft.client.AttackIndicatorStatus.CROSSHAIR){
                float strength=mc.player.getAttackStrengthScale(0);int x=cx-8,y=cy+9;
                if(strength<1){g.blitSprite(pipeline,ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background"),x,y,16,4);
                    g.blitSprite(pipeline,ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress"),16,4,0,0,x,y,(int)(strength*17),4);}
            }
        });
        
    }
    private static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(RpgNetwork.ProfessionState.TYPE,(value,context)->{professionState=value;dev.emersonoxy.rpg.EquipmentAccess.clientXp(value.mining(),value.farming(),value.arsenal());var mc=Minecraft.getInstance();if(mc.screen instanceof ProfessionsScreen screen)screen.update(value);});
        event.register(RpgNetwork.VillagerMark.TYPE,(value,context)->EnemyNameplates.villagerMark(value.entityId(),value.state()));
        event.register(RpgNetwork.EliteInfo.TYPE,(value,context)->EnemyNameplates.eliteTitle(value.entityId(),value.title()));
        event.register(RpgNetwork.VillagerDialogue.TYPE,(value,context)->Minecraft.getInstance().setScreen(new VillagerDialogueScreen(value)));
        event.register(RpgNetwork.Resources.TYPE,(value,context)->{
            resourceState=value;var mc=Minecraft.getInstance();
            if(mc.screen instanceof AttributesScreen screen)screen.update(value);
        });
        event.register(RpgNetwork.MobLevel.TYPE, (value, context) -> EnemyNameplates.mobLevel(value.entityId(), value.level()));
        event.register(RpgNetwork.AbilityStatus.TYPE, (value, context) -> {
            var mc=Minecraft.getInstance();long time=mc.level==null?0:mc.level.getGameTime();
            for(int slot=0;slot<3;slot++){effectUntil[slot]=value.remaining(slot)==-1?Long.MAX_VALUE:time+value.remaining(slot);effectTotal[slot]=value.total(slot);effectPhase[slot]=value.phase(slot);}
        });
        event.register(RpgNetwork.Ghost.TYPE, (ghost, context) -> EnemyNameplates.ghost(ghost.entityId(), ghost.ticks()));
        event.register(RpgNetwork.State.TYPE, (state, context) -> {
            var mc = Minecraft.getInstance();
            hudState = state;
            dev.emersonoxy.rpg.EquipmentAccess.clientClass(state.rpgClass());
            dev.emersonoxy.rpg.EquipmentAccess.clientLevel(state.level());
            if(state.rpgClass()!=0)for(int slot=0;slot<3;slot++){
                cooldownTotal[slot]=new dev.emersonoxy.rpg.RpgProfile(RpgClass.values()[state.rpgClass()],100,0,0,0,state.skills()).abilityCooldown(slot);
                int node=slot*7+(state.hasSkill(slot*7+3)?3:1);
                var icon=dev.emersonoxy.rpg.SkillTree.nodes(RpgClass.values()[state.rpgClass()]).get(node).icon();
                var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(icon));
                skillIcons[slot]=new net.minecraft.world.item.ItemStack(item==null?net.minecraft.world.item.Items.BOOK:item);
            }
            for (int slot = 0; slot < 3; slot++) hudReadyAt[slot] = (mc.level == null ? 0 : mc.level.getGameTime()) + state.cooldown(slot);
            if (mc.screen instanceof RpgScreen screen) screen.update(state);
            if (mc.screen instanceof SkillTreeScreen screen) screen.update(state);
            if (mc.screen instanceof MissionScreen screen) screen.update(state);
        });
    }
    private static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player != trackedPlayer || mc.level != trackedLevel) {
            ShoulderCamera.reset();TargetLock.clear(); trackedPlayer = mc.player;
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
        TargetLock.tick();
        while(CAMERA_OPTIONS.consumeClick())if(mc.player!=null&&mc.screen==null)mc.setScreen(new CameraSettingsScreen(null)); while(SWAP_SHOULDER.consumeClick())if(mc.player!=null&&mc.screen==null)ShoulderCamera.swap(); while (TOGGLE_HUD.consumeClick()) {
            if (mc.player != null && mc.screen == null) showHud = !showHud;
        }
        while (USE_ABILITY.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.screen == null) cast(0);
        }
        while (SECOND_ABILITY.consumeClick()) if (mc.player != null && mc.screen == null) cast(1);
        while (THIRD_ABILITY.consumeClick()) if (mc.player != null && mc.screen == null) cast(2);
        while (OPEN_MENU.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new RpgScreen());
        }
    }

    private static void renderHud(GuiGraphics g) {
        var mc = Minecraft.getInstance();
        if (!showHud || mc.options.hideGui || mc.player == null || mc.level == null || mc.screen != null || hudState == null) return;
        renderAbilityIcons(g);
        int x = 8, y = 8, width = Math.min(192, g.guiWidth() - 16);
        var font = mc.font;
        var state = hudState;
        g.fill(x, y, x + width, y + 147, 0xB018202C);
        g.fill(x, y, x + 2, y + 147, 0xFF53D5BF);
        String header = RpgClass.values()[state.rpgClass()].label() + " • Nv. " + state.level() + (state.debug() ? " • DEBUG" : "");
        g.drawString(font, font.plainSubstrByWidth(header, width - 16), x + 8, y + 6, state.debug() ? 0xFFFFCC79 : 0xFF53D5BF);
        int next = state.level() == 100 ? 0 : state.level() * 100;
        g.drawString(font, next == 0 ? "Nível máximo" : "XP " + state.xp() + "/" + next, x + 8, y + 19, 0xFFFFFFFF);
        g.fill(x + 8, y + 30, x + width - 8, y + 34, 0xFF313B4B);
        int filled = next == 0 ? width - 16 : (width - 16) * state.xp() / next;
        g.fill(x + 8, y + 30, x + 8 + filled, y + 34, 0xFF53D5BF);
        g.drawString(font, "Caçada " + state.kills() % 10 + "/10 • +100 XP", x + 8, y + 39, 0xFFE1EAF0);
        var quest = QuestCatalog.quest(state.questStage());
        var first = quest.targets().get(0);
        g.drawString(font, "Missão: " + first.label() + " " + state.questFirst() + "/" + first.amount(), x + 8, y + 53, 0xFFB7C8D7);
        String second = quest.targets().size() == 1 ? "Recompensa: +" + quest.reward() + " XP" : quest.targets().get(1).label()
            + " " + state.questSecond() + "/" + quest.targets().get(1).amount();
        g.drawString(font, second, x + 8, y + 66, 0xFFB7C8D7);
        if(resourceState!=null)g.drawString(font,"Mana "+resourceState.mana()+"/"+resourceState.maxMana()+" • Atributos "+resourceState.points(),x+8,y+118,0xFF70BAFF);
        var keys = new KeyMapping[] {USE_ABILITY, SECOND_ABILITY, THIRD_ABILITY};
        for (int slot = 0; slot < 3; slot++) {
            long remaining = Math.max(0, hudReadyAt[slot] - mc.level.getGameTime());
            String name = state.rpgClass() == 0 ? "Habilidade " + (slot + 1) : dev.emersonoxy.rpg.SkillTree.abilityName(RpgClass.values()[state.rpgClass()], slot);
            String status = !state.canUse(slot) ? "bloqueada" : state.debug() ? "DEBUG" : remaining == 0 ? "pronta" : ((remaining + 19) / 20) + " s";
            g.drawString(font, font.plainSubstrByWidth(keys[slot].getTranslatedKeyMessage().getString() + " " + name + " • " + status, width - 16),
                x + 8, y + 79 + slot * 13, remaining == 0 && state.canUse(slot) ? 0xFFFFCC79 : 0xFFB7C8D7);
        }
        g.drawString(font, OPEN_MENU.getTranslatedKeyMessage().getString() + " menu • " + TOGGLE_HUD.getTranslatedKeyMessage().getString() + " HUD", x + 8, y + 131, 0xFFB7C8D7);
    }
    private static void renderMana(GuiGraphics g){
        var mc=Minecraft.getInstance();var r=resourceState;
        if(!showHud||mc.options.hideGui||mc.screen!=null||mc.player==null||r==null||hudState==null||hudState.rpgClass()==0)return;
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

    }
    private static void renderAbilityIcons(GuiGraphics g) {
        var mc=Minecraft.getInstance();var state=hudState;
        if(state==null||state.rpgClass()==0)return;
        int start=g.guiWidth()/2-92,y=g.guiHeight()-Math.max(89,mc.gui.leftHeight+43);
        var keys=new KeyMapping[]{USE_ABILITY,SECOND_ABILITY,THIRD_ABILITY};
        for(int slot=0;slot<3;slot++){
            int x=start+slot*62;long remaining=effectUntil[slot]==Long.MAX_VALUE?Long.MAX_VALUE:Math.max(0,effectUntil[slot]-mc.level.getGameTime());
            long cooldown=Math.max(0,hudReadyAt[slot]-mc.level.getGameTime());
            int phase=remaining>0?effectPhase[slot]:0;
            boolean available=state.canUse(slot);
            int accent=phase==1||phase==2?0xFFFFB74D:phase>0?0xFF53D5BF:0xFF707D90;
            g.fill(x,y,x+58,y+39,0xB018202C);
            g.fill(x,y,x+58,y+1,accent);
            if(skillIcons[slot]!=null)g.renderFakeItem(skillIcons[slot],x+5,y+5);
            if(!available)g.fill(x+4,y+4,x+22,y+22,0xAA101820);
            g.drawString(mc.font,keys[slot].getTranslatedKeyMessage().getString(),x+27,y+5,0xFFFFFFFF);
            String label=!available?"Bloq.":phase==6?"Aliados":phase==1?"Projétil":phase==2?"Canal":phase==4?"Área":phase==5?"Efeito":phase>0?"Ativa":cooldown>0?((cooldown+19)/20)+" s":"Pronta";
            g.drawString(mc.font,mc.font.plainSubstrByWidth(label,52),x+3,y+26,accent);
            g.fill(x+3,y+21,x+55,y+24,0xFF313B4B);
            if(phase>0){
                float fraction=remaining==Long.MAX_VALUE?1:Math.clamp((float)remaining/Math.max(1,effectTotal[slot]),0,1);
                if(phase==1||phase==2)fraction=1-fraction;
                g.fill(x+3,y+21,x+3+(int)(52*fraction),y+24,accent);
                if(remaining!=Long.MAX_VALUE)g.drawString(mc.font,((remaining+19)/20)+"s",x+27,y+13,0xFFE1EAF0);
            } else if(available){
                float charge=1-Math.clamp((float)cooldown/Math.max(1,cooldownTotal[slot]),0,1);
                g.fill(x+3,y+21,x+3+(int)(52*charge),y+24,cooldown==0?0xFF53D5BF:0xFF707D90);
            }
        }
    }
    private static void potionInput(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||hudState==null||hudState.rpgClass()!=RpgClass.XAMA.ordinal()
            ||effectUntil[2]<=mc.level.getGameTime()||!mc.player.getMainHandItem().isEmpty()||!mc.player.getOffhandItem().isEmpty())return;
        if(!event.isAttack()&&!event.isUseItem())return;
        event.setCanceled(true);event.setSwingHand(false);
        if(event.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND)mc.player.connection.send(new RpgNetwork.Request(event.isUseItem()?9:8));
    }
    public static void cast(int slot) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int side = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);
        int forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        int direction = side == 0 && forward == 0 ? 0 : Math.floorMod((int)Math.round(Math.atan2(side, forward) / (Math.PI / 4)), 8);
        mc.player.connection.send(new RpgNetwork.Request(slot == 0 ? 1 : slot == 1 ? 5 : 6, direction));
    }
}






