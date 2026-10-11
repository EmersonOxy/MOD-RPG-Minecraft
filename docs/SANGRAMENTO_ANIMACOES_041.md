# Sangramento e pacote de animações — 0.41.0

Minecraft 1.21.10, NeoForge 21.10.64, protocolo 28. Revisão detalhada dos caminhos alterados; compilação assemble, sem testes automatizados ou abertura do jogo.

## Sangramento

Efeito nocivo `oxyrpg:bleeding`, persistido pelo sistema nativo de efeitos em jogadores e criaturas. Não expira sozinho: bandagens estancam; leite não estanca. Morte e comandos administrativos continuam funcionando normalmente. Três intensidades causam 0,5 / 1 / 1,5 ponto de dano por segundo antes das resistências nativas (dois pontos = um coração). Feridas não acumulam instâncias; o efeito mais forte prevalece. Pode matar. Criativo/espectador não recebem dano nem rastro.

O dano tem tipo próprio, ignora armadura, escudo e janela de invulnerabilidade dos golpes, e não empurra. Esquiva, críticos, roubo de vida, procs de armas/classes e aprendizado de golpes não são reativados pelo dano periódico. O autor é persistido por UUID para crédito quando disponível; a ferida continua se ele desconectar. O efeito ativo gera rastros a cada quatro ticks, com partículas existentes nos pés e suas marcas em superfícies, respeitando as opções de sangue/marcas. Não existe nova varredura de entidades nem cache de partículas adicional. Marcas continuam transitórias conforme a duração configurada.

Somente acertos físicos efetivos podem aplicar a chance da arma; erro/parry/bloqueio total não aplicam. As mesmas regras valem para armas empunhadas por mobs, incluindo ataques ao jogador. Dados opcionais `bleed_chance` e `bleed_severity` em weapon_tuning; armas de outros mods podem receber entradas em datapacks.

| Arma | Chance | Intensidade |
|---|---:|---:|
| Espada de ferro | 8% | I |
| Espada de diamante | 12% | I |
| Espada de netherita | 18% | II |
| Machado de ferro | 12% | I |
| Machado de diamante | 16% | I |
| Machado de netherita | 20% | II |

Chance e dano base aparecem no tooltip. O sangramento antigo do Arqueiro foi migrado: Ferida leve I, Corte profundo/Assassino II e Passo duplo durante Assassino III. Não há duas rotinas de dano coexistindo. A futura classe dedicada ainda não foi criada.

## Bandagens

A bandagem existente mantém receita, aparência, cura de dois corações e grupo de recarga de oito segundos compartilhado com antídotos. Consumir estanca ao completar o uso. Botão direito numa criatura ferida aplica uma bandagem e cura; consome uma unidade e entra em recarga (criativo e recursos ilimitados de debug preservam a unidade consumida/aplicada). Villagers feridos permitem esse tratamento antes da abertura do diálogo. A ação nativa Usar Item continua personalizável nas configurações.

Comandos administrativos para experimentar: `/effect give @s oxyrpg:bleeding infinite 0` e `/give @s oxyrpg:bandage`. Intensidades II/III usam amplificador 1/2.

## Animações externas

Pacote original [Fresh Animations](https://modrinth.com/resourcepack/fresh-animations) 1.10.4, com [EMF](https://modrinth.com/mod/entity-model-features) 3.3.11 e [ETF](https://modrinth.com/mod/entitytexturefeatures) 7.2.5. As versões NeoForge estão explicitamente marcadas para 1.21.10, embora os nomes dos JARs mencionem 1.21.9. Metadados, dependências e SHA-512 dos arquivos oficiais: ANIMACOES_PACOTE_041.json. O resourcepack possui formatos suportados 15–999, incluindo o formato 69 desta versão.

Instalação externa na instância local, sem copiar modelos/animações para o JAR ou versionar o ZIP. EMF requer ETF >=7.2.0. O resourcepack atua nos mobs cobertos pelo autor. Não instala novas animações de combate do jogador; gestos nativos do Oxy e Barrel Roll seguem sendo os sistemas atuais. Mods de combate/player serão avaliados em uma etapa posterior se necessário.

## Validação manual pendente

Confirmar movimentos de mobs com Fresh Animations; manter aparência/gestos de conjuração e voo do jogador. Verificar sangramento aplicado por espada e por inimigo armado, rastro ao caminhar/parado, persistência após relogar, cura por consumo/aplicação, leite mantendo ferida e sem dano duplicado/crítico. Nenhum desses comportamentos foi confirmado no jogo nesta entrega.
