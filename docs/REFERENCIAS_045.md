# Referências e integrações — 12/10/2026

Pesquisa geral do CurseForge retornou erro; as páginas individuais indicadas foram acessadas. Nenhum mod novo foi instalado e nenhum arquivo de terceiros foi copiado. Compatibilidade do projeto não equivale à compatibilidade de um artefato específico.

| Projeto | Avaliação |
|---|---|
| [Better Combat](https://www.curseforge.com/minecraft/mc-mods/better-combat-by-daedelus) | Candidato prioritário: combate/animações completas via integração e PAL; auditar parry, miss/crit, arma da classe e alcance antes de ativar. Página lista All Rights Reserved: não extrair assets automaticamente. |
| [Terralith](https://www.curseforge.com/minecraft/mc-mods/terralith) | Mundo/biomas: integração opcional em mundo de avaliação; custo de geração e biomas do minimapa. Preferir pacote completo compatível a recortar worldgen. |
| [Combat Roll](https://www.curseforge.com/minecraft/mc-mods/combat-roll) | Referência de animação de esquiva e atributos; sistema Oxy já tem cargas e recarga. Integração precisaria evitar duas esquivas. Licença GPLv3: não copiar código para Oxy sem avaliar condições. |
| [Presence Footsteps](https://www.curseforge.com/minecraft/mc-mods/presence-footsteps-forge) | Bom candidato para áudio por superfície; página Forge não prova NeoForge. Conferir artefato exato antes de integração; MIT, atribuição necessária se houver reutilização. |
| [Better Villages](https://www.curseforge.com/minecraft/mc-mods/better-village-forge) | Referência visual de vilas e expansão. Conferir compatibilidade de pools com expansão Oxy e estruturas instaladas; não sobrepor cegamente. |
| [Common Network](https://www.curseforge.com/minecraft/mc-mods/common-network) | Biblioteca de rede unificada; Oxy só usa NeoForge e já tem payloads. Pouco benefício imediato em reescrever protocolo; MIT. |
| [Spyglass Improvements](https://www.curseforge.com/minecraft/mc-mods/spyglass-improvements) | Referência para zoom/acesso à luneta; periférico às correções de mira de skills. |
| [Dynamic Lights](https://www.curseforge.com/minecraft/mc-mods/tschipcrafts-dynamic-lights-mod) | Implementa blocos light no servidor. Autor documenta atualizações discretas e possíveis blocos residuais; evitar para muitos projéteis/chamas móveis, estudar alternativa só no cliente. |
| [Borderless Window](https://www.curseforge.com/minecraft/mc-mods/borderless) | Conforto da janela, instalação independente; não melhora classes nem requer portar código para Oxy. |
| [Biome Dither](https://www.curseforge.com/minecraft/mc-mods/puffish-biome-dither) | Transição de blocos nas bordas de biomas; não corrige iluminação do mato animado. Interessante com worldgen, avaliar desempenho primeiro. |
| [Spawn Animations](https://www.curseforge.com/minecraft/mc-mods/spawn-animations-mod) | Referência de nascer do chão/materializar. Oxy já tem entrada visual; instalação simultânea pode duplicar/hide animações. Adaptar nossa implementação própria. |
| [More Mobs](https://www.curseforge.com/minecraft/mc-mods/mod-more-mobs) | Variantes e comportamentos naturais; avaliar interações com níveis, drops, patrulhas e disfarces antes de integrar. |
| [Identity 2](https://www.curseforge.com/minecraft/mc-mods/identity2) | Referência de arquitetura de transformação, dimensões e sincronização. Página oferece NeoForge 1.21.10, mas All Rights Reserved: não extrair código/assets. Integração completa acrescentaria menu/skills concorrentes. |
| [Homesteads](https://www.curseforge.com/minecraft/mc-mods/homesteads) | Expansão de aldeões/estruturas/profissões. Bom candidato futuro; conferir missões e trocas customizadas, geração e artefato 1.21.10. |
| [Simple Health Bar](https://www.curseforge.com/minecraft/mc-mods/simple-health-bar) | Referência de barra e cores por condição. Página descreve Forge até 1.20.1 / Fabric até 1.21.3: não instalar como compatível com nossa versão. |
| [Villager Guards](https://www.curseforge.com/minecraft/mc-mods/villager-guards) | Não instalar no mundo atual: autor informa conversão de aldeões existentes com perda de trocas e remoção não restaura; referência para guardas próprios separados. AGPLv3. |
| [Nickname Easy](https://www.curseforge.com/minecraft/mc-mods/nickname-easy) | Cosmético, prioridade baixa; não resolve modelos ou HUD de combate. |
| [Eating Animation Fork](https://www.curseforge.com/minecraft/mc-mods/eating-animation-fork) | Candidato client-side para alimentos/bebidas e futuras poções de mana; MIT. Não oferece por si animação esquelética de ataques ou mordida. Conferir arquivo exato NeoForge 1.21.10. |

Prioridade recomendada: PAL + integração Better Combat; áudio por superfície; expansão opcional de mundo/aldeões depois. Correções de IA, mira e partículas nesta versão são implementações próprias e recursos nativos, sem dependência nova.
