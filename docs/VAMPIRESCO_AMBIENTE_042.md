# Vampiresco, ambiente e HUD — 0.42.0

Minecraft 1.21.10 / NeoForge 21.10.64 / Java 21. Protocolo 29: o pacote de números de dano passou a transportar cor e há um pacote de estado do Vampiresco. Clientes e servidor precisam desta versão. Classe adicionada ao fim do enum: IDs e perfis das outras classes preservados.

## Arqueiro e números de dano

Removida a aplicação de sangramento da passiva/Assassino do Arqueiro. Os IDs da árvore e pontos investidos permanecem. Nó 15 prepara a arrancada; nó 16 acrescenta 0,5 bloco ao alcance. Os dados de classe distribuídos foram atualizados, evitando que as descrições antigas sobreponham o código. Invisibilidade e ocultação ao agachar foram preservadas para avaliação posterior do usuário. Armas que têm chance de sangrar continuam funcionando em qualquer classe.

| Condição/origem | Cor |
|---|---|
| Sangramento | Vermelho |
| Veneno | Verde |
| Fogo/sol | Laranja |
| Frio | Ciano |
| Afogamento | Azul |
| Wither | Roxo acinzentado |
| Magia/ilusão | Violeta |
| Pressão aquática | Azul claro |
| Queda | Cinza claro |
| Raio | Amarelo |

Veneno nativo usa o mesmo tipo de dano que magia; um hook no tick exato de PoisonMobEffect distingue os dois. Agrupamento de números respeita a cor, sem misturar condições diferentes. Danos de condição podem aparecer sem jogador autor, incluindo veneno no próprio jogador; o próprio dano usa um ponto na tela em primeira pessoa. Condições não reativam som/marcador de acerto. Crítico de dano mágico continua identificado.

## Vampiresco

Disponível na seleção, comando `/rpg classe vampiresco`, modo teste/debug, kit, lições e árvore com 51 nós. A disposição da árvore forma uma silhueta de duas asas e uma cadeia central. Pictogramas: sangue/presas, morcego e crescente. O item vinculado Presa Noturna reutiliza o modelo de sílex e atributos de arma que crescem pelas etapas; sua ação no botão direito prepara cura de um coração no próximo ataque.

### Sede de Sangue

Exige vida perdida antes de cobrar mana. Ativação prepara uma janela de 6/10 segundos, ampliada pela árvore. O próximo golpe corpo a corpo cria um vínculo: cura imediata limitada e uma reserva de cura gradual. Novos golpes no alvo vinculado recebem +20% de dano (+40% com melhoria), imobilizam por 2 segundos (3 com melhoria) e impedem seus ataques nesse intervalo. O vínculo abre sangramento, integrado ao efeito existente; feridas continuam exigindo bandagem. A cura gradual tem reserva finita, não cresce indefinidamente.

### Metamorfose

Um morcego nativo acompanha o jogador oculto; voo livre usa controles nativos e custa 1 MP por segundo, além da ativação. Sem atacar, usar itens, abrir blocos/interagir, quebrar blocos ou usar outras skills/esquiva. Reativar Metamorfose é gratuito e ignora a recarga para retornar. Mana insuficiente também encerra a forma. Há uma curta proteção de queda ao voltar.

Retorno convoca 2/4 morcegos, mais dois com melhoria, por 8 segundos (12 com melhoria). Cada um absorve 3 pontos de dano (5 com melhoria), morre ao esgotar a vida e deixa passar o dano excedente. Erros e esquivas são resolvidos antes da escolta, para não gastar morcegos em ataques evitados. Criaturas da escolta também podem receber dano diretamente. Permissões de voo anteriores são restauradas ao sair, trocar de classe, morrer ou desconectar; há dados de recuperação para a sessão interrompida. A forma visual não reduz o hitbox do jogador nesta entrega.

### Twilight

Força, velocidade e pressa por 6/12 segundos, ampliados pela árvore. Melhorias concedem Velocidade II/Pressa II. Atravessa folhas por colisão contextual, sem remover blocos. Durante a janela recebe +25% de dano, mas bloqueia os tipos de feitiço cadastrados e a aplicação de efeitos de poções; cura/dano instantâneo de poção também são bloqueados. Os próprios bônus e fraquezas da classe continuam válidos. Sangramento físico continua válido.

Trilhas recentes de sangue recebem destaque luminoso; proximidade da trilha concede +15% de velocidade (+25% com melhoria). O registro de posições para jogabilidade é local ao servidor, expira após 60 segundos e tem orçamento separado das partículas visuais. A duração visual das manchas continua configurável, sem reintroduzir o teto removido de sangue.

