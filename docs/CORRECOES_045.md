# Correções 0.45.0 — 12/10/2026

## Mecânicas

- Vampiresco: chuva exposta causa 0,5 de dano por segundo, sem dano em criativo/espectador. A versão anterior excluía chuva do dano solar e não tinha dano de chuva. Sol e fraqueza diurna permanecem. Descrição de fraquezas atualizada.
- Metamorfose já consumia 1 MP/s na 0.44; mantém custo, interrupção quando acaba mana e exceção exclusiva do debug ilimitado. Modo de testes normal paga mana.
- Morcegos e representações cosméticas não empurram jogadores. O disfarce do ilusionista era uma entidade visual sobreposta que podia empurrar o dono: removida essa interação.
- Aldeões: removido modificador -30%; navegação 0,75, correspondente à fuga nativa (base 0,5 × 1,5). Distância e procura de caminhos continuam limitadas.
- Forma de morcego: movimento em qualquer direção permite impulso de corrida em terceira pessoa independentemente da opção global; modelo aponta para a direção do comando de movimento. Câmera continua independente; subida/descida seguem controles nativos de voo.
- Aviário: mergulho descendente, inclinação >=35 graus, a até quatro blocos do chão encerra o planeio e zera velocidade horizontal. Predição local e servidor; o meteoro mantém sua colisão/impacto.
- Água: partículas usam comportamento visual de billboard, tamanho 1,5 e duração 16 ticks do SonicBoom nativo, com tonalidade azul. Antes eram pequenas e vistas de perfil. Linha, colisão, dano e empurrão continuam do servidor.
- Mira: ponto das habilidades vem do raio da câmera, até 64 blocos, com primeiro bloco/mob. Projetil converge a esse ponto desde sua origem; validação de alcance, valores finitos e obstrução permanecem, sem rejeitar pelo ângulo do corpo. Ataques e interações comuns conservam alcance nativo. Segurar Usar mostra o ponto das habilidades no retículo.
- Lança-chamas mantém direção de mira capturada durante a ativação, em vez de retornar à rotação do corpo nos pulsos seguintes.
- Fênix incendeia criaturas próximas a cada cinco ticks; um salto adicional por saída do chão, pressionando pular novamente. Agachado não ganha salto fortalecido. Proteção de queda continua limitada ao salto da Fênix.
- Mímico: IA das cópias ativa; movimento normal e navegação de volta se distância >10 blocos. Removidos órbita compulsória e dano artificial automático da órbita. Cópias não selecionam dono ou outras manifestações como alvo. Disfarce visual pode nascer sobre o player sem exigir espaço para o tamanho do mob; criaturas reais procuram posição livre em até 16 tentativas. Bosses/tamanho excessivo seguem excluídos explicitamente.
- Trava/marcação permite aldeões; aliados e pets domesticados continuam protegidos. Não remove limite de 24 blocos ou linha de visão.

## Arqueiro e proposta de divisão

A invisibilidade ao agachar por dois segundos vem do nó 8, **Discrição**, já descrito na árvore. Não é efeito aleatório. Proposta registrada, ainda não implementada: conservar IDs e progressão atuais ao migrar furtividade/dash para Assassino; criar um novo Arqueiro com tiro preciso, rajada e controle de distância. Não renomear enum ou reutilizar IDs antes de uma migração explícita, pois há perfis, equipamento e máscaras de skills salvos.

## Validação e instalação

Compilar com build-windows.bat (assemble), sem testes e sem abrir Minecraft, conforme AGENTS.md. Validar descritor do novo mixin em fontes/bytecode 1.21.10. Não afirmar comportamento no jogo a partir da compilação. Instalada com jogo fechado após confirmação do usuário; backup de saves/config/options e JAR anterior fora de mods. Nenhum mod/pacote externo alterado. Recibo em INSTALACAO_045.json.

## Prints

Revisados os nove prints mais recentes da instância, incluindo 12.18.34, 12.16.59, 12.14.21, 12.14.18, 12.08.06 e sequência 11.52 de 12/10. A imagem anexada do Warden é referência visual, não instrução. A folha de contato está em external-mods/review-045.jpg (ignorada).
