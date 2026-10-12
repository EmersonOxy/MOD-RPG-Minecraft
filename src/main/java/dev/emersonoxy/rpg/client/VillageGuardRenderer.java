package dev.emersonoxy.rpg.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.npc.*;
public final class VillageGuardRenderer extends VillagerRenderer {
    public VillageGuardRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public void extractRenderState(Villager entity,VillagerRenderState state,float partial){super.extractRenderState(entity,state,partial);state.villagerData=state.villagerData.withProfession(entity.registryAccess(),VillagerProfession.ARMORER);}
}
