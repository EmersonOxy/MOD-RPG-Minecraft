# Oxy RPG 0.26.0 — classes, progressão e itens vinculados

## Novas classes

Cada classe tem 51 nós, uma disposição própria e três ramos. G, V e B continuam remapeáveis nas opções do Minecraft.

### Ilusionista

- Mímico: mire em um mob visível a até 24 blocos. Cria duas cópias e uma aparência visual da criatura sobre o jogador, ocultando o modelo/equipamento do jogador. Melhorias chegam a quatro cópias. Cópias distraem e causam dano reduzido. A transformação mantém os atributos e o tamanho de colisão do jogador; não concede atributos de chefes. Dragão, Wither, Warden e criaturas muito grandes não são copiáveis.
- Leviosá: levitação, fraqueza e dano periódico. O principal aplica lentidão e desorientação (fraqueza) na área; melhorias ampliam o controle.
- Corpo de Eclipse: enxame de morcegos, imunidade a danos comuns, mobilidade e perda de alvo. Não permite ataques, mineração, uso de itens ou outras skills. Começa atravessando folhas, teias e escaláveis; melhorias permitem portas, cercas, grades e vidro. O fim da forma retorna à última posição livre se necessário. Dano que ignora invulnerabilidade, como vazio, continua válido.

### Hidromante

- Águaman: respiração aquática e trabalho submerso. Nado cresce pela árvore. G na água dá impulso na mira; mirar para cima permite sair da água. Chuva sob céu aberto fortalece ataques; tempestades melhoram o bônus com a especialização.
- Canhão d’água: jato giratório contínuo, dano e empurrão na direção escolhida ao ativar. Submerso cria manifestações marinhas temporárias, sem drops/XP.
- Sobre-água: superfície sólida enquanto a habilidade estiver ativa; agache para mergulhar. Ativar em contato com água cura e, com o principal, drena um espaço pequeno por 5 s. A água é restaurada por um registro persistente, inclusive após reiniciar. Blocos colocados pelo jogador não são substituídos na restauração.

### Piromaníaco

- Carnaval de Pólvora: G marca inimigos próximos com fogos orbitais. G novamente inicia explosões sequenciais. Queimaduras atraem mais fogos; a melhoria final propaga a cadeia, com limite.
- Coração de Caldeira: V lança magma aderente. V adiciona núcleos, até o limite da árvore; **agache e pressione V** para detonar os núcleos. Com o principal, detonações criam linhas ardentes entre pontos próximos. São efeitos temporários, sem incêndio/destruição permanente de construções.
- Fênix de Cinzas: B exige 30 de calor, além da mana. A forma ganha imunidade ao fogo, mobilidade na lava, saltos explosivos e rastro de brasas. O salto usa o comando normal de pular. Termina com explosão; a melhoria final consome núcleos próximos para ampliar o encerramento. Vapor na água exige a melhoria correspondente.
- Calor: aparece em uma barra própria; habilidades, acertos e inimigos queimando próximos geram calor. Água/chuva esfriam rapidamente. Calor alto aumenta dano, mas chegar a 100 causa recuperação de 6 s. Mana e recargas continuam normais no modo teste; apenas debug ignora limites de recursos.

## Fraquezas

| Classe | Limitações |
|---|---|
| Guerreiro | Sem armas de distância; fome extra, inclusive no roubo de vida |
| Mago | -1 coração base; +20% dano corpo a corpo recebido |
| Arqueiro | Sem escudo; -2 corações base |
| Necromante | -20% dano próprio; -15% velocidade durante o dia |
| Xamã | Sem carne/peixe; -10% dano com armas convencionais |
| Aviário | -2 corações base; +20% dano de projéteis recebido no ar |
| Ilusionista | -2 corações base; -20% dano de ataques diretos |
| Hidromante | Fora da água, +15% dano de fogo; -10% dano causado fora da água sem chuva |
| Piromaníaco | -1 coração base; +20% dano de frio; água reduz dano causado em 20% e esfria o calor |

Os bônus de vida da árvore continuam sendo adicionados à vida base modificada.

## Aviário e classes existentes

O Aviário começa com voo de 8 s e impulso curto. O teto de velocidade vai de aproximadamente 1,07 bloco/tick no nível 1 até 3,8 com nível 100 e melhoria final. A aceleração, duração e velocidade adicional crescem com progressão. Impulsos gastam 8 MP antes dos descontos. A pluma substitui a maça do kit inicial; bastão na etapa intermediária, maça apenas no nível 50 + Arsenal 16.

Arqueiro: bônus do Assassino e de ataques durante furtividade crescem com o nível. Necromante: vida e dano das invocações crescem com o nível. Guerreiro: cura limitada por evento e intervalo mínimo de 0,5 s durante Vampirismo; ataques consomem fome, em vez de restaurar alimento. Os ajustes preservam IDs, desbloqueios, atributos e profissões dos saves existentes.

## Equipamentos vinculados

Armas e armaduras do kit de classe retornam ao inventário após a morte. Se o inventário estiver cheio, ficam em uma fila persistente até haver espaço. A recuperação impõe **60 s** de espera para usar a arma e seu efeito exclusivo. Provisões e munição continuam consumíveis normais.

O efeito exclusivo da arma/item é acionado pelo **botão direito**, seguindo o controle nativo Usar Item, com o item em uma das mãos (atualizado na 0.27). Custa 10 MP e recarrega em 35/30/25 s conforme a etapa. Essa recarga do efeito permite continuar usando normalmente a arma entre ativações; a espera após morrer bloqueia seu uso até recuperar.

| Classe | Efeito exclusivo |
|---|---|
| Guerreiro | Guarda: resistência breve e empurrão |
| Mago | Próximo ataque desacelera |
| Arqueiro | Próximo alvo fica destacado |
| Necromante | Próximo ataque recupera 1 coração |
| Xamã | Cura e limpeza de veneno/fraqueza |
| Aviário | Impulso de recuperação e queda lenta |
| Ilusionista | Desorientação de inimigos próximos |
| Hidromante | Cura/nado na água; respiração breve fora dela |
| Piromaníaco | Queima inimigos próximos |

Kits antigos são atualizados quando a classe estiver ativa. `/rpg kit` no modo teste concede o item avançado da classe selecionada se ainda não estiver no inventário. `/rpg teste ilusionista`, `/rpg teste hidromante` e `/rpg teste piromante` permitem explorar as novas árvores com custos normais.

## Recursos usados e próximos passos

Esta versão usa tags de blocos para passagem do enxame, tipos próprios de dano (brasas, pressão e ilusão), sincronização de estado e arquivos persistentes para recuperação. Resistências ao fogo dos Data Maps também se aplicam ao dano das brasas. Definições das nove árvores continuam sendo geradas e customizáveis por datapack. O profiler também registra a seção `oxyrpg_specialists`.

Próximos recursos úteis: loot tables/Global Loot Modifiers para recompensas por nível e um guia de combos dentro do jogo. Patchouli é uma referência para livros de guia; sua disponibilidade exata para Minecraft 1.21.10 precisa ser confirmada antes de adicioná-lo como dependência.

## Validação desta entrega

Compilação assemble e geração de dados realizadas. As nove definições são validadas pelo gerador. Não foram executados testes automatizados nem aberto o Minecraft nesta atualização, conforme a preferência registrada em AGENTS.md. Aparência, sensação de movimento e balanceamento ainda precisam de validação pessoal no jogo.
