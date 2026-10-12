# Correções 0.51.1 — transporte, alcance, Trovão e interação

12/10/2026 · Minecraft 1.21.10 · NeoForge 21.10.64 · protocolo 36. Cliente e servidor devem usar esta versão. Documentos anteriores permanecem históricos.

## Aviário: causa e novo transporte

O código anterior chamava `mob.startRiding(player, true, true)`. A implementação do Minecraft 1.21.10 rejeita veículos cujo tipo não pode ser serializado, incluindo Player. Portanto, a criatura não era capturada; a aproximação podia deixar jogador e mob sobrepostos, como nos prints 19.06.32 e 19.06.38.

O transporte agora usa um vínculo próprio, sem transformar jogador ou criatura em montaria. O mob permanece com entidade/UUID originais e posição abaixo dos pés do jogador. Durante o transporte, IA, gravidade e empurrão contra o carregador são suspensos; flags originais são restauradas na soltura. As flags de restauração também acompanham o salvamento do mob, para recuperar seu comportamento se uma sessão for interrompida enquanto ele estiver sendo carregado. Nenhum mob é convertido em item ou removido do mundo para transportar.

Captura livre: em voo, passar perto de uma criatura à frente, a até 3,5 blocos, com visão e espaço suficiente abaixo do jogador. Captura marcada: marcar no ar, segurar Agachar para queda lenta, soltar Agachar e usar Decolagem novamente para retomar voo. A aproximação tem até seis segundos, usa o mesmo vetor no cliente e servidor e não é interrompida pelo pouso automático enquanto está ativa. Obstáculos permanecem físicos; chefes grandes e criaturas já transportadas/montadas são excluídos.

Ao capturar, há uma breve subida assistida de 0,8 s para afastar a carga do solo, sem girar a mira do jogador. F6 solta a criatura e a trava; o atalho é personalizável nos controles. Soltar dá dois segundos antes de outra captura automática. Pouso, água/lava, morte, troca de classe/dimensão ou obstáculo na posição da carga também encerram o transporte. A criatura solta retoma a queda a partir da altura atual. Validação visual e de sincronização em jogo continua pendente.

## Trava de mira

- Aviário no ar e Arqueiro novo: 64 blocos, tanto na seleção/manutenção do cliente quanto na validação de marca/alvo no servidor.
- Demais classes e Aviário no chão: 24 blocos.
- Criatura precisa estar carregada no mundo, viva e visível; não há geração forçada de chunks nem seleção através de paredes.
- O alcance da trava não altera alcance de interação com blocos, teleporte, dano de canais ou ataque corpo a corpo. Flechas guiadas usam o alvo marcado até 64 blocos no disparo.

## Deus do Trovão

Corrida contínua agora conta saltos enquanto o sprint permanece ativo, fora da água/lava e sem voo. Após três segundos ganha +75% de velocidade, crescendo gradualmente até +125% após oito segundos. Parar o sprint zera a contagem.

Pulso inicial dá +90%; habilidade principal Avatar dá +180%; melhoria Velocidade divina acrescenta 50 pontos percentuais. A corrida acumulada soma ao bônus da habilidade. Superpulo é aplicado pela força de salto: +0,30 inicial, +0,50 principal, mais +0,10 com Escalada elétrica. Modificadores são retirados quando a habilidade termina ou a classe muda; efeitos externos continuam independentes. Textos da árvore atualizados para esses valores e para Salto V do Arqueiro.

## Blocos e bússola

A validação da câmera de ombro comparava o ponto de impacto da câmera com o dos olhos e podia considerar obstrução quando os dois atingiam faces diferentes do mesmo bloco. Agora o mesmo bloco é aceito pela face alcançável a partir dos olhos. Alcance nativo e obstáculos diferentes continuam verificados. A criatura carregada não intercepta a seleção de blocos pelo próprio carregador. Colocar bloco num espaço ocupado pelo corpo ainda respeita a regra normal do Minecraft; colocar abaixo dos pés pode exigir saltar.

Bússola movida de y=10 para y=64 em coordenadas da interface, abaixo do painel padrão do Jade. Layouts personalizados do Jade ou tooltips muito extensos podem exigir outro ajuste.

## Evidência e validação

Inspecionados prints 2026-10-12_19.16.30, 19.07.24, 19.06.38 e 19.06.32; contrato nativo de montagem/posicionamento e fluxo de mira conferidos nas fontes locais. Compilação `build-windows.bat`/assemble. Sem testes automatizados, GameTests ou abertura do jogo, conforme AGENTS.md.

Verificação manual pendente: captura livre, marcada e soltura F6; carga passando perto de telhado/solo; queda e comportamento do mob depois da soltura; reabertura de mundo após salvamento com carga; trava a 40–64 blocos; corrida pulando; superpulo durante Avatar e fim do efeito; quebrar/colocar abaixo dos pés em primeira e terceira pessoa; posição da bússola com Jade.
