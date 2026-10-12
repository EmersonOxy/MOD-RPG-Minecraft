# Catálogo de classes — Oxy RPG 0.50.0

Estado implementado em 12/10/2026. Minecraft 1.21.10, NeoForge 21.10.64; cliente e servidor usam protocolo 33. Compilação não substitui validação em jogo.

## Como funciona a progressão

São 12 classes, 3 habilidades por classe e 51 nós por árvore. Os nós 0 e 1 são gratuitos; os outros 49 custam um ponto. O jogador recebe 1 ponto a cada 2 níveis. Habilidades começam em versões iniciais: ramo 1 no nível 1, ramo 2 no nível 10 e ramo 3 no nível 15. As habilidades principais exigem nível 10/20/30 e os caminhos de preparação. As tabelas abaixo registram também cada pré-requisito; nível sozinho não libera uma habilidade.

No nível 100 existem 50 pontos de habilidade, suficientes para todos os 49 nós pagos. Atualmente a decisão é a ordem de construção da classe, não uma especialização exclusiva no fim da progressão. A árvore de atributos é separada e dá 1 ponto por nível a partir do nível 2; os trabalhos continuam independentes.

Valores apresentados são os padrões: dano, mana, duração e recarga podem mudar por atributos, equipamento, config do servidor e datapack. Os 30 nós avançados por classe acrescentam potência (+3% cada), duração (+5%), eficiência de mana (-3%) e redução de recarga (-3%). Preparação e Domínio também acrescentam +3% de dano. Tudo comprado representa +36% de dano da classe, +30% de duração e -18% de mana/recarga, antes dos demais modificadores.

Atalhos padrão: R abre o menu, G/V/B ativam habilidades, Z marca/trava uma criatura, Alt executa esquiva. Todos podem ser personalizados nos Controles do jogo. Peso pena é passiva e usa Agachar. A esquiva geral tem 1 a 3 cargas conforme nível; deslocamento máximo de 3 blocos. A Execução do Assassino é uma habilidade distinta, com alcance próprio de até 6,5 blocos.

## Visão geral

| Classe | Habilidade 1 | Habilidade 2 | Habilidade 3 |
|---|---|---|---|
| Guerreiro | Corte circular | Vampirismo | Bastião |
| Mago | Bola de gelo | Lança-chamas | Tremor |
| Assassino | Adaga espectral | Furtividade | Execução furtiva |
| Arqueiro | Rajada debilitante | Chuva de flechas | Caçada implacável |
| Necromante | Aracnídeo | Rei dos Mortos | Céu Noturno |
| Xamã | Deus Relâmpago | Doutor Animal | Especialista |
| Aviário | Decolagem | Peso pena | Impacto Meteoro |
| Ilusionista | Mímico | Leviosá | Corpo de Eclipse |
| Hidromante | Águaman | Canhão d’água | Sobre-água |
| Piromaníaco | Carnaval de Pólvora | Coração de Caldeira | Fênix de Cinzas |
| Vampiresco | Sede de Sangue | Metamorfose | Twilight |
| Deus do Trovão | Descida do trovão | Corrente de raios | Avatar do trovão |

## Classes, passivas, fraquezas e melhorias

### Guerreiro

**Passiva e talentos automáticos:** Corpo a corpo +5%. Talentos compráveis acrescentam vida, roubo de vida com intervalo e resistência a empurrão.

**Pontos negativos atuais:** Não usa arco, besta ou tridente; fome adicional. Bastião também desacelera.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Corte circular | dano em raio de 3 blocos e Força I por 10 s. Recarga 25 s. | 12 MP |
| Vampirismo | por 12 s cura 25% do dano. Fome drena em cerca de 6 s; acertos recuperam fome. Recarga 40 s. | 20 MP |
| Bastião | brilho, Resistência III, Lentidão I, espinhos e reflexão frontal por 8 s. Recarga 45 s. | 25 MP |

**Sugestão, ainda não implementada:** Medir a cura por segundo em grupos e limitar o quanto múltiplos alvos sustentam o Vampirismo.

### Mago

**Passiva e talentos automáticos:** Dano de congelamento recebido -10%; dano mágico contra criaturas congeladas +10%. Talentos acrescentam dano mágico e resistência a empurrão.

**Pontos negativos atuais:** -1 coração base; recebe +20% de dano corpo a corpo de criaturas hostis; depende de mana e recargas.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Bola de gelo | cubo de gelo em área de 3 blocos; paralisia 3 s e frio 8 s. Recarga 25 s. | 20 MP |
| Lança-chamas | jato na mira por 3 s, alcance 6 blocos; queima por 6 s. Recarga 30 s. | 30 MP |
| Tremor | área de 4 blocos por 6 s, dano periódico, Lentidão II e desgaste de até 14 blocos naturais. Recarga 40 s. | 35 MP |

**Sugestão, ainda não implementada:** Ter resistência crescente a paralisia nos chefes e uma opção de servidor para impedir Tremor em áreas protegidas.

### Assassino

**Passiva e talentos automáticos:** Treino de adaga: corpo a corpo +5%. Talentos de passos leves e agilidade são compráveis. Furtividade é uma habilidade ativável; não há camuflagem automática ao agachar nesta classe.

**Pontos negativos atuais:** Somente armaduras leves: couro e elytra por padrão; projéteis recebidos causam +25% de dano. Outros mods podem incluir peças na tag oxyrpg:light_armor.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Adaga espectral | Adaga arremessada atravessa até 3 criaturas, destaca por 10 s e causa lentidão por 4 s. | 12 MP |
| Furtividade | jogador e equipamentos desaparecem por 10 s; fumaça por 2 s. Atacar encerra. Recarga 35 s. | 20 MP |
| Execução furtiva | Arrancada de 5 blocos, velocidade e dano físico +30% a +100% por 8 s, conforme o nível. | 15 MP |

Os perfis antigos de Arqueiro viram Assassino preservando nível, XP, atributos, nós e treinamentos. O ID interno ARQUEIRO e o ordinal 3 foram mantidos para compatibilidade. O item vinculado antigo é convertido na Adaga das Sombras. O comando público é `/rpg classe assassino`.

**Sugestão, ainda não implementada:** Especializar em adagas e ataques pelas costas, dando contrajogo de revelação à furtividade em PvP.

### Arqueiro

**Passiva e talentos automáticos:** Mantém Mira treinada: flechas +5%. Passos leves desbloqueia camuflagem ao agachar por 2 s e menor detecção; Agilidade concede +3% de velocidade. Flechas também podem destacar criaturas pelo talento inicial.