### Passiva e sede

Em dimensão com céu: de dia recebe Fraqueza I e, exposto ao céu sem chuva, 1 ponto de dano por segundo (0,5 com melhoria). Cobertura protege do sol. À noite recebe visão noturna. A fome nativa é apresentada por dez gotas azuis no lugar dos ícones de comida, preservando sincronização, exaustão e HUD dinâmico. Comida comum não recupera sede. Acertos corpo a corpo efetivos em criaturas pacíficas recuperam 2 pontos (3 com melhoria), no máximo uma vez por segundo. Acertos em mobs pacíficos ou hostis abrem feridas. Parte dos animais não domesticados próximos foge quando o Vampiresco está com sede.

## Ambiente

Efeitos locais em chunks carregados, sem gerar terreno nem enviar pacotes de ambiente. Configuração: **R > Interface > Combate > Animações e efeitos > Ambiente**; arquivo oxyrpg-ambient.properties. Folhas/vento, água e vegetação podem ser desligados separadamente.

- Folhas: orçamento de três colunas a cada quatro ticks, num raio de 12 blocos e altura local limitada. Cor usa a árvore/bioma, com cerejeira rosa, carvalho pálido acinzentado e agulhas menores para pinheiro. Sprites nativos leaf_0–11; vento coerente com rajadas, rotação e esmaecimento. Até 192 partículas próprias, respeitando a opção mínima de partículas.
- Folhas no chão: adiciona uma instância própria do configurado patch_leaf_litter nativo, evitando duplicar a referência já usada em dark_forest na ordenação de features; a dark_forest, old_growth_birch_forest e flower_forest. Usa altura que ignora a copa para alcançar o chão, com uma colocação extra por chunk. Afeta novos chunks; não reescreve florestas já exploradas.
- Voo sobre água: até 2,5 blocos da superfície, movimento horizontal gera respingos e ondulações horizontais. Partículas ficam sobre água, respeitam as margens e esmaecem em 24 ticks. Até 16 ondas próprias; independem da visibilidade do HUD.
- Vegetação: grama curta/alta e samambaias curtas/altas próximas inclinam e achatam ao passar jogadores e mobs, e se recuperam em 48 ticks. Substituição do modelo na renderização, com dados extraídos em ExtractLevelRenderStateEvent; os blocos reais, colisão, drops e saves permanecem intactos. Limite de 48 posições e novas alterações locais em lotes; seções só são sinalizadas na entrada/saída, sem recompilar a cada quadro. Plantas de duas partes usam o mesmo pivô.

## Ícones e referências

HUD das três skills reduzido de cartões 50×52 para 30×34, com pictogramas de traço fino, sombra, tecla em tamanho compacto, recarga e duração. Removidos rótulos permanentes “Pronta” e textos de estado que competiam com os símbolos. Cores da classe e exibição dinâmica mantidas.

Referências de comportamento e apresentação, sem importar código/modelos/texturas desses projetos:

- [Particular](https://modrinth.com/mod/particular): folhas de tipos diferentes e interação com água.
- [Effective](https://github.com/Ladysnake/Effective): respingos, gotas e ondulações.
- [Rustling Leaves](https://www.curseforge.com/minecraft/mc-mods/rustling-leaves): vento e cobertura de folhas.
- [Tactile](https://www.curseforge.com/minecraft/mc-mods/tactile): grama que se inclina com passagem.
- [Manual oficial de Guild Wars](https://legal.guildwars.com/en/docs/gwn-manual-en.pdf): organização de barras e ícones compactos.

São referências visuais, não uma afirmação de compatibilidade dessas versões com 1.21.10. Nenhuma biblioteca adicional instalada nesta entrega. Fresh Animations/EMF/ETF existentes preservados.

## Validação e limites

Assemble compilado; fontes locais exatas 1.21.10 verificadas para partículas, colisões, eventos, modelos, extração de estado, fome, voo e efeitos. Recursos JSON e inventário registrados. Sem testes automatizados, GameTests ou abertura do Minecraft, conforme AGENTS.md.

Validação manual pendente: cada ramo em nível inicial/máximo, custo/cooldown/reativação, retorno do voo e troca de classe/dimensão, regeneração e alimentação, ataques na escolta, poções instantâneas/feitiços em Twilight, destaque e velocidade em trilhas, cores de dano próprio, compacidade do HUD, folhas e margem das ondas, vegetação com resourcepacks/shaders e impacto real em FPS. A compilação não confirma a renderização no jogo.
