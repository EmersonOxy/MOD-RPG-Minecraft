package dev.emersonoxy.rpg.client;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.neoforged.fml.loading.FMLPaths;
import dev.emersonoxy.rpg.mixin.TerralithWorldInvoker;
/** External official datapack, enabled per new world through Minecraft's validation flow. */
public final class TerralithChoice {
    private TerralithChoice(){}
    private static final String NAME="Terralith_1.21.5_v2.5.13.zip",ID="file/"+NAME;
    public static String label(CreateWorldScreen screen){return "Terralith: "+(screen.getUiState().getSettings().dataConfiguration().dataPacks().getEnabled().contains(ID)?"ativado":"desativado");}
    public static void toggle(CreateWorldScreen screen){
        var api=(TerralithWorldInvoker)screen;var data=screen.getUiState().getSettings().dataConfiguration();var pair=api.oxyPacks(data);if(pair==null)return;
        var source=FMLPaths.GAMEDIR.get().resolve("oxyrpg-packs").resolve(NAME);
        if(!Files.isRegularFile(source)){org.slf4j.LoggerFactory.getLogger(TerralithChoice.class).warn("Pacote Terralith ausente: {}",source);Minecraft.getInstance().setScreen(new PackMissingScreen(screen));return;}
        try{Files.copy(source,pair.getFirst().resolve(NAME),StandardCopyOption.REPLACE_EXISTING);}catch(java.io.IOException e){org.slf4j.LoggerFactory.getLogger(TerralithChoice.class).error("Não foi possível preparar Terralith",e);return;}
        var repo=pair.getSecond();repo.reload();var selected=new ArrayList<>(data.dataPacks().getEnabled());if(selected.contains(ID))selected.remove(ID);else selected.add(ID);repo.setSelected(selected);
        api.oxyApply(repo,true,newData->{Minecraft.getInstance().setScreen(screen);});
    }
    private static class PackMissingScreen extends net.minecraft.client.gui.screens.Screen {
        final CreateWorldScreen parent;PackMissingScreen(CreateWorldScreen parent){super(net.minecraft.network.chat.Component.literal("Pacote Terralith não encontrado"));this.parent=parent;}
        @Override protected void init(){addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.literal("Voltar"),b->minecraft.setScreen(parent)).bounds(width/2-75,height/2+24,150,20).build());}
        @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float d){g.drawCenteredString(font,title,width/2,height/2-16,0xFFFFFFFF);g.drawCenteredString(font,"Instale o pacote oficial em oxyrpg-packs.",width/2,height/2,0xFFBBBBBB);super.render(g,x,y,d);}
    }
}