**Pontos negativos atuais:** -2 corações base; não usa escudo. Rajada e chuva exigem uma flecha por disparo; cobertura sólida bloqueia flechas guiadas.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Rajada debilitante | Dispara 6 flechas na mira, aplicando lentidão e fraqueza. Consome uma flecha por disparo. | 18 MP |
| Chuva de flechas | 10 flechas sobem e caem sobre a criatura marcada, acompanhando seu deslocamento. | 28 MP |
| Caçada implacável | Por 10 s: velocidade, superpulo e esquivas livres. Ao mirar ou marcar uma criatura, flechas perseguem o alvo e causam lentidão. | 30 MP |

Rajada: 2 flechas na versão inicial; 6 na principal, chegando a 10 com melhorias. Chuva: 4 iniciais ou 10 principais, chegando a 14. A munição é reservada antes da mana; sem munição suficiente, o uso é negado. Flechas especiais do inventário são consumidas como munição da habilidade, que define seus próprios efeitos. Caçada permite esquiva no ar sem cargas; flechas guiadas exigem o desbloqueio principal, criatura visível na mira ou marcada, e continuam colidindo com blocos. Explosões da chuva causam dano em criaturas, sem quebrar terreno.

**Sugestão, ainda não implementada:** Limitar o rastreio contra jogadores por alcance/tempo e manter formas de quebrar a perseguição. Comparar dano por mana da rajada com o arco normal.

### Necromante

**Passiva e talentos automáticos:** Dano de invocações +5%; talentos ampliam vida dos servos e resistência a empurrão. Servos temporários acompanham ataques e têm regras próprias de alvo.

**Pontos negativos atuais:** Dano próprio -20%; movimento -15% durante o dia nas dimensões com céu. Depende de mana e de suas invocações.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Aracnídeo | Lança teia e invoca 3 aranhas que saltam no alvo. Recarga 25 s. | 20 MP |
| Rei dos Mortos | Invoca 1 esqueleto e 3 zumbis por 30 s. Seguem seus ataques; sozinhos, buscam hostis. Recarga 40 s. | 40 MP |
| Céu Noturno | Cega mobs em raio de 6 blocos por 4 s e invoca 3 phantoms, com 1 mergulho cada. Recarga 45 s. | 35 MP |

**Sugestão, ainda não implementada:** Orçamento de servos por jogador e por região, para que grupos de necromantes não sobrecarreguem o servidor.

### Xamã

**Passiva e talentos automáticos:** Raios da habilidade +5%. Talentos ampliam a vida dos animais e resistência a empurrão; aura pode curar e limpar condições.

**Pontos negativos atuais:** Não consome carne/peixe; armas convencionais causam -10% de dano. Especialista exige ambas as mãos vazias.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Deus Relâmpago | ataques de punho chamam raios no inimigo a cada 1,5 s por 15 s, sem ferir o jogador. Recarga 30 s. | 20 MP |
| Doutor Animal | 3 animais protetores, aura de cura e limpeza por 10 s, com farinha de osso na área. Recarga 40 s. | 35 MP |
| Especialista | modo de poções por 15 s, esquerdo negativo e direito positivo, ambas as mãos vazias. Recarga 35 s. | 20 MP |

**Sugestão, ainda não implementada:** Focar suporte e natureza para diferenciar do Deus do Trovão. O nome antigo Deus Relâmpago pode virar Toque da Tempestade.

### Aviário

**Passiva e talentos automáticos:** Queda comum causa 70% do dano normal. Peso pena, depois de aprendido, freia a queda ao segurar Agachar, sem pose agachada, e pode reduzir dano recebido durante a descida.

**Pontos negativos atuais:** -2 corações base; projéteis causam +20% de dano enquanto está no ar. Voo usa janela de duração, mana nos impulsos e velocidade gradual por nível.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Decolagem | Voo por 15 s. Impulsos gastam 8 MP; velocidade cresce com nível e melhorias. | 20 MP |
| Peso pena | Passiva: queda recebida -30%. Agache ao cair: queda lenta e dano de inimigos -25%. | 0 MP |
| Impacto Meteoro | No ar: explode ao atingir o solo. Voando, avança na mira e explode ao atingir mobs ou obstáculos. Altura e velocidade ampliam dano e destruição. | 30 MP |

Primeira ativação lança verticalmente; a segunda entra em planagem e aplica impulso. Ao mergulhar perto de terreno/obstáculo, o pouso controlado põe o corpo ereto, reduz deslocamento horizontal e freia a descida. Durante essa transição, queda/colisão de voo não causam dano; quedas comuns mantêm a redução de 30%. Ao voar, marque uma criatura e passe a até 3,5 blocos para carregá-la abaixo do jogador. Destravar a mira ou pousar a solta; dano de queda passa a seguir a regra da criatura. Chefes Wither/Dragão não são transportáveis.

**Sugestão, ainda não implementada:** Adicionar resistência/peso das criaturas carregadas e custo contínuo para o transporte. Impedir destruição de construções protegidas pelo Meteoro.

### Ilusionista

**Passiva e talentos automáticos:** Disfarce e cópias distraem criaturas. Mímico preserva variantes, equipamento e proporção de vida da referência; cópias têm navegação própria.

**Pontos negativos atuais:** -2 corações base; ataques diretos -20%. Corpo de Eclipse impede atacar, interagir e usar itens; atravessa apenas categorias de blocos liberadas.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Mímico | Duas cópias e aparência do alvo por 12 s. | 28 MP |
| Leviosá | Suspende, enfraquece e fere o alvo; lentidão e desorientação ao redor. | 24 MP |
| Corpo de Eclipse | Forma espectral por 6 s; Espaço sobe, Shift desce. | 35 MP |

**Sugestão, ainda não implementada:** Criar criaturas capazes de revelar ilusões e separar claramente engano visual de imunidade, sobretudo em PvP.

### Hidromante

**Passiva e talentos automáticos:** Respiração aquática, mineração normal submersa e nado melhorado; chuva aumenta dano em 20%, tempestade pode chegar a 40% com talento.

**Pontos negativos atuais:** Sem chuva e fora da água, dano causado -10%; dano de fogo recebido fora da água +15%.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Águaman | Nado veloz, respiração e mineração normal na água; chuva fortalece ataques. | 8 MP |
| Canhão d’água | Jato giratório contínuo; submerso cria manifestações marinhas. | 24 MP |
| Sobre-água | Caminha sobre a água por 15 s; drena um espaço ao ativar e recupera vida. | 25 MP |

**Sugestão, ainda não implementada:** Medir seu desempenho em mapas secos, garantindo fontes de água e objetivos onde a especialização aquática tenha valor.

### Piromaníaco

