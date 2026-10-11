package dev.emersonoxy.rpg.client;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.slf4j.LoggerFactory;

public final class DynamicHud {
    public enum Mode { ALWAYS("Sempre visível"), DYNAMIC("Dinâmica"), HIDDEN("Oculta");
        final String label;Mode(String label){this.label=label;}}
    public enum Part {
        HEALTH("Vida"), ARMOR("Armadura"), FOOD("Fome"), AIR("Ar"), HOTBAR("Hotbar"),
        XP("XP vanilla"), EFFECTS("Efeitos"), VEHICLE("Vida da montaria"),
        RPG("Nível e XP RPG"), QUEST("Missões"), MANA("Mana"), HEAT("Calor"),
        ABILITY_1("Habilidade 1"), ABILITY_2("Habilidade 2"), ABILITY_3("Habilidade 3"), EVASION("Esquiva"), MINIMAP("Minimapa");
        public final String label;Part(String label){this.label=label;}}
    private static final Map<ResourceLocation,Part> LAYERS=Map.ofEntries(
        Map.entry(VanillaGuiLayers.PLAYER_HEALTH,Part.HEALTH),Map.entry(VanillaGuiLayers.ARMOR_LEVEL,Part.ARMOR),
        Map.entry(VanillaGuiLayers.FOOD_LEVEL,Part.FOOD),Map.entry(VanillaGuiLayers.AIR_LEVEL,Part.AIR),
        Map.entry(VanillaGuiLayers.HOTBAR,Part.HOTBAR),Map.entry(VanillaGuiLayers.SELECTED_ITEM_NAME,Part.HOTBAR),
        Map.entry(VanillaGuiLayers.EXPERIENCE_LEVEL,Part.XP),Map.entry(VanillaGuiLayers.CONTEXTUAL_INFO_BAR,Part.XP),
        Map.entry(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,Part.XP),
        Map.entry(VanillaGuiLayers.EFFECTS,Part.EFFECTS),Map.entry(VanillaGuiLayers.VEHICLE_HEALTH,Part.VEHICLE));
    private static final EnumSet<Part> enabled=EnumSet.allOf(Part.class);
    private static final long[] until=new long[Part.values().length];
    private static final double[] opacity=new double[Part.values().length];
    private static final long[] frames=new long[Part.values().length];
    private static Mode mode=Mode.DYNAMIC,panels=Mode.DYNAMIC;
    private static boolean loaded,initialized;
    private static long ticks;
    private static float health,absorption,experience,vehicleHealth;
    private static int food,armor,slot,level;
    private static net.minecraft.world.item.Item mainItem,offhandItem;
    private DynamicHud(){}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-hud.properties");}
    private static void load(){
        if(loaded)return;loaded=true;
        if(!Files.exists(file()))return;
        try(var in=Files.newBufferedReader(file())){
            var p=new Properties();p.load(in);
            try{mode=Mode.valueOf(p.getProperty("mode","DYNAMIC"));}catch(IllegalArgumentException ignored){}
            try{panels=Mode.valueOf(p.getProperty("panels","DYNAMIC"));}catch(IllegalArgumentException ignored){}
            for(var part:Part.values())if(!Boolean.parseBoolean(p.getProperty(part.name(),"true")))enabled.remove(part);
        }catch(IOException e){LoggerFactory.getLogger(DynamicHud.class).warn("Não foi possível ler opções de HUD",e);}
    }
    private static void save(){
        try{
            Files.createDirectories(file().getParent());var p=new Properties();p.setProperty("mode",mode.name());p.setProperty("panels",panels.name());
            for(var part:Part.values())p.setProperty(part.name(),""+enabled.contains(part));
            var temp=Files.createTempFile(file().getParent(),"oxyrpg-hud-",".tmp");
            try{
                try(var out=Files.newBufferedWriter(temp)){p.store(out,"Oxy RPG HUD");}
                try{Files.move(temp,file(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
                catch(AtomicMoveNotSupportedException e){Files.move(temp,file(),StandardCopyOption.REPLACE_EXISTING);}
            }finally{Files.deleteIfExists(temp);}
        }catch(IOException e){LoggerFactory.getLogger(DynamicHud.class).warn("Não foi possível salvar opções de HUD",e);
            var p=Minecraft.getInstance().player;if(p!=null)p.displayClientMessage(Component.literal("Falha ao salvar opções de HUD."),true);}
    }
    public static Mode mode(){load();return mode;}
    public static Mode panelsMode(){load();return panels;}
    public static String panelsLabel(){return panelsMode().label;}
    public static void cyclePanels(){load();panels=Mode.values()[(panels.ordinal()+1)%3];for(var part:new Part[]{Part.RPG,Part.QUEST}){opacity[part.ordinal()]=0;signal(part);}save();var p=Minecraft.getInstance().player;if(p!=null)p.displayClientMessage(Component.literal("Painéis: "+panels.label),true);}
    public static String modeLabel(){return mode().label;}
    public static boolean enabled(Part part){load();return enabled.contains(part);}
    public static void toggle(Part part){load();if(!enabled.remove(part))enabled.add(part);save();}
    public static void cycle(){load();mode=Mode.values()[(mode.ordinal()+1)%Mode.values().length];for(var part:Part.values())if(part!=Part.RPG&&part!=Part.QUEST){if(mode==Mode.HIDDEN)opacity[part.ordinal()]=0;frames[part.ordinal()]=System.nanoTime();signal(part);}save();
        var p=Minecraft.getInstance().player;if(p!=null)p.displayClientMessage(Component.literal("HUD: "+mode.label),true);}
    public static void defaults(){load();mode=panels=Mode.DYNAMIC;enabled.clear();enabled.addAll(EnumSet.allOf(Part.class));save();revealAll();}
    public static void signal(Part part){until[part.ordinal()]=ticks+80;}
    public static void revealAll(){for(var part:Part.values())signal(part);}
    public static Part ability(int slot){return Part.values()[Part.ABILITY_1.ordinal()+slot];}
    public static float alpha(Part part){
        load();var mc=Minecraft.getInstance();
        int index=part.ordinal();long frame=System.nanoTime();
        double dt=frames[index]==0||mc.isPaused()?0:Math.min(.1,(frame-frames[index])/1e9);frames[index]=frame;
        Mode visibility=part==Part.RPG||part==Part.QUEST?panels:mode;
        if(!enabled.contains(part)||visibility==Mode.HIDDEN){opacity[index]=0;return 0;}
        double now=ticks+(mc.isPaused()?0:mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        double desired=visibility==Mode.ALWAYS||OxyRpgClient.REVEAL_HUD.isDown()?1:Math.clamp((until[index]+24-now)/24,0,1);
        opacity[index]=CameraMotion.smooth(opacity[index],desired,12,dt);
        if(desired==0&&opacity[index]<.004)opacity[index]=0;
        return (float)opacity[index];
    }
    public static void reset(){initialized=false;ticks=0;Arrays.fill(until,0);Arrays.fill(opacity,0);Arrays.fill(frames,0);HudOpacity.end();revealAll();}
    public static void tick(){
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.level==null||mc.isPaused())return;ticks++;
        if(!initialized){initialized=true;revealAll();}
        if(p.getHealth()!=health||p.getAbsorptionAmount()!=absorption||p.hurtTime>0||p.getHealth()<=p.getMaxHealth()*.25)signal(Part.HEALTH);
        if(p.getArmorValue()!=armor||p.hurtTime>0)signal(Part.ARMOR);
        if(p.getFoodData().getFoodLevel()!=food||p.getFoodData().getFoodLevel()<=6)signal(Part.FOOD);
        if(p.getAirSupply()<p.getMaxAirSupply()||p.isUnderWater())signal(Part.AIR);
        if(p.experienceProgress!=experience||p.experienceLevel!=level)signal(Part.XP);
        if(p.isPassenger()&&mc.options.keyJump.isDown())signal(Part.XP);
        if(!p.getActiveEffects().isEmpty())signal(Part.EFFECTS);
        var vehicle=p.getVehicle();float vh=vehicle instanceof LivingEntity living?living.getHealth():0;
        if(vehicle!=null&&(vh!=vehicleHealth||p.hurtTime>0))signal(Part.VEHICLE);
        if(p.getInventory().getSelectedSlot()!=slot||p.getMainHandItem().getItem()!=mainItem||p.getOffhandItem().getItem()!=offhandItem)signal(Part.HOTBAR);
        mainItem=p.getMainHandItem().getItem();offhandItem=p.getOffhandItem().getItem();
        health=p.getHealth();absorption=p.getAbsorptionAmount();armor=p.getArmorValue();food=p.getFoodData().getFoodLevel();
        slot=p.getInventory().getSelectedSlot();experience=p.experienceProgress;level=p.experienceLevel;vehicleHealth=vh;
        for(int i=0;i<3;i++)if(OxyRpgClient.abilityActive(i))signal(ability(i));
    }
    public static void beforeLayer(RenderGuiLayerEvent.Pre event){
        HudOpacity.end();var part=LAYERS.get(event.getName());
        if(mode()==Mode.HIDDEN&&(event.getName().equals(VanillaGuiLayers.CROSSHAIR)||event.getName().equals(VanillaGuiLayers.BOSS_OVERLAY))){event.setCanceled(true);return;}
        if(part!=null)HudOpacity.begin(alpha(part));
    }
    public static void afterLayer(RenderGuiLayerEvent.Post event){HudOpacity.end();}
}
