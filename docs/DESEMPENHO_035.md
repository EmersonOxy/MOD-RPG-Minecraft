# Oxy RPG 0.35.0 — desempenho e avisos de região

## Evidência e diagnóstico

Revisão em 11/10/2026, Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21. O latest.log disponível pertence à sessão da 0.33, encerrada antes da instalação da 0.34. Preparação do mundo: 16.937 ms (11:10:54); distâncias configuradas: renderização 12 e simulação 12 chunks, aproximadamente 16 GiB de RAM física. O log não fornece comparação de FPS, distribuição do tempo de ticks ou uso de memória por mod. Esses dados não permitem atribuir o atraso a um único componente.

O minimapa fazia até 128 amostras por tick, procurava também o chão da coluna vizinha em cavernas e reescrevia/enviava 4.096 pixels a cada quatro ticks mesmo parado. Os três sistemas de habilidades/elites abriam seções de profiler para todas as entidades, incluindo entidades do cliente e tipos sem trabalho correspondente. A oclusão dos indicadores de criaturas traçava um raio em cada frame.

ProfileStore já usa anexos nativos após a migração inicial; não há leitura de arquivo por criatura em um perfil já migrado. Profissões também possuem cache. Não alteramos essas fontes de dados nem deslocamos acesso ao mundo para threads auxiliares. Crateras já são processadas em lotes; blocos e água temporários continuam com seus registros de recuperação.

## Correções

- Minimap: 32 amostras/tick nos primeiros 60 ticks ativos, depois até 64. A amostragem interrompe entre amostras ao atingir aproximadamente 1 ms; isso não é limite rígido da função inteira, nem do upload. O mapa preenche gradualmente a partir do centro. Usa somente chunks carregados.
- Reutiliza alturas das colunas vizinhas em cavernas, eliminando a segunda varredura vertical. Mantém cortes por altura, TTL e limite de 16.384 entradas, inclusive para colunas sem chão detectado.
- A textura só é enviada se os pixels, posição ou formato mudarem. Atualizações de terreno continuam sendo verificadas. Resets de mundo, zoom e camada preservam a invalidação.
- Habilidades/especialistas/elites filtram lado, entidade e estado antes do profiler; continuam aplicando efeitos, passivas, expiração de invocações e projéteis no servidor.
- Indicadores de criaturas reutilizam o teste de oclusão por até dois ticks (100 ms a 20 TPS), com invalidação quando câmera ou alvo se deslocam 0,25 bloco. Distância e campo de visão continuam sendo verificados em cada frame. Cache com chaves fracas e limpeza na troca de mundo.
- Avisos de bioma consultam o terreno duas vezes por segundo, depois que o chunk do jogador existe. Avisos desligados não fazem a consulta.
- Corrigidos metadados do pacote do port Barrel Roll: o log registrava ausência de supported_formats. O patch reproduzível inclui o campo para formatos anteriores a 82; mecânicas do port não foram alteradas.

Vilas maiores e anexos, frequência de estruturas, encontros e recompensas foram preservados. Geração de novos chunks ainda tem custo: esta entrega não promete eliminar o trabalho de worldgen. Não alteramos options.txt, alocação de RAM ou flags de Java.

## Referência dos avisos

Foi lido o código de [Traveler’s Titles](https://github.com/YUNG-GANG/Travelers-Titles), ramo 26.1.2, em LocalPlayerTickMixin, TitleRenderManager e TitleRenderer. Ele separa detecção, cache de entradas recentes, tradução e temporizadores de entrada/saída, verificando se a posição está carregada. Nossa implementação própria mantém o padrão visual Oxy e usa esses princípios; nenhum código do mod foi copiado ou dependência adicionada.

Os avisos Oxy mantêm estabilização de 30 ticks e intervalo de oito segundos, guardam os três últimos biomas por até um minuto e usam interpolação entre ticks com fade suavizado de um segundo na entrada e 1,5 segundo na saída. Entrada em dimensão tem prioridade e mostra o bioma como subtítulo. Traduções vanilla/mod são usadas quando disponíveis, com nome derivado do identificador como fallback.

## Complementos

Instalação separada dos arquivos oficiais, sem incorporá-los ao JAR Oxy:

- [Lithium](https://modrinth.com/mod/lithium/version/HADn4LHe), 0.20.1 para 1.21.10/NeoForge: otimizações da simulação. Esta revisão inclui melhorias de compatibilidade de acesso a chunks NeoForge.
- [FerriteCore](https://modrinth.com/mod/ferrite-core/version/G34Jddxu), 8.1.0 para 1.21.9–1.21.10/NeoForge: redução de memória de estruturas internas.

Versões, licenças, fontes, URLs, dependências e SHA-512 conferidos em [OTIMIZACOES_035.json](OTIMIZACOES_035.json). Arquivos originais mantidos; não são forks Oxy. Compatibilidade de execução permanece pendente de validação pessoal, especialmente voo/Barrel Roll, intangibilidade, andar sobre água e explosões.

## Revisão e validação

Inventário de todos os arquivos versionáveis, leitura de textos, sintaxe JSON, cabeçalhos PNG, descompressão NBT e presença das classes declaradas nos mixins: [REVISAO_ARQUIVOS_035.json](REVISAO_ARQUIVOS_035.json). Fontes Java, incluindo fontes de testes que não foram executadas: [INVENTARIO_CODIGO_035.json](INVENTARIO_CODIGO_035.json). Revisão semântica detalhada concentrou-se nos caminhos de desempenho, HUD, persistência, geração de estruturas e integrações; inventário não equivale a revisão semântica exaustiva.

assemble do Oxy e do port concluídos; sem testes, GameTests ou lançamento do jogo, conforme AGENTS.md. Instalação apenas com o jogo fechado e backup de saves, config e JARs substituídos. Não existe benchmark antes/depois desta entrega e nenhuma porcentagem de ganho foi medida.

Para medir pessoalmente: usar o mesmo mundo/local e as mesmas distâncias, observar entrada e os primeiros 60 segundos; separar abrir chunks existentes de explorar terreno novo. F3+L coleta o perfil nativo. development.profiling no oxyrpg-server.toml permite consultar /rpg desempenho e limpar com /rpg desempenho limpar; em singleplayer inclui as amostras locais. Após medir, desligar profiling. Se a simulação continuar pesada, um próximo ajuste reversível é testar simulação 6–8 e renderização 8–10; não foi aplicado automaticamente.