**Passiva e talentos automáticos:** Caldeira acumula calor com dano/fogo; calor pode aumentar dano. Fênix desbloqueia imunidade a fogo, mobilidade na lava, saltos, brasas e explosão final.

**Pontos negativos atuais:** -1 coração base; frio causa +20% de dano. Na água causa -20% de dano e perde calor; atingir 100 de calor esvazia a caldeira e impõe recuperação de 6 s.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Carnaval de Pólvora | Fogos orbitam os alvos e explodem em sequência; queimaduras atraem mais fogos. | 20 MP |
| Coração de Caldeira | Núcleos detonados criam linhas de fogo entre pontos próximos. | 16 MP |
| Fênix de Cinzas | Imune a fogo; mobilidade na lava, saltos, rastro de brasas e explosão final. | 35 MP |

**Sugestão, ainda não implementada:** Dar ao adversário informação clara do calor e permitir que controle de frio/água interrompa combinações; proteger terreno de contaminação no servidor.

### Vampiresco

**Passiva e talentos automáticos:** Visão noturna à noite. Alimentação usa o botão direito em criatura pacífica: recupera sede e tem 35% de chance de causar sangramento. Golpes em hostis causam sangramento. Twilight destaca trilhas de sangue.

**Pontos negativos atuais:** Sol causa dano e dia aplica Fraqueza; chuva exposta causa dano leve. Comida comum não sacia. Morcego gasta 1 MP/s e não ataca/interage; Twilight aumenta dano recebido em 25%. Guardas/golens reagem à presença noturna.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Sede de Sangue | Ao perder vida, marque um alvo com um golpe. Cura imediata e gradual; repetir o golpe paralisa e fortalece o dano. | 18 MP |
| Metamorfose | Voo livre consome 1 MP/s. Reative sem custo para voltar e convocar morcegos que absorvem dano. | 24 MP |
| Twilight | Fortalece atributos, atravessa folhas e bloqueia feitiços e poções. Recebe +25% de dano. Trilhas de sangue guiam e aceleram. | 30 MP |

Alimentação normal restaura 4 de sede (6 com o talento correspondente), usa intervalo de 3 s e causa 2 pontos de dano na mordida bem-sucedida. A dica de botão aparece até três alimentações bem-sucedidas; depois permanece só o ícone de presas.

**Sugestão, ainda não implementada:** Definir regras de alimentação/retaliação para NPCs importantes e janelas de imunidade de chefes à paralisia. Verificar empilhamento de cura.

### Deus do Trovão

**Passiva e talentos automáticos:** Após 3 s de corrida contínua em solo seco ganha +15% de velocidade até parar. Água impede nado e o faz afundar; calor extremo acumula superaquecimento.

**Pontos negativos atuais:** Sem nado normal. Após aproximadamente 10 s no Nether ou bioma de temperatura base ≥1,5, sofre 1 ponto de dano por segundo enquanto o calor persiste. Água esfria, mas continua sendo uma fraqueza de mobilidade.

| Habilidade principal | Comportamento | Mana base |
|---|---|---|
| Descida do trovão | Teleporte de até 12 blocos; raio causa 6 de dano em raio de 3 blocos. | 20 MP |
| Corrente de raios | Corrente na mira por 4 s; atinge o alvo inicial e salta para até 2 criaturas próximas. | 30 MP |
| Avatar do trovão | Por 8 s: velocidade +40%, subida automática e choque nos punhos com até 3 saltos entre criaturas. Exige ambas as mãos vazias para o choque. | 28 MP |

Teleporte verifica espaço e chunks já carregados; alcance máximo de 20 blocos com as melhorias diretas. O raio aparece no destino e pode ferir criaturas pacíficas e hostis. Corrente segue a mira e, na principal, atinge um alvo mais 2 saltos; o ápice permite mais 2. Choque dos punhos exige ambas as mãos vazias, acrescenta 2 pontos ao golpe e desacelera. Água bloqueia a pose de nado e a velocidade vertical de subida, com simulação nos dois lados. Criativo/espectador preservam seus controles nativos.

**Sugestão, ainda não implementada:** Reservar o papel ofensivo elétrico para esta classe. Medir dano da corrente por mana e adicionar resistência ao choque em certos chefes.

## Desbloqueios completos

IDs são estáveis e começam em zero. “Exige” lista os nós que precisam estar comprados antes; os dois nós gratuitos já estão disponíveis. A linha descreve o talento da árvore, enquanto o catálogo acima explica regras adicionais da mecânica. Os nomes e descrições vêm das definições enviadas ao jogo.

