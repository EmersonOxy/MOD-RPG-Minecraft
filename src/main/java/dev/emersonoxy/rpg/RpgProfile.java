package dev.emersonoxy.rpg;

/** Immutable progression: RPG experience is independent of vanilla experience. */
public record RpgProfile(RpgClass rpgClass, int level, int xp, int kills, long abilityReadyAt, long skills,
                         int questStage, int questFirst, int questSecond, long readySecond, long readyThird, AttributeStats attributes, String attributeNodes, boolean temporary) {
    public static final int MAX_LEVEL = 100;
    public static final int ABILITY_DURATION_TICKS = 300;
    public static final int ABILITY_COOLDOWN_TICKS = 600;
    public static final RpgProfile NEW = new RpgProfile(RpgClass.NONE, 1, 0, 0, 0);

    public RpgProfile(RpgClass rpgClass, int level, int xp, int kills, long abilityReadyAt) {
        this(rpgClass, level, xp, kills, abilityReadyAt, 0);
    }
    public RpgProfile(RpgClass rpgClass, int level, int xp, int kills, long abilityReadyAt, long skills) {
        this(rpgClass, level, xp, kills, abilityReadyAt, skills, 0, 0, 0);
    }

    public RpgProfile(RpgClass rpgClass, int level, int xp, int kills, long abilityReadyAt, long skills,
                      int questStage, int questFirst, int questSecond) {
        this(rpgClass, level, xp, kills, abilityReadyAt, skills, questStage, questFirst, questSecond, 0, 0);
    }
    public RpgProfile(RpgClass rpgClass, int level, int xp, int kills, long abilityReadyAt, long skills,
                      int questStage, int questFirst, int questSecond, long readySecond, long readyThird) {
        this(rpgClass,level,xp,kills,abilityReadyAt,skills,questStage,questFirst,questSecond,readySecond,readyThird,AttributeStats.EMPTY);
    }
    public RpgProfile(RpgClass rpgClass,int level,int xp,int kills,long abilityReadyAt,long skills,int questStage,int questFirst,int questSecond,long readySecond,long readyThird,AttributeStats attributes){
        this(rpgClass,level,xp,kills,abilityReadyAt,skills,questStage,questFirst,questSecond,readySecond,readyThird,attributes,"");
    }
    public RpgProfile(RpgClass rpgClass,int level,int xp,int kills,long abilityReadyAt,long skills,int questStage,int questFirst,int questSecond,long readySecond,long readyThird,AttributeStats attributes,String attributeNodes){
        this(rpgClass,level,xp,kills,abilityReadyAt,skills,questStage,questFirst,questSecond,readySecond,readyThird,attributes,attributeNodes,false);
    }
    public RpgProfile allocateNode(java.util.UUID owner,int id){
        var nodes=AttributeTree.decode(attributeNodes);
        if(rpgClass==RpgClass.NONE||attributePoints()<1||!AttributeTree.available(nodes,id))throw new IllegalStateException("Escolha um nó conectado e tenha 1 ponto disponível.");
        nodes.set(id);
        return new RpgProfile(rpgClass,level,xp,kills,abilityReadyAt,skills,questStage,questFirst,questSecond,readySecond,readyThird,attributes.add(AttributeTree.type(owner,id)),AttributeTree.encode(nodes));
    }
    public int attributePoints(){return level-1-attributes.total();}
    public RpgProfile allocateAttribute(int id){
        if(id<0||id>4||rpgClass==RpgClass.NONE||attributePoints()<1)throw new IllegalStateException("Sem pontos de atributo disponíveis.");
        return new RpgProfile(rpgClass,level,xp,kills,abilityReadyAt,skills,questStage,questFirst,questSecond,readySecond,readyThird,attributes.add(id),attributeNodes);
    }
    public RpgProfile {
        if (attributes==null||attributeNodes==null||AttributeTree.decode(attributeNodes).cardinality()>attributes.total())throw new IllegalArgumentException("Árvore de atributos inválida");
        if (attributes == null || !temporary&&attributes.total()>level-1 || rpgClass == null || level < 1 || level > MAX_LEVEL || xp < 0
                || xp >= level * 100 || (level == MAX_LEVEL && xp != 0)
                || kills < 0 || abilityReadyAt < 0 || readySecond < 0 || readyThird < 0 || (temporary?(skills&~SkillTree.PAID_MASK)!=0:!SkillTree.validMask(level,skills))
                || (rpgClass == RpgClass.NONE && skills != 0) || !QuestCatalog.valid(questStage, questFirst, questSecond)) {
            throw new IllegalArgumentException("Perfil RPG inválido");
        }
    }

    public int xpToNextLevel() { return level == MAX_LEVEL ? 0 : level * 100; }
    public int skillPoints() { return level / 2 - Long.bitCount(skills); }
    public boolean hasSkill(int id) { return id >= 0 && id < SkillTree.COUNT && rpgClass != RpgClass.NONE && (id < 2 || (skills & (1L << id)) != 0); }
    public boolean canUseAbility(int slot) { return slot >= 0 && slot < 3 && hasSkill(slot * 7 + 1); }
    public boolean mainUnlocked(int slot) { return hasSkill(slot * 7 + 3); }
    public long readyAt(int slot) { return switch (slot) {case 0 -> abilityReadyAt; case 1 -> readySecond; case 2 -> readyThird; default -> throw new IllegalArgumentException("Habilidade inválida.");}; }
    public int abilityDuration() { return abilityDuration(0); }
    public int abilityDuration(int slot) {return (int)Math.round(baseDuration(slot)*(1+.05*SkillTree.bonus(skills,1))*RpgContent.durationMultiplier(rpgClass));}
    private int baseDuration(int slot) {
        return switch (rpgClass) {
            case GUERREIRO -> slot == 0 ? (mainUnlocked(0) ? 200 : 40) + (hasSkill(2) ? 20 : 0)
                : slot == 1 ? (mainUnlocked(1) ? 240 : 100) + (hasSkill(13) ? 60 : 0)
                : (mainUnlocked(2) ? 160 : 60) + (hasSkill(18) ? 20 : 0);
            case MAGO -> slot == 0 ? 160 + (hasSkill(5) ? 40 : 0) + (hasSkill(6) ? 40 : 0)
                : slot == 1 ? 60 : (mainUnlocked(2) ? 120 : 20) + (hasSkill(19) ? 40 : 0);
            case ARQUEIRO -> slot == 0 ? (mainUnlocked(0) ? 200 : 40) + (hasSkill(2) && !mainUnlocked(0) ? 20 : 0) + (hasSkill(5) ? 40 : 0)
                : slot == 1 ? (mainUnlocked(1) ? 200 : 60) + (hasSkill(11) ? 40 : 0)
                : mainUnlocked(2) ? 160 : 60;
            case NECROMANTE -> slot==0?80:slot==1?(mainUnlocked(1)?600:300):240;
            case AVIARIO -> slot==0?(mainUnlocked(0)?300:160)+(hasSkill(2)?40:0)+(hasSkill(4)?80:0)+(hasSkill(6)?(int)(240*level/100.0):0):slot==1?0:600;
            case ILUSIONISTA -> slot==0?(mainUnlocked(0)?240:120):slot==1?(mainUnlocked(1)?100:60):(mainUnlocked(2)?120:60)+(hasSkill(18)?40:0);
            case HIDROMANTE -> slot==0?60:slot==1?(mainUnlocked(1)?80:40):(mainUnlocked(2)?300:120);
            case VAMPIRESCO -> slot==0?(mainUnlocked(0)?200:120):slot==1?160:(mainUnlocked(2)?240:120);
            case PIROMANTE -> slot==0?200:slot==1?240:(mainUnlocked(2)?180:80)+(hasSkill(18)?60:0);
            case XAMA -> slot==0?(mainUnlocked(0)?300:100)+(hasSkill(4)?60:0):slot==1?(mainUnlocked(1)?200:100)+(hasSkill(9)?40:0)+(hasSkill(13)?100:0):(mainUnlocked(2)?300:160)+(hasSkill(18)?100:0);
            default -> 0;
        };
    }
    public int abilityCooldown() { return abilityCooldown(0); }
    public int abilityCooldown(int slot) {return (int)Math.round(baseCooldown(slot)*Math.max(.4,1-.03*SkillTree.bonus(skills,3))*RpgContent.cooldownMultiplier(rpgClass)*RpgConfig.value(RpgConfig.COOLDOWN,1));}
    private int baseCooldown(int slot) {
        if(rpgClass==RpgClass.VAMPIRESCO)return new int[]{25,35,45}[slot]*20;
        if(rpgClass==RpgClass.AVIARIO)return slot==0?abilityDuration(0)+200:slot==1?0:600;
        if(rpgClass==RpgClass.HIDROMANTE&&slot==0)return 100;
        if(rpgClass==RpgClass.PIROMANTE&&slot==1)return 120;
        if (!mainUnlocked(slot)) return 240;
        if(rpgClass==RpgClass.XAMA)return new int[]{30,40,35}[slot]*20;
        return (slot == 0 ? 25 : slot == 1 ? rpgClass == RpgClass.MAGO ? 30 : (rpgClass == RpgClass.GUERREIRO || rpgClass == RpgClass.NECROMANTE) ? 40 : 35
            : rpgClass == RpgClass.MAGO ? 40 : 45) * 20;
    }
    public int abilityDamage() { return rpgClass == RpgClass.NONE ? 0 : (mainUnlocked(0) ? rpgClass == RpgClass.MAGO ? 6 : 8 : 3); }
    public RpgProfile unlockSkill(int id) {
        if (id < 2 || id >= SkillTree.COUNT || rpgClass == RpgClass.NONE || hasSkill(id))
            throw new IllegalStateException("Essa skill não pode ser aprendida.");
        if (level < SkillTree.requiredLevel(id)) throw new IllegalStateException("Nível necessário: " + SkillTree.requiredLevel(id) + ".");
        if (((skills | SkillTree.FREE_MASK) & SkillTree.parents(id)) != SkillTree.parents(id))
            throw new IllegalStateException("Aprenda as skills anteriores primeiro.");
        if (skillPoints() < 1) throw new IllegalStateException("Sem pontos de skill. Você ganha 1 a cada 2 níveis.");
        return new RpgProfile(rpgClass, level, xp, kills, abilityReadyAt, skills | (1L << id), questStage, questFirst, questSecond, readySecond, readyThird, attributes,attributeNodes);
    }
    public RpgProfile resetClass() {
        return new RpgProfile(RpgClass.NONE, 1, 0, kills, 0, 0, questStage, questFirst, questSecond, 0, 0);
    }

    public RpgProfile choose(RpgClass selected) {
        if (rpgClass != RpgClass.NONE || selected == RpgClass.NONE || selected == null)
            throw new IllegalStateException("A classe só pode ser escolhida uma vez.");
        return new RpgProfile(selected, level, xp, kills, abilityReadyAt, 0, questStage, questFirst, questSecond, readySecond, readyThird, attributes,attributeNodes);
    }

    public RpgProfile gainXp(int amount) {
        if (amount < 0) throw new IllegalArgumentException("XP negativo");
        int nextLevel = level;
        long remaining = (long) xp + amount;
        while (nextLevel < MAX_LEVEL && remaining >= nextLevel * 100) {
            remaining -= nextLevel * 100;
            nextLevel++;
        }
        return new RpgProfile(rpgClass, nextLevel, nextLevel == MAX_LEVEL ? 0 : (int) remaining,
                kills, abilityReadyAt, skills, questStage, questFirst, questSecond, readySecond, readyThird, attributes,attributeNodes);
    }

    public RpgProfile defeatedEnemy() {
        return defeatedEnemy("");
    }
    public RpgProfile defeatedEnemy(String mob) {return defeatedEnemy(mob,25);}
    public RpgProfile defeatedEnemy(String mob,int baseXp) {
        if(baseXp<0)throw new IllegalArgumentException("XP negativo");
        if (rpgClass == RpgClass.NONE) return this;
        int nextKills = Math.addExact(kills, 1);
        var quest = QuestCatalog.quest(questStage);
        int first = questFirst, second = questSecond, stage = questStage;
        if (quest.targets().get(0).mob().equals(mob)) first = Math.min(first + 1, quest.targets().get(0).amount());
        if (quest.targets().size() == 2 && quest.targets().get(1).mob().equals(mob)) second = Math.min(second + 1, quest.targets().get(1).amount());
        boolean complete = first == quest.targets().get(0).amount()
            && (quest.targets().size() == 1 || second == quest.targets().get(1).amount());
        if (complete) { stage = Math.addExact(stage, 1); first = 0; second = 0; }
        RpgProfile reward = gainXp(baseXp + (nextKills % 10 == 0 ? 100 : 0) + (complete ? quest.reward() : 0));
        return new RpgProfile(rpgClass, reward.level, reward.xp, nextKills, abilityReadyAt, skills, stage, first, second, readySecond, readyThird, attributes,attributeNodes);
    }

    public RpgProfile useAbility(long tick) { return useAbility(tick, 0); }
    public RpgProfile useAbility(long tick, int slot) {
        if (!canUseAbility(slot) || tick < readyAt(slot)) throw new IllegalStateException("Habilidade indisponível.");
        long ready = Math.addExact(tick, abilityCooldown(slot));
        return new RpgProfile(rpgClass, level, xp, kills, slot == 0 ? ready : abilityReadyAt, skills, questStage, questFirst,
            questSecond, slot == 1 ? ready : readySecond, slot == 2 ? ready : readyThird, attributes,attributeNodes);
    }
}





