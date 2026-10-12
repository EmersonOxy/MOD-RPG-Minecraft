# Oxy RPG — Auditoria de game design, progressão e balanceamento Survival PvPvE

**Data:** 12/10/2026. **Versão examinada:** 0.50.1. **Referência Git:** `6c922dc5aa24901accf3a17ef88db40176df8506` (árvore de trabalho limpa antes desta documentação). **Alvo:** Minecraft 1.21.10, NeoForge 21.10.64, Java 21.

Esta é uma análise estática do código e dos recursos do projeto. Nenhuma alteração de implementação, configuração, instalação, compilação, teste ou execução do jogo foi feita para esta auditoria. O único arquivo criado é este relatório. Números derivados de fórmulas são cálculos analíticos, não medições de combate. As integrações presentes no código são examinadas; o comportamento de um conjunto arbitrário de mods instalados não está certificado.

**Convenções de evidência:** **C** = comportamento confirmado por um caminho de execução; **R** = risco plausível cujo resultado requer teste; **D** = decisão/sugestão opcional de design. “Confirmado” não significa “validado dentro do jogo”. Dano é expresso em pontos de vida: 2 pontos equivalem a um coração. Tempos em segundos pressupõem 20 ticks/s; o código usa ticks, portanto TPS baixo aumenta a duração real.

## 1. Leitura principal para a revisão externa

O projeto possui progressão persistente, 12 classes, 36 habilidades, profissões, equipamento vinculado, mana, esquiva/parry, elites, invocações, sangramento e sistemas de exploração. Entretanto, **a cobertura ofensiva de PvP é parcial e desigual**. O filtro compartilhado `RpgAbilities.attackable()` aceita `Mob`, não jogadores. Muitas habilidades, controles, curas por dano e bônus de classe só funcionam contra criaturas. Flechas físicas do novo Arqueiro, poções do Xamã, ataques vanilla e alguns efeitos do Deus do Trovão seguem caminhos diferentes e podem atingir jogadores.

Isso impede avaliar as classes como se todas as descrições também valessem em duelos. Uma revisão de dano numérico deve vir depois de definir explicitamente quais habilidades pertencem ao PvP, quais são apenas PvE e quais precisam de regras diferentes por alvo.

Há outros pontos estruturais: a árvore de classe pode ser inteiramente comprada; a árvore de atributos tem escolhas permanentes; o crescimento de vida das criaturas chega ao teto nativo de 1.024; alguns textos anunciam melhorias que não são executadas; a intangibilidade atravessa mais blocos que os níveis da árvore sugerem; os mecanismos de proteção de terreno variam entre habilidades. Os diagnósticos comprovados e os riscos estão separados na seção 9.

## 2. Arquitetura, fórmulas e persistência

### 2.1 Nível, XP e relação com o vanilla

`RpgProfile` mantém classe, nível, XP parcial, abates, três recargas, máscara de habilidades, atributos e progresso das missões recorrentes. Nível mínimo 1, máximo 100. Para passar do nível `L` ao seguinte:

```text
XP necessário(L) = 100 × L
XP total de 1 a 100 = Σ(L=1..99) 100L = 495.000
Pontos de classe disponíveis = floor(L/2) − quantidade de nós pagos comprados
Pontos de atributos disponíveis = L − 1 − total de atributos comprados
```

XP excedente passa para os níveis seguintes; no 100 o XP parcial é zero. Escolher uma classe é necessário para progredir por abates. O XP RPG é separado das orbes, níveis de encantamento e experiência vanilla: ganhar RPG não concede o mesmo número de níveis de encantamento.

Em `OxyRpg.onEnemyDeath()`, um inimigo válido concede base 25 XP com crédito direto, ou 12 quando atribuído à invocação, multiplicado pelos ajustes de XP da entidade e elite. A cada 10 abates há +100 XP. As cinco missões recorrentes de `QuestCatalog` concedem 100/150/150/200/175 XP e reiniciam o ciclo. Não há multiplicador de XP de abate baseado no nível da criatura. Missões profissionais e lições da classe usam XP de trabalho, não esta mesma reserva. Mobs pacíficos, mesmo os golems que recebem nível, não seguem automaticamente o caminho `Enemy` de XP RPG.

**Evidência:** `RpgProfile.gainXp/defeatedEnemy`, `OxyRpg.onEnemyDeath`, `QuestCatalog.quest`, `EliteMobs.xp` [S01, S02, S15].

### 2.2 Atributos e limites efetivamente alcançáveis

| Atributo comprado | Efeito por ponto | Limite da árvore atual por tipo |
|---|---|---:|
| Vida | +2 à vida máxima | 32 pontos / +64 vida |
| Dano | multiplicador final `1 + 0,02 × pontos` | 32 pontos / ×1,64 |
| Sorte | +0,25 ao atributo vanilla de sorte; fórmulas próprias usam os pontos diretamente | 32 pontos |
| Defesa | +0,5 de armadura | 32 pontos / +16 armadura |
| Mana | mana máxima `100 + 5 × pontos` | 32 pontos / 260 MP antes de equipamento |

Há 160 nós, oito raios de 20 posições, 32 nós de cada tipo. A distribuição é determinística por UUID, embaralhada; não muda a cada abertura. Os 99 pontos totais não completam os 160 nós. Após os oito nós de origem, exige-se pelo menos **um** dos pais desbloqueados; conexões adicionais aparecem a cada terceira camada. Não há requisito de nível individual além do orçamento de pontos.

O validador `AttributeStats` aceita até 99 por atributo; isso não significa que a árvore nova permita 99 do mesmo tipo. Perfis legados válidos podem conter uma distribuição diferente. Debug/teste com atributos máximos coloca 32 em cada tipo, totalizando **160**, acima do orçamento normal de 99: é inadequado usar esse perfil como uma build survival de referência.

Vida final também inclui fraquezas de classe, +2 do Guerreiro no nó 7 e +2 de Agricultura no nível 20. Armadura pode incluir equipamento e amuleto; a resistência resultante não é uma porcentagem linear de armadura. O atributo vanilla de vida máxima tem intervalo **1..1.024**; armadura, velocidade e outros atributos conservam seus limites nativos. Não se deve converter multiplicadores de vida de elites diretamente em HP acima desse teto.

**Evidência:** `AttributeStats`, `AttributeTree.types/parents/available`, `RpgResources.update`, `RpgAbilities.stats`, `ClassRules.refresh`, `Professions.refreshModifiers`; fonte nativa `Attributes.MAX_HEALTH` [S03, S04, S11].

### 2.3 Mana e custos

```text
MP máximo = 100 + 5M + bônus do equipamento de classe + bônus de acessórios
Regeneração = 0,1 MP/tick = 2 MP/s, após 60 ticks sem gasto positivo
Custo principal = ceil(base × multiplicador da classe × config de mana
                       × max(0,4; 1 − 0,03 × nós de eficiência))
Custo após equipamento = desconto específico de ClassEquipment.discount()
```

Os multiplicadores de mana/dano/recarga/duração das 12 definições distribuídas são todos **1**. O equipamento de Mago desconta 5/10/20% nos graus 1/2/3; o de Xamã desconta 10% no grau 3. O desconto arredonda para baixo e preserva custo mínimo 1 quando a base é positiva. Mana zero não autoriza uma habilidade que tenha custo positivo; a verificação é no servidor.

O custo e a recarga normalmente são cobrados antes da ativação. Validações específicas verificam alvo, flechas, calor, água e outras condições antes dessa cobrança. Não há um mecanismo geral de estorno se uma ativação posterior falhar ou não produzir resultado. Errar uma habilidade válida não significa necessariamente falha da cobrança.

**Exceções importantes:** permanência do Vampiresco em morcego cobra 1 MP/s sem aplicar a fórmula principal; cada poção do Xamã custa 4/5 MP, −1 pelo nó 20, com desconto de equipamento; relíquias custam 10 MP; lâminas de aventura 18/24 MP. Estes gastos não usam todos os multiplicadores de eficiência/configuração. Gasto contínuo do morcego reinicia o bloqueio de regeneração: o voo não é compensado automaticamente pela regeneração de 2 MP/s.

Poções de mana restauram 35/80/160 MP, limitadas pelo máximo, com recarga compartilhada de 2 s e consumo por animação de bebida vanilla. Morte pode repor mana máxima no caminho de clone do jogador; logout salva mana e horário. Na leitura do arquivo, a regeneração atrasada usa o tempo do mundo, não o relógio do computador: um servidor que continua rodando pode regenerar durante a ausência; fechar completamente o mundo não acrescenta horas de regeneração.

**Evidência:** `RpgResources.cost/baseCost/update/spend/load/clonePlayer`, `ClassEquipment.discount`, `RpgVampire.tick`, `RpgShaman.potion`, `AdventureItems`, `ClassRelics.use` [S04, S08, S10, S12].

### 2.4 Referência comum de custos e recargas das habilidades

Cada célula contém **inicial → completa**. São bases, antes dos nós genéricos, equipamento e multiplicadores de administrador. `T0` é a duração calculada de Decolagem; a recarga do Aviário usa esse valor, depois aplica os multiplicadores de recarga.

| Classe | Mana S1 / S2 / S3 | Recarga S1 / S2 / S3, segundos |
|---|---|---|
| Guerreiro | 0→12 / 20→20 / 25→25 | 12→25 / 12→40 / 12→45 |
| Mago | 8→20 / 8→30 / 10→35 | 12→25 / 12→30 / 12→40 |
| Assassino | 5→12 / 8→20 / 5→15 | 12→25 / 12→35 / 12→45 |
| Arqueiro | 8→18 / 12→28 / 16→30 | 12→25 / 12→35 / 12→45 |
| Necromante | 10→20 / 15→40 / 15→35 | 12→25 / 12→40 / 12→45 |
| Xamã | 8→20 / 15→35 / 10→20 | 12→30 / 12→40 / 12→35 |
| Aviário | 20→20 / passiva / 30→30 | `(T0+200)/20` / passiva / 30→30 |
| Ilusionista | 14→28 / 12→24 / 18→35 | 12→25 / 12→35 / 12→45 |
| Hidromante | 8→8 / 24→24 / 25→25 | 5→5 / 12→35 / 12→45 |
| Piromaníaco | 10→20 / 10→16 / 20→35 | 12→25 / 6→6 / 12→45 |
| Vampiresco | 10→18 / 14→24 / 16→30 | 25→25 / 35→35 / 45→45 |
| Deus do Trovão | 10→20 / 12→30 / 14→28 | 12→25 / 12→35 / 12→45 |

Reativações reconhecidas dispensam nova recarga principal: boost aéreo base 8 MP; novo magma 10 MP; retorno de morcego 0; segundo dash do Assassino e detonação de fogos usam base 4. Elas continuam tendo as condições e intervalos internos descritos em cada classe.

```text
Duração genérica = round(base em ticks × (1 + 0,05 × nós de persistência)
                         × multiplicador de duração da classe)
Recarga genérica = round(base em ticks × max(0,4; 1 − 0,03 × nós de recuperação)
                        × multiplicador de recarga da classe × config de recarga)
```

Nem toda janela interna, duração de status, invocação, chão ou projétil passa por essa fórmula. Os casos são apontados abaixo. No Aviário, ampliar a duração também amplia a **base** da recarga; não são parâmetros independentes.

### 2.5 Dano: ordem real e acumulação

O projeto não tem uma fórmula única que substitua o sistema vanilla. Há fases diferentes:

1. Dano nativo da arma/projétil/efeito, cooldown de ataque, críticos vanilla e encantamentos.
2. `LivingIncomingDamageEvent`: esquiva, imunidades, chance de erro/crítico RPG e reduções/absorção especiais.
3. Reduções nativas, armadura e efeitos. A documentação do próprio NeoForge confirma que `LivingDamageEvent.Pre` ocorre **depois** de armadura e modificadores de poção, e antes da absorção.
4. Os handlers `Pre` do Oxy aplicam resistências, regras de classe, bônus específicos, equipamento, escala de mobs, nós genéricos e atributos de dano. Algumas operações são aditivas: o Deus do Trovão acrescenta +2 nesta fase.
5. Absorção e perda efetiva de vida; os handlers `Post` curam, aplicam efeitos e exibem feedback a partir do dano efetivamente aceito.

Para um acerto cujo caminho usa os modificadores compartilhados, um trecho da fórmula final é:

```text
Fglobal = configDano × multiplicadorDanoClasse
          × (1 + 0,03 × nós de potência) × (1 + 0,02 × atributoDano)
Farma = ajuste do data map da arma, apenas se directEntity == jogador
DpréAbsorção = composição ordenada dos handlers Pre sobre DpósMitigação
```

Não é correto escrever “dano base × todos os bônus, depois armadura” para todos os ataques: boa parte da escala Oxy entra depois da mitigação. Poções de Força/Resistência conservam a lógica nativa; bônus de vida/armadura não viram multiplicadores diretos de vida perdida. Flechas nativas usam velocidade, `baseDamage`, arredondamento e crítico; `baseDamage=2,2` não significa perda fixa de 2,2 HP.

Modificadores de atributos identificados por `ResourceLocation` são substituídos quando mudam, em vez de empilhados a cada sync. Bônus genéricos de nós somam dentro do seu grupo; grupos diferentes e várias regras de classe multiplicam. Modificadores `ADD_VALUE`, `ADD_MULTIPLIED_BASE` e `ADD_MULTIPLIED_TOTAL` seguem operações nativas distintas. Não foi encontrado acúmulo infinito causado apenas por reabrir o menu.

**Crítico/erro RPG:** base 5% erro, 10% crítico e ×1,5 no dano crítico. Sorte `S`:

```text
Erro = max(min(baseErro; 0,01); baseErro − 0,001S)
Crítico = min(1; baseCrítico + 0,0025S + bônus crítico da arma)
```

Com 32 de sorte e bases padrão: erro 1,8%, crítico 18%, antes de bônus de arma. Com legado 99: erro atinge o piso 1%, crítico 34,75%. O código reconhece crítico vanilla para evitar simplesmente aplicar outro ×1,5 sobre ele. O alcance do sorteio é definido por tipos de dano: `player_attack`, projéteis, magia, magia indireta e raio. Fontes próprias de pressão, ilusão, brasa e mordida não recebem automaticamente esse sorteio. Inimigos elegíveis têm 5% erro/10% crítico/×1,5; invocações e temporários são excluídos. Flecha marcada como guiada dispensa o erro RPG, mas ainda pode bater em blocos, escudo ou não colidir com o alvo.

**Evidência:** registro e ordem dos eventos em `OxyRpg`, `RpgResources.damage`, `CombatChances.incoming`, `CombatFeedback.vanillaCritical`, `RpgDataMaps.resistance`, `AdvancedClasses.damage` [S02, S04, S09, S13].

### 2.6 Profissões, arsenal e equipamento

Mineração, Agricultura e Arsenal vão de 1 a 20, com XP separado. A passagem do nível profissional `L` custa `100L × 0,5` nos níveis 1..5, `100L × 0,75` nos 6..9 e `100L` nos 10..19; total até 20: **17.500 XP**.

Mineração: 3 XP por bloco natural elegível, 15 por minério reconhecido. Agricultura: 3 por cultivo maduro/verruga, 2 por cacau, 15 por melancia/abóbora. Arsenal: 15 por morte de `Enemy` creditada; contratos e três lições de classe acrescentam XP, as lições dão 50/100/150. Colocação de blocos relevantes é registrada para impedir parte do farming de recolocar/quebrar. Movimentação desses blocos por pistões e outras transformações não está coberta por um rastreador geral: requer teste.

Restrições de ferramentas/equipamento vanilla usam patamares 1/4/8/12/16 para madeira/couro, pedra/corrente/cobre, ferro/ouro, diamante e netherite. Arco/escudo pedem Arsenal 4; besta 8; tridente/maça 16. Enxadas usam Agricultura; picaretas/pás Mineração; armas/armaduras Arsenal. Há requisitos próprios para plantar culturas. **A verificação profissional ignora namespace diferente de `minecraft`**: itens de mods, inclusive parte do arsenal Oxy, não herdam essas exigências por mera equivalência com diamante/netherite.

No 20: Mineração +10% de velocidade de quebrar blocos; Agricultura +2 vida máxima; Arsenal +5% ao atributo de dano de ataque. Estes são modificadores nativos, não o mesmo bônus final do atributo RPG de dano.

Equipamento vinculado tem dono, classe, grau e parte. Grau 1 inicial; grau 2 exige nível RPG 20 e Arsenal 8; grau 3 nível 50 e Arsenal 16. A ativação procura o **maior grau** válido na mão principal, secundária, cabeça, peito e pés; não soma todos os graus nem exige necessariamente a arma daquele grau. Guerreiro/Aviário recebem ×`1+0,05grau` no dano aplicável; Arqueiro nas flechas; Necromante amplia vida/dano dos servos; Mago ganha mana/desconto/frio; Xamã amplia suas fórmulas. Algumas armas recebem encantamentos, por exemplo Afiada II/IV ou Poder II/IV; os efeitos só influenciam caminhos que realmente usam a arma nativa.

