# Oxy RPG 0.33.0 — interface e exploração

Data: 11/10/2026. Minecraft 1.21.10 / NeoForge 21.10.64 / protocolo 23.

## Correções solicitadas

- Os últimos 10 prints da instância foram inspecionados: 01.16.15 até 03.57.20. Os três mais recentes mostram a 0.32; os demais preservam problemas anteriores, incluindo elementos vanilla e nós próximos demais.
- Minimap troca automaticamente a superfície pelo piso da caverna na altura do jogador. Paredes ficam mais escuras, corredores/pisos ficam legíveis e dimensões com teto recebem mapa local. Usa somente chunks carregados, 128 amostras por tick e varredura vertical limitada a 24 blocos. Não é radar ou mapa persistente do mundo; mudanças levam até uma passagem de amostragem para completar.
- Classe/XP e missões têm modo próprio: sempre visível, dinâmico ou oculto, independente de H. Em R > Interface, selecione o modo de Painéis. O atalho Alternar painéis está disponível nos controles, sem tecla padrão. F1 e tela de debug ainda ocultam tudo.
- Movimento abrange AbstractContainerScreen: inventário, criativo, baús, fornalhas e comércio. Mantém a corrida quando a tela abre. Campos de texto, pausa, morte e perda de foco interrompem o polling. A causa da corrida era KeyModifier.NONE com contexto GUI, que rejeitava movimento ao segurar Ctrl; agora segue a semântica IN_GAME. Mouse/scancode não fazem parte desse polling.
- Trocas usam moldura, cores, botões e slots do mesmo tema das missões, mantendo o MerchantMenu nativo, seleção de ofertas, preços, descontos, progressão, tooltips e transferência de itens. Villager para durante a sessão e retoma após seu encerramento.
- Calor do Piromaníaco tem componente próprio e fica acima dos cartões, corrigindo a sobreposição nos prints. XP/pontos não compartilham mais a mesma linha. Árvores recebem separação mínima determinística entre nós; IDs, conexões, custos e desbloqueios permanecem estáveis.

## Novos recursos

### Orientação

Bússola horizontal no topo, orientada pela câmera, com pontos cardeais e diagonais. Avisos de bioma/dimensão possuem entrada e saída suaves, aguardam estabilidade da região e têm intervalo entre notificações. Ambos podem ser desligados em R > Interface > Explorar. Configuração local em oxyrpg-exploration.properties.

### Equipamento

| Item | Efeito | Obtenção |
|---|---|---|
| Lâmina da Geada | Espada de ferro; usar lança corte de até 7 blocos, lentidão e empurrão. 18 MP, 8 s. | Capitão saqueador, nível 20+; chance de drop. |
| Lâmina da Brasa | Espada de diamante; usar lança onda de até 10 blocos, dano e fogo por 4 s. 24 MP, 11 s. | Capitão saqueador, nível 40+; chance de drop. |
| Anel do Foco | +30 mana máxima. | Recompensa possível de capitão. |
| Anel do Vigor | +8% dano de ataque. | Recompensa possível de capitão. |
| Amuleto da Guarda | +3 armadura. | Recompensa possível de capitão. |
| Amuleto do Andarilho | +8% velocidade. | Recompensa possível de capitão. |
| Botas do Desbravador | Armadura de ferro; 15% de redução multiplicativa de dano de queda. | Receita e capitães. |
| Bandagem | Restaura 2 corações após consumir. | Papel e linha. |
| Antídoto | Remove veneno e fraqueza após consumir. | Olho de aranha, mel e garrafa. |

- Curios: dois slots de anel e um de colar, além da relíquia existente. Uma cópia por tipo de acessório; escolha entre os dois colares.
- Armas e acessórios possuem geometria original em JSON usando materiais/texturas nativos. Botas usam aparência de ferro; antídoto usa garrafa com cor própria.
- Custos e efeitos são autoritativos no servidor; as ondas colidem com blocos e exigem linha de visão. Debug dispensa mana/recarga das armas; teste gasta normalmente.
- Bandagem/antídoto compartilham recarga de 8 s. Entradas no livro de receitas são desbloqueadas pelos ingredientes.
- R > Arsenal > Achados de exploração apresenta descrição e origem dos nove itens sem conceder ou transferir itens.

### Patrulhas

Grupos de 3 a 5 saqueadores surgem em terreno externo carregado do Mundo Superior, com capitão elite e vindicator a partir do nível 35. Aproveitam os níveis, elites, erro/crítico e avisos de combate do Oxy RPG. O capitão concede XP superior ao elite comum, esmeraldas, duas poções e uma recompensa de equipamento; as lâminas exigem níveis 20/40 na geração de loot.

Não aparecem no Pacífico, em criativo/espectador, com doMobSpawning desativado, em cavernas ou próximos de villagers/vilas. A distância mínima de jogadores é 32 blocos, há limite de grupo próximo e intervalo após sucesso de 10 minutos. A primeira tentativa ocorre após 3–5 minutos. Não forçam carregamento/geração de chunks. São separados das invasões vanilla de vilas. Patrulhas antigas sem jogadores por perto são removidas após 20 minutos. Controle de mundo: exploration.raiderPatrols no config do servidor. Loot respeita doMobLoot e exige morte atribuída a jogador/invocação.

### Estruturas

Espaçamentos vanilla em chunks: pirâmides, iglus, templos e cabanas/postos 32→25; portais 40→32; naufrágios 24→19; ruínas arqueológicas/câmaras 34→27; mansões 80→64; cidades ancestrais 24→20; monumentos 32→26; Nether 27→22; cidades do End 20→17. Minas: frequência 0,004→0,006. Vilas continuam em 26. Preservados salts, separações, pesos, métodos de distribuição e exclusão de postos perto de vilas. Loot reduzido de vilas permanece.

Complementos oficiais separados, sem copiar seus JARs para o repositório:

- [Explorify 1.6.4](https://modrinth.com/datapack/explorify/version/9vHj342y): estruturas de exploração, suporte NeoForge/1.21.10 e formato de dados até 88. Licença ARR.
- [Structory 1.3.12](https://modrinth.com/datapack/structory/version/qm2RM1eD): ruínas, torres, assentamentos e construções atmosféricas. Suporte NeoForge/1.21.10 e formato até 88. Stardust Labs License.

Escolhidas versões com metadados alinhados ao data pack 88 do Minecraft 1.21.10. Downloads oficiais, SHA-512 verificado. Sem dependências obrigatórias adicionais. Hashes e URLs em ESTRUTURAS_033.json. **Estruturas novas aparecem em chunks novos; terrenos existentes não são apagados/regerados.**

## Validação e instalação

Compilado com build-windows.bat (assemble). Não foram executados testes, GameTests ou cliente, conforme AGENTS.md. Revisão de fontes e recursos não confirma aparência, comportamento em jogo ou balanceamento. Instalação somente com jogo fechado, backup de saves/config e JAR anterior fora de mods. Instalado com o jogo fechado em 11/10/2026. Backup saves/config/JAR anterior: mod-backups/20261011-042427-0.33.0. JAR 0.33 e os dois complementos conferidos por SHA-256 após cópia. Registro completo em [INSTALACAO_033.json](INSTALACAO_033.json).
