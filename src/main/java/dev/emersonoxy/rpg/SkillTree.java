package dev.emersonoxy.rpg;

import java.util.List;
import java.util.ArrayList;

/** Versioned three-branch tree: preparation, minor effect, minor upgrade, main, two upgrades, capstone. */
public final class SkillTree {
    public static final int VERSION = 3, COUNT = 51;
    public static final long FREE_MASK = 3, PAID_MASK = ((1L << COUNT) - 1) & ~FREE_MASK;
    private static final int[] LEVELS = {1,1,5,10,15,20,25, 5,10,15,20,25,30,35, 10,15,20,30,35,40,45};
    public record Node(int id, int level, long parents, String title, String description, String icon) {}
    private SkillTree() {}
    public static int requiredLevel(int id) { check(id); return id<21?LEVELS[id]:((id-21)%10<2?LEVELS[((id-21)/10)*7+2]:30+((id-21)%10-2)*8); }
    private static void check(int id) { if (id < 0 || id >= COUNT) throw new IllegalArgumentException("Skill desconhecida."); }
    public static long parents(int id) {
        check(id);
        if(id>=21){int branch=(id-21)/10,step=(id-21)%10;return 1L << (step==0?branch*7+2:step==1?id-1:step<6?branch*7+6:id-4);}
        int step = id % 7;
        if(step==3)return 1L << (21+(id/7)*10+1);
        if (id == 0) return 0;
        if (step == 0) return 1;
        if (step == 5) return 1L << (id - 2);
        if (step == 6) return (1L << (id - 1)) | (1L << (id - 2));
        return 1L << (id - 1);
    }
    public static boolean validMask(int level, long mask) {
        if ((mask & ~PAID_MASK) != 0 || Long.bitCount(mask) > level / 2) return false;
        for (int id = 2; id < COUNT; id++) if ((mask & (1L << id)) != 0
            && (level < requiredLevel(id) || ((mask | FREE_MASK) & parents(id)) != parents(id))) return false;
        return true;
    }
    public static long migrate(long mask){for(int b=0;b<3;b++)if((mask&(1L<<(b*7+3)))!=0)mask|=3L<<(21+b*10);return mask;}
    public static int bonus(long mask,int kind){int count=0;for(int id=21;id<COUNT;id++)if((mask&(1L<<id))!=0&&((id-21)%10<2?kind==0:((id-23)%10)%4==kind))count++;return count;}
    public static String abilityName(RpgClass type, int slot) { return nodes(type).get(slot * 7 + 3).title(); }
    private static final java.util.Map<RpgClass,List<Node>> TREES = new java.util.EnumMap<>(RpgClass.class);
    public static synchronized List<Node> nodes(RpgClass type) { return TREES.computeIfAbsent(type, SkillTree::createNodes); }
    private static List<Node> createNodes(RpgClass type) {
        String[][] data = switch (type) {
            case MAGO -> new String[][] {
                {"Afinidade glacial","Frio recebido -10%; dano mágico contra alvos gelados +10%.","snowball"},
                {"Toque gelado","G lança um fragmento de gelo fraco; Lentidão II por 3 s.","ice"},
                {"Frio crescente","Lentidão inicial +1 s.","blue_ice"},
                {"Bola de gelo","G: cubo de gelo em área de 3 blocos; paralisia 3 s e frio 8 s. Recarga 25 s.","packed_ice"},
                {"Expansão glacial","Área da Bola de gelo +0,5 bloco.","snow_block"},
                {"Gelo duradouro","Frio da Bola de gelo +2 s.","powder_snow_bucket"},
                {"Nevasca","Área +1 bloco adicional, paralisia +1 s e frio +2 s.","diamond"},
                {"Calor interior","Dano mágico +5%.","blaze_powder"},
                {"Faísca","V libera uma faísca fraca; ataques mágicos queimam por 1 s, com intervalo.","flint_and_steel"},
                {"Combustão","Queimadura inicial +1 s.","coal"},
                {"Lança-chamas","V: jato na mira por 3 s, alcance 6 blocos; queima por 6 s. Recarga 30 s.","fire_charge"},
                {"Chama longa","Alcance do jato +1 bloco.","blaze_rod"},
                {"Fogo intenso","Dano do jato +10%.","magma_cream"},
                {"Cone infernal","Jato mais amplo, alcance +1 bloco e queimadura +4 s.","magma_block"},
                {"Sintonia terrestre","Resistência a empurrão +10%.","stone"},
                {"Abalo","B gera um abalo fraco; impactos mágicos aplicam breve lentidão em área.","cobblestone"},
                {"Vibração","Raio do abalo inicial +0,5 bloco.","gravel"},
                {"Tremor","B: área de 4 blocos por 6 s, dano periódico, Lentidão II e desgaste de até 4 blocos naturais. Recarga 40 s.","deepslate"},
                {"Epicentro","Raio do Tremor +1 bloco.","obsidian"},
                {"Persistência sísmica","Duração do Tremor +2 s.","clock"},
                {"Réplicas","Após o Tremor, duas ondas menores causam dano e lentidão.","echo_shard"}
            };
            case GUERREIRO -> new String[][] {
                {"Treino de lâmina","Dano corpo a corpo +5%.","wooden_sword"},
                {"Ímpeto","G fortalece brevemente; derrotar inimigos concede +5% de dano por 2 s.","stone_sword"},
                {"Fôlego de combate","Ímpeto dura +1 s.","apple"},
                {"Corte circular","G: dano em raio de 3 blocos e Força I por 10 s. Recarga 25 s.","iron_sword"},
                {"Lâmina ampla","Raio do corte +0,5 bloco.","diamond_sword"},
                {"Golpe pesado","Dano do corte +10%.","iron_axe"},
                {"Ímpeto de batalha","Cada inimigo atingido prolonga a Força em 1 s, até +5 s.","netherite_sword"},
                {"Vitalidade","Vida máxima +1 coração.","golden_apple"},
                {"Sede de sangue","Cura 5% do dano, com intervalo; V ativa uma versão fraca de Vampirismo.","redstone"},
                {"Sangue quente","Cura passiva passa para 8%.","ghast_tear"},
                {"Vampirismo","V: por 12 s cura 25% do dano. Fome drena em cerca de 6 s; acertos recuperam fome. Recarga 40 s.","cooked_beef"},
                {"Controle da fome","Drenagem de fome -10%.","bread"},
                {"Banquete","Fome recuperada por acerto +1 ponto.","cooked_porkchop"},
                {"Sangue vigoroso","Vampirismo: cura +10 pontos percentuais e duração +3 s.","beetroot_soup"},
                {"Postura firme","Resistência a empurrão +10%.","iron_boots"},
                {"Espinhos leves","Retaliação fraca com intervalo; B ativa guarda inicial.","cactus"},
                {"Pele endurecida","Dano recebido -5%.","iron_chestplate"},
                {"Bastião","B: brilho, Resistência III, Lentidão I, espinhos e reflexão frontal por 8 s. Recarga 45 s.","shield"},
                {"Guarda prolongada","Bastião dura +1 s.","diamond_chestplate"},
                {"Peso controlado","Penalidade de movimento do Bastião -20%.","feather"},
                {"Fortaleza viva","Retaliação e dano de projéteis refletidos +50%; retaliação empurra.","netherite_chestplate"}
            };
            case ARQUEIRO -> new String[][] {
                {"Mira treinada","Dano de flechas +5%.","bow"},
                {"Marca breve","G dispara uma flecha inicial; flechas destacam o alvo por 2 s.","arrow"},
                {"Rastro visível","Destaque inicial +1 s.","glow_ink_sac"},
                {"Flecha espectral","G: flecha espectral atravessa até 3 inimigos, destaca por 10 s e causa lentidão por 4 s. Recarga 25 s.","spectral_arrow"},
                {"Perfuração","Atravessa +1 inimigo.","crossbow"},
                {"Marca persistente","Brilho +2 s.","glowstone_dust"},
                {"Caçador espectral","Dano da flecha especial +25% e lentidão +2 s.","spyglass"},
                {"Passos leves","Distância de detecção dos mobs -10%.","leather_boots"},
                {"Discrição","Agachar por 2 s concede camuflagem fraca; V ativa sumiço inicial.","ink_sac"},
                {"Silêncio","Distância de detecção -5% adicionais.","black_wool"},
                {"Furtividade","V: jogador e equipamentos desaparecem por 10 s; fumaça por 2 s. Atacar encerra. Recarga 35 s.","ender_pearl"},
                {"Sumiço prolongado","Invisibilidade +2 s.","phantom_membrane"},
                {"Névoa densa","Raio da fumaça +0,5 bloco.","campfire"},
                {"Ataque das sombras","Primeiro ataque saindo da furtividade causa +75% de dano.","ender_eye"},
                {"Agilidade","Velocidade de movimento +3%.","sugar"},
                {"Ferida leve","Ataques pelas costas causam sangramento fraco por 2 s; B libera dash inicial.","flint"},
                {"Corte profundo","Sangramento inicial +1 s.","iron_sword"},
                {"Assassino","B: dash de 5 blocos, velocidade e dano físico dobrado por 8 s. Ataques causam sangramento e lentidão. Recarga 45 s.","diamond_sword"},
                {"Passo longo","Dash +1 bloco.","rabbit_foot"},
                {"Execução","Multiplicador de dano da principal +10%.","netherite_axe"},
                {"Passo duplo","Um segundo dash durante a ativação; fortalece sangramento.","feather"}
            };
            case NECROMANTE -> new String[][] {
                {"Pacto sombrio","Dano das invocações +5%.","fermented_spider_eye"},
                {"Filhote aracnídeo","G lança teia e invoca 1 aranha no alvo. Após perder o alvo, espera 4 s pelo próximo ataque do jogador.","string"},
                {"Ninhada","Aracnídeo inicial invoca +1 aranha.","spider_eye"},
                {"Aracnídeo","G lança teia e invoca 3 aranhas que saltam no alvo. Recarga 25 s.","cobweb"},
                {"Colônia","Aracnídeo principal invoca +1 aranha.","cave_spider_spawn_egg"},
                {"Fios persistentes","Lentidão da teia +2 s.","white_wool"},
                {"Matriarca","Aracnídeo principal invoca +1 aranha, até 5.","spider_spawn_egg"},
                {"Ossos firmes","Vida das invocações +10%.","bone"},
                {"Servo morto","V invoca 1 zumbi por 15 s; segue seus ataques e busca apenas hostis sozinho.","rotten_flesh"},
                {"Companhia pútrida","Rei dos Mortos inicial invoca +1 zumbi.","zombie_spawn_egg"},
                {"Rei dos Mortos","V invoca 1 esqueleto e 3 zumbis por 30 s. Seguem seus ataques; sozinhos, buscam hostis. Recarga 40 s.","skeleton_skull"},
                {"Guarda óssea","Rei dos Mortos principal invoca +1 esqueleto.","skeleton_spawn_egg"},
                {"Horda","Rei dos Mortos principal invoca +1 zumbi.","zombie_head"},
                {"Coroa dos mortos","Rei dos Mortos principal invoca +1 esqueleto e +1 zumbi, até 3 e 5.","wither_skeleton_skull"},
                {"Sombra firme","Resistência a empurrão +10%.","blackstone"},
                {"Asa sombria","B cega mobs próximos por 2 s e invoca 1 phantom para 1 mergulho.","phantom_membrane"},
                {"Breve eclipse","Cegueira inicial +1 s.","black_dye"},
                {"Céu Noturno","B cega mobs em raio de 6 blocos por 4 s e invoca 3 phantoms, com 1 mergulho cada. Recarga 45 s.","phantom_spawn_egg"},
                {"Horizonte escuro","Raio de Céu Noturno +1 bloco.","ender_eye"},
                {"Mergulho cruel","Dano dos phantoms +15%.","iron_sword"},
                {"Noite profunda","Cegueira de Céu Noturno +2 s.","echo_shard"}
            };
            case AVIARIO -> new String[][] {
                {"Instinto das alturas","Queda recebida -30%: 10 de dano vira 7.","feather"},
                {"Salto alado","Decolagem inicial: voo por 10 s. Reative para impulso de 2 s.","firework_rocket"},
                {"Asas firmes","Voo +3 s.","phantom_membrane"},
                {"Decolagem","Voo livre de elytra por 25 s, sem equipamento. Reative para forte impulso de 4 s: 5 MP; reaplicações aceleram mais.","elytra"},
                {"Fôlego aéreo","Voo +5 s.","clock"},
                {"Corrente ascendente","Impulso +1 s.","wind_charge"},
                {"Senhor dos céus","Voo +10 s e impulso mais veloz.","nether_star"},
                {"Penas leves","Prepara a descida controlada; redução de queda permanece em 30%.","white_wool"},
                {"Pluma protetora","Passiva: queda recebida -30%; agache ao cair para descer lentamente.","feather"},
                {"Ar amortecido","Enquanto desce agachado, dano de inimigos -10%.","white_carpet"},
                {"Peso pena","Passiva: queda recebida -30%. Agache ao cair: queda lenta e dano de inimigos -25%.","chicken_spawn_egg"},
                {"Brisa gentil","Descida agachada mais lenta.","breeze_rod"},
                {"Manto de penas","Redução de dano durante a descida +10%.","leather_chestplate"},
                {"Corpo etéreo","Redução de dano durante a descida +10%.","totem_of_undying"},
                {"Massa concentrada","Impactos causam +5% de dano.","stone"},
                {"Queda ofensiva","No ar: prepara impacto menor ao pousar, sem destruir blocos.","anvil"},
                {"Peso crescente","Dano do impacto +15%.","iron_block"},
                {"Impacto Meteoro","No ar: mergulha e explode ao pousar. Altura e velocidade ampliam dano e raio; destrói e arremessa blocos.","magma_block"},
                {"Cratera ampla","Raio do impacto +1 bloco.","tnt"},
                {"Núcleo denso","Dano do impacto +20%.","obsidian"},
                {"Cataclismo","Raio +1 bloco e mais detritos arremessados.","netherite_block"}
            };
            case XAMA -> new String[][] {
                {"Carga somática","Dano dos raios +5%.","copper_ingot"},
                {"Estalos","G ativa raios fracos nos ataques de punho por 5 s, com intervalo de 1,5 s.","lightning_rod"},
                {"Faísca intensa","Dano dos raios +1.","copper_block"},
                {"Deus Relâmpago","G: ataques de punho chamam raios no inimigo a cada 1,5 s por 15 s, sem ferir o jogador. Recarga 30 s.","amethyst_shard"},
                {"Tempestade longa","Duração de Deus Relâmpago +3 s.","clock"},
                {"Raio intenso","Dano dos raios +2.","diamond"},
                {"Raio encadeado","Um segundo hostil próximo recebe metade do dano elétrico.","end_rod"},
                {"Laço vital","Vida dos animais invocados +10%.","lead"},
                {"Primeiros socorros","V invoca 1 animal, cura levemente e remove efeitos negativos por 5 s; fertiliza plantas.","bone_meal"},
                {"Aura persistente","Aura de cura +2 s.","wheat"},
                {"Doutor Animal","V: 3 animais protetores, aura de cura e limpeza por 10 s, com farinha de osso na área. Recarga 40 s.","wolf_spawn_egg"},
                {"Círculo de cuidado","Raio de cura +1 bloco.","golden_apple"},
                {"Cura reforçada","Cura por pulso +0,5 ponto de vida.","glistering_melon_slice"},
                {"Guardião da natureza","Invoca +1 animal; aura dura +5 s.","polar_bear_spawn_egg"},
                {"Foco terrestre","Resistência a empurrão +10%.","moss_block"},
                {"Misturas básicas","B abre Especialista por 8 s: mãos vazias, esquerdo negativo e direito positivo. Poções fracas e aleatórias.","glass_bottle"},
                {"Preparo rápido","Intervalo entre poções reduzido de 1 s para 0,75 s.","brewing_stand"},
                {"Especialista","B: modo de poções por 15 s, esquerdo negativo e direito positivo, ambas as mãos vazias. Recarga 35 s.","splash_potion"},
                {"Bolsa de misturas","Duração do modo +5 s.","bundle"},
                {"Essência duradoura","Efeitos não instantâneos das poções principais +5 s.","dragon_breath"},
                {"Destilação econômica","Custo de mana por poção -1.","cauldron"}
            };
            default -> throw new IllegalArgumentException("Escolha uma classe primeiro.");
        };
        var result = new ArrayList<Node>();
        for (int id = 0; id < data.length; id++) result.add(new Node(id, LEVELS[id], parents(id), data[id][0], data[id][1], data[id][2]));
        for(int id=21;id<COUNT;id++){
            int step=(id-21)%10,branch=(id-21)/10,kind=step<2?0:(step-2)%4;
            String name=step<2?(step==0?"Preparação: ":"Domínio: ")+data[branch*7+3][0]:new String[]{"Potência","Persistência","Eficiência","Recuperação"}[kind]+" "+(step<6?"I":"II");
            String description=step<2?"Dano da classe +3%. Prepara o desbloqueio de "+data[branch*7+3][0]+".":new String[]{"Dano da classe +3%.","Duração das habilidades +5%.","Custo de mana das habilidades -3%.","Recarga das habilidades -3%."}[kind];
            result.add(new Node(id,requiredLevel(id),parents(id),name,description,new String[]{"iron_sword","clock","lapis_lazuli","amethyst_shard"}[kind]));
        }
        return List.copyOf(result);
    }
}





