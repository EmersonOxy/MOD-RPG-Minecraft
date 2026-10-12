# 0.49.0 — mira, disfarces, voo e efeitos

Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21. Protocolo 32: cliente e servidor precisam da mesma versão.

## Evidências e alcance

Inspecionados os nove prints mais recentes desta solicitação: 2026-10-12_15.18.20, 15.18.19, 15.18.15_2, 15.18.15, 14.54.13, 14.49.46, 14.43.42, 14.40.46 e 14.40.41. Evidenciam a mira deslocada perto de terreno/alvos, inclinação durante voo e a seta de esquiva. São imagens da versão anterior, não validação da 0.49.

Revisão detalhada dos caminhos alterados, fontes/descritores nativos e APIs do port instalado. Compilação por assemble; sem testes automatizados, GameTests ou abertura do Minecraft, conforme AGENTS.md. O inventário de hashes registra arquivos, não comprova revisão funcional de cada arquivo.

## Guardas e golens

VampireHunt diferencia defensores de moradores. Guardas próprios e AbstractGolem (ferro/neve) revidam ao vampiro em vez de receber intenção de fuga. À noite, a proximidade provoca agressão, como já provocava medo nos aldeões comuns. Ataques/sangramento também provocam retaliação. Guardas procuram vampiros na sua seleção de alvos, respeitando linha de visão e limite da patrulha, sem apagar o alvo na próxima atualização. Uma conversa em andamento é encerrada ao entrar em combate. Aldeões comuns mantêm a fuga. Dano ao jogador continua sujeito às regras vanilla de criativo/espectador.

## Aviário e Barrel Roll

Decolagem pode partir do voo criativo: desativa apenas o estado flying, preserva mayfly/modo criativo, sincroniza as habilidades e inicia o lançamento vertical. Segunda ativação continua entrando na planagem/boost. Se o jogador reativar o voo criativo, encerra-se a planagem RPG em ambos os lados. A ponte opcional limpa PITCH/YAW/ROLL_SMOOTHER e reinicia rolling/roll nas transições, evitando reutilizar a rotação acumulada. Métodos conferidos no bytecode do port 3.7.4 instalado; nenhum JAR externo foi modificado. Eficácia do ajuste na rotação precisa ser confirmada em jogo.

## Ilusionista

Mímico deixa de criar um mob físico na posição do jogador. DisguiseState comunica tipo do mob, entidade de referência e duração; o cliente constrói somente um modelo, sem adicioná-lo ao mundo, navegar, executar AI ou colidir. O renderizador da criatura substitui o avatar, com pose/movimento do jogador e aparência/equipamento sincronizados da referência quando disponível. A física continua pertencendo ao jogador; não se passa a controlar uma entidade AI.

As cópias permanecem mobs separados, com sua navegação normal e retorno quando passam do raio de acompanhamento. Disfarce expira, limpa na mudança de classe/logout/dimensão e é comunicado a novos observadores. Durante o enxame o modelo do disfarce é suspenso e pode voltar quando o enxame termina. Mantidas as restrições anteriores de chefes/mobs excessivamente grandes.

## Mira e canais

O deslocamento vinha de projetar hitResult do raio dos olhos/corpo na câmera de ombro, enquanto mirar com botão direito trocava para o raio da câmera. O retículo agora permanece central. A seleção de blocos/criaturas usa o raio da câmera; interações mantêm os alcances nativos medidos dos olhos do jogador e exigem passagem desobstruída, sem permitir atacar/usar através de paredes. Raycast de interação tem alcance limitado à distância da câmera mais alcance vanilla.

O hook usa o descritor público pick(F)V, conferido na fonte do Minecraft alvo. Seleção é renovada também depois de setup da câmera atual. Mira de habilidades continua validada pelo servidor, com colisões e compensação já existente de projéteis. A visão frontal espelhada mantém a seleção nativa.

Lança-chamas deixa de conservar a direção inicial. LiveAim acompanha o canal do Mago, como o canhão aquático. Anéis usam sprites nativos do sonic boom com provider próprio vermelho/laranja, cercados por chamas. Atualização visual a cada cinco ticks; dano mantém pulsos a cada dez ticks, além do início do lançamento. Colisões e alcance originais permanecem.

## Efeitos, tremor e arma

- Corte Circular/primeira habilidade do Guerreiro apresenta varredura, faíscas e sons mesmo antes do nó principal; dano em área continua exigindo o desbloqueio correspondente.
- Botão direito da Espada do Gladiador: rajada, varredura, som de escudo/empurrão e resistência. Preset de arma data/oxyrpg/weapon_attributes/relic_guerreiro.json herda bettercombat:sword, igual ao contrato dos presets instalado, sem copiar animações ou substituir Better Combat.
- Invocações do Necromante sobre terreno sólido emitem dezoito fragmentos do bloco abaixo e o som correspondente; não destroem o terreno.
- Tremor escolhe até catorze blocos, ou vinte e dois com melhoria de alcance, distribuídos aleatoriamente no círculo de quatro/cinco blocos. Pulsos a cada dez ticks e estágios de rachadura escalonados, quebrando em cerca de um a um e meio segundos. Preserva lista de materiais, proteção de interação e exclusão de blocos com inventários. Dano de pulsos seguintes acompanha a frequência maior.
- Removida a seta lateral da esquiva. Barra segmentada e animação Oxy/PAL continuam; Combat Roll completo segue não instalado.

## Configurações e fechamento

R > Interface > Combate > Marcações no chão antes de lançar. Padrão desligado: habilidade sai ao pressionar; habilitada: segurar mostra a previsão e soltar lança. Chave groundPreview substitui o antigo padrão preview; instalações sem a nova chave começam desligadas. Outras opções de efeitos/HUD permanecem independentes. Avisos de ataques das elites permanecem separados.

Clique fora da moldura fecha inventário, baús, containers, comércio/missões e telas próprias. Botões fora da área nativa, livro de receitas e abas do criativo permanecem clicáveis. Usa onClose/rotina vanilla de fechamento para devolver o item segurado e encerrar a sessão de comércio/conversa. Esse clique passa a fechar a janela em vez da ação vanilla de jogar o item para fora. Telas principais do jogo não recebem o fechamento.

## Validação pendente

Confirmar manualmente: guardas/golens à noite; decolagem em criativo e retomada após toggles; disfarces de mobs passivos/hostis e retorno do enxame; câmera próxima a paredes, interação em alcance e canal do Mago ao mover o mouse; arma com Better Combat/Fresh Player; fechamento com item no cursor, receitas abertas e comércio com painel lateral. Não foram instalados novos complementos nesta versão.
