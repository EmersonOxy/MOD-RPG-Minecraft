# Oxy RPG 0.26.0

Minecraft **1.21.10**, NeoForge **21.10.64**, JDK **21**. Usa assets nativos do Minecraft e GeckoLib 5.3-alpha-3 para o selo animado.

## Interface e controles

- **R** abre o menu, a árvore e o diário de missões.
- **H** mostra/oculta o HUD. F1 oculta a interface, inclusive etiquetas.
- **G**, **V** e **B** ativam os três ramos da classe, com recargas independentes. **C** usa o efeito exclusivo do item de classe.
- No dash do arqueiro, segure uma direção de movimento (WASD) ao pressionar B. Sem direção, usa a mira.
- Todas as teclas são remapeáveis em Opções > Controles.
- As árvores usam ícones e molduras de conquistas. Arraste o fundo com o botão esquerdo para mover e use a roda para zoom; Centralizar volta ao nó selecionado.

## Progressão das skills

Cada classe tem **51 nós** em três ramos. Além dos efeitos iniciais, principais e
melhorias específicas, há duas preparações antes de cada principal e oito especializações
por ramo, em dano, duração, eficiência de mana e recarga. Os bônus das especializações
são cumulativos para a classe. Os dois primeiros nós são gratuitos; os outros custam
1 ponto. Receba **1 ponto a cada 2 níveis**; nível mínimo e conexões também são exigidos.

A árvore de atributos tem **160 nós**: vida, dano, sorte, resistência e mana distribuídos
por uma semente fixa do UUID. Há oito entradas e conexões alternativas entre caminhos;
qualquer conexão anterior libera o próximo nó. Cada compra custa 1 ponto, recebido por
nível a partir do 2. A distribuição não muda ao abrir a tela ou reiniciar.

Perfis da árvore antiga mantêm skills e recebem as preparações necessárias. Atributos
comprados anteriormente continuam como bônus permanentes e descontam do mesmo orçamento;
novas compras passam pelo mapa. Profissões e contratos mantêm a progressão existente.

| Classe | G | V | B |
|---|---|---|---|
| Mago | Bola de gelo: área, paralisia e frio | Lança-chamas: jato contínuo na mira | Tremor: dano periódico, lentidão e desgaste do solo |
| Guerreiro | Corte circular: dano em área e Força | Vampirismo: cura ao causar dano, com consumo rápido de fome | Bastião: resistência, brilho, espinhos e reflexão frontal |
| Arqueiro | Flecha espectral: atravessa inimigos, destaca e desacelera | Furtividade: invisibilidade inclusive equipamento, fumaça e ocultação dos inimigos | Assassino: dash, velocidade, dano aumentado e sangramento |

Valores iniciais das habilidades principais, antes das melhorias:
- Bola de gelo: raio 3 blocos, paralisia 3 s, frio 8 s; recarga 25 s.
- Lança-chamas: jato por 3 s, alcance 6 blocos, queimadura 6 s; recarga 30 s.
- Tremor: raio 4 blocos, duração 6 s; recarga 40 s.
- Corte circular: raio 3 blocos, 8 pontos de dano, Força I por 10 s; recarga 25 s.
- Vampirismo: duração 12 s, cura de 25% do dano aplicado; fome esvazia em cerca de 6 s sem ataques. Acertar inimigos consome fome; cura tem teto e intervalo mínimo; recarga 40 s.
- Bastião: Resistência III, brilho e Lentidão I por 8 s, retaliação e reflexão de projéteis pela frente; recarga 45 s.
- Flecha espectral: até três inimigos, 8 pontos de dano, brilho 10 s e lentidão 4 s; recarga 25 s.
- Furtividade: invisibilidade 10 s e fumaça 2 s. Atacar encerra a furtividade; recarga 35 s.
- Assassino: dash de até 5 blocos sem atravessar sólidos, velocidade e dano físico ×2 por 8 s, sangramento 5 s; recarga 45 s.

As melhorias aumentam gradualmente dano, raio, alcance, duração, cura e velocidade;
reduzem penalidades e acrescentam efeitos como réplicas do Tremor ou segundo dash.
Cada ícone descreve exatamente a melhoria. Dano expresso em pontos: 2 pontos = 1 coração.
Armadura, imunidades e regras vanilla podem modificar resultados.

Tremor pode quebrar **até quatro blocos naturais** de pedra, terra, grama, areia ou cascalho,
ao redor do ponto de ativação, com drops após o desgaste. Não seleciona o bloco sob os pés
nem blocos com inventários. Fogo das skills queima entidades; não coloca fogo no terreno.
Combate especial atinge inimigos; animais e jogadores não rendem XP RPG.

## Etiquetas dos mobs e níveis — 0.6.0

- Mostradas apenas dentro do campo de visão da câmera, até **24 blocos** e com
  linha de visão até o mob. As barras e textos também usam teste de profundidade:
  paredes/obstáculos ocultam as etiquetas. F1 oculta tudo.
- Hostis: nome acima, barra de vida abaixo, **Lv. N** ao lado e vida atual/máxima.
- Pacíficos/neutros: nome acima da barra e números de vida, sem nível.
  A classificação segue a interface vanilla Enemy, também utilizada pelo combate RPG.
- Novos hostis, incluindo ovos, spawners e comandos, recebem nível próximo ao do
  jogador vivo não espectador mais próximo na mesma dimensão, a até 128 blocos.
  A variação é **±2**, limitada a 1–100. Sem jogador próximo, usam nível 1.
- Cada nível acima de 1 aumenta vida máxima em **4% da base** e o atributo de
  dano de ataque em **2% da base**. Esse atributo afeta ataques que o utilizem;
  explosões e projéteis vanilla podem ter regras próprias.
- Nível e modificadores ficam salvos com o mob, sem mudar ao recarregar chunks
  ou reiniciar. Não cura mobs carregados nem recalcula seus atributos repetidamente.
  Mobs anteriores sem nível registrado ficam no nível 1; a progressão afeta novos nascimentos.
  Não muda os nomes personalizados gravados no mundo.

## Lentidão e indicadores — 0.6.0

O fragmento inicial do mago agora aplica **Lentidão II por 3 s**, ou 4 s com
Frio crescente. Corrigida a exclusão do alvo atingido quando o impacto no tronco
ficava longe dos pés usados para medir o pequeno raio de área.

Acima da barra rápida há três ícones nativos para G/V/B. Mostram estado, segundos
e barra de progresso: projétil em voo, canalização, habilidade/área ativa,
efeito aplicado aos alvos e recarga. A duração dos efeitos em inimigos é a
janela aplicada no impacto, não um acompanhamento individual de cada alvo;
morte, imunidades ou remoção do efeito podem encerrar antes.
São informações enviadas pelo servidor; não introduzem espera adicional ao lançar.
H e F1 ocultam os indicadores junto com o HUD; ficam ocultos com menus abertos.

## XP, missões e persistência

Nível 1 a 100; avançar exige 100 × nível atual XP. Inimigos derrotados rendem 25 XP.
A caçada recorrente de dez inimigos concede mais 100 XP.
Há contratos de tipos específicos e contratos mistos (zumbis, esqueletos, aranhas e creepers);
todos os alvos devem ser concluídos para receber a recompensa e avançar.
O diário e o HUD acompanham os objetivos. Partículas e sons distintos indicam nível novo
e pontos de skill recebidos. XP vanilla é independente.

Perfis por UUID ficam em **oxyrpg/<UUID>.properties dentro de cada mundo**.
Classe, nível, XP, skills, três recargas e contadores são persistidos atomicamente.
Na migração da árvore antiga para esta versão, as skills anteriores são limpas e os pontos
voltam a ficar disponíveis. Classe, nível, XP e missões existentes são preservados.
Faça backup do mundo junto com os perfis antes de retornar a versões anteriores.

## Comandos

- /rpg ou /rpg status
- /rpg classe guerreiro|mago|arqueiro|necromante|xama
- /rpg habilidade [1|2|3]
- /rpg missao
- /rpg resetclasse [jogador]: exige operador nível 2/cheats. Remove a classe,
  volta ao **nível 1**, zera XP, skills, atributos e três recargas; preserva caçadas e missões.
  Depois escolha uma classe novamente. Reset existe somente por comando.
- /rpg debug on|off|limpar: exige operador nível 2/cheats. Debug remove recargas e
  limites de pontos/nível para aprender skills, mantendo predecessores. Skills de debug
  são temporárias, não são salvas e não consomem pontos reais. Sair do servidor encerra
  debug. XP e missões obtidos nesse modo continuam sendo progresso real.

Servidor e clientes devem usar a mesma versão **0.11.0**.

## Compilar e instalar no Windows

Execute **build-windows.bat**. Usa o JDK 21 do SK Launcher e **gradlew.bat assemble**,
compilando e empacotando sem executar testes. O script usa junções locais em
%ProgramData%\OxyRPG para evitar problemas do Gradle com caminhos acentuados neste PC.
TEMP/TMP são ajustados somente durante a compilação. Não altera proxy nem certificados.

JAR: **build/libs/oxyrpg-0.11.0.jar**.
Com Minecraft fechado, instale em mods da instância **neoforge-1-21-10** do SK Launcher.
Guarde o JAR anterior fora de mods para não carregar versões duplicadas.
Preserve outros mods, mundos e perfis.

Por preferência do usuário, testes automatizados e abertura do jogo só serão executados
quando solicitados. Para solicitar o build completo com testes: build-windows.bat build.
Os resultados históricos de versões anteriores não comprovam o funcionamento de 0.11.0.
A compilação assemble de 0.11.0 foi aprovada; a validação no Minecraft fica com o usuário.


## Ajustes, atributos, mana e equipamentos — 0.7.0

As seis capturas recentes de 10/10 foram inspecionadas. Etiquetas foram elevadas
0,5 bloco e reduzidas de escala 0,025 para 0,018. Texto usa iluminação cheia;
as camadas da barra têm profundidades distintas para remover a disputa de
profundidade que deixava o preenchimento cinza/irregular. A ocultação por paredes
e distância continua ativa. A aparência desta versão ainda precisa de observação.

