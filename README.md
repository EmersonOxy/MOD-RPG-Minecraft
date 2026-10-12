# Oxy RPG 0.48.0

Mod RPG para Minecraft **1.21.10**, NeoForge **21.10.64**, JDK **21**.

Dez classes, progressão até nível 100, árvores de habilidades e atributos com zoom/arraste, trabalhos, arsenal, missões, criaturas por nível, elites, mana, esquiva e parry.

A 0.33.0 acrescenta mapa local de cavernas, painéis independentes da HUD, corrida em inventários/baús, tema comum para missões/trocas, bússola e avisos de região. Inclui nove itens, slots de anéis/colar, patrulhas de saqueadores com recompensas e estruturas mais frequentes.

A 0.34 corrige a detecção de cavernas, melhora encontros de patrulhas, amplia vilas com anexos conectados, simplifica a bússola e permite corrida em qualquer direção no modo independente. Ver [correções 0.34](docs/CORRECOES_034.md).

A 0.35 reduz trabalho do minimapa, habilidades e indicadores, suaviza avisos de região e acrescenta Lithium/FerriteCore como complementos separados. [Desempenho, referências e limites](docs/DESEMPENHO_035.md).

A 0.36 acrescenta gestos e efeitos das nove classes, enxame visual do Ilusionista, reposicionamento de inimigos e preparação/recuperação mais legível dos elites. Cosméticos configuráveis em R > Interface > Combate > Animações e efeitos. [Detalhes, limites e validação](docs/COMBATE_E_ANIMACOES_036.md).

A 0.37 acrescenta **R > Debug**, liberação total de skills, níveis ajustáveis de 1 a 100, classe livre e controles independentes de recarga/recursos. `/rpg debug on` já libera a árvore; `/rpg debug cooldowns on|off` alterna recargas. [Comandos e preservação do perfil](docs/DEBUG_037.md).

A 0.38 acrescenta sangue com marcas temporárias nas superfícies, fumaça em impactos/explosões/decolagem e partículas de golpes fortes. Controles individuais, intensidade e duração em Animações e efeitos. [Efeitos, limites e validação](docs/PARTICULAS_038.md).

A 0.39 amplia/anima números e textos, torna sangue proporcional ao dano, remove fumaça de golpes comuns, estabiliza a camada do minimapa e separa lançamento/voo do Aviário com freio aéreo em pé. [Correções e validação](docs/CORRECOES_039.md). [Opções externas de animação pesquisadas, sem instalação](docs/ANIMACOES_EXTERNAS_039.md).

A 0.42 adiciona Vampiresco, cores de dano por condição, HUD compacto das skills, folhas/vento, ondulações sobre água e vegetação reativa. Sangramento das passivas do Arqueiro removido. [Implementação, referências e limites](docs/VAMPIRESCO_AMBIENTE_042.md).

A 0.41 adiciona sangramento persistente, bandagens para estancar, chances por arma e instala Fresh Animations original com EMF/ETF na instância local. [Funcionamento e versões](docs/SANGRAMENTO_ANIMACOES_041.md).

A 0.40 remove tetos próprios de sangue/manchas, processa grandes respingos em lotes e preserva caches separados de superfície/pisos do minimapa. [Mudanças e evidências dos prints](docs/CORRECOES_040.md).

A 0.42.1 corrige a falha de inicialização do hook de vegetação: seleção explícita da sobrecarga NeoForge de `renderBatched`. [Diagnóstico e verificação](docs/CORRECOES_0421.md).

A 0.43 muda a alimentação do Vampiresco para botão direito, inclui indicador de presa, medo noturno/fuga e bandagens para alguns humanoides. Sede vermelha, morcego ampliado, sangue destacado no Twilight, HUD de skills reposicionada, grama amassada enquanto ocupada e ondas de água mais longas. Acrescenta Fresh Animations: Player Extension oficial como pacote separado. [Detalhes e limites](docs/CORRECOES_043.md).

A 0.44 substitui o avatar pela forma renderizada de morcego com dimensões/câmera menores, protege a escolta, reduz a fuga dos aldeões, impede dano de queda do impulso da Fênix, cria canhão aquático em anéis e impactos de magma com cratera/contaminação. Grama usa a iluminação de blocos; HUD com ícones coloridos ao lado da hotbar. Golems recebem nível e novas criaturas/invocações têm animação de surgimento. [Detalhes e limites](docs/CORRECOES_044.md).

A 0.47 faz o canhão aquático acompanhar a mira, alinha arco/tridente/besta com a câmera, corrige a seleção e registro da animação de esquiva, separa itens do criativo e acrescenta ovo de guarda. [Correções, instruções de guardas e escolha de complementos](docs/CORRECOES_047.md).

