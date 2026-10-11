# Oxy RPG 0.28 — interface e efeitos

## Interface

owo-lib NeoForge 0.12.28+1.21.10 integrado como dependência obrigatória. O seletor de classes usa componentes/entrada/foco do owo-ui dentro da tela existente. Menu R e escolha de classe ganham abertura gradual; detalhes reaparecem suavemente ao trocar de classe. Mantidos os atalhos personalizados, confirmação no servidor, prévia da árvore, modo de teste e volta ao menu.

A integração Gradle usa accessTransformers (plural, como exige ModDevGradle) e interfaceInjectionData. O JAR oficial inclui suas bibliotecas auxiliares e módulos Forgified Fabric API. Não instalar Fabric API de Fabric na instância NeoForge.

## Efeitos

- Enxame: duas fitas de chama espectral orbitam os morcegos com fumaça mais discreta.
- Gelo e flecha espectral: rastros curtos e pulsos de contato.
- Magma: rastro curto amostrado no trajeto e onda ao aderir ao alvo/solo.
- Explosões: ondas de impacto que expandem e desaparecem.
- Hidromante: pulsos azuis nos alvos atingidos pelo canhão.
- Meteoro: onda dourada conforme o raio efetivo do impacto.

Os efeitos são cosméticos; dano, consumo, recarga e destruição de blocos não foram alterados. Pacotes enviados somente a jogadores até 48 blocos. Cliente limita a 12 ondas, com duração de 14 ticks, oclusão por paredes, ocultação com F1/HUD escondida/F3 e limpeza ao trocar de mundo.

## Validação

Compilação assemble e geração de dados runData concluídas com sucesso, incluindo carregamento do owo-lib e dependências. Validação de jogabilidade e aparência será feita pelo usuário; não abrir o jogo nem executar testes sem solicitação.