A habilidade inicial do guerreiro continua dando apenas Força. Foram retirados
dela o efeito visual circular e o som de corte. Dano em área, partículas de corte
e som ficam reservados ao desbloqueio de Corte circular principal.

### Atributos

Abra **R > Atributos**. Cada nível conquistado concede **1 ponto de atributo**,
independente dos pontos de skills. Perfis anteriores recebem retroativamente
nível atual menos 1 pontos disponíveis; nenhuma distribuição é feita automaticamente.
Até o nível 100 são 99 pontos. Cada clique em + gasta 1 ponto:

| Atributo | Benefício por ponto |
|---|---|
| Vida | +2 pontos de vida, equivalentes a 1 coração |
| Dano | +2% de dano causado, incluindo habilidades atribuídas ao jogador |
| Sorte | +0,2 ponto percentual na chance de drop raro adicional; também +0,25 no atributo vanilla de sorte |
| Resistência | +0,5 ponto de armadura, somado ao equipamento |
| Mana | +5 de mana máxima |

Sorte gera um item adicional ao derrotar hostis: ferro (65%), ouro (25%),
esmeralda (9%) ou diamante (1%) dentro da rolagem bem-sucedida.
Não remove drops vanilla nem ignora doMobLoot. A chance de conseguir o bônus
é 0,2% × pontos investidos em Sorte. Sorte vanilla ajuda tabelas que a utilizem.
Armadura segue os limites e cálculos de redução do Minecraft.

A distribuição fica salva no perfil por UUID/mundo. Resetar classe também
zera os atributos, nível e XP, mantendo contadores de caçadas/missões.

### Mana

Mana base **100**; gotas azuis abaixo dos corações representam dez frações
da capacidade total, mantendo uma única linha mesmo com capacidade maior.
A vida e armadura vanilla são elevadas uma linha para abrir espaço sem cobrir XP.
O HUD e a tela de atributos mostram a quantidade numérica.

Regenera **2 por segundo**, após **3 segundos sem gasto**.
O recurso é salvo em oxyrpg/resources/<UUID>.properties no mundo, com gravação
atômica após gastos e ao sair. A recuperação considera tempo de jogo; trocar
de sessão não refaz mana gratuitamente. Morrer restaura a mana máxima.
A partir de 0.8.0, o criativo também gasta mana. Apenas debug não gasta mana.
O nível e os pontos reais não são aumentados pelo debug.

| Classe | G inicial / principal | V inicial / principal | B inicial / principal |
|---|---:|---:|---:|
| Guerreiro | 0 / 12 | 20 / 20 | 25 / 25 |
| Mago | 8 / 20 | 8 / 30 | 10 / 35 |
| Arqueiro | 5 / 12 | 8 / 20 | 5 / 15 |

O segundo dash custa 5 adicionais. Sem mana suficiente, a habilidade não é
ativada e não inicia recarga. Os botões informam custos em MP.

### Equipamento de inimigos

Aplica-se a **novos nascimentos**, incluindo ovos, mantendo o nível atribuído:
zumbis, esqueletos e variantes humanoides elegíveis. Creepers e aranhas não
recebem equipamento incompatível com sua IA/visual. Arqueiros preservam arco
ou besta; inimigos corpo a corpo recebem espadas do material correspondente.

| Nível | Armadura | Arma corpo a corpo |
|---|---|---|
| 1–9 | Regras vanilla | Regras vanilla |
| 10–24 | Couro | Madeira |
| 25–39 | Malha | Pedra |
| 40–59 | Ferro | Ferro |
| 60–79 | Diamante | Diamante |
| 80–100 | Netherite | Netherite |

Cada peça de armadura tem chance progressiva de aparecer (20% + 0,8% × nível).
Encantamentos começam no nível 30; chance cresce de 25% até 90%, com poder
limitado a 30 e encantamentos de mesa. Equipamento especial, nomeado ou já
encantado é preservado; itens de tier superior não são rebaixados.
Novos itens RPG têm chance básica de drop de 1,5%, seguindo modificadores vanilla.
Equipamento e encantamentos persistem com o mob. Não reequipamos mobs antigos
a cada carregamento.

## Necromante — versão 0.8.0

Classe nova disponível em **R > Necromante > Confirmar classe** ou
**/rpg classe necromante**. A escolha segue a regra atual: para trocar uma classe
existente, /rpg resetclasse volta ao nível 1 e limpa habilidades/atributos.
Perfis das outras classes permanecem compatíveis; suas árvores não são redistribuídas.

A árvore do Necromante tem 21 nós, no mesmo padrão das outras classes:
três ramos com preparações, efeito inicial, melhoria simples, principal,
duas melhorias e melhoria final. Os primeiros dois nós são gratuitos.
Pontos de atributos e skills continuam separados.

| Ramo | Inicial | Principal e melhorias finais |
|---|---|---|
| G — Aracnídeo | Teia + 1 aranha; Ninhada aumenta para 2 | 3 aranhas; Colônia aumenta para 4 e Matriarca para 5 |
| V — Rei dos Mortos | 1 zumbi por 15 s; Companhia pútrida aumenta para 2 | 1 esqueleto + 3 zumbis por 30 s; melhorias chegam a 3 esqueletos + 5 zumbis |
| B — Céu Noturno | Cegueira 2 s em raio 3 + 1 phantom com 1 mergulho | Cegueira 4 s em raio 6 + 3 phantoms, com 1 mergulho cada |

As melhorias de Céu Noturno aumentam raio para 7, dano dos phantoms em 15%
e cegueira para 6 s. Breve eclipse aumenta apenas a cegueira inicial para 3 s.
O pacto aumenta dano das invocações em 5%; Ossos firmes aumenta sua vida em 10%;
Sombra firme aumenta resistência a empurrão do jogador em 10%.

### Controle das criaturas

- Aracnídeo lança um projétil com visual de teia. Ao atingir um mob, aplica
  Lentidão IV por 2 s na versão inicial ou 4 s na principal; Fios persistentes
  acrescenta 2 s. Coloca blocos reais de teia temporária no alvo, quando houver espaço livre.
- As aranhas aparecem perto do alvo e saltam em sua direção. Atacam o alvo marcado;
  quando ele morre, esperam **4 segundos pelo próximo mob que o jogador atacar**,
  desaparecendo se não houver novo alvo. Não escolhem alvos automaticamente.
- Rei dos Mortos busca sozinho somente mobs hostis próximos (raio 16), excluindo
  outras invocações. Atacar outro mob direciona a tropa, inclusive para pacíficos.
  Esqueletos usam arco; zumbis atacam corpo a corpo.
- Céu Noturno aplica cegueira a mobs próximos visíveis, incluindo pacíficos,
  e suspende aquisição de alvos pela IA durante a cegueira. Invocações aliadas
  são excluídas. Os phantoms mergulham na criatura da mira ou no hostil mais próximo;
  ataques do jogador podem redirecioná-los. Cada phantom da principal desaparece
  depois de **um acerto**.
- Phantoms duram no máximo 12 s caso não consigam completar ataques. Colisões e
  linha de visão são respeitadas; invocações exigem espaço livre para aparecer.
- Cada ramo mantém um único grupo. Reutilizar substitui o grupo daquele ramo.
  As criaturas desaparecem ao morrer/sair/resetar classe ou mudar de dimensão;
  afastar-se mais de 64 blocos também remove a invocação. Não são pets permanentes.
- O indicador mostra projétil em voo, aliados ativos e os 4 segundos de espera das
  aranhas. Enquanto combatem, aranhas não exibem uma duração fixa inventada.

### Imunidade, XP e drops

Suas invocações recebem dano de **mobs hostis**, mas são protegidas contra dano
do jogador, outras invocações e fontes ambientais. Não atacam jogadores nem outras invocações,
e não dão XP, drops ou equipamentos ao desaparecer. São identificadas como
**Aliado** junto da barra, em vez de inimigos de nível 1.
Mobs pacíficos agora também mostram o **nome acima da barra**, mantendo-se sem nível.

Inimigos derrotados pelas invocações dão **12 XP RPG**, contra 25 por derrota direta.
Os drops vanilla continuam disponíveis, incluindo o crédito de derrota ao jogador.
Os bônus de caçada e contrato não foram reduzidos; contadores avançam normalmente.
Sorte do jogador também pode conceder seu drop raro adicional.
Animais não rendem XP RPG, seguindo a regra existente, mas mantêm drops vanilla.

### Custos e recargas

| Habilidade | Mana inicial / principal | Recarga inicial / principal |
|---|---:|---:|
| Aracnídeo | 10 / 20 | 10 s / 25 s |
| Rei dos Mortos | 15 / 40 | 10 s / 40 s |
| Céu Noturno | 15 / 35 | 10 s / 45 s |

Mana é debitada uma vez ao lançar. Teia que não atingir um mob ainda consome mana.
Os botões mostram MP e a mensagem de ativação também informa o custo.
A partir de 0.8.0, **o criativo também consome mana**. Apenas /rpg debug on
mantém mana livre para experimentar skills. Use /rpg debug off para observar
o gasto; lembre que isso restaura as skills reais do perfil. O G inicial do guerreiro
continua custando 0 MP por ser somente um fortalecimento breve.

Compilação assemble aprovada; não foram executados testes nem aberto o Minecraft.
O funcionamento das invocações e sua IA nesta versão precisa da validação do usuário.

## Xamã e ajustes — 0.9.0

Nova classe em **R > Xamã** ou **/rpg classe xama**. A árvore mantém 21 nós,
com efeitos iniciais, habilidades principais e melhorias simples de dano,
duração, alcance, cura, quantidade de animais e custo das poções.
Para trocar de classe, /rpg resetclasse também reinicia nível, XP e atributos.

| Tecla | Efeito inicial | Habilidade principal |
|---|---|---|
| G — Deus Relâmpago | 5 s; raios com 3 pontos de dano | 15 s; raios com 6 pontos de dano |
| V — Doutor Animal | 5 s; 1 animal, cura de 0,5 ponto/s em raio 2 | 10 s; 3 animais, cura de 1 ponto/s em raio 4 |
| B — Especialista | Modo de poções por 8 s; efeitos de 4 s | Modo por 15 s; efeitos de 10 s |

