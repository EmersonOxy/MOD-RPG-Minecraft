package dev.emersonoxy.rpg;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class RpgNetwork {
    private RpgNetwork() {}

    public record Request(int action, int direction) implements CustomPacketPayload {
        public Request(int action) { this(action, 0); }
        public static final Type<Request> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeVarInt(value.action); buf.writeVarInt(value.direction); }, buf -> new Request(buf.readVarInt(), buf.readVarInt()));
        @Override public Type<Request> type() { return TYPE; }
    }

    public record State(int rpgClass, int level, int xp, int kills, long cooldown, long secondCooldown, long thirdCooldown, int skills, boolean debug,
                        int questStage, int questFirst, int questSecond, String notice) implements CustomPacketPayload {
        public State(int rpgClass, int level, int xp, int kills, long cooldown, int skills, String notice) {
            this(rpgClass, level, xp, kills, cooldown, 0, 0, skills, false, 0, 0, 0, notice);
        }
        public State(int rpgClass, int level, int xp, int kills, long cooldown, int skills, boolean debug,
                     int questStage, int questFirst, int questSecond, String notice) {
            this(rpgClass, level, xp, kills, cooldown, 0, 0, skills, debug, questStage, questFirst, questSecond, notice);
        }
        public long cooldown(int slot) { return slot == 0 ? cooldown : slot == 1 ? secondCooldown : thirdCooldown; }
        public boolean hasSkill(int id) { return rpgClass != 0 && (id < 2 || (skills & (1 << id)) != 0); }
        public boolean canUse(int slot) { return hasSkill(slot * 7 + 1); }
        public static final Type<State> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg", "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, State> CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeVarInt(value.rpgClass); buf.writeVarInt(value.level);
                buf.writeVarInt(value.xp); buf.writeVarInt(value.kills);
                buf.writeVarLong(value.cooldown); buf.writeVarLong(value.secondCooldown); buf.writeVarLong(value.thirdCooldown); buf.writeVarInt(value.skills); buf.writeBoolean(value.debug);
                buf.writeVarInt(value.questStage); buf.writeVarInt(value.questFirst); buf.writeVarInt(value.questSecond);
                buf.writeUtf(value.notice, 512);
            }, buf -> new State(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong(), buf.readVarLong(), buf.readVarLong(),
                buf.readVarInt(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(512)));
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
    public record Resources(int points,int health,int damage,int luck,int armor,int manaPoints,int mana,int maxMana) implements CustomPacketPayload {
        public int value(int id){return switch(id){case 0->health;case 1->damage;case 2->luck;case 3->armor;case 4->manaPoints;default->0;};}
        public static final Type<Resources> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","resources"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Resources> CODEC=StreamCodec.of(
            (b,v)->{b.writeVarInt(v.points);for(int i=0;i<5;i++)b.writeVarInt(v.value(i));b.writeVarInt(v.mana);b.writeVarInt(v.maxMana);},
            b->new Resources(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<Resources> type(){return TYPE;}
    }
    public record ProfessionState(int mining,int farming,int arsenal,String journal) implements CustomPacketPayload {
        public int xp(int id){return id==0?mining:id==1?farming:arsenal;}
        public static final Type<ProfessionState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","professions"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ProfessionState> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.mining);b.writeVarInt(v.farming);b.writeVarInt(v.arsenal);b.writeUtf(v.journal,512);},b->new ProfessionState(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readUtf(512)));
        @Override public Type<ProfessionState> type(){return TYPE;}
    }
    public record VillagerDialogue(int entityId,String name,String title,String dialogue,String objective,String reward,int status) implements CustomPacketPayload {
        public static final Type<VillagerDialogue> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("oxyrpg","villager_dialogue"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VillagerDialogue> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.entityId);b.writeUtf(v.name,256);b.writeUtf(v.title,256);b.writeUtf(v.dialogue,512);b.writeUtf(v.objective,256);b.writeUtf(v.reward,256);b.writeVarInt(v.status);},b->new VillagerDialogue(b.readVarInt(),b.readUtf(256),b.readUtf(256),b.readUtf(512),b.readUtf(256),b.readUtf(256),b.readVarInt()));
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
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("11");
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
            if (context.player() instanceof ServerPlayer player) OxyRpg.menuAction(player, request.action(), request.direction());
        });
    }
}






