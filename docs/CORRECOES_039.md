# Correções de combate, minimapa e Aviário — 0.39.0

Minecraft 1.21.10 / NeoForge 21.10.64 / JDK 21; protocolo 27.

## Números e textos de combate

Números normais são 45% maiores que a escala configurada; críticos usam 75% a mais. Contorno escuro em oito direções, texto em negrito e crítico em dourado. Entrada de 0,55× a 1,35×, acomodação nos oito primeiros ticks, salto inicial de 18 pixels e subida posterior de 0,4 pixel por tick. Saída com fade suave nos últimos dez ticks de uma vida de 32 ticks (1,6 s). Texto de erro, parry, crítico e demais mensagens usa a mesma animação.

O salto ocorre em coordenadas de tela para permanecer legível independentemente da distância; ponto de referência continua ancorado no mundo com teste de oclusão. A quantidade numérica continua sendo o dano concluído pelo servidor. Agrupamento de acertos e controle de tamanho existente foram preservados.

## Sangue e impactos

Sangue passou de um mínimo fixo de quatro gotas e incrementos discretos para `ceil(dano × 1,5)`, limitado a 1–40 gotas antes das opções de intensidade e da preferência nativa Reduzidas. Exemplo com intensidade 100%/partículas completas: 1 de dano → 2 gotas; 4 → 6; 10 → 15; 20 → 30. Dano acima de aproximadamente 26,7 atinge o teto. Permanecem limites por tick, partículas simultâneas e distância, portanto cenas muito carregadas podem mostrar menos gotas.

Foram removidos os gatilhos de fumaça/impacto para ataques comuns acima de seis pontos de dano e cliques carregados de espada/machado/maça contra blocos. Sangue e manchas continuam nos acertos. Fumaça e estilhaços continuam em impactos de habilidades, explosões reais, tremor, meteoro e decolagem. Não há novo gatilho de mineração ou ataque normal ao chão.

## Minimap

O problema era a seleção de camada pela altura exata dos pés: saltar ou subir um bloco apagava o cache. Agora a camada subterrânea se ancora ao piso local, tolera cinco blocos de diferença e exige mudança sustentada por vinte ticks antes de trocar. A seleção é conferida a cada dez ticks. Voar sem piso próximo preserva a camada anterior; trocar o modo manual muda imediatamente.

A detecção automática deixa de usar apenas a altura da copa/coluna do terreno: consulta visibilidade do céu e teto sólido, ignorando folhas e troncos. Superfície permanece um mapa de superfície independente da altitude. Dimensões com teto, como Nether, usam a camada local em modo automático; End segue a mesma detecção de céu/teto, sem exceção por altura. Os pixels anteriores podem permanecer durante a reconstrução da mesma modalidade. Limites de amostragem e chunks carregados preservados; pequenas elevações são procuradas dentro da camada.

Não foi feita validação no Nether ou End nesta sessão. A representação continua sendo um mapa local de piso, não um modelo 3D completo de todas as cavernas conectadas.

## Aviário

- Primeiro uso da Decolagem: impulso estritamente vertical, sem iniciar planagem automaticamente.
- Segundo uso enquanto a janela está ativa e fora do chão: inicia voo e impulso na direção da mira. Próximos usos continuam os boosts. Custos e duração da habilidade preservados.
- Peso pena: o binding de Agachar mantém sua função como freio no ar; a pose fica em pé quando há espaço, preservando agachamento no chão, dentro de espaços baixos e comportamento aquático.
- Ao frear, a planagem é suspensa e a velocidade de descida fica em até 0,018 bloco/tick (0,36 bloco/s); Brisa gentil reduz para 0,008 (0,16 bloco/s). O movimento horizontal continua disponível. A distância de queda é reiniciada durante a descida controlada.
- Soltar o freio retoma o voo já ativado e ainda dentro da duração, sem precisar pressionar Espaço. Não inicia voo se houve apenas o primeiro lançamento.
- Cliente e servidor recebem/simulam a fase de voo explicitamente. Clamp de movimento antes/depois da física nativa e correção separada do flag de agachamento local evitam a pose presa.
- Voo criativo, água/lava, montarias, levitação e meteoro mantêm seus controles próprios. Redução passiva de dano de queda em 30% preservada fora da descida controlada.

## Recursos externos

O usuário escolheu **somente verificar opções**, sem instalar pacotes ou bibliotecas novas. [Pesquisa de animações externas](ANIMACOES_EXTERNAS_039.md).

## Verificação

Assemble por `build-windows.bat`, revisão dos alvos de mixins na fonte exata da 1.21.10, registros/codec do FlightState, gatilhos removidos, seleção de camadas e sintaxe dos JSONs. Não foram executados testes automatizados nem aberto o jogo (AGENTS.md). Aparência e resposta dos controles precisam de validação pessoal no jogo, especialmente soltar/segurar Agachar durante voo e troca entre cavernas de níveis distintos.
