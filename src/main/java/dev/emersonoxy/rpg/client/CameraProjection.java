package dev.emersonoxy.rpg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** Projects markers through the world projection, including damage and FOV effects. */
public final class CameraProjection {
    private static Matrix4f worldProjection;
    private CameraProjection(){}
    public static void capture(Matrix4f projection){worldProjection=new Matrix4f(projection);}
    public static Vec3 project(Vec3 point){
        var mc=Minecraft.getInstance();
        if(worldProjection==null)return mc.gameRenderer.projectPointToScreen(point);
        var camera=mc.gameRenderer.getMainCamera();
        var transform=new Matrix4f(worldProjection).rotate(camera.rotation().conjugate(new Quaternionf()));
        var projected=transform.transformProject(point.subtract(camera.getPosition()).toVector3f());
        return new Vec3(projected.x,projected.y,projected.z);
    }
}
