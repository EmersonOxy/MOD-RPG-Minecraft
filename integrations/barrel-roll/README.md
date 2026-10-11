# Port independente do Do Another Barrel Roll

GPL-3.0-only: a licença nesta pasta aplica-se ao patch e código do port, separados do código Oxy RPG.

Upstream: https://github.com/aspctt/do-a-barrel-roll-neoforge.git
Commit de origem: `838ddb76824b8c723971761f1b0a0bb655331d5b`

Reproduzir em um clone separado:

```powershell
git clone https://github.com/aspctt/do-a-barrel-roll-neoforge.git DoAnotherBarrelRoll
git -C DoAnotherBarrelRoll checkout 838ddb76824b8c723971761f1b0a0bb655331d5b
git -C DoAnotherBarrelRoll apply <caminho-absoluto>/neoforge-1.21.10.patch
```

Depois execute `build-windows.bat` no clone. JAR e sources JAR em versions/1.21.10/build/libs. Preserve LICENSE e os créditos dos autores ao distribuir. Consulte PORT_1_21_10.md criado pelo patch. Esta adaptação é local, não oficial.