A 0.48 corrige o ovo do guarda e o alinhamento das trocas, reformula missões/conversa, integra Homesteads e More Mobs como complementos e permite poses de combate/rolamento coexistirem com EMF. [Implementação, estado real das integrações e limites](docs/CORRECOES_048.md).

## Controles

- **R:** menu de classe, árvores, missões, trabalhos, arsenal e configurações.
- **G/V/B:** três ramos de habilidades. **Z/N:** trava de alvo/alternar alvo.
- **Alt esquerdo:** esquiva. Parry exige escudo e a janela de bloqueio.
- **H:** alterna HUD sempre visível/dinâmico/oculto. F1 oculta a interface.
- **O/J:** ajustes de câmera/troca de ombro. **M:** liga/desliga minimapa.
- **Shift esquerdo** sobre um item no inventário: comparação com equipamento atual.
- **Botão direito / Usar Item:** ação do artefato da classe.
- Todos os atalhos Oxy são remapeáveis em Opções > Controles. Atalhos adicionais podem estar sem tecla por padrão.

Movimento em inventários e containers usa as teclas vanilla configuradas e preserva a corrida e é suspenso ao pesquisar. Pode ser desligado em **R > Interface > Inventário**. Minimap em **R > Minimapa** permite ajustar formato, tamanho, zoom, opacidade e posição.

Painéis: **R > Interface > Painéis**. Bússola e avisos: **R > Interface > Explorar**. Itens novos: **R > Arsenal > Achados de exploração**. Detalhes em [0.33.0](docs/EXPLORACAO_033.md).

## Recursos e progresso

- Cada classe possui **51 nós**. Atributos têm **160 nós**, com distribuição estável por jogador.
- Mana tem custo, regeneração e poções de **35/80/160 MP**. Receitas e detalhes em [0.30.0](docs/CORRECOES_030.md).
- Teste libera nível 100 e desbloqueios com custos normais; debug oferece os bypasses de desenvolvimento. Use `/rpg` para os comandos disponíveis.
- Dados são separados por mundo/UUID. Equipamentos vinculados retornam após morte, com espera para uso.
- Habilidades podem atingir mobs pacíficos; invocações/entidades temporárias têm regras próprias. Nem todo alvo atingível concede XP ou drops RPG.

## Dependências

owo-lib **0.12.28+1.21.10**, Curios **13.0.0+1.21.10**, GeckoLib **5.3-alpha-3**. O port Do Another Barrel Roll e YACL são separados; instruções e licenças em [integrations/barrel-roll](integrations/barrel-roll/README.md) e [integrations/yacl](integrations/yacl/README.md). JEI não é usado. Complementos de estruturas: Explorify 1.6.4 e Structory 1.3.12, instalados separadamente; não incluídos no JAR Oxy. Ver [manifesto de versões e hashes](docs/ESTRUTURAS_033.json).

## Registro do projeto

[Estado atual, evidências, limitações e preferências](docs/ESTADO_DO_PROJETO.md) · [Inventário de fontes](docs/INVENTARIO_CODIGO_040.json) · [Atualização 0.32](docs/INTERFACE_032.md) · [Atualização 0.33](docs/EXPLORACAO_033.md) · [Referências e próximos passos](docs/REFERENCIAS_E_IDEIAS_033.md).

Os documentos numerados em docs preservam o histórico; descrições antigas não substituem o estado atual.

## Desenvolvimento e instalação

No Windows, `build-windows.bat` compila com assemble. Testes e GameTests são opt-in, conforme [AGENTS.md](AGENTS.md). Não abrir o jogo automaticamente para validar.

Instalar somente com Minecraft fechado, preservando o JAR anterior fora de mods e fazendo backup dos mundos/configurações. Cliente e servidor precisam do mesmo protocolo — **23** nesta versão. Compilação não confirma validação visual ou balanceamento em jogo.

A 0.45 corrige mira de skills pela câmera, cópias com IA, colisão cosmética, fuga dos aldeões, Fênix, mergulho próximo ao chão e visual do canhão de água. [Correções](docs/CORRECOES_045.md) · [Análise dos mods indicados](docs/REFERENCIAS_045.md).

A 0.46 reúne trocas e contratos de profissão, acrescenta guardas próprios/diálogos, integra Better Combat + PAL e permite escolher Terralith por novo mundo. Reutiliza apenas os visuais do Combat Roll em pacote separado. [Funcionamento e limites](docs/NPCS_E_INTEGRACOES_046.md).
