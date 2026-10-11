# Oxy RPG 0.27

- Itens de classe registrados separadamente, com ação no botão direito; removido o atalho C que conflitava com habilidades remapeadas. O arco mantém carga/disparo normal e prepara o efeito quando disponível.
- Migração dos kits antigos conserva dono, classe, etapa e componentes. Vínculo e recuperação de 60 s após a morte mantidos. Efeitos continuam custando 10 MP e recarregam em 35/30/25 s; somente DEBUG ignora recursos.
- Enxame: Espaço sobe, Shift desce; flutua até 5 blocos do terreno (8 com último aprimoramento). Atravessa blocos comuns, incluindo pedra, terra, madeira e vidro. Blocos indestrutíveis, portais e blocos com inventário/entidade de bloco conservam colisão. Ataques, itens e interação continuam bloqueados; expiração restaura colisão e retorna à última posição livre se necessário.
- Mímico impede seleção/revalidação do jogador como alvo pelos mobs enquanto o disfarce durar. Não concede imunidade: perigos ambientais e contato acidental continuam podendo ferir. Seleção pela mira considera a caixa do mob, incluindo criaturas pacíficas não domesticadas. Aldeões, pets e chefes permanecem excluídos.
- Morcegos e corpo visual do disfarce deixam de mostrar barras/nomes que poluíam a tela. Cópias de combate conservam indicadores de vida.
- Ataques diretos e projéteis de mobs têm 5% de erro e 10% de crítico (1,5x), configuráveis no servidor. Erro azul e crítico vermelho aparecem junto ao atacante; dano ambiental não recebe essas rolagens. Invocações aliadas não usam essa regra contra jogadores.
- Reaparecimento restaura a vida máxima RPG; nível 100 mostra Nível máximo. HUD/minimapa não sobrepõem o painel F3. Árvore do Hidromante redistribuída em três correntes.
- Curios API 13.0.0+1.21.10 obrigatório: slot Relíquia no inventário Curios aceita o Selo animado. Sem bônus de combate adicional nesta etapa.

## Bibliotecas visuais

Photon/LDLib2 têm documentação voltada ao Minecraft 1.21.1/NeoForge 21.1. Modern UI oficial não confirmou um arquivo compatível com 1.21.10 na verificação desta versão. Não foram incluídos builds destinados a outra versão. GeckoLib continua responsável pelas animações existentes.

## Validação

Compilação assemble e geração de dados runData concluídas com sucesso. Testes e abertura do jogo somente sob solicitação do usuário, conforme AGENTS.md. Validação visual e de jogabilidade pendente do teste pessoal.
