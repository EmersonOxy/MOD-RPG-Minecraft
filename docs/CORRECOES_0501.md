# Correções 0.50.1 — debug e seleção de classes

## Evidências e causas

Os prints `2026-10-12_17.33.58.png`, `17.34.06.png` e `17.34.46.png` da instância mostram o erro de comando `rpg debug classe arqueiro_novo` e a repetição da ajuda/status no chat ao ajustar o nível. A revisão encontrou também colisões entre ordinais e ações: o botão do novo Arqueiro enviava a operação de esquiva; o Deus do Trovão enviava a operação do Ilusionista.

As opções antigas existiam apenas em mapas de memória apagados ao sair/encerrar o servidor. O kit ainda considerava a classe normal durante o debug, e o cliente exigia conexões na árvore mesmo com liberação de desenvolvimento ativa.

## Implementação

- `DebugSettings` e attachment serializado `oxyrpg:debug_settings` guardam modo, classe, nível, máscara de skills, recursos, recargas, atributos, Arsenal e tempos das recargas por classe. O perfil normal usa seu attachment existente separado.
- Toda alteração atualiza o attachment; o Minecraft grava os dados do jogador no salvamento normal. Login, clonagem e respawn restauram os controles. Logout/encerramento limpam somente caches. Desativar explicitamente salva o modo desligado e volta ao perfil normal.
- Debug e teste são permitidos ao dono do mundo singleplayer e a operadores de nível 2 em servidor. O cliente recebe a permissão do servidor, evitando depender do estado de cheats ao reabrir o mundo. Configurações de alguém sem permissão ficam inativas sem apagar o attachment.
- **R > Debug > Escolher classe** abre o seletor completo. Todas as classes usam `Request(21, ordinal)` com validação no servidor e regras normais/debug/teste existentes. IDs e perfis antigos foram preservados.
- Comandos usam nomes públicos (`assassino`, `arqueiro`, `deus_trovao` etc.). `arqueiro_novo` permanece como alias de compatibilidade para o novo Arqueiro.
- Kit, recursos e habilidades consultam a classe/nível efetivos. A sincronização publica o perfil efetivo antes de atualizar equipamentos e mana.
- Debug permite liberar qualquer nó sem pontos/nível/conexões; os pré-requisitos são incluídos pelo servidor. O botão da árvore indica liberação de debug. Teste mantém tudo liberado no nível 100 com custos normais.
- Ajustes deixam de repetir ajuda e status no chat; resumo aparece como aviso sobre a hotbar. `/rpg debug ajuda` exibe a lista de comandos quando solicitada.
- Consultas de permissão não criam contexto de comando para jogadores comuns sem override ativo.

## Uso

1. Abra **R > Debug** e ative o debug completo (ou `/rpg debug on`).
2. Ajuste nível, recargas, recursos, atributos e Arsenal; escolha a classe no seletor.
3. Use **Liberar todas as skills**, **Limpar skills temporárias** ou a árvore para combinações específicas.
4. Saia normalmente do mundo. Ao reabrir, as opções salvas devem ser restauradas.
5. Desative para voltar à progressão normal; nível e skills normais não são substituídos por estes controles.

Também disponíveis: `/rpg debug nivel 1–100`, `nivel subir/descer N`, `skills todas/limpar`, `classe <nome>`, `cooldowns on/off/limpar`, `recursos normais/ilimitados`, `atributos maximos/normais`, `arsenal on/off`, `tudo`, `recuperar`.

**Atualização da 0.50.0:** é necessário configurar uma vez após instalar, pois as versões anteriores não gravavam essas opções. Não existe estado antigo salvo para recuperar. Recursos consumidos no modo de desenvolvimento continuam usando pools separados em memória; este patch persiste as configurações e recargas, não o saldo desses pools nem habilidades em execução.

## Verificação e limites

Compilação/empacotamento por `build-windows.bat` (assemble). Sem testes automatizados, GameTests ou abertura do jogo, conforme AGENTS.md. Revisão de contratos nativos locais, caminhos de menu/comando, codec do attachment, eventos de ciclo do jogador e sincronização; JSON e diff conferidos. Protocolo 34: cliente e servidor precisam desta mesma versão.

Validação manual pendente: alternar as doze classes, confirmar kit/habilidades, limpar/liberar nós, variar nível/recargas/recursos, salvar/reabrir o mundo, morrer/renascer e desativar o debug para conferir o perfil normal. Em servidor, conferir acesso de OP e bloqueio de não OP. Os demais relatos visuais precisam ser confirmados nessa validação; a compilação não comprova o comportamento no jogo.
