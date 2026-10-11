package dev.emersonoxy.rpg;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.google.gson.*;
import net.minecraft.data.*;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Generates class definitions, quest definitions, recipe, data maps and the developer test template. */
public final class RpgDataGenerator implements DataProvider {
    private final PackOutput output;
    public RpgDataGenerator(PackOutput output){this.output=output;}
    public static void gather(GatherDataEvent.Client event){event.createProvider(RpgDataGenerator::new);}
    @Override public String getName(){return "Oxy RPG definitions and integration resources";}
    @Override public CompletableFuture<?> run(CachedOutput cache){
        var tasks=new ArrayList<CompletableFuture<?>>();var bundle=new JsonObject();var classDefinitions=new JsonObject();bundle.add("classes",classDefinitions);bundle.add("quests",new Gson().toJsonTree(QuestCatalog.defaults()));
        for(var c:RpgClass.values())if(c!=RpgClass.NONE){var definition=new JsonObject();definition.addProperty("label",c.defaultLabel());for(String key:List.of("mana_multiplier","cooldown_multiplier","damage_multiplier","duration_multiplier"))definition.addProperty(key,1);
            var nodes=new JsonArray();for(var n:SkillTree.defaults(c)){var node=new JsonObject();node.addProperty("id",n.id());node.addProperty("title",n.title());node.addProperty("description",n.description());node.addProperty("icon",n.icon());double[] position=SkillTreeLayouts.nodePosition(c,n.id());node.addProperty("x",Math.round(position[0]));node.addProperty("y",Math.round(position[1]));nodes.add(node);}definition.add("nodes",nodes);classDefinitions.add(c.name(),definition);tasks.add(save(cache,"data/oxyrpg/rpg/classes/"+c.name().toLowerCase(Locale.ROOT)+".json",definition));}
        tasks.add(save(cache,"data/oxyrpg/rpg/quests.json",new Gson().toJsonTree(QuestCatalog.defaults())));
        tasks.add(save(cache,"data/oxyrpg/data_maps/entity_type/mob_tuning.json",JsonParser.parseString("{\"values\":{\"minecraft:zombie\":{\"health\":1,\"damage\":1,\"xp\":1},\"minecraft:skeleton\":{\"health\":1,\"damage\":1,\"xp\":1}}}")));
        tasks.add(save(cache,"data/oxyrpg/data_maps/item/weapon_tuning.json",JsonParser.parseString("{\"values\":{\"#minecraft:swords\":{\"damage\":1,\"critical_bonus\":0},\"minecraft:iron_sword\":{\"bleed_chance\":0.08},\"minecraft:diamond_sword\":{\"bleed_chance\":0.12},\"minecraft:netherite_sword\":{\"bleed_chance\":0.18,\"bleed_severity\":1},\"minecraft:iron_axe\":{\"bleed_chance\":0.12},\"minecraft:diamond_axe\":{\"bleed_chance\":0.16},\"minecraft:netherite_axe\":{\"bleed_chance\":0.2,\"bleed_severity\":1}}}")));
        tasks.add(save(cache,"data/oxyrpg/recipe/sigil.json",JsonParser.parseString("{\"type\":\"minecraft:crafting_shaped\",\"category\":\"misc\",\"pattern\":[\" G \",\"GAG\",\" G \"],\"key\":{\"G\":\"minecraft:gold_ingot\",\"A\":\"minecraft:amethyst_shard\"},\"result\":{\"id\":\"oxyrpg:sigil\",\"count\":1}}")));
        RpgContent.validate(bundle);
        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }
    private CompletableFuture<?> save(CachedOutput cache,String file,JsonElement json){return DataProvider.saveStable(cache,json,output.getOutputFolder().resolve(file));}
}
