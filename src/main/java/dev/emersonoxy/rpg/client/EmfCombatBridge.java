package dev.emersonoxy.rpg.client;

import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.world.entity.Avatar;
import org.slf4j.LoggerFactory;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.utils.EMFEntity;

/** Gameplay animations own the avatar while active; EMF resumes for ordinary movement. */
public final class EmfCombatBridge {
    private EmfCombatBridge() {}
    public static void register() {
        try {
            EMFAnimationApi.registerPauseCondition(EmfCombatBridge::active);
            EMFAnimationApi.registerVanillaModelCondition(EmfCombatBridge::active);
        } catch (Exception e) {
            LoggerFactory.getLogger(EmfCombatBridge.class).warn("Não foi possível registrar a compatibilidade EMF/PAL", e);
        }
    }
    private static boolean active(EMFEntity entity) {
        if (!(entity instanceof Avatar avatar)) return false;
        return PlayerAnimationAccess.getPlayerAnimManager(avatar).getLayers().stream()
            .anyMatch(layer -> layer.left() >= 1000 && layer.right().isActive());
    }
}
