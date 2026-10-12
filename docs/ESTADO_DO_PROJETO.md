# Estado do Oxy RPG — revisão 0.44.0

Revisão registrada em 11/10/2026. Minecraft 1.21.10, NeoForge 21.10.64, JDK 21, protocolo de rede 30. Este registro substitui descrições antigas do README. Os documentos numerados são históricos de cada entrega.

A revisão inventariou fontes Java, registros, eventos, mixins, telas, recursos JSON, geração de dados, integração e persistência. Código das áreas alteradas foi inspecionado em detalhe. Inventário de arquivos e hashes: [INVENTARIO_CODIGO_040.json](INVENTARIO_CODIGO_040.json). Não equivale a validação integral dentro do jogo nem a auditoria de segurança.

## Entrega 0.44

Forma de morcego renderizada diretamente no lugar do avatar, caixa 0,7×0,85 e olho 0,55; escolta protegida do próprio dono. Fuga de aldeões reduzida. Proteção de queda dos impulsos da Fênix. Canhão aquático de anéis, magma com dano em área/cratera/terreno convertido, grama em pipeline de blocos, slots coloridos na HUD. Iron/Snow Golems ganham nível, vida/dano; animações cosméticas de surgimento e reação ao golpe reutilizam os modelos existentes. Protocolo 30 pelo novo payload EntityMotion; cliente/servidor devem usar a mesma versão. [Diagnóstico e validação](CORRECOES_044.md).

## Entrega 0.43

Alimentação por interação, aviso de presa, sangramento probabilístico na mordida, reações de mobs e medo noturno de aldeões. Autocura limitada de alguns humanoides; fome/sede vermelha. Números de dano ao jogador suprimidos; textos de crítico/erro preservados. Morcego visual maior/mais alto e sangue do Twilight vivo/emissivo. Vegetação mantém pressão e modelo/cor nativos, ondas alcançam sete blocos e duram quatro segundos. Skills na HUD separadas da barra de avisos; aba de trocas acessível, ausência de ofertas explicada, conversa renovada ao alternar tela. Extensão oficial Fresh Animations: Player 1.1 preparada para EMF/ETF existentes. [Implementação e validação](CORRECOES_043.md). Protocolo 29.

## Correção 0.42.1

Corrigida a seleção ambígua de `renderBatched` no mixin de vegetação. O relatório enviado identificou `Invalid descriptor` na inicialização. O hook agora aponta para a assinatura exata do NeoForge com `Function`, preservando a vegetação reativa. [Diagnóstico e validação](CORRECOES_0421.md). Protocolo 29 preservado.

## Entrega 0.42

Vampiresco: vínculos de cura/sangramento, forma de morcego com voo, escolta com vida própria e absorção limitada, Twilight, fraquezas solares e sede. Décima classe integrada às árvores/kit/menus/debug; IDs anteriores preservados. Sangramento do Arqueiro removido. Números distinguem condição por cor, inclusive dano próprio; HUD de skills reduzido. Folhas/vento com tintas de árvores, ondas ao voar sobre água, grama/samambaias reativas sem mudar blocos e folha seca em novos chunks de florestas selecionadas. [Mecânicas, recursos, referências e validação pendente](VAMPIRESCO_AMBIENTE_042.md).

## Correções 0.41.1

Arco do Caçador verifica munição antes de cobrar mana/ativar seu efeito, inclusive na entrada compartilhada. Nock cancelado não cobra; marca ainda pendente não é recobrada. Sangramento da passiva do Arqueiro exige flecha ou arma de corte, excluindo punhos, arcos usados como bastão e outros objetos. Avisos rotineiros de habilidades, itens, progressão e restrições usam a barra de ação em vez do chat. Dano periódico de sangramento não renova a agressão nativa, preservando o crédito do autor. [Prints, diagnóstico e limites](CORRECOES_0411.md).

## Entrega 0.41

Sangramento persistente em jogadores e mobs, dano periódico, rastros de sangue existentes e cura por bandagens. Chances por arma em datamaps e tooltip; Arqueiro migrado ao efeito registrado. Fresh Animations original + EMF/ETF instalados como pacote/mods separados na instância local. [Detalhes, versões e validação manual pendente](SANGRAMENTO_ANIMACOES_041.md). Bibliotecas de combate/animação do jogador permanecem para avaliação posterior. Nenhuma nova classe nesta etapa.

## Entrega 0.40

