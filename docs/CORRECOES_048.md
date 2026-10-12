# 0.48.0 — aldeões, complementos e esquiva

Minecraft 1.21.10 / NeoForge 21.10.64; protocolo 31. Prioridade confirmada pelo usuário: vilas e novos mobs.

## Prints e problemas concretos

Inspecionados: 2026-10-12_14.10.45, 14.10.48, 14.10.51 (ovo de guarda), 14.07.58 e 14.08.01 (jogador), 13.32.05, 13.32.10 e 13.32.12 (trocas/missões). Não são evidência de validação da versão nova.

O log da sessão 0.47 registra `No model loaded for default item model ID oxyrpg:village_guard_spawn_egg`. A tentativa anterior de trocar ITEM_MODEL pela propriedade não fornecia uma definição para o modelo padrão. Agora o item tem seu próprio arquivo assets/oxyrpg/items/village_guard_spawn_egg.json referindo o modelo nativo minecraft:item/villager_spawn_egg. Aparência igual ao ovo do aldeão, entidade gerada continua sendo o guarda próprio.

Na tela dos aldeões, o inventário usava leftPos deslocado, enquanto MerchantScreen recalculava a posição central para ofertas, botões e barra de rolagem. O resultado foi uma coluna vazia e ofertas desenhadas sobre a outra área. Oxy agora usa getGuiLeft nas três operações; índices locais e descritores conferidos no bytecode do Minecraft alvo.

## Interface de aldeões

Comércio e missões na mesma moldura, painel lateral mais largo (até 260), linhas com o item solicitado, seleção destacada, texto de estado curto e rolagem visível. Botões de ação com rótulos curtos. Conversa ocupa o espaço da lista enquanto aberta, com separação entre a pergunta e a resposta; Missões retorna à lista e Outro assunto percorre os três tópicos. Nomes/vida de mobs são ocultados atrás de menus.

Largura de GUI abaixo de 480 mantém as trocas na tela nativa e abre missões/conversa em uma tela compacta, com Abrir trocas no cabeçalho. Não se força uma composição lateral sem espaço. Regras de contrato ativo, pagamentos e entrega parcial permanecem no servidor. IDs existentes não foram reordenados.

## Esquiva e Combat Roll

O mod Combat Roll completo NÃO está instalado. Seta e animação originais são usadas em OxyRollVisuals-1.1.zip, externo, com autoria e GPL-3.0. A mecânica de cargas, recarga, proteção e impulso é do Oxy, na tecla Alt esquerdo por padrão.

A sessão do usuário registrou DEBUG ON / Recargas OFF às 14:07:42 e 14:09:03. Nesse estado a barra fica cheia por definição. Ela agora mostra ∞ ao lado, tornando explícito o override. Para observar consumo e recarga: `/rpg debug cooldowns on`, ou desativar debug. Não alteramos o pedido de permitir cooldowns ilimitados no debug.

O pacote de rolamento foi carregado na sessão 0.47, conforme Reloading ResourceManager; ausência de metadados já não explica a animação. Fresh Player usa EMF, que pode substituir poses de bibliotecas. Acrescentada integração própria pelas APIs de EMF/PAL: enquanto uma camada PAL de prioridade >=1000 está ativa, as animações e o modelo customizado EMF do avatar dão lugar ao modelo nativo; ao finalizar, voltam automaticamente. Vale para rolamento e Better Combat. A biblioteca não foi copiada. Diagnóstico de animação ausente/controlador ausente e primeiro disparo agora aparece uma vez no log, sem mensagens de gameplay no chat. A eficácia visual ainda depende de validação em jogo.

