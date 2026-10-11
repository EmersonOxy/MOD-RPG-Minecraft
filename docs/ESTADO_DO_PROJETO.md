# Estado do Oxy RPG — revisão 0.34.0

Revisão registrada em 11/10/2026. Minecraft 1.21.10, NeoForge 21.10.64, JDK 21, protocolo de rede 23. Este registro substitui descrições antigas do README. Os documentos numerados são históricos de cada entrega.

A revisão inventariou fontes Java, registros, eventos, mixins, telas, recursos JSON, geração de dados, integração e persistência. Código das áreas alteradas foi inspecionado em detalhe. Inventário de arquivos e hashes: [INVENTARIO_CODIGO_034.json](INVENTARIO_CODIGO_034.json). Não equivale a validação integral dentro do jogo nem a auditoria de segurança.

## Sistemas implementados

| Sistema | Estado e arquivos principais |
|---|---|
| Classes | Nove: Guerreiro, Mago, Arqueiro, Necromante, Xamã, Aviário, Ilusionista, Hidromante e Piromante. Três ramos por classe. RpgClass, RpgAbilities, RpgAviary, RpgNecromancer, RpgShaman, RpgSpecialists. |
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
