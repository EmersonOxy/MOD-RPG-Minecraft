# NPCs e integrações 0.46.0 — 12/10/2026

## Trocas, contratos e conversa

A interação com um profissional abre as trocas nativas e o painel lateral de contratos/diálogo. Movimentação de itens, seleção de oferta e fechamento continuam do MerchantMenu original. Quando largura de GUI >=416, os dois painéis ficam lado a lado; GUI menor usa botão para consultar contratos em tela própria, evitando sobrepor inventário/slots. Moradores sem ofertas e guardas usam a tela de contratos/conversa, com informação de que não negociam. Textos do painel podem ser rolados. NPC para durante conversa; heartbeat servidor a cada 20 ticks, validade de cinco segundos e distância máxima seis blocos.

Cada uma das 13 profissões vanilla tem três contratos exclusivos: fazendeiro, pescador, pastor, flecheiro, bibliotecário, cartógrafo, clérigo, armeiro, ferreiro de armas, ferreiro de ferramentas, açougueiro, coureiro e pedreiro. São 39 contratos de oficina/fornecimento, sem novos contratos de matar mobs para moradores comuns. Três contratos de combate adicionais pertencem aos guardas (pillagers, vindicators, witches). Cada categoria tem fala própria e três assuntos de conversa. O botão Diálogo percorre profissão, ajuda e conselho. São conversas curtas escritas, sem IA generativa.

Um contrato ativo por jogador, mantendo o modelo anterior. Escolha um dos três, aceite, entregue os itens pelo botão Entregar/verificar e receba recompensa ao concluir. Entregas parciais aceitas. O servidor valida NPC, distância, linha de visão, catálogo e inventário. Conclusão é persistida antes do pagamento. Nenhum cliente escolhe recompensa ou quantidade.

Os sete IDs antigos estão preservados para contratos já aceitos. Não são oferecidos novamente; nem aldeões nem progressão são convertidos/apagados. NOVO protocolo 31 (payload de diálogo inclui opções e fala do NPC).

## Guardas

Entidade própria `oxyrpg:village_guard`, corpo de aldeão com camada visual nativa do armeiro e nome de guarda. 40 de vida, seis de armadura e dano básico cinco. Patrulha até 12 blocos do ponto-base, regressa quando se afasta; enfrenta inimigos com visão em até 16 blocos, limitado ao raio de defesa da aldeia. Não é cidadão convertido e não remove trocas existentes. Fala e missões próprias. Pode ser invocado por `/summon oxyrpg:village_guard` para inspeção manual.

Nas aldeias já carregadas, adultos verificam a presença de até dois guardas em 40 blocos a cada dez segundos (consulta por morador tem intervalo mínimo de um minuto). Exige `ServerLevel.isVillage`, posição livre e NPC vivo. Não força geração nem procura estruturas à distância. Guardas novos persistem no mundo. Modelo não é uma nova malha de armadura desenhada do zero; utiliza recursos nativos.

## Better Combat

Instalado como complemento oficial: 3.0.0+1.21.10 NeoForge, PAL 1.1.3 e Cloth Config 20.0.149. Armas nativas utilizadas pelo arsenal recebem a compatibilidade do Better Combat. Não foi copiado código nem animações do mod para o JAR Oxy: [licença All Rights Reserved](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.21.10/LICENSE). Mod suporta integração por arquivos de atributos/presets e componentes; mantemos essa separação para atualizações.

Mana, miss/crit, parry com escudo e esquiva continuam do Oxy. O ataque do Better Combat muda o momento de acerto e pode acertar vários alvos, por isso o equilíbrio e a convivência com Fresh Player exigem validação manual. Não declarar sucesso visual em jogo a partir de assemble.

## Terralith opcional

`Criar mundo > Mais > Terralith: desativado/ativado`. Desativado por padrão; alternar executa o fluxo nativo de validação/recarregamento de datapacks antes de criar o mundo. Pacote oficial integral 2.5.13, registrado na API para 1.21.10; data formats 71–88, incluindo nosso 88. ZIP original em `oxyrpg-packs/Terralith_1.21.5_v2.5.13.zip` da instância. Nome 1.21.5 é do artefato oficial multi-versão. Não há JAR Terralith em mods, então nenhum mundo existente muda automaticamente. O pacote selecionado é copiado para datapacks daquele novo mundo e salvo na configuração dele.

