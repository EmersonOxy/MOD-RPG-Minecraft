package dev.emersonoxy.rpg.client;

import java.lang.reflect.Method;
import net.neoforged.fml.ModList;
import org.slf4j.LoggerFactory;

/** Optional client bridge: the flight controller owns view rotation while active. */
public final class BarrelRollCompat {
    private static boolean resolved;
    private static Method active;
    private BarrelRollCompat() {}
    public static boolean active() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded("do_a_barrel_roll")) {
                try {
                    active = Class.forName("nl.enjarai.doabarrelroll.DoABarrelRollClient")
                        .getMethod("isFallFlying");
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