Removidos os tetos próprios de sangue por golpe/partículas/manchas; emissão em lotes preserva o volume solicitado. Caches distintos de superfície e pisos no minimapa, seleção de teto mais estável e sombreado subterrâneo independente da ordem de amostras. Sete prints vistos, anteriores à 0.39; nenhuma validação de jogo desta entrega. [Mudanças e limites do motor](CORRECOES_040.md). Animações externas permanecem somente em planejamento/pesquisa.

## Entrega 0.39

Números e textos maiores com salto/fade, sangue proporcional, retirada de impactos em ataques comuns, minimapa com piso estável e Aviário com lançamento separado da planagem, freio aéreo em pé e retomada ao soltar Agachar. [Detalhes e validação pendente](CORRECOES_039.md). Recursos externos de animação apenas pesquisados por escolha do usuário; nenhuma dependência nova instalada. Inventário de arquivos atualizado; revisão detalhada limitada aos caminhos alterados.

## Entrega 0.38

Sangue e marcas transitórias em superfícies, fumaça de impactos/explosões/decolagem e estilhaços de golpes fortes. Três providers de partículas nativas com limites, colisões e opções individuais. [Registro de implementação e validação pendente](PARTICULAS_038.md). Nenhuma nova biblioteca ou textura bitmap copiada. Revisão detalhada nesta entrega restrita aos caminhos alterados; o inventário registra os demais arquivos sem alegar validação completa.

## Sistemas implementados

| Sistema | Estado e arquivos principais |
|---|---|
| Classes | Dez: Guerreiro, Mago, Arqueiro, Necromante, Xamã, Aviário, Ilusionista, Hidromante, Piromante e Vampiresco. Três ramos por classe. RpgClass, RpgAbilities, RpgAviary, RpgNecromancer, RpgShaman, RpgSpecialists. |
| Progressão | Nível 1–100, XP RPG separado do vanilla. 51 nós por classe com posições próprias, preparação, principais e melhorias. RpgProfile, SkillTree, SkillTreeLayouts, SpecialistTrees. |
| Atributos | 160 nós; distribuição estável por UUID, caminhos conectados, arraste e zoom. AttributeTree, AttributeStats, AttributesScreen, GraphScreen. |
| Trabalhos e arsenal | Mineração, Agricultura e Arsenal; bloqueios de equipamentos, progressão de uso e lições da classe. Professions, ProfessionStore, EquipmentAccess, ClassJourney. |
| Itens de classe | Itens registrados e vinculados, ação no botão direito, requisitos, melhorias e recuperação persistente após morrer com espera para uso. ClassEquipment, ClassRelics, ClassRelicItem, RpgItems. |
| Comparação | Segurar o atalho configurado sobre o item compara com o equipado; também existe tela de arsenal. EquipmentComparison, EquipmentTooltip, ArsenalScreen. |
| Mana | Gasto validado pelo servidor, regeneração, HUD sincronizado e três poções de 35/80/160 MP com receitas e drops. RpgResources, ManaPotionItem, MobLoot. |
| Teste/debug | Teste libera nível máximo, árvore e equipamentos com custos normais; debug ignora os limites previstos. RpgDebug, RpgResources, RpgEvasion. Não sobrescreve a progressão real. |
| Combate | Esquiva com 1–3 cargas, parry apenas com escudo, erro/crítico do jogador e dos mobs, números de dano e confirmação de acerto. RpgEvasion, ShieldParry, CombatChances, CombatFeedback. |
| Criaturas | Níveis, vida/dano ajustados, elites com habilidades e afixos, avisos no chão e drops por nível. MobLevels, EliteMobs, EliteAbilities, EliteAffixes, MobLoot, EliteWarnings. |
| Etiquetas | Nome, nível, vida, elites, aliados/invocações e marcações de missão, com projeção/oclusão. EnemyNameplates, CameraProjection. Parte da função sugerida para Jade já existe aqui. |
| Missões | Caçadas recorrentes e diário; sete contratos de villagers com aceite, progresso, entrega e recompensa persistida antes do pagamento. QuestCatalog, VillagerQuests, MissionScreen, VillagerDialogueScreen. |
| Villagers — 0.32 | Abas Missões/Trocas usam o comércio vanilla, com preços e descontos nativos. Conversa validada por NPC, alcance e linha de visão; NPC para temporariamente e retoma ao sair, afastar, desconectar ou expirar a sessão. VillagerClient e dois mixins. |
| Exploração | Mais vilas em novos chunks pelo structure_set e redução de loot de baús vanilla ainda fechados. Não altera vilas existentes. VillageLoot e recursos de worldgen. |
| Câmera | Ombro ajustável com valores negativos, movimento independente, trava de alvo e mira validada. ShoulderCamera, TargetLock, MovementFacing, AbilityAim. |
| Voo | Aviário sem elytra, boost limitado pela progressão, queda reduzida e queda lenta. Meteoro em queda ou investida durante voo, com explosão no contato e cratera processada em lotes. RpgAviary, MeteorCrater. |
| Barrel roll | Port local NeoForge separado, controles acrobáticos e bridge opcional. Patch/licença em integrations/barrel-roll. Propulsão extra bloqueada durante voo da classe. |
| Minimap | Superfície de chunks carregados, textura 64x64, zoom, opacidade, posição por arraste e proteção dos efeitos. Na 0.32: formato circular/quadrado, marcador direcional desenhado e prévia sem o antigo limite de tamanho. SimpleMinimap, MinimapOptions, duas telas. |
| HUD | Modo sempre/dinâmico/oculto, visibilidade individual, fade, mana e esquiva. Na 0.32: cartões de classe/objetivos no centro esquerdo, pictogramas de habilidades com teclas, duração/recarga e estado. DynamicHud, SkillGlyphs, OxyRpgClient. |
| Inventário — 0.32 | Movimento com bindings vanilla remapeados; apenas inventário comum/criativo, sem combate/câmera e suspenso em pesquisa, jogo pausado ou janela sem foco. Toggle em R > Interface > Inventário. InventoryMovement e mixin de KeyboardInput. |
| Efeitos | Partículas/som nativos, rastros e ondas cosméticas com limites. SkillEffects, SkillEffectsRenderer, WorldLines, ProgressionEffects. |
| Persistência | Dados por mundo/UUID, perfis, profissões, recursos e recuperação de itens; registros duráveis para água/blocos temporários. ProfileStore, ProfessionStore, PlayerAttachments, FluidPockets, TemporaryBlocks. |
| Customização | Server configs NeoForge, Data Maps de entidades/armas, definições de classes/missões por datapack, validação e sincronização. RpgConfig, RpgContent, RpgDataMaps, RpgDataGenerator. |
| Desenvolvimento | Build Windows assemble, datagen, profiler e fixtures de teste/GameTest existentes. Não executados nesta revisão. RpgProfiler, RpgGameTests, src/test. |

