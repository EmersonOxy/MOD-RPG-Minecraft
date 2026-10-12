package dev.emersonoxy.rpg.client;
import com.zigythebird.playeranim.api.*;
import com.zigythebird.playeranim.animation.*;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Avatar;
public final class DodgeAnimation {
    private static final ResourceLocation LAYER=ResourceLocation.fromNamespaceAndPath("oxyrpg","dodge"),ANIMATION=ResourceLocation.fromNamespaceAndPath("oxy_roll","roll");
    private DodgeAnimation(){}
    public static void register(){PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER,2500,avatar->new PlayerAnimationController(avatar,(controller,state,setter)->CombatOptions.animations?PlayState.CONTINUE:PlayState.STOP));}
    public static void play(Avatar avatar){if(!CombatOptions.animations||!PlayerAnimResources.hasAnimation(ANIMATION))return;var layer=PlayerAnimationAccess.getPlayerAnimationLayer(avatar,LAYER);if(layer instanceof PlayerAnimationController controller)controller.triggerAnimation(ANIMATION);}
}
