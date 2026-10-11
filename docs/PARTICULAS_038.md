# Partículas e marcas — Oxy RPG 0.38.0

Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21. Protocolo 26.

## Efeitos

- **Sangue:** gotas vermelhas com gravidade em dano positivo concluído pelo servidor a jogadores e mobs, inclusive pacíficos. Golpes, projéteis, magia com autor, explosões e quedas podem provocar sangue. Erros, bloqueio completo, invulnerabilidade e dano ambiental sem autor (como fome/afogamento) não emitem. A quantidade aumenta com o dano, com teto. Não modifica dano, loot ou progressão.
- **Marcas:** gotas que atingem uma superfície deixam uma mancha fixa, orientada pela face atingida, tanto no chão como em paredes e tetos. São quads do renderizador nativo de partículas, iluminados e ocultados pela geometria do mundo, não desenhos sobre a HUD. A forma irregular reaproveita um sprite nativo tingido. Cantos são verificados contra a mesma superfície de colisão para evitar marcas suspensas em escadas, lajes e cercas.
- **Fumaça:** nuvens que crescem, sobem e esmaecem. Aparecem em impactos de habilidades, explosões nativas (TNT/creepers inclusive), tremor do Mago, meteoro e início da decolagem do Aviário. O boost de voo não gera uma nova decolagem. Jatos de água produzem um pequeno efeito de névoa no impacto.
- **Impacto:** estilhaços visuais breves em explosões/impactos, tremor, meteoro e golpes fortes que causam pelo menos seis pontos de dano com contato direto de um ser vivo a uma vítima no chão. Golpes carregados (85% ou mais) com espada, machado ou maça contra blocos sólidos também emitem; mineração com picareta não dispara o efeito. Há limite de um efeito por jogador a cada 12 ticks contra blocos. As partículas não destroem blocos adicionais.

O sangue é estilizado, sem feridas ou modelos anatômicos. Todos os mobs usam o mesmo efeito nesta entrega; materiais específicos para esqueletos, construtos e criaturas mágicas ficam para uma extensão futura.

## Configuração

**R > Interface > Combate > Animações e efeitos**:

- Sangue, marcas de sangue, fumaça e impactos: controles individuais.
- Intensidade: 50%, 100% ou 150%.
- Duração das marcas novas: 15, 30, 60 ou 120 segundos; padrão de 60 segundos. Escurecem e esmaecem no quarto final da duração.
- As opções anteriores de gestos, efeitos de classe e enxame continuam independentes.

Arquivo `config/oxyrpg-combat.properties`, com preservação das opções existentes. Desligar sangue também remove marcas. Marcas não são blocos e não ficam gravadas nos saves: são cosméticos temporários de cada cliente, desaparecem ao trocar de mundo, afastar-se mais de 32 blocos, molhar a superfície ou remover/alterar o bloco de suporte. Chuva também limpa manchas expostas. Alterar a duração afeta marcas criadas depois da alteração.

## Implementação e limites

- Três tipos registrados: `oxyrpg:blood`, `oxyrpg:smoke`, `oxyrpg:impact`. Providers próprios para a API de partículas de **1.21.10**, com extração de `SingleQuadParticle` para `QuadParticleRenderState`.
- Os JSONs apontam para sprites do atlas nativo (`generic_7..0`, `critical_hit`). Nenhum PNG vanilla foi copiado e nenhuma biblioteca foi acrescentada.
- `BloodFx`: mensagem de evento com posição, direção e dano. Um evento se expande em partículas no cliente; não há pacotes por gota ou marca. Dano por vítima limitado a um evento a cada quatro ticks e máximo de 64 eventos cosméticos por dimensão/tick.
- Emissão local compartilhada: até 64 partículas por tick; limites de partículas simultâneas: 96 gotas, 128 nuvens, 64 estilhaços e 160 marcas. Raycasts de gotas: até 48 por tick; excesso é descartado.
- Distância: até 32 blocos da câmera; apenas chunks carregados. Opção nativa de partículas Reduzidas reduz emissão pela metade; Mínimas suprime estes cosméticos. Indicadores de ataques dos elites permanecem independentes.
- Vida máxima: gotas 28 ticks; fumaça 32–48; impacto 8–13. Marcas verificam suporte/fluido/chuva a cada dez ticks. Nenhuma entidade persistente ou bloco extra criado.
- Rede: servidor e cliente devem usar 0.38.0/protocolo 26.

## Verificação

Compilação pelo `build-windows.bat` (`assemble`), leitura da API local da versão exata, revisão de registro/provider, JSONs, separação cliente/servidor, colisões, limites e empacotamento. Testes automatizados e jogo não são executados conforme AGENTS.md. A aparência, direção dos respingos, densidade e leitura em primeira/terceira pessoa ainda dependem da validação pessoal no jogo.

Roteiro pessoal sugerido: receber e causar dano com espada/flecha; verificar erro/parry sem sangue; observar manchas em pedra, laje, parede e escada; quebrar ou molhar o suporte; testar TNT, tremor, decolagem e meteoro frontal; alternar os controles e as opções nativas de partículas.
