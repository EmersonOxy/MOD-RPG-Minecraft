package dev.emersonoxy.rpg;

import java.nio.file.*;
import java.io.IOException;
import java.util.UUID;

public final class ProgressionTest {
    private static int checks;
    private static void check(boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("Check " + checks);
    }
    public static void main(String[] args) throws Exception {
        RpgProfile profile = RpgProfile.NEW;
        check(profile.defeatedEnemy().equals(profile));
        profile = profile.choose(RpgClass.GUERREIRO);
        check(profile.gainXp(99).level() == 1);
        check(profile.gainXp(100).level() == 2);
        check(profile.gainXp(300).level() == 3);
        check(profile.gainXp(Integer.MAX_VALUE).level() == 100);
        check(profile.gainXp(Integer.MAX_VALUE).xp() == 0);
        try { profile.choose(RpgClass.MAGO); throw new AssertionError("Class changed"); }
        catch (IllegalStateException expected) { checks++; }
        for (int i = 0; i < 10; i++) profile = profile.defeatedEnemy();
        check(profile.kills() == 10 && profile.level() == 3 && profile.xp() == 50);
        check(profile.defeatedEnemy().xp() == 75);
        RpgProfile used = profile.useAbility(100);
        check(used.abilityReadyAt() == 300);
        try { used.useAbility(299); throw new AssertionError("Cooldown skipped"); }
        catch (IllegalStateException expected) { checks++; }
        check(used.useAbility(300).abilityReadyAt() == 500);
        Path directory = Files.createTempDirectory("oxyrpg-test-");
        try {
            UUID id = UUID.randomUUID();
            ProfileStore store = new ProfileStore(directory);
            check(store.load(id).equals(RpgProfile.NEW));
            store.save(id, used);
            check(new ProfileStore(directory).load(id).equals(used));
            store.save(id, used.defeatedEnemy());
            check(store.load(id).kills() == 11);
            check(store.load(UUID.randomUUID()).equals(RpgProfile.NEW));
            Files.writeString(directory.resolve(id + ".properties"), "class=invalid\n");
            try { store.load(id); throw new AssertionError("Corruption hidden"); }
            catch (IOException expected) { checks++; }
        } finally {
            try (var files = Files.list(directory)) {
                for (Path file : files.toList()) Files.delete(file);
            }
            Files.delete(directory);
        }
        System.out.println("PASS: " + checks + " progression and persistence checks");
    }
}