Deus Relâmpago exige a mão principal vazia e um ataque a mob hostil.
Invoca um raio a cada **1,5 s** enquanto a habilidade estiver ativa.
O raio usa o visual vanilla e dano direcionado ao inimigo: não atinge o jogador,
não incendeia terreno e não transforma animais. Melhorias acrescentam dano,
3 s de duração e um segundo alvo próximo com metade do dano.
A preparação Carga somática acrescenta 5% ao dano dos raios.

Doutor Animal escolhe lobos, raposas ou ursos polares aleatoriamente.
Os animais seguem o jogador e atacam apenas hostis próximos ou marcados por seus ataques.
A aura cura o jogador, aliados e criaturas não hostis visíveis, remove efeitos negativos
(e também estados RPG de frio/cegueira/sangramento) e aplica farinha de osso real
em plantas elegíveis próximas. Pode crescer plantas e árvores pelas regras vanilla.
As melhorias aumentam raio, cura, vida dos animais, duração e quantidade até 4.
Animais temporários usam as mesmas regras de proteção, XP reduzido e drops
atribuídos ao jogador das invocações do Necromante.

Especialista exige **ambas as mãos vazias**. Ative com **B** e, durante o modo:
- Mouse esquerdo: arremessa poção negativa aleatória (dano instantâneo, veneno, lentidão ou fraqueza).
- Mouse direito: arremessa poção positiva aleatória (cura instantânea, regeneração, velocidade, força, resistência ao fogo ou resistência).

São poções arremessáveis vanilla com efeitos de nível I. Efeitos instantâneos
seguem as regras vanilla; por exemplo, cura causa dano a mortos-vivos e dano
instantâneo cura mortos-vivos. Os efeitos de área podem atingir o próprio jogador.
O intervalo entre lançamentos é 1 s, reduzido para 0,75 s com a melhoria.
Nesse modo os cliques substituem ataque/uso normais; equipar um item suspende
os lançamentos. Melhorias estendem o modo em 5 s, efeitos principais em 5 s
e reduzem em 1 MP o custo de cada poção.

| Habilidade | Mana inicial / principal | Recarga inicial / principal |
|---|---:|---:|
| Deus Relâmpago | 8 / 20 | 10 s / 30 s |
| Doutor Animal | 15 / 35 | 10 s / 40 s |
| Especialista: ativação | 10 / 20 | 10 s / 35 s |
| Especialista: cada poção | 4 / 5 | Intervalo próprio de 1 s |

Mana é consumida inclusive no criativo; /rpg debug on libera os gastos e intervalos.
Habilidades, animais e modo de poções terminam ao sair, morrer ou resetar a classe.

### Teia, gelo e phantoms

Aracnídeo coloca até dois blocos reais de teia no alvo durante a lentidão,
sem substituir blocos existentes nem colocar teia sobre jogadores.
Bola de gelo coloca gelo compacto temporário no impacto: um bloco na versão
inicial e uma estrutura cúbica oca de até 3×3×3 na principal, apenas nos espaços
livres. Evita sobrepor criaturas/jogadores. O gelo principal acompanha a janela
da habilidade; o inicial permanece 3 s. Não produz água ao expirar.

Teias/gelo são removidos ao expirar ou quebrados sem drops. O registro
oxyrpg/temporary-<dimensão>.properties preserva a remoção após reiniciar.
Chunks descarregados são limpos ao voltar a carregar, sem forçar carregamentos.
Blocos existentes não são substituídos; substituições do jogador são preservadas.

Inimigos podem ferir e matar invocações. Jogadores não podem causar dano nelas.
Invocações continuam sem XP/drops próprios; hostis derrotados por elas mantêm
drops e concedem 12 XP RPG. Cada phantom realiza **um único acerto** e desaparece.

A compilação assemble foi concluída. Não foram executados testes nem aberto o jogo;
a validação desta versão no Minecraft fica com o usuário.

## Classes, profissões, villagers e elites — 0.10.0

### Regras das classes

| Classe | Regra inicial |
|---|---|
| Guerreiro | Não usa arcos, bestas nem tridentes; +0,2 de exaustão por segundo no sobrevivência |
| Arqueiro | Não equipa escudos em nenhuma mão; vida base reduzida em 4 pontos (2 corações) |
| Mago | Recebe 20% mais dano de ataques diretos corpo a corpo de mobs hostis; não inclui projéteis nem explosões |
| Necromante | Causa 20% menos dano próprio; invocações mantêm seu dano. Velocidade reduzida em 15% durante o dia |
| Xamã | Não consome carnes, ensopado de coelho, carne podre ou peixes crus/cozidos |

O dia do Necromante corresponde aos primeiros 12.000 ticks do ciclo de 24.000,
apenas em dimensões com céu; vale também dentro de casas/cavernas. A penalidade
é um modificador independente e não apaga lentidões ou bônus de outras fontes.
Leite, ovos e alimentos vegetais permanecem permitidos para Xamãs.
Guerreiro continua podendo usar suas habilidades, incluindo as de área.

Requisitos são verificados no servidor. Itens bloqueados podem ser guardados.
Armadura acima do nível de Arsenal, itens de mão secundária bloqueados e escudos
do Arqueiro são retirados do equipamento e devolvidos ao inventário. Se estiver
cheio, equipamentos bloqueados anteriores aguardam espaço no próprio slot; não são apagados nem lançados no chão.
A redução de vida do Arqueiro soma-se aos atributos de Vida e ao bônus de profissão.
Passe o mouse sobre os botões de classe para ler suas limitações.

### Profissões

Abra **R > Profissões e contratos**. As três profissões coexistem e têm XP
independente do nível da classe. Todas começam no nível 1, inclusive em perfis
anteriores. Não distribui XP retroativo. Os níveis de classe existentes permanecem.
**/rpg resetclasse não reseta profissões nem contratos de villagers.**

O próximo nível exige 100 × nível atual XP. Máximo: nível 20, equivalente a
19.000 XP totais. Não ganha XP de profissão no criativo/espectador.
Debug de skills não libera requisitos de profissão ou regras de classe.

| Nível | Mineração: picaretas/pás | Arsenal: armas e armaduras | Agricultura |
|---|---|---|---|
| 1 | Madeira | Armas de madeira, armadura de couro | Trigo; enxada de madeira |
| 4 | Pedra/cobre | Pedra/cobre, malha, arco e escudo | Beterraba, cana e cacto; enxada de pedra/cobre |
| 8 | Ferro/ouro | Ferro/ouro e besta | Cenoura, batata e bambu; enxada de ferro/ouro |
| 12 | Diamante | Diamante | Melancia, abóbora e cogumelos; enxada de diamante |
| 16 | Netherite | Netherite, tridente e maça | Fungo do Nether, cacau, frutas doces, sementes florais especiais; enxada de netherite |

Arsenal é o nome inicial da profissão de armas/equipamentos. Machados usam
Arsenal; picaretas e pás usam Mineração; enxadas usam Agricultura.
As liberações de material se aplicam a itens vanilla. Não tenta adivinhar tiers
de ferramentas fornecidas por outros mods.
Plantar culturas bloqueadas é impedido; encontrar/comer seus alimentos continua
permitido (respeitando também a dieta do Xamã).
Plantar árvores e colocar blocos decorativos não têm requisitos de agricultura.

XP inicial:
- Mineração: 3 por bloco minerável com picareta/pá; minérios dão 15. Blocos com
  inventários e teias/gelo temporários de habilidades não rendem esse XP.
- Agricultura: 15 ao quebrar culturas maduras (trigo, cenoura, batata, beterraba,
  fungo do Nether, cacau) ou colher melancias/abóboras quebrando seus blocos.
  Plantar, colher antes da maturidade e crescimento automático não rendem XP.
- Arsenal: 15 por hostil derrotado com crédito ao jogador, incluindo invocações.
- Contratos de villagers: mais 50 para a profissão relacionada à missão.

Blocos mineráveis e frutos colocados pelo jogador ficam registrados no mundo
para não conceder XP simplesmente ao colocar e quebrar o mesmo bloco.
Não remove seus drops. Plantações podem ser replantadas e colhidas após crescer.
O registro é oxyrpg/placed-<dimensão>.txt; se ficar ilegível, é preservado e
os ganhos por quebra ficam suspensos naquela dimensão, com registro no log.

Bônus passivos ao nível 20:
- Mineração: +10% de velocidade de quebra.
- Agricultura: +2 de vida máxima, equivalentes a 1 coração.
- Arsenal: +5% no atributo de dano corpo a corpo.

XP, níveis e contrato ativo ficam em **oxyrpg/professions/<UUID>.properties**
no mundo, com gravação atômica. São preservados ao morrer/reiniciar/resetar classe.

### Missões e diálogo de villagers

Interaja normalmente com qualquer villager adulto: abre o diálogo de contrato.
**Agache (Shift) ao interagir para abrir as trocas vanilla.** Crianças e o vendedor
ambulante mantêm seus comportamentos normais.

Há sete contratos iniciais, alternados conforme o villager e os contratos concluídos:
zumbis, esqueletos, aranhas, entrega de trigo, entrega de cenouras, entrega de ferro
e colheita de trigo maduro. Cada jogador mantém **um contrato de villager ativo**;
caçadas e missões RPG anteriores continuam independentes.

Aceite no diálogo. Derrotas/colheitas contam somente após aceitar; contratos de
entrega aceitam parcelas do item na mão principal. Ao concluir, volte ao **mesmo villager**
e confirme a conclusão no diálogo nas missões de combate/colheita. Entregas de itens encerram ao fornecer a última parcela.
Há 4–8 esmeraldas e 50 XP de profissão por contrato. A cada cinco contratos recebe
um bônus: 3 barras de ouro; nos múltiplos de dez, 1 diamante em vez do ouro.
Recompensas excedentes ficam aos seus pés se o inventário estiver cheio.

