# 0.50.0 — classes, transporte aéreo e correções

Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21. Protocolo 33: cliente e servidor precisam da mesma versão.

## Classes e preservação de dados

O Arqueiro anterior passa a ser **Assassino**. Seu ID interno ARQUEIRO/ordinal 3 permanece estável: níveis, XP, nós, atributos, recibos e treinamentos existentes não são reiniciados. A árvore ganha textos e identidade de adagas; o primeiro projétil é uma adaga visual baseada em item nativo, a segunda habilidade mantém furtividade e a terceira mantém execução/arrancada. A passiva automática de agachar foi transferida para um talento do novo Arqueiro. Assassino usa somente armaduras leves (tag `oxyrpg:light_armor`, couro/elytra por padrão) e recebe +25% de dano de projéteis. Talentos compráveis preservam detecção reduzida e agilidade.

O equipamento vinculado usa schema 4 para reconstruir o antigo arco como Adaga das Sombras, respeitando o dono, recibos e etapas. Slot/inventário completo usa a rotina existente de preservação do item; não descarta a armadura para resolver restrições. Better Combat recebe um preset de adaga para a relíquia. O comando público passa a ser `/rpg classe assassino`; `/rpg classe arqueiro` seleciona a nova classe.

**Novo Arqueiro:** rajada de 2/6 flechas com lentidão/fraqueza e melhorias de quantidade, dano e veneno; chuva de 4/10 flechas sobre uma criatura marcada, com mais flechas e explosões de criaturas em raio de 2,5 blocos; Caçada concede velocidade, superpulo e esquiva sem cargas inclusive no ar. O desbloqueio principal da Caçada habilita flechas guiadas ao mirar/marcar criaturas, sem a rolagem RPG de erro, e lentidão ao acertar. Continuam valendo colisões, terreno, escudos e regras nativas de dano. Rajada também pode ser guiada durante Caçada principal. Chuva explosiva não destrói blocos. Cada flecha de habilidade consome munição, reservada antes de gastar mana; teste mantém custos, debug ilimitado/criativo dispensam munição.

**Deus do Trovão:** teleporte para o ponto da mira em alcance progressivo, com espaço/chunk carregado verificados antes de gastar mana, raio visual e dano no destino; corrente elétrica segue a mira e salta entre criaturas próximas; Avatar aumenta velocidade/altura de passo e acrescenta choque/lentidão aos golpes com ambas as mãos vazias, encadeando entre criaturas. Após três segundos de corrida em solo seco recebe velocidade adicional. Água impede nado e subida vertical; Nether/biomas de temperatura base ≥1,5 acumulam calor, causando dano após aproximadamente dez segundos. Criativo/espectador preservam seus controles nativos. Arcos elétricos enviam um pacote por segmento completo e geram partículas no cliente.

As duas classes novas foram acrescentadas ao fim do enum; nenhum ordinal antigo foi deslocado. Ambas têm 51 nós, disposição própria, itens vinculados, traduções, seleção/debug, custos, duração, recargas e treinamentos. Definições JSON acompanham os padrões compilados. Catálogo completo e sugestões: [CATALOGO_CLASSES_050.md](CATALOGO_CLASSES_050.md). O gerador `scripts/generate-class-catalog.py` usa as definições entregues ao jogo e registra todos os IDs/pré-requisitos.

## Prints e correções anteriores

Inspecionados os 23 prints desta etapa, das séries 15:46–16:24 de 12/10/2026, incluindo Terralith, canal do Mago, porcos, sangue, golens/guardas e voo. São evidências da versão anterior. Não comprovam funcionamento da 0.50.0.

- **Terralith:** botão observa o estado real da configuração de datapacks após validação nativa. O texto acompanha ativação/desativação; não apenas o clique inicial.
- **Lança-chamas:** mesmo espaçamento/estrutura do canal de água, com anéis próprios em escala de cinza coloridos em vermelho/laranja e pequenas chamas. Removida a multiplicação de órbitas genéricas. A textura cyan do sonic boom causava a coloração verde ao aplicar filtro quente; novos anéis são geometria procedural original, com fontes SVG e gerador PNG.
- **Mímico:** copia dados sincronizados visuais, equipamento, nome e vida máxima/proporção da referência. Preserva variantes nativas como porcos quentes/frios e aparência de dano dos golens. Não copia UUID, posição, cérebro, navegação ou propriedade. Modelo do jogador continua substituído por um renderizador, sem criar AI física sobre ele. Cópias continuam mobs separados.
- **Sangue:** marcas podem atravessar limites entre faces coplanares de blocos vizinhos. Validam apoio em cada superfície, conservando restrições em bordas reais, água e degraus. Não há nova limitação artística de quantidade de sangue.
- **Esquiva geral:** níveis melhoram cargas, não alcance. Movimento normal fica limitado a um raio de três blocos desde o início, com colisões e previsão no cliente. A Execução do Assassino continua sendo habilidade separada, com alcance próprio de cinco a seis e meio blocos.
- **Vampiresco:** instrução de alimentação aparece até três refeições bem-sucedidas, com contador persistente; depois fica apenas o ícone de presas. Não muda a chance de 35% de sangramento da mordida.
- **Defensores:** sair de Vampiresco limpa alvo/vingança nos defensores carregados que perseguiam esse jogador. Guardas e golens escolhem ameaça visível mais próxima, podendo priorizar um hostil ao vampiro. Navegação de perseguição dos guardas reduzida em 15%.
- **Câmera:** posição dos olhos usa a interpolação nativa de altura; a saída do voo com Barrel Roll reinicia a transição a partir da distância da câmera nativa, suavizando a retomada da câmera de ombro.
- **Aviário/pouso:** mergulho perto de terreno ou obstáculo interrompe a planagem e aplica freio horizontal/vertical, com proteção contra queda/colisão durante a transição. Quedas normais continuam recebendo 70% do dano.
- **Aviário/transporte:** voando, marcar uma criatura e passar a até 3,5 blocos permite carregá-la abaixo do jogador; destravar a mira/pousar solta. Retoma gravidade/dano de queda natural da criatura. Chefes Wither/Dragão e criaturas já montadas/transportando outras ficam excluídos. Alvo carregado não força a câmera a olhar para os pés.
- **Esc:** opção de movimento em containers agora inclui o menu Esc. Com ela ligada o mundo continua e as teclas de movimento/corrida funcionam; campos de texto e perda de foco continuam suspendendo leitura. Com ela desligada Esc volta à pausa nativa em singleplayer.
- **Descrição do Tremor:** texto alinhado ao comportamento existente: até 14 blocos naturais, ou 22 com Epicentro; não quatro.

## Verificação e limites

Executado apenas `build-windows.bat`/assemble, conforme AGENTS.md. Conferidos descritores e contratos nas fontes/bytecode do Minecraft/NeoForge locais. Sem testes automatizados, GameTests ou abertura do jogo. Não foram instalados novos mods auxiliares nesta etapa. O inventário de hashes registra arquivos; não constitui revisão funcional de todo arquivo.

Validação manual pendente: inicialização com os novos mixins; escolha das 12 classes; migração de personagem/kit antigo; falta de munição sem gasto de mana; flechas guiadas e chuva sob coberturas; fraquezas de água/calor e teleporte; transporte/soltura de diferentes criaturas; pouso no sobrevivência; transições de câmera; variantes/vida dos disfarces; Terralith ao ligar/desligar; interface Esc; prioridades de NPCs ao trocar de classe. O catálogo distingue sugestões futuras das mecânicas implementadas.
