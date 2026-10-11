package dev.emersonoxy.rpg;

final class SpecialistTrees {
    private SpecialistTrees(){}
    static String[][] nodes(RpgClass type){return switch(type){
        case ILUSIONISTA -> new String[][]{
            {"Espelhos frágeis","Ataques diretos -20%; ilusões distraem os inimigos.","glass"},
            {"Reflexo vivo","Duas cópias e aparência do alvo por 6 s.","ender_eye"},
            {"Imitação firme","Cópias resistem a um golpe adicional.","glass_pane"},
            {"Mímico","Duas cópias e aparência do alvo por 12 s.","amethyst_shard"},
            {"Reflexo extra","Uma cópia adicional.","quartz"},
            {"Ilusão duradoura","Duração do Mímico +3 s.","clock"},
            {"Salão de espelhos","Mais uma cópia; ilusões causam mais dano.","nether_star"},
            {"Fio invisível","Prepara o ramo de controle.","string"},
            {"Suspensão","Ergue e enfraquece o alvo por 3 s.","phantom_membrane"},
            {"Peso da dúvida","Controle causa mais dano.","fermented_spider_eye"},
            {"Leviosá","Suspende, enfraquece e fere o alvo; lentidão e desorientação ao redor.","ender_pearl"},
            {"Eco expansivo","Área de desorientação ampliada.","echo_shard"},
            {"Desorientação","Controle dura mais tempo.","spider_eye"},
            {"Gravidade falsa","Alvos controlados ficam mais vulneráveis.","anvil"},
            {"Fuga nas sombras","Prepara a forma de enxame.","black_wool"},
            {"Enxame fugaz","Forma espectral por 3 s; voo baixo e passagem por blocos comuns.","bat_spawn_egg"},
            {"Asas noturnas","Enxame se move mais rapidamente.","feather"},
            {"Corpo de Eclipse","Forma espectral por 6 s; Espaço sobe, Shift desce.","crying_obsidian"},
            {"Noite prolongada","Enxame +2 s.","clock"},
            {"Entre frestas","Pode atravessar portas, cercas e grades.","iron_bars"},
            {"Eclipse total","Voo até 8 blocos do terreno e movimento mais rápido.","tinted_glass"}
        };
        case HIDROMANTE -> new String[][]{
            {"Filho das marés","Respiração aquática e trabalho submerso; vulnerável ao fogo fora da água.","nautilus_shell"},
            {"Impulso aquático","Na água, avance na direção da mira; mire para cima para saltar.","prismarine_shard"},
            {"Nado firme","Nado mais veloz.","dolphin_spawn_egg"},
            {"Águaman","Nado veloz, respiração e mineração normal na água; chuva fortalece ataques.","heart_of_the_sea"},
            {"Correnteza","Impulso mais forte.","prismarine_crystals"},
            {"Salto das ondas","Impulso ascendente ampliado.","breeze_rod"},
            {"Senhor da tempestade","Tempestades ampliam o dano; nado ainda mais veloz.","conduit"},
            {"Pressão crescente","Prepara o canhão.","water_bucket"},
            {"Jato d’água","Jato curto com dano e empurrão.","blue_dye"},
            {"Pressão concentrada","Jato causa mais dano.","lapis_lazuli"},
            {"Canhão d’água","Jato giratório contínuo; submerso cria manifestações marinhas.","tube_coral"},
            {"Alcance da maré","Jato alcança mais longe.","fishing_rod"},
            {"Mar revolto","Empurrão mais forte.","trident"},
            {"Abismo vivo","Mais manifestações marinhas e duração de jato.","guardian_spawn_egg"},
            {"Equilíbrio da maré","Prepara o controle da superfície.","lily_pad"},
            {"Passo aquático","Caminha sobre a água por 6 s; agache para mergulhar.","leather_boots"},
            {"Água restauradora","Ativação em contato com água cura mais.","potion"},
            {"Sobre-água","Caminha sobre a água por 15 s; drena um espaço ao ativar e recupera vida.","blue_ice"},
            {"Bolha ampla","Espaço drenado maior.","sponge"},
            {"Maré persistente","Caminhada sobre água dura mais.","clock"},
            {"Renovação oceânica","Cura ampliada e limpeza de efeitos negativos.","sea_lantern"}
        };
        case PIROMANTE -> new String[][]{
            {"Caldeira viva","Fogo e explosões geram calor; excesso causa superaquecimento.","blaze_powder"},
            {"Pavio encantado","Marca inimigos próximos com fogos. Reative para detonar.","firework_rocket"},
            {"Estopim longo","Fogos duram mais.","string"},
            {"Carnaval de Pólvora","Fogos orbitam os alvos e explodem em sequência; queimaduras atraem mais fogos.","firework_star"},
            {"Pólvora espalhada","Marca mais alvos.","gunpowder"},
            {"Explosão ascendente","Explosões lançam inimigos mais alto.","wind_charge"},
            {"Festa sem fim","Detonações propagam fogos para outros inimigos, com limite.","tnt"},
            {"Pedra incandescente","Prepara núcleos de magma.","magma_block"},
            {"Gota de magma","Lança um núcleo que adere ao alvo ou ao chão. Reative agachado para detonar.","magma_cream"},
            {"Calor aderente","Núcleos causam queimadura mais longa.","fire_charge"},
            {"Coração de Caldeira","Núcleos detonados criam linhas de fogo entre pontos próximos.","lava_bucket"},
            {"Reservatório","Pode preparar mais núcleos.","cauldron"},
            {"Fios ardentes","Linhas ficam ativas por mais tempo.","redstone"},
            {"Magma voraz","Núcleos e linhas causam mais dano.","netherite_scrap"},
            {"Brasa ancestral","Prepara a transformação.","charcoal"},
            {"Asas de brasa","Consome 30 de calor: forma ígnea breve e salto explosivo.","blaze_rod"},
            {"Penas ardentes","Saltos da Fênix causam mais dano.","feather"},
            {"Fênix de Cinzas","Imune a fogo; mobilidade na lava, saltos, rastro de brasas e explosão final.","totem_of_undying"},
            {"Cinzas persistentes","Fênix dura mais tempo.","clock"},
            {"Vapor escaldante","Água cria vapor que desorienta inimigos.","white_dye"},
            {"Renascimento explosivo","Explosão final consome núcleos próximos e fica mais forte.","nether_star"}
        };
        default -> throw new IllegalArgumentException("Classe sem árvore especializada");
    };}
}