O diálogo confere distância, linha de visão, villager e objetivos no servidor,
e impede receber novamente a recompensa do mesmo contrato por repetir um pacote.
Sem retornar ao contratante, não há pagamento automático. Se ele morrer ou ficar
inacessível, use Abandonar contrato em **R > Profissões e contratos**, sem recompensa.
Em missões de entrega, o diário reflete a quantidade efetivamente entregue, não o estoque do inventário.

### Inimigos elites

**5% de chance** por novo nascimento de mob hostil, incluindo ovos, spawners e
comandos normais de invocação. Mobs carregados do disco não são sorteados novamente.
Invocações aliadas, animais e bosses vanilla (Wither/Dragão) ficam excluídos.

- Escala física e visual aleatória de **2× a 4×**, incluindo sua caixa de colisão.
- Vida multiplicada por **4× a 6×**; dano por **2× a 3×**, além do nível normal.
- Nome e preenchimento da vida em **laranja**, com indicação Elite ao lado do nível.
- XP RPG **4×**: 100 normalmente ou 48 quando derrotado por invocações.
- XP vanilla **3×** o valor que seria concedido.
- Drops adicionais: 2–5 esmeraldas e 1–3 barras de ferro; chance de 25% de
  substituir essas barras por diamantes. Mantém os drops normais e respeita doMobLoot.

Nível, escala e modificadores são salvos no mob. Etiquetas mantêm campo de visão,
limite de 24 blocos e ocultação por paredes. Elites maiores precisam de espaço;
corredores estreitos podem limitar sua navegação. Alvos/grupos vanilla e outros
mods continuam usando suas regras normais.

### Interface

Menu, atributos, missões, árvore, profissões e diálogos usam molduras derivadas
da textura nativa de inventário, botões vanilla e ícones de itens. Barras novas de
progressão usam as sprites da barra de XP. A árvore mantém molduras de conquistas
e fundo de pedra. Não exige instalação de outro mod ou pacote de recursos.

Compilação **assemble** concluída; nenhuma suíte de testes nem jogo foi executado.
Ainda falta a validação manual desta versão: regras de classe, liberações de cada
profissão, diálogo/entrega de contratos, persistência após reiniciar e elites.
Os valores acima são o balanceamento inicial, ajustável nas próximas alterações.

## Correções e entregas parciais — 0.10.1

### Equipamentos preservados

Novas colocações em slots de armadura/mão secundária são bloqueadas conforme
classe e profissão, tanto no inventário normal quanto no criativo. O servidor
confere também os pacotes de colocação criativa. Os demais slots de armazenamento
continuam aceitando itens acima dos requisitos, para guardá-los normalmente.

A devolução de equipamento bloqueado anterior usa o próprio ItemStack e um
espaço vazio específico entre os 36 slots principais. Não usa Inventory.add,
não volta para a mão secundária e não cria cópias nem descarta excedentes no criativo.
O inventário é sincronizado integralmente após a movimentação. Sem espaço,
o item permanece no slot original até liberar espaço; nunca é apagado ou jogado
no chão por essa regra. Encantamentos, desgaste, quantidade e componentes permanecem.
Xamãs podem segurar carne na mão secundária; o consumo continua bloqueado.

### Entrega e confirmação dos contratos

- Aceite o contrato no diálogo como antes.
- Para entregar trigo, cenouras ou ferro, segure o item na **mão principal**
  e clique no **mesmo villager**, sem agachar. Cada clique retira somente a
  quantidade ainda necessária. O restante da pilha fica com o jogador.
- Entregas podem ser parciais. Quantidades entregues ficam salvas no contrato
  por UUID/mundo e persistem ao sair ou reiniciar. O diário conta as parcelas,
  e não os itens ainda guardados no inventário.
- A última parcela conclui o contrato e entrega a premiação automaticamente.
  Não há pagamento nem contagem de contrato concluído enquanto faltarem itens.
- Em contratos de combate (e de colheita), atingir a quantidade apenas prepara
  a conclusão. Entre no diálogo e pressione **Confirmar conclusão** para
  registrar a missão feita e receber a premiação.
- Abandonar o contrato não devolve itens já entregues.
- Shift + interação continua abrindo as trocas vanilla, sem fazer entregas.

Ao concluir há partículas de comemoração, partículas de aldeão feliz e sons.
Não dispara entidades de fogos que possam causar dano.
Recompensas usam inserção explícita nos slots principais, inclusive no criativo;
excedentes de recompensas podem ficar aos pés do jogador, seguindo a regra anterior.

### Marcações dos villagers

As marcações são individuais para cada jogador e aparecem acima do nome:
- **! amarelo:** há um contrato disponível para aceitar.
- **? amarelo:** é seu contratante e há objetivos/entregas pendentes.
- **! verde:** objetivos cumpridos, aguardando confirmar a conclusão.

Outros villagers não oferecem novos contratos enquanto houver um ativo.
As marcações respeitam campo de visão, distância e paredes, como as etiquetas.
Atualizam ao aceitar, entregar, avançar, abandonar, concluir e voltar a carregar
um villager. Villagers crianças não recebem marcação.

### Interface e elites

Foram lidos os prints **2026-10-10_03.17.14.png** e **2026-10-10_03.17.21.png**.
O fundo do menu ampliava um recorte com parte de um slot de armadura, produzindo
retângulos escuros. O centro passou a usar uma área limpa da textura nativa;
a correção se aplica a todas as janelas com a moldura compartilhada.

Elites têm redução gradual de velocidade conforme a escala:
**2×: -20%; 3×: -35%; 4×: -50%**, com interpolação entre os valores.
Aplica-se aos atributos nativos de movimento e voo quando presentes. Mobs cuja
IA usa deslocamentos especiais ou teleporte continuam seguindo essas regras próprias.
O modificador persiste e é acrescentado também a elites anteriores ao recarregar,
sem sortear novamente escala, nível, vida, dano ou loot.

Compilação assemble aprovada. Não foram executados testes automatizados nem
aberto o Minecraft. Equipamentos no criativo/sobrevivência, entregas parciais,
marcação/comemoração, aparência da UI e velocidade ficam para validação do usuário.

## Kits evolutivos e habilidades dos elites — 0.11.0

### Três etapas por classe

| Etapa | Requisito |
|---|---|
| Básica | Escolher a classe; nível 1 e Arsenal 1 |
| Intermediária | Nível de classe 20 e Arsenal 8 |
| Avançada | Nível de classe 50 e Arsenal 16 |

Os equipamentos são variantes próprias de itens vanilla, com nomes, descrição,
modelos nativos, identificação do dono por UUID, classe e etapa. Não exigem outros
mods. Armas/artefatos exclusivos não podem ser usados por outro dono ou classe.
O arco básico do kit é liberado imediatamente para seu Arqueiro, mesmo antes de
Arsenal 4. Arcos normais continuam seguindo o requisito habitual.

Ao escolher a classe recebe a arma/artefato e provisões. Jogadores com classe já
escolhida recebem uma vez o kit da maior etapa para a qual estão qualificados.
A arma principal é indestrutível e resistente a fogo/lava para permitir acompanhar
a progressão desde o começo. Ela ainda pode ser perdida, descartada ou largada
na morte pelas regras normais do mundo; indestrutível não significa recuperação automática.
O equipamento defensivo avançado tem durabilidade normal e pode ser reparado.

A evolução modifica o item existente, preservando a quantidade, os componentes
adicionais e encantamentos do jogador. Encantamentos do kit são acrescentados sem
rebaixar encantamentos superiores. A fração de desgaste é preservada quando há
mudança de material. Equipamentos guardados em baús evoluem ao retornar ao inventário.
Não evolui armas comuns ou itens de outros mods.

Bônus funcionam com a arma/artefato na mão principal/secundária, ou com a peça
avançada vestida no slot correto. Apenas segurar uma armadura não concede seus bônus.
Itens evoluídos não são rebaixados nem apagados ao resetar: ficam bloqueados até
recuperar classe, nível e Arsenal exigidos. O reset não dá provisões novas.

### Equipamentos e efeitos

| Classe | Arma/artefato | Básico | Intermediário | Avançado e equipamento extra |
|---|---|---|---|---|
| Guerreiro | Espada do Gladiador | Madeira; +5% dano | Ferro, Afiação II; +10% dano e +0,5 bloco ao Corte Circular principal | Diamante, Afiação IV/Pilhagem II; +15% dano, +1 bloco ao Corte; Peitoral do Gladiador de diamante |
| Arqueiro | Arco do Caçador | +5% dano de flechas | Força II/Impacto I; +10% dano | Força IV/Impacto I/Infinidade I; +15% dano; Botas do Vento de diamante, Peso Pena IV e queda lenta ao cair |
| Mago | Cajado Glacial | Desconto base de 5% de mana | Desconto de 10%; +1 s ao frio/paralisia ou lentidão inicial do gelo | Desconto de 20%; +2 s ao gelo; Capuz Glacial com +20 de mana máxima |
| Necromante | Grimório dos Mortos | +10% vida das invocações | +20% vida e +5% dano das invocações | +30% vida e +10% dano; Coroa dos Mortos |
| Xamã | Totem da Tempestade | +5% aos raios e cura | +10% aos raios e cura | +15% aos raios e cura; Manto da Tempestade mantém esses bônus com mãos vazias e reduz mana em 10% |

As peças avançadas têm Proteção II e Durabilidade III. Capuz e coroa são capacetes
de couro tingidos; o manto é um peitoral de couro verde. Bônus da peça mantêm o
nível avançado do kit ativo mesmo com a arma guardada.
O Totem da Tempestade usa o modelo nativo do totem sobre um artefato próprio:
**não ressuscita**. Use-o na mão secundária para continuar atacando com os punhos.
Especialista ainda exige ambas as mãos vazias; o manto permite seu bônus nessa situação.

Mana usa números inteiros: descontos arredondam o custo para baixo, com mínimo
1 MP para habilidades pagas. Portanto custos pequenos podem economizar mais que
a porcentagem nominal. A interface informa o custo efetivo. A cura/raio do Xamã
e a vida/dano das invocações somam-se às melhorias existentes na árvore.
A vida adicional de uma invocação é definida ao nascer; o bônus de dano acompanha
o equipamento ativo do dono. Não altera a regra de um acerto por phantom.

