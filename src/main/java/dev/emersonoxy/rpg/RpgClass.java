package dev.emersonoxy.rpg;

public enum RpgClass {
    NONE("Sem classe"), GUERREIRO("Guerreiro"), MAGO("Mago"), ARQUEIRO("Arqueiro"), NECROMANTE("Necromante"), XAMA("Xamã");
    private final String label;
    RpgClass(String label) { this.label = label; }
    public String label() { return label; }
}


