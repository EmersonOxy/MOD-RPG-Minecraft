package dev.emersonoxy.rpg;

import java.util.*;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Atomic, validated datapack definitions. Stable node IDs and objective sizes preserve old saves. */
public final class RpgContent extends SimplePreparableReloadListener<RpgContent.Bundle> {
    public record ClassDefinition(String label,List<SkillTree.Node> nodes,Map<Integer,double[]> positions,double mana,double cooldown,double damage,double duration){}
    public record Bundle(Map<RpgClass,ClassDefinition> classes,List<QuestCatalog.Quest> quests,String json){}
    private static volatile Bundle active=new Bundle(Map.of(),List.of(),"{}");
    private static final RpgContent INSTANCE=new RpgContent();
    private RpgContent(){}
    public static void listeners(AddServerReloadListenersEvent event){event.addListener(ResourceLocation.fromNamespaceAndPath("oxyrpg","rpg_content"),INSTANCE);}
    public static List<SkillTree.Node> nodes(RpgClass c,List<SkillTree.Node> defaults){var definition=active.classes().get(c);return definition==null?defaults:definition.nodes();}
    public static double[] position(RpgClass c,int id,double[] defaults){var definition=active.classes().get(c);return definition==null?defaults:definition.positions().getOrDefault(id,defaults);}
    public static String label(RpgClass c,String defaults){var definition=active.classes().get(c);return definition==null?defaults:definition.label();}
    public static double manaMultiplier(RpgClass c){var d=active.classes().get(c);return d==null?1:d.mana();}
    public static double cooldownMultiplier(RpgClass c){var d=active.classes().get(c);return d==null?1:d.cooldown();}
    public static double damageMultiplier(RpgClass c){var d=active.classes().get(c);return d==null?1:d.damage();}
    public static double durationMultiplier(RpgClass c){var d=active.classes().get(c);return d==null?1:d.duration();}
    public static QuestCatalog.Quest quest(int stage,List<QuestCatalog.Quest> defaults){var list=active.quests().isEmpty()?defaults:active.quests();return list.get(Math.floorMod(stage,list.size()));}
    public static void validate(JsonObject root){decode(root);}
    public static String json(){return active.json();}
    public static void receive(String json){active=decode(JsonParser.parseString(json).getAsJsonObject());}
    @Override protected Bundle prepare(ResourceManager resources,ProfilerFiller profiler){
        var root=new JsonObject();var classes=new JsonObject();root.add("classes",classes);
        for(var c:RpgClass.values())if(c!=RpgClass.NONE){var id=ResourceLocation.fromNamespaceAndPath("oxyrpg","rpg/classes/"+c.name().toLowerCase(Locale.ROOT)+".json");try(var reader=resources.getResourceOrThrow(id).openAsReader()){classes.add(c.name(),JsonParser.parseReader(reader));}catch(java.io.IOException e){throw new IllegalStateException("Definição de classe ausente: "+id,e);}}
        var id=ResourceLocation.fromNamespaceAndPath("oxyrpg","rpg/quests.json");try(var reader=resources.getResourceOrThrow(id).openAsReader()){root.add("quests",JsonParser.parseReader(reader));}catch(java.io.IOException e){throw new IllegalStateException("Definição de missões ausente",e);}
        return decode(root);
    }
    @Override protected void apply(Bundle data,ResourceManager resources,ProfilerFiller profiler){active=data;}
    private static double multiplier(JsonObject json,String field){double n=json.has(field)?json.get(field).getAsDouble():1;if(!Double.isFinite(n)||n<.1||n>10)throw new IllegalArgumentException(field+" deve estar entre 0,1 e 10");return n;}
    private static String text(JsonObject json,String field,int maximum){String s=json.get(field).getAsString();if(s.isBlank()||s.length()>maximum)throw new IllegalArgumentException("Texto inválido: "+field);return s;}
    private static Bundle decode(JsonObject root){
        // A client can receive the empty pre-world snapshot; compiled defaults remain available.
        if(!root.has("classes"))return new Bundle(Map.of(),List.of(),"{}");
        var classes=new EnumMap<RpgClass,ClassDefinition>(RpgClass.class);
        for(var c:RpgClass.values())if(c!=RpgClass.NONE){
            var data=root.getAsJsonObject("classes").getAsJsonObject(c.name());var defaults=SkillTree.defaults(c);var list=new ArrayList<SkillTree.Node>();var positions=new HashMap<Integer,double[]>();var nodes=data.getAsJsonArray("nodes");
            if(nodes.size()!=SkillTree.COUNT)throw new IllegalArgumentException("Preserve os 51 IDs da árvore de "+c);
            for(int id=0;id<SkillTree.COUNT;id++){var n=nodes.get(id).getAsJsonObject();var old=defaults.get(id);if(n.get("id").getAsInt()!=id)throw new IllegalArgumentException("IDs dos nós fora de ordem");
                String icon=text(n,"icon",128);ResourceLocation.parse(icon);
                list.add(new SkillTree.Node(id,old.level(),old.parents(),text(n,"title",80),text(n,"description",512),icon));
                if(n.has("x")&&n.has("y")){double x=n.get("x").getAsDouble(),y=n.get("y").getAsDouble();if(!Double.isFinite(x)||!Double.isFinite(y)||Math.abs(x)>4000||Math.abs(y)>4000)throw new IllegalArgumentException("Posição inválida");positions.put(id,new double[]{x,y});}
            }
            classes.put(c,new ClassDefinition(text(data,"label",32),List.copyOf(list),Map.copyOf(positions),multiplier(data,"mana_multiplier"),multiplier(data,"cooldown_multiplier"),multiplier(data,"damage_multiplier"),multiplier(data,"duration_multiplier")));
        }
        var defaults=QuestCatalog.defaults();var quests=new ArrayList<QuestCatalog.Quest>();var data=root.getAsJsonArray("quests");if(data.size()!=defaults.size())throw new IllegalArgumentException("Preserve a quantidade de etapas de missão");
        for(int i=0;i<data.size();i++){var q=data.get(i).getAsJsonObject();var targets=new ArrayList<QuestCatalog.Target>();var array=q.getAsJsonArray("targets");if(array.size()!=defaults.get(i).targets().size())throw new IllegalArgumentException("Preserve o número de objetivos");
            for(int j=0;j<array.size();j++){var t=array.get(j).getAsJsonObject();int amount=t.get("amount").getAsInt();if(amount!=defaults.get(i).targets().get(j).amount())throw new IllegalArgumentException("Preserve a contagem de objetivos para manter saves compatíveis");String mob=text(t,"mob",128);ResourceLocation.parse(mob);targets.add(new QuestCatalog.Target(mob,text(t,"label",64),amount,text(t,"icon",128)));}
            int reward=q.get("reward").getAsInt();if(reward<0||reward>100000)throw new IllegalArgumentException("Recompensa fora do limite");quests.add(new QuestCatalog.Quest(text(q,"title",80),reward,List.copyOf(targets)));
        }
        String json=new Gson().toJson(root);if(json.length()>262144)throw new IllegalArgumentException("Definições excedem o limite de sincronização");return new Bundle(Map.copyOf(classes),List.copyOf(quests),json);
    }
    public static void sync(OnDatapackSyncEvent event){
        var player=event.getPlayer();if(player!=null)PacketDistributor.sendToPlayer(player,new RpgNetwork.ContentState(json()));else{for(var p:event.getPlayerList().getPlayers()){PacketDistributor.sendToPlayer(p,new RpgNetwork.ContentState(json()));OxyRpg.sync(p,"");}rebalance();}
    }
    public static void configReloaded(ModConfigEvent.Reloading event){if(event.getConfig().getModId().equals(OxyRpg.MOD_ID))rebalance();}
    private static void rebalance(){var server=ServerLifecycleHooks.getCurrentServer();if(server==null)return;server.execute(()->{for(var world:server.getAllLevels())for(var entity:world.getAllEntities())if(entity instanceof net.minecraft.world.entity.Mob mob&&mob instanceof net.minecraft.world.entity.monster.Enemy&&!RpgNecromancer.isSummon(mob)){MobLevels.balance(mob);EliteMobs.slow(mob);}});}
}