### Árvore de Guerreiro

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Treino de lâmina | Dano corpo a corpo +5%. |
| 1 | 1 | gratuito | Ímpeto | G fortalece brevemente; derrotar inimigos concede +5% de dano por 2 s. |
| 2 | 5 | 1 | Fôlego de combate | Ímpeto dura +1 s. |
| 3 | 10 | 22 | Corte circular | dano em raio de 3 blocos e Força I por 10 s. Recarga 25 s. |
| 4 | 15 | 3 | Lâmina ampla | Raio do corte +0,5 bloco. |
| 5 | 20 | 3 | Golpe pesado | Dano do corte +10%. |
| 6 | 25 | 5, 4 | Ímpeto de batalha | Cada inimigo atingido prolonga a Força em 1 s, até +5 s. |
| 7 | 5 | 0 | Vitalidade | Vida máxima +1 coração. |
| 8 | 10 | 7 | Sede de sangue | Cura 5% do dano, com intervalo; V ativa uma versão fraca de Vampirismo. |
| 9 | 15 | 8 | Sangue quente | Cura passiva passa para 8%. |
| 10 | 20 | 32 | Vampirismo | por 12 s cura 25% do dano. Fome drena em cerca de 6 s; acertos recuperam fome. Recarga 40 s. |
| 11 | 25 | 10 | Controle da fome | Drenagem de fome -10%. |
| 12 | 30 | 10 | Banquete | Fome recuperada por acerto +1 ponto. |
| 13 | 35 | 12, 11 | Sangue vigoroso | Vampirismo: cura +10 pontos percentuais e duração +3 s. |
| 14 | 10 | 0 | Postura firme | Resistência a empurrão +10%. |
| 15 | 15 | 14 | Espinhos leves | Retaliação fraca com intervalo; B ativa guarda inicial. |
| 16 | 20 | 15 | Pele endurecida | Dano recebido -5%. |
| 17 | 30 | 42 | Bastião | brilho, Resistência III, Lentidão I, espinhos e reflexão frontal por 8 s. Recarga 45 s. |
| 18 | 35 | 17 | Guarda prolongada | Bastião dura +1 s. |
| 19 | 40 | 17 | Peso controlado | Penalidade de movimento do Bastião -20%. |
| 20 | 45 | 19, 18 | Fortaleza viva | Retaliação e dano de projéteis refletidos +50%; retaliação empurra. |
| 21 | 5 | 2 | Preparação: Corte circular | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Corte circular | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Vampirismo | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Vampirismo | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Bastião | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Bastião | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Mago

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Afinidade glacial | Frio recebido -10%; dano mágico contra alvos gelados +10%. |
| 1 | 1 | gratuito | Toque gelado | Lança um fragmento de gelo fraco; Lentidão II por 3 s. |
| 2 | 5 | 1 | Frio crescente | Lentidão inicial +1 s. |
| 3 | 10 | 22 | Bola de gelo | cubo de gelo em área de 3 blocos; paralisia 3 s e frio 8 s. Recarga 25 s. |
| 4 | 15 | 3 | Expansão glacial | Área da Bola de gelo +0,5 bloco. |
| 5 | 20 | 3 | Gelo duradouro | Frio da Bola de gelo +2 s. |
| 6 | 25 | 5, 4 | Nevasca | Área +1 bloco adicional, paralisia +1 s e frio +2 s. |
| 7 | 5 | 0 | Calor interior | Dano mágico +5%. |
| 8 | 10 | 7 | Faísca | Libera uma faísca fraca; ataques mágicos queimam por 1 s, com intervalo. |
| 9 | 15 | 8 | Combustão | Queimadura inicial +1 s. |
| 10 | 20 | 32 | Lança-chamas | jato na mira por 3 s, alcance 6 blocos; queima por 6 s. Recarga 30 s. |
| 11 | 25 | 10 | Chama longa | Alcance do jato +1 bloco. |
| 12 | 30 | 10 | Fogo intenso | Dano do jato +10%. |
| 13 | 35 | 12, 11 | Cone infernal | Jato mais amplo, alcance +1 bloco e queimadura +4 s. |
| 14 | 10 | 0 | Sintonia terrestre | Resistência a empurrão +10%. |
| 15 | 15 | 14 | Abalo | B gera um abalo fraco; impactos mágicos aplicam breve lentidão em área. |
| 16 | 20 | 15 | Vibração | Raio do abalo inicial +0,5 bloco. |
| 17 | 30 | 42 | Tremor | área de 4 blocos por 6 s, dano periódico, Lentidão II e desgaste de até 14 blocos naturais. Recarga 40 s. |
| 18 | 35 | 17 | Epicentro | Raio do Tremor +1 bloco; seleciona até 22 blocos naturais. |
| 19 | 40 | 17 | Persistência sísmica | Duração do Tremor +2 s. |
| 20 | 45 | 19, 18 | Réplicas | Após o Tremor, duas ondas menores causam dano e lentidão. |
| 21 | 5 | 2 | Preparação: Bola de gelo | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Bola de gelo | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Lança-chamas | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Lança-chamas | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Tremor | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Tremor | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Assassino

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Treino de adaga | Dano corpo a corpo +5%. Somente armaduras leves; projéteis recebidos causam 25% mais dano. |
| 1 | 1 | gratuito | Adaga breve | Arremessa uma adaga fraca que destaca a criatura por 2 s. |
| 2 | 5 | 1 | Rastro visível | Destaque inicial +1 s. |
| 3 | 10 | 22 | Adaga espectral | Adaga arremessada atravessa até 3 criaturas, destaca por 10 s e causa lentidão por 4 s. |
| 4 | 15 | 3 | Perfuração | Atravessa +1 inimigo. |
| 5 | 20 | 3 | Marca persistente | Brilho +2 s. |
| 6 | 25 | 5, 4 | Lâmina fantasma | Dano da adaga +25% e lentidão +2 s. |
| 7 | 5 | 0 | Passos leves | Distância de detecção dos mobs -10%. |
| 8 | 10 | 7 | Sumir nas sombras | Ative uma furtividade breve; atacar encerra o efeito. |
| 9 | 15 | 8 | Silêncio | Distância de detecção -5% adicionais. |
| 10 | 20 | 32 | Furtividade | jogador e equipamentos desaparecem por 10 s; fumaça por 2 s. Atacar encerra. Recarga 35 s. |
| 11 | 25 | 10 | Sumiço prolongado | Invisibilidade +2 s. |
| 12 | 30 | 10 | Névoa densa | Raio da fumaça +0,5 bloco. |
| 13 | 35 | 12, 11 | Ataque das sombras | Primeiro ataque na furtividade causa entre 25% e 75% mais dano, conforme o nível. |
| 14 | 10 | 0 | Agilidade | Velocidade de movimento +3%. |
| 15 | 15 | 14 | Ataque furtivo | Desbloqueia a arrancada inicial, preparando ataques físicos. |
| 16 | 20 | 15 | Passo preciso | Alcance da arrancada +0,5 bloco. |
| 17 | 30 | 42 | Execução furtiva | Arrancada de 5 blocos, velocidade e dano físico +30% a +100% por 8 s, conforme o nível. |
| 18 | 35 | 17 | Passo longo | Dash +1 bloco. |
| 19 | 40 | 17 | Execução precisa | Bônus de dano durante Execução ganha até 20%, conforme o nível. |
| 20 | 45 | 19, 18 | Passo duplo | Um segundo dash durante a ativação; prolonga a mobilidade. |
| 21 | 5 | 2 | Preparação: Adaga espectral | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Adaga espectral | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Furtividade | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Furtividade | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Execução furtiva | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Execução furtiva | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Arqueiro

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Mira treinada | Flechas causam 5% mais dano. |
| 1 | 1 | gratuito | Rajada breve | Dispara 2 flechas na mira; causa lentidão por 3 s. Consome 2 flechas. |
| 2 | 5 | 1 | Toxina persistente | Lentidão da rajada dura 5 s. |
| 3 | 10 | 22 | Rajada debilitante | Dispara 6 flechas na mira, aplicando lentidão e fraqueza. Consome uma flecha por disparo. |
| 4 | 15 | 3 | Aljava ampla | Rajada dispara mais 2 flechas. |
| 5 | 20 | 3 | Disparo pesado | Dano da rajada +15%. |
| 6 | 25 | 5, 4 | Rajada venenosa | Mais 2 flechas na rajada e veneno por 3 s. |
| 7 | 5 | 0 | Passos leves | Agachar por 2 s concede camuflagem breve; detecção de criaturas -10%. |
| 8 | 10 | 7 | Chuva breve | Dispara 4 flechas para cima que convergem sobre uma criatura marcada. |
| 9 | 15 | 8 | Flechas firmes | Dano das flechas da chuva +15%. |
| 10 | 20 | 32 | Chuva de flechas | 10 flechas sobem e caem sobre a criatura marcada, acompanhando seu deslocamento. |
| 11 | 25 | 10 | Nuvem de setas | Chuva dispara mais 4 flechas. |
| 12 | 30 | 10 | Ponta reforçada | Dano da chuva +25%; explosões ganham 1 de dano. |
| 13 | 35 | 12, 11 | Chuva explosiva | Flechas da chuva explodem ao colidir; raio 2,5 blocos, sem destruir terreno. |
| 14 | 10 | 0 | Agilidade | Velocidade de movimento +3%. |
| 15 | 15 | 14 | Caçada breve | Velocidade e superpulo por 4 s; esquivas sem consumir cargas, inclusive no ar. |
| 16 | 20 | 15 | Fôlego da caçada | Caçada inicial e principal duram mais 1 s. |
| 17 | 30 | 42 | Caçada implacável | Por 10 s: velocidade, superpulo e esquivas livres. Ao mirar ou marcar uma criatura, flechas perseguem o alvo e causam lentidão. |
| 18 | 35 | 17 | Caçada prolongada | Caçada dura mais 2 s; lentidão dos disparos guiados dura 6 s. |
| 19 | 40 | 17 | Setas velozes | Velocidade dos disparos guiados aumenta de 3,4 para 4 blocos/tick. |
| 20 | 45 | 19, 18 | Caçador aéreo | Superpulo III e lentidão II nos disparos guiados. |
| 21 | 5 | 2 | Preparação: Rajada debilitante | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Rajada debilitante | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Chuva de flechas | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Chuva de flechas | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Caçada implacável | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Caçada implacável | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Necromante

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Pacto sombrio | Dano das invocações +5%. |
| 1 | 1 | gratuito | Filhote aracnídeo | Lança teia e invoca 1 aranha no alvo. Após perder o alvo, espera 4 s pelo próximo ataque do jogador. |
| 2 | 5 | 1 | Ninhada | Aracnídeo inicial invoca +1 aranha. |
| 3 | 10 | 22 | Aracnídeo | Lança teia e invoca 3 aranhas que saltam no alvo. Recarga 25 s. |
| 4 | 15 | 3 | Colônia | Aracnídeo principal invoca +1 aranha. |
| 5 | 20 | 3 | Fios persistentes | Lentidão da teia +2 s. |
| 6 | 25 | 5, 4 | Matriarca | Aracnídeo principal invoca +1 aranha, até 5. |
| 7 | 5 | 0 | Ossos firmes | Vida das invocações +10%. |
| 8 | 10 | 7 | Servo morto | Invoca 1 zumbi por 15 s; segue seus ataques e busca apenas hostis sozinho. |
| 9 | 15 | 8 | Companhia pútrida | Rei dos Mortos inicial invoca +1 zumbi. |
| 10 | 20 | 32 | Rei dos Mortos | Invoca 1 esqueleto e 3 zumbis por 30 s. Seguem seus ataques; sozinhos, buscam hostis. Recarga 40 s. |
| 11 | 25 | 10 | Guarda óssea | Rei dos Mortos principal invoca +1 esqueleto. |
| 12 | 30 | 10 | Horda | Rei dos Mortos principal invoca +1 zumbi. |
| 13 | 35 | 12, 11 | Coroa dos mortos | Rei dos Mortos principal invoca +1 esqueleto e +1 zumbi, até 3 e 5. |
| 14 | 10 | 0 | Sombra firme | Resistência a empurrão +10%. |
| 15 | 15 | 14 | Asa sombria | Cega mobs próximos por 2 s e invoca 1 phantom para 1 mergulho. |
| 16 | 20 | 15 | Breve eclipse | Cegueira inicial +1 s. |
| 17 | 30 | 42 | Céu Noturno | Cega mobs em raio de 6 blocos por 4 s e invoca 3 phantoms, com 1 mergulho cada. Recarga 45 s. |
| 18 | 35 | 17 | Horizonte escuro | Raio de Céu Noturno +1 bloco. |
| 19 | 40 | 17 | Mergulho cruel | Dano dos phantoms +15%. |
| 20 | 45 | 19, 18 | Noite profunda | Cegueira de Céu Noturno +2 s. |
| 21 | 5 | 2 | Preparação: Aracnídeo | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Aracnídeo | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Rei dos Mortos | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Rei dos Mortos | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Céu Noturno | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Céu Noturno | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Xamã

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Carga somática | Dano dos raios +5%. |
| 1 | 1 | gratuito | Estalos | Ativa raios fracos nos ataques de punho por 5 s, com intervalo de 1,5 s. |
| 2 | 5 | 1 | Faísca intensa | Dano dos raios +1. |
| 3 | 10 | 22 | Deus Relâmpago | ataques de punho chamam raios no inimigo a cada 1,5 s por 15 s, sem ferir o jogador. Recarga 30 s. |
| 4 | 15 | 3 | Tempestade longa | Duração de Deus Relâmpago +3 s. |
| 5 | 20 | 3 | Raio intenso | Dano dos raios +2. |
| 6 | 25 | 5, 4 | Raio encadeado | Um segundo hostil próximo recebe metade do dano elétrico. |
| 7 | 5 | 0 | Laço vital | Vida dos animais invocados +10%. |
| 8 | 10 | 7 | Primeiros socorros | Invoca 1 animal, cura levemente e remove efeitos negativos por 5 s; fertiliza plantas. |
| 9 | 15 | 8 | Aura persistente | Aura de cura +2 s. |
| 10 | 20 | 32 | Doutor Animal | 3 animais protetores, aura de cura e limpeza por 10 s, com farinha de osso na área. Recarga 40 s. |
| 11 | 25 | 10 | Círculo de cuidado | Raio de cura +1 bloco. |
| 12 | 30 | 10 | Cura reforçada | Cura por pulso +0,5 ponto de vida. |
| 13 | 35 | 12, 11 | Guardião da natureza | Invoca +1 animal; aura dura +5 s. |
| 14 | 10 | 0 | Foco terrestre | Resistência a empurrão +10%. |
| 15 | 15 | 14 | Misturas básicas | Abre Especialista por 8 s: mãos vazias, esquerdo negativo e direito positivo. Poções fracas e aleatórias. |
| 16 | 20 | 15 | Preparo rápido | Intervalo entre poções reduzido de 1 s para 0,75 s. |
| 17 | 30 | 42 | Especialista | modo de poções por 15 s, esquerdo negativo e direito positivo, ambas as mãos vazias. Recarga 35 s. |
| 18 | 35 | 17 | Bolsa de misturas | Duração do modo +5 s. |
| 19 | 40 | 17 | Essência duradoura | Efeitos não instantâneos das poções principais +5 s. |
| 20 | 45 | 19, 18 | Destilação econômica | Custo de mana por poção -1. |
| 21 | 5 | 2 | Preparação: Deus Relâmpago | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Deus Relâmpago | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Doutor Animal | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Doutor Animal | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Especialista | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Especialista | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Aviário

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Instinto das alturas | Dano de queda -30%. |
| 1 | 1 | gratuito | Salto alado | Voo inicial por 8 s. Impulso breve com velocidade limitada pelo nível. |
| 2 | 5 | 1 | Asas firmes | Voo +2 s. |
| 3 | 10 | 22 | Decolagem | Voo por 15 s. Impulsos gastam 8 MP; velocidade cresce com nível e melhorias. |
| 4 | 15 | 3 | Fôlego aéreo | Voo +4 s. |
| 5 | 20 | 3 | Corrente ascendente | Impulso +1 s. |
| 6 | 25 | 5, 4 | Senhor dos céus | Voo e velocidade adicionais crescem com o nível. |
| 7 | 5 | 0 | Penas leves | Dano de queda -30%. |
| 8 | 10 | 7 | Pluma protetora | Passiva: queda recebida -30%; agache ao cair para descer lentamente. |
| 9 | 15 | 8 | Ar amortecido | Enquanto desce agachado, dano de inimigos -10%. |
| 10 | 20 | 32 | Peso pena | Passiva: queda recebida -30%. Agache ao cair: queda lenta e dano de inimigos -25%. |
| 11 | 25 | 10 | Brisa gentil | Descida agachada mais lenta. |
| 12 | 30 | 10 | Manto de penas | Redução de dano durante a descida +10%. |
| 13 | 35 | 12, 11 | Corpo etéreo | Redução de dano durante a descida +10%. |
| 14 | 10 | 0 | Massa concentrada | Impactos causam +5% de dano. |
| 15 | 15 | 14 | Queda ofensiva | No ar: prepara impacto menor. Voando, avance até atingir um alvo; caindo, mergulhe. Não destrói blocos. |
| 16 | 20 | 15 | Peso crescente | Dano do impacto +15%. |
| 17 | 30 | 42 | Impacto Meteoro | No ar: explode ao atingir o solo. Voando, avança na mira e explode ao atingir mobs ou obstáculos. Altura e velocidade ampliam dano e destruição. |
| 18 | 35 | 17 | Cratera ampla | Raio do impacto +1 bloco. |
| 19 | 40 | 17 | Núcleo denso | Dano do impacto +20%. |
| 20 | 45 | 19, 18 | Cataclismo | Raio +1 bloco e mais detritos arremessados. |
| 21 | 5 | 2 | Preparação: Decolagem | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Decolagem | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Peso pena | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Peso pena | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Impacto Meteoro | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Impacto Meteoro | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Ilusionista

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Espelhos frágeis | Ataques diretos -20%; ilusões distraem os inimigos. |
| 1 | 1 | gratuito | Reflexo vivo | Duas cópias e aparência do alvo por 6 s. |
| 2 | 5 | 1 | Imitação firme | Cópias resistem a um golpe adicional. |
| 3 | 10 | 22 | Mímico | Duas cópias e aparência do alvo por 12 s. |
| 4 | 15 | 3 | Reflexo extra | Uma cópia adicional. |
| 5 | 20 | 3 | Ilusão duradoura | Duração do Mímico +3 s. |
| 6 | 25 | 5, 4 | Salão de espelhos | Mais uma cópia; ilusões causam mais dano. |
| 7 | 5 | 0 | Fio invisível | Prepara o ramo de controle. |
| 8 | 10 | 7 | Suspensão | Ergue e enfraquece o alvo por 3 s. |
| 9 | 15 | 8 | Peso da dúvida | Controle causa mais dano. |
| 10 | 20 | 32 | Leviosá | Suspende, enfraquece e fere o alvo; lentidão e desorientação ao redor. |
| 11 | 25 | 10 | Eco expansivo | Área de desorientação ampliada. |
| 12 | 30 | 10 | Desorientação | Controle dura mais tempo. |
| 13 | 35 | 12, 11 | Gravidade falsa | Alvos controlados ficam mais vulneráveis. |
| 14 | 10 | 0 | Fuga nas sombras | Prepara a forma de enxame. |
| 15 | 15 | 14 | Enxame fugaz | Forma espectral por 3 s; voo baixo e passagem por blocos comuns. |
| 16 | 20 | 15 | Asas noturnas | Enxame se move mais rapidamente. |
| 17 | 30 | 42 | Corpo de Eclipse | Forma espectral por 6 s; Espaço sobe, Shift desce. |
| 18 | 35 | 17 | Noite prolongada | Enxame +2 s. |
| 19 | 40 | 17 | Entre frestas | Pode atravessar portas, cercas e grades. |
| 20 | 45 | 19, 18 | Eclipse total | Voo até 8 blocos do terreno e movimento mais rápido. |
| 21 | 5 | 2 | Preparação: Mímico | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Mímico | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Leviosá | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Leviosá | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Corpo de Eclipse | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Corpo de Eclipse | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Hidromante

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Filho das marés | Respiração aquática e trabalho submerso; vulnerável ao fogo fora da água. |
| 1 | 1 | gratuito | Impulso aquático | Na água, avance na direção da mira; mire para cima para saltar. |
| 2 | 5 | 1 | Nado firme | Nado mais veloz. |
| 3 | 10 | 22 | Águaman | Nado veloz, respiração e mineração normal na água; chuva fortalece ataques. |
| 4 | 15 | 3 | Correnteza | Impulso mais forte. |
| 5 | 20 | 3 | Salto das ondas | Impulso ascendente ampliado. |
| 6 | 25 | 5, 4 | Senhor da tempestade | Tempestades ampliam o dano; nado ainda mais veloz. |
| 7 | 5 | 0 | Pressão crescente | Prepara o canhão. |
| 8 | 10 | 7 | Jato d’água | Jato curto com dano e empurrão. |
| 9 | 15 | 8 | Pressão concentrada | Jato causa mais dano. |
| 10 | 20 | 32 | Canhão d’água | Jato giratório contínuo; submerso cria manifestações marinhas. |
| 11 | 25 | 10 | Alcance da maré | Jato alcança mais longe. |
| 12 | 30 | 10 | Mar revolto | Empurrão mais forte. |
| 13 | 35 | 12, 11 | Abismo vivo | Mais manifestações marinhas e duração de jato. |
| 14 | 10 | 0 | Equilíbrio da maré | Prepara o controle da superfície. |
| 15 | 15 | 14 | Passo aquático | Caminha sobre a água por 6 s; agache para mergulhar. |
| 16 | 20 | 15 | Água restauradora | Ativação em contato com água cura mais. |
| 17 | 30 | 42 | Sobre-água | Caminha sobre a água por 15 s; drena um espaço ao ativar e recupera vida. |
| 18 | 35 | 17 | Bolha ampla | Espaço drenado maior. |
| 19 | 40 | 17 | Maré persistente | Caminhada sobre água dura mais. |
| 20 | 45 | 19, 18 | Renovação oceânica | Cura ampliada e limpeza de efeitos negativos. |
| 21 | 5 | 2 | Preparação: Águaman | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Águaman | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Canhão d’água | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Canhão d’água | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Sobre-água | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Sobre-água | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Piromaníaco

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Caldeira viva | Fogo e explosões geram calor; excesso causa superaquecimento. |
| 1 | 1 | gratuito | Pavio encantado | Marca inimigos próximos com fogos. Reative para detonar. |
| 2 | 5 | 1 | Estopim longo | Fogos duram mais. |
| 3 | 10 | 22 | Carnaval de Pólvora | Fogos orbitam os alvos e explodem em sequência; queimaduras atraem mais fogos. |
| 4 | 15 | 3 | Pólvora espalhada | Marca mais alvos. |
| 5 | 20 | 3 | Explosão ascendente | Explosões lançam inimigos mais alto. |
| 6 | 25 | 5, 4 | Festa sem fim | Detonações propagam fogos para outros inimigos, com limite. |
| 7 | 5 | 0 | Pedra incandescente | Prepara núcleos de magma. |
| 8 | 10 | 7 | Gota de magma | Lança um núcleo que adere ao alvo ou ao chão. Reative agachado para detonar. |
| 9 | 15 | 8 | Calor aderente | Núcleos causam queimadura mais longa. |
| 10 | 20 | 32 | Coração de Caldeira | Núcleos detonados criam linhas de fogo entre pontos próximos. |
| 11 | 25 | 10 | Reservatório | Pode preparar mais núcleos. |
| 12 | 30 | 10 | Fios ardentes | Linhas ficam ativas por mais tempo. |
| 13 | 35 | 12, 11 | Magma voraz | Núcleos e linhas causam mais dano. |
| 14 | 10 | 0 | Brasa ancestral | Prepara a transformação. |
| 15 | 15 | 14 | Asas de brasa | Consome 30 de calor: forma ígnea breve e salto explosivo. |
| 16 | 20 | 15 | Penas ardentes | Saltos da Fênix causam mais dano. |
| 17 | 30 | 42 | Fênix de Cinzas | Imune a fogo; mobilidade na lava, saltos, rastro de brasas e explosão final. |
| 18 | 35 | 17 | Cinzas persistentes | Fênix dura mais tempo. |
| 19 | 40 | 17 | Vapor escaldante | Água cria vapor que desorienta inimigos. |
| 20 | 45 | 19, 18 | Renascimento explosivo | Explosão final consome núcleos próximos e fica mais forte. |
| 21 | 5 | 2 | Preparação: Carnaval de Pólvora | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Carnaval de Pólvora | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Coração de Caldeira | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Coração de Caldeira | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Fênix de Cinzas | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Fênix de Cinzas | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Vampiresco

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Instinto noturno | Passiva: sol fere, dia enfraquece; à noite recebe visão noturna. |
| 1 | 1 | gratuito | Primeira mordida | Arme um vínculo ao perder vida: seu próximo golpe cura e abre uma ferida. |
| 2 | 5 | 1 | Paladar aguçado | Botão direito em um alvo pacífico recupera mais sede. |
| 3 | 10 | 22 | Sede de Sangue | Ao perder vida, marque um alvo com um golpe. Cura imediata e gradual; repetir o golpe paralisa e fortalece o dano. |
| 4 | 15 | 3 | Vínculo cruel | Golpes no alvo vinculado recebem mais dano adicional. |
| 5 | 20 | 3 | Sangue restaurador | Amplia a cura gradual do vínculo. |
| 6 | 25 | 5, 4 | Noite eterna | O vínculo paralisa por mais tempo; reduz o dano do sol. |
| 7 | 5 | 0 | Pele de morcego | Metamorfose conserva o controle do voo. |
| 8 | 10 | 7 | Asas menores | Transforme-se em morcego: voe sem atacar ou usar itens. Reative para voltar com escolta. |
| 9 | 15 | 8 | Reservas sombrias | Preparação para ampliar sua escolta. |
| 10 | 20 | 32 | Metamorfose | Voo livre consome 1 MP/s. Reative sem custo para voltar e convocar morcegos que absorvem dano. |
| 11 | 25 | 10 | Legião crescente | Dois morcegos adicionais protegem você. |
| 12 | 30 | 10 | Carne das sombras | Cada morcego suporta dois pontos de dano adicionais. |
| 13 | 35 | 12, 11 | Guarda noturna | Escolta permanece mais quatro segundos. |
| 14 | 10 | 0 | Afinidade crepuscular | Prepare-se para enxergar a trilha de sangue. |
| 15 | 15 | 14 | Crepúsculo menor | Força, velocidade e pressa; atravessa folhas, mas recebe mais dano físico. |
| 16 | 20 | 15 | Olhar carmesim | Preparação para o domínio do Twilight. |
| 17 | 30 | 42 | Twilight | Fortalece atributos, atravessa folhas e bloqueia feitiços e poções. Recebe +25% de dano. Trilhas de sangue guiam e aceleram. |
| 18 | 35 | 17 | Caçador veloz | Twilight concede Velocidade II. |
| 19 | 40 | 17 | Mãos vorazes | Twilight concede Pressa II. |
| 20 | 45 | 19, 18 | Trilha da caça | Seguir sangue concede mais velocidade. |
| 21 | 5 | 2 | Preparação: Sede de Sangue | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Sede de Sangue | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Metamorfose | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Metamorfose | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Twilight | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Twilight | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

