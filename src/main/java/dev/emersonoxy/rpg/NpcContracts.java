package dev.emersonoxy.rpg;
import java.util.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.core.registries.BuiltInRegistries;
/** Stable appended IDs preserve contracts issued by previous versions. */
public final class NpcContracts {
    private static final Map<Integer,String> ROLES=new HashMap<>();
    private static final Map<String,String> CHAT=Map.ofEntries(
        Map.entry("farmer","A chuva chegou no momento certo. Agora precisamos guardar parte da colheita."),
        Map.entry("fisherman","O rio conta histórias. Peixe fresco e uma rede inteira pagam o jantar."),
        Map.entry("shepherd","Cada cor tem seu lugar. Quero mantas resistentes para as casas."),
        Map.entry("fletcher","Uma flecha boa começa na bancada, muito antes de encontrar um alvo."),
        Map.entry("librarian","Nossa biblioteca guarda as memórias de quem passou por esta aldeia."),
        Map.entry("cartographer","As estradas continuam além do horizonte. Quero registrá-las com cuidado."),
        Map.entry("cleric","Conhecimento e cuidado caminham juntos. O altar também serve quem precisa de ajuda."),
        Map.entry("armorer","Boa armadura permite que nossos guardas voltem para casa."),
        Map.entry("weaponsmith","Uma lâmina equilibrada importa mais do que seu brilho."),
        Map.entry("toolsmith","Ferramentas bem cuidadas sustentam a aldeia inteira."),
        Map.entry("butcher","Nada se desperdiça na cozinha. Precisamos de reservas para os dias difíceis."),
        Map.entry("leatherworker","Uma costura boa precisa de paciência. Minha oficina cuida de cada detalhe."),
        Map.entry("mason","As casas precisam de fundações fortes antes de receberem decoração."),
        Map.entry("guard","Meu dever é proteger quem vive aqui. Patrulhamos as entradas e as estradas; você pode ajudar com os contratos de defesa."),
        Map.entry("none","Ainda estou procurando uma profissão. Um bloco de trabalho me ajudaria a encontrar meu lugar."),
        Map.entry("nitwit","Prefiro ouvir as histórias da praça. Nem todo mundo precisa de uma oficina."));
    private NpcContracts(){}
    public static String role(Villager v){return v instanceof VillageGuard?"guard":BuiltInRegistries.VILLAGER_PROFESSION.getKey(v.getVillagerData().profession().value()).getPath();}
    public static VillagerQuests.Quest[] append(VillagerQuests.Quest[] legacy){var q=new ArrayList<>(List.of(legacy));
        add(q,"farmer","Celeiro de inverno","Minha oficina precisa de 24 unidades para celeiro de inverno.","minecraft:wheat",24,1,6,1);
        add(q,"farmer","Legumes para a feira","Minha oficina precisa de 20 unidades para legumes para a feira.","minecraft:carrot",20,1,6,1);
        add(q,"farmer","Sementes da próxima safra","Minha oficina precisa de 16 unidades para sementes da próxima safra.","minecraft:beetroot_seeds",16,1,6,1);
        add(q,"fisherman","Varal de bacalhau","Minha oficina precisa de 12 unidades para varal de bacalhau.","minecraft:cod",12,1,6,1);
        add(q,"fisherman","Salmão para o defumador","Minha oficina precisa de 10 unidades para salmão para o defumador.","minecraft:salmon",10,1,6,1);
        add(q,"fisherman","Linha para as redes","Minha oficina precisa de 16 unidades para linha para as redes.","minecraft:string",16,1,6,1);
        add(q,"shepherd","Mantas para o frio","Minha oficina precisa de 16 unidades para mantas para o frio.","minecraft:white_wool",16,1,6,1);
        add(q,"shepherd","Tinta para as bandeiras","Minha oficina precisa de 8 unidades para tinta para as bandeiras.","minecraft:blue_dye",8,1,6,1);
        add(q,"shepherd","Tesouras da oficina","Minha oficina precisa de 4 unidades para tesouras da oficina.","minecraft:iron_ingot",4,1,6,1);
        add(q,"fletcher","Hastes bem retas","Minha oficina precisa de 32 unidades para hastes bem retas.","minecraft:stick",32,0,6,1);
        add(q,"fletcher","Penas para as flechas","Minha oficina precisa de 16 unidades para penas para as flechas.","minecraft:feather",16,0,6,1);
        add(q,"fletcher","Pederneira afiada","Minha oficina precisa de 12 unidades para pederneira afiada.","minecraft:flint",12,0,6,1);
        add(q,"librarian","Papel para os registros","Minha oficina precisa de 24 unidades para papel para os registros.","minecraft:paper",24,0,6,1);
        add(q,"librarian","Encadernação da coleção","Minha oficina precisa de 8 unidades para encadernação da coleção.","minecraft:book",8,0,6,1);
        add(q,"librarian","Tinta dos mapas antigos","Minha oficina precisa de 12 unidades para tinta dos mapas antigos.","minecraft:ink_sac",12,0,6,1);
        add(q,"cartographer","Papel para a expedição","Minha oficina precisa de 24 unidades para papel para a expedição.","minecraft:paper",24,0,6,1);
        add(q,"cartographer","Bússola do explorador","Minha oficina precisa de 1 unidades para bússola do explorador.","minecraft:compass",1,0,6,1);
        add(q,"cartographer","Mapas da região","Minha oficina precisa de 3 unidades para mapas da região.","minecraft:map",3,0,6,1);
        add(q,"cleric","Redstone do altar","Minha oficina precisa de 16 unidades para redstone do altar.","minecraft:redstone",16,0,6,1);
        add(q,"cleric","Lápis para os símbolos","Minha oficina precisa de 12 unidades para lápis para os símbolos.","minecraft:lapis_lazuli",12,0,6,1);
        add(q,"cleric","Unguento da enfermaria","Minha oficina precisa de 4 unidades para unguento da enfermaria.","minecraft:honey_bottle",4,0,6,1);
        add(q,"armorer","Chapas para as couraças","Minha oficina precisa de 12 unidades para chapas para as couraças.","minecraft:iron_ingot",12,0,6,1);
        add(q,"armorer","Carvão para a forja","Minha oficina precisa de 24 unidades para carvão para a forja.","minecraft:coal",24,0,6,1);
        add(q,"armorer","Correias das armaduras","Minha oficina precisa de 12 unidades para correias das armaduras.","minecraft:leather",12,0,6,1);
        add(q,"weaponsmith","Ferro das lâminas","Minha oficina precisa de 10 unidades para ferro das lâminas.","minecraft:iron_ingot",10,0,6,1);
        add(q,"weaponsmith","Carvão da têmpera","Minha oficina precisa de 20 unidades para carvão da têmpera.","minecraft:coal",20,0,6,1);
        add(q,"weaponsmith","Pedra para amolar","Minha oficina precisa de 32 unidades para pedra para amolar.","minecraft:cobblestone",32,0,6,1);
        add(q,"toolsmith","Cabeças de ferramentas","Minha oficina precisa de 8 unidades para cabeças de ferramentas.","minecraft:iron_ingot",8,0,6,1);
        add(q,"toolsmith","Cabos de reposição","Minha oficina precisa de 32 unidades para cabos de reposição.","minecraft:stick",32,0,6,1);
        add(q,"toolsmith","Forja abastecida","Minha oficina precisa de 20 unidades para forja abastecida.","minecraft:coal",20,0,6,1);
        add(q,"butcher","Reserva do defumador","Minha oficina precisa de 12 unidades para reserva do defumador.","minecraft:porkchop",12,1,6,1);
        add(q,"butcher","Ensopado da cozinha","Minha oficina precisa de 10 unidades para ensopado da cozinha.","minecraft:beef",10,1,6,1);
        add(q,"butcher","Combustível do fogão","Minha oficina precisa de 16 unidades para combustível do fogão.","minecraft:coal",16,1,6,1);
        add(q,"leatherworker","Couro para as selas","Minha oficina precisa de 16 unidades para couro para as selas.","minecraft:leather",16,0,6,1);
        add(q,"leatherworker","Costura das bolsas","Minha oficina precisa de 20 unidades para costura das bolsas.","minecraft:string",20,0,6,1);
        add(q,"leatherworker","Tinta para os aventais","Minha oficina precisa de 8 unidades para tinta para os aventais.","minecraft:brown_dye",8,0,6,1);
        add(q,"mason","Pedra para o muro","Minha oficina precisa de 32 unidades para pedra para o muro.","minecraft:stone",32,0,6,1);
        add(q,"mason","Argila para os telhados","Minha oficina precisa de 32 unidades para argila para os telhados.","minecraft:clay_ball",32,0,6,1);
        add(q,"mason","Escadaria de granito","Minha oficina precisa de 24 unidades para escadaria de granito.","minecraft:granite",24,0,6,1);
        add(q,"guard","Saqueadores na estrada","Nossos mensageiros precisam da estrada segura. Intercepte os saqueadores.","minecraft:pillager",5,2,12,0);
        add(q,"guard","Machados no portão","Vindicadores ameaçam nossas casas. Mantenha-os longe dos moradores.","minecraft:vindicator",3,2,14,0);
        add(q,"guard","Ameaça dos pântanos","Bruxas estão preparando ataques. Proteja a aldeia.","minecraft:witch",3,2,14,0);
        return q.toArray(VillagerQuests.Quest[]::new);
    }
    private static void add(List<VillagerQuests.Quest> list,String role,String title,String text,String item,int count,int area,int reward,int kind){ROLES.put(list.size(),role);list.add(new VillagerQuests.Quest(title,text,item,count,area,reward,kind));}
    public static int[] offers(Villager v){String role=role(v);return ROLES.entrySet().stream().filter(e->e.getValue().equals(role)).mapToInt(Map.Entry::getKey).sorted().toArray();}
    public static boolean allows(Villager v,int id){return Arrays.stream(offers(v)).anyMatch(q->q==id);}
    public static String chat(Villager v,int topic){String line=CHAT.getOrDefault(role(v),"Cada ofício tem sua importância por aqui.");return topic%3==0?line:topic%3==1?v instanceof VillageGuard?"Se ouvir o sino, procure abrigo. Eu e os outros guardas cuidaremos dos portões.":"Se precisar dos produtos da minha profissão, veja minhas trocas. Os contratos ajudam a manter a oficina funcionando.":v instanceof VillageGuard?"As marcas nas estradas avisam onde houve problemas. Traga notícias das patrulhas; queremos que todos retornem.":"É bom reconhecer quem ajuda nossa aldeia. Volte para conversar quando passar pela praça.";}
}