## Bibliotecas e uso real

- **owo-lib 0.12.28+1.21.10:** seletor de classe e suporte de UI; demais telas usam widgets nativos com tema próprio. Não existe ainda editor completo de HUD por arraste.
- **Curios 13.0.0+1.21.10:** slot de relíquia e acesso ao artefato da classe. Na 0.33 há dois slots de anel, um de colar e quatro acessórios com efeitos reais; não há ainda conjuntos extensos.
- **GeckoLib 5.3-alpha-3:** selo animado. Não existem ainda chefes próprios, modelos animados de invocações nem poses de combate do jogador.
- **YACL 3.8.2+1.21.10:** configuração do port de voo. Ajuste local apenas do intervalo de NeoForge documentado em integrations/yacl.
- **Do Another Barrel Roll:** port 3.7.4+1.21.10 separado e adaptado localmente; não é uma versão oficial dos autores.
- **JEI:** deixado de lado por preferência do usuário. **Jade:** não integrado; evitar duplicar as etiquetas já presentes.

## Limitações e pendências reais

- Minimap agora possui fatia local de cavernas/Nether. Não há radar, waypoints ou mapa mundial persistente; atualização limitada a 128 amostras por tick.
- Posição dos cartões/skills é responsiva, mas ainda não existe posicionamento individual de todo o HUD pelo usuário.
- Os pictogramas da 0.32 cobrem habilidades principais e capstones; ícones de melhorias menores e atributos ainda usam recursos vanilla.
- A maioria das entidades/projéteis/invocações continua usando modelos nativos. Animação completa do jogador requer solução própria e compatibilidade específica.
- Escolha inicial de classe é pelo menu R; popup obrigatório ao criar mundo ainda não foi implementado.
- Contratos dos villagers estão em código; missões recorrentes do diário têm definição por datapack. Não há ainda sistema geral de NPCs, facções ou masmorras próprias.
- Movimento no inventário usa bindings de teclado. Remapeamentos de movimento para mouse/scancode não são suportados por esse polling; inventários, baús, fornalhas e comércio recebem movimento, suspenso por campos de texto/foco/pausa.
- Balanceamento exige avaliação em jogo, inclusive elites, voo e economia de poções. Compilar não comprova sensação de uso nem equilíbrio.
- Migrações de save, efeitos visuais, cliques e alternância de comércio desta versão ainda precisam de validação pessoal. Não declarar validação de versões novas com base em testes de versões antigas.

