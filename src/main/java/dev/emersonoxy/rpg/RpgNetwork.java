package dev.emersonoxy.rpg;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class RpgNetwork {
    private RpgNetwork() {}

    public record DebugState(int mode,int level,int rpgClass,boolean allSkills,boolean cooldowns,boolean resources,boolean attributes,boolean arsenal,boolean allowed) implements CustomPacketPayload {
        public static final Type<DebugState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","debug_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf,DebugState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.mode);b.writeVarInt(v.level);b.writeVarInt(v.rpgClass);b.writeBoolean(v.allSkills);b.writeBoolean(v.cooldowns);b.writeBoolean(v.resources);b.writeBoolean(v.attributes);b.writeBoolean(v.arsenal);b.writeBoolean(v.allowed);},b->new DebugState(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean()));
        @Override public Type<DebugState> type(){return TYPE;}
    }
    public record BloodFx(double x,double y,double z,float dx,float dy,float dz,float damage) implements CustomPacketPayload {
        public static final Type<BloodFx> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","blood_fx"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BloodFx> CODEC=StreamCodec.of((b,v)->{b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeFloat(v.dx);b.writeFloat(v.dy);b.writeFloat(v.dz);b.writeFloat(v.damage);},b->new BloodFx(b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat()));
        @Override public Type<BloodFx> type(){return TYPE;}
    }
    public record CastFx(int entity,int kind,int slot,int duration) implements CustomPacketPayload {
        public static final Type<CastFx> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","cast_fx"));
        public static final StreamCodec<RegistryFriendlyByteBuf,CastFx> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entity);b.writeVarInt(v.kind);b.writeVarInt(v.slot);b.writeVarInt(v.duration);},b->new CastFx(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<CastFx> type(){return TYPE;}
    }
    public record ImpactFx(double x,double y,double z,float radius,int kind) implements CustomPacketPayload {
        public static final Type<ImpactFx> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","impact_fx"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ImpactFx> CODEC=StreamCodec.of((b,v)->{b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeFloat(v.radius);b.writeVarInt(v.kind);},b->new ImpactFx(b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readVarInt()));
        @Override public Type<ImpactFx> type(){return TYPE;}
    }
    public record DisguiseState(int entity,int source,String mob,int ticks) implements CustomPacketPayload {
        public static final Type<DisguiseState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","disguise"));
        public static final StreamCodec<RegistryFriendlyByteBuf,DisguiseState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entity);b.writeVarInt(v.source);b.writeUtf(v.mob,128);b.writeVarInt(v.ticks);},b->new DisguiseState(b.readVarInt(),b.readVarInt(),b.readUtf(128),b.readVarInt()));
        @Override public Type<DisguiseState> type(){return TYPE;}
    }
    public record EntityMotion(int entity,int kind,int ticks) implements CustomPacketPayload {
        public static final Type<EntityMotion> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","entity_motion"));
        public static final StreamCodec<RegistryFriendlyByteBuf,EntityMotion> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entity);b.writeVarInt(v.kind);b.writeVarInt(v.ticks);},b->new EntityMotion(b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<EntityMotion> type(){return TYPE;}
    }
    public record VampireState(boolean bat,int twilight,int bats,int meals) implements CustomPacketPayload {
        public static final Type<VampireState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","vampire"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VampireState> CODEC=StreamCodec.of((b,v)->{b.writeBoolean(v.bat);b.writeVarInt(v.twilight);b.writeVarInt(v.bats);b.writeVarInt(v.meals);},b->new VampireState(b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<VampireState> type(){return TYPE;}
    }
    public record SpecialState(int phase,int ticks,float heat,int overheated,boolean waterwalk) implements CustomPacketPayload {
        public static final Type<SpecialState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","special"));
        public static final StreamCodec<RegistryFriendlyByteBuf,SpecialState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.phase);b.writeVarInt(v.ticks);b.writeFloat(v.heat);b.writeVarInt(v.overheated);b.writeBoolean(v.waterwalk);},b->new SpecialState(b.readVarInt(),b.readVarInt(),b.readFloat(),b.readVarInt(),b.readBoolean()));
        @Override public Type<SpecialState> type(){return TYPE;}
    }
    public record ContentState(String json) implements CustomPacketPayload {
        public static final Type<ContentState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","content"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ContentState> CODEC=StreamCodec.of((b,v)->b.writeUtf(v.json,262144),b->new ContentState(b.readUtf(262144)));
        @Override public Type<ContentState> type(){return TYPE;}
    }
    public record CarryState(int player,int mob) implements CustomPacketPayload {
        public static final Type<CarryState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","carry"));
        public static final StreamCodec<RegistryFriendlyByteBuf,CarryState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.player);b.writeVarInt(v.mob);},b->new CarryState(b.readVarInt(),b.readVarInt()));
        @Override public Type<CarryState> type(){return TYPE;}
    }
    public record EvasionMotion(double x,double y,double z,int ticks) implements CustomPacketPayload {
        public static final Type<EvasionMotion> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","evasion_motion"));
        public static final StreamCodec<RegistryFriendlyByteBuf,EvasionMotion> CODEC=StreamCodec.of((b,v)->{b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeVarInt(v.ticks);},b->new EvasionMotion(b.readDouble(),b.readDouble(),b.readDouble(),b.readVarInt()));
        @Override public Type<EvasionMotion> type(){return TYPE;}
    }
    public record EvasionState(int maximum,float energy,int recharge) implements CustomPacketPayload {
        public static final Type<EvasionState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","evasion"));
        public static final StreamCodec<RegistryFriendlyByteBuf,EvasionState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.maximum);b.writeFloat(v.energy);b.writeVarInt(v.recharge);},b->new EvasionState(b.readVarInt(),b.readFloat(),b.readVarInt()));
        @Override public Type<EvasionState> type(){return TYPE;}
    }
    public record CombatText(int entity,double x,double y,double z,String text,int color) implements CustomPacketPayload {
        public static final Type<CombatText> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","combat_text"));
        public static final StreamCodec<RegistryFriendlyByteBuf,CombatText> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entity);b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeUtf(v.text,32);b.writeInt(v.color);},b->new CombatText(b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readUtf(32),b.readInt()));
        @Override public Type<CombatText> type(){return TYPE;}
    }
    public record FlightState(int remaining,int boost,int meteor,boolean fast,float thrust,boolean forward,float meteorX,float meteorY,float meteorZ,boolean gliding) implements CustomPacketPayload {
        public FlightState(int remaining,int boost,int meteor,boolean fast){this(remaining,boost,meteor,fast,0);}
        public FlightState(int remaining,int boost,int meteor,boolean fast,float thrust){this(remaining,boost,meteor,fast,thrust,false,0,0,0,false);}
        public static final Type<FlightState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","flight"));
        public static final StreamCodec<RegistryFriendlyByteBuf,FlightState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.remaining);b.writeVarInt(v.boost);b.writeVarInt(v.meteor);b.writeBoolean(v.fast);b.writeFloat(v.thrust);b.writeBoolean(v.forward);b.writeFloat(v.meteorX);b.writeFloat(v.meteorY);b.writeFloat(v.meteorZ);b.writeBoolean(v.gliding);},b->new FlightState(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readFloat(),b.readBoolean(),b.readFloat(),b.readFloat(),b.readFloat(),b.readBoolean()));
        @Override public Type<FlightState> type(){return TYPE;}
    }
    public record EliteWarning(int entity,double x,double y,double z,float radius,int ticks,int kind) implements CustomPacketPayload {
        public static final Type<EliteWarning> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","elite_warning"));
        public static final StreamCodec<RegistryFriendlyByteBuf,EliteWarning> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entity);b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeFloat(v.radius);b.writeVarInt(v.ticks);b.writeVarInt(v.kind);},b->new EliteWarning(b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readVarInt(),b.readVarInt()));
        @Override public Type<EliteWarning> type(){return TYPE;}
    }
    public record DamageNumber(int entity,double x,double y,double z,float damage,boolean critical,boolean blocked,int color) implements CustomPacketPayload {
        public DamageNumber(int entity,double x,double y,double z,float damage,boolean critical,boolean blocked){this(entity,x,y,z,damage,critical,blocked,0);}
        public static final Type<DamageNumber> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","damage_number"));
        public static final StreamCodec<RegistryFriendlyByteBuf,DamageNumber> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entity);b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeFloat(v.damage);b.writeBoolean(v.critical);b.writeBoolean(v.blocked);b.writeInt(v.color);},b->new DamageNumber(b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readBoolean(),b.readBoolean(),b.readInt()));
        @Override public Type<DamageNumber> type(){return TYPE;}
    }
    public record JourneyState(int rpgClass,int stage,int progress) implements CustomPacketPayload {
        public static final Type<JourneyState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","journey"));
        public static final StreamCodec<RegistryFriendlyByteBuf,JourneyState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.rpgClass);b.writeVarInt(v.stage);b.writeVarInt(v.progress);},b->new JourneyState(b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<JourneyState> type(){return TYPE;}
    }
    public record Request(int action, int direction,int targetId,double aimX,double aimY,double aimZ) implements CustomPacketPayload {
        public Request(int action,int direction,int targetId){this(action,direction,targetId,Double.NaN,Double.NaN,Double.NaN);}
        public Request(int action,int direction){this(action,direction,-1);}
        public Request(int action) { this(action, 0); }
        public static final Type<Request> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeVarInt(value.action); buf.writeVarInt(value.direction);buf.writeVarInt(value.targetId);buf.writeDouble(value.aimX);buf.writeDouble(value.aimY);buf.writeDouble(value.aimZ); }, buf -> new Request(buf.readVarInt(), buf.readVarInt(),buf.readVarInt(),buf.readDouble(),buf.readDouble(),buf.readDouble()));
        @Override public Type<Request> type() { return TYPE; }
    }

    public record State(int rpgClass, int level, int xp, int kills, long cooldown, long secondCooldown, long thirdCooldown, long skills, boolean debug, boolean testing,
                        int questStage, int questFirst, int questSecond, String notice) implements CustomPacketPayload {
        public State(int rpgClass, int level, int xp, int kills, long cooldown, long skills, String notice) {
            this(rpgClass, level, xp, kills, cooldown, 0, 0, skills, false, false, 0, 0, 0, notice);
        }
        public State(int rpgClass, int level, int xp, int kills, long cooldown, long skills, boolean debug,
                     int questStage, int questFirst, int questSecond, String notice) {
            this(rpgClass, level, xp, kills, cooldown, 0, 0, skills, debug, false, questStage, questFirst, questSecond, notice);
        }
        public State(int rpgClass,int level,int xp,int kills,long cooldown,long secondCooldown,long thirdCooldown,long skills,boolean debug,int questStage,int questFirst,int questSecond,String notice){this(rpgClass,level,xp,kills,cooldown,secondCooldown,thirdCooldown,skills,debug,false,questStage,questFirst,questSecond,notice);}
        public long cooldown(int slot) { return slot == 0 ? cooldown : slot == 1 ? secondCooldown : thirdCooldown; }
        public boolean hasSkill(int id) { return rpgClass != 0 && (id < 2 || (skills & (1L << id)) != 0); }
        public boolean canUse(int slot) { return hasSkill(slot * 7 + 1); }
        public static final Type<State> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, State> CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeVarInt(value.rpgClass); buf.writeVarInt(value.level);
                buf.writeVarInt(value.xp); buf.writeVarInt(value.kills);
                buf.writeVarLong(value.cooldown); buf.writeVarLong(value.secondCooldown); buf.writeVarLong(value.thirdCooldown); buf.writeVarLong(value.skills); buf.writeBoolean(value.debug);buf.writeBoolean(value.testing);
                buf.writeVarInt(value.questStage); buf.writeVarInt(value.questFirst); buf.writeVarInt(value.questSecond);
                buf.writeUtf(value.notice, 512);
            }, buf -> new State(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong(), buf.readVarLong(), buf.readVarLong(),
                buf.readVarLong(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(512)));
        @Override public Type<State> type() { return TYPE; }
    }

    public record Ghost(int entityId, int ticks) implements CustomPacketPayload {
        public static final Type<Ghost> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "ghost"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Ghost> CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeVarInt(value.entityId); buf.writeVarInt(value.ticks); },
            buf -> new Ghost(buf.readVarInt(), buf.readVarInt()));
        @Override public Type<Ghost> type() { return TYPE; }
    }
    public record MobLevel(int entityId, int level) implements CustomPacketPayload {
        public static final Type<MobLevel> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "mob_level"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MobLevel> CODEC = StreamCodec.of(
            (b,v)-> { b.writeVarInt(v.entityId); b.writeVarInt(v.level); }, b->new MobLevel(b.readVarInt(),b.readVarInt()));
        @Override public Type<MobLevel> type() { return TYPE; }
    }
    public record AbilityStatus(int first, int second, int third, int totalFirst, int totalSecond, int totalThird, int phaseFirst, int phaseSecond, int phaseThird) implements CustomPacketPayload {
        public int remaining(int slot) { return slot==0?first:slot==1?second:third; }
        public int total(int slot) { return slot==0?totalFirst:slot==1?totalSecond:totalThird; }
        public int phase(int slot) { return slot==0?phaseFirst:slot==1?phaseSecond:phaseThird; }
        public static final Type<AbilityStatus> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "ability_status"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AbilityStatus> CODEC = StreamCodec.of(
            (b,v)-> { for(int i=0;i<3;i++)b.writeVarInt(v.remaining(i));for(int i=0;i<3;i++)b.writeVarInt(v.total(i));for(int i=0;i<3;i++)b.writeVarInt(v.phase(i)); },
            b->new AbilityStatus(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<AbilityStatus> type() { return TYPE; }
    }
    public record Resources(int points,int health,int damage,int luck,int armor,int manaPoints,int mana,int maxMana,String nodes) implements CustomPacketPayload {
        public Resources(int points,int health,int damage,int luck,int armor,int manaPoints,int mana,int maxMana){this(points,health,damage,luck,armor,manaPoints,mana,maxMana,"");}
        public int value(int id){return switch(id){case 0->health;case 1->damage;case 2->luck;case 3->armor;case 4->manaPoints;default->0;};}
        public static final Type<Resources> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","resources"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Resources> CODEC=StreamCodec.of(
            (b,v)->{b.writeVarInt(v.points);for(int i=0;i<5;i++)b.writeVarInt(v.value(i));b.writeVarInt(v.mana);b.writeVarInt(v.maxMana);b.writeUtf(v.nodes,1024);},
            b->new Resources(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readUtf(1024)));
        @Override public Type<Resources> type(){return TYPE;}
    }
    public record ProfessionState(int mining,int farming,int arsenal,String journal) implements CustomPacketPayload {
        public int xp(int id){return id==0?mining:id==1?farming:arsenal;}
        public static final Type<ProfessionState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","professions"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ProfessionState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.mining);b.writeVarInt(v.farming);b.writeVarInt(v.arsenal);b.writeUtf(v.journal,512);},b->new ProfessionState(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readUtf(512)));
        @Override public Type<ProfessionState> type(){return TYPE;}
    }
    public record VillagerDialogue(int entityId,String name,String title,String dialogue,String objective,String reward,int status,boolean canTrade,String offers,int selectedQuest,String npcDialogue) implements CustomPacketPayload {
        public VillagerDialogue(int entityId,String name,String title,String dialogue,String objective,String reward,int status,boolean canTrade){this(entityId,name,title,dialogue,objective,reward,status,canTrade,"[]",-1,dialogue);}
        public static final Type<VillagerDialogue> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","villager_dialogue"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VillagerDialogue> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entityId);b.writeUtf(v.name,256);b.writeUtf(v.title,256);b.writeUtf(v.dialogue,512);b.writeUtf(v.objective,256);b.writeUtf(v.reward,256);b.writeVarInt(v.status);b.writeBoolean(v.canTrade);b.writeUtf(v.offers,4096);b.writeVarInt(v.selectedQuest);b.writeUtf(v.npcDialogue,1024);},b->new VillagerDialogue(b.readVarInt(),b.readUtf(256),b.readUtf(256),b.readUtf(512),b.readUtf(256),b.readUtf(256),b.readVarInt(),b.readBoolean(),b.readUtf(4096),b.readVarInt(),b.readUtf(1024)));
        @Override public Type<VillagerDialogue> type(){return TYPE;}
    }
    public record VillagerMark(int entityId,int state) implements CustomPacketPayload {
        public static final Type<VillagerMark> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","villager_mark"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VillagerMark> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entityId);b.writeVarInt(v.state);},b->new VillagerMark(b.readVarInt(),b.readVarInt()));
        @Override public Type<VillagerMark> type(){return TYPE;}
    }
    public record EliteInfo(int entityId,String title) implements CustomPacketPayload {
        public static final Type<EliteInfo> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","elite_info"));
        public static final StreamCodec<RegistryFriendlyByteBuf,EliteInfo> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entityId);b.writeUtf(v.title,128);},b->new EliteInfo(b.readVarInt(),b.readUtf(128)));
        @Override public Type<EliteInfo> type(){return TYPE;}
    }
    public record ElectricArc(double x,double y,double z,double tx,double ty,double tz) implements CustomPacketPayload {
        public static final Type<ElectricArc> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","electric_arc"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ElectricArc> CODEC=StreamCodec.of((b,v)->{b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);b.writeDouble(v.tx);b.writeDouble(v.ty);b.writeDouble(v.tz);},b->new ElectricArc(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble()));
        @Override public Type<ElectricArc> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("34");
        registrar.playToClient(BloodFx.TYPE,BloodFx.CODEC);
        registrar.playToClient(ImpactFx.TYPE,ImpactFx.CODEC);
        registrar.playToClient(CastFx.TYPE,CastFx.CODEC);
        registrar.playToClient(DebugState.TYPE,DebugState.CODEC);
        registrar.playToClient(EntityMotion.TYPE,EntityMotion.CODEC);
        registrar.playToClient(DisguiseState.TYPE,DisguiseState.CODEC);
        registrar.playToClient(VampireState.TYPE,VampireState.CODEC);
        registrar.playToClient(SpecialState.TYPE,SpecialState.CODEC);
        registrar.playToClient(ContentState.TYPE,ContentState.CODEC);
        registrar.playToClient(EvasionState.TYPE,EvasionState.CODEC);
        registrar.playToClient(EvasionMotion.TYPE,EvasionMotion.CODEC);
        registrar.playToClient(CarryState.TYPE,CarryState.CODEC);
        registrar.playToClient(ElectricArc.TYPE,ElectricArc.CODEC);
        registrar.playToClient(CombatText.TYPE,CombatText.CODEC);
        registrar.playToClient(EliteWarning.TYPE,EliteWarning.CODEC);
        registrar.playToClient(FlightState.TYPE,FlightState.CODEC);
        registrar.playToClient(DamageNumber.TYPE,DamageNumber.CODEC);
        registrar.playToClient(JourneyState.TYPE,JourneyState.CODEC);
        registrar.playToClient(EliteInfo.TYPE,EliteInfo.CODEC);
        registrar.playToClient(VillagerMark.TYPE,VillagerMark.CODEC);
        registrar.playToClient(ProfessionState.TYPE,ProfessionState.CODEC);
        registrar.playToClient(VillagerDialogue.TYPE,VillagerDialogue.CODEC);
        registrar.playToClient(State.TYPE, State.CODEC);
        registrar.playToClient(Ghost.TYPE, Ghost.CODEC);
        registrar.playToClient(MobLevel.TYPE, MobLevel.CODEC);
        registrar.playToClient(AbilityStatus.TYPE, AbilityStatus.CODEC);
        registrar.playToClient(Resources.TYPE, Resources.CODEC);
        registrar.playToServer(Request.TYPE, Request.CODEC, (request, context) -> {
            if (context.player() instanceof ServerPlayer player){
                if(request.action()==1||request.action()==5||request.action()==6||request.action()==8||request.action()==9||request.action()==16)
                    AbilityAim.cast(player,request.targetId(),new net.minecraft.world.phys.Vec3(request.aimX(),request.aimY(),request.aimZ()),()->OxyRpg.menuAction(player,request.action(),request.direction()));
                else if(request.action()==20)AdvancedClasses.mark(player,request.targetId());
                else if(request.action()==19)AviaryCarry.request(player,request.targetId());
                else if(request.action()==18)AbilityAim.receive(player,new net.minecraft.world.phys.Vec3(request.aimX(),request.aimY(),request.aimZ()));
                else if(request.action()>=300&&request.action()<=308)VillagerQuests.action(player,request.action(),request.direction(),request.targetId());
                else OxyRpg.menuAction(player,request.action(),request.direction());
            }
        });
    }
}






