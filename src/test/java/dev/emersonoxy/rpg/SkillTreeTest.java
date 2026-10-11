package dev.emersonoxy.rpg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

final class SkillTreeTest {
    @TempDir Path directory;
    private RpgProfile veteran(RpgClass type) { return new RpgProfile(type, 100, 0, 20, 700); }

    private RpgProfile learn(RpgProfile p,int id){if(p.hasSkill(id))return p;long parents=SkillTree.parents(id);for(int n=0;n<SkillTree.COUNT;n++)if((parents&(1L<<n))!=0)p=learn(p,n);return p.unlockSkill(id);}
    @Test void noClassOrInsufficientLevelCannotBuySkills() {
        assertThrows(IllegalStateException.class, () -> RpgProfile.NEW.unlockSkill(2));
        assertThrows(IllegalStateException.class, () -> RpgProfile.NEW.choose(RpgClass.GUERREIRO).unlockSkill(2));
    }
    @Test void prerequisitesAndPointBudgetAreEnforced() {
        var low = new RpgProfile(RpgClass.GUERREIRO, 5, 0, 0, 0);
        assertEquals(2, low.skillPoints());
        var bought = low.unlockSkill(2);
        assertEquals(1, bought.skillPoints());
        assertThrows(IllegalStateException.class, () -> new RpgProfile(RpgClass.GUERREIRO,2,0,0,0).unlockSkill(7));
        assertThrows(IllegalStateException.class, () -> bought.unlockSkill(2));
        assertThrows(IllegalStateException.class, () -> veteran(RpgClass.GUERREIRO).unlockSkill(4));
        var branch = learn(veteran(RpgClass.GUERREIRO),4);
        assertThrows(IllegalStateException.class, () -> branch.unlockSkill(6));
    }
    @Test void allThreeTreesCanBeCompletedAndHaveIndependentCooldowns() {
        for (var type : new RpgClass[] {RpgClass.GUERREIRO, RpgClass.MAGO, RpgClass.ARQUEIRO}) {
            var profile = veteran(type);
            assertTrue(profile.hasSkill(0)); assertTrue(profile.hasSkill(1));
            assertEquals(3, profile.abilityDamage());
            for (int id = 2; id < SkillTree.COUNT; id++) profile = learn(profile,id);
            assertEquals(1, profile.skillPoints());
            assertEquals(type == RpgClass.MAGO ? 6 : 8, profile.abilityDamage());
            assertEquals(410, profile.abilityCooldown(0));
            var first = profile.useAbility(800, 0);
            assertEquals(800+profile.abilityCooldown(0), first.readyAt(0));
            assertEquals(0, first.readyAt(1));
            var second = first.useAbility(801, 1);
            assertEquals(first.readyAt(0), second.readyAt(0));
            assertTrue(second.readyAt(1) > 801);
            assertEquals(0, second.readyAt(2));
            assertEquals(SkillTree.COUNT, SkillTree.nodes(type).size());
        }
    }
    @Test void resetClearsLevelsExperienceSkillsAndAllCooldownsButPreservesHunts() {
        var before = learn(veteran(RpgClass.MAGO),3);
        var reset = before.resetClass();
        assertEquals(RpgClass.NONE, reset.rpgClass());
        assertEquals(1, reset.level()); assertEquals(0, reset.xp());
        assertEquals(before.kills(), reset.kills());
        assertEquals(before.questStage(), reset.questStage());
        assertEquals(0, reset.skillPoints());
        for (int slot = 0; slot < 3; slot++) assertEquals(0, reset.readyAt(slot));
        var switched = reset.choose(RpgClass.ARQUEIRO);
        assertEquals(0, switched.skills());
        assertEquals(200, switched.useAbility(0).abilityReadyAt());
    }
    @Test void skillDataSurvivesPersistenceAndProgression() throws Exception {
        var id = UUID.randomUUID();
        var profile = learn(veteran(RpgClass.MAGO),3).unlockSkill(7).unlockSkill(8).useAbility(800, 1);
        var store = new ProfileStore(directory);
        store.save(id, profile);
        assertEquals(profile, new ProfileStore(directory).load(id));
        assertEquals(profile.skills(), profile.defeatedEnemy().skills());
        assertEquals(profile.skills(), profile.gainXp(10000).skills());
    }
    @Test void oldTreeRefundsPointsWithoutChangingExistingProgress() throws Exception {
        var id = UUID.randomUUID();
        Files.writeString(directory.resolve(id + ".properties"), "class=GUERREIRO\nlevel=5\nxp=50\nkills=30\nabilityReadyAt=600\nskills=2\n");
        var store = new ProfileStore(directory);
        var profile = store.load(id);
        assertEquals(new RpgProfile(RpgClass.GUERREIRO, 5, 50, 30, 600), profile);
        assertEquals(2, profile.skillPoints());
        var upgraded = profile.unlockSkill(2);
        store.save(id, upgraded);
        assertEquals(upgraded, store.load(id));
    }
    @Test void invalidSkillDataIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RpgProfile(RpgClass.GUERREIRO, 30, 0, 0, 0, 16));
        assertThrows(IllegalArgumentException.class, () -> new RpgProfile(RpgClass.NONE, 30, 0, 0, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> new RpgProfile(RpgClass.GUERREIRO, 5, 0, 0, 0, 12));
        assertThrows(IllegalStateException.class, () -> veteran(RpgClass.MAGO).unlockSkill(99));
    }
}