### Flechas, comida e entrega

| Etapa | Comida de cada classe | Flechas adicionais do Arqueiro |
|---|---|---|
| Básica | 8 pães | 32 normais |
| Intermediária | 12 bifes; Xamã recebe 12 batatas assadas | 48 espectrais |
| Avançada | 4 maçãs douradas e 8 cenouras douradas | 32 flechas de Lentidão I por 5 s e 1 flecha normal para Infinidade |

Restos de comida/flechas marcados como provisões do próprio kit evoluem junto
com a etapa, sem aumentar a quantidade. Alimentos e flechas comuns do inventário
não são trocados. A comida do Xamã respeita sua dieta vegetariana.

**Infinidade funciona com flechas normais pela regra vanilla.** Flechas espectrais
e de poção continuam consumíveis. Para priorizar a flecha normal de Infinidade,
segure-a na mão secundária; troque a munição quando desejar os efeitos especiais.

Provisões são entregues apenas uma vez por etapa/classe neste mundo. Escolher a
mesma classe após reset, morrer, relogar, abrir menus ou repetir /rpg kit não
renova comida, flechas nem equipamentos. Existe um registro persistente e uma
fila de entregas no PlayerPersisted/oxyrpg_kits do jogador, salvos junto do inventário
nos dados vanilla do mundo; esse registro acompanha o jogador ao morrer.
Se não houver espaço, o restante aguarda na fila e é entregue quando liberar
slots principais. A fila não descarta itens no criativo nem usa a mão secundária.
**/rpg kit** mostra a etapa e tenta entregar os pendentes; não cria reposições de
armas que foram perdidas nem repete provisões. Debug de skills não aumenta o nível
real, Arsenal ou etapa do kit.

### Explosão do creeper elite

O raio vanilla original é multiplicado pela escala física, arredondado para
um número inteiro de blocos. Com raio original 3 e escala 2–4, fica **6–12 blocos**.
Creepers carregados mantêm também o multiplicador vanilla de carga elétrica.
O raio original é registrado para não multiplicar novamente ao recarregar.

É uma explosão real: afeta entidades e, conforme **mobGriefing** e as regras
normais do Minecraft, também os blocos. Mantém o buff de dano já existente do elite.
Creepers elites anteriores recebem o ajuste quando carregados.

### Habilidades por tipo de elite

O título laranja do elite indica seu arquétipo. Habilidades novas têm uma
preparação de aproximadamente 1 s com partículas e intervalo de 9–14 s.
Exigem alvo válido, alcance e linha de visão; quebrar a linha de visão interrompe
a preparação. Ataques e IA vanilla continuam ativos. Creepers mantêm sua própria
preparação/explosão vanilla em vez desse temporizador extra.

| Tipo | Arquétipo | Habilidade |
|---|---|---|
| Creeper | Demolidor | Explosão proporcional ao tamanho |
| Zumbi, husk, afogado e aldeão zumbi | Esmagador | Onda de choque, dano em área, empurrão e lentidão |
| Esqueleto, stray e bogged | Atirador Glacial | Rajada de três flechas espectrais; acertos aplicam lentidão |
| Esqueleto Wither | Ceifador | Ataque em área que aplica Wither |
| Aranha e aranha das cavernas | Caçador Venenoso | Salto, ataque próximo, veneno e lentidão |
| Enderman | Distorcedor | Cegueira e puxão de alvos próximos, sem teleporte através de paredes |
| Bruxa | Alquimista | Aura de veneno e fraqueza |
| Blaze e cubo de magma | Incendiário | Pulso de fogo e queimadura em área; não coloca fogo no terreno |
| Slime | Instável | Salto, onda de impacto, empurrão e lentidão |
| Phantom | Grito Sombrio | Cegueira e fraqueza ao redor |
| Pillager | Balestrista | Rajada de três flechas com perfuração |
| Evocador | Invocador | Até dois reforços silverfish temporários, além da IA vanilla |
| Vindicator, piglins, hoglin, zoglin e piglin zumbi | Berserker | Força e Resistência temporárias, mantendo a redução normal de velocidade |
| Guardião e guardião ancião | Maré Sombria | Puxão e lentidão forte |
| Breeze | Vendaval | Rajada de empurrão em área |
| Warden | Sentinela | Recupera 5% da vida máxima e ganha Resistência temporária |
| Outros hostis elegíveis | Arquétipo sorteado | Sentinela, Tempestade (raio direcionado) ou Predador (salto e lentidão) |

Reforços do Invocador duram até 12 s, não viram elites e dão metade do XP RPG
normal quando derrotados. As flechas extras não podem ser recolhidas e expiram
em até 6 s. Os efeitos de área respeitam linha de visão e não atingem outros
hostis indiscriminadamente. Também podem atingir invocações aliadas atacadas pelo elite.
Animais, criaturas aliadas e bosses Wither/Dragão continuam excluídos da seleção
natural de elites. Mobs neutros não ganham um alvo novo só por terem uma habilidade;
precisam de um alvo da própria IA para ativá-la.

Para sua validação manual com cheats/operador nível 2:
**/rpg elite [escala]** transforma o hostil da mira a até 8 blocos; escala entre
2 e 4, padrão 3. Elites existentes preservam sua escala. Não executei esse comando.

Compilação assemble aprovada. Não foram executados testes, GameTests, comandos
no mundo nem aberto o Minecraft. Ainda precisa validar kits iniciais/evolução,
fila com inventário cheio, persistência, bônus das classes, Infinidade/munição,
queda lenta, habilidades dos elites e o raio do creeper na sua sessão.

## Atualização 0.12.0 — câmera sobre o ombro e profissões

A câmera traseira em terceira pessoa (F5) agora fica sobre o ombro direito,
com distância de aproximadamente 2,8 blocos, deslocamento lateral de 0,65 e
altura extra de 0,18. Ao usar/mirar um item, a distância vai para 2,2 blocos e
 o balanço diminui. A entrada no modo e a troca de ombro têm suavização.
Uma varredura de oito pontos ao longo do deslocamento inteiro aproxima a
câmera ao encontrar blocos, inclusive paredes ao lado do ombro. Não muda blocos.
Primeira pessoa, câmera frontal, sono e montarias mantêm o comportamento vanilla.

**R → Câmera** ou **O** abre opções para ligar/desligar, trocar ombro e escolher
balanço desligado/leve/moderado. **J** troca o ombro durante o jogo. O e J podem
ser remapeados nos controles do Minecraft. O balanço respeita também a opção
vanilla de movimentação da visão; desligá-la remove o balanço do Oxy RPG.
As preferências são locais em `config/oxyrpg-camera.properties` da instância.

O centro da imagem converge para o ponto da mira original do jogador, com
sprite nativo de mira e indicador de recarga de ataque na câmera sobre o ombro.
O deslocamento e o balanço são visuais: não mudam rotação do jogador, direção
autoritativa das skills/projéteis, alcance ou validações do servidor. A seleção
vanilla tem prioridade; além de seu alcance, um raio visual de até 64 blocos
localiza blocos/criaturas para convergência. Flechas conservam a queda normal.
Ainda é necessário você validar precisão/conforto, uso de blocos, arcos/skills,
paredes, troca de perspectiva e coexistência com possíveis mods de câmera.

A curva de **Mineração, Agricultura e Arsenal** fica mais rápida no começo:

| Nível atual | XP para o próximo nível |
|---|---|
| 1–5 | 50 × nível (50, 100, 150, 200, 250) |
| 6–9 | 75 × nível (450, 525, 600, 675) |
| 10–19 | 100 × nível, como antes |
| 20 | Máximo |

As três profissões usam a mesma função no servidor e nas barras da interface.
O total para o nível máximo passa de 19.000 para **17.500 XP**. Ganhos por ação,
limites de equipamentos e bônus passivos continuam iguais. O XP acumulado dos
registros existentes é preservado; a nova curva pode aumentar o nível ao carregar.
Nenhum arquivo de mundo foi editado diretamente durante a instalação.

Compilação por assemble, sem executar testes automatizados ou abrir o jogo.
A validação dentro do Minecraft fica com o usuário, conforme solicitado.

## Atualização 0.13.0 — câmera estável, posição ajustável e trava de mira

A câmera agora mantém a orientação do jogador, sem recalcular rotação ao alternar
entre alvos próximos/distantes. A mira usa a projeção do ponto do raio do jogador
na tela; pode aparecer fora do centro na visão sobre o ombro. Ataques e projéteis
continuam partindo do jogador e respeitando alcance, obstáculos e queda vanilla.

O balanço acompanha a distância caminhada e a animação nativa de passos, em vez
de uma oscilação que continua com o jogador parado. Ao sofrer dano, permanece a
reação nativa do Minecraft, respeitando os ajustes de acessibilidade do jogo.
O balanço pode ser desligado, leve ou moderado; ao mirar, fica reduzido.

Distância, lateral, altura e aproximação ao usar arco/besta/tridente/luneta são
suavizadas. Também há transição na volta à primeira pessoa e passagem pela câmera
frontal do F5. As mãos em primeira pessoa aguardam a câmera aproximar-se do olho.
A colisão usa blocos sólidos e uma margem para a câmera, retraindo imediatamente
por segurança e recuperando a distância gradualmente com tolerância a ruído.
O próprio modelo do jogador é ocultado quando a câmera fica muito próxima,
com dois limiares para evitar que sua cabeça obstrua a visão ou pisque.

**O** ou **R → Câmera** abre os botões de distância, lateral e altura (+/− 0,10),
além de ombro, intensidade e restaurar padrões. No teclado numérico:

- **+/−:** afastar/aproximar;
- **4/6:** diminuir/aumentar o deslocamento lateral;
- **2/8:** baixar/subir.

Distância: 1,20–6,00 blocos; lateral: 0–1,60; altura: −0,50–1,50.
Preferências locais persistem em `config/oxyrpg-camera.properties`, aceitando
os campos antigos e adicionando `distance`, `lateral` e `height`. Não foi copiada
a configuração da arena de testes para a instância do usuário.

### Trava de mira

