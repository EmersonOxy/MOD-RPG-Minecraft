package dev.emersonoxy.rpg;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.UUID;

/** One file per UUID, inside each world; never replaces unreadable profiles with defaults. */
public final class ProfileStore {
    private final Path directory;
    public ProfileStore(Path directory) { this.directory = directory; }

    public RpgProfile load(UUID id) throws IOException {
        Path file = directory.resolve(id + ".properties");
        if (Files.notExists(file)) return RpgProfile.NEW;
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { data.load(reader); }
        try {
            return new RpgProfile(RpgClass.valueOf(data.getProperty("class")),
                Integer.parseInt(data.getProperty("level")), Integer.parseInt(data.getProperty("xp")),
                Integer.parseInt(data.getProperty("kills")), Long.parseLong(data.getProperty("abilityReadyAt")),
                "2".equals(data.getProperty("skillTreeVersion")) ? SkillTree.migrate(Long.parseLong(data.getProperty("skills", "0"))) : "3".equals(data.getProperty("skillTreeVersion")) ? Long.parseLong(data.getProperty("skills", "0")) : 0, Integer.parseInt(data.getProperty("questStage", "0")),
                Integer.parseInt(data.getProperty("questFirst", "0")), Integer.parseInt(data.getProperty("questSecond", "0")), Long.parseLong(data.getProperty("readySecond", "0")), Long.parseLong(data.getProperty("readyThird", "0")),
                new AttributeStats(Integer.parseInt(data.getProperty("attributeHealth","0")),Integer.parseInt(data.getProperty("attributeDamage","0")),Integer.parseInt(data.getProperty("attributeLuck","0")),Integer.parseInt(data.getProperty("attributeArmor","0")),Integer.parseInt(data.getProperty("attributeMana","0"))),data.getProperty("attributeNodes",""));
        } catch (RuntimeException e) { throw new IOException("Perfil RPG inválido: " + file, e); }
    }

    public void save(UUID id, RpgProfile profile) throws IOException {
        Files.createDirectories(directory);
        Properties data = new Properties();
        data.setProperty("class", profile.rpgClass().name());
        data.setProperty("level", Integer.toString(profile.level()));
        data.setProperty("xp", Integer.toString(profile.xp()));
        data.setProperty("kills", Integer.toString(profile.kills()));
        data.setProperty("abilityReadyAt", Long.toString(profile.abilityReadyAt()));
        data.setProperty("skillTreeVersion", Integer.toString(SkillTree.VERSION));
        data.setProperty("readySecond", Long.toString(profile.readySecond()));
        data.setProperty("readyThird", Long.toString(profile.readyThird()));
        data.setProperty("skills", Long.toString(profile.skills()));
        data.setProperty("attributeNodes",profile.attributeNodes());
        data.setProperty("attributeHealth",Integer.toString(profile.attributes().health()));
        data.setProperty("attributeDamage",Integer.toString(profile.attributes().damage()));
        data.setProperty("attributeLuck",Integer.toString(profile.attributes().luck()));
        data.setProperty("attributeArmor",Integer.toString(profile.attributes().armor()));
        data.setProperty("attributeMana",Integer.toString(profile.attributes().mana()));
        data.setProperty("questStage", Integer.toString(profile.questStage()));
        data.setProperty("questFirst", Integer.toString(profile.questFirst()));
        data.setProperty("questSecond", Integer.toString(profile.questSecond()));
        Path file = directory.resolve(id + ".properties");
        Path temp = Files.createTempFile(directory, id.toString(), ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                data.store(writer, "Oxy RPG player profile");
            }
            // Require an atomic replacement: keep the existing profile if the filesystem cannot provide it.
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temp); }
    }
}


