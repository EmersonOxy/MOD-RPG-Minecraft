# Sangue abundante e caches do minimapa — 0.40.0

Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21; protocolo 27, sem mudança no formato dos pacotes desta entrega.

## Evidências da sessão

Foram vistos os sete prints mais recentes da instância, entre 17:58:44 e 18:21:25 de 11/10/2026 (horários de São Paulo): acertos com sangue/fumaça, Aviário e sala com marcas nas paredes/piso. A sequência mostra grandes mudanças/revelação de pixels do minimapa. As capturas são anteriores à instalação da 0.39 às 18:44; portanto não constituem validação dessa versão. O log mais recente também terminou antes dessa instalação. Não foram encontrados prints novos do Nether/End nesta sequência.

## Sangue

- Retirado o corte de dano em 40 no servidor, o teto de 40 gotas no cliente e a rejeição de mensagens com dano maior que 40.
- Quantidade solicitada: `ceil(dano real × 1,5 × intensidade × preferência de partículas)`. Não há teto artístico do mod por golpe. Ex.: 100 de dano solicita 150 gotas em intensidade 100%/partículas completas; 1.000 solicita 1.500.
- Retirados os limitadores próprios de 96 gotas e 160 manchas simultâneas, bem como o descarte de sangue causado pelo limite compartilhado de raycasts. Acertos positivos consecutivos também deixam de ser ignorados por uma janela de quatro ticks.
- Golpes grandes geram respingos mais abertos. Uma fila em lotes de até 128 gotas/tick, alternando grupos de 16 entre acertos, conserva o volume solicitado em vez de descartar o excedente por tick. Portanto dano enorme pode continuar jorrando por vários ticks.
- A fila é transitória e descartada ao trocar de mundo, desligar sangue ou sair da região visível do evento. Sangue continua respeitando chunks carregados, distância de emissão e a preferência nativa de partículas.
- Marcas deixam de desaparecer por ultrapassar 32 blocos da câmera; permanecem enquanto o chunk está carregado e durante a duração configurada. Mantidos fade, iluminação, orientação por face e limpeza por chuva/água/alteração do suporte.
- A aparência dos sprites e cores aprovadas pelo usuário foi preservada. Fumaça/impactos continuam sem gatilho de ataques normais.

Isso remove os tetos próprios do Oxy RPG, não transforma o motor em memória infinita: a API nativa da 1.21.10 usa uma fila de até 16.384 partículas por grupo, compartilhada também com outros efeitos, e pode substituir as mais antigas. A duração das manchas e as opções de desligar/intensidade continuam válidas. Não há gravação de manchas no save.

## Minimap

- Cache da superfície separado por modalidade, sempre com a mesma chave independente do Y do jogador.
- Até quatro caches de modalidades/pisos recentemente usados. Mudança real de piso troca o cache sem apagar a superfície ou outros pisos recentes. Só troca de mundo/dimensão, escala de amostragem ou reset explícito descarta todos.
- Mantidas âncora no piso, tolerância de cinco blocos e confirmação de vinte ticks da 0.39; saltos e pequenas variações de altura não escolhem novas camadas.
- Telhados finos, copas e troncos não bastam para selecionar modo subterrâneo automático: teto sólido precisa ter três blocos consecutivos. Manual Superfície/Subsolo continua disponível.
- No Nether, a escolha por teto considera estar abaixo da superfície superior do terreno; acima do teto não alterna camada a cada altura. End usa céu/teto como as demais dimensões.
- Sombreado subterrâneo deixa de depender da ordem de chegada de amostras vizinhas, evitando mudanças de brilho em atualizações do cache.
- Permanecem amostragem limitada por tick/tempo, textura 64×64, somente chunks carregados e reconstrução gradual de locais ainda não visitados.

O mapa continua mostrando pisos locais. Mudanças reais de andar ou entrada/saída de cavernas ainda devem mudar o desenho; atualizações de chunks e alterações do terreno também continuam sendo refletidas.

## Ideia para animações (planejamento, sem instalação)

1. Parry e esquiva: defesa curta com escudo e passo/rolamento que coincidam com a janela real de proteção. Preservar controles e direção de movimentação.
2. Conjuração e Aviário: poses distintas por classe, transição de lançamento vertical para planagem, freio aéreo ereto e investida/queda do meteoro. Partículas sincronizadas com o momento visual de contato.
3. Elites: antecipação antes do golpe, ação e recuperação; animações que tornem claro quando esquivar ou contra-atacar. Depois ampliar movimento/expressões dos mobs comuns.

Player Animation Library é a candidata para animações próprias do jogador; Fresh Animations + EMF/ETF para mobs existentes; GeckoLib já presente para futuras entidades próprias. [Pesquisa de versões, fontes e compatibilidade](ANIMACOES_EXTERNAS_039.md). A autorização vigente é apenas pesquisar recursos externos. Nenhum pacote ou biblioteca novo foi instalado e essas novas animações ainda não foram implementadas.

## Verificação

Assemble por build-windows.bat, conferência dos JSONs, análise do caminho de sangue/filas/marks, caches e API nativa de partículas. Nenhum teste automatizado ou execução do Minecraft. Validação de imagem/movimento e comportamento nas dimensões permanece pessoal.