Os itens de classe são inquebráveis/vinculados, entregues uma vez por grau e repostos através da fila de recuperação, sem recolher os itens perdidos de todos os jogadores. Morrer retira esses itens dos drops e impõe recuperação de **60 s**, além de atualizar recargas principais para pelo menos esse instante. Isso é uma alteração intencional da progressão survival; não foi classificada como defeito apenas por diferir do vanilla. Resta medir se arma inicial, encantamentos e bônus de grau tornam etapas normais de criação dispensáveis cedo demais.

Curios: dois slots de anel, um colar e um `oxy_relic`. Acessórios Oxy: anel de foco +30 mana; vigor +8% ao atributo de ataque; proteção +3 armadura; viajante +8% movimento. `findFirst` evita simplesmente somar duas cópias do mesmo bônus nesses caminhos. Botas de trilha reduzem queda em 15%; essa redução pode multiplicar a do Aviário, por exemplo `0,85 × 0,7 = 0,595`, não uma soma de 45%.

**Evidência:** `Professions.requirement/award/placed/broken/refreshModifiers`, `ProfessionStore`, `ClassEquipment.active/stage/weapon/armor`, `ClassRelics.drops/clonePlayer`, `AdventureItems` [S11, S12].

### 2.7 Troca, redistribuição e persistência

Em survival normal, a escolha de classe é única. `/rpg resetclasse` é administrativo e reseta classe, nível, XP, skills, atributos e recargas; preserva abates e missão recorrente. Não há um respec comum que devolva apenas pontos de árvore. Profissões/contratos/entregas de equipamento têm armazenamento próprio e não são automaticamente zerados pelo reset da classe.

| Estado | Morte | Logout/reentrada | Dimensão/reinício |
|---|---|---|---|
| Classe, nível, XP, nós, atributos, missão e recargas | attachment com `copyOnDeath`; recuperação de relíquias impõe espera | attachment serializado | perfil pertence ao mundo/UUID, não à dimensão |
| Mana | caminho de clone repõe máximo | arquivo próprio salvo; reserva em memória descartada | relógio usa tempo do mundo; confirmar transições com consumíveis |
| Trabalhos, contratos, lições e recibos | arquivos próprios persistem | permanecem | não dependem do chunk do NPC para progresso salvo |
| Energia de esquiva | clonada; detalhes de intervalos são temporários | energia/horário em NBT; i-frames e último uso não são persistidos como combate | conferir reentrada durante esquiva |
| Debug/teste | attachment separado copiado | restaura só se ainda autorizado | flags e recargas salvas; mana temporária não é o perfil real |
| Invocações/disfarce/enxames | rotinas de limpeza | grupos/copias removidos; entidades temporárias carregadas sem sessão são rejeitadas | modules com referência de mundo/dono limpam ao mudar; projéteis podem ter caminhos distintos |
| Sangramento | efeito nativo no alvo; não há regra universal de preservação no clone | duração infinita de efeito/UUID do causador na criatura; dono pode estar offline | fonte perde crédito resolvível se causador ausente; rastro visual/lógico é outro estado |
| Gelo/teias/bolhas | ledger do mundo | não depende da presença do lançador | restauração adiada em chunks descarregados; ledger persistente |
| Cratera já enfileirada | fila independente do efeito de voo | não há cancelamento explícito pelo logout do dono | fila em memória não é ledger de reversão; alterações já feitas são permanentes |

A migração de perfil legado lê `oxyrpg/<UUID>.properties` uma vez para `PlayerAttachments.PROFILE`; após `migrated=true`, esse arquivo não é a fonte ativa. Arquivo legado inválido não é silenciosamente sobrescrito com um perfil novo. O administrador não deve editar o `.properties` antigo esperando alterar um jogador já migrado.

Limpeza de um módulo não equivale a remover todo `MobEffect` vanilla já aplicado: Força, Velocidade, Resistência etc. podem continuar pelo tempo restante. Mapas como paralisia/rastros e projéteis têm rotinas diferentes; a lista de testes exige logout, morte e dimensão em cada família, sem presumir resultado uniforme.

**Evidência:** `PlayerAttachments`, `ProfileStore.load/save`, `RpgDebug.restore`, `RpgResources.logout`, `RpgAbilities.clear`, módulos e ledgers [S01, S04, S06–S10, S17, S20].

## 3. Árvores de habilidades e significado das builds

### 3.1 Orçamento e topologia

Cada classe tem 51 IDs estáveis, versão 3: 0 e 1 gratuitos, 49 pagos de um ponto. No nível 100 há 50 pontos; é possível completar os 49 pagos e sobrar um. O orçamento permite completar no nível 98, desde que todos os pré-requisitos sejam respeitados. Não existem exclusões mútuas, limite de ramo ou custo crescente por nó. O desenho muda por classe; a topologia lógica e a quantidade de nós permanecem compartilhadas.

IDs 0..20 formam três ramos de sete nós. Habilidade inicial nos IDs 1/8/15; completa nos 3/10/17. Níveis dos primeiros 21 IDs:

```text
IDs   0  1  2  3  4  5  6 | 7  8  9 10 11 12 13 |14 15 16 17 18 19 20
Nv.   1  1  5 10 15 20 25 | 5 10 15 20 25 30 35 |10 15 20 30 35 40 45
```

Preparação/domínio adicionais, IDs 21/22, 31/32 e 41/42, passam a integrar o caminho das habilidades completas. Todos os pais indicados devem existir (**AND** na árvore de classe). Capstones antigos exigem duas conexões. Nos dez nós novos por ramo, etapas 0/1 exigem nível 5/15/20 conforme ramo, e etapas 2..9 níveis 30/38/46/54/62/70/78/86. Preparação depende do antigo primeiro upgrade; domínio depende da preparação; as etapas 2..5 dependem do capstone; etapas 6..9 dependem do respectivo nó quatro IDs antes.

O validador impede gastar pontos inexistentes, comprar fora do nível, ignorar pais ou usar bits fora da máscara. Debug pode incluir pais automaticamente e exceder nível/orçamento, dentro de autorização administrativa.

### 3.2 Bônus genéricos: globais, mesmo quando desenhados em um ramo

| Tipo | IDs por ramo | Total na árvore | Resultado completo |
|---|---|---:|---:|
| Potência | preparação, domínio, potência I/II | 12 | dano ×1,36 |
| Persistência | duração I/II | 6 | duração ×1,30 |
| Eficiência | custo I/II | 6 | custo ×0,82 antes de arredondamento |
| Recuperação | recarga I/II | 6 | recarga ×0,82 antes de arredondamento |

Os bônus contam a máscara toda, não só a habilidade daquele ramo. Comprar potência no ramo de mobilidade também amplia dano em outras habilidades e ataques que passam pelo handler comum. Não são doze multiplicadores ×1,03: são uma soma interna que resulta em ×1,36. Dano de atributo, equipamento e buffs específicos podem multiplicar esse resultado.

Os nós de preparação/domínio são obrigatórios para chegar às habilidades completas, inclusive para uma build cuja intenção seja suporte/movimento. Isso confirma um investimento comum em dano para todos esses caminhos; não prova que cada nó seja igualmente valioso em toda situação.

**Conclusão de design:** a árvore de classe distingue principalmente a **ordem de evolução** até o máximo. A árvore de atributos distingue alocação final, por 99 pontos para 160 nós e distribuição por UUID. Para justificar especializações permanentes na classe seriam necessárias regras novas; este relatório não as implementa nem determina se o projeto deve adotá-las.

### 3.3 Divergências verificáveis de descrição

| Classe/nó | Descrição | Execução atual |
|---|---|---|
| Guerreiro 10/12 | acertos recuperam fome; upgrade +1 comida | `damagePost` causa **exaustão** 0,8 ou 0,6; não adiciona comida |
| Guerreiro 19 | penalidade do Bastião −20% | acrescenta +0,03 de movimento; não reduz em 20% o modificador de Lentidão |
| Ilusionista 2 | cópias resistem a golpe adicional | cópia recebe vida máxima/atual do alvo por `MimicAppearance`; não há bônus de vida/absorção específico pelo nó 2 |
| Ilusionista 6 | cópias causam mais dano | aumenta contagem; não há multiplicador de dano das cópias pelo nó |
| Ilusionista 13 | controlados ficam mais vulneráveis | não há aumento de dano recebido específico neste caminho |
| Ilusionista 19 | amplia passagem para portas/cercas/grades | fallback de colisão já permite qualquer bloco destrutível sem block entity, com poucas exceções, nos três níveis |
| Hidromante 9 | jato causa mais dano | fórmula de dano do jato não consulta o nó 9 |
| Hidromante 13 | mais manifestações **e duração do jato** | altera manifestações; duração genérica do jato não aumenta pelo nó 13 |
| Piromaníaco 2 | fogos duram mais | duração base de S1 continua 200 ticks; não há incremento específico pelo nó 2 |
| Assassino 8/10 | atacar encerra invisibilidade | limpeza por dano está dentro do filtro de mobs; dano contra jogador não chega a ela |
| Xamã 6 | raio encadeia a outro hostil | filtro aceita mobs pacíficos também |

Nós “prepara o ramo” não são automaticamente defeitos: funcionam como pré-requisitos, sem prometer bônus próprio. IDs com efeito secundário e global não devem ser marcados como totalmente inúteis só porque uma frase está errada. A tabela identifica a diferença concreta a revisar. [S05–S10]

## 4. Catálogo das 12 classes e 36 habilidades

### Convenções do catálogo

S1/S2/S3 correspondem aos slots 0/1/2 do código. `nX` significa ter o nó de ID X. `L` é nível e `P(L)=0,65+0,35L/100`, usado pelos especialistas e parte das invocações. Danos descritos são argumentos da chamada de dano, antes de mitigações/multiplicadores posteriores, salvo menção a `Post`. Custos/recargas estão na tabela 2.4. As durações de perfil abaixo passam pela fórmula genérica quando o módulo de fato a chama; incrementos feitos após essa chamada são explicitados.

**Alvos padrão:** a maioria das áreas/projéteis customizados aceita mobs pacíficos e hostis, exclui invocações próprias/temporários e **não aceita jogadores**. Isso não significa que todo mob aceite todos os efeitos: imunidade nativa, boss multipart, visibilidade, colisão e resistência específica precisam dos testes da seção 10. Não há um sistema compartilhado de resistência progressiva a CC para chefes.

### 4.1 Guerreiro

**Identidade:** combate próximo, dano circular e sustentação com custo alimentar; defesa ativa. Armas de projétil e tridente são bloqueados pela regra de classe. Armaduras vanilla são compatíveis, sujeitas a Arsenal. Passivas: dano corpo a corpo +5% contra mobs pelo nó gratuito 0; após abate +5% por 2 s (+1 s n2); +2 vida n7; roubo de vida n8/9; resistência a empurrão +0,1 n14; dano recebido ×0,95 n16. Consome exaustão adicional e depende de alimento. Os dois +5% citados e o bônus do equipamento multiplicam quando aplicáveis, não somam como um único +10%.

**S1 — Ímpeto / Corte circular.** Inicial concede Força I por 40 ticks (+20 n2), sem dano circular e sem mana base. Completa acerta imediatamente mobs com linha de visão no raio `3 + 0,5n4 + 0,5max(grau−1;0)`, dano base `8 × (n5 ? 1,1 : 1)`, e Força I por 200 ticks (+20 n2). n6 acrescenta 20 ticks por mob atingido, máximo cinco, **após** calcular duração genérica. É uma área centrada no jogador; não quebra blocos. Força afeta ataques nativos e funciona em PvP, mas a área e o bônus de treino do handler Mob não atingem jogadores. Combina com S2 e S3 para curar por dano e sustentar contato; não existe limite geral de número de mobs na área além do raio/visibilidade.

**S2 — Sede de sangue / Vampirismo.** Passiva cura 5% do dano `Post` contra mobs, ou 8% n9, intervalo 60 ticks. Inicial ativa 100 ticks de cura 10%; completa 240 (+60 n13), cura 25%, ou 35% n13. Cura máxima por evento `1,5 + 2,5L/100`; durante ativação intervalo 10 ticks. Completa zera saturação, aplica Fome I e drena um ponto de comida a cada seis ticks em média: **3,333 pontos/s**, ou 3 n11. Uma barra de 20 esvazia em aproximadamente 6 s sem reposição; o efeito base dura 12 s. O acerto acrescenta exaustão 0,8 (0,6 n12), não restaura comida. Dano de área pode selecionar o primeiro evento que passa no intervalo; não cura uma vez por cada vítima sem limite. Não há esse roubo de vida contra jogadores.

**S3 — Guarda / Bastião.** Inicial: Resistência I e Brilho por 60 ticks. Completa: Resistência III, Brilho e Lentidão I por 160 (+20 n18). n19 adiciona movimento +0,03 ao invés de substituir Lentidão. Retaliação passiva n15: 1 de dano a `Enemy`, intervalo 40 ticks; completa usa `max(1; 0,25 × dano recebido Post)`, ×1,5 n20, e empurra n20. Contra-atacante jogador não entra nessa retaliação. Reflexão frontal de projétil durante Bastião verifica direção (dot < −0,25), inverte velocidade e transfere dono; n20 grava bônus ×1,5. O bônus gravado é lido no caminho Mob, não necessariamente no PvP. Escudos, parry e Resistência podem coexistir, sujeitos à lógica nativa de bloqueio. A janela de reflexão não representa imunidade universal a explosões, DoT ou ataques pelas costas. [S06, S11, S12]

### 4.2 Mago

**Identidade:** controle de grupos, canais curtos e modificação localizada do terreno. Vida base −2. Dano de ataques corpo a corpo de `Enemy` recebido ×1,2; essa fraqueza não é aplicada ao ataque corpo a corpo de outro jogador por essa condição. Frio recebido ×0,9; magia contra mobs já gelados ×1,1; n7 magia ×1,05; n14 resistência a empurrão +0,1. Equipamentos vanilla compatíveis segundo profissão; equipamento de classe oferece desconto de mana, até +20 MP e extensão do gelo.

**S1 — Fragmento / Bola de gelo.** Snowball sem gravidade, velocidade 1,7 bloco/tick, vida 120 ticks. Dano 3 inicial, 6 completo, área 0,6 ou `3 + 0,5n4 + n6`, com linha de visão. Inicial Lentidão II por 60/80 ticks n2, mais extensão de equipamento. Completa coloca uma casca de gelo compactado em espaços de ar disponíveis, paralisa 60/80 ticks n6 mais equipamento, e mantém frio por duração de perfil `160+40n5+40n6`, mais equipamento. Frio chama dano `freeze` de 1 a cada 40 ticks; paralisia cancela ataques cuja entidade direta é o ser gelado, para navegação e movimento horizontal. Só usa o frio completo quando `canFreeze()` permite. Gelo/teias têm ledger de restauração; não rendem drop por quebra comum. A paralisia cancela dano de entidade direta gelada; não se presume que remova toda flecha já disparada por ela. O gelo inicial dura 60 ticks no ledger; o completo usa a duração de perfil, separada da extensão de frio por equipamento. Bosses e fontes protegidas contra frio precisam teste, não há uma regra Oxy única de imunidade para todos os chefes. Não acerta jogadores pelo filtro de impacto.

**S2 — Faísca / Lança-chamas.** Inicial é projétil de fogo customizado, dano 3 e queimadura 1/2 s n9. Completa cria jato que atualiza direção da mira: alcance `6+n11+n13`, bloqueado por terreno, largura crescente `0,4 + distância × (n13 ? 0,18 : 0,1)`. Dano 2/2,2 n12 na abertura e a cada 10 ticks, duração de perfil 60 ticks, queima 6/10 s n13. O tipo de dano do jato é magia indireta, não o mesmo tipo fogo da brasa do Piromaníaco; resistência de data map a fogo não reduz automaticamente esse dano direto. Passiva de magia n8 queima 1/2 s com intervalo 60 ticks. Não lesa jogadores pela seleção Mob; fogo na vítima pode continuar após o canal. Trocar a mira muda o próximo pulso, não redireciona uma faísca que já saiu.

**S3 — Abalo / Tremor.** Âncora no local da ativação, não acompanha o jogador. Inicial raio `1,5+0,5n16`, dano 0,5 e Lentidão I, perfil 20 ticks. Completa raio `4+n18`, duração `120+40n19`, primeiro pulso 2 e seguintes 1 a cada 10 ticks, Lentidão II 50 ticks; n20 faz duas réplicas de raio ×0,7 e dano 1,5 após o tremor. Seleciona até 14/22 n18 blocos naturais elegíveis, embaralhados; progressão visual de rachadura aumenta cerca de três estágios por pulso até quebrar. Não aceita block entity. `destroyBlock` chama drops, mas não passa explicitamente pelo mesmo `CommonHooks.fireBlockBreak` usado em meteoros/crateras. Passiva n15 dá Lentidão breve em área em impactos mágicos. Canal de fogo em alvo congelado combina frio, queimadura, +10% de dano mágico e +5% n7, com as limitações de alvo. [S06, S17]

### 4.3 Assassino

