# Correções 0.51.2 — queda lenta, Meteoro e bússola

12/10/2026 · Minecraft 1.21.10 · NeoForge 21.10.64 · protocolo 37. Cliente e servidor precisam desta versão.

## Transporte durante Peso pena

A validação do transporte exigia planagem ou voo criativo. Peso pena interrompe a animação de planagem para deixar o jogador reto, por isso a criatura era solta ao usar Agachar (Ctrl na configuração do usuário). O transporte agora também permanece válido durante a queda lenta passiva. Soltar a tecla e retomar o voo preserva a mesma criatura; F6 continua sendo a soltura manual. Pousar, colidir a carga com terreno, entrar em água/lava, morrer ou trocar de classe/dimensão ainda encerra o transporte.

## Impacto Meteoro

A versão em voo aplicava a cada tick o vetor guardado na ativação, em cliente e servidor, disputando a física normal e o controle Barrel Roll. Agora prepara o impacto e mantém direção, impulso e controles normais do voo. Preserva a duração restante de Decolagem e seus impulsos; o Meteoro em queda continua sendo um mergulho. Expirar a preparação não interrompe Decolagem ainda ativa.

Criatura transportada foi excluída da detecção de contato e do dano em área do próprio carregador. A detecção não avança mais 0,35 bloco além do percurso real; só verifica criaturas após movimento e blocos efetivamente em contato, sem aproveitar flags antigas de colisão isoladamente. Não verifica contato aéreo no tick da ativação. Isso evita a carga ou a proximidade de outro mob detonarem a habilidade imediatamente.

Colidir de fato com outra criatura no ar ainda pode produzir impacto: esse comportamento preserva a versão horizontal solicitada anteriormente. O jogador precisa atravessar a área física do alvo; estar próximo ao ativar não basta. Colisão com chão/estrutura continua causando explosão/cratera conforme força e desbloqueios.

## Bússola e exploração

O print 2026-10-12_19.49.16 mostra a bússola distante do painel Jade. Sua altura padrão passou de 64 para 32 unidades da interface; a posição agora é ajustável por barra de 24 a 56.

A bússola mantém visual simples, com oito direções, subdivisões de 15°, ponteiro central e graus opcionais na convenção norte=0°, leste=90°, sul=180°, oeste=270°. O alvo travado aparece com marcador azul. Um local escolhido pelo jogador aparece em dourado, acompanhado de distância; marcadores fora da faixa visível indicam o lado para virar. O local marcado só é exibido no mesmo mundo/servidor e dimensão e fica salvo nas preferências locais.

**R → Interface → Bússola** abre os ajustes: ligar/desligar, altura, mostrar/ocultar graus, marcar o local atual, limpar marcador e manter os avisos de região independentes. A opção de desligar já existia sob “Explorar”; o nome e o estado ficaram explícitos.

Não há busca automática de vilas, tesouros ou estruturas. A bússola não consulta chunks nem envia pacotes por frame; o marcador usa posição conhecida e a trava já existente.

## Verificação

Compilação pelo build-windows.bat/assemble; revisão dos fluxos de transporte, planagem, contato e payload. Sem testes automatizados, GameTests ou abertura do Minecraft, conforme AGENTS.md. A auditoria PvPvE da 0.50.1 permanece inalterada.

Validação manual pendente: capturar e usar Ctrl/Peso pena, retomar voo sem perder a carga, F6, Meteoro durante voo com Barrel Roll e carga, ativar perto de mobs sem tocá-los, impacto físico contra criatura/parede/chão, expiração do Meteoro, altura e desligamento da bússola, direção/graus ao girar e marcador em outro mundo/dimensão.
