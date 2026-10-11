package dev.emersonoxy.rpg;

import dev.emersonoxy.rpg.RpgClass;

/** Class silhouettes affect coordinates only; node IDs and unlock dependencies stay intact. */
public final class SkillTreeLayouts {
    private SkillTreeLayouts(){}
    private static final java.util.Map<RpgClass,double[][]> CACHE=new java.util.EnumMap<>(RpgClass.class);
    public static double[] nodePosition(RpgClass type,int id){return CACHE.computeIfAbsent(type,SkillTreeLayouts::layout)[id].clone();}
    private static double[][] layout(RpgClass type){double[][] points=new double[SkillTree.COUNT][];for(int id=0;id<points.length;id++)points[id]=raw(type,id);separate(points);return points;}
    private static void separate(double[][] points){for(int pass=0;pass<80;pass++)for(int a=0;a<points.length;a++)for(int b=a+1;b<points.length;b++){double dx=points[b][0]-points[a][0],dy=points[b][1]-points[a][1],distance=Math.hypot(dx,dy);if(distance>=60)continue;if(distance<.001){dx=1;dy=0;distance=1;}double shift=(60-distance)/2+.05;if(a>0){points[a][0]-=dx/distance*shift;points[a][1]-=dy/distance*shift;}points[b][0]+=dx/distance*shift*(a==0?2:1);points[b][1]+=dy/distance*shift*(a==0?2:1);}}
    private static double[] raw(RpgClass type,int id){
        int branch=id<21?id/7:(id-21)/10,step=id<21?id%7:(id-21)%10;double radius,offset=0;
        if(id==0)radius=0;
        else if(id<21){radius=new int[]{70,130,190,370,445,445,515}[step];if(step==4)offset=-.22;if(step==5)offset=.22;}
        else if(step<2){radius=250+step*60;offset=step==0?-.08:.08;}
        else{radius=590+((step-2)/4)*85;offset=((step-2)%4-1.5)*.23;}
        return position(type,branch,radius,offset);
    }
    public static double[] position(RpgClass type,int branch,double radius,double offset){
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
            case ILUSIONISTA -> polar(radius,angle+offset+Math.sin(radius/90)*.3);
            case HIDROMANTE -> polar(radius,angle+offset+Math.sin(radius/140+branch)*.16);
            case PIROMANTE -> polar(radius,angle+offset+Math.sin(radius/80+branch)*.18);
            default -> polar(radius,angle+offset);
        };
    }
    private static double[] polar(double radius,double angle){return new double[]{Math.cos(angle)*radius,Math.sin(angle)*radius};}
}
