# Oxy RPG — instruções locais

- Alvo: Minecraft 1.21.10, NeoForge 21.10.64, JDK 21.
- Preserve mundos, UUIDs, perfis existentes e outros mods.
- Use build-windows.bat para compilar e empacotar (assemble), sem executar testes.
- O usuário prefere fazer a validação pessoalmente. Execute testes automatizados,
  GameTests ou abra o jogo para validação somente se ele solicitar explicitamente.
- Quando testes forem solicitados, build-windows.bat build executa a suíte.
- Instale na instância neoforge-1-21-10 do SK Launcher somente com Minecraft fechado.
- Preserve o JAR anterior fora de mods para evitar mods duplicados.
- Não importe caminhos, proxy ou certificados da nuvem para o Windows.
- Não afirme que uma versão funciona no jogo sem evidência dessa versão.