**Identidade:** adagas, invisibilidade e execução móvel. No save permanece enum `ARQUEIRO`; o Arqueiro novo é outro ID. Só armaduras da tag `oxyrpg:light_armor` (couro e Elytra distribuídos); projéteis recebidos ×1,25. Dano melee +5% contra mobs; detecção dos mobs ×0,9 n7 e ×0,85 n9 em lugar do primeiro fator; movimento +3% n14. Fraqueza de projétil também vale em PvP. Não há requisito de arma curta para todos os bônus da execução.

**S1 — Adaga breve / Adaga espectral.** Snowball sem gravidade, velocidade 3,3, TTL 120 ticks, dano `3→8 × (n6 ? 1,25 : 1)`. Completa atravessa até 3/4 n4 criaturas; inicial destaque 40 ticks (+20 n2), completa 200 (+40 n5), com duração genérica. Completa Lentidão I 80/120 n6. Não usa o dano base/encantamentos de uma espada carregada para calcular esse argumento. Não quebra blocos. Não causa o dano da adaga em jogadores pelo impacto customizado; golpes nativos continuam possíveis.

**S2 — Sumir nas sombras / Furtividade.** Inicial 60 ticks, completa 200 (+40 n11), invisibilidade nativa e ocultação de equipamentos; fumaça é visual, raio 1/1,5 n12. Limpa alvos de `Enemy` próximos: completa para todos na consulta, inicial só além de quatro blocos. Restaura invisibilidade anterior se ainda tiver duração. n13 multiplica o primeiro dano enquanto furtivo por `1,25+0,5L/100`, **no caminho contra mobs**. Após dano positivo contra mob, encerra o estado. Contra jogador, o retorno antecipado do filtro impede esse encerramento: não deve ser tratado como “um único golpe invisível” em PvP sem teste/correção futura. Ataque que erra ou é totalmente bloqueado também não é o mesmo evento de dano positivo.

**S3 — Arrancada / Execução furtiva.** Inicial desloca 2 blocos; completa `5+0,5n16+n18`, passos de colisão de 0,25 para limitar travessia de parede, com direção do comando. Velocidade I/II por 60/160 ticks. Durante a completa, melee/flecha/adaga contra mobs multiplicam por `1,3+0,7L/100+0,2L/100 se n19`. No 100 e n19: ×2,2. n20 permite um segundo dash na janela, custo base 4, sem recarga principal adicional. Não se deve confundir esse dash com a esquiva universal limitada a três blocos.

Com n13, furtividade e execução no nível 100: ×`1,75 × 2,2 = 3,85` antes dos outros fatores. Incluindo treino, árvore genérica completa e 32 de dano: ×`3,85 × 1,05 × 1,36 × 1,64 = 9,016392` sobre o dano mitigado aplicável, sem críticos/equipamento. **É exemplo PvE**, não multiplicador de PvP: esses bônus específicos estão depois do filtro Mob. Compatibilidade com maça, ataque em queda e encantamentos merece medição de burst; não foi calculada uma morte instantânea sem cenário de defesa real. [S06, S11]

### 4.4 Arqueiro

**Identidade:** projéteis nativos, debuffs e mobilidade. Vida base −4, não pode usar o escudo vanilla; armas/armaduras restantes sujeitas a Arsenal. O check é `Items.SHIELD`, não uma proibição automática de todo escudo acrescentado por outro mod. Ataques com flecha +5%. n7 habilita invisibilidade breve após 40 ticks agachado, n9 reduz detecção; n14 +3% movimento. Portanto a furtividade agachada ainda existe no **novo** Arqueiro, não somente no Assassino. Flechas físicas acertam jogadores, mas alvo marcado e perseguição selecionam mobs.

**S1 — Rajada.** Precisa de flechas na tag de munição antes de gastar mana; criatividade/debug ilimitado dispensam consumo. Inicial 2 flechas, completa 6, +2 n4 e +2 n6, máximo 10. Consome munição antecipadamente; dispara a cada dois ticks, velocidade 2,6, TTL 160. `baseDamage` 1,6/2,2, ×1,15 n5; direção inicial da mira com pequena distribuição lateral. Acerto aplica Lentidão I 60/100 ticks n2; completa Fraqueza I 80; n6 Veneno I 60. Estes efeitos de acerto recebem `LivingEntity`, inclusive jogador. Flecha com efeito/espectral consumida vira **Arrow comum**, sem conservar aquele efeito original; o encantamento Infinidade não dispensa o consumo de munição da skill. Poder do arco não entra automaticamente na Arrow criada sem arma.

**S2 — Chuva de flechas.** Exige mob marcado/visível; marca validada e renovada, seleção comum até 24 blocos, marca existente até 32. Inicial 4 flechas, completa 10, +4 n11, máximo 14; saída vertical 1,7 e dispersão horizontal. `baseDamage` 1,8/2,6 ×1,15 n9 ×1,25 n12. Depois de 12 ticks seguem o mob, velocidade 2,3, TTL 160. n13 gera explosão visual/dano adicional 3/4 n12, raio 2,5, sem destruir terreno, **apenas mobs**. A flecha continua sendo física; pode colidir com jogador no trajeto, mas a chuva não marca jogador nem aplica a explosão a ele. A marca de guiada que dispensa erro RPG não é aplicada igual a todo projétil da chuva.

**S3 — Caçador veloz.** Inicial dura 80 ticks, completa 200, +20 n16 e +40 n18. Velocidade I/II; Salto II/III n20. Permite esquiva sem consumir cargas, inclusive no ar, mas mantém intervalo 8 ticks, restrições de água/lava/montaria e colisão. Completa, com alvo mob válido, converte flechas nativas elegíveis em perseguidoras, acelera inicialmente ×1,4 e depois segue a 3,4/4 n19 blocos/tick, sem gravidade; aplica Lentidão. Arco/besta nativos preservam seus efeitos nativos nesse caminho, diferentemente da rajada. “Sempre acerta” não é garantia frente a parede/escudo/colisão; o que há é orientação e supressão do erro RPG. A janela de perfil de chuva/rajada não define sozinho TTL real dos projéteis.

Combinar S1 com S3 direciona rajada a mobs; combinar S2 com n13 cria muitos impactos, mas não é correto somar todos os danos nominais como DPS: frames de invulnerabilidade e colisões das flechas precisam ser medidos. Em PvP o dano nativo e debuffs tornam a classe distinta das que têm ofensivas Mob-only. Flechas guiadas aplicam Lentidão por 80/120 ticks n18 e nível I/II n20 no handler de acerto. [S07, S13]

### 4.5 Necromante

**Identidade:** exército temporário e controle de criaturas. Dano direto do dono ×0,8, salvo dano reconhecido como invocação; de dia movimento −15%. Vida das invocações +10% n7; dano +5% n0; resistência a empurrão +0,1 n14. Equipamento vanilla sem proibição geral adicional. Servos não atacam jogadores pelo filtro próprio; jogadores também não conseguem causar dano a eles no handler de imunidade.

**S1 — Aracnídeo.** Projétil teia sem gravidade, velocidade 1,7, TTL 120, alvo mob. Lentidão IV 40/80 ticks, +40 n5. Coloca teia temporária no pé/acima quando elegível. Inicial 1 aranha (+1 n2); completa 3 (+1 n4, +1 n6), máximo 5. Reativar remove o grupo anterior do slot. **As aranhas não têm o limite de 80 ticks do perfil enquanto o alvo permanece vivo**; após perder o alvo ficam 80 ticks à espera de novo ataque. Removidas por distância >64 do dono, morte, dimensão ou limpeza. Marcação por dano do dono pode incluir pacíficos, mas aquisição autônoma procura hostis. Vida base de aranha 12 ×P(L) ×1,1 n7 ×`(1+0,1grau)`; ataque base 3 e modificadores de invocação. Não são aranhas naturais com drops normais.

**S2 — Rei dos Mortos.** Inicial 1 zumbi (+1 n9), 300 ticks; completa 3 zumbis (+1 n12,+1 n13) e 1 esqueleto (+1 n11,+1 n13), máximos 5 e 3, 600 ticks, com duração genérica. Vida base 16 ×P(L) ×1,1 n7 ×grau; ataque base 3, dano ajustado por P(L), n0 e equipamento. Fogo solar é neutralizado por resistência/helmet. Seguem dono, alvos de seus ataques e hostis próximos; não ganham drops/XP vanilla ao morrer. Só `Enemy` autorizado pode ferir invocações; fogo/quedas e jogadores não constituem um contrajogo normal contra esse grupo.

**S3 — Céu Noturno.** Inicial raio 3 e cegueira 40 (+20 n16), uma investida de phantom; completa raio `6+n18`, cegueira 80 (+40 n20), três phantoms. Cegueira tem um mapa Oxy que interrompe alvo/navegação, além do efeito nativo. Phantoms base 14 de vida, um mergulho por unidade, dano manual 4, ×1,15 n19 e fatores de invocação; grupo no máximo 240 ticks × duração genérica. Investidas espaçadas em 20+12 por índice e movimentadas a ~0,65/tick. Não é dano aéreo sustentado infinito.

Máximo simultâneo por necromante: **5 aranhas + 5 zumbis + 3 esqueletos + 3 phantoms = 16**, sem contar projéteis/teias. Ataques dos servos podem receber o fator específico de nível/gear e depois o fator global de dano do proprietário. Não têm crescimento individual de MobLevels como hostis comuns. Recebem crédito de abate/XP reduzido no caminho próprio. Invocações carregadas de disco sem sessão válida são rejeitadas. Matar chefes com servos imunes a dano do jogador e manter aranhas num alvo duradouro são cenários de PvE a validar, não prova de vitória contra todo boss. [S08]

### 4.6 Xamã

**Identidade:** suporte, animais, fertilização e poções aleatórias. Consumo de carnes reconhecidas é bloqueado; dano direto com item na mão ×0,9. Não há proibição geral de armadura. n14 +0,1 resistência a empurrão. A mão deve estar vazia para raios de punho; modo de poções exige ambas vazias. Aura de cura não distingue aliados de inimigos jogadores.

**S1 — Estalos / Deus Relâmpago.** Inicial 100 ticks, completa 300, +60 n4. Ao evento de ataque com mão principal vazia contra mob, no máximo a cada 30 ticks, gera raio visual sem incêndio/transformação vanilla. Dano `((3→6)+n2+2n5) ×1,05 ×(1+0,05grau)`. n6 outro mob próximo recebe metade; não só hostis. A chamada nasce no evento de ataque, antes de saber se o soco nativo errou: cancelar/errar o soco não implica necessariamente cancelar o raio separado. Não há raio ofensivo próprio contra jogadores neste filtro. Fonte elétrica nativa pode ser reconhecida pelo data map de resistência a raio.

**S2 — Doutor Animal.** Inicial 1 animal, completa 3 (+1 n13), no máximo 4, via infraestrutura de invocação; duração 100/200 +40 n9 +100 n13. Pulso inicial e a cada 20 ticks: raio `2→4 +n11`, cura `((0,5→1)+0,5n12) ×(1+0,05grau)`. Cura pacíficos, invocações e **qualquer jogador**, desde que na esfera e visível, incluindo adversário PvP. Remove todos os efeitos nocivos, inclusive sangramento, e limpa frio/cegueira Oxy. Fertiliza até quatro plantas por pulso, até 32 tentativas, com uma pilha virtual de farinha de osso; não consome farinha do inventário. Verifica chunk/`mayInteract`, sem integração própria de claims. É vantagem agrícola intencional, mas a produção por MP precisa ser comparada a uma fazenda real.

**S3 — Especialista.** Abre janela 160/300 +100 n18. Clique negativo escolhe aleatoriamente dano instantâneo/veneno/lentidão/fraqueza; positivo cura instantânea/regeneração/velocidade/força/resistência a fogo/resistência. Todas nível I. Não-instantâneos 80 ticks inicial, 200+100 n19 completa. Lança poção nativa velocidade 1,05; pode afetar jogadores e aliados pelo splash vanilla, com inversões de cura/dano em mortos-vivos. Cada lançamento custa 4/5 MP −1 n20, intervalo 20/15 n16 ticks; além do custo de abrir o modo. Só o desconto de equipamento é aplicado ao lançamento, não todos os ajustes de custo da habilidade principal. Benefícios também podem acertar inimigos; efeito exato é aleatório, portanto não há escolha garantida de dano instantâneo a cada clique. [S08, S10]

### 4.7 Aviário

**Identidade:** exploração vertical, planagem, transporte e impacto físico. Vida base −4; projéteis recebidos enquanto no ar ×1,2. Queda recebida ×0,7 desde a classe, sem empilhar uma segunda redução idêntica pelos textos dos nós 0/7/8. Armaduras e armas vanilla são permitidas segundo profissão; voo substitui necessidade de Elytra durante a janela, não a transforma em item do inventário. Equipamento de classe amplia dano. Não há voo criativo permanente em survival por esta skill.

**S1 — Salto alado / Decolagem.** Primeiro uso lança verticalmente com velocidade Y=1,25, sem iniciar imediatamente planagem. Segundo uso no ar ativa a planagem e boost. Duração de perfil `160→300 +40n2+80n4+floor(240L/100)n6`. A reativação não renova essa duração total. Boost custa base 8 MP, intervalo mínimo 10 ticks; janela 20/40 ticks, +20 n5, limitada ao tempo de voo restante. Impulso:

```text
Vmax = 1,05 + 2,15L/100 + (n6 ? 0,6L/100 : 0)
thrustNovo = min(Vmax; max(0,75+0,5L/100;
                         max(velocidadeConhecida;thrustAnterior)+0,15+0,45L/100))
```

Unidade é blocos/tick, não blocos/s. Sem n6: teto 1,0715 no nível 1 e 3,2 no 100; com n6 no 100, 3,8. O cliente limita sua simulação e recebe estado do servidor. O servidor limpa em morte, classe incorreta, dimensão, água/lava, montaria ou voo criativo coexistente; pousar depois da decolagem encerra a janela. Há controle de aterrissagem antes do choque, sondando profundidade 6..16 conforme velocidade, limitando horizontal a 0,2 e queda a −0,28, zerando distância de queda. A previsão do cliente e a execução do servidor precisam teste com latência/Barrel Roll: não há medição de suavidade nesta auditoria.

**S2 — Peso pena.** Passiva, não é um cast válido no slot. Com n8, agachar durante queda/planagem sem montaria, água/lava ou levitação interrompe planagem e limita Y a −0,018; n11 reduz para −0,008. Zera distância de queda enquanto esse freio funciona; a redução basal de 30% ainda vale nas quedas normais. Redução de dano com fonte `LivingEntity` durante a descida: 10% n9 na inicial, 25% completa, +10 pontos percentuais n12 e n13; máximo 45% nesta fórmula. Não é redução de todo dano ambiental. Meteor preparado impede esse freio. Nós de duração não têm o mesmo valor em uma skill passiva sem janela de ativação.

**S3 — Impacto Meteoro.** Requer no ar, sem água/lava/montaria/voo criativo. Se já estiver planando, segue vetor fixado da mira por até 100 ticks e explode ao encontrar mob/obstáculo; se caindo, prepara mergulho por até 600 ticks. Acumula altura máxima e maior velocidade observada; deslocamento >64 em um tick cancela. Fórmulas:

```text
F = min(12; 0,38√max(0;alturaMáxima−alturaAtual) + 0,35velocidadeMáxima²)
Raio = min(14; (inicial ? 2 : 3) + 0,8F + n18 + n20)
Dbase = (inicial ? 4 : 8) ×(1+F) ×(n16?1,15:1)
        ×(n19?1,2:1) ×(n14?1,05:1)
```

Velocidade acumulada é limitada a 8 antes do cálculo de força. Na completa, F=12 e todos os upgrades dão argumento **150,696 HP**, antes de fatores gerais/mitigação; não é dano final medido. Área esférica contra mobs não exige a mesma linha de visão de outras áreas: pode alcançar criaturas através de estruturas no raio. Inicial não quebra blocos; completa enfileira cratera com força/altura e detritos, testa permissão e evento de quebra. Queda/aterrissagem preparada tem cancelamento específico. Contra jogadores não aplica dano da explosão customizada. Destruição por impacto pode persistir após logout porque é outra fila.

**Transporte adicional:** `AviaryCarry` permite um mob agarrado ao voar, marca até 24 blocos por 20 ticks renováveis, pega a até 3,5 com visão/colisão livre. Exclui Wither/Dragão, **não exclui Warden, pets ou aldeões pelo tipo**. Não há custo extra de mana ou massa. Soltar zera distância de queda acumulada até então, mas permite nova queda dali em diante. Pouso/dimensão/logout soltam; carregar jogador não é permitido. Isso facilita logística intencional e também exige regras para aldeões/pets/bosses no servidor. [S14, S17]

### 4.8 Ilusionista

**Identidade:** disfarce, cópias e fuga espectral. Vida base −4, dano cuja entidade direta é o jogador ×0,8, inclusive ataques PvP. Não tem restrição geral de equipamento vanilla além das profissões. Inicial S1 gratuita como nó, mas custa mana desde o nível 1. Disfarce não equivale a trocar o player por uma entidade com a IA do mob: modelo é apresentação; o jogador continua servidor `ServerPlayer`.

