# Correções 0.51.0 — mira, voo, vegetação e apresentação

12/10/2026 · Minecraft 1.21.10 · NeoForge 21.10.64 · protocolo 35. Cliente e servidor precisam desta versão. A auditoria PvPvE existente permanece um retrato da 0.50.1; este documento registra as mudanças posteriores.

## Mira e Deus do Trovão

- A rajada do Arqueiro armazenava a direção do primeiro disparo. Agora cada flecha lê a mira atual, atualizada durante habilidades ativas a cada dois ticks. Canais e rajadas acompanham a mira; uma flecha comum já lançada mantém sua trajetória. Flechas guiadas acompanham a criatura selecionada. Essa regra foi registrada no AGENTS.md.
- O teleporte exibe destino, distância e indicação de destino bloqueado enquanto sua tecla é segurada; soltar executa a habilidade. A prévia permanece disponível mesmo com marcações gerais desativadas. Cliente e servidor compartilham o cálculo de alcance, colisão e espaço livre; movimento do jogador ou alterações do mundo ainda podem alterar o resultado entre prévia e execução.
- O canal elétrico e as correntes usam geometria de raio com o renderizador de relâmpagos vanilla e dano do tipo elétrico. Removidas as faíscas grandes na trajetória. Segmentos próximos à câmera são omitidos para evitar flashes em primeira pessoa. O canal não cria relâmpagos incendiários no terreno.
- Velocidade do estado de tempestade: +50% inicial, +100% com habilidade principal e mais +30% com melhoria. Passiva após três segundos correndo: +45%. Esses modificadores somam entre si, antes de efeitos externos.

## Arqueiro

- Salto da terceira habilidade aumentado para Salto IV, ou V com melhoria.
- Flechas guiadas corrigem a direção para o centro da caixa do alvo, incluindo Vex. Um segmento de colisão até o próximo passo resolve contato com alvos pequenos usando impacto nativo e eventos NeoForge, sem atravessar blocos.
- Durante a habilidade principal de pontaria, a HUD indica o alvo identificado ou orienta a mirar/travar uma criatura. A assistência precisa de alvo vivo e visível no disparo; não garante dano contra escudo, imunidade, bloqueio ou alvo removido. Não elimina a necessidade de munição.

## Aviário

- Em voo, passar perto de uma criatura à frente permite capturá-la sem trava. Distância máxima de captura: 3,5 blocos; exige visão e espaço livre para o passageiro. Chefes grandes são excluídos.
- Marcar um alvo durante queda lenta e retomar o voo inicia uma aproximação de até três segundos antes da captura. A aproximação usa velocidade normal com colisões, não teleporte.
- Uma vez capturada, a criatura permanece transportada sem precisar manter a câmera travada. F6 libera a carga e a trava, com dois segundos de intervalo antes da captura automática seguinte. O atalho está registrado e pode ser alterado nos controles do Minecraft.
- Parar de voar também libera a criatura, que volta à física de queda. A soltura reinicia a distância de queda, evitando somar a viagem anterior.

## Esquiva e câmeras

- Avanço limitado a três blocos, distribuído por uma curva suave ao longo dos 13 ticks da animação existente; remove o impulso excessivo seguido de parada. Mantém colisões, recarga e seis ticks de proteção atuais.
- Trocar primeira/terceira pessoa reinicia deslocamento, colisão e suavização da perspectiva. A integração Barrel Roll também reinicia sua rotação nas trocas de perspectiva e transições de voo.

## Vampiresco, Ilusionista e vegetação

- Removido o dano próprio de chuva. A chuva continua protegendo do sol; dano solar e fraqueza diurna permanecem.
- Overlays de fome, saturação e previsão de cura do AppleSkin são ocultados apenas no Vampiresco, preservando a sede vermelha e a interface das outras classes.
- Avatar e cópias do Ilusionista usam a mesma fonte visual e UUID exclusivamente no modelo temporário de renderização, mantendo variantes dependentes de ETF/EMF. Entidades reais mantêm UUIDs únicos, movimento e IA próprios.
- O Sodium usa seu próprio pipeline de blocos e ignorava o hook vanilla que substitui a grama pressionada. Adicionado mixin opcional ao pipeline Sodium para omitir a vegetação original quando o modelo dinâmico está ativo. A reconstrução assíncrona da seção ainda pode ter uma transição curta; precisa ser observada no jogo.

## Verificação e validação pendente

Inspecionados prints 2026-10-12_18.43.43, 18.39.34, 18.35.32, 18.34.33, 18.17.18 e 18.06.48. Assinaturas locais do Sodium 0.7.3, IDs das camadas AppleSkin e API nativa de relâmpagos/colisão foram conferidos. Compilação pelo build-windows.bat (assemble); nenhum teste automatizado, GameTest ou abertura de jogo, conforme instrução do projeto.

Validação manual: grama com Sodium; teleporte em obstáculos; canal elétrico em primeira pessoa; rajada movendo a mira; pontaria contra Vex móvel e parede; salto; captura livre e aproximação após queda lenta; F6; esquiva contra paredes; chuva/HUD do Vampiresco; cópias com variantes e troca de perspectiva durante Barrel Roll. Compilação confirma APIs e empacotamento, não confirma a inicialização dos mixins nem o resultado visual no jogo.
