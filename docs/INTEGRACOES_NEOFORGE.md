# Integrações NeoForge — Oxy RPG 0.25.0

## Configurar o balanceamento

No mundo local, abra **R → Ajustes** ou **Mods → Oxy RPG → Config**. O NeoForge apresenta as opções nativas e valida os limites. Em servidor remoto, as regras pertencem ao servidor; o cliente recebe os valores sincronizados.

Arquivo padrão: `config/oxyrpg-server.toml`. Um mundo pode ter seu próprio arquivo em `saves/<mundo>/serverconfig/oxyrpg-server.toml`. Opções visuais da câmera e HUD continuam nas telas específicas existentes.

Exemplo (os demais valores continuam com os padrões):

```toml
[combat]
missChance = 0.05
criticalChance = 0.10
criticalDamage = 1.5
playerDamageMultiplier = 1.0
parryWindowTicks = 6

[evasion]
rechargeSeconds = 6
secondChargeLevel = 25
thirdChargeLevel = 50

[difficulty]
mobHealthMultiplier = 1.0
mobDamageMultiplier = 1.0
eliteHealthMultiplier = 1.0
eliteDamageMultiplier = 1.0
eliteChance = 0.05

[skills]
manaCostMultiplier = 1.0
cooldownMultiplier = 1.0

[exploration]
villageLootMultiplier = 1.0

[development]
profiling = false
```

Chances usam frações: 0,10 representa 10%. Multiplicadores de dificuldade iguais a 1 mantêm o balanceamento da 0.24.0. Loot de vila permanece com retenção de 60%; o multiplicador é aplicado sobre essa retenção, limitada a 100%. Configurações recarregadas atualizam os mobs carregados preservando a proporção de vida. Mudanças de recarga passam a valer para novas ativações, sem apagar recargas já em andamento.

## Classes, árvores e missões por datapack

Definições geradas ficam em `src/generated/resources/data/oxyrpg/rpg/`. No datapack do mundo, substitua o mesmo caminho `data/oxyrpg/rpg/` e use `/reload`.

- `classes/guerreiro.json`, `mago.json`, `arqueiro.json`, `necromante.json`, `xama.json`, `aviario.json`: nome exibido, multiplicadores de dano, mana, recarga e duração; título, descrição, ícone e posição de cada nó.
- `quests.json`: títulos, recompensas, mobs hostis, nomes e ícones dos objetivos.
- As definições são validadas antes da troca e enviadas aos clientes pelo servidor. Uma definição inválida faz a recarga falhar; não substitui o pacote ativo por dados incompletos.
- IDs dos 51 nós, pré-requisitos, requisitos de nível, quantidade de missões e contagens dos objetivos permanecem estáveis para preservar saves. Alterar o mob de um objetivo mantém sua contagem atual.
- A lógica de cada habilidade e os nove tipos de classe continuam em Java. Arquivos não executam código nem criam comportamentos novos por si só.

Para alterar a frequência de vilas, substitua `data/minecraft/worldgen/structure_set/villages.json`. Novas colocações afetam terreno ainda não gerado. A retenção de loot também é definida em `data/oxyrpg/loot_modifiers/village_loot.json`.

## Data Maps

Caminhos no datapack:

- `data/oxyrpg/data_maps/entity_type/mob_tuning.json`: `health`, `damage` e `xp` são multiplicadores para criaturas hostis; `fire_resistance`, `frost_resistance` e `lightning_resistance` são frações entre 0 e 1. Resistências correspondem aos tipos de dano fogo, congelamento e raio.
- `data/oxyrpg/data_maps/item/weapon_tuning.json`: `damage` multiplica golpes diretos; `critical_bonus` adiciona uma fração à chance de crítico. Mobs, armas e tags de outros mods podem ser usados.

Exemplo:

```json
{"values":{"minecraft:zombie":{"health":1.2,"damage":1.1,"xp":1.15,"fire_resistance":0.25}}}
```

Os arquivos distribuídos são neutros, sem rebalanceamento adicional. Data Maps são recarregados com os datapacks; o mapa de armas também é sincronizado.

## Perfis nativos e migração

O perfil real passa a usar o Data Attachment `oxyrpg:profile`, salvo junto aos dados do jogador pelo Minecraft e copiado automaticamente após a morte. O conteúdo inclui classe, nível, XP, caçadas, árvore, atributos e recargas.

A primeira leitura importa `oxyrpg/<UUID>.properties` usando o validador existente. O arquivo original permanece intacto. Um arquivo antigo ilegível não é silenciosamente substituído por um personagem novo. Teste/debug continuam temporários e não substituem o perfil real.

Após a migração, o anexo é a fonte do perfil. Use `/rpg exportarperfil` (operador) para salvar uma cópia atual em `oxyrpg/exports/`. Profissões, contratos e mana mantêm seus arquivos existentes nesta versão; não foi feita uma migração desnecessária desses sistemas.

## GeckoLib e selo animado

Dependência fixada: GeckoLib **5.3-alpha-3**, para Minecraft **1.21.10 / NeoForge**. Deve acompanhar o JAR do Oxy RPG nos dois lados.

O item `oxyrpg:sigil` tem cristal flutuante e anel giratório, com modelo e animação próprios. Obtenha com `/rpg selo` ou faça a receita com quatro barras de ouro em cruz e uma ametista no centro. É cosmético: não substitui armas, não aumenta atributos e não é obrigatório para usar skills.

Arquivos do selo: `assets/oxyrpg/geckolib/models/`, `geckolib/animations/`, `textures/item/` e `items/`. Podem ser substituídos por resource packs.

## Geração de dados

Execute `build-windows.bat runData`, depois `build-windows.bat`. O gerador roda sem janela e escreve definições de classe, missões, receita e Data Maps em `src/generated/resources`. Os arquivos gerados entram no JAR. Edite um datapack para customizações locais; regenerar substitui os padrões gerados.

## Desempenho

Seções nativas: `oxyrpg_abilities`, `oxyrpg_elites`, `oxyrpg_crater` e `oxyrpg_minimap`. Podem ser observadas pelos mecanismos de profiling do Minecraft.

Para médias e picos locais, ative `development.profiling` e execute `/rpg desempenho`. `/rpg desempenho limpar` reinicia as amostras. São tempos de execução de cada seção, não FPS nem TPS. Não existe envio automático de dados para serviços externos.

## GameTests e testes de regressão

GameTests registrados: `oxyrpg:profile`, `oxyrpg:mana`, `oxyrpg:evasion` e `oxyrpg:parry`. Verificam armazenamento de um perfil com atributos e cópia após morte, consumo/limite de mana, gasto de carga e bloqueio preciso de um ataque real com escudo. Usam uma arena própria com piso e jogadores simulados.

Quando a execução for desejada, `build-windows.bat runGameTestServer` usa uma pasta isolada `run-gametest`, sem abrir mundos do SKlauncher. Em ambiente de desenvolvimento com GameTests habilitados, também podem ser executados pelo comando `/test`.

Os testes JUnit existentes foram atualizados para a árvore de 51 nós e para os novos pacotes. `build-windows.bat build` executa a suíte. Nesta entrega foram compilados, sem execução automática: a preferência registrada é que o usuário solicite a execução dos testes explicitamente.

## Documentação de referência

- https://docs.neoforged.net/docs/1.21.10/misc/config/
- https://docs.neoforged.net/docs/1.21.10/datastorage/attachments/
- https://docs.neoforged.net/docs/1.21.10/resources/server/datamaps/
- https://docs.neoforged.net/docs/1.21.10/misc/gametest/
- https://wiki.geckolib.com/docs/geckolib5/