**Z** trava/destrava a mira, e **N** troca entre criaturas candidatas na tela.
Escolhe uma criatura viva, visível e a até 24 blocos, priorizando proximidade da
mira. Acompanha o alvo com rotação suavizada e usa os pacotes vanilla do jogador.
Quando ativo, o movimento do mouse deixa de girar a visão; Z libera novamente.
O jogador pode continuar se movendo e deve atacar/usar habilidades manualmente.
Não há ataque automático nem alteração de mana, recargas, alcance ou permissões.

A trava pode escolher hostis e animais, mas exclui invocações aliadas, animais
domesticados e villagers. Solta o alvo ao morrer, ficar invisível, sair do alcance,
ficar encoberto por blocos ou mudar de mundo; também é cancelada ao morrer,
dormir, montar ou entrar na câmera frontal. Menus suspendem o acompanhamento.
Todas as teclas são remapeáveis nos controles. Z evita o conflito de L com as
conquistas vanilla, identificado e corrigido durante a validação.

### Validação desta atualização

Nesta alteração o usuário pediu expressamente testes. Foram executados `build`,
17 testes JUnit e 17 verificações adicionais de progressão/persistência.
A sequência de cliente de desenvolvimento usa o mesmo Minecraft/NeoForge/JDK,
com mundo **Oxy Camera QA** em `run-camera-qa`, separado da instância SK Launcher.
O modo de integração exige `-PcameraQA`, propriedade JVM específica, pasta
exclusiva e nome de mundo exato; não executa na instância normal.

A sequência cobre entrada, saída, câmera frontal, caminhada, ajustes, uso real
 de arco, dano recebido, aquisição/troca de alvo, perda por parede e colisão/
recuperação. Na repetição aprovada: 374 amostras, 29 durante colisão, zero falhas
numéricas/colisões com o volume da câmera dentro de blocos; todos os marcos
(entrada/saída/trava/troca/oclusão/arco/dano) confirmados. Também foi verificada
a interface de ajustes e alteração persistida pelo teclado numérico.
O relatório entregue detalha a validação e seus limites; conforto visual em
sessões longas e compatibilidade com outros mods de câmera não foram medidos.

## Atualização 0.14.0 — trava de mira, câmera livre e HUD dinâmica

A câmera sobre o ombro usa as posições interpoladas do jogador e do alvo em cada
frame durante a trava. A órbita acompanha o movimento lateral, a orientação visual
olha do ponto da câmera para o alvo e o balanço de caminhada é reduzido até zero.
O marcador identifica a posição do alvo selecionado, mesmo se outra criatura
interceptar a mira. A rotação do jogador continua sendo enviada pelos pacotes
vanilla: não há ataque automático nem alteração de alcance ou dano.

Os três eixos aceitam valores negativos: distância de **−6 a +6**, lateral e altura
 de **−3 a +3** blocos. Distância negativa coloca a câmera à frente do jogador;
lateral negativa inverte o lado relativo ao ombro selecionado. Zero é permitido.
A colisão continua ativa em todo o deslocamento. Preferências anteriores são
preservadas em `config/oxyrpg-camera.properties`.

**H** alterna Sempre visível → Dinâmica → Oculta. **R → HUD** permite escolher o
modo e habilitar/desabilitar cada parte. O padrão novo é Dinâmica. As opções ficam
em `config/oxyrpg-hud.properties`, sem modificar mundos ou perfis RPG.

No modo dinâmico, cada parte tem seu próprio temporizador: aparece por quatro
segundos após atividade e esmaece durante 1,2 segundo. Ao entrar no mundo ou mudar
o modo, os indicadores aparecem brevemente para orientação.

- Vida: dano, cura ou mudança de absorção; permanece com vida a 25% ou menos.
- Armadura: dano ou mudança da quantidade de armadura.
- Fome: alteração de alimento; permanece com seis pontos de alimento ou menos.
- Ar: falta de ar ou submersão; permanece enquanto necessário.
- Hotbar, ícones e contagens: troca de slot, mudança nos nove slots/offhand,
  ataque ou uso de item. Movimento sozinho não a mantém visível.
- XP vanilla: mudança de XP/nível; também mostra a barra ao carregar salto montado.
- Efeitos: permanecem enquanto houver efeitos ativos.
- Vida da montaria: mudança da vida da montaria ou dano ao jogador montado.
- Nível/XP RPG e atributos: mudanças de progressão ou pontos disponíveis.
- Missões: avanço de caçadas, objetivos ou estágio de missão.
- Mana: gasto e regeneração; cada habilidade: uso, efeito ativo ou recarga,
  com temporizadores separados entre as três habilidades.

Sempre visível respeita os indicadores desabilitados nas opções. Oculta também
esconde os indicadores RPG, a mira e as etiquetas do mod; mensagens e menus
continuam acessíveis. F1 mantém seu comportamento vanilla.

Todos os **17 atalhos** estão em **Opções → Controles → Teclas → Oxy RPG**.
As teclas antigas são preservadas. “Abrir opções de HUD” e “Mostrar HUD enquanto
pressionada” começam sem tecla, para o jogador escolher sem criar conflitos.
O segundo revela temporariamente as partes habilitadas no modo Dinâmico.
Novos atalhos devem usar a fábrica `key()` para registro automático, com traduções
nos dois idiomas; essa regra está em AGENTS.md.

Os dois prints locais foram inspecionados. Eles mostram a composição da câmera,
mas não comprovam o movimento entre frames. A correção de movimento foi baseada
na revisão do código. A validação de conforto, enquadramento, transparência e
remapeamento dentro do jogo permanece manual; não foram abertos clientes de
validação nem executados testes automatizados nesta atualização.
Compilação por build-windows.bat (assemble) concluída com sucesso. As assinaturas
dos métodos de GUI usados pelos mixins foram conferidas no bytecode local de
Minecraft/NeoForge. O JAR 0.14.0 foi instalado com Minecraft fechado e SHA-256
confirmado; o JAR 0.13.0 foi preservado em mod-backups fora de mods.

## Atualização 0.15.0 — primeira pessoa suave e movimento independente

A terceira pessoa da 0.14.0 foi preservada. Em primeira pessoa, LocalPlayer retorna
a rotação atual sem interpolar; a trava atualizava essa rotação somente nos ticks.
A câmera agora calcula sua orientação pelos pontos interpolados a cada frame,
com amortecimento angular por tempo decorrido. O balanço vanilla fica suspenso
enquanto a mira estiver travada, evitando oscilações da projeção ao andar.
Não há filtro de motion blur adicionado pelo mod. Os prints foram inspecionados,
mas o relato do usuário foi necessário para localizar a diferença de perspectiva.

A HUD tem entrada e saída graduais, inclusive ao voltar do modo Oculta. Os tempos
de atividade continuam independentes por indicador. A hotbar aparece ao trocar
o slot selecionado ou o tipo de item em uma das mãos, e na apresentação inicial
do mundo/modo. Ataques, uso de habilidades, contagem de itens e desgaste da arma
não acionam sua exibição. O atalho de exibição temporária continua disponível.

A trava identifica o alvo também no pedido de habilidade. O servidor confere
existência, alcance de 24 blocos, direção aproximada da mira, visibilidade,
linha de visão e exclusão de villagers, pets domesticados e invocações aliadas.
A teia das aranhas do necromante e os projéteis de habilidades do mago/arqueiro
passam a lançar em direção à posição prevista do alvo, sem adicionar a velocidade
lateral do jogador. Para os projéteis com gravidade há compensação aproximada
inicial. Sem alvo válido, o lançamento anterior permanece. Não há perseguição
em voo nem acerto garantido: obstáculos, mudanças de direção e esquivas ainda
podem fazer o projétil errar. Dano, custo, recarga e colisões continuam os mesmos.
O protocolo de rede passou de 11 para 12; servidor e clientes devem usar 0.15.0.

**R → Câmera → Movimento: independente da mira** é uma opção nova, desligada por
padrão. Funciona em terceira pessoa traseira, tanto com a câmera sobre o ombro
quanto com a câmera traseira vanilla quando o ombro estiver desligado. WASD
continua relativo à câmera; o corpo do personagem gira suavemente para onde anda
e preserva sua orientação quando para, mesmo ao girar a câmera com o mouse.
Ao atacar ou mirar um item, ele volta a se orientar para a mira. A direção de
interação enviada ao servidor permanece a da mira; a mudança é na orientação
visual local do personagem. Primeira pessoa, montarias, sono e voo com elytra
mantêm a orientação vanilla. Configuração salva junto das opções da câmera.

“Alternar movimento independente da mira” é o 18º atalho na categoria Oxy RPG,
inicialmente sem tecla para o usuário escolher. Todos os atalhos antigos são
preservados. Não foram abertos clientes de validação nem executados testes;
conforto, animações, movimento e precisão continuam sujeitos à validação no jogo.
Assemble concluído com sucesso. Instalada a versão 0.15.0 com Minecraft fechado,
SHA-256 conferido e cópia anterior preservada em mod-backups. Validação no jogo
ainda não executada nesta versão.

## Atualização 0.16.0 — modo completo de testes e debug de Arsenal

Com cheats/operador nível 2:

- `/rpg debug arsenal`: Arsenal no nível 20 temporariamente, com liberação dos
  requisitos de armas/armaduras e bônus passivo. Outras profissões e skills ficam
  como estavam. `/rpg debug off` restaura os valores salvos.
- `/rpg teste` ou `/rpg teste on`: nível RPG 100, todas as 21 skills da classe,
  cada atributo no máximo de 99, Mineração/Agricultura/Arsenal no nível 20,
  habilidades com gasto de mana e recargas normais (desde 0.18.0); vida e fome preenchidas ao entrar.
- `/rpg teste guerreiro|mago|arqueiro|necromante|xama`: ativa o mesmo modo e troca
  livremente a classe temporária. Cada classe entra completa, sem resetar o perfil.
  Sem argumento, preserva a classe atual; sem classe escolhida, usa Guerreiro.
- `/rpg classe <classe>` também troca a classe temporária enquanto Teste está ativo.
- `/rpg teste off`: desliga os overrides e restaura classe, nível, skills,
  atributos e XP das profissões que já estavam salvos. `/rpg debug off` também sai.

