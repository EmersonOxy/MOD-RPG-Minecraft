# Opções externas de animação — consulta em 11/10/2026

Pesquisa apenas. Nenhuma biblioteca, pacote, opção de vídeo ou skin foi instalada/alterada. A seleção considera Minecraft 1.21.10 e NeoForge; compatibilidade publicada não substitui validação junto do Oxy RPG.

| Uso | Recurso | Situação consultada |
|---|---|---|
| Animações próprias das skills do player | [Player Animation Library](https://modrinth.com/mod/player-animation-library) | 1.1.3 NeoForge, versão Ciw2S8Ez, publicada para 1.21.9 **e 1.21.10**. Nome do arquivo contém 1.21.9. Biblioteca de desenvolvimento: não muda animações sozinha. Licença MIT. |
| Mobs vanilla | [Fresh Animations](https://modrinth.com/resourcepack/fresh-animations) | Pacote 1.10.4 (xN57JJts) listado para 1.21.10. Precisa de suporte a Custom Entity Models via EMF/ETF. Termos próprios do autor; não copiar assets para dentro do mod. |
| Modelos/expressões de pacotes | [EMF](https://modrinth.com/mod/entity-model-features) + [ETF](https://modrinth.com/mod/entitytexturefeatures) | EMF 3.3.11 (gunEHTty) e ETF 7.2.5 (TodzZznO), builds NeoForge consultadas no filtro 1.21.10. EMF requer ETF. LGPL-3.0-only. |
| Player pronto via pacote | [Fresh Moves](https://modrinth.com/resourcepack/tras-fresh-player) | Usa EMF/ETF, mas o projeto consultado não retornou nenhuma versão explicitamente marcada 1.21.10. Não tratar como compatibilidade confirmada. |
| Entidades próprias do mod | [GeckoLib](https://github.com/bernie-g/geckolib/wiki) | Já existe na instância. Para mobs com novos modelos/rigs e animações de entidades próprias; não transforma automaticamente todos os mobs/player vanilla. |

## Como aproveitar

Minha recomendação de implementação é PAL para gestos originais de conjuração, parry, esquiva e habilidades do Aviário, com transições e controle de prioridade. Ela carrega animações JSON exportadas de Blockbench/Blender em formatos GeckoLib/Bedrock e permite keyframes de efeitos. Isso permite sincronizar gesto e emissão de partículas. Exige animar e integrar cada ação, além de conciliar braços, itens, armadura e voo.

Fresh Animations é a opção de pacote para dar mais vida aos mobs existentes: marcha, movimentos de cabeça e expressões. Os avisos e tempos de ataques especiais dos elites continuam sendo sistemas do Oxy. Não substitui a criação de animações específicas de cada habilidade. Para inimigos com aparência inteiramente própria, usar os modelos e controladores do GeckoLib já presente.

Antes de incorporar, validar separadamente jogador com/sem armadura, uso de escudo/arco, Peso pena, meteoro e Barrel Roll; mobs durante preparação de habilidades; desempenho em grupos. A integração atual do Oxy mexe nos modelos Humanoid e no estado de voo, então não dá para afirmar ausência de conflitos apenas pelas versões publicadas.

Fontes primárias: [documentação PAL](https://docs.zigythebird.com/), [código PAL](https://github.com/PlayerAnimationLibrary/PlayerAnimationLibrary), [código EMF](https://github.com/Traben-0/Entity_Model_Features), páginas dos autores acima e API pública do Modrinth, filtrada com `game_versions=["1.21.10"]` e loader NeoForge quando aplicável. Evitar páginas de terceiros que se apresentam como sites oficiais dos pacotes.
