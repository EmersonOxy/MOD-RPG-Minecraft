package dev.emersonoxy.rpg;

import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Cosmetic focus. It never changes existing class weapons or their combat values. */
public final class AnimatedSigilItem extends Item implements GeoItem,top.theillusivec4.curios.api.type.capability.ICurioItem {
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE=RawAnimation.begin().thenLoop("animation.sigil.idle");
    public AnimatedSigilItem(Properties properties){super(properties);}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){controllers.add(new AnimationController<AnimatedSigilItem>("idle",test->test.setAndContinue(IDLE)));}
    @Override public void createGeoRenderer(Consumer<GeoRenderProvider> consumer){consumer.accept(new GeoRenderProvider(){
        private software.bernie.geckolib.renderer.GeoItemRenderer<AnimatedSigilItem> renderer;
        @Override public software.bernie.geckolib.renderer.GeoItemRenderer<AnimatedSigilItem> getGeoItemRenderer(){if(renderer==null)renderer=new dev.emersonoxy.rpg.client.SigilRenderer();return renderer;}
    });}
}
