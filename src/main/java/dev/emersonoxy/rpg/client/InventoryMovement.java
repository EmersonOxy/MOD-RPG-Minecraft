package dev.emersonoxy.rpg.client;

import java.nio.file.*;
import java.util.Properties;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Input;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.LoggerFactory;

/** Poll the player's mapped movement bindings, never fixed WASD codes. */
public final class InventoryMovement {
    public static boolean enabled=true;private static boolean loaded;
    private static net.minecraft.client.player.LocalPlayer tracked;private static boolean sprintCarry;
    private InventoryMovement(){}
    public static void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event){var p=Minecraft.getInstance().player;if(p!=tracked){tracked=p;sprintCarry=false;}if(p!=null&&event.getNewScreen() instanceof AbstractContainerScreen)sprintCarry=p.isSprinting();else sprintCarry=false;}
    private static Path file(){return FMLPaths.CONFIGDIR.get().resolve("oxyrpg-inventory.properties");}
    public static void load(){if(loaded)return;loaded=true;if(!Files.exists(file()))return;try(var in=Files.newBufferedReader(file())){var p=new Properties();p.load(in);enabled=Boolean.parseBoolean(p.getProperty("movement","true"));}catch(java.io.IOException e){LoggerFactory.getLogger(InventoryMovement.class).warn("Falha ao ler movimento do inventário",e);}}
    public static void save(){try{var p=new Properties();p.setProperty("movement",""+enabled);try(var out=Files.newBufferedWriter(file())){p.store(out,"Oxy RPG inventory movement");}}catch(java.io.IOException e){LoggerFactory.getLogger(InventoryMovement.class).warn("Falha ao salvar movimento do inventário",e);}}
    private static boolean textFocused(GuiEventListener listener){if(listener instanceof EditBox edit&&edit.isFocused())return true;if(listener instanceof ContainerEventHandler container)for(var child:container.children())if(textFocused(child))return true;return false;}
    // In-game modifier semantics allow ordinary forward movement while Ctrl/Shift are held.
    private static boolean pressed(KeyMapping binding){var mc=Minecraft.getInstance();var key=binding.getKey();return key.getType()==InputConstants.Type.KEYSYM&&key.getValue()!=InputConstants.UNKNOWN.getValue()&&InputConstants.isKeyDown(mc.getWindow(),key.getValue())&&binding.getKeyModifier().isActive(net.neoforged.neoforge.client.settings.KeyConflictContext.IN_GAME);}
    public static Input input(){load();var mc=Minecraft.getInstance();if(!enabled||mc.player==null||!mc.player.isAlive()||!mc.isWindowActive()||mc.isPaused()||!(mc.screen instanceof AbstractContainerScreen)||textFocused(mc.screen))return null;var o=mc.options;boolean forward=pressed(o.keyUp);if(!forward||pressed(o.keyShift))sprintCarry=false;return new Input(pressed(o.keyUp),pressed(o.keyDown),pressed(o.keyLeft),pressed(o.keyRight),pressed(o.keyJump),pressed(o.keyShift),pressed(o.keySprint)||sprintCarry);}
}