**S1 — Mímico.** Alvo mob visível até 24; proíbe Wither/Dragão/Warden e tamanho >3 largura/>4 altura. Inicial 2 cópias e aparência por 120 ticks; completa 240; +60 n5 após fórmula genérica; +1 cópia n4 e n6, máximo 4. Spawn em posições próximas livres/carregadas; cópias recebem dados sincronizados, equipamento, vida máxima e vida atual do alvo (`MimicAppearance.copy`). n2 não adiciona absorção/vida; n6 não amplifica dano da cópia. Cópias têm AI comum, corrigem alvo dono/temporários e voltam navegando se além de 10 blocos. Não clonam UUID, dono ou posição. Dano de **qualquer jogador** a temporários é cancelado. Ataque das cópias usa sua IA e não o sistema completo de escala de servos do Necromante.

Enquanto disfarçado, o hook cancela aquisição de alvo do jogador por **qualquer Mob**, e a rotina limpa alvos próximos. Atacar não encerra Mímico nesse caminho. Isso permite atacar mobs sob uma proteção de targeting que requer avaliação de PvE; não assegura que todo cérebro de boss/mod use esse hook. Cópias hostis podem selecionar jogadores que não sejam o dono pela IA comum: esse é um risco indireto de PvP distinto dos servos, pois não há filtro próprio equivalente que impeça todo ataque ao outro jogador. Drops e XP vanilla das cópias são cancelados; crédito em missões/outros mods necessita teste.

**S2 — Suspensão / Leviosá.** Mob alvo: Levitação I por `60→100` ticks de perfil +40 n12 depois da fórmula; Fraqueza I/II pela duração de perfil sem esse incremento. Dano por segundo `2→3 n9`, ×P(L), apenas durante janela `until[1]`. Portanto n12 alonga levitação, mas não alonga igualmente todos os componentes. Na completa, mobs ao redor raio 3/5 n11 recebem Lentidão II e Fraqueza I por 30 ticks renovados; não há veneno neste caminho. n13 não cria vulnerabilidade adicional. Não levita jogador através desta seleção. Levitação/proteções nativas de chefes continuam relevantes; não há resistência compartilhada de CC.

**S3 — Enxame / Corpo de Eclipse.** Inicial 60 ticks; completa 120; +40 n18, duração genérica. Fecha menu, guarda posição segura, cria seis morcegos cosméticos sem AI e torna player intangível/oculto. Impede atacar, usar itens, interagir com blocos/entidades, quebrar ou lançar outras habilidades. Cancela dano recebido salvo fontes `bypasses_invulnerability`; também pode negar sangramento, cuja tag não tem esse bypass. Voo baixo: Espaço/Shift vertical ±0,22; limite de altura 5/8 n20 acima de terreno sólido, limite horizontal 0,4/0,55; velocidade adicional +0,15/+0,3 n16.

Colisão por tiers consulta tags `phase_light/barriers/glass`, **mas também há um OR** para qualquer bloco de dureza não negativa, sem block entity e que não seja portal/estrutura de portal explicitamente excluída. Pedra, paredes e vários blocos que não estão nas tags já são atravessáveis no nível inicial. Portanto n19 não é um desbloqueio estrito de portas/cercas. Retorno tenta posição segura se dentro de colisão; modificações de modelo/no-gravity são limpas ao expirar/logout. O custo e incapacidade de atacar dão contrapeso, mas atravessar bases/entrar em estruturas fechadas não tem política de território explícita. [S09, S17]

### 4.9 Hidromante

**Identidade:** sobrevivência aquática, pressão linear e controle da superfície. Respiração aquática renovada em todos os níveis; dentro da água, Poder do Conduto renovado; completa S1 também Graça dos Golfinhos. Dano em chuva local ×1,2, tempestade com n6 ×1,4, submerso sem chuva ×1, fora de ambos ×0,9. Fogo recebido fora da água ×1,15. Não há proibição geral de armas/armadura. A vantagem de mineração submersa vem de efeitos nativos, sem inventar uma segunda ferramenta.

**S1 — Impulso aquático / Águaman.** Ativo exige água, custo 8, recarga 5 s. Vetor da mira ×`(0,6→1 +0,25n4+0,003L)`; acrescenta Y=0,1, ou 0,45/0,8 n5 se mirando suficientemente para cima. Perfil 60 ticks, mas o impulso é imediato. n2 dá eficiência de movimentação aquática +0,2/0,5; n6 +0,2. Graça dos Golfinhos, Conduto e enchants aquáticos podem combinar pelo sistema vanilla; não há teto único Oxy para a velocidade resultante de todas as bibliotecas/mods. Passivas não exigem repetir o cast a cada três segundos para respirar.

**S2 — Jato / Canhão d’água.** Canal de 40/80 ticks, a cada cinco ticks; alcance `16→22 +6n11`, direção viva da mira e segmento bloqueado por terreno. Testa interseção de bounding box de mob inflada 0,65. Dano `1,5→2,5 ×P(L)` por chamada; empurrão horizontal 0,3/0,55 n12, Y +0,06. Não consulta n9 para dano nem n13 para aumentar duração. Ao ativar submerso, cria 2/3 n13 manifestações entre peixes/lula/golfinho, temporárias, removidas quando termina o jato; sem drops/XP vanilla. Não afeta jogador por seleção de dano. A aparência de raio/anel não implica ignorar parede nem usar os atributos de dano do Warden.

**S3 — Passo / Sobre-água.** Caminhar na superfície por 120/300 ticks, +100 n19 **depois** do cálculo genérico; agachar permite mergulhar. Ao ativar em contato com água, cura `2→4 +2n16+2n20`; completa remove água em bolha raio 1/2 n18, três camadas de Y, por 100 ticks. Água retorna por ledger quando o local continua livre; não remove lava nem água contida em blocos waterlogged nesse caminho. n20 limpa Veneno e Fraqueza, não todas as condições, nem sangramento. Pode combinar respiração/impulso/caminhada para exploração e cura, sem custo permanente de oxigênio; clima afeta dano inclusive em PvP quando a fonte tem dono jogador. [S09, S17]

### 4.10 Piromaníaco

**Identidade:** calor, encadeamentos, núcleos e forma Fênix. Vida base −2. Dano quando dentro da água ×0,8; frio recebido ×1,2. Fora da água, fator de calor `1+0,002calor`, até ×1,2 antes de superaquecer. Calor vai de 0 a 100: ativações/lançamentos +8, detonação de magma +5, dano positivo contra mob +2; há ganho periódico por alvos queimando e resfriamento. Chuva/água retira 8/s; fora delas, após espera o calor cai 2/s. Ao atingir 100, zera e bloqueia casts por 120 ticks, salvo debug. Passiva de queimar golpes n9 aplica fogo por 3 s. Não é imunidade permanente ao fogo fora da Fênix.

**S1 — Pavio / Carnaval de Pólvora.** Marca mobs próximos raio 5/7 n4, até 5/8 n4, por 200 ticks; n2 não alonga esse valor. Na completa, alvo já queimando recebe peso 2, demais peso 1. Reativar inicia sequência, custo extra base 4: uma detonação a cada seis ticks, dano `4 ×peso ×P(L)`, raio 2/3, linha de visão, fogo 3 s e impulso Y 0,3/0,65 n5. n6 propaga até oito adições na sequência. O contador limita propagação, mas não representa um raio infinitamente crescente nem garante que cada alvo apareça uma única vez em toda a sequência. Não é explosão vanilla que automaticamente quebra todos os blocos. Contra jogadores não há seleção da área.

**S2 — Gota / Coração de Caldeira.** Snowball magma sem gravidade, velocidade 1,2, TTL 100. Intervalo interno 10 ticks; primeira mana 10/16 e recarga 6 s. Com núcleos presentes, novo lançamento é extra de base 10 sem esperar essa recarga; reativar agachado detona. Impacto: área raio 2/3, dano `4→6 ×P(L)`, cratera/scars e núcleo preso a mob ou ao chão por 240 ticks; alvo direto também recebe 2×P(L) e fogo 3/5 s n9. Guarda 3/6 n11 núcleos, removendo o mais antigo se cheio.

Detonação de núcleos: dano `6/8 n13 ×P(L)`, raio 2,5 e nova alteração de terreno; completa liga pares separados por até 10 blocos, linhas por 60/100 n12 ticks, dano `1/2 n13 ×P(L)` a cada 10 ticks e fogo 2 s. Seis pontos podem produzir 15 pares numa detonação. Lista de linhas não tem um teto global por jogador independente de sobreposição de detonações. A janela do perfil não é a duração exata de cada núcleo/linha. Crateras de impacto quebram até 32 blocos elegíveis, dureza até 3; scars substituem até 32 blocos naturais de superfície por 60% magma/40% netherrack **permanentes**, não restauração temporária. Avaliar obtenção de materiais e proteção; não chamar isso de duplicação comprovada.

**S3 — Fênix de Cinzas.** Exige 30 calor, consome esse calor além de mana. Inicial 80 ticks; completa 180 +60 n18. Salto inicial +0,7 Y; saltos normais não agachados recebem impulso extra e explosão raio 2, dano 2/4 n16 ×P(L); permite um salto extra no ar antes de aterrissar. Há proteção de queda relacionada ao lançamento/aterrissagem. Renova Resistência a Fogo, adapta movimento na lava em 0,12/0,2 de contribuição de intenção, incendeia mobs próximos raio 2,5 a cada cinco ticks. Grava no máximo 24 posições de brasas, a cada cinco ticks; a cada dez, cada posição pode ferir mobs raio 0,8 com 1×P(L) e fogo 2 s. n19 em água aplica Fraqueza I raio 4 e bolha de água temporária; explosão final raio 3, dano `4+min(6;núcleos)` com n20, detona núcleos com n20. A fórmula de dano global/calor continua aplicável, mas parte do calor foi consumida ao entrar.

Combinar marcas queimadas, vários núcleos, linhas e Fênix concentra fontes, partículas, fogo e alterações de blocos. É necessário medir pulsos aceitos, calor e tempo de exposição: somar cada chamada ignora invulnerabilidade de mobs e resfriamento. Escudos/fogo nativo não oferecem uma mesma defesa frente a todos os tipos customizados; Ember tem tag de fogo, o dano mágico do Mago é outro caminho. [S09, S17]

### 4.11 Vampiresco

**Identidade:** sustento por sangue, dano contínuo, fuga aérea e caça de rastros. Alimentação normal é bloqueada; a barra de comida continua sendo a reserva usada por `FoodData`, apresentada como sede. Dia em dimensão com céu: Fraqueza I; exposto ao céu e sem chuva, dano `on_fire` 1/s ou 0,5 n6. Chuva local causa `drown` 0,5/s. Noite ou dimensão sem céu recebe Visão Noturna renovada. Armaduras/enchants podem alterar fontes nativas; a classe não tem uma proibição geral de armadura. Guarda/golem podem perseguir vampiro à noite; aldeões comuns acordam/fogem.

**Alimentação passiva:** clique direito de mão principal em mob não-`Enemy`, até 3 blocos e visão, fome <20, sem Shift, intervalo 60 ticks. Exclui invocações/temporários/proxy; pode incluir aldeão, golem e pet. Tenta dano de mordida 2, somente se perder vida concede 4 comida (+2 n2), saturação 0,4; 35% de aplicar sangramento rank 0 a sobrevivente. Instrução textual aparece só nas primeiras refeições; isso não altera as regras. Ataque melee positivo contra hostil aplica sangramento rank 0 sempre. Não há esta mordida alimentar em outro player. Cerca de um terço dos animais próximos podem fugir quando o vampiro está faminto.

**S1 — Sede de Sangue.** Exige vida faltante. Janela 120/200 ticks. Primeiro acerto melee positivo em mob: cura imediata `min(4; 0,15 × danoPost +0,5+0,01L)`; reserva de regeneração `min(vidaMáxima × (n5 ? 0,25 : 0,15); 4+0,04L)`. Cura 1/1,5 n5 por segundo enquanto a janela de cura durar; aplica sangramento rank 0/1 conforme inicial/completa, infinito até tratamento. Reacerto no alvo vinculado: dano ×1,2/1,4 n4 e raiz 40/60 ticks n6, além de Lentidão X; enquanto enraizado, navegação/ataques são interrompidos. Não há um intervalo de resistência a raízes por vítima: pode renovar com ataques ao mesmo mob. Trocar para novo alvo reinicia reserva/cura inicial dentro da janela; medir cura real por vários alvos, não assumir que só há uma concessão por cast. Todo vínculo ofensivo desse ramo seleciona mobs, não jogadores.

**S2 — Metamorfose.** Entrada inicial/completa 14/24 MP, recarga 35 s; player ganha dimensões/modelo de morcego, `mayfly/flying` e consome 1 MP/s. Duração de perfil 160 é usada em apresentação; **não é teto de oito segundos do voo**. Voa até retornar, mana acabar ou limpeza. Não pode atacar, usar itens, abrir blocos ou outras skills salvo retorno. Não é intangibilidade/imunidade do Ilusionista; pode sofrer dano. Retorno extra 0 MP fecha forma e convoca 2/4 morcegos +2 n11, máximo 6, por 160+80 n13 ticks, sem multiplicador genérico nessa duração. Vida por morcego 3/5 n12; bloqueia uma unidade por evento de dano, com excesso chegando ao jogador, sem absorver sangramento/fontes sem causador ou `bypasses_invulnerability`. Morcegos sem colisão com o jogador e protegidos de dano do próprio dono. Forma anterior de voo é restaurada; se crescer dentro de parede tenta posição segura, e dá Queda Lenta por 40 ticks.

**S3 — Twilight.** Janela 120/240 ticks, Força I/II, Velocidade I/II n18 e Pressa I/II n19. Atravessa folhas. Cancela fontes na tag `oxyrpg:spells`: magia, magia indireta, sopro de dragão, ilusão, brasa e pressão. Não equivale a “todo dano mágico de todo mod”: raio nativo, frio e sangramento não estão automaticamente nessa tag. Dano não cancelado recebe ×1,25, inclusive dano ambiental/sangramento. Bloqueia novos efeitos nativos recebidos, inclusive benéficos, exceto sangramento e buffs próprios; mixin também cancela cura/dano instantâneo de poções. Não é apenas imunidade a debuffs.

Rastro lógico de sangue dura 1.200 ticks, máximo 8.192 posições por mundo. Twilight ganha movimento +0,15/+0,25 n20 ao seguir posições próximas (distância² <12); rastro não filtra dono, pode ser do próprio vampiro. Renderização não deve ser tomada como prova de que esse bônus foi aplicado. Combina cura/raiz em PvE e Força/Velocidade/escorta em PvP; roubo/raiz do ramo S1 não está implementado contra player, mas armas com sangramento de data map podem feri-lo. [S10, S16]

### 4.12 Deus do Trovão

**Identidade:** teleporte, canal e punhos elétricos. Não tem proibição geral de armadura; S3 pede mãos principal/secundária vazias para o bônus ofensivo. Não consegue nadar normalmente: movimento aquático é limitado para baixo e horizontal amortecido. Ambientes ultrawarm ou temperatura ≥1,5 acumulam calor uma vez/s até 12; na água esfria; a partir de 10 toma `dry_out` 1/s, salvo criativo. Correr no chão fora da água por 60 ticks concede +15% movimento até interromper. Isso não é resistência automática a fogo/raios.

**S1 — Salto do trovão.** Marca ponto de terreno pela mira, alcance 5/12 +4 n4 +4 n6, máximo 20. Recuo de 0,7 da direção e tentativas Y 0..2; exige chunk carregado e colisão livre, não valida um piso seguro/sem lava nem consulta região PvP/claim. Teleporta, produz raio visual e dano em área raio 1,5/3 +n5, base 3/6 +2 n5, com visão e filtro Mob; n2 Lentidão I 40. Perfil 20 ticks não é um buff longo. Pode transportar o jogador em PvP, mas não aplica a área a oponentes jogadores. Raio visual não converte aldeão/porco nem incendeia terreno como raio natural.

**S2 — Corrente de raios.** Inicial 40 ticks; completa 80 +40 n12. Visual a cada cinco ticks, dano a cada dez; alcance 18/24 n11, alinhado à mira. Alvo primário mob recebe 1,5 inicial, ou 3+1 n9 completa; completa encadeia dois saltos, +2 n13 (máximo quatro), dano secundário 1,5, ×1,2 n5. Corrente seleciona mobs próximos visíveis, não jogadores. A fonte real é **magia indireta**, portanto resistência `lightning_resistance` do data map não a reduz, apesar da aparência elétrica. Twilight do Vampiresco a cancela se fosse recebida por um caminho válido de dano; a seleção atual já exclui o player.

**S3 — Corpo Tempestuoso.** Janela 60/160 +20 n16 +40 n18. Movimento +0,2/+0,4, +0,15 n19; altura de passo +0,6, +0,5 n20. Com ambas as mãos vazias, `player_attack` acrescenta **+2 no `Pre`, depois de mitigação nativa**, antes dos fatores posteriores. Dano positivo aplica Lentidão I 40 ticks no alvo `LivingEntity`, inclusive jogador. Encadeia um/ três mobs adicionais, base 2, ×1,2 n5. Esse +2/Slow funciona em PvP mesmo quando a corrente secundária não. Não é equivalente a um bônus de +2 no atributo da arma antes da armadura. Rapidez/auto-subida facilitam exploração, mas calor e água limitam biomas/dimensões.

