package dev.emersonoxy.rpg;

public enum RpgClass {
    NONE("Sem classe"), GUERREIRO("Guerreiro"), MAGO("Mago"), ARQUEIRO("Arqueiro"), NECROMANTE("Necromante"), XAMA("Xamã"), AVIARIO("Aviário"), ILUSIONISTA("Ilusionista"), HIDROMANTE("Hidromante"), PIROMANTE("Piromaníaco"), VAMPIRESCO("Vampiresco");
    private final String label;
    RpgClass(String label) { this.label = label; }
    public String defaultLabel(){return label;}
    public String label() { return RpgContent.label(this,label); }
}


