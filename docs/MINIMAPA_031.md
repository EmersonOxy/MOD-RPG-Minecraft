# Minimap 0.31.0

R > Minimapa abre opções com prévia. M liga/desliga. Atalho Configurar minimapa é remapeável em Opções > Controles, inicialmente sem tecla para evitar conflitos.

Novo terreno 64x64, relevo sombreado, uma textura dinâmica, moldura discreta, bússola com norte fixo e seta do jogador. Zoom perto/médio/longe corresponde a 64/128/256 blocos de largura. Tamanho 72–160 pixels e opacidade 35–100%. Coordenadas XYZ opcionais. Somente chunks já carregados; não gera terreno. Amostra até 128 pontos por tick, cache limitado a 16.384 pontos, atualização visual em lotes e descarte da textura ao trocar de dimensão ou zoom.

Escolha entre quatro cantos ou Mover na tela para arrastar. Posição normalizada permanece dentro da tela ao mudar resolução/escala de GUI. Evitar efeitos calcula as duas linhas vanilla de efeitos visíveis e move o mapa para preservar os ícones. Configuração independente salva em config/oxyrpg-minimap.properties; Restaurar padrão reinicia os ajustes do mapa. Visibilidade continua vinculada ao HUD Oxy. Mapas em dimensões com teto mostram Sem superfície.

Referências de comportamento e visual (implementação própria):
- Xaero: https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap — posicionamento configurável e convivência com efeitos.
- JourneyMap: https://teamjm.github.io/journeymap-docs/latest/client/settings/minimap/ — tamanho, opacidade, posição e informações configuráveis.

As atualizações 0.27 a 0.31 estão incluídas neste checkpoint. Patch reproduzível do port GPL separado em integrations/barrel-roll; ajuste de metadado YACL em integrations/yacl.

Validação: assemble, sem testes ou abertura do jogo conforme AGENTS.md. Validação interativa pelo usuário.