Empilhar sprint +S3 +acessório +Velocidade nativa tem operações multiplicativas diferentes; teto de velocidade resultante não é uma soma simples. Canal e teleporte usam a infraestrutura de mira, enquanto fontes customizadas/raios de outras classes podem seguir tipos diferentes: revisar afinidade elétrica como decisão de conteúdo. [S07]

### 4.13 Itens únicos, fontes de condição e efeitos comuns às classes

**Relíquias vinculadas:** ação de botão direito, 10 MP, recarga `(40−5grau)` s =35/30/25 s, mais bloqueio de recuperação de morte. Guerreiro: Resistência I 3 s e empurrão próximo; Mago/Assassino/Arqueiro/Necromante/Vampiresco armam o próximo dano elegível por 10 s: Lentidão II, Veneno/Fraqueza, Brilho, ou cura 2 conforme classe. O consumo do efeito usa crédito de dano e alvo mob, não uma condição geral de “atingiu com esta arma”; servos podem participar do crédito. Xamã limpa Veneno/Fraqueza e cura 2; Aviário sobe 0,45 e Queda Lenta; Ilusionista desacelera e retira alvo de mobs; Hidromante cura e dá benefício aquático; Piromaníaco incendeia próximos; Trovão limpa Lentidão e dá Velocidade. Esses efeitos não convertem todos os golpes comuns em skills PvP.

**Frostblade/Emberblade:** ataques nativos de espada de ferro/diamante e ação própria de linha 7/10 blocos, custo 18/24 MP, recarga 8/11 s, dano `5/7 +0,08L`, Lentidão II/empurrão ou fogo. A ação usa `getLookAngle()` em vez de todas as dicas de câmera de `AbilityAim`: um possível desvio de mira em terceira pessoa é um risco testável. Filtros continuam Mob/visão. Enchants de golpe nativo não são automaticamente incorporados ao dano manual desta linha.

**Sangramento:** infinito, rank 0..2, `0,5 ×(rank+1)` HP/s. Tipo `oxyrpg:bleeding` entra nas tags de bypass de armadura, shield e cooldown de dano; pula vários multiplicadores Oxy, mas não todos os handlers `Incoming`, por exemplo Twilight ×1,25. Golpe melee com espada/axe de data map pode aplicá-lo: espadas ferro/diamante/netherite 8/12/18%, machados 12/16/20%; netherite rank 1. Mão vazia não tem chance de arma no mapa distribuído. Vampiresco tem sua regra própria contra mobs. Sangramento em player pode ocorrer pelas armas; player criativo/espectador é excluído.

Bandagem cura 4, remove sangramento, recarga 8 s; também pode ser aplicada em outro ser vivo. Leite não remove sangramento pelo hook específico, mas aura do Xamã remove. Antídoto trata Veneno/Fraqueza. Alguns aldeões/illagers/piglins usam autocura sem inventário real de bandagem: um terço por UUID, após seis segundos ferido, dois segundos de canal sem inimigo próximo/dano, duas utilizações por entidade. Isso não constitui resistência universal de mobs ao sangramento.

**Erro/crítico, números e partículas:** são consequência de eventos aceitos, não previsão de cliente. O handler de feedback retorna ao encontrar vítima jogador e exibe só crítico/erro, sem números ordinários de HP. Partículas de sangue podem ocorrer em dano real; sangue visual não implica necessariamente que o efeito de sangramento infinito foi aplicado. Ver riscos de desempenho e parry na seção 7. [S12, S13, S16]

## 5. PvP e matriz entre as 12 classes

### 5.1 Cobertura ofensiva efetiva

| Família | Contra jogadores | Limitação importante |
|---|---|---|
| Golpes/armas/projéteis vanilla | sim, sujeitos à política vanilla PvP | classe, profissão, encantamentos, escudo e eventos continuam relevantes |
| Corte, gelo, jato mágico, tremor, adaga customizada | não pelo filtro Mob | buffs do jogador podem valer, o dano/CC selecionado não |
| Rajada de flechas | flecha física e debuffs de acerto, sim | consumo e construção não conservam toda a arma/munição nativa |
| Chuva e perseguição | não escolhem player; flecha pode acertar no trajeto | explosão customizada só mobs; marca é Mob-only |
| Raios de Xamã / corrente e teleporte do Trovão | áreas/correntes não selecionam player | movimento/Força/punho próprio são casos diferentes |
| +2 e Lentidão dos punhos de Trovão | sim | +2 entra depois de armadura; secundários só mobs |
| Poções nativas do Xamã | sim | aleatórias, splash também pode beneficiar adversários |
| Aura/limpeza do Xamã | cura/limpa jogadores, inclusive adversários | não consulta time/aliança |
| Servos do Necromante / animais do Xamã | não selecionam player | player não consegue causar dano a esses servos |
| Cópias hostis do Ilusionista | possível por IA nativa | não se presume bloqueio de PvP por região/dono; testar |
| Impacto Meteoro / pressão / brasa | não selecionam player | terreno/fogo/obstáculos podem mudar o cenário de combate |
| Sede/raízes do Vampiresco | não vincula player | sangramento de armas comuns é outro caminho |
| Eclipse / Twilight / morcegos defensivos / esquiva | podem defender player | cada um distingue fonte e exceção, não são a mesma imunidade |

`ServerPlayer.hurtServer()` continua verificando PvP global e `canHarmPlayer` quando a fonte é outro jogador. Isso foi conferido na fonte nativa local. **Não se conclui que o mod ignora `pvp=false` em todo dano.** Não foi encontrada uma política Oxy para região, facção, zona segura, evento de duelo ou friendly fire própria. Dano indireto de entidade com dono não-jogador, por exemplo uma cópia hostil, precisa de teste distinto; validação vanilla de autoria não é uma garantia de proteção regional.

### 5.2 Matriz de rotas implementadas, sem previsão de vencedor

Esta matriz é **direcional**: linha atacante, coluna defensor. Registra o pacote ofensivo que realmente pode entrar em PvP e exceções defensivas especiais relevantes. Não é ranking, win rate, DPS ou alegação de superioridade. Todos os pares precisam dos cenários de equipamento/nível da seção 10.

**Rotas:** `V` = ataques vanilla autorizados, com fatores gerais; `B` = V + flechas físicas/debuffs das skills do Arqueiro; `P` = V + splash nativo de poções do Xamã; `F` = V +2/Lentidão do punho ativo do Trovão. Nas outras classes a ofensiva customizada principal continua Mob-only. **Defesas condicionais:** `g` = Bastião/reflexão frontal, sem espinhos PvP; `s` = fraqueza do Assassino a projétil; `a` = fraqueza aérea do Aviário a projétil/freio; `e` = Eclipse cancela dano salvo bypass, impede contra-ataque; `t` = Twilight/escortas do Vampiresco, com bloqueio de poções e +25% no restante. A ausência de sufixo não retira escudo, armadura, parry, esquiva, poções ou fraquezas ambientais do defensor.

| Atacante ↓ / defensor → | Guerreiro | Mago | Assassino | Arqueiro | Necromante | Xamã | Aviário | Ilusionista | Hidromante | Piromaníaco | Vampiresco | Trovão |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Guerreiro | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Mago | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Assassino | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Arqueiro | B·g | B | B·s | B | B | B | B·a | B·e | B | B | B·t | B |
| Necromante | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Xamã | P·g | P | P·s | P | P | P | P·a | P·e | P | P | P·t | P |
| Aviário | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Ilusionista | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Hidromante | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Piromaníaco | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Vampiresco | V·g | V | V·s | V | V | V | V·a | V·e | V | V | V·t | V |
| Deus do Trovão | F·g | F | F·s | F | F | F | F·a | F·e | F | F | F·t | F |

Leitura: `B·s` é flecha/debuff implementado contra um Assassino que recebe ×1,25 de projétil; `P·t` exige testar bloqueio de splash e instantâneos pelo Twilight; `F·e` verifica imunidade do Eclipse frente ao punho e seus eventos. Sufixos de projétil só se aplicam quando a fonte for de projétil: o Guerreiro não ganha um arco válido por estar numa célula `V·s`.

**Interações adicionais que a matriz compacta não representa como vitória:**

- Guerreiro × Arqueiro: reflexão pode transferir dono da Arrow e alterar classificação/bônus; verificar velocidade/dano após retorno. Espinhos não retaliam jogadores.
- Assassino × qualquer classe: atacar jogador não encerra furtividade e não concede os bônus de execução/primeiro golpe Mob-only. Há ocultação, mas não o pacote de burst anunciado contra mobs.
- Xamã × Vampiresco: aura pode limpar sangramento/curar adversário; Twilight recusa várias curas/status e instantâneos. Testar aura direta `heal()` separada da poção, pois não são o mesmo hook.
- Ilusionista × qualquer classe: cópias hostis são uma rota indireta não representada por `V`; validar aggro, dano, `pvp=false` e proteção regional. O Eclipse impede que o próprio jogador ataque.
- Aviário × Arqueiro/Assassino: mobilidade vertical e fraquezas de projétil; sem dano PvP de meteoro, mesmo quando a cratera muda abrigo/terreno.
- Trovão × Guerreiro: +2 pós-mitigação não sofre novamente toda a fórmula de armadura/Resistência; verificar escudo, parry e absorção na ordem real.
- Necromante × Xamã: suporte pode curar servos; não transforma servos em atacantes de jogadores. Cura/AI não justificam presumir “exército PvP”.

### 5.3 Controles, burst e contrajogo

Lentidão por flecha/punho, poções, parry e invisibilidade têm aplicação PvP; paralisação do gelo, raízes do Vampiresco, cegueira de phantoms e levitação não têm seleção normal de jogador. Não há DR progressivo compartilhado de controle. Não há combat tag, penalidade de logout em combate, imunidade posterior a uma raiz ou medidor de tenacidade próprio.

Parry exige item `ShieldItem` ou tag de ferramentas-escudo. Janela padrão após levantar: ticks 5..10, seis ticks; intervalo de novo registro 20 ticks. Cancela dano/desgaste bloqueados, aplica Lentidão IV/Fraqueza II ao atacante por 20 ticks e abre contra-ataque por 30 ticks (1,5 s), crítico garantido e sem erro. **`CombatFeedback.damage` retorna para vítima jogador antes de `ShieldParry.consume()`**. Assim, o estado de contra-ataque não é consumido nesse caminho: ataques PvP durante a janela podem conservar crítico garantido. Consumo contra mob funciona no caminho posterior. Isso é uma diferença comprovada, não um resultado medido de múltiplos acertos.

Esquiva padrão: uma carga inicialmente, duas no nível 25, três no 50, reposição de **uma carga a cada 6 s** por energia contínua. Cargas não regeneram três unidades em paralelo a cada seis segundos. Distância máxima horizontal 3, duração de movimento 13 ticks, impulso ~0,95, intervalo 8 ticks, i-frames 6 ticks. Não protege sangramento, fontes ambientais comuns ou bypass de invulnerabilidade. S3 do Arqueiro permite repetir sem consumir energia, ainda com intervalo; janelas de seis ticks separadas por mínimo oito deixam dois ticks teóricos de exposição, antes de latência e timing de hits.

Não foi provada eliminação inevitável antes da reação de um player. Há fatores que justificam medir: flechas aceleradas, +2 depois de mitigação, sangramento infinito, invisibilidade sem encerramento PvP, parry não consumido e ofensivas muito desiguais. O multiplicador ×9,016392 do exemplo Assassino é **PvE**; usá-lo como evidência de one-shot PvP seria incorreto. Armadura, escudo, distância, estado da vítima, cooldown nativo e TPS precisam ser registrados.

## 6. PvE, chefes, progressão de dificuldade e recompensas

### 6.1 Escala dos mobs

`MobLevels` atribui nível uma vez, persiste `oxy_level`, usando o jogador vivo/não espectador mais próximo até 128 blocos: `clamp(nívelJogador + sorteio[-2;2];1;100)`. Não usa média da party nem dificuldade regional. Criativo/debug não são excluídos dessa referência. Ferro mínimo 15, neve mínimo 8; `Enemy` e esses golems são elegíveis. Guarda NPC é `Villager`, não entra automaticamente nessa curva. Ao subir o nível do jogador, os mobs existentes não sobem automaticamente juntos; precisam de nova atribuição/mecanismo que não está nesse fluxo.

```text
n = nívelMob − 1
H(n) = (1 + 0,07n + 0,0007n²) × configVidaMob × ajusteVidaDataMap
D(n) = (1 + 0,045n + 0,00035n²) × configDanoMob × ajusteDanoDataMap
```

| Nível | Vida multiplicador antes de teto | Dano multiplicador |
|---:|---:|---:|
| 1 | 1 | 1 |
| 10 | 1,6867 | 1,43335 |
| 20 | 2,5827 | 1,98135 |
| 50 | 6,1107 | 4,04535 |
| 100 | 14,7907 | 8,88535 |

Melee usa atributo nativo; projétil/explosão de mob recebe multiplicador em `Pre`. Equipamentos de humanoides aparecem a partir do 10, em faixas de couro/madeira, corrente/pedra, ferro, diamante e netherite; chances de armadura/encantamentos crescem com nível. Isso soma proteção nativa à vida aumentada. A tabela não incorpora dificuldade vanilla, equipamento, crítico, elite ou tipo de dano.

No 100, uma base de 20 HP resulta em 295,814 antes de elite. Multiplicar por elite ×8..11 daria 2.366,512..3.253,954, mas o atributo nativo limita a **1.024**. Elites diferentes podem ter a mesma vida efetiva apesar de escalas distintas; armadura/dano ainda diferem. Um Warden com vida base alta atinge teto muito antes. Não há evidência de remoção desse limite vanilla pelo Oxy.

### 6.2 Elites e comportamentos

Chance padrão 5%, escala de tamanho 2..4. Wither/Dragão excluídos do sorteio, Warden não. Vida ×`5+1,5escala` =8..11; ataque ×`1,5+0,5escala` =2,5..3,5; config/data map quando aplicáveis. Velocidade normal é reduzida a ×0,92..0,80. Abaixo de 40% HP, enrage aumenta movimento/dano. Creeper elite multiplica raio original, normalmente 3→6..12, explosão vanilla, não a mesma fila manual do meteor.

`EliteAbilities` dá telegraph de 20 ticks e recuperação após uso; ataques têm intervalo típico 130 ticks, invocador 220 e sentinela 200. Exemplos: esmagamento/knockback, tiro múltiplo, veneno, wither, cegueira/puxão, fogo, terror, maré, vento, raios e cura/Resistência. Dano manual é `base × D(nível) ×(1,5+0,5escala)` nesse módulo. Config de **dano de elite não entra em todas essas chamadas manuais**, embora entre no atributo/handler de projétil: ajustar a config não equivale a ajustar igualmente cada habilidade especial.

`EliteAffixes` acrescenta um dos seis afixos: Erupção, Carcereiro, Vampírico, Baluarte, Tempestade, Predador. Telegraph 32 ticks (Baluarte 20), intervalo 220/140 em enrage. Fontes de área geralmente raio 3,5, dano base 3, ou 1,5 Carcereiro/3,5 Tempestade, com escala. Baluarte reduz dano por ×0,6 durante 70 ticks; Vampírico cura até `min(2%vidaMáxima;30%danoPost)`, intervalo 20. Invocador limita reforços por consulta local (até dois), vida temporária de 240 ticks; drops/progresso de reforços diferem do caminho de invocações do jogador.

Telegraphs são pacotes visuais, não só partículas aleatórias. `CombatTactics` acrescenta reposicionamento em zumbis/esqueletos/pillagers/vindicators vanilla e limita navegação durante o preparo. Não foi validado que todos os goals de ataque de todos os mobs/mods respeitem essa pausa; uma animação de aviso não garante ausência de outro ataque nativo durante ela.

### 6.3 Chefes e controle

Não existe lista compartilhada de chefes imunes a gelo, raiz, levitação, lentidão, cegueira, AOE ou minions. Existem restrições locais: transformação não aceita Wither/Dragão/Warden; transporte exclui Wither/Dragão; `canFreeze()` condiciona congelamento; Minecraft decide aplicação de muitos efeitos e danos. Dragão usa partes de entidade: seleção por `Mob` e impacto em partes podem não ser equivalentes a impactar o corpo; teste obrigatório.

Mímico mantém negação de targeting ao atacar; aranhas persistem com alvo vivo; invocações são imunes a várias fontes; voo/esquiva gratuitos durante buffs limitados podem reduzir exposição. Esses são candidatos a estratégias de exploração de IA. Não se afirma que derrotam automaticamente Dragão/Wither/Warden: dano indireto, goals próprios, alcance vertical, partes, imunidades e carregamento de chunks precisam ser reproduzidos.

Na prática de grupos: Mago/Piromaníaco/Hidromante/Trovão têm múltiplos pulsos/áreas; Guerreiro explosão próxima; Assassino burst concentrado; Necromante distribui AI; Xamã sustenta grupo; Aviário converte altura/velocidade; Vampiresco sustenta por vários vínculos; Ilusionista redireciona pressão. Esta leitura funcional não substitui medição de tempo para matar, gasto por dano útil e exposição.

### 6.4 Drops, economia e incentivos

