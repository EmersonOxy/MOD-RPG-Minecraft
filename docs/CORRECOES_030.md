# Oxy RPG 0.30.0

## Escolha de classe

A criação `OwoUIAdapter.create(screen, ...)` já registrava o seletor como filho da tela. O registro manual posterior duplicava esse seletor e mantinha sua primeira instância antes dos botões no hit-test. Agora `createWithoutScreen(0, 0, width, height, ...)` cria um único adaptador com coordenadas da tela inteira, registrado depois do rodapé. A seleção das classes mantém o visual owo.

## Poções de mana

- Poção de mana: 35 MP. Fabricação sem formato: garrafa de vidro + lápis-lazúli + açúcar.
- Poção de mana maior: 80 MP. Poção de mana + fragmento de ametista.
- Poção de mana superior: 160 MP. Poção maior + pó de blaze + pó de pedra luminosa.

Beber leva 1,6 s, consome uma unidade em sobrevivência/teste e devolve uma garrafa. Até 16 unidades por pilha. Recarga compartilhada de 2 s entre todos os tamanhos. Recuperação limitada à mana máxima; não começa o consumo se a mana já estiver cheia. Atualização do HUD enviada imediatamente e recursos salvos no servidor.

Disponíveis na aba criativa Comidas e Bebidas, no livro de receitas e por `/give @s oxyrpg:mana_potion`, `greater_mana_potion` ou `superior_mana_potion` com o mesmo namespace. Mobs hostis têm 6% de chance e elites 20%; tamanho sobe nos níveis 25 e 60. Invocações não dão poções.

## Impacto Meteoro

Ativar durante voo planado conserva a direção da mira naquele instante e avança por até 5 s. O primeiro contato com mob, obstáculo ou chão detona a explosão, com dano, arremesso e destruição de blocos em volume ao redor do impacto. A velocidade continua limitada pela progressão da classe. Ativar durante queda mantém o mergulho até o solo.

A detecção percorre o segmento entre posições para evitar atravessar alvos em alta velocidade e compara as distâncias de mobs e blocos para respeitar o primeiro obstáculo. A cratera continua sendo processada em parcelas com limites de blocos/destroços e respeita permissões e eventos de quebra. Altura e velocidade continuam ampliando a força. O barrel roll continua ativo durante a investida.

O pacote FlightState mudou; protocolo 22 exige Oxy RPG 0.30.0 também no servidor. O port de voo não precisa ser substituído.

Validação: assemble concluído. Sem execução de testes ou abertura do Minecraft; interação será validada pessoalmente pelo usuário.
