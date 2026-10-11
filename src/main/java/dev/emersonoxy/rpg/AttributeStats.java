package dev.emersonoxy.rpg;

/** Permanent point allocation. One point per level gained, independently of skill points. */
public record AttributeStats(int health, int damage, int luck, int armor, int mana) {
    public static final AttributeStats EMPTY = new AttributeStats(0,0,0,0,0);
    public AttributeStats { if(health<0||damage<0||luck<0||armor<0||mana<0||health>99||damage>99||luck>99||armor>99||mana>99)throw new IllegalArgumentException("Atributos inválidos"); }
    public int total(){return health+damage+luck+armor+mana;}
    public int value(int id){return switch(id){case 0->health;case 1->damage;case 2->luck;case 3->armor;case 4->mana;default->throw new IllegalArgumentException("Atributo inválido");};}
    public AttributeStats add(int id){return new AttributeStats(health+(id==0?1:0),damage+(id==1?1:0),luck+(id==2?1:0),armor+(id==3?1:0),mana+(id==4?1:0));}
    public int maxMana(){return 100+mana*5;}
    public static String label(int id){return switch(id){case 0->"Vida";case 1->"Dano";case 2->"Sorte";case 3->"Resistência";case 4->"Mana";default->"";};}
    public static String benefit(int id){return switch(id){case 0->"+1 coração";case 1->"+2% de dano";case 2->"+0,2% drop raro; +0,25% crítico; -0,1% erro";case 3->"+0,5 de armadura";case 4->"+5 de mana máxima";default->"";};}
}
