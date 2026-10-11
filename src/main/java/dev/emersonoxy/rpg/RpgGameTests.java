package dev.emersonoxy.rpg;

import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RpgGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS=DeferredRegister.create(Registries.TEST_FUNCTION,OxyRpg.MOD_ID);
    static {FUNCTIONS.register("profile",()->RpgGameTests::profile);FUNCTIONS.register("mana",()->RpgGameTests::mana);FUNCTIONS.register("evasion",()->RpgGameTests::evasion);FUNCTIONS.register("parry",()->RpgGameTests::parry);}
    private RpgGameTests(){}
    public static void functions(net.neoforged.bus.api.IEventBus bus){FUNCTIONS.register(bus);}
    public static void register(RegisterGameTestsEvent event){
        var environment=event.registerEnvironment(id("integration"),new TestEnvironmentDefinition.AllOf(java.util.List.of()));
        for(String name:java.util.List.of("profile","mana","evasion","parry"))event.registerTest(id(name),new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION,id(name)),new TestData<>(environment,id("test_arena"),100,0,true)));
    }
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(OxyRpg.MOD_ID,path);}
    // Isolated server entity: no client handshake or login hooks are needed for these contracts.
    private static final class TestPlayer extends net.minecraft.server.level.ServerPlayer {
        TestPlayer(GameTestHelper h,com.mojang.authlib.GameProfile profile,net.minecraft.server.network.CommonListenerCookie cookie){super(h.getLevel().getServer(),h.getLevel(),profile,cookie.clientInformation());}
        void advanceUse(){updateUsingItem(getUseItem());}
    }
    private static TestPlayer player(GameTestHelper h){
        var profile=new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"oxy-test");
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false);
        var p=new TestPlayer(h,profile,cookie);
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
        };
        p.setClientLoaded(true);
        h.getLevel().addFreshEntity(p);
        return p;
    }
    private static void profile(GameTestHelper h){
        var p=player(h);var profile=new RpgProfile(RpgClass.MAGO,30,120,9,0).allocateNode(p.getUUID(),0);
        try{new ProfileStore(p).save(p.getUUID(),profile);h.assertTrue(new ProfileStore(p).load(p.getUUID()).equals(profile),Component.literal("Perfil e atributos precisam sobreviver ao armazenamento nativo"));h.assertTrue(p.getData(PlayerAttachments.PROFILE).migrated(),Component.literal("Anexo deve ser a fonte do perfil"));var respawn=player(h);respawn.copyAttachmentsFrom(p,true);h.assertTrue(new ProfileStore(respawn).load(respawn.getUUID()).equals(profile),Component.literal("Classe, nós e atributos devem sobreviver à cópia após morte"));h.succeed();}catch(java.io.IOException e){h.fail(Component.literal(e.getMessage()));}
    }
    private static void mana(GameTestHelper h){
        var p=player(h);RpgResources.update(p,RpgProfile.NEW.choose(RpgClass.MAGO));float before=RpgResources.currentMana(p);RpgResources.spend(p,20);
        h.assertTrue(Math.abs(RpgResources.currentMana(p)-(before-20))<.001,Component.literal("Mana deve ser consumida pelo servidor"));h.assertFalse(RpgResources.affordable(p,100),Component.literal("Mana insuficiente deve impedir nova ativação"));h.succeed();
    }
    private static void evasion(GameTestHelper h){
        var p=player(h);var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,1,2));p.teleportTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setOnGround(true);
        float before=RpgEvasion.currentEnergy(p);RpgEvasion.use(p,0);h.assertTrue(RpgEvasion.currentEnergy(p)<before,Component.literal("Esquiva deve gastar carga"));
        float spent=RpgEvasion.currentEnergy(p);p.setOnGround(true);RpgEvasion.use(p,0);h.assertTrue(RpgEvasion.currentEnergy(p)==spent,Component.literal("Repetição imediata não pode gastar ou conceder outra esquiva"));h.succeed();
    }
    private static void parry(GameTestHelper h){
        h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
        var p=player(h);try{var profession=ProfessionStore.get(p);profession.xp[2]=ProfessionStore.MAX_XP;ProfessionStore.save(p,profession);}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}p.getAbilities().invulnerable=false;p.invulnerableTime=0;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SHIELD));var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,1,2));p.teleportTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setYRot(0);p.setYHeadRot(0);p.startUsingItem(InteractionHand.MAIN_HAND);
        var attacker=h.spawnWithNoFreeWill(EntityType.ZOMBIE,2,1,4);float health=p.getHealth();
        // Mock connections do not tick players; advance the normal shield raise explicitly.
        h.runAfterDelay(6,()->{for(int i=0;i<6;i++)p.advanceUse();h.assertTrue(p.getTicksUsingItem()>=5,Component.literal("Escudo deve completar o tempo de levantar"));p.hurtServer(h.getLevel(),p.damageSources().mobAttack(attacker),4);h.assertTrue(p.getHealth()==health,Component.literal("Escudo deve bloquear o ataque frontal"));h.assertTrue(ShieldParry.counter(p),Component.literal("Bloqueio preciso deve abrir contra-ataque"));h.succeed();});
    }
}
