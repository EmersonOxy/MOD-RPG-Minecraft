package dev.emersonoxy.rpg.client;
import com.zigythebird.playeranim.api.*;
import com.zigythebird.playeranim.animation.*;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Avatar;
public final class DodgeAnimation {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(DodgeAnimation.class);
    private static boolean missingReported,startedReported;
    private static final ResourceLocation LAYER=ResourceLocation.fromNamespaceAndPath("oxyrpg","dodge"),ANIMATION=ResourceLocation.fromNamespaceAndPath("oxy_roll","roll");
    private DodgeAnimation(){}
    public static void register(){PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER,2500,avatar->new PlayerAnimationController(avatar,(controller,state,setter)->PlayState.STOP));}
    public static void play(Avatar avatar){
        if(!CombatOptions.animations)return;
        if(!PlayerAnimResources.hasAnimation(ANIMATION)){if(!missingReported){missingReported=true;LOG.warn("Esquiva sem animação: recurso {} ausente; verifique OxyRollVisuals",ANIMATION);}return;}
        var layer=PlayerAnimationAccess.getPlayerAnimationLayer(avatar,LAYER);
        if(layer instanceof PlayerAnimationController controller){controller.triggerAnimation(ANIMATION);if(!startedReported){startedReported=true;LOG.info("Animação de esquiva {} iniciada pelo PAL",ANIMATION);}}
        else if(!missingReported){missingReported=true;LOG.warn("Esquiva sem controlador PAL para {}",avatar.getId());}
    }
}
