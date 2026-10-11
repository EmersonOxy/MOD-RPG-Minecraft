package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgClass;
import net.minecraft.client.gui.GuiGraphics;

/** Original vector pictograms; consistent strokes at every GUI scale. */
public final class SkillGlyphs {
    private SkillGlyphs(){}
    private static void line(GuiGraphics g,int x,int y,int xx,int yy,int color){int dx=Math.abs(xx-x),dy=-Math.abs(yy-y),sx=x<xx?1:-1,sy=y<yy?1:-1,e=dx+dy;for(;;){g.fill(x,y,x+2,y+2,color);if(x==xx&&y==yy)break;int n=2*e;if(n>=dy){e+=dy;x+=sx;}if(n<=dx){e+=dx;y+=sy;}}}
    private static void ring(GuiGraphics g,int x,int y,int radius,int c){for(int i=0;i<16;i++){double a=i*Math.PI/8,b=(i+1)*Math.PI/8;line(g,x+(int)Math.round(Math.cos(a)*radius),y+(int)Math.round(Math.sin(a)*radius),x+(int)Math.round(Math.cos(b)*radius),y+(int)Math.round(Math.sin(b)*radius),c);}}
    private static void wing(GuiGraphics g,int c){line(g,3,5,11,11,c);line(g,11,11,21,5,c);for(int i=0;i<4;i++){line(g,3+i*2,5+i,5+i*2,13+i,c);line(g,21-i*2,5+i,19-i*2,13+i,c);}line(g,11,11,12,20,c);}
    private static void flame(GuiGraphics g,int c){line(g,12,2,7,10,c);line(g,7,10,4,9,c);line(g,4,9,5,17,c);line(g,5,17,12,21,c);line(g,12,21,19,17,c);line(g,19,17,17,8,c);line(g,17,8,13,13,c);line(g,13,13,12,2,c);}
    private static void drop(GuiGraphics g,int c){line(g,12,2,5,13,c);line(g,5,13,5,17,c);line(g,5,17,11,21,c);line(g,11,21,18,17,c);line(g,18,17,18,13,c);line(g,18,13,12,2,c);}
    private static void waves(GuiGraphics g,int c){for(int y=5;y<22;y+=6){line(g,2,y+2,7,y,c);line(g,7,y,13,y+3,c);line(g,13,y+3,20,y,c);}}
    private static void eye(GuiGraphics g,int c){line(g,2,12,10,5,c);line(g,10,5,14,5,c);line(g,14,5,22,12,c);line(g,22,12,14,18,c);line(g,14,18,10,18,c);line(g,10,18,2,12,c);ring(g,12,11,3,c);}
    private static void shield(GuiGraphics g,int c){line(g,4,4,20,4,c);line(g,20,4,18,15,c);line(g,18,15,12,21,c);line(g,12,21,6,15,c);line(g,6,15,4,4,c);line(g,12,8,12,15,c);}
    private static void spark(GuiGraphics g,int c){for(int i=0;i<8;i++){double a=i*Math.PI/4;line(g,12+(int)(Math.cos(a)*3),12+(int)(Math.sin(a)*3),12+(int)(Math.cos(a)*9),12+(int)(Math.sin(a)*9),c);}}
    private static void arrow(GuiGraphics g,int c){line(g,3,20,20,3,c);line(g,12,3,20,3,c);line(g,20,3,20,11,c);line(g,3,14,9,20,c);}
    public static void draw(GuiGraphics g,RpgClass cls,int slot,int x,int y,int size,int color){
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(size/24f,size/24f);
        switch(cls){
            case GUERREIRO->{if(slot==0){arrow(g,color);line(g,5,11,13,19,color);}else if(slot==1){drop(g,color);line(g,10,12,14,12,color);}else shield(g,color);}
            case MAGO->{if(slot==0)spark(g,color);else if(slot==1)flame(g,color);else waves(g,color);}
            case ARQUEIRO->{if(slot==0)arrow(g,color);else if(slot==1){eye(g,color);line(g,3,21,21,3,color);}else{line(g,3,6,13,12,color);line(g,13,12,3,19,color);line(g,12,6,22,12,color);line(g,22,12,12,19,color);}}
            case NECROMANTE->{if(slot==0){spark(g,color);ring(g,12,12,6,color);}else if(slot==1){ring(g,12,9,7,color);g.fill(7,9,10,12,color);g.fill(15,9,18,12,color);line(g,8,16,8,20,color);line(g,12,16,12,21,color);line(g,16,16,16,20,color);}else wing(g,color);}
            case XAMA->{if(slot==0){line(g,16,2,6,12,color);line(g,6,12,14,12,color);line(g,14,12,8,22,color);}else if(slot==1){drop(g,color);line(g,7,18,18,6,color);}else{ring(g,12,15,7,color);line(g,9,3,15,3,color);line(g,9,3,9,8,color);line(g,15,3,15,8,color);}}
            case AVIARIO->{if(slot==0)wing(g,color);else if(slot==1){line(g,5,20,19,4,color);line(g,8,15,4,9,color);line(g,11,12,6,6,color);line(g,14,8,17,14,color);}else{ring(g,15,16,5,color);line(g,2,3,10,11,color);line(g,8,2,14,9,color);line(g,2,10,8,16,color);}}
            case ILUSIONISTA->{if(slot==0){eye(g,color);line(g,5,19,19,19,color);}else if(slot==1){ring(g,12,15,5,color);line(g,12,9,12,2,color);line(g,7,6,12,2,color);line(g,17,6,12,2,color);}else{wing(g,color);ring(g,12,12,10,color);}}
            case HIDROMANTE->{if(slot==0){waves(g,color);line(g,5,2,19,2,color);}else if(slot==1){arrow(g,color);line(g,2,7,8,7,color);}else{drop(g,color);line(g,2,20,21,20,color);}}
            case PIROMANTE->{if(slot==0)spark(g,color);else if(slot==1){flame(g,color);ring(g,12,16,3,color);}else{wing(g,color);flame(g,color);}}
            default->spark(g,color);
        }
        g.pose().popMatrix();
    }
    public static void marker(GuiGraphics g,int x,int y,float yaw){g.pose().pushMatrix();g.pose().translate(x,y);g.pose().rotate((float)Math.toRadians(yaw+180));
        for(int yy=-9;yy<=6;yy++){int half=(yy+9)*6/15;g.fill(-half,yy,half+1,yy+1,0xFF111B27);}
        for(int yy=-7;yy<=4;yy++){int half=(yy+7)*4/11;g.fill(-half,yy,half+1,yy+1,0xFFF8EAC5);}
        g.fill(-1,1,2,5,0xFFB19A65);g.pose().popMatrix();}
    public static void disk(GuiGraphics g,int cx,int cy,int radius,int color){for(int y=-radius;y<=radius;y++){int width=(int)Math.sqrt(radius*radius-y*y);g.fill(cx-width,cy+y,cx+width+1,cy+y+1,color);}}
}