Os overrides são exclusivamente de sessão e desaparecem ao sair do mundo/servidor.
O nível e a classe efetivos aparecem na HUD e em `/rpg status`; a tela de profissões
mostra os níveis de teste, e a de atributos mostra os valores máximos.
Os 99 pontos de cada atributo são uma sobreposição de debug; o limite normal de
99 pontos totais do perfil persistido não foi alterado. No modo Teste, XP RPG e
XP das profissões não são acumulados nos registros reais. Desbloquear ou alocar
pontos não grava alterações no perfil. Limpar a árvore exige voltar ao debug normal;
resetar a classe real exige sair do modo Teste antes.

Efeitos, invocações e buffs ativos são limpos ao mudar de classe, entrar ou sair.
Restrições próprias de cada classe continuam em vigor. O modo desbloqueia o uso
dos níveis de equipamento, sem conceder/evoluir kits automaticamente ou consumir
seus recibos. Inventário, blocos, drops e ações normais no mundo não são revertidos
quando o modo é desligado; apenas a progressão sobreposta é removida.

Compilação por assemble; testes automatizados e validação no jogo não executados,
conforme a preferência do usuário registrada em AGENTS.md.
Assemble aprovado. Versão 0.16.0 instalada com Minecraft fechado, SHA-256
confirmado e JAR 0.15.0 preservado fora de mods. Comandos ainda não executados
no jogo nesta versão.

### 0.17.0 — barras de câmera e mira em terceira pessoa

Distância, lateral e altura usam barras nativas do Minecraft, com valores negativos, ajuste imediato e suporte às setas do teclado. As posições são salvas ao soltar a barra ou sair da tela.

A mira usa o resultado de interação do próprio jogo, inclusive quando não há alvo no alcance. Removida a busca visual de inimigos a 64 blocos que não podiam ser atingidos. Mira e marcador de alvo usam a projeção do mundo, acompanhando FOV, inclinação da câmera, reação a dano e efeitos de portal. O alcance e as regras de ataque continuam sendo os do Minecraft, incluindo dano de varredura da espada.

Validação desta versão: compilação e empacotamento; confirmação visual e de combate a cargo do usuário.

### 0.18.0 — direção das habilidades, alvos e recursos de teste

- Projéteis de habilidades seguem o ponto da mira enviado pelo cliente e validado pelo servidor, com ou sem alvo travado. Isso inclui as poções do xamã; retirado o desvio lateral causado pela velocidade do jogador. Colisões e gravidade continuam em vigor.
- Efeitos ofensivos do mago, guerreiro, arqueiro e raio do xamã também atingem mobs pacíficos. Invocações próprias continuam excluídas. Animais invocados pelo xamã podem atacar o mob pacífico que o jogador marcar; a busca automática continua priorizando hostis.
- Tremor desgasta até quatro blocos naturais em direções separadas ao redor do epicentro, respeitando o raio, a superfície e as permissões existentes.
- `/rpg teste` libera a progressão completa, mas respeita mana, regeneração e recargas. Mana de teste e recargas por classe são temporárias, preservadas durante trocas de classe e separadas dos registros reais. Trocar de classe não preenche novamente vida/fome. `/rpg debug on` continua ilimitado em mana/recargas.
- HUD distingue TESTE de DEBUG e mostra mana atual/máxima em números. O gasto é sincronizado imediatamente; mana insuficiente impede a habilidade no servidor.

Compilação por assemble. Validação de mira, efeitos e recursos no jogo permanece a cargo do usuário, sem testes automatizados ou abertura do jogo nesta atualização.

Assemble concluído e 0.18.0 instalado com Minecraft fechado. Hash SHA-256 conferido; JAR anterior preservado em mod-backups. Validação no jogo ainda não realizada.

### 0.18.1 — nível dos inimigos no modo de testes

O jogador em /rpg teste já possui nível RPG 100 temporário. Novos inimigos que usem esse jogador como referência agora consideram esse nível máximo, mantendo a variação normal de 98 a 100. Ao sair do modo teste, novos spawns voltam a usar o nível real salvo. Mobs existentes preservam o nível atribuído no nascimento. Mana e recargas continuam normais; nenhum nível real do jogador é sobrescrito.

### 0.19.0 — feedback, previsão, comparação e treinamento

**Feedback:** os acertos causados pelo jogador e por suas invocações recebem números flutuantes brancos; críticos reais de ataques vanilla/flechas aparecem em dourado, maiores, com confirmação na mira. O valor vem de LivingDamageEvent.Post no servidor, após as reduções. Bloqueios reportados pelo evento usam azul. Números sobem e esmaecem; ataques rápidos no mesmo alvo podem ser agrupados sem misturar normal/crítico. O limite de 96 rótulos evita acumulação sem limite. Números e contornos respeitam obstáculos, F1 e troca de mundo.

**Configurações:** R → HUD → Combate. Pode desligar números, confirmação na mira e som separadamente; alternar agrupamento, ajustar tamanho de 60% a 160% e escolher segurar/soltar ou lançamento imediato. Opções persistem em config/oxyrpg-combat.properties. Todos os atalhos continuam na categoria Oxy RPG dos controles.

**Previsão de habilidades:** por padrão, segure a tecla configurada da habilidade para ver o contorno e solte para lançar. Abrir um menu, morrer, trocar de classe ou de mundo cancela a previsão. Há círculos para áreas, cone para Lança-chamas e linhas para projéteis/dash, aderentes ao terreno; as linhas são desenhadas sem partículas. Verde indica disponibilidade, vermelho bloqueio/mana insuficiente e cinza recarga. A previsão não causa dano, não gasta mana nem altera os alcances do servidor. Linhas de projéteis indicam direção até 24 blocos e não garantem um impacto: gravidade, colisões, movimento do alvo e assistência do lock continuam em vigor.

**Arsenal:** R → Profissões e contratos → Comparar armas. Escolha uma arma do inventário para comparar com a mão principal: dano corpo a corpo, ataques por segundo e alcance de interação, com diferenças verdes/vermelhas. Valores base são separados dos atributos/passivo de Arsenal e etapa do kit ativo. Os bônus próprios do item aparecem na descrição/tooltip; bloqueios identificam dono, classe, profissão e etapa. Para armas de distância, dano de projétil depende da carga e dos encantamentos; o número de dano melee não é uma estimativa do disparo. A tela apenas compara, sem equipar ou transferir itens.

**Treinamento:** R → Diário de missões → Treinamento da classe. Três lições por classe, relacionadas aos três ramos. Cada lição oferece 50, 100 ou 150 XP de Arsenal; conclusão e recompensa são gravadas juntas, atomicamente, no registro de profissões do mundo/UUID. Classes têm progresso separado, preservado ao resetar a classe. Perfis antigos começam com zero lições sem perder profissões/contratos. Teste, debug, criativo e espectador não acumulam lições/recompensas. Caçadas e contratos existentes continuam disponíveis.

Compilação por assemble; testes automatizados e abertura do jogo não executados, conforme AGENTS.md. Precisão visual dos contornos, conforto dos números e conclusão das lições devem ser validados pelo usuário no jogo.

Assemble concluído. Versão 0.19.0 instalada com Minecraft fechado, hash conferido e versão 0.18.1 preservada fora de mods. Nenhum cliente de validação ou teste automatizado foi executado.

### 0.19.1 — comparação ao segurar sobre o item

No inventário, baú e demais telas com tooltip de item, passe o mouse sobre uma arma/ferramenta, armadura ou escudo e segure **Shift esquerdo**. Dois tooltips aparecem lado a lado: EQUIPADO e DESTACADO. Armas/ferramentas usam a mão principal, armaduras o slot correspondente e escudos a mão secundária. Slot vazio é identificado; diferenças de atributos usam verde/vermelho/cinza. Armaduras comparam armadura, resistência da armadura e resistência a empurrão; armas comparam dano corpo a corpo, ataques/s e alcance. A durabilidade e os requisitos também são apresentados. Dano de projétil e encantamentos situacionais continuam descritos separadamente, sem estimar um disparo.

O atalho **Comparar equipamento (segurar no tooltip)** pode ser alterado em Opções → Controles → Teclas → Oxy RPG, inclusive para um botão do mouse. A implementação usa o código da tecla configurada e contexto GUI; todos os atalhos continuam registrados pela fábrica key(). O tooltip normal mostra a dica da tecla atual. Ao soltar, volta à descrição normal. Tooltips longos são ajustados à tela e podem ser abreviados; solte para consultar a descrição inteira. A tela Comparar armas do Arsenal continua disponível e compartilha os mesmos cálculos/requisitos. A comparação não transfere, equipa nem consome itens.

Assemble concluído; testes e abertura do jogo não realizados, conforme AGENTS.md. Validação visual dos tooltips e do atalho permanece a cargo do usuário.

Versão 0.19.1 instalada após o usuário fechar o Minecraft. Hash conferido e 0.19.0 preservada fora de mods. Validação visual ainda não realizada.

## Aviário e comparação — 0.20.0

- **G — Decolagem:** lança para cima e inicia voo usando a física da elytra, sem equipá-la.
  Voo inicial: 10 s; principal: base de 25 s, ampliada pelas melhorias. Reativar G custa
  base de 5 MP e impulsiona na direção do voo por 2 s (inicial) ou 4 s (principal),
  limitado ao tempo restante de voo. Impulsos podem ser repetidos ao terminar o anterior.
  Decolagem custa base de 20 MP. Pousar, entrar na água, morrer ou trocar de classe encerra.
- **Peso pena — passiva:** redução de queda nas etapas iniciais; a principal elimina
  dano de queda. Agachar durante a descida interrompe o planeio e dá queda lenta e
  redução de dano de inimigos: 25% na principal, até 45% nas melhorias específicas.
  Não precisa pressionar V e não gasta mana.
- **B — Impacto Meteoro:** exige estar no ar e custa base de 30 MP. Arma um mergulho;
  ao pousar, causa dano em mobs, inclusive pacíficos. Altura e velocidade aumentam dano
  e raio, limitado a 7 blocos. A versão principal destrói até 96 blocos e arremessa parte
  como blocos em queda; preserva blocos com inventário, líquidos e blocos resistentes,
  e respeita permissões de construção e eventos de proteção. A versão inicial só causa dano.
