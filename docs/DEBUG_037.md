# Oxy RPG 0.37.0 — painel de debug

## Uso

Abra **R > Debug** (operador/cheats, permissão 2). O painel oferece ativar/desativar, liberar todas as skills, nível −10/−1/+1/+10, classe seguinte, recargas, recursos, atributos máximos, Arsenal, maximizar tudo, limpar skills e recuperar vida/mana/esquiva. Os botões usam os mesmos comandos validados pelo servidor; o cliente não modifica progressão.

`/rpg debug on` agora entra com nível 100 e todas as habilidades liberadas, recursos ilimitados e recargas desligadas. Mantém a classe salva ou usa Guerreiro temporariamente se ainda não houver classe. `/rpg debug off` encerra os overrides. Escolha de classe no menu também permite alternar enquanto o debug estiver ativo.

| Comando | Resultado temporário |
|---|---|
| `/rpg debug skills todas` | Libera a árvore completa |
| `/rpg debug skills limpar` ou `/rpg debug limpar` | Limpa a árvore para escolher uma combinação |
| `/rpg debug nivel 25` | Define nível RPG entre 1 e 100 |
| `/rpg debug nivel subir 10` | Soma níveis, limitado a 100 |
| `/rpg debug nivel descer 10` | Subtrai níveis, limitado a 1 |
| `/rpg debug classe aviario` | Troca classe sem resetar o perfil salvo |
| `/rpg debug cooldowns on` | Recargas normais, independentemente dos recursos |
| `/rpg debug cooldowns off` | Ignora recargas das skills, itens Oxy e carga da esquiva |
| `/rpg debug cooldowns limpar` | Zera timers da sessão para skills/item de classe, recargas nativas dos itens Oxy e recarrega a esquiva |
| `/rpg debug recursos normais` | Consome mana/calor normalmente, em pool de sessão |
| `/rpg debug recursos ilimitados` | Bypass de mana e calor |
| `/rpg debug atributos maximos` | Aplica todos os atributos temporariamente |
| `/rpg debug atributos normais` | Usa os atributos salvos, separadamente do nível escolhido |
| `/rpg debug arsenal on` ou `/rpg debug arsenal` | Arsenal 20 temporário |
| `/rpg debug arsenal off` | Restaura o Arsenal salvo |
| `/rpg debug tudo` | Nível 100, árvore completa, atributos máximos e Arsenal 20; preserva a escolha atual de recursos/recargas |
| `/rpg debug recuperar` | Recupera vida, comida, mana e energia da esquiva |
| `/rpg debug off` | Restaura classe, nível, árvore e controles salvos |

É possível combinar nível 1 com todas as skills, ou nível 100 com árvore limpa. Alterar nível, classe, skills ou atributos encerra efeitos/habilidades anteriores para evitar carregar invocações ou modificadores de outra configuração. Trocar classe preserva as recargas de sessão de cada classe; use limpar se quiser reiniciá-las. Recursos e recargas são controles independentes; a prévia de habilidade e a HUD refletem os valores efetivos.

## Limites e preservação

O modo `/rpg teste` continua separado: nível 100, classe livre, skills/atributos máximos e profissões 20, **com custos e recargas normais**. Entrar no teste limpa as opções de debug. Editar opções de debug exige ativar o debug; elas não passam a funcionar no teste.

Dados de debug são por UUID e só duram a sessão. Perfil real, classe, nível, XP, árvore e timers de skills/item de classe não são sobrescritos pelos comandos. Mana e esquiva de teste/debug usam pools separados; desligar o modo restaura o pool normal. Kills em debug não concedem XP RPG ao perfil real. Saúde/comida restauradas são ações reais sobre o personagem; itens e drops não são apagados ao sair. Profissões e missões têm seus próprios eventos: esta entrega não promete desfazer toda ação realizada no mundo.

O perfil efetivo recebe um marcador Java `temporary` que permite combinações de nível/skills/atributos fora das regras de progressão. ProfileStore rejeita a gravação desses perfis. Perfis normais mantêm as validações estritas e o codec persistente de 13 campos; o marcador não é adicionado ao formato salvo. O cliente também constrói corretamente a prévia de um perfil temporário de baixo nível, evitando erro de validação da árvore.

O nível escolhido orienta criaturas que forem geradas depois, inclusive as patrulhas que usam o perfil efetivo. Criaturas existentes mantêm seu nível. Recuperação de item vinculada à morte conserva seu registro salvo; bypass de recarga permite usá-lo no debug sem apagar esse histórico.

Desligar recargas não remove requisitos físicos (estar no ar/água, alvo válido, mãos livres, não atacar no enxame), duração dos efeitos nem intervalos curtos que protegem boosts/detonações em andamento. Somente itens Oxy têm seu cooldown nativo ignorado/limpo; outros mods e vanilla preservam seus timers. Consumo de stacks de poções e outros itens continua nativo.

## Implementação e evidência

DebugControls concentra comandos; DebugScreen oferece painel; RpgDebug separa nível, classe, árvore, atributos, recursos e recargas. RpgResources/RpgEvasion isolam pools e ClassRelics usa timers de ação separados. DebugItemCooldownMixin aplica o controle a consumíveis Oxy em cliente e servidor, sem afetar outros namespaces. DebugState sincroniza opções; protocolo 25 exige a mesma versão no servidor e clientes.

Compilação assemble concluída. Revisão de registros, codecs, construção de perfil, persistência, reset/logout, classes, mana, esquiva, itens e prévia. Sem testes, GameTests nem lançamento do jogo, conforme AGENTS.md. Validação pessoal pendente: alternar recargas e recursos, baixar nível mantendo skills completas, trocar classes, sair do debug e confirmar retorno ao perfil, e verificar o modo teste com custos normais.

Instalação apenas com o jogo fechado e backup; registro e SHA-256 em INSTALACAO_037.json. Fontes/hashes em INVENTARIO_CODIGO_037.json. Nenhuma biblioteca nova ou geração de mundo alterada.
