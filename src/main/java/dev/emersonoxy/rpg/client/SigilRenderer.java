package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.AnimatedSigilItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class SigilRenderer extends GeoItemRenderer<AnimatedSigilItem> {
    public SigilRenderer(){super(new GeoModel<>(){
        @Override public ResourceLocation getModelResource(GeoRenderState state){return ResourceLocation.fromNamespaceAndPath("oxyrpg","sigil");}
        @Override public ResourceLocation getAnimationResource(AnimatedSigilItem item){return ResourceLocation.fromNamespaceAndPath("oxyrpg","sigil");}
        @Override public ResourceLocation getTextureResource(GeoRenderState state){return ResourceLocation.fromNamespaceAndPath("oxyrpg","textures/item/sigil.png");}
    });}
}