- **Teste:** `/rpg teste aviario` libera a classe completa no nível 100 temporário.
  Mana e recargas continuam normais; `/rpg teste off` restaura o perfil real.
- **Comparação:** no inventário, mantenha o mouse sobre um equipamento e segure
  **Shift esquerdo** (remapeável em Controles > Oxy RPG). Mostra equipado e destacado
  lado a lado, com diferenças verdes/vermelhas. Corrigida a rejeição do Shift pelo
  contexto de modificadores da interface.
- Números de dano aparecem mais altos e alternam os lados do alvo em relação à câmera,
  com negrito, contorno e destaque inclinado para críticos.

Validação desta versão: compilação/empacotamento via `build-windows.bat` (assemble).
A validação no jogo fica com o usuário; não foram executados testes ou iniciado o Minecraft.

## Balanceamento e interface — 0.21.0

- Aviário recebe **70% do dano de queda normal** (10 vira 7); Peso pena continua
  dando queda lenta ao agachar e proteção contra inimigos durante a descida.
  O mergulho de Meteoro absorve a própria queda para executar o impacto.
- G em voo pode reaplicar impulso a cada 0,5 s. A velocidade desejada começa em
  3,3–3,8 blocos/tick e sobe nas reaplicações até 5,2, consumindo mana normalmente;
  o impulso continua limitado ao tempo restante de Decolagem.
- Meteoro usa altura e velocidade ao quadrado para dimensionar o impacto.
  Raio máximo: 14 blocos; profundidade: até 7; até 1.780 blocos no impacto máximo,
  com teto de 160 detritos. Trabalha em etapas com no máximo 120 alterações e
  12 novos blocos em queda por tick no servidor. Inventários, líquidos, blocos
  muito resistentes e proteção de área continuam respeitados.
- Para nível L, com n=L−1: vida ×(1+0,07n+0,0007n²) e dano
  ×(1+0,045n+0,00035n²). O multiplicador de dano também cobre projéteis e explosões.
  No nível 50: vida ×6,11 e dano ×4,05; no 100: vida ×14,79 e dano ×8,89.
  Mobs carregados recebem os modificadores novos preservando a proporção de vida.
- Elites: vida adicional ×8–11, dano ×2,5–3,5, menos penalidade de movimento e
  habilidades principais mais frequentes. Ganham uma identidade secundária fixa:
  Erupção, Carcereiro, Vampírico, Baluarte, Tempestade ou Predador. Abaixo de
  40% de vida: +25% de velocidade e dano, com menor recarga secundária.
  Ataques secundários de área avisam a posição no chão antes de acertar;
  sair do círculo evita a área. Esses ataques não destroem terreno.
- Drops adicionais passam a respeitar nível: cobre abaixo de 20; ouro a partir
  de 20; esmeralda a partir de 40; diamante a partir de 60; fragmento de netherita
  a partir de 85, raro. Nível e Sorte aumentam a chance; elites têm duas rolagens
  garantidas e uma recompensa adicional. Drops comuns ganham quantidade a cada
  40 níveis; equipamentos personalizados não são duplicados. Invocações excluídas.
- A árvore de classe agora forma uma constelação radial de três setores, com
  bifurcações e especializações externas. IDs, compras e pré-requisitos preservados.
  Tooltips das árvores têm largura limitada, quebras de linha e contraste maior.
- Comparação: cabeçalhos fortes, nomes em uma linha, estatísticas principais sem
  repetição e lista de encantamentos; solte Shift para o tooltip completo.
- Menu R reorganizado com progressão e habilidades em colunas. A escolha de classe
  fica em uma tela própria, com estilo, regras e descrição das três habilidades;
  o painel de detalhes aceita rolagem. A abertura automática em mundos novos
  permanece planejada para uma atualização futura.

Validação: assemble apenas; testes e abertura do jogo dependem de solicitação do usuário.

## Interface e referências — 0.22.0

O menu R é um painel de navegação compacto, sem botões de ativação de skills.
Há seis entradas: classe, árvore de habilidades, atributos, diário, trabalhos e arsenal.
As habilidades continuam nos atalhos configurados em Controles e no HUD de combate.

A seleção de classe usa seis ícones, identidade visual e cor por classe, ficha com
ícone ampliado, estilo de jogo, limitações e resumos curtos das habilidades. A ficha
aceita rolagem em janelas menores e oferece acesso à árvore antes de escolher.
O modo de teste continua permitindo troca de classe; a escolha normal mantém a regra existente.

As duas árvores usam medalhões circulares, com habilidades principais maiores,
cores por estado e painel com moldura comum às novas telas. O diário tem página
clara, objetivos e recompensa em blocos, abas de caçadas e contratos, e acesso ao
 treinamento de classe. Compras, profissões, objetivos e dados persistentes preservados.

Referências estudadas: RPG Origins, Skill Tree (RPG Series), Realm RPG: Quests & Rewards,
Dynamic RPG Resource Bars e Mouse Tweaks. O desenho desta interface é implementação
própria usando itens vanilla e primitivas de GUI; não incorpora código ou assets desses mods.

- RPG Origins: https://www.curseforge.com/minecraft/mc-mods/rpg-origins
- Skill Tree: https://www.curseforge.com/minecraft/mc-mods/skill-tree
- Realm RPG: https://www.curseforge.com/minecraft/mc-mods/realm-rpg-quests-rewards
- Dynamic Resource Bars: https://www.curseforge.com/minecraft/mc-mods/dynamic-rpg-resource-bars
- Mouse Tweaks: https://www.curseforge.com/minecraft/mc-mods/mouse-tweaks

HUD animado e gestos de inventário são referências para evolução futura; esta versão
foca navegação, escolha de classe, árvores e diário. Validação desta versão: assemble;
sem testes automatizados ou abertura do Minecraft, conforme preferência do usuário.

## 0.23.0 — telas e árvores por classe

Treinamento, profissões, arsenal e configurações usam a moldura escura e dourada do menu RPG. Treinamento e profissões apresentam cartões separados; o arsenal vazio explica como iniciar a comparação. Cada classe tem uma silhueta própria na árvore: lâmina e guardas, espiral arcana, leque do arco, cadeias do necromante, raízes do xamã e asas do aviário. IDs, dependências, custos e progresso existentes foram preservados.

## 0.24.0 — esquiva, parry e exploração

- Esquiva universal com Alt esquerdo (remapeável): WASD escolhe a direção; sem movimento, segue a mira. Movimento no chão, com colisões normais, sem teleporte e 0,3 s de proteção contra ataques. Queda, lava e outros danos ambientais continuam ativos.
- Barra inspirada na XP vanilla, em três segmentos acima da hotbar. Começa com uma carga; níveis 25 e 50 liberam a segunda e a terceira. Uma carga recarrega a cada 6 s, em sequência. Cargas parcialmente cheias não permitem uso. Modo teste tem gasto normal; debug é ilimitado. A energia normal é preservada ao sair e morrer.
- Parry exclusivo de escudos: bloqueio entre os ticks 5 e 10 após levantar o escudo, respeitando a direção e ataques que atravessam escudos. Evita desgaste nesse bloqueio, desequilibra o atacante e abre 1,5 s para um contra-ataque corpo a corpo crítico. Um parry por segundo.
- Acertos diretos do jogador: 5% de erro e 10% de crítico base (dano ×1,5). Sorte reduz erro em 0,1 ponto percentual por ponto, até 1%; aumenta crítico em 0,25 ponto percentual, até 35%. Críticos vanilla são preservados e não recebem multiplicador duplicado. Sangramento, frio periódico e retaliação não fazem novas rolagens. Erros exibem “Errou” em azul; críticos exibem o dano seguido de “Crítico”. Decisão e dano vêm do servidor.
- Minimapa simples de superfície com norte, direção do jogador e coordenadas; M alterna visibilidade. Amostra somente chunks carregados, em lotes limitados. Não é um mapa de cavernas e não revela terreno pelo servidor. Esquiva e minimapa podem ser ocultados nas opções da HUD.
- Vilas: espaçamento 34 → 26 chunks, mantendo separação 8, tipos de vila e biomas vanilla. A frequência real depende dos biomas. Aplica-se a terreno gerado após a atualização; áreas existentes são preservadas.
- Baús vanilla de vilas retêm cada item com probabilidade de 60% (redução média de 40%). Atua na geração de loot ainda não aberto, sem alterar itens já obtidos, trocas ou recompensas de missões. Arquivos de worldgen e loot podem ser substituídos por datapacks.
- Descrições de atributos e skills mostram benefícios e requisitos sem explicar detalhes internos da distribuição.

Compilação: `build-windows.bat` (assemble). A validação dentro do jogo é feita pelo usuário.

## 0.25.0 — integrações nativas do NeoForge

Configurações de balanceamento no menu R → Ajustes, classes e missões por datapack, Data Maps de criaturas/armas, perfil real em Data Attachment com importação dos arquivos antigos, gerador de dados, seções de profiler e quatro GameTests. GeckoLib 5.3-alpha-3 adiciona o selo RPG animado (receita ou `/rpg selo`). O balanceamento padrão da 0.24.0 é mantido.

Consulte [o guia de integrações](docs/INTEGRACOES_NEOFORGE.md) para arquivos, limites de customização, migração, comandos e execução opcional dos testes.

## 0.26.0 — novas classes, balanceamento e equipamentos vinculados

Ilusionista, Hidromante e Piromaníaco têm árvores de 51 nós e mecanismos próprios: enxame intangível, controle aquático, núcleos de magma, explosões encadeadas e calor. Voo do Aviário, cura do Guerreiro, bônus do Arqueiro e invocações do Necromante agora crescem de forma mais gradual. Todas as classes têm limitações explícitas na seleção.

Equipamentos do kit são recuperados após morrer, com recarga de 60 s. Cada item principal tem um efeito acionado pela tecla C, configurável. Consulte [o guia da versão](docs/CLASSES_026.md) para detalhes e controles.