### Árvore de Deus do Trovão

| Nó | Nível | Exige | Desbloqueio | Efeito |
|---:|---:|---|---|---|
| 0 | 1 | gratuito | Condutor vivo | Após correr por 3 s no chão, ganha 15% de velocidade até interromper a corrida. |
| 1 | 1 | gratuito | Salto elétrico | Teleporte de até 5 blocos na mira; raio causa 3 de dano em raio de 1,5 bloco. |
| 2 | 5 | 1 | Choque de chegada | Criaturas atingidas no teleporte recebem lentidão por 2 s. |
| 3 | 10 | 22 | Descida do trovão | Teleporte de até 12 blocos; raio causa 6 de dano em raio de 3 blocos. |
| 4 | 15 | 3 | Horizonte elétrico | Alcance do teleporte +4 blocos. |
| 5 | 20 | 3 | Carga de chegada | Dano do teleporte +2; raio do impacto +1 bloco; correntes causam 20% mais dano. |
| 6 | 25 | 5, 4 | Passo de tempestade | Teleporte ganha mais 4 blocos; salto entre criaturas alcança 6 blocos. |
| 7 | 5 | 0 | Afinidade elétrica | Preparação para canalizar a corrente. |
| 8 | 10 | 7 | Faísca dirigida | Corrente na mira por 2 s; alcance de 18 blocos e dano periódico. |
| 9 | 15 | 8 | Carga intensa | Dano do alvo inicial da corrente +1 por pulso. |
| 10 | 20 | 32 | Corrente de raios | Corrente na mira por 4 s; atinge o alvo inicial e salta para até 2 criaturas próximas. |
| 11 | 25 | 10 | Arco longo | Alcance da corrente aumenta para 24 blocos. |
| 12 | 30 | 10 | Canalização persistente | Corrente dura mais 2 s. |
| 13 | 35 | 12, 11 | Tempestade encadeada | Corrente salta para mais 2 criaturas próximas. |
| 14 | 10 | 0 | Punhos carregados | Preparação para a tempestade de punhos. |
| 15 | 15 | 14 | Pulso de tempestade | Por 3 s ganha velocidade, sobe blocos e aplica choque com as mãos vazias; o raio salta para outra criatura. |
| 16 | 20 | 15 | Pulso duradouro | Pulso de tempestade dura mais 1 s. |
| 17 | 30 | 42 | Avatar do trovão | Por 8 s: velocidade +40%, subida automática e choque nos punhos com até 3 saltos entre criaturas. Exige ambas as mãos vazias para o choque. |
| 18 | 35 | 17 | Avatar persistente | Avatar dura mais 2 s. |
| 19 | 40 | 17 | Velocidade divina | Velocidade do Avatar ganha mais 15 pontos percentuais. |
| 20 | 45 | 19, 18 | Escalada elétrica | Altura de passo durante o Avatar aumenta mais 0,5 bloco. |
| 21 | 5 | 2 | Preparação: Descida do trovão | Dano da classe +3%. |
| 22 | 5 | 21 | Domínio: Descida do trovão | Dano da classe +3%. |
| 23 | 30 | 6 | Potência I | Dano da classe +3%. |
| 24 | 38 | 6 | Persistência I | Duração das habilidades +5%. |
| 25 | 46 | 6 | Eficiência I | Custo de mana das habilidades -3%. |
| 26 | 54 | 6 | Recuperação I | Recarga das habilidades -3%. |
| 27 | 62 | 23 | Potência II | Dano da classe +3%. |
| 28 | 70 | 24 | Persistência II | Duração das habilidades +5%. |
| 29 | 78 | 25 | Eficiência II | Custo de mana das habilidades -3%. |
| 30 | 86 | 26 | Recuperação II | Recarga das habilidades -3%. |
| 31 | 15 | 9 | Preparação: Corrente de raios | Dano da classe +3%. |
| 32 | 15 | 31 | Domínio: Corrente de raios | Dano da classe +3%. |
| 33 | 30 | 13 | Potência I | Dano da classe +3%. |
| 34 | 38 | 13 | Persistência I | Duração das habilidades +5%. |
| 35 | 46 | 13 | Eficiência I | Custo de mana das habilidades -3%. |
| 36 | 54 | 13 | Recuperação I | Recarga das habilidades -3%. |
| 37 | 62 | 33 | Potência II | Dano da classe +3%. |
| 38 | 70 | 34 | Persistência II | Duração das habilidades +5%. |
| 39 | 78 | 35 | Eficiência II | Custo de mana das habilidades -3%. |
| 40 | 86 | 36 | Recuperação II | Recarga das habilidades -3%. |
| 41 | 20 | 16 | Preparação: Avatar do trovão | Dano da classe +3%. |
| 42 | 20 | 41 | Domínio: Avatar do trovão | Dano da classe +3%. |
| 43 | 30 | 20 | Potência I | Dano da classe +3%. |
| 44 | 38 | 20 | Persistência I | Duração das habilidades +5%. |
| 45 | 46 | 20 | Eficiência I | Custo de mana das habilidades -3%. |
| 46 | 54 | 20 | Recuperação I | Recarga das habilidades -3%. |
| 47 | 62 | 43 | Potência II | Dano da classe +3%. |
| 48 | 70 | 44 | Persistência II | Duração das habilidades +5%. |
| 49 | 78 | 45 | Eficiência II | Custo de mana das habilidades -3%. |
| 50 | 86 | 46 | Recuperação II | Recarga das habilidades -3%. |

