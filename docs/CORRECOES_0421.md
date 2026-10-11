# Correção de inicialização 0.42.1

O crash enviado pelo usuário identifica `VegetationModelMixin.oxyBend`: `Invalid descriptor` durante a aplicação do mixin. A 0.42.0 selecionava `renderBatched` somente pelo nome; o Minecraft/NeoForge possui duas sobrecargas, uma com `VertexConsumer` e outra com `Function<ChunkSectionLayer, VertexConsumer>`. O handler corresponde apenas à segunda.

A seleção agora inclui o descritor JVM completo da sobrecarga NeoForge. A vegetação reativa continua habilitada, sem alterar mundos, perfis, pacotes de animação ou outros mods. Protocolo de rede 29 preservado.

As assinaturas foram conferidas com `javap -s` no artefato Minecraft 1.21.10/NeoForge 21.10.64 usado pelo projeto. Os hooks novos de veneno e poções também correspondem aos métodos reais e não têm a ambiguidade encontrada na vegetação. A compilação Java não aplica os mixins e, por isso, não detectou a falha anterior.

Empacotamento por `build-windows.bat assemble`; verificação estática do JAR e instalação registradas em `INSTALACAO_0421.json`. Sem testes automatizados ou abertura do jogo, conforme AGENTS.md. A confirmação de inicialização no jogo depende da validação manual do usuário.