[Stardust Labs / Terralith](https://www.curseforge.com/minecraft/mc-mods/terralith). Pacote não modificado; não extrair biomas individuais para distribuição Oxy. [Licença Stardust Labs](https://github.com/Stardust-Labs-MC/license/blob/main/license.txt).

## Só imagem/animação do Combat Roll

Pacote de recursos separado `OxyRollVisuals-1.0.zip`, GPL-3.0, com `arrow.png` e `roll.json` originais não alterados, autoria Daedelus e fonte/license completas. Nenhum código, recarga, atributos, enchantments ou atalho do Combat Roll. Imagem exibida ao lado da barra Oxy segmentada; biblioteca PAL lê o formato original da animação e a toca quando o servidor autoriza a esquiva. Oxy continua controlando cargas, seis ticks de proteção, colisão e impulso. Clientes próximos recebem a animação. Pode desativar efeitos/animações nas opções Oxy. Sem PAL/pacote, a mecânica continua e os cosméticos ficam ausentes.

O pacote está agregado separadamente e conserva sua licença; não é incorporado ao JAR Oxy nem distribuído como código próprio. [Origem e licença](https://www.curseforge.com/minecraft/mc-mods/combat-roll).

## O que falta para Presence Footsteps

O projeto Fabric e o port Forge pesquisados não fornecem release NeoForge 1.21.10 nas consultas filtradas da API. A página de port Forge não equivale a binário NeoForge. Nenhum Footsteps instalado. Para funcionar: portar eventos/mixins/renderização para nossa versão, resolver suas dependências de cliente, preservar banco de sons/mapeamento de superfícies e atribuição MIT, e validar áudio. Alternativa: um release NeoForge compatível de outro projeto ou uma implementação própria menor de passos por superfície. Instalar Fabric API/owo/GeckoLib não converte sozinho código Fabric/Forge em NeoForge.

## Como usar os demais recursos citados

| Recurso | Podemos usar? Como |
|---|---|
| Better Villages | Como mod/datapack completo de arquivo compatível; conciliar pools e expansão Oxy. Referência de ruas/casas sem copiar assets sem licença. |
| Homesteads | Complemento completo para profissões/estruturas; adaptar catálogo Oxy às profissões extras antes de integrar. |
| More Mobs | Complemento completo, conciliando variantes com níveis, drops e disfarce; não precisamos copiar todo o sistema. |
| Spawn Animations | Referência da nossa entrada visual; integração completa duplicaria a animação já feita. |
| Villager Guards | Preferimos os guardas próprios implementados aqui. O datapack citado pode converter moradores e perder trocas. |
| Identity 2 | Integração completa possível com release exato; adiciona outro sistema de transformação. Licença não autoriza copiar seus arquivos; Oxy mantém sua implementação. |
| Common Network | Biblioteca utilizável, mas resolveria port entre loaders; rede NeoForge Oxy já existe. Sem benefício imediato. |
| Dynamic Lights citado | Complemento que coloca blocos de luz no servidor. Evitar acoplá-lo a muitos projéteis, pois atualiza terreno/luz. Para feitiços móveis preferir solução de iluminação no cliente. |
| Biome Dither | Complemento de transições de terreno, sobretudo com Terralith; não resolve sombra do mato animado. |
| Spyglass Improvements | Complemento de zoom/luneta; depois conferir compatibilidade com nossa câmera. |
| Borderless | Complemento de janela, sem integração de classes/HUD. |
| Simple Health Bar | Apenas referência de interface: releases pesquisados não cobrem nosso alvo NeoForge 1.21.10. |
| Nickname Easy | Complemento cosmético; não é necessário para NPCs nomeados pelo Oxy. |
| Eating Animation Fork | Complemento client-side compatível, quando verificado o artefato; pode acrescentar animação visual de comida/poção, sem alterar mana. |

As referências de cada projeto estão em REFERENCIAS_045.md. Nesta versão não foram instalados todos esses mods. Arquivos oficiais e hashes do que foi integrado constam em INTEGRACOES_046.json.

## Verificação

Build `assemble` pela rotina Windows; fontes/descritores exatos conferidos para mixins de Merchant e CreateWorld. Inventário de arquivos/JSON e CRC/hashes dos pacotes. Sem testes automatizados, GameTests ou abertura do jogo, conforme AGENTS.md. Instalação com jogo fechado e backup; recibo em INSTALACAO_046.json. Interfaces, animação de rolamento, IA dos guardas e validação do pacote na criação do mundo aguardam teste manual do usuário.
