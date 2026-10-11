package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgClass;

/** Class silhouettes affect coordinates only; node IDs and unlock dependencies stay intact. */
final class SkillTreeLayouts {
    private SkillTreeLayouts(){}
    static double[] position(RpgClass type,int branch,double radius,double offset){
        if(radius==0)return new double[]{0,0};
        double angle=-Math.PI/2+branch*Math.PI*2/3;
        return switch(type){
            // A central blade flanked by two outward guards.
            case GUERREIRO -> new double[]{branch==0?Math.sin(offset)*radius:(branch==1?-1:1)*(100+radius*.55)+Math.sin(offset)*radius,
                -radius+ (branch==0?0:70)+Math.abs(offset)*radius*.35};
            // Three curved arcs form an arcane spiral.
            case MAGO -> polar(radius,angle+offset+radius*.00125);
            // A bow fans toward the right, with arrows at its tips.
            case ARQUEIRO -> polar(radius, (branch-1)*.85+offset);
            // Three skeletal chains alternate between inward and outward bends.
            case NECROMANTE -> polar(radius,angle+offset+Math.sin(radius/115)*.15);
            // Roots grow into a spreading crown; each root bends independently.
            case XAMA -> new double[]{(branch-1)*radius*.85+Math.sin(radius/150+branch)*55+Math.sin(offset)*radius,
                -radius*.8+Math.abs(branch-1)*radius*.22+Math.abs(offset)*radius*.4};
            // A descending spine and two broad, upward sweeping wings.
            case AVIARIO -> branch==0?new double[]{Math.sin(offset)*radius,radius*.8}:
                new double[]{(branch==1?-1:1)*(radius*.9+Math.sin(offset)*radius),-radius*.3-radius*radius/2600+Math.cos(offset)*35};
            default -> polar(radius,angle+offset);
        };
    }
    private static double[] polar(double radius,double angle){return new double[]{Math.cos(angle)*radius,Math.sin(angle)*radius};}
}