## Prioridade sugerida para o servidor

1. **Definir PvE/PvP, grupos e proteção de terreno.** As habilidades atualmente miram criaturas; golpes vanilla continuam sujeitos às regras do servidor. Centralizar permissões e aliados antes de incluir combate entre classes. Tremor, Meteoro e magma precisam respeitar construções/claims; chuva explosiva do novo Arqueiro já preserva blocos.

2. **Criar escolhas de especialização.** Limitar pontos finais ou acrescentar caminhos mutuamente exclusivos. Minha sugestão inicial é três especializações por classe com um ápice escolhido, mantendo versões iniciais úteis. Isso ainda não foi aplicado.

3. **Medir curvas de combate em sobrevivência.** Comparar dano por mana/segundo, cura, mobilidade, tempo de controle e tempo de morte nos níveis 1, 10, 30, 60 e 100. Começar por cura do Guerreiro/Vampiro, mobilidade do Aviário, flechas guiadas e correntes elétricas.

4. **Dar resistência a chefes e orçamento por região.** Evitar paralisia permanente e limitar invocações/cópias/efeitos simultâneos por área. Partículas elétricas desta versão usam um pacote por arco e geração visual no cliente, em vez de dezenas de pacotes por segmento.

5. **Preparar operação.** Clientes precisam da mesma versão do mod e dependências. Debug é de operador (nível de permissão 2); modo de testes mantém custos normais. Backups, atualização com servidor fechado, documentação de datapacks e uma rodada de testes de multiplayer precedem a abertura pública.

Esta lista é uma recomendação de evolução; a 0.50.0 não constitui validação de um servidor público pronto.