## Acréscimos da 0.33

- Bússola, avisos de região, visibilidade independente dos painéis, minimapa de cavernas e corrida em containers.
- Comércio com tema de missões, separação de nós e calor acima das habilidades.
- Nove itens: duas armas, quatro acessórios, botas e dois consumíveis; catálogo em Arsenal > Achados.
- Patrulhas de saqueadores e estruturas vanilla mais frequentes; complementos Explorify/Structory selecionados.
- Detalhes e limites: [EXPLORACAO_033.md](EXPLORACAO_033.md). Pesquisa/ideias: [REFERENCIAS_E_IDEIAS_033.md](REFERENCIAS_E_IDEIAS_033.md).

## Correções da 0.34

Minimap usa heightmap sincronizado e permite forçar subsolo; patrulhas aceitam vegetação e terreno irregular, com prazo menor; vilas novas expandem em até 112 blocos e recebem três anexos conectados às ruas. Bússola minimalista e corrida lateral/para trás em terceira pessoa independente. Nenhum item novo adicionado. [Detalhes e limites](CORRECOES_034.md).

## Preferências registradas

- Não executar testes, GameTests ou abrir o jogo sem solicitação explícita; seguir AGENTS.md.
- Preservar mundos e instalar apenas com o Minecraft fechado, com backup.
- Atalhos personalizáveis nas configurações do jogo; Usar Item para itens das classes.
- Teste gasta recursos; apenas debug é ilimitado.
- Indicadores de área com linhas no chão, sem usar nuvens de partículas para definir o alcance.
- Não adicionar JEI; aproveitar as funções nativas existentes antes de introduzir bibliotecas redundantes.

## Evidência desta atualização

Os prints 2026-10-11_03.21.04 e 03.22.54 foram inspecionados. A compilação assemble foi concluída. Não foram executados testes nem aberto o Minecraft para validar. O registro distingue implementação em código de comportamento confirmado pessoalmente no jogo.

## Desempenho e exploração — 0.35

Minimapa com orçamento de amostragem e uploads por mudança; filtro antecipado de ticks e cache curto de oclusão dos indicadores. Avisos de bioma/dimensão com cache de regiões recentes e fade interpolado, inspirados na leitura do Traveler’s Titles. Lithium 0.20.1 e FerriteCore 8.1.0 em versões oficiais NeoForge/1.21.10 instalados separadamente. Metadados do Barrel Roll corrigidos. Distâncias, vilas e saves preservados. Compilação concluída, ganho em jogo ainda não medido. [Diagnóstico, revisão e limites](DESEMPENHO_035.md).

## Identidade do combate — 0.36

Gestos de conjuração nos modelos nativos do jogador/armadura, cosméticos por classe e silhuetas orbitais no enxame do Ilusionista. Recuo/lateral para arqueiros e flanqueamento de parte dos inimigos corpo a corpo. Preparação de elites com contornos/legendas, especiais alternados e recuperação curta. Sem novos mobs, itens ou bibliotecas. Primeira etapa priorizada; chefes, eventos de vilas e especializações ainda não implementados. [Detalhes e limites](COMBATE_E_ANIMACOES_036.md). Compilação confirmada; comportamento no jogo ainda não validado.

## Debug — 0.37

Painel em R > Debug para operadores. Debug ON libera árvore completa e nível 100; nível 1–100, classe, atributos, Arsenal, recursos e recargas ajustáveis. Overrides não são gravados no perfil; pools de mana/esquiva isolados e cooldowns de skills/ação do item de classe por sessão. Modo teste mantém custos normais. Perfis temporários explicitamente identificados e bloqueados em ProfileStore. [Comandos, limitações e validação](DEBUG_037.md).

## Atualização 0.45.0 — 12/10/2026

Correções e limites registrados em CORRECOES_045.md; pesquisa de todos os links em REFERENCIAS_045.md. Compilação assemble, sem abrir jogo ou executar testes. Nenhuma dependência nova. Proposta de Assassino + novo Arqueiro registrada; classe atual preservada. Instalação e SHA256 em INSTALACAO_045.json.
