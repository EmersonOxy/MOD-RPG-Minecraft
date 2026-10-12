package dev.emersonoxy.rpg;

public enum RpgClass {
    NONE("Sem classe"), GUERREIRO("Guerreiro"), MAGO("Mago"), ARQUEIRO("Assassino"), NECROMANTE("Necromante"), XAMA("Xamã"), AVIARIO("Aviário"), ILUSIONISTA("Ilusionista"), HIDROMANTE("Hidromante"), PIROMANTE("Piromaníaco"), VAMPIRESCO("Vampiresco"), ARQUEIRO_NOVO("Arqueiro"), DEUS_TROVAO("Deus do Trovão");
    private final String label;
    RpgClass(String label) { this.label = label; }
    public String command(){return this==ARQUEIRO?"assassino":this==ARQUEIRO_NOVO?"arqueiro":name().toLowerCase(java.util.Locale.ROOT);}
    public String defaultLabel(){return label;}
    public String label() { return RpgContent.label(this,label); }
}


