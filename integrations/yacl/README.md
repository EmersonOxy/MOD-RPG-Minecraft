# YACL metadata adjustment — 2026-10-11

YACL 3.8.2+1.21.10-neoforge by isXander, LGPL-3.0-or-later.
Source: https://github.com/isXander/YetAnotherConfigLib
Original artifact SHA1: 9f39f765d26d380a8258569d75af8e96e32bafda

Only META-INF/neoforge.mods.toml changed: NeoForge versionRange `[21.9,21.10]` became `[21.10.64,21.11)`. Original range excludes 21.10.64. Java code, Minecraft range and assets unchanged. A provenance note is embedded in the adjusted JAR.

Reproduce with Python zipfile: copy each ZipInfo and its contents to a new ZIP; for META-INF/neoforge.mods.toml replace the exact string above. Preserve the original artifact outside the Minecraft mods directory.