`MobLoot` só recompensa `Enemy` válido, exclui invocação/filho de elite e respeita `doMobLoot`. Uma tentativa normal, duas em elite. Chance normal `min(0,65;0,10+0,002nível+0,002sorte)`; elite passa a verificação. Material depende da faixa: cobre cedo; ferro/ouro após 20; esmeralda após 40; diamante após 60; fragmento de netherite após 85, ramo de 3% no sorteio. Diamante/fragmento quantidade 1; demais `1+floor(nível/35)+1 se elite`. Algumas faixas são condicionais sucessivas, portanto percentuais de ramos não somam de forma independente.

Poção de mana: 6% normal/20% elite, grau por nível 25/60. Drops stackáveis vanilla elegíveis recebem complemento `min(4;floor(nível/40)+1 se elite)`. Há ainda `EliteMobs.reward`: esmeraldas 2..5 e 25% de diamantes (senão ferro), quantidade 1..3, **sem exigir nível 60**. Assim, o gate de diamante do `MobLoot` não é universal.

Patrulhas próprias: capitão elite recebe XP RPG ×6, outros elites ×4; orbes vanilla elite ×3. Capitão dá esmeraldas, duas poções e item/acessório próprio por faixa/sorteio, adicionais aos outros fluxos. Recompensas profissionais/contratos também se somam. A dificuldade de alto nível aumenta muito mais que o XP por abate normal: é plausível que farmar mobs antigos de baixo nível seja economicamente mais eficiente. Não se recomenda mudar a curva sem medir XP/h, recursos/h, risco e mortes.

**Evidência da seção:** `MobLevels.assigned/balance/equip/damage`, `EliteMobs`, `EliteAbilities`, `EliteAffixes`, `CombatTactics`, `MobLoot`, `RaiderPatrols.drops` [S15, S18].

## 7. Survival, mundo aberto, multiplayer e desempenho

### 7.1 Mundo, mineração, fazendas e estruturas

| Interação | Implementação confirmada | Questão a validar para survival/servidor |
|---|---|---|
| Mineração em área | Tremor natural até 14/22 blocos; cratera de meteor e magma | drops não equivalem automaticamente a ferramenta com Toque Suave/Fortuna; hooks de proteção divergem |
| Cratera Meteoro | raio até 14, força até 12, limite `min(1800;100+floor(140F))`, no máximo 1.780 com F=12 | alterações permanentes; logística de reconstrução e spawn protection/claims |
| Detritos | limite `min(160;24+floor(F×10/14))`, orçamento de criação global | possíveis danos/colisões/drop; não presumir que todo detrito seja bloco coletável |
| PyroTerrain | até 32 quebras e 32 scars por impacto, magma/netherrack permanentes | criação de material sem mineração comum; finalidade/economia e colocação protegida |
| Gelo/teia temporários | só em ar, limites e ledger; quebra comum não dá drop | pistão/deslocamento pode afastar bloco de sua posição rastreada; risco de material permanente |
| Bolha de água | remove água temporariamente, respeita checks locais, restaura ar | fazendas, correntes, água reentrante, bloco colocado antes de restaurar; sem cap global explícito de ledger |
| Agricultura do Xamã | até quatro fertilizações por pulso, sem farinha real | recursos por MP e automatização; não é defeito só por ser vantagem de classe |
| Transporte Aviário | um mob como passageiro; solta em pouso/logout/dimensão | pets/aldeões/Warden, captura sem consentimento territorial, invasão de farms |
| Voo/metamorfose | exploração sem Elytra na janela ou por mana | estruturas/cavernas, fuga de risco, limites de mana e carregamento de terreno |
| Água/lava/queda | respiração/Conduto, Fênix/fogo, freio/meteoro, Twilight/Eclipse | fronteiras de fonte de dano, imunidade real a contato ou queimadura e combos com enchants |
| Comida/sede | Warrior drena rápido, Shaman evita carne, Vamp usa sangue | alimento infinito em fazendas, alvos protegidos, consumíveis benéficos recusados |

A fila de cratera tem limites de trabalho por tick: até 600 checagens, 120 alterações e 12 detritos no servidor inteiro, com até 160 verificações de uma cratera por rodada. Isso controla o pico, mas **não limita a quantidade total de crateras enfileiradas** por vários jogadores. O dono é referenciado na fila; logout não contém uma remoção explícita da tarefa. Não há rollback automático do terreno destruído. O ledger de gelo/água tem outro objetivo, não reverte crateras/scars.

Recursos de worldgen sobrepõem conjuntos vanilla: aldeias com espaçamento 26/separação 8, villages jigsaw tamanho 8/distância máxima 112; outros conjuntos incluem cidade antiga, trial chambers, mansões, estruturas do Nether/End. Pools de casas adicionam estruturas Oxy, como posto, torre e ruínas. **Não há garantia de estrada global interligando todas as aldeias/estruturas**: participação em pools/jigsaw não significa uma rede contínua. Alterações só geram em chunks novos; não redimensionam aldeia já salva. Datapacks/mods que sobrescrevem o mesmo resource ID podem disputar a definição pela ordem de packs.

`VillageLoot` conserva cada item de loot de baú vanilla de aldeia com probabilidade `clamp(0,6 × configVillageLoot;0;1)`, não simplesmente corta o número de baús. Não reduz todo comércio de aldeão nem loot de estruturas de qualquer namespace. Aumentar vilas reduzidas em loot de baú ainda pode aumentar comércio, casas, ferro e habitantes úteis para farms: efeitos econômicos devem ser medidos em conjunto.

### 7.2 Aldeões, guardas, contratos e patrulhas

Existe um contrato ativo por jogador, vinculado ao UUID do NPC. Oferta atual: três por profissão vanilla (13 profissões, 39), três de guarda e 15 de cinco papéis de Homesteads; catálogo inclui sete legados, total **64 entradas**, nem todas ofertadas para novas seleções. Aldeões comuns dão entregas ligadas a profissão; guardas têm objetivos de combate. Contratos vanilla normalmente dão 6 esmeraldas +50 XP de trabalho; guarda 12/14/14; Homesteads 8. A cada cinco conclusões mais três ouro; a cada dez um diamante. Não há cooldown/limite diário/reputação que impeça repetir entregas prontas de fazenda. Isso é uma fonte renovável intencional, não duplicação demonstrada.

Conclusão é salva antes de entregar recompensa para evitar repetição da mesma conclusão; atualização de XP e itens são ações separadas, não transação atômica única de tudo. Distância/visão no protocolo de conversa são verificadas; ações 303/306 aceitam NPC vivo e visível até seis blocos e criam sessão curta. Não há confiança cega num UUID remoto enviado pelo cliente. NPC é parado ao negociar/conversar.

Guardas são uma entidade própria derivada de Villager: 40 HP, armadura 6, ataque manual 5, sem curva `Enemy` de MobLevels. Auto-spawn considera aldeão adulto em vila ou job site de Homesteads, conta guardas próximos, e tenta reforço com intervalos; não exige converter todo aldeão em guarda. Fazem defesa local, cura 1 a cada 10 s e controle de área da casa. Hostilidade ao vampiro é condicionada pelo rastreador/noite e dá prioridade a ameaças próximas; mudança de classe limpa ameaça rastreada para carregados. A classe do vampiro não deve tornar guarda um mob permanentemente hostil por save sem esse contexto, mas descarregamento/reentrada merece teste.

Patrulhas: Overworld, dificuldade não pacífica, `doMobSpawning`, config habilitada, player vivo/não espectador/acordado; não exclui criativo. Checa a cada 200 ticks. Primeira espera 30..60 s, tentativas com doze colunas **já carregadas** a 44..68 blocos, evita vila/jogador a menos de 32; nova patrulha espera 6.000 ticks (5 min). Três a cinco unidades por nível; capitão elite, vindicator na faixa ≥35. Não força geração de chunks; estar em região sem colunas adequadas impede aparecer. Expiração depois de 24.000 ticks (20 min) e sem player próximo a 96 não é despawn imediato ao sair de uma luta.

### 7.3 Autoridade, protocolo e proteção

Habilidades, XP, pontos, recargas, gasto de mana, invocações e destruição são executados no lado servidor. `RpgNetwork.Request` encaminha para handlers, não recebe dano escolhido pelo cliente. Compra de nó verifica classe, máscara, orçamento, nível e pais. Debug/teste exige OP nível 2 ou dono singleplayer e valida autorização de novo ao restaurar. Jogador normal não deveria ganhar modo ilimitado por enviar uma action de menu.

`AbilityAim` aceita dica finita de posição, entre 0,2 e 65 blocos dos olhos, corta em bloco sólido, validade viva até dez ticks e mesmo mundo. Lock de entidade exige mob visível vivo até 24, sem pet domesticado, invocação, montaria ou sono. Não compara o vetor recebido a um cone do `getLookAngle()`; isso é coerente com câmera independente/360, mas significa que o servidor trata a dica validada como a mira autorizada. É necessário distinguir essa escolha de câmera de cheat/aimbot antes de exigir um cone incompatível com o projeto.

Mana/recarga impedem spam de **casts**, porém não há um rate limit compartilhado por pacote de menu/aim/sync. A ação de consulta pode enviar vários estados e recalcular/atualizar sistemas; tráfego excessivo por cliente é um risco plausível de desempenho, não um ataque de negação de serviço demonstrado. Entradas inválidas são descartadas/negadas em vários caminhos, mas não foi executado fuzzing de protocolo.

Proteções de terreno:

- Meteor/PyroTerrain: chunk, `mayBuild`/`mayInteract`, dureza/block entity e `CommonHooks.fireBlockBreak` no caminho de quebra.
- Tremor: `mayInteract` e elegibilidade, seguido de `destroyBlock`, sem o mesmo evento explícito de quebra.
- Gelo/teia: ledger/limites/air/chunk/`mayInteract`; colocação manual `setBlock`, sem integração específica de claims.
- FluidPockets: checks de build/interação e spawn protection explícita; alterações diretas de água por ledger.
- Scars magma/netherrack: check de quebra não significa que um mod de proteção receba também evento de **colocação**.
- Teleporte, transporte e phasing: não consultam uma API de claim/região de PvP.

Logo, **não há compatibilidade comprovada** com plugins Bukkit/Paper, facções, land claims ou PvP regional. Hooks genéricos NeoForge podem permitir a alguns mods interceptar parte das ações, mas precisam ser testados em cada operação. Um futuro servidor NeoForge também não ganha suporte a plugins de outro software de servidor por apenas instalar Oxy.

### 7.4 Orçamentos, operações por tick e riscos de escala

| Sistema | Limite/ritmo encontrado | Risco que o limite não resolve |
|---|---|---|
| Necromante | 16 mobs/dono, três grupos; TTL por família | AI/pathfinding multiplicam com número de donos; aranhas com alvo vivo podem durar |
| Xamã | quatro animais, pulso a cada 20 ticks, 32 tentativas de planta | farms/consultas de pacientes por muitos jogadores |
| Ilusionista | quatro cópias +seis cosméticos; retorno além de dez blocos | cópia de AI/mod com custo alto; toda consulta de targeting/colisão |
| Vampiresco | seis escoltas; 8.192 pontos lógicos/mundo, 60 s | render de marcas é outra estrutura; escaneamento de ameaças/animais |
| Arqueiro | rajada até dez, chuva até 14; flechas com TTL 160 | rotina percorre coleção global de flechas por player: aproximadamente O(P×A) |
| Piromaníaco | 3/6 núcleos, 24 posições de brasa, 10 ticks entre magma | linhas de pares acumulam; cada brasa faz consulta de mobs |
| Meteoro | 600 verificações/120 alterações/12 detritos servidor/tick | fila não tem teto total; latência de fila cresce com simultaneidade |
| Gelo/teias | até 1.024 posições somadas de gelo e teia por mundo; restauração periódica | escrita de ledger/posição movida externamente |
| Sangramento | dano 1 Hz; partículas a cada quatro ticks por ferido | rede/emissão cresce com número de feridos, sem budget único global para todos os sangramentos |
| Sangue por impacto | pacote para próximos até 48; expansão local | não usa o budget de 64 eventos de explosão para cada dano |
| Partículas de impacto | emissão local até 64/tick neste caminho | partículas nativas emitidas por outros módulos têm outros limites |
| Sangue visual local | até 128 partículas/tick, lotes de até 16; total `ceil(dano×1,5×intensidade)` | fila de bursts sem teto de quantidade; sangue alto pode criar atraso/backlog |
| Minimap | cache 16.384 amostras, até quatro camadas; 32/64 amostras e deadline 1 ms | upload/varredura de textura é outro trabalho; não mede GPU/FPS automaticamente |
| Patrulha | até doze colunas já carregadas por tentativa, grupos 3..5 | múltiplos jogadores distantes criam grupos independentes |

Marcas de sangue são partículas de superfície com duração configurável no cliente; nenhuma alegação de “sem limite físico do engine” foi feita. O código retém o total artístico desejado em lotes, mas o engine vanilla e opções de distância/partículas podem descartá-las. A vontade de sangue abundante pode ser preservada enquanto se mede memória/backlog; não é necessário cortar a estética sem evidência de gargalo.

Há persistências síncronas de mana a cada gasto normal e de vários progressos/colocações por arquivo atômico; colocação relevante pode regravar o conjunto inteiro de blocos rastreados. Isso pode afetar MSPT em construção/farming/combate simultâneo. Não há medição de disco, TPS ou número máximo seguro de jogadores nesta auditoria.

Cleanup merece atenção: ao limpar `AdvancedClasses`, flechas de skill podem perder o rastreamento Oxy sem serem todas descartadas imediatamente, diferente de remover um grupo de mobs. Projéteis de outros módulos também têm TTL/mapa próprios. Chunks não são forçados para restauração de gelo/água; operações esperam carregamento e evitam sobrescrever blocos que já mudaram. Testar reinício, dimensões e entidade descarregada é necessário para confirmar crédito de dano/estado residual.

`RpgProfiler` usa seções nativas e contadores opcionais, com média/pico e número de chamadas. `/rpg desempenho` mostra apenas essas seções instrumentadas, não o custo total do servidor nem consumo de render externo. Medir também profiler geral/TPS/MSPT e cliente em cenários definidos. [S02, S08–S10, S14, S17–S20]

## 8. Configurações e personalização administrativa

### 8.1 Config de servidor

`RpgConfig` registra `ModConfig.Type.SERVER`, sincronizada pelo NeoForge. Pode ser administrada no config server do mundo sem recompilar, respeitando carga/reload do NeoForge; não confundir com opções client de aparência. Estes são os valores distribuídos e intervalos aceitos:

| Grupo/chave | Padrão | Intervalo | Alcance da mudança |
|---|---:|---|---|
| combat.missChance / criticalChance | 0,05 / 0,10 | 0..1 | fontes aceitas pelo sorteio, não cada fonte customizada |
| combat.criticalDamage | 1,5 | 1..10 | multiplicador RPG de crítico |
| combat.enemyMissChance / enemyCriticalChance | 0,05 / 0,10 | 0..1 | inimigos elegíveis contra player |
| combat.enemyCriticalDamage | 1,5 | 1..10 | crítico inimigo |
| combat.playerDamageMultiplier | 1 | 0,1..10 | handler comum de dano com dono player, exclui sangramento |
| combat.parryWindowTicks | 6 | 1..20 | extensão da janela após início nativo do bloqueio |
| evasion.rechargeSeconds | 6 | 1..60 | energia por carga |
| evasion.secondChargeLevel / thirdChargeLevel | 25 / 50 | 1..100 | terceiro limiar é pelo menos o segundo |
| difficulty.enemyTactics | true | boolean | goals/táticas próprias |
| difficulty.mobHealthMultiplier / mobDamageMultiplier | 1 / 1 | 0,1..20 | curva geral, considerando teto de atributos |
| difficulty.eliteHealthMultiplier / eliteDamageMultiplier | 1 / 1 | 0,1..20 | modificadores principais; dano manual especial não cobre igualmente a config |
| difficulty.eliteChance | 0,05 | 0..1 | sorteio futuro, não transforma todos os mobs já vivos |
| skills.manaCostMultiplier | 1 | 0..10 | custo principal, não todos os gastos flat |
| skills.cooldownMultiplier | 1 | 0,1..10 | recargas principais; não todos os intervalos internos |
| exploration.raiderPatrols | true | boolean | geração das patrulhas próprias |
| exploration.villageLootMultiplier | 1 | 0..2 | retenção de item de baú de aldeia vanilla |
| development.profiling | false | boolean | contadores de tempo locais do Oxy |

`RpgContent.configReloaded/rebalance()` recalcula mobs hostis carregados e elites. O laço de rebalance nesse listener filtra `Enemy`, portanto não cobre automaticamente todos os golems elegíveis a MobLevels. Buffs/recargas já agendados são instantes persistidos e snapshots de cast; alterar um multiplicador não garante reescrever todos os horários/efeitos ativos imediatamente. Chance de elite e definições de worldgen não se aplicam retroativamente como respawn global.

### 8.2 Datapacks

