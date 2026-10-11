# Interface e interação 0.32.0

- Minimap: seta triangular com contorno, formato redondo/quadrado, máscara circular no terreno e prévia que acompanha o tamanho configurado sem o limite anterior da coluna. Formato salvo em oxyrpg-minimap.properties.
- Cartões de classe/XP/objetivos movidos para o centro esquerdo, com bordas, cores e barras no tema RPG. A mana permanece na barra inferior.
- Pictogramas vetoriais próprios para as 27 habilidades: HUD, cartões de classe e nós principais/capstones da árvore. Teclas, progresso, recargas e disponibilidade preservados.
- Movimento habilitado no inventário comum/criativo, seguindo teclas remapeadas e modificadores. Suspenso ao digitar em pesquisa, sem foco ou pausa. Configuração em R > Interface > Inventário.
- Villagers: Missões e Trocas alternam por ações validadas no servidor. Comércio usa abertura vanilla com preços/descontos. Villagers sem ofertas mantêm a aba de trocas indisponível. NPC interrompe navegação/AI durante a sessão e retoma sem gravar NoAI no save. Fechar encerra a sessão; timeout de 5 s cobre desconexões/perda de cliente.
- Protocolo 23: canTrade foi acrescentado ao estado do diálogo. Cliente/servidor devem usar a mesma versão.

Revisão do código e registro do projeto em ESTADO_DO_PROJETO.md e INVENTARIO_CODIGO_032.json. README antigo foi substituído para remover descrições contraditórias de versões anteriores.

Validação: assemble, sem testes ou abertura do Minecraft. Conferência interativa a cargo do usuário.
