# Correções e integração — 0.47.0

Alvo: Minecraft 1.21.10, NeoForge 21.10.64, protocolo 31.

## Mira e canhão aquático

O canhão aquático usa a mira atual durante a canalização, mantendo os anéis e o desenho aprovados. O cliente transmite o ponto visado a 10 Hz somente durante uso/mira/canalização, e antes de iniciar ou soltar o uso de um item. O servidor valida coordenadas finitas, alcance de 65 blocos e obstruções a partir dos olhos; o ponto expira após dez ticks e não atravessa paredes.

Arco, tridente arremessado e flechas da besta passam a mirar nesse ponto da câmera, partindo da posição real do projétil. O lançamento não soma a velocidade lateral do jogador, que causava desvio ao andar. Gravidade, dispersão, potência de carga, colisões, munição e custos continuam existindo; não há perseguição automática do alvo. Disparos sem indicação recente usam o comportamento nativo. Fogos de artifício da besta não foram alterados.

Os três hooks novos usam os descritores exatos do Minecraft alvo, conferidos no bytecode com javap: Projectile.shootFromRotation, CrossbowItem.shootProjectile, MultiPlayerGameMode.useItem/releaseUsingItem. A compilação, por si só, não comprova aplicação de mixins em tempo de execução.

## Esquiva

O log da sessão anterior registrou que Minecraft rejeitou OxyRollVisuals-1.0.zip: supported_formats é obsoleto a partir do formato 65, e o jogo retirou o pacote da seleção. O pacote 1.1 usa min_format/max_format 69, sem supported_formats. A animação e seta originais continuam intactas e externas ao JAR Oxy, com COPYING GPL-3.0 e SOURCE.txt.

O controlador PAL agora é registrado em FMLClientSetupEvent.enqueueWork, como exige a documentação NeoForge da biblioteca, e permanece parado até receber o disparo da animação. A mecânica continua nativa: Alt esquerdo por padrão (personalizável), cargas, recarga e deslocamento próprios. O mod Combat Roll completo não foi instalado. Para observar o movimento completo do corpo, usar terceira pessoa e manter Animações e efeitos habilitados.

Referência de implementação: [documentação oficial do PAL](https://docs.zigythebird.com/pal/gettingstarted/how_to_play_animations/).

## Criativo e guardas

Duas abas próprias: Oxy RPG — Equipamentos (armas, acessórios, itens de classe, sigilo e ovo de guarda) e Oxy RPG — Consumíveis (mana, bandagem, antídoto). Complementos — Itens reúne itens registrados por outros mods; as abas próprias desses mods permanecem. As abas de categoria vanilla deixam de receber itens de outros namespaces; pesquisa geral, inventário e barras salvas permanecem disponíveis.

Aldeão comum não se converte em guarda. O sistema cria um NPC separado perto de um aldeão adulto vivo em uma aldeia reconhecida pelo jogo, com posições de interesse ocupadas, espaço livre e menos de dois guardas em uma caixa de 40 blocos ao redor. A elegibilidade é observada a cada 200 ticks e limitada a uma tentativa por minuto por aldeão. Não há requisito de profissão, nível ou entrega de arma. Não se espera surgimento automático ao colocar apenas um aldeão fora de uma aldeia reconhecida.

Com comandos habilitados: `/summon oxyrpg:village_guard ~ ~ ~`. No criativo, usar Ovo de Guarda da Aldeia na aba Equipamentos. Guardas invocados manualmente passam a memorizar a posição de nascimento como centro da patrulha, recebem espada e nome, e oferecem os contratos e diálogos próprios da 0.46.

## Better Combat ou Epic Fight

Recomendação para este projeto: Better Combat, já instalado em 3.0.0+1.21.10, com PAL e Cloth Config como complementos. Mira, canhão, NPCs, abas e mecânica de esquiva são implementados no Oxy; o motor externo de animação/combate é mantido como dependência separada. A licença do Better Combat é All Rights Reserved; seu código não foi copiado para o Oxy.

Epic Fight oferece um sistema de combate mais amplo, descrito pelo autor como Souls-like, que exigiria adaptar classes, câmera, parry e esquiva. A página oficial consultada em 12/10/2026 apresenta release NeoForge 1.21.1; a consulta Modrinth filtrada por NeoForge + 1.21.10 não retornou versões. Não foi instalado nem combinado com Better Combat, e a versão do Minecraft não foi alterada.

Fontes: [Epic Fight oficial](https://www.curseforge.com/minecraft/mc-mods/epic-fight-mod), [licença Better Combat](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.21.10/LICENSE). As escolhas de integração são avaliação do projeto, não garantia dos autores de compatibilidade com o Oxy.

## Verificação e limites

`build-windows.bat` / assemble concluído com sucesso. JAR e pacote visual conferidos, descritores dos hooks inspecionados, arquivos JSON analisados e diff verificado. Sem testes automatizados, GameTests ou abertura do jogo, conforme AGENTS.md. A validação da mira durante movimento, aplicação dos mixins, animação com os pacotes Fresh Animations e conteúdo final das abas depende da próxima sessão do usuário.

Instalação, backup e hashes em INSTALACAO_047.json. Nenhum complemento adicional de gameplay foi necessário nesta atualização; os 14 JARs de outros mods e o Terralith opcional foram preservados.
