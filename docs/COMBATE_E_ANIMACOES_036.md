# Oxy RPG 0.36.0 — gestos, efeitos e táticas

Primeira etapa das melhorias propostas: identidade visual das nove classes e comportamento dos inimigos existentes. Chefes, eventos de vila e especializações alternativas ficam para etapas futuras. Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21; protocolo 24, necessário nas duas pontas de multiplayer.

## Classes

Ativações aceitas pelo servidor enviam um único CastFx aos clientes que acompanham o jogador e a ele próprio. Falha por mana, recarga, árvore ou condição da habilidade não dispara o evento. O evento é cosmético: não altera dano, recursos, direção do ataque ou duração da habilidade.

Gestos de braços interpolados por 18 ticks (0,9 s), usando os modelos humanoides nativos do jogador e da armadura. Guerreiro ergue o braço de ataque; Mago projeta as mãos; Arqueiro prepara os braços; Necromante e Ilusionista abrem os braços; Xamã faz um gesto de invocação; Aviário abre os braços na decolagem; Hidromante projeta as mãos; Piromaníaco usa uma pose assimétrica. Respeita mão principal esquerda, montaria, natação, uso de item e voo com elytra/Barrel Roll. Pernas, câmera e movimento não são controlados por esses gestos. São gestos do corpo em terceira pessoa; não há nova animação específica de mãos em primeira pessoa.

Paleta e partículas distintas: gelo/fogo/encantamento do Mago, acertos do Guerreiro, encantamento do Arqueiro, almas do Necromante, partículas vitais do Xamã, vento do Aviário, portais do Ilusionista, gotas/bolhas do Hidromante e pequenas chamas do Piromaníaco. Motivos são emitidos pelo cliente, perto do torso e sem pacotes por tick. Meteoro recebe onda clara de pressão própria.

No enxame do Ilusionista, quatro silhuetas estilizadas de morcegos orbitam o jogador e batem as asas. São desenhos procedurais projetados no mundo, com teste de oclusão, sem criar entidades ou alterar colisão. Duram até o período informado pela habilidade, limitado visualmente a 600 ticks; cancelamento de invisibilidade encerra o efeito. Não são um modelo 3D novo nem transformação por GeckoLib.

Configuração: R > Interface > Combate > Animações e efeitos. Gestos, cosméticos das classes e morcegos podem ser desligados separadamente em oxyrpg-combat.properties. Partículas mínimas desativam emissões adicionais; reduzidas diminuem a frequência. Indicadores de ataques inimigos continuam independentes desses cosméticos.

## Táticas dos inimigos

Novo goal de reposicionamento para entidades vanilla das famílias Zombie, AbstractSkeleton, Pillager e Vindicator. Só funciona em combate com alvo já escolhido pela IA: jogador vulnerável ou invocação aliada. Não escolhe novos alvos, não reconhece o Ilusionista pelo jogador escondido, não altera alcance de percepção e não é instalado em tipos registrados por outros mods. Entidades temporárias e invocações próprias são excluídas também durante a execução.

Com arco/besta, tenta recuar três blocos quando o alvo fica a menos de seis; em distância maior, tenta deslocamento lateral de 2,5 blocos. Um terço dos inimigos corpo a corpo, escolhido de forma estável pelo UUID, tenta um flanco lateral de dois blocos quando está entre quatro e dez blocos do alvo. Alterna lados por identidade. Cada tentativa tem intervalo de 80–119 ticks; o movimento dura no máximo 16 ticks antes de devolver controle aos ataques vanilla. Carregamento de arco/besta em andamento é preservado.

Usa pathfinding nativo, endpoint em chunk já carregado, chão sólido, espaço substituível e seco, e exige caminho alcançável. Não adiciona varredura global nem loop próprio por entidade. Não reposiciona nadando, queimando, montado ou no ar; prioridades dos esqueletos preservam metas anteriores de fuga ao sol/lobos. Configuração de servidor difficulty.enemyTactics pode desligar reposicionamento. Outros mods que alterem a IA de tipos vanilla ainda podem disputar prioridades: esta compatibilidade requer validação em jogo.

## Elites

Os golpes de área dos traços existentes agora exibem contorno no chão, legenda e barra de preparação. Salvas e fortificações usam legenda/barra sem apresentar um círculo como se fosse área de dano. Contornos de área respeitam o raio do traço; a preparação existente de 20 ticks foi mantida. Área é indicativa em terreno irregular, não substitui as verificações autoritativas de distância e linha de visão.

Traço e afixo evitam preparar dois ataques especiais simultaneamente. Após execução existe recuperação de 16 ticks antes de outra preparação. Nos quatro grupos com novas metas, um goal suspende navegação e ataques vanilla durante preparação/recuperação, mantendo a orientação para o alvo; segurança de flutuação/fogo continua prioritária. Outros tipos de elite recebem aviso e intervalo entre especiais, sem substituir sua IA. Vida, dano, drops, chance de elite e habilidades existentes não foram aumentados.

## Limites e desempenho

32 ativações visuais em memória; até 24 partículas novas por tick, raio visual de 32 blocos; até oito morcegos desenhados por frame. Eventos expiram, são removidos com entidades mortas/ausentes e limpos em mudança de mundo. Avisos: máximo de 16 armazenados, até oito por frame, 24 segmentos de contorno e chão calculado no recebimento. Impactos usam 16 segmentos. WorldLines.contour reutiliza a visibilidade de vértices adjacentes em vez de traçar o mesmo ponto duas vezes. Não foram adicionadas bibliotecas, arquivos de textura ou geração de estruturas.

Esses limites controlam custos; não há medição de FPS ou TPS desta versão. Não foi prometido aumento de desempenho.

## Evidências

Repositório local recuperado do último commit publicado 045917b, pois o caminho anterior estava vazio. Compilação assemble concluída e revisão de JSON, registros de payload, mixins, caminhos de ativação, prioridades da IA e modelos/armaduras nativos. Inventário de fontes em INVENTARIO_CODIGO_036.json. Não foram executados testes, GameTests nem iniciado o Minecraft, conforme AGENTS.md. Validação visual, navegação e compatibilidade de jogo pendentes da avaliação pessoal.

Instalação com o jogo fechado, backup de saves/config/JAR anterior e conferência de SHA-256 em INSTALACAO_036.json. Complementos da 0.35 permanecem separados. Commit/push seguem o pedido de registrar e publicar as entregas.

Para conferir pessoalmente: testar as nove classes em terceira pessoa, com e sem armadura e com mão esquerda; usar as três opções cosméticas; verificar voo/Barrel Roll e enxame; aproximar-se de arqueiros em terreno aberto, observar zumbis e saqueadores alternando deslocamentos/ataques; verificar aviso e recuperação de elites, parry, esquiva e disfarce. Em fogo, água ou terreno estreito, espera-se que as táticas cedam ao comportamento vanilla.
