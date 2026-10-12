package dev.emersonoxy.rpg.mixin;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import com.mojang.datafixers.util.Pair;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(CreateWorldScreen.class)
public interface TerralithWorldInvoker {
    @Invoker("getDataPackSelectionSettings") Pair<Path,PackRepository> oxyPacks(WorldDataConfiguration data);
    @Invoker("tryApplyNewDataPacks") void oxyApply(PackRepository packs,boolean confirm,Consumer<WorldDataConfiguration> callback);
}
