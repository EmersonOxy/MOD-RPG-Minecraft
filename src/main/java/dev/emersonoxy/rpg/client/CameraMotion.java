package dev.emersonoxy.rpg.client;

/** Numerical rules independent of Minecraft, shared by runtime and regression tests. */
public final class CameraMotion {
    private CameraMotion(){}
    public static double smooth(double from,double to,double rate,double dt){
        if(!Double.isFinite(from)||!Double.isFinite(to))throw new IllegalArgumentException("Non-finite camera coordinate");
        return from+(to-from)*(-Math.expm1(-Math.max(0,rate)*Math.clamp(dt,0,.1)));
    }
    public static double angle(double from,double to,double rate,double dt){return smooth(from,from+((to-from)%360+540)%360-180,rate,dt);}
    public static double clamp(double value,double min,double max,double fallback){return Double.isFinite(value)?Math.clamp(value,min,max):fallback;}
    public static final class Collision {
        private double fraction=1,clearTime;
        public double update(double allowed,double dt){
            allowed=Math.clamp(allowed,0,1);
            if(allowed<fraction){fraction=allowed;clearTime=0;}
            else if(allowed>fraction+.025||clearTime>.12&&allowed>fraction){clearTime+=Math.max(0,dt);if(clearTime>.12)fraction=smooth(fraction,allowed,5,dt);}
            else clearTime=0;
            return Math.min(allowed,fraction);
        }
        public void reset(){fraction=1;clearTime=0;}
    }
}