| Dados | Pode alterar sem recompilar | Limites da implementação |
|---|---|---|
| `data/oxyrpg/rpg/classes/<enum>.json` | label, títulos, descrições, ícones, posições x/y; multiplicadores de mana/recarga/dano/duração | 51 IDs, ordem, pais e níveis preservados pelo decoder; textos não reprogramam skills |
| `data/oxyrpg/rpg/quests.json` | identidade de mob/textos/ícones e recompensa XP 0..100.000 | preserva cinco etapas, número de objetivos e quantidades para compatibilidade de save |
| `data/oxyrpg/data_maps/entity_type/mob_tuning.json` | vida/dano 0,01..20; XP 0..20; resistências fogo/frio/raio 0..1 | resistência decide pela tag/tipo da fonte real, não aparência; valores padrões zombie/skeleton =1 |
| `data/oxyrpg/data_maps/item/weapon_tuning.json` | dano 0,01..20, bônus crítico/bleed 0..1, rank bleed 0..2, por item/tag | dano da arma depende de directEntity; não configura automaticamente munição/skill flat |
| Tags de itens, blocos e dano | armaduras leves, fases, spells, comportamento de dano | fallback do Eclipse ultrapassa tags; trocar tag não remove sozinho esse OR |
| Recipes/loot modifiers/Curios | custos de crafting, retenção de loot, slots/validadores | requer respeitar IDs/codec e mods necessários; bônus não somam duas cópias só por aumentar slots |
| Worldgen vanilla/Oxy | spacing, jigsaw, pools, pesos, estruturas | chunks novos; conflitos de mesmo resource ID/ordem de packs |

`RpgContent` carrega e valida um bundle antes de aplicar; snapshot de conteúdo sincronizado até 262.144 caracteres. `/reload` pode recarregar conteúdo/datapacks, com as limitações acima. Há limites de texto/posição e obrigatoriedade das 12 definições. Definição inválida deve ser corrigida pelo administrador; o relatório não executou reload com um pack inválido.

**Exigem código/recompilação:** número de classes/slots/IDs, curva quadrática dos mobs, XP por abate base, orçamento de pontos, exclusividade/respec, maioria dos raios/danos/limites específicos, critérios de equipe/claim, políticas de CC, nova fonte/comportamento de skill e custo flat que não usa os multiplicadores. JSON de descrição não é um interpretador de habilidade.


Receitas distribuídas: poção 35 MP = frasco +lápis-lazúli +açúcar; 80 MP = poção básica +ametista; 160 MP = maior +pó de blaze +pó de glowstone. Bandagem usa cinco linhas +um papel para duas unidades; antídoto olho de aranha +frasco de mel +frasco; botas quatro ferros +duas penas. Esses insumos ligam consumíveis ao survival, mas farmabilidade e crafting em lote precisam da medição de economia, especialmente onde custos flat escapam do multiplicador geral.

### 8.3 Comandos e dados persistidos

Comandos normais: `/rpg classe <classe>` (seleção única), `/rpg habilidade [1|2|3]`, `/rpg status`, `/rpg missao`, `/rpg kit`. `/rpg selo` entrega o item de selo sem exigir OP; não autoriza buff arbitrário. Administração: `/rpg resetclasse [jogador]`, `/rpg elite [escala 2..4]`, `/rpg exportarperfil`, `/rpg desempenho [limpar]` e debug/teste conforme autorização.

`/rpg debug ajuda` descreve `on/off`, `skills todas/limpar`, `nivel 1..100/subir/descer`, `classe`, `cooldowns on/off/limpar`, `recursos normais/ilimitados`, `atributos maximos/normais`, `arsenal`, `tudo`, `recuperar`. `/rpg teste on/off/<classe>` usa 100 e liberação geral, com custo/recarga normais. Debug permite estado irreal: desabilitar skill cooldown não remove cada intervalo interno/heat/restrição de água. Não existe comando normal de respec parcial.

Mapeamento estável relevante: enum `ARQUEIRO` = Assassino (comando `assassino`); `ARQUEIRO_NOVO` = Arqueiro (comando `arqueiro`, alias `arqueiro_novo`); `PIROMANTE` aparece como Piromaníaco, `HIDROMANTE` como Hidromante. Renomear enum não é só tradução, altera compatibilidade dos perfis.

Dados do mundo: attachment serializado do perfil/debug nos dados do player; mana em `oxyrpg/resources/<UUID>.properties`; profissões em `oxyrpg/professions/<UUID>.properties`; ledger `temporary-<dimensão>.properties` e `fluid-<dimensão>.json`; export em `oxyrpg/exports`. Outros NBTs têm owner/recibos/cooldowns/ameaça/energia. Editar arquivos com o mundo ativo pode competir com caches/saves; nenhum editor administrativo validado de todos esses estados foi encontrado. Na revisão de servidor, preferir comandos e datapacks suportados em vez de tratar o legado `.properties` como a única fonte.

### 8.4 Bibliotecas e complementos: o que a auditoria confirma

O build declara owo-lib, Curios e GeckoLib; Player Animation Library, EMF/ETF são dependências de compilação opcionais. Há código de render/animação, compatibilidade de Barrel Roll, escolha client de pack Terralith e `ComplementCompat`: temporários recebem tag `ts.mm.exclude`; Homesteads é reconhecido por papel/job site. Essas são integrações concretas e limitadas. Não certificam qualquer versão futura dessas bibliotecas nem tornam suas mecânicas parte da fórmula de combate Oxy.

Adicionar Better Combat/Combat Roll/terreno/novos mobs pode mudar ataques nativos, poses, geração ou AI. A esquiva, mana, filtros e valores Oxy continuam nos módulos examinados; uma animação externa não fornece automaticamente validação de PvP. Não foi presumido que recursos de mods externos instalados corrigem os achados deste relatório. Não houve pesquisa externa nem instalação de complementos neste pedido; a fonte de evidência é o checkout atual.

## 9. Diagnóstico final

Critério: **Crítico** = exploração/perda de integridade/falha grave; **Alto** = falha importante de gameplay ou vantagem desproporcional; **Médio** = limitação/desequilíbrio possível; **Baixo** = refinamento. Gravidade descreve impacto para o objetivo Survival PvPvE; não é uma certificação de vulnerabilidade técnica. Não há exploração crítica comprovada por execução nesta auditoria. Riscos de integridade abaixo permanecem explicitamente hipóteses.

### 9.1 Problemas/diferenças comprovados pelo código

| Sistema ou classe | Comportamento confirmado | Possível problema para o objetivo | Gravidade | Evidência no código |
|---|---|---|---|---|
| Ofensivas compartilhadas | `attackable` exige `Mob`; várias skills nunca selecionam player | classes não possuem o mesmo conjunto anunciado em PvP; precisa política explícita | Alto | `RpgAbilities.attackable/near/onImpact`, módulos de áreas [S06–S10] |
| Assassino | invisibilidade só acaba por `Post` positivo dentro de filtro Mob | atacar player mantém estado apesar de texto de primeiro ataque | Alto | `RpgAbilities.damagePost` 328–348 [S06] |
| Parry | retorno do feedback para player antecede consumo do counter | crítico garantido pode permanecer por vários golpes na janela PvP | Alto | `CombatFeedback.damage` 26–42; `ShieldParry.counter/consume`; `CombatChances.incoming` [S13] |
| Ilusionista/Eclipse | fallback passa blocos destrutíveis sem block entity nos três tiers | melhorias não limitam acesso; passagem por paredes já inicial | Alto | `SpecialistCollisionMixin.oxyShape` 22–25 [S09] |
| Guerreiro/comida | Vampirismo drena ~3,333 comida/s e acerto causa exaustão | texto promete reposição que não acontece; sustain e custo têm leitura errada | Alto | `RpgAbilities.onTick` 279; `damagePost` 334–335 [S06] |
| Árvore de classe | 49 pagos, até 50 pontos, nenhuma exclusão | especialização final não é escolha entre ramos | Médio | `SkillTree`, `RpgProfile.skillPoints/unlockSkill` [S01, S05] |
| Vida elite | fórmulas podem ultrapassar 1.024; native sanitiza atributo | escala visual/dificuldade deixa de corresponder à vida pretendida em níveis altos | Médio | `MobLevels.balance`, `EliteMobs.promote`; fonte nativa `Attributes.MAX_HEALTH` [S15] |
| Upgrades | Ilusionista 2/6/13, Hydro 9/13, Pyro 2 divergem em efeitos específicos | gastar ponto não entrega a parcela anunciada | Médio | tabela 3.3; `MimicAppearance`, `RpgSpecialists`, `RpgProfile.baseDuration` [S09] |
| Xamã/suporte | aura inclui todo player, limpa todo nocivo | cura/limpa oponente sem distinção de equipe | Médio | `RpgShaman.pulse` 72–76 [S10] |
| Sangramento | aura Xamã remove; leite é bloqueado | bandagem não é a única cura de ferida apesar de regra textual | Médio | `Bleeding.removed`, `RpgShaman.pulse` [S16, S10] |
| Recursos/config | drain, poções, relíquias e itens usam custos flat | custo global não equilibra igualmente todas as ações | Médio | `RpgResources.baseCost`, `RpgVampire.tick`, `RpgShaman.potion`, `ClassRelics`, `AdventureItems` |
| Elite/config | dano manual especial não usa toda a config ELITE_DAMAGE | administrador altera golpes/tiros, mas não uniformemente skills | Médio | `EliteAbilities.hit`, `EliteAffixes` versus `EliteMobs.damage` [S15] |
| XP/dificuldade | XP de abate base não depende do nível; nível fica salvo | esforço cresce quadraticamente sem recompensa de XP equivalente | Médio | `OxyRpg.onEnemyDeath`, `MobLevels.assigned`, `EliteMobs.xp` |
| Loot elite | pode dar 1..3 diamantes com chance 25% em nível baixo | gate do loot comum não é gate universal | Médio | `EliteMobs.reward` 57–60 [S15] |
| Profissões | requisito de item só reconhece namespace vanilla | modded gear pode ignorar a progressão equivalente | Médio | `Professions.requirement` [S11] |
| Guerreiro defesa | espinhos só `Enemy`; reflexão tem bônus Mob-only | defesa contra mobs não equivale ao pacote anunciado em duel | Médio | `RpgAbilities.damagePost/onImpact/damagePre` |
| Trovão elétrico | corrente/teleporte usam magia indireta | data map de resistência elétrica não reduz estes danos | Médio | `AdvancedClasses.channel/chain`, `RpgDataMaps.resistance` [S07, S20] |
| Custos de flecha | munição skill vira Arrow comum; Infinidade não dispensa gasto | tipo de flecha/encantamento percebido não corresponde ao disparo da skill | Médio | `AdvancedClasses.consume/arrow` [S07] |
| Mago/CC | impacto aplica frio/lentidão/fogo após `hurtServer`, sem exigir retorno positivo | um erro/dano negado pode ainda aplicar controle/status nesse caminho | Médio | `RpgAbilities.onImpact` 388–399 [S06] |
| Pós-logout de cratera | fila não é cancelada junto com voo | destruição continua mesmo sem o lançador presente | Médio | `MeteorCrater.enqueue/tick` versus `RpgAviary.clear` [S17, S14] |
| Twilight | rejeita também efeitos benéficos; +25% vale para dano não-spell | texto “dano físico”/“poções” pode esconder custos/benefícios diferentes | Médio | `RpgVampire.incoming/applicable`, `VampirePotionMixin` [S10] |
| Guerreiro n19 | +3% movimento, não redução direta de 20% da Lentidão | diferença menor de descrição quantitativa | Baixo | `RpgAbilities.stats` 307–308 [S06] |

### 9.2 Riscos plausíveis — exigem testes

| Sistema ou classe | Comportamento confirmado | Risco a reproduzir | Gravidade potencial | Evidência no código |
|---|---|---|---|---|
| Gelo/teia/pistões | restaura por posição e bloqueia só quebra rastreada | mover temporário e colher fora da posição pode produzir material indevido | Crítico se exploração renovável for confirmada | `TemporaryBlocks.tick/breakBlock/placed`, ausência de rastreador geral de movimento [S17] |
| Proteção de terrenos | hooks variam; muitas mudanças são `setBlock/destroyBlock` | dano/entrada/remoção de água em região protegida contra a política do servidor | Alto | operações detalhadas em 7.3; nenhum adaptador de claim [S17] |
| Cópias hostis | AI comum, proibição de dano de qualquer player às cópias | atacar adversário/área PvP-off através de mob protegido do contragolpe | Alto | `RpgSpecialists.incoming/targeting`, `MimicAppearance` [S09] |
| Mímico e chefes | targeting do player disfarçado negado, sem cancelar por ataque | dano PvE sem exposição e manipulação de AI de boss | Alto | `RpgSpecialists.targeting/illusionTick` [S09] |
| Muitos casts/packets | custos principais existem, não rate limit geral; filas/coleções globais | aumento de MSPT/rede/backlog com simultaneidade ou consulta excessiva | Alto | `RpgNetwork` 225–233; `MeteorCrater`, `AdvancedClasses`, `ParticleFx` |
| Controle recorrente | raízes renováveis e frio/cegueira sem DR comum | chefes/pacíficos neutralizados por cadeia de CC | Alto | `RpgVampire.damage/Post`, mapas FROST/BLIND [S06, S08, S10] |
| Native gear/burst | multiplicadores e +2 pós-mitigação, flechas rápidas | TTK incompatível com reação/escudos em alguns equipamentos/níveis | Alto | exemplos e matriz; medir HP efetivo/latência [S06, S07, S13] |
| Logout durante DoT/buffs | efeitos e créditos usam UUID, mapas/tempo do mundo | fugir do combate pausando dano, perder crédito ou reter estado parcial | Médio | `Bleeding.owner`, módulos `clear/logout`, ausência combat tag |
| Filho de elite/temporário | exclusões de XP/loot/profissão não são idênticas entre handlers | farm de progresso auxiliar usando reforços ou crédito indireto | Médio | `OxyRpg.onEnemyDeath`, `VillagerQuests.death`, `MobLoot.reward` |
| Profissão/blocos movidos | colocado rastreado por posição | recolocar via pistão/transporte para farmar XP | Médio | `Professions.placed/broken` [S11] |
| Minions e boss multipart | filtros Mob, sessão de grupo e AI própria | skill sem efeito em partes, servos presos, invisibilidade seletiva | Médio | `RpgNecromancer.valid`, `RpgAbilities.onImpact`, `AbilityAim` |
| Água/ledgers | restaura só em estado elegível, defer unloaded | fonte perdida ou interação inesperada com farms/packs durante restart | Médio | `FluidPockets.tick`, `TemporaryBlocks.tick` [S17] |
| Flechas após limpeza | remover tracking não descarta toda Arrow de skill imediatamente | TTL/efeitos nativos residuais após logout/dimensão | Médio | `AdvancedClasses.clear/tick` [S07] |
| Sangue abundante | burst queue sem teto, produção local 128/tick | backlog visual/FPS/memória em dano alto ou muitos feridos | Médio | `ParticleFx.blood/tick`, `CombatParticle` [S19] |
| Mira e voo | hints validados mas simulação client/servidor e fontes distintas | desvio de item/shot, giro, falsa colisão em ping alto | Médio | `AbilityAim`, `AdventureItems`, `RpgAviary`/mixins [S12, S14, S20] |
| Contratos/vilas | entregas repetíveis e bônus por conclusão, mais vilas | excesso de diamantes/esmeraldas/ferro por infraestrutura pronta | Médio | `NpcContracts`, `VillagerQuests`, `VillageLoot` [S18] |

### 9.3 Sugestões opcionais para o especialista

1. **Definir a política de PvP por família antes de ajustar dano.** A evidência é a tabela 5.1: algumas classes usam três ofensivas Mob-only e outras acertam players por caminhos nativos. Pode-se preservar skills PvE se isso for deliberado, mas a seleção/descrição deve tornar o escopo claro.
2. **Decidir a finalidade da árvore de classe.** Com 49 pontos pagos e 50 disponíveis, exclusividade final não existe. Manter liberação total é legítimo; especializações finais exigiriam orçamento/topologia/respec próprios. Não sugerir cortar pontos sem decidir o objetivo.
3. **Medir as três economias separadamente.** XP RPG, profissões e recursos materiais têm fontes/limites diferentes. Curvas H/D do nível 100, XP fixo e diamonds de elite/contratos justificam medir ganho por risco, não um nerf arbitrário em todos os drops.
4. **Separar imunidade de controle, escape e counterplay.** Eclipse impede ataque, morcego consome MP, Mímico mantém targeting protegido mesmo atacando; não são benefícios equivalentes. Definir regras desejadas para chefe/território/duelo antes de copiar imunidade nativa.
5. **Tratar blocos permanentes como decisão de mundo.** Crateras/magma podem ser identidade da classe. Se servidor tiver bases protegidas, definir autorização/rollback e medir destruição (até 1.780 blocos alterados por impacto de força máxima), em vez de eliminar a habilidade por divergir do vanilla.
6. **Medir pacotes simultâneos com observabilidade existente.** Há caps por tick e limites por dono, mas não número máximo seguro de jogadores. Testar a carga real do servidor pretendido antes de escolher limites novos para minions/sangue.

