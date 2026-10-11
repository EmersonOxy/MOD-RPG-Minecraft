# Oxy RPG 0.34.0 — correções de exploração

Minecraft 1.21.10 / NeoForge 21.10.64 / protocolo 23. Revisão do print 2026-10-11_11.19.20.png e do log da sessão do usuário; não foi aberto o jogo para validação.

## Minimap

A 0.33 consultava OCEAN_FLOOR para detectar cavernas. Esse heightmap não é enviado ao cliente; o valor vazio mantinha o mapa de superfície. A 0.34 usa MOTION_BLOCKING, sincronizado nos chunks do cliente, e amostra a fatia na altura dos pés. Amostragem começa pelo centro e preserva pixels apenas entre alturas do mesmo modo, evitando misturar superfície e caverna.

R > Minimapa > Camada: automática / superfície / subsolo. O modo subsolo força a fatia local para facilitar comparação pessoal. Chunks não carregados continuam desconhecidos e nenhuma geração é disparada pelo mapa.

## Movimento em 360°

O modo independente estava ativo no arquivo de configuração do usuário. A orientação do corpo usava velocidade/teclas brutas, e a corrida continuava exigindo impulso para frente pelas regras vanilla. Agora o corpo usa a entrada efetiva do jogador, inclusive lateral e diagonal, e o modo independente em terceira pessoa aceita corrida lateral/para trás. Alimentação, colisão, uso de itens e restrições vanilla continuam em vigor. Primeira pessoa, voo acrobático, água e montarias preservam os respectivos controles. A câmera e a direção autoritativa dos ataques continuam sendo a mira; não são giradas junto com o corpo visível.

## Patrulhas

- O criativo deixou de impedir encontros: é possível encontrá-los ao explorar para avaliar o mod. Espectador, Pacífico e doMobSpawning=false continuam excluídos.
- Vegetação substituível não invalida mais todo ponto de spawn. Cada integrante consulta sua própria altura no terreno, sem exigir um platô.
- Primeira tentativa: 30–60 s; nova tentativa após falha: 20 s; intervalo após sucesso: 5 min. O intervalo antigo é migrado uma vez por jogador, evitando aguardar o prazo da 0.33.
- Mantidos limite de grupo próximo, distância de jogadores, proteção de vilas, recompensas por nível e geração somente em chunks carregados.

## Vilas maiores e anexos conectados

As cinco variantes vanilla passam de profundidade de geração 6 para 8 e de raio máximo 80 para 112 blocos. Isso amplia o limite de expansão; tamanho final depende de terreno, caminhos e escolhas dos pools.

Três estruturas originais entram nos pools de construções: torre de vigia, ruínas e entreposto de estrada. Possuem conectores building_entrance para se ligarem às ruas da vila, com caminho de acesso. As estruturas NBT são reproduzíveis por tools/generate_village_annexes.py, usando apenas blocos Minecraft. Não introduzem itens ou entidades novos.

**Afeta vilas geradas em novos chunks. Não modifica construções existentes.** A ligação implementada é entre ruas e esses anexos; não existe ainda uma rede de estradas entre vilas distantes ou quaisquer estruturas arbitrárias do mundo.

[Towns and Towers](https://modrinth.com/datapack/towns-and-towers) foi consultado como referência para vilas e postos variados. Não foi instalado nesta atualização; Explorify/Structory continuam separados no pacote existente.

O log da sessão também mostrava pool inexistente `structory:harvest/well_bottoms`: o JAR oficial instalado contém a definição sem extensão .json. Um pequeno pool de compatibilidade referencia o template já fornecido pelo Structory, corrigindo o nome de recurso sem redistribuir a estrutura dos autores.

## Interface e itens

Bússola sem caixa de fundo e sem linha longa, com N/S/L/O, poucos traços e transparência progressiva nas extremidades. O marcador central ocupa apenas um pixel de largura. Usa a direção da câmera.

Nenhum item novo adicionado. Lâminas, acessórios e bandagem reaproveitam modelos/texturas Minecraft; as lâminas recebem cor própria via definição do item. Mecânicas, IDs, receitas, slots e dados existentes são mantidos.

## Validação

Build Windows assemble; sem testes automatizados, GameTests ou execução do cliente, conforme AGENTS.md. A compilação não comprova os resultados visuais, a geração de anexos ou os encontros na sessão real. Instalação deve ocorrer apenas com Minecraft fechado, backup e verificação do JAR.

Instalado com o jogo fechado, saves/config e JAR anterior preservados. Registro e SHA-256 em [INSTALACAO_034.json](INSTALACAO_034.json).
