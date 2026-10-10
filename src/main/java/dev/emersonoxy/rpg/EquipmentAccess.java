package dev.emersonoxy.rpg;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;

/** Pure client preview plus authoritative slot guard; never creates, deletes or moves stacks. */
public final class EquipmentAccess {
    private static RpgClass clientClass=RpgClass.NONE;
    private static int[] clientXp=new int[3];
    private static int clientLevel=1;
    private EquipmentAccess(){}
    public static void clientClass(int ordinal){clientClass=ordinal>=0&&ordinal<RpgClass.values().length?RpgClass.values()[ordinal]:RpgClass.NONE;}
    public static void clientXp(int mining,int farming,int arsenal){clientXp=new int[]{mining,farming,arsenal};}
    public static void clientLevel(int level){clientLevel=Math.clamp(level,1,100);}
    public static int clientLevel(){return clientLevel;}
    public static RpgClass clientSelected(){return clientClass;}
    public static int clientProfessionLevel(int area){return ProfessionStore.level(clientXp[area]);}
    public static void resetClient(){clientClass=RpgClass.NONE;clientXp=new int[3];clientLevel=1;}
    public static boolean allowed(Player player,ItemStack stack){
        if(stack.isEmpty())return true;
        if(ClassEquipment.gear(stack))return ClassEquipment.allowed(player,stack);
        RpgClass selected=player instanceof ServerPlayer p?ClassRules.selected(p):clientClass;
        if(selected==RpgClass.ARQUEIRO&&stack.is(Items.SHIELD)||selected==RpgClass.GUERREIRO&&ClassRules.ranged(stack))return false;
        var requirement=Professions.requirement(stack);if(requirement.area()<0)return true;
        int level=player instanceof ServerPlayer p?Professions.level(p,requirement.area()):ProfessionStore.level(clientXp[requirement.area()]);
        return level>=requirement.level();
    }
}
