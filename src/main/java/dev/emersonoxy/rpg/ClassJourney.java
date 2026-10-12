package dev.emersonoxy.rpg;

import java.io.IOException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.LoggerFactory;

/** Three permanent lessons per class; reward and completion share one atomic profession record. */
public final class ClassJourney {
    private ClassJourney(){}
    public static String objective(RpgClass c,int stage){
        if(stage>=3)return "Treinamento concluído! Continue com as caçadas e contratos.";
        return switch(c){
            case GUERREIRO->new String[]{"Acerte 3 ataques corpo a corpo em mobs.","Acerte 2 mobs no mesmo Corte Circular.","Bloqueie 3 ataques usando um escudo."}[stage];
            case MAGO->new String[]{"Acerte 3 Bolas de Gelo em mobs.","Acerte 2 mobs com o mesmo pulso de Lança-chamas.","Acerte 2 mobs com a mesma onda de Tremor."}[stage];
            case ARQUEIRO->new String[]{"Acerte 3 Adagas Espectrais em criaturas.","Acerte 3 ataques enquanto estiver invisível.","Faça 3 dashes do Assassino com deslocamento."}[stage];
            case ARQUEIRO_NOVO->new String[]{"Acerte 3 flechas da rajada em criaturas.","Acerte 3 flechas da chuva em criaturas.","Faça 3 esquivas durante a Caçada."}[stage];
            case DEUS_TROVAO->new String[]{"Realize 3 teleportes elétricos.","Acerte 3 pulsos da Corrente de Raios.","Acerte 3 choques com as mãos vazias durante Avatar."}[stage];
            case NECROMANTE->new String[]{"Acerte 3 projéteis de teia em mobs.","Seus mortos-vivos devem acertar 3 ataques.","Seus phantoms devem acertar 3 ataques."}[stage];
            case AVIARIO->new String[]{"Realize 3 decolagens.","Use a descida de Peso pena em 3 saltos.","Acerte mobs com 3 Impactos Meteoro."}[stage];
            case ILUSIONISTA->new String[]{"Use Mímico em 3 alvos.","Suspenda 3 inimigos.","Use Corpo de Eclipse 3 vezes."}[stage];
            case HIDROMANTE->new String[]{"Faça 3 impulsos aquáticos.","Acerte 3 jatos em mobs.","Recupere vida com Sobre-água 3 vezes."}[stage];
            case VAMPIRESCO->new String[]{"Ative Sede de Sangue 3 vezes após perder vida.","Assuma a forma de morcego 3 vezes.","Ative Twilight 3 vezes."}[stage];
            case PIROMANTE->new String[]{"Detone 3 sequências de fogos.","Detone 3 grupos de núcleos.","Assuma a forma da Fênix 3 vezes."}[stage];
            case XAMA->new String[]{"Acerte 3 raios da habilidade em mobs.","Restaure vida com a aura em 3 ocasiões.","Lance 3 poções do Especialista."}[stage];
            default->"Escolha uma classe para começar.";
        };
    }
    public static int goal(RpgClass c,int stage){return stage>=3||c==RpgClass.NONE?0:(stage==1&&(c==RpgClass.MAGO||c==RpgClass.GUERREIRO)||stage==2&&c==RpgClass.MAGO?1:3);}
    public static int reward(int stage){return 50*(stage+1);}
    public static void sync(ServerPlayer p){try{var c=RpgAbilities.profile(p).rpgClass();var data=ProfessionStore.get(p);PacketDistributor.sendToPlayer(p,new RpgNetwork.JourneyState(c.ordinal(),data.lesson[c.ordinal()],data.lessonProgress[c.ordinal()]));}catch(IOException e){LoggerFactory.getLogger(ClassJourney.class).warn("Não foi possível ler treinamento de classe",e);}}
    public static void advance(ServerPlayer p,int lesson,int amount){
        if(amount<=0||p.isCreative()||p.isSpectator()||RpgDebug.enabled(p))return;
        var c=RpgAbilities.profile(p).rpgClass();if(c==RpgClass.NONE)return;
        try{
            var d=ProfessionStore.get(p);int id=c.ordinal();if(d.lesson[id]!=lesson||lesson>=3)return;
            d.lessonProgress[id]=Math.min(goal(c,lesson),d.lessonProgress[id]+amount);
            boolean complete=d.lessonProgress[id]>=goal(c,lesson);int oldLevel=ProfessionStore.level(d.xp[2]);
            if(complete){d.lesson[id]++;d.lessonProgress[id]=0;d.xp[2]=Math.min(ProfessionStore.MAX_XP,d.xp[2]+reward(lesson));}
            ProfessionStore.save(p,d);
            if(complete){p.displayClientMessage(Component.literal("[Oxy RPG] Lição de "+c.label()+" concluída! +"+reward(lesson)+" XP de Arsenal."),true);if(ProfessionStore.level(d.xp[2])>oldLevel)p.displayClientMessage(Component.literal("[Oxy RPG] Arsenal chegou ao nível "+ProfessionStore.level(d.xp[2])+"!"),true);}
            Professions.sync(p);
        }catch(IOException e){LoggerFactory.getLogger(ClassJourney.class).error("Treinamento não salvo; dados preservados",e);}
    }
    public static void damage(ServerPlayer p,LivingDamageEvent.Post event){
        var source=event.getSource();var direct=source.getDirectEntity();int type=direct instanceof Projectile shot?shot.getPersistentData().getIntOr("oxy_type",0):0;
        switch(RpgAbilities.profile(p).rpgClass()){
            case GUERREIRO->{if(direct==p)advance(p,0,1);}
            case MAGO->{if(type==1&&!direct.getPersistentData().getBooleanOr("oxy_lesson_counted",false)){direct.getPersistentData().putBoolean("oxy_lesson_counted",true);advance(p,0,1);}}
            case ARQUEIRO->{if(type==4&&!direct.getPersistentData().getBooleanOr("oxy_lesson_counted",false)){direct.getPersistentData().putBoolean("oxy_lesson_counted",true);advance(p,0,1);}}
            case NECROMANTE->{var ally=RpgNecromancer.isSummon(direct)?direct:RpgNecromancer.isSummon(source.getEntity())?source.getEntity():null;if(ally!=null){int slot=ally.getPersistentData().getIntOr("oxy_summon_slot",0);if(slot==1||slot==2)advance(p,slot,1);}}
            case XAMA->{if(source.is(DamageTypes.LIGHTNING_BOLT))advance(p,0,1);}
            default->{}
        }
    }
}
