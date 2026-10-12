package dev.emersonoxy.rpg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Shared selection range; individual abilities retain their own damage/reach limits. */
public final class TargetingRange {
    private TargetingRange() {}
    public static double of(Player player){
        var type=player instanceof ServerPlayer server?RpgAbilities.profile(server).rpgClass():EquipmentAccess.clientSelected();
        if(type==RpgClass.ARQUEIRO_NOVO)return 64;
        if(type==RpgClass.AVIARIO&&!player.onGround())return 64;
        return 24;
    }
}
