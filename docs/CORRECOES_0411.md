# Correções do Arqueiro e avisos — 0.41.1

Prints inspecionados: 2026-10-11_19.41.27, 19.39.46 e 19.36.33. Nos dois primeiros, o chat repete “Olho do Caçador”; a origem é o item Arco do Caçador, acionado pelo botão direito, e não a skill de flecha invocada com Z.

## Munição e recursos

HunterBow acionava ClassRelics.use antes de BowItem.use: o efeito cobrava 10 MP e só depois o arco vanilla rejeitava a tentativa sem munição. Agora a falta de flechas retorna FAIL antes da ativação. O efeito só é acionado se o nock nativo foi aceito; hooks que cancelam o nock não cobram. A entrada compartilhada ClassRelics.use também verifica munição. Criativo mantém o comportamento vanilla sem necessidade de flechas. Uma marca do Caçador ainda pendente não cobra novamente ao começar outra puxada do arco.

A skill Z mantém sua flecha especial conjurada, independente da munição do arco. Não alteramos seu custo para introduzir outro requisito.

## Sangramento com mãos vazias

WeaponTuning já tinha chance zero para punhos. A aplicação vinha da passiva Ferida leve/Assassino do Arqueiro, que aceitava qualquer dano causado pelo jogador. Agora essa aplicação exige uma flecha ou golpe direto com espada/machado (tags nativas). Punhos, arco usado para bater e outros objetos não ativam essa passiva. As chances por arma continuam sendo aplicadas apenas após dano físico efetivo.

## Chat

Mensagens rotineiras de skills, efeitos de itens, recuperação, restrições, trabalhos e progressão passaram à barra de ação. O aviso continua legível sem acumular histórico no chat. Resumos explícitos dos comandos de debug/elite e mensagens vanilla, como captura de tela salva, não foram alterados. O menu continua recebendo seu aviso sincronizado.

## Illagers no gelo

O efeito Bleeding não altera velocidade, navegação ou posição. Encontramos um comportamento nativo evitável: dano periódico com autor renovava lastHurtByMobTimestamp e a reação de agressão. O tipo de dano passou à tag `minecraft:no_anger`, preservando o autor/crédito de morte sem reaplicar essa reação a cada segundo.

Não é possível afirmar pelos prints que isso causou a imobilidade. Não há evidência de que o gelo seja a causa. Criativo/espectador, invisibilidade do Arqueiro, lentidão breve do Assassino e preparações de elites também podem mudar a perseguição. A IA foi preservada; não forçamos alvos ou movimento em criaturas que deveriam ignorar o jogador.

Fontes locais exatas de Minecraft/NeoForge 1.21.10: BowItem.use, Player.getProjectile, LivingEntity.hurtServer/resolveMobResponsibleForDamage e HurtByTargetGoal. Compilação assemble; sem testes automatizados ou abertura do jogo. Verificação manual pendente: arco sem flechas não consome mana/recarga, punhos não abrem feridas, avisos fora do chat e perseguição de um Vindicator em sobrevivência sem invisibilidade, comparando gelo e terra.
