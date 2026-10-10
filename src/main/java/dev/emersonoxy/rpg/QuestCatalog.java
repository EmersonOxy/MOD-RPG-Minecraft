package dev.emersonoxy.rpg;

import java.util.List;

/** Stable recurring contracts; exact entity ids keep objectives unambiguous. */
public final class QuestCatalog {
    public record Target(String mob, String label, int amount, String icon) {}
    public record Quest(String title, int reward, List<Target> targets) {}
    private static Target target(String mob, String label, int amount, String icon) {
        return new Target("minecraft:" + mob, label, amount, icon);
    }
    private static final List<Quest> QUESTS = List.of(
        new Quest("Vigília noturna", 100, List.of(target("zombie", "Zumbis", 5, "rotten_flesh"))),
        new Quest("Ossos e teias", 150, List.of(target("skeleton", "Esqueletos", 4, "bone"), target("spider", "Aranhas", 3, "string"))),
        new Quest("Caça explosiva", 150, List.of(target("creeper", "Creepers", 3, "gunpowder"))),
        new Quest("Patrulha da noite", 200, List.of(target("zombie", "Zumbis", 4, "rotten_flesh"), target("skeleton", "Esqueletos", 4, "bone"))),
        new Quest("Perigo nas sombras", 175, List.of(target("spider", "Aranhas", 5, "spider_eye"), target("creeper", "Creepers", 2, "gunpowder")))
    );
    private QuestCatalog() {}
    public static Quest quest(int stage) { return QUESTS.get(Math.floorMod(stage, QUESTS.size())); }
    public static boolean valid(int stage, int first, int second) {
        if (stage < 0 || first < 0 || second < 0) return false;
        var targets = quest(stage).targets();
        if (first > targets.get(0).amount()) return false;
        if (targets.size() == 1) return second == 0 && first < targets.get(0).amount();
        return second <= targets.get(1).amount() && (first < targets.get(0).amount() || second < targets.get(1).amount());
    }
    public static String progress(int stage, int first, int second) {
        var quest = quest(stage);
        String text = quest.targets().get(0).label() + " " + first + "/" + quest.targets().get(0).amount();
        if (quest.targets().size() == 2) text += " • " + quest.targets().get(1).label() + " " + second + "/" + quest.targets().get(1).amount();
        return text;
    }
}