Nenhuma nova faixa de dano, porcentagem de custo, nerf ou buff é prescrita como valor final. Os valores atuais e as interações que justificam cada investigação estão documentados; o especialista deve propor metas de TTK, tempo de progressão e economia e usar as medições para escolher números.

## 10. Roteiro de testes práticos necessários

**Não executado nesta auditoria.** Fazer cópia de mundo de teste e manter registros de versão, mods, datapacks, dificuldade, gamerules PvP/mobGriefing/mobLoot, TPS, distância de simulação/render, latência e equipamentos. Modo teste é útil para acesso, mas seus **160 pontos de atributos** não representam uma build normal; medir também perfis com orçamento legal de 99 ou menos. Debug ilimitado não serve para concluir sustentabilidade de mana/cooldown.

### 10.1 Progressão e integridade

| Teste | Procedimento | Evidência a registrar |
|---|---|---|
| XP RPG/vanilla | matar inimigos iguais em níveis 1/20/50/100, elite/normal/minion; completar ciclo de missão | XP RPG, orbes vanilla, bônus de 10 kills; comprovar ausência de escala automática por nível |
| Árvores | construir progressão legal, comprar full skills e 49 pagos; tentar sem pais/nível/ponto | gasto, nível mínimo, ponto sobrando, masks; tipos por UUID e 99 atributos |
| Mana | uma skill de cada classe; rejeição sem alvo/munição/calor; extra, drain, poção, item, relíquia | MP antes/depois, regen início, cooldown restante; config e eficiência separadas |
| Salvamento | logout/restart/morte/dimensão com skill, mana e recarga ativos; desligar debug | perfil real, flags temporárias, mana, equipamento, efeitos, entidades e logs |
| Reset/respec | testar reset administrativo em cópia do mundo e retorno de gear | o que preserva/zera, jobs/recibos; nenhum teste destrutivo no mundo principal |
| Autorização | contas normal/OP, retirada de OP, pacote/debug inválido | negação server, sem ganho de pontos/resources ou acesso a estado alheio |

### 10.2 Combate, classes e PvP

Aplicar a matriz de 12×12 em ida e volta, inicialmente mesma build legal/equipamento vanilla e depois matchup assimétrico deliberado. Medir HP perdido por acerto, TTK, mana por dano útil, taxa real de hit/crit/miss, tempo de controle, recuperação, oportunidade de reação e mortes. Repetir com escudo/parry, armadura/Proteção, efeitos, alturas e latency; amostras suficientes para probabilidades, sem inferir 18% de crítico de três golpes.

| Classe/sistema | Cenários obrigatórios |
|---|---|
| Guerreiro | circular 1/5/20 mobs e player; Força; cura por AOE; fome 20/saturação alta; espinho Enemy/player; reflexão frente/costas e dano/owner da Arrow |
| Mago | gelo em mob resistente/normal, miss e dano negado; boss/partes; tempo paralisia/frio; jato girando; quake após afastar/dimensão; hooks e drops |
| Assassino | dagger multi-hit; invis atacando mob/player, acerto bloqueado/erro; execução+furtividade+maça/Sharpness; second dash e paredes |
| Arqueiro | sem munição, spectral/tipped/Infinity; rajada/chuva native i-frames; player no trajeto; mark player negada; guiada atrás de parede; roll livre aerial durante/fora da janela |
| Necromante | 16 unidades; alvo pacífico/hostil/boss; ataque player contra servo; spider no alvo vivo >duração HUD; crédito de flecha/dot/minion, unload e rejoin |
| Xamã | aura com ally/enemy player e bleeding; cura mortos-vivos; raio em soco miss; agricultura por MP; splash em escudo/Twilight, poções positivas/negativas |
| Aviário | decolagem/segunda ativação em níveis; água/criativo/ping/Barrel Roll; freio agachado/landing; meteor frontal/vertical através de parede; carregar pet/aldeão/Warden e soltura |
| Ilusionista | formas pequenas/grandes/copies variantes e HP; hostil contra player/território/PvP-off; dano contra cópia; matar enquanto disfarçado; phase pedra/porta/BE/portal e retorno seguro |
| Hidromante | chuva/tempestade/submerso/seco; waterbreathing/mining/speed; mira jato em movimento; n9/n13; waterwalk dive, cura, pocket com corrente/waterlogged/restart |
| Piromaníaco | 100 heat/overheat; water/raincooling; weights de fogos; 6 cores/15 linhas; scars/drops, claim; duplo pulo e pouso; brasas sobrepostas e final explosion |
| Vampiresco | feeding fome cheia/vazia com pet/aldeão/golem; probabilidade bleed; múltiplos bonds/heal/root; mana drain/regen; dano/escorta bat; dia/chuva/Nether; owntrail; Twilight positivo/negativo/bleed |
| Trovão | teleport alvo parede/lava/borda/chunk não carregado; channel real tipo de dano; dois punhos vazios/+2 em armadura/player; steps e sprint; calor bioma/Nether e swim |
| Parry | counter de 30 ticks contra mob/player, vários golpes e block/miss; garantir diferença documentada; escudo de outro mod pela tag |
| Sangramento | mãos vazias, cada arma data map e rank; player/mob undead/elite; leite/bandagem/aura/antídoto; dano infinito/owner offline/Twilight/Eclipse |

Aferir imunidades/controles separadamente em Zombie, Skeleton, Creeper, Pillager, animal, pet domesticado, aldeão, guarda, golems, Warden, Wither e partes do Dragão. Na validação probabilística e de DPS, registrar tentativas versus danos aceitos; pulsos nominais não são acertos confirmados.

### 10.3 Survival, exploração e proteção

- Mineração com/sem ferramentas/enchants/profissão, usando cada skill de quebra: material realmente dropado, XP profissional, evento de proteção e custo.
- Gelo/teia com pistões normais/pegajosos, TNT, fluidos, unloaded/restart e quebra em local movido: verificar se material permanente indevido pode ser obtido.
- Cratera/bolha/scars em spawn protegido e com o mod de claim realmente escolhido; testar colocação, quebra, dano a pet/aldeão, phasing, teleporte e transporte individualmente.
- Fazendas de mob/cultivo, rota de contratos e aldeia comercial: XP RPG/h, XP trabalho/h, esmeralda/diamante/ferro/h, itens/MP, mortes e tempo de infraestrutura.
- Mundo novo/chunks novos versus antigos, diferentes ordens de datapacks e Terralith ligado/desligado: tamanho de vila, estrutura ligada ao jigsaw, quantidade de baús e loot por área.
- Patrulha após espera em colunas carregadas adequadas, vila/caverna/pacífico/criativo: logs de tentativas, distância, quantidade, capitão e rewards cumulativos.
- Cave/Overworld/Nether/End: minimapa/colisão/região e capacidade de voo; separar custo de gerar chunks novos do custo de renderizar/consultar cache.

### 10.4 Carga multiplayer e compatibilidade

Testar 1, 4, 10 e um cenário que represente a lotação pretendida, sem tratar esses números como limite seguro confirmado. Mesma área e jogadores espalhados, usando invocações máximas, chuva/rajadas, magma/linhas, meteoros, bleeds e consultas de menu. Medir MSPT percentis/pico, TPS, uso de memória, entidades/projéteis/filas, tráfego, I/O, FPS e backlog de sangue. Profiler Oxy +profiler geral; opções de partículas reduzidas/minimapa separado.

Repetir limpeza: morte/logout/dimensão/restart/unload em cada família; contabilizar entidades e danos após o causador sair. Testar complementos de combate/animação e novos mobs um por vez, registrando versões. Melhorar aparência não é evidência de que dano, owner, i-frame e hook de proteção continuam corretos. Para PvP por região, registrar se cada fonte chega ao evento/API do sistema escolhido; não presumir compatibilidade a partir de um duelo em terreno livre.

## 11. Índice de evidências e pontos de entrada

As referências usam caminhos absolutos deste checkout. Para revisão em outro PC, a parte a partir de `src/` (ou o nome do arquivo na raiz) identifica o mesmo arquivo no repositório. Métodos/ranges identificam pontos de leitura, não alegam que o arquivo inteiro seja responsável por cada achado. O índice cobre o código executável e recursos relevantes; testes existentes não foram usados como prova de funcionamento nesta versão.

| ID | Arquivos e pontos de entrada |
|---|---|
| S01 | [RpgProfile](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgProfile.java): linhas 32–79 atributos/duração; 84–103 recarga/unlock; 105–151 reset/XP/cast. [RpgClass](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgClass.java), [ProfileStore](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ProfileStore.java): 18–43 migração/attachment. [PlayerAttachments](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/PlayerAttachments.java): codecs/copyOnDeath. |
| S02 | [OxyRpg](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/OxyRpg.java): registro de listeners 23–160; sync/menu 172–252; `castAbility` 253–284; comandos/reset 309–402; `onEnemyDeath` 404 em diante. [RpgNetwork](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgNetwork.java): `Request`, codecs, handler 225–233. |
| S03 | [AttributeStats](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AttributeStats.java); [AttributeTree](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AttributeTree.java): `types/parents/available`, 160 nós, shuffle UUID. |
| S04 | [RpgResources](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgResources.java): carregamento/reservas 15–47, custo 55–73, regen/spend/sync 75–96, dano 97–104, saves/clone 106–121. |
| S05 | [SkillTree](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/SkillTree.java): constantes, níveis, pais, masks e bonus 1–33; descrições/defaults. [SkillTreeLayouts](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/SkillTreeLayouts.java); [SpecialistTrees](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/SpecialistTrees.java); [AdvancedTrees](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AdvancedTrees.java). |
| S06 | [RpgAbilities](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgAbilities.java): `attackable/activate/launch/fire/quake` 54–227; ghost 229–245; tick/passivas/stats 246–310; damage/retaliação 312–358; impacto/reflexão 366–414; status/clear 421–435. |
| S07 | [AdvancedClasses](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AdvancedClasses.java): mark/ammo/teleport/validate 45–70; activate/arrow/tick; impacto/hit/damage/channel 131–155; `chainFrom/clear`. |
| S08 | [RpgNecromancer](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgNecromancer.java): `valid/activate/spawn/tick/group/owner/summonKill/damage/incoming/drops/join/clear`; sessões, limites, crédito e TTL. |
| S09 | [RpgSpecialists](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgSpecialists.java): validate/activate 50–101; spawn/power/hit 104–129; magma/tick 131–159; `illusionTick/jet/fireTick`; dano/targeting/cleanup 215–247. [MimicAppearance](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/MimicAppearance.java): 10–20. [SpecialistCollisionMixin](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/mixin/SpecialistCollisionMixin.java): 17–27. |
| S10 | [RpgVampire](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgVampire.java): `validate/activate/returnHuman/tick/damage/incoming/shield/feed/trail/clear`, custos/formas/bonds/Twilight. [VampirePotionMixin](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/mixin/VampirePotionMixin.java): instantâneos. [RpgShaman](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgShaman.java): activate/attack/potion/pulse 20–86. |
| S11 | [ClassRules](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ClassRules.java): allowedArmor/allowedWeapon/refresh/damage/use. [Professions](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/Professions.java): requirement/award/broken/placed/refreshModifiers. [ProfessionStore](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ProfessionStore.java): arquivos/cache/nextXp/level. |
| S12 | [ClassEquipment](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ClassEquipment.java): `stage/active/allowed/weapon/armor/discount/damage/receipt`. [ClassRelics](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ClassRelics.java): `use/damage/attack/drops/clonePlayer`. [AdventureItems](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AdventureItems.java), [RpgItems](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgItems.java), [ClassJourney](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ClassJourney.java). |
| S13 | [CombatChances](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/CombatChances.java): incoming/hit marks. [CombatFeedback](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/CombatFeedback.java): 20–42 crítico e retorno para vítima player. [ShieldParry](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ShieldParry.java): janela/counter/consume. [RpgEvasion](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgEvasion.java): pool/use/tick/incoming/clone. |
| S14 | [RpgAviary](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgAviary.java): 26–69 validação/impulso, 71–143 voo/freio/queda, 146–167 contato/impacto. [AviaryCarry](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AviaryCarry.java): 19–51 marca/pega/solta. Mixins de voo/pose/carry em `src/main/java/dev/emersonoxy/rpg/mixin/`. |
| S15 | [MobLevels](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/MobLevels.java): elegibilidade/assigned/balance/equip/damage. [EliteMobs](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/EliteMobs.java): 15–60. [EliteAbilities](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/EliteAbilities.java), [EliteAffixes](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/EliteAffixes.java), [CombatTactics](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/CombatTactics.java), [MobLoot](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/MobLoot.java). |
| S16 | [Bleeding](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/Bleeding.java): 27–44 aplicação/armas, 53–75 leite/tick/dano. [VampireHunt](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/VampireHunt.java): medo/defensores/autocura. [CombatParticles](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/CombatParticles.java): 21–39 budgets/emissão. Tags de dano em `src/main/resources/data/minecraft/tags/damage_type/`. |
| S17 | [TemporaryBlocks](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/TemporaryBlocks.java): ledger/place/tick/break/placed. [FluidPockets](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/FluidPockets.java): drain/tick/placed. [MeteorCrater](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/MeteorCrater.java): enqueue/allowed/tick. [PyroTerrain](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/PyroTerrain.java): impact/allowed/scars. |
| S18 | [RaiderPatrols](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RaiderPatrols.java): 23–63 spawn/cleanup/drops. [VillagerQuests](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/VillagerQuests.java): interact/action/death/claim/session. [NpcContracts](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/NpcContracts.java): roles/catalog/rewards. [VillageGuards](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/VillageGuards.java), [VillageGuard](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/VillageGuard.java), [VillageLoot](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/VillageLoot.java): 21–25 retenção. [QuestCatalog](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/QuestCatalog.java). |
| S19 | [ParticleFx](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/client/ParticleFx.java): 37–70 budgets/blood queue. [CombatParticle](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/client/CombatParticle.java): `BloodMark`/lifetime/render. [SimpleMinimap](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/client/SimpleMinimap.java): 40–102 amostras/camadas/cache. |
| S20 | [RpgConfig](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgConfig.java): padrões/limites 14–36. [RpgContent](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgContent.java): decode/sync/rebalance 39–66. [RpgDataMaps](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgDataMaps.java): codecs/resistance 13–34. [AbilityAim](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/AbilityAim.java): 18–82. [RpgDebug](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgDebug.java), [DebugSettings](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/DebugSettings.java), [DebugControls](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/DebugControls.java), [RpgProfiler](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/RpgProfiler.java), [ComplementCompat](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/src/main/java/dev/emersonoxy/rpg/ComplementCompat.java), [build.gradle](C:/Users/canal/Documents/Codex/2026-10-10/pre/outputs/MOD-RPG-Minecraft/build.gradle). |

### Recursos de dados examinados

- `src/generated/resources/data/oxyrpg/rpg/classes/*.json` e `rpg/quests.json`: conteúdo distribuído, todos os multiplicadores de classe =1.
- `src/generated/resources/data/oxyrpg/data_maps/{entity_type/mob_tuning,item/weapon_tuning}.json`: padrão de escala/resistência e chances de armas.
- `src/main/resources/data/oxyrpg/tags/`, `data/minecraft/tags/damage_type/`, tags Curios: spells/phase/light armor/bleeding/fire e acessórios.
- `src/main/resources/data/oxyrpg/curios/slots/`, `recipe/`, `loot_modifiers/village_loot.json`: slots, consumíveis/receitas e retenção 0,6.
- `src/main/resources/data/minecraft/worldgen/{structure_set,structure,template_pool}/` e `data/oxyrpg/structure/village/`: frequência, tamanho, pools e estruturas adicionais.

### Fontes nativas locais usadas para interpretar a ordem

O cache do ambiente contém o source runtime de Minecraft/NeoForge; foi lido sem executar jogo/teste. Fontes: `net.neoforged.neoforge.event.entity.living.LivingDamageEvent`, documentação 17–44 (armadura/poções antes de Pre, absorção depois); `net.minecraft.world.entity.ai.attributes.Attributes`, 93–95 (vida máxima 1..1.024); `net.minecraft.server.level.ServerPlayer`, 1.018–1.035 (PvP/canHarmPlayer); `net.minecraft.world.entity.projectile.AbstractArrow`, impacto aproximadamente 419 (`ceil(clamp(velocidade×danoBaseEfetivo;0;Integer.MAX_VALUE))`); `net.minecraft.world.damagesource.CombatRules` (mitigação nativa).

Para dano comum com armadura, antes das alterações posteriores Oxy, a parte nativa usa `r=clamp(armadura−dano/(2+tenacidade/4);0,2armadura;20)/25`, ajustada por encantamentos aplicáveis, e dano ×`(1−r)`. Proteção usa limite de contribuição 20 e fator `1−proteção/25`. Fontes com bypass, poções, armas e hooks podem mudar quais etapas se aplicam. Essa referência explica por que porcentagens finais de armadura e soma de danos de skills não podem ser inferidas apenas pela interface.

**Limitação final de evidência:** os caminhos e fórmulas acima descrevem a implementação examinada. Funcionamento visual, comportamento completo de AI, balanceamento percebido, carga multiplayer, proteção regional e compatibilidade do modpack devem ser demonstrados pelo roteiro de testes, antes de publicar conclusões de desempenho ou de justiça competitiva.
