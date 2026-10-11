package dev.emersonoxy.rpg;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

/** Authoritative settings are SERVER configs, synchronized by NeoForge. */
public final class RpgConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MISS,CRITICAL,CRITICAL_DAMAGE,ENEMY_MISS,ENEMY_CRITICAL,ENEMY_CRITICAL_DAMAGE,PLAYER_DAMAGE,MOB_HEALTH,MOB_DAMAGE,ELITE_HEALTH,ELITE_DAMAGE,ELITE_CHANCE,MANA_COST,COOLDOWN,VILLAGE_LOOT;
    public static final ModConfigSpec.IntValue DODGE_RECHARGE,DODGE_SECOND,DODGE_THIRD,PARRY_WINDOW;
    public static final ModConfigSpec.BooleanValue PROFILING,RAIDER_PATROLS,ENEMY_TACTICS;
    static {
        var b=new ModConfigSpec.Builder();
        b.push("combat");
        ENEMY_MISS=decimal(b,"enemyMissChance",.05,0,1);ENEMY_CRITICAL=decimal(b,"enemyCriticalChance",.10,0,1);ENEMY_CRITICAL_DAMAGE=decimal(b,"enemyCriticalDamage",1.5,1,10);
        MISS=decimal(b,"missChance",.05,0,1);CRITICAL=decimal(b,"criticalChance",.10,0,1);CRITICAL_DAMAGE=decimal(b,"criticalDamage",1.5,1,10);PLAYER_DAMAGE=decimal(b,"playerDamageMultiplier",1,.1,10);PARRY_WINDOW=integer(b,"parryWindowTicks",6,1,20);b.pop();
        b.push("evasion");DODGE_RECHARGE=integer(b,"rechargeSeconds",6,1,60);DODGE_SECOND=integer(b,"secondChargeLevel",25,1,100);DODGE_THIRD=integer(b,"thirdChargeLevel",50,1,100);b.pop();
        b.push("difficulty");ENEMY_TACTICS=b.translation("oxyrpg.config.enemyTactics").define("enemyTactics",true);MOB_HEALTH=decimal(b,"mobHealthMultiplier",1,.1,20);MOB_DAMAGE=decimal(b,"mobDamageMultiplier",1,.1,20);ELITE_HEALTH=decimal(b,"eliteHealthMultiplier",1,.1,20);ELITE_DAMAGE=decimal(b,"eliteDamageMultiplier",1,.1,20);ELITE_CHANCE=decimal(b,"eliteChance",.05,0,1);b.pop();
        b.push("skills");MANA_COST=decimal(b,"manaCostMultiplier",1,0,10);COOLDOWN=decimal(b,"cooldownMultiplier",1,.1,10);b.pop();
        b.push("exploration");RAIDER_PATROLS=b.translation("oxyrpg.config.raiderPatrols").define("raiderPatrols",true);VILLAGE_LOOT=decimal(b,"villageLootMultiplier",1,0,2);b.pop();
        b.push("development");PROFILING=b.translation("oxyrpg.config.profiling").define("profiling",false);b.pop();SPEC=b.build();
    }
    private RpgConfig(){}
    private static ModConfigSpec.DoubleValue decimal(ModConfigSpec.Builder b,String name,double initial,double min,double max){return b.translation("oxyrpg.config."+name).defineInRange(name,initial,min,max);}
    private static ModConfigSpec.IntValue integer(ModConfigSpec.Builder b,String name,int initial,int min,int max){return b.translation("oxyrpg.config."+name).defineInRange(name,initial,min,max);}
    public static void register(ModContainer container){container.registerConfig(ModConfig.Type.SERVER,SPEC);}
    public static double value(ModConfigSpec.DoubleValue v,double fallback){return SPEC.isLoaded()?v.get():fallback;}
    public static int value(ModConfigSpec.IntValue v,int fallback){return SPEC.isLoaded()?v.get():fallback;}
    public static boolean tactics(){return !SPEC.isLoaded()||ENEMY_TACTICS.get();}
    public static boolean patrols(){return !SPEC.isLoaded()||RAIDER_PATROLS.get();}
    public static boolean profiling(){return SPEC.isLoaded()&&PROFILING.get();}
    public static int charges(int level){int second=value(DODGE_SECOND,25),third=Math.max(second,value(DODGE_THIRD,50));return level>=third?3:level>=second?2:1;}
    public static int recharge(){return value(DODGE_RECHARGE,6)*20;}
    public static double miss(int luck){double base=value(MISS,.05);return Math.max(Math.min(base,.01),base-luck*.001);}
    public static double critical(int luck){return Math.min(1,value(CRITICAL,.1)+luck*.0025);}
}
