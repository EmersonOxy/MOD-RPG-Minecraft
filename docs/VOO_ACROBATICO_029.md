# Voo acrobático — 0.29.0

Compatibilidade opcional com o port local Do Another Barrel Roll para Minecraft 1.21.10 / NeoForge 21.10.64. O port GPL permanece em projeto e JAR separados; o Oxy apenas consulta sua API pública por reflexão e continua funcionando sem ele.

Durante o voo acrobático, a câmera de ombro deixa a orientação com o controlador e a trava de alvo suspende a assistência. Ao encerrar o voo, as opções de câmera continuam preservadas e a trava pode retomar se o alvo continuar válido. Desligar o controlador com I permite usar a câmera Oxy durante o voo.

O port impede sua propulsão extra durante a habilidade do Aviário. Decolagem, boost, mana, recarga, velocidade e Impacto Meteoro continuam sendo controlados pelo Oxy.

Controles do port: I liga/desliga; mouse inclina/rola; A/D guinam. São remapeáveis em Opções > Controles. Ajustes em Mods > Do Another Barrel Roll > Configurar, usando YACL 3.8.2+1.21.10-neoforge.

Projeto e código-fonte do port local: ../DoAnotherBarrelRoll-1.21.10 (diretório irmão do repositório Oxy), com LICENSE GPL-3.0 e PORT_1_21_10.md. Base: https://github.com/aspctt/do-a-barrel-roll-neoforge.

Validação: assemble dos dois projetos. Não foram executados testes nem aberto o Minecraft; interação de voo pendente de validação pessoal no jogo.

A cópia local do YACL tem apenas o intervalo de NeoForge no metadado corrigido para `[21.10.64,21.11)`, preservando código e recursos. Ajuste reproduzível documentado em `DoAnotherBarrelRoll-1.21.10/artifacts/YACL-METADATA-PATCH.md`.
