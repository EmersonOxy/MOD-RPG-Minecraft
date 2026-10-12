package dev.emersonoxy.rpg.client;

import java.lang.reflect.Method;
import net.neoforged.fml.ModList;
import org.slf4j.LoggerFactory;

/** Optional client bridge: the flight controller owns view rotation while active. */
public final class BarrelRollCompat {
    private static boolean resolved;
    private static Method active,clear,setRoll,setRolling;
    private static net.minecraft.client.player.LocalPlayer tracked;
    private static boolean gliding;
    private static net.minecraft.client.CameraType perspective;
    private BarrelRollCompat() {}
    public static void resetFlight(){
        active();var p=net.minecraft.client.Minecraft.getInstance().player;if(p==null||clear==null)return;
        try{clear.invoke(null);setRoll.invoke(p,0f);setRolling.invoke(p,false);}
        catch(ReflectiveOperationException e){clear=null;LoggerFactory.getLogger(BarrelRollCompat.class).warn("Não foi possível reiniciar a rotação de voo",e);}
    }
    public static void tick(){
        var mc=net.minecraft.client.Minecraft.getInstance();var p=mc.player;boolean next=p!=null&&p.isFallFlying()&&!p.getAbilities().flying;
        var view=mc.options.getCameraType();
        if(tracked!=p||gliding!=next||perspective!=view){tracked=p;gliding=next;perspective=view;resetFlight();ShoulderCamera.reset();TargetLock.perspectiveChanged();}
    }
    public static boolean active() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded("do_a_barrel_roll")) {
                try {
                    active = Class.forName("nl.enjarai.doabarrelroll.DoABarrelRollClient")
                        .getMethod("isFallFlying");
                    clear=Class.forName("nl.enjarai.doabarrelroll.DoABarrelRollClient").getMethod("clearValues");
                    var roll=Class.forName("nl.enjarai.doabarrelroll.api.RollEntity");
                    setRoll=roll.getMethod("doABarrelRoll$setRoll",float.class);
                    setRolling=roll.getMethod("doABarrelRoll$setRolling",boolean.class);
                } catch (ReflectiveOperationException e) {
                    LoggerFactory.getLogger(BarrelRollCompat.class).warn("Controlador de voo incompatível; câmera Oxy preservada", e);
                }
            }
        }
        if (active == null) return false;
        try { return Boolean.TRUE.equals(active.invoke(null)); }
        catch (ReflectiveOperationException e) {
            active = null;
            LoggerFactory.getLogger(BarrelRollCompat.class).warn("Integração de voo desativada", e);
            return false;
        }
    }
}