Referências: [PAL oficial](https://docs.zigythebird.com/pal/gettingstarted/how_to_play_animations/), [descrição do conflito pelo projeto EMF Compat](https://github.com/victorkozhokin/emf-compat). O código Oxy usa as APIs públicas instaladas; não copia código GPL do projeto de compatibilidade.

## Homesteads — integração efetiva

Complemento original NeoForge 1.0.1 para 1.21.10. Cinco profissões: apicultor, lenhador, mateiro, prospector e coletor; bancadas, trocas e seis conjuntos de assentamentos/acampamentos próprios. Estruturas aparecem em terreno ainda não gerado, sem reconstruir regiões existentes. Biomas/pools oficiais mantidos.

Oxy acrescenta três missões de suprimentos e três falas por profissão (15 contratos, 15 falas), traduções em português e reconhecimento do namespace da profissão. Guardas Oxy podem surgir perto de um profissional Homesteads com bancada de trabalho reconhecida a até 16 blocos, além da regra de aldeias vanilla. Limites, colisão e frequência de guardas são preservados. Não converte moradores nem remove suas trocas.

Para procurar uma estrutura: `/locate structure homesteads:forest_home_steads` (comandos habilitados). As cinco bancadas podem ser encontradas nas abas do Homesteads/Complementos no criativo e usadas por aldeões normais.

[Homesteads oficial](https://www.curseforge.com/minecraft/mc-mods/homesteads). Código/assets do complemento não foram incorporados ao JAR Oxy.

## More Mobs — integração efetiva

Complemento original universal 1.5.11+mod, com entrada específica NeoForge e suporte declarado a 1.21.10. Acrescenta variantes visuais e alguns comportamentos/variantes de entidades vanilla: zumbis, esqueletos, saqueadores, piglins, mooshrooms etc. Não é um pacote de dezenas de espécies inéditas.

Oxy marca invocações do Necromante, cópias/entidades temporárias do Ilusionista e morcegos auxiliares com a exclusão oficial ts.mm.exclude antes de processar o complemento. Variantes naturais continuam sob níveis, elites e recompensas do Oxy. O catálogo inspecionado não altera atributos nas funções principais; alterações visuais/equipamentos continuam do complemento. Os metadados NeoForge não exigem Fabric API/QSL: essas dependências listadas genericamente na API pertencem aos outros loaders. Não foram instaladas bibliotecas de outro loader.

[More Mobs oficial](https://www.curseforge.com/minecraft/mc-mods/mod-more-mobs), [código/seletores oficiais](https://github.com/Tschipcraft/more_mobs). Mantido como complemento separado, com hash oficial; nenhum asset foi recortado. O processamento do datapack pode acrescentar custo em áreas com muitos mobs; impacto não medido nesta atualização.

## Estado dos demais pedidos

| Recurso | Estado real |
|---|---|
| Better Combat, PAL, Cloth Config | Instalados na 0.46; compatibilidade EMF acrescentada agora. |
| Terralith | Datapack original opcional por mundo, desde 0.46. |
| Combat Roll | Apenas seta/animação; mecânica Oxy, não o mod completo. |
| Guardas, missões, entrada de mobs | Implementações nativas Oxy, sem instalar Villager Guards/Spawn Animations. |
| Homesteads, More Mobs | Complementos preparados/instalados nesta atualização; integração Oxy descrita acima. |
| Presence Footsteps | Apenas pesquisado; sem release NeoForge 1.21.10 encontrado nas consultas. Port nativo de áudio não implementado. |
| Eating Animation Fork | Apenas pesquisado; não instalado nesta atualização, após priorização de vilas/mobs. |
| Better Villages | Referência; consulta filtrada não encontrou release 1.21.10. Expansão Oxy e Homesteads usados nesta etapa. |
| Identity 2 | Referência; sistema de transformação permanece Oxy. Mod completo não instalado. |
| Spyglass Improvements | Artefato compatível encontrado; não instalado nesta etapa. |
| Common Network, Borderless, Biome Dither, Dynamic Lights, Simple Health Bar, Nickname Easy | Pesquisa/referências; não declarar como integrados. |

Os complementos existentes continuam separados. Esta etapa não conclui os ports e recursos da última parte da tabela; prioriza a escolha explícita de vilas/mobs.

## Verificação

Compilar com build-windows.bat / assemble; sem testes automatizados, GameTests ou abrir Minecraft, conforme AGENTS.md. Conferência de descritores/variáveis locais, JSONs, integridade dos JARs e hashes oficiais. Instalar só com jogo fechado, preservando mods, UUIDs, mundos e configurações. Recibo de instalação e backup em INSTALACAO_048.json. Interface, IA, mistura de animações e desempenho ainda precisam de validação pelo usuário em jogo.
