# Oxy RPG 0.28.1

## Correções

O seletor owo recebia coordenadas absolutas da tela em uma raiz deslocada (createWithoutScreen). O roteamento interno espera coordenadas locais. A raiz agora acompanha a tela inteira e a linha de classes é posicionada como filha, mantendo origem zero para o mouse. Cliques, foco e confirmação continuam usando os mecanismos nativos/owo, sem simular escolha de classe.

A árvore de atributos pode ser aberta quando o personagem ainda está sem classe. Compra continua exigindo pontos e conexões; nenhum atributo é concedido gratuitamente.

## Interface

Botões de todas as telas do Oxy RPG, incluindo abas, rodapés, voltar, aprender, centralizar, câmera, interface, combate, arsenal, missões, trabalhos e ajustes nativos do NeoForge: fundo escuro, bordas douradas, foco destacado e transição de hover. Sliders acompanham o mesmo tema, preservando mouse, teclado, limites e valores negativos da câmera.

O tema é aplicado somente às telas do mod e às configurações cuja identificação é oxyrpg. Telas vanilla e configurações de outros mods não recebem o tema.

## Validação

Compilação assemble; aparência e interação no jogo aguardam validação pessoal. Sem testes automatizados nem abertura do jogo, conforme AGENTS.md.
