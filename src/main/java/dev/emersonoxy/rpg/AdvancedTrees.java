package dev.emersonoxy.rpg;

/** Native class nodes; stable legacy IDs remain assigned to the Assassin. */
public final class AdvancedTrees {
    private AdvancedTrees(){}
    public static String[][] nodes(RpgClass c){return switch(c){
        case ARQUEIRO_NOVO -> new String[][] {
            {"Mira treinada","Flechas causam 5% mais dano.","bow"},
            {"Rajada breve","Dispara 2 flechas na mira; causa lentidão por 3 s. Consome 2 flechas.","arrow"},
            {"Toxina persistente","Lentidão da rajada dura 5 s.","fermented_spider_eye"},
            {"Rajada debilitante","Dispara 6 flechas na mira, aplicando lentidão e fraqueza. Consome uma flecha por disparo.","crossbow"},
            {"Aljava ampla","Rajada dispara mais 2 flechas.","bundle"},
            {"Disparo pesado","Dano da rajada +15%.","iron_nugget"},
            {"Rajada venenosa","Mais 2 flechas na rajada e veneno por 3 s.","spider_eye"},
            {"Passos leves","Agachar por 2 s concede camuflagem breve; detecção de criaturas -10%.","leather_boots"},
            {"Chuva breve","Dispara 4 flechas para cima que convergem sobre uma criatura marcada.","spectral_arrow"},
            {"Flechas firmes","Dano das flechas da chuva +15%.","flint"},
            {"Chuva de flechas","10 flechas sobem e caem sobre a criatura marcada, acompanhando seu deslocamento.","bow"},
            {"Nuvem de setas","Chuva dispara mais 4 flechas.","feather"},
            {"Ponta reforçada","Dano da chuva +25%; explosões ganham 1 de dano.","iron_ingot"},
            {"Chuva explosiva","Flechas da chuva explodem ao colidir; raio 2,5 blocos, sem destruir terreno.","fire_charge"},
            {"Agilidade","Velocidade de movimento +3%.","sugar"},
            {"Caçada breve","Velocidade e superpulo por 4 s; esquivas sem consumir cargas, inclusive no ar.","rabbit_foot"},
            {"Fôlego da caçada","Caçada inicial e principal duram mais 1 s.","apple"},
            {"Caçada implacável","Por 10 s: velocidade, superpulo e esquivas livres. Ao mirar ou marcar uma criatura, flechas perseguem o alvo e causam lentidão.","spyglass"},
            {"Caçada prolongada","Caçada dura mais 2 s; lentidão dos disparos guiados dura 6 s.","clock"},
            {"Setas velozes","Velocidade dos disparos guiados aumenta de 3,4 para 4 blocos/tick.","wind_charge"},
            {"Caçador aéreo","Superpulo V e lentidão II nos disparos guiados.","elytra"}
        };
        case DEUS_TROVAO -> new String[][] {
            {"Condutor vivo","Após correr por 3 s, ganha 75% de velocidade; aumenta até 125% após 8 s. Saltos mantêm a corrida.","lightning_rod"},
            {"Salto elétrico","Teleporte de até 5 blocos na mira; raio causa 3 de dano em raio de 1,5 bloco.","ender_pearl"},
            {"Choque de chegada","Criaturas atingidas no teleporte recebem lentidão por 2 s.","glowstone_dust"},
            {"Descida do trovão","Teleporte de até 12 blocos; raio causa 6 de dano em raio de 3 blocos.","lightning_rod"},
            {"Horizonte elétrico","Alcance do teleporte +4 blocos.","spyglass"},
            {"Carga de chegada","Dano do teleporte +2; raio do impacto +1 bloco; correntes causam 20% mais dano.","diamond"},
            {"Passo de tempestade","Teleporte ganha mais 4 blocos; salto entre criaturas alcança 6 blocos.","ender_eye"},
            {"Afinidade elétrica","Preparação para canalizar a corrente.","redstone"},
            {"Faísca dirigida","Corrente na mira por 2 s; alcance de 18 blocos e dano periódico.","copper_ingot"},
            {"Carga intensa","Dano do alvo inicial da corrente +1 por pulso.","gold_ingot"},
            {"Corrente de raios","Corrente na mira por 4 s; atinge o alvo inicial e salta para até 2 criaturas próximas.","end_rod"},
            {"Arco longo","Alcance da corrente aumenta para 24 blocos.","copper_block"},
            {"Canalização persistente","Corrente dura mais 2 s.","clock"},
            {"Tempestade encadeada","Corrente salta para mais 2 criaturas próximas.","diamond_block"},
            {"Punhos carregados","Preparação para a tempestade de punhos.","iron_ingot"},
            {"Pulso de tempestade","Por 3 s ganha 90% de velocidade, superpulo, sobe blocos e aplica choque com as mãos vazias; o raio salta para outra criatura.","quartz"},
            {"Pulso duradouro","Pulso de tempestade dura mais 1 s.","lapis_lazuli"},
            {"Avatar do trovão","Por 8 s: velocidade +180%, superpulo, subida automática e choque nos punhos com até 3 saltos entre criaturas. Exige ambas as mãos vazias para o choque.","nether_star"},
            {"Avatar persistente","Avatar dura mais 2 s.","clock"},
            {"Velocidade divina","Velocidade do Avatar ganha mais 50 pontos percentuais.","sugar"},
            {"Escalada elétrica","Altura de passo durante o Avatar aumenta mais 0,5 bloco; superpulo fica mais forte.","smooth_stone"}
        };
        default -> throw new IllegalArgumentException("Classe sem árvore avançada");
    };}
}
