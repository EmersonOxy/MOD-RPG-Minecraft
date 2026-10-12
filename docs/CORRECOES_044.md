# Classes, modelos e efeitos 0.44.0

## Prints e causas

Vistos os três prints novos de 11/10/2026, 21:54:28, 21:54:28_2 e 21:56:05: vegetação na encosta da savana e HUD de skills pouco legível. Não há print novo do canhão/magma; essas alterações atendem ao relato do usuário. A renderização de grama da 0.43 usava o pipeline de itens/entidades apesar de conservar biome/seed, causando iluminação diferente. O canhão era um conjunto de respingos em hélice de curto alcance. Magma criava núcleos/efeitos, sem destruir terreno. A Fênix aplicava impulso sem proteção de aterrissagem.

## Vampiresco

O avatar humano é cancelado e substituído pela submissão do renderizador nativo BatRenderer, com seu BatRenderState extraído de um modelo cliente sem entidade adicionada ao mundo. O antigo morcego separado da forma foi removido. Uma instância cosmética por jogador/cliente evita alocações de entidades a cada frame. Modelo de asas nativo, olhos/câmera a 0,55 bloco, caixa de colisão 0,7×0,85. Dimensões são usadas nas checagens de poses, colisão e atualização da caixa, no cliente e servidor.

Ao retornar num espaço onde o humano não cabe, volta à última posição livre registrada nesta forma/dimensão. Não teleporta o cadáver nem usa coordenadas da dimensão anterior. Atalhos, mana por segundo, proibição de ataque/interação e escolta ao retornar permanecem. O jogador continua sendo a entidade Player para identidade/inventário, com geometria e dimensões da forma; não vira uma segunda criatura controlada.

Escolta recebe UUID do dono. Ataque direto, projétil e dano atribuído ao dono são recusados pelo servidor. Inimigos ainda podem feri-la e ela continua absorvendo dano. Proxies antigos salvos da 0.43 são descartados ao recarregar, pois eram temporários de sessão.

A fuga passa de multiplicador 1,35 a 0,65; aldeões assustados recebem também redução transitória de 30% na velocidade para limitar a aceleração da IA de pânico. Destinos de fuga mais próximos; redução removida após o medo. Continuam acordando e fugindo à noite.

## Classes e terreno

Fênix registra a janela de aterrissagem ao lançar/impulsionar, zera acúmulo de queda e cancela LivingFallEvent durante essa janela. Termina ao aterrissar ou expirar; não concede imunidade geral a qualquer queda do Piromaníaco.

Canhão d'água ganha 16 blocos de alcance inicial, 22 com a habilidade principal e mais seis com melhoria. O feixe continua parando em blocos sólidos, atravessa água e mantém dano/empurrão da classe. WATER_PULSE usa os 16 sprites nativos sonic_boom, agora azuis, como anéis perpendiculares à direção do feixe. Limite de 160 partículas e controle de efeitos existente. Mesmo estilo visual do Warden, com regras de colisão da skill aquática.

Magma explode no contato e na detonação dos núcleos. Dano em área e pequenos crateras reais: raio destrutivo 1,25/1,8, até 32 blocos rompidos por impacto. Superfície natural exposta no raio 2/3 se transforma em magma/netherrack, até 32 conversões. Mudanças persistentes intencionais, autorizadas pelo pedido de contaminação. Exclui containers, líquidos, blocos indestrutíveis/resistentes acima de dureza três; respeita mayBuild, mayInteract e evento NeoForge de quebra. Mantém núcleos/rede/chamas. Em voo alto sem superfície próxima, a explosão não cria terreno flutuante.

## Vegetação e HUD

A grama passa a usar RenderTypeHelper.getMovingBlockRenderType, evitando a iluminação difusa adicional de itens/entidades. Mantém modelos/seed/tinta/offset/luz/AO nativos, permanência sob corpos e recuperação após sair. O descritor exato de renderBatched da 0.42.1 foi preservado. Conferência visual de sombra em savana/floresta ainda manual.

HUD: três slots de 26×26, ícones coloridos dos itens de skills existentes, borda discreta, sombreamento progressivo de recarga, tempo acima e tecla abaixo. Em telas largas, barra fica à esquerda da hotbar; em GUI estreita, centraliza acima das barras sem cobrir a barra de ação. Dinamismo/ocultação e teclas personalizadas preservados. Referências: [Iron's Spells](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks) e [SpellActionbar](https://www.curseforge.com/minecraft/mc-mods/spellactionbar). Nenhum asset desses mods copiado.

## Níveis e animações

Iron Golem tem nível mínimo 15; Snow Golem, oito. Novos golemas acompanham o jogador mais próximo com a mesma variação de nível dos inimigos; existentes sem nível recebem o mínimo. Vida/dano e projéteis usam fatores de nível quando o dano original existe. Snowballs que naturalmente causam zero dano continuam com zero. Nível salvo, barra sincronizada; não recebem sorteio elite nem deixam de ser neutros.

Nascimento cosmético: hostis NATURAL/CHUNK_GENERATION emergem por um segundo; voadores materializam pelo tamanho. Invocações do Necromante também entram dessa forma. Não se aplica a recarregamento de chunks, comandos, ovos ou invocações do Xamã. Movimento/IA/colisão continuam no servidor; somente a apresentação se eleva do chão. Ataques corpo a corpo concluídos recebem pequena animação de torso/impulso de oito ticks, complementar à animação de braços/armas do Fresh Animations existente. Opção de animações atual desativa surgimento/gesto, mas não a forma de morcego. Fresh Animations/Player Extension/EMF/ETF preservados.

[Spawn Animations](https://modrinth.com/datapack/spawn-animations) serviu de referência de nascimento. [Better Combat](https://github.com/ZsoltMolnarrr/BetterCombat) e [Player Animation Library](https://modrinth.com/mod/player-animation-library) foram pesquisados para novos ataques. Better Combat 3.0.0+1.21.10-neoforge existe para nossa versão e requer PAL e Cloth Config; PAL 1.1.3 listado para 1.21.10. Isso envolve controlador de animações e compatibilidade com modelos EMF, não apenas copiar um ZIP. Nesta entrega, surgimento/gestos são implementação original nas APIs nativas, reutilizando os modelos já instalados; não foram extraídos arquivos de animação de terceiros nem instalados novos mods externos de combate. Combos e nova coreografia de armas ficam como integração futura, documentada, sem alegar que os pequenos gestos atuais equivalem ao Better Combat.

## Verificação

Protocolo 30 pelo payload EntityMotion; cliente/servidor devem usar Oxy RPG 0.44.0 juntos. Compilação assemble, inspeção JSON/CRC, conferência de sprites nativos e assinatura exata de Avatar.getDefaultDimensions por javap. Sem testes automatizados ou abertura do jogo, conforme AGENTS.md. Pendente: F5/inventário/câmera em morcego, voo em corredor baixo e retorno, disparo contra escolta, Fênix e quedas comuns, canhão em terra/água, impactos de magma/proteções, sombras do mato nos prints, HUD em GUI larga/estreita, surgimento natural/Necromante/recarregamento e golemas novos/existentes. Registros de instalação e inventário em INSTALACAO_044.json/REVISAO_ARQUIVOS_044.json.
