package dev.emersonoxy.rpg;

import dev.emersonoxy.rpg.client.CameraMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class CameraMotionTest {
    @Test void smoothingIsIndependentOfFrameRate(){
        double slow=0,fast=0;
        for(int i=0;i<30;i++)slow=CameraMotion.smooth(slow,2.8,10,1.0/30);
        for(int i=0;i<144;i++)fast=CameraMotion.smooth(fast,2.8,10,1.0/144);
        assertEquals(slow,fast,1e-10);assertTrue(slow>2.79&&slow<2.8);
    }
    @Test void entryExitAndAimAreContinuousAndDoNotOvershoot(){
        double value=0;
        for(int i=0;i<120;i++){double next=CameraMotion.smooth(value,2.8,10,1.0/60);assertTrue(next>=value&&next<=2.8);value=next;}
        double aimed=CameraMotion.smooth(value,2.184,10,1.0/60);assertTrue(aimed<value&&aimed>2.184);
        for(int i=0;i<120;i++){double next=CameraMotion.smooth(value,0,10,1.0/60);assertTrue(next<=value&&next>=0);value=next;}
        assertTrue(value<.001);
    }
    @Test void pauseDoesNotAdvanceTheCamera(){assertEquals(2,CameraMotion.smooth(2,5,10,0));}
    @Test void rotationUsesShortestPathAcrossTheYawSeam(){assertTrue(CameraMotion.angle(179,-179,10,.05)>179);assertTrue(CameraMotion.angle(-179,179,10,.05)<-179);}
    @Test void collisionRetractsImmediatelyButDoesNotBounceOnCorners(){
        var collision=new CameraMotion.Collision();assertEquals(.2,collision.update(.2,.016));
        for(int i=0;i<30;i++){assertEquals(.2,collision.update(.21,.016),1e-10);assertEquals(.2,collision.update(.2,.016),1e-10);}
        for(int i=0;i<4;i++)assertEquals(.2,collision.update(1,.016),1e-10);
        for(int i=0;i<180;i++)assertTrue(collision.update(1,.016)<=1);
        assertTrue(collision.update(1,.016)>.99);assertEquals(.1,collision.update(.1,.016));
    }
    @Test void configurationRejectsNonFiniteAndOutOfRangeValues(){
        assertEquals(2.8,CameraMotion.clamp(Double.NaN,1.2,6,2.8));assertEquals(2.8,CameraMotion.clamp(Double.POSITIVE_INFINITY,1.2,6,2.8));
        assertEquals(6,CameraMotion.clamp(20,1.2,6,2.8));assertThrows(IllegalArgumentException.class,()->CameraMotion.smooth(Double.NaN,0,10,.01));
    }
    @Test void professionThresholdsMatchHudAndKeepAllEarnedExperience(){
        int sum=0;for(int level=1;level<20;level++){
            int cost=ProfessionStore.nextXp(level);assertEquals(level,ProfessionStore.level(sum));assertEquals(0,ProfessionStore.remainder(sum));
            assertEquals(level,ProfessionStore.level(sum+cost-1));sum+=cost;assertEquals(level+1,ProfessionStore.level(sum));
        }
        assertEquals(17500,sum);assertEquals(20,ProfessionStore.level(19000));assertEquals(0,ProfessionStore.remainder(19000));
        assertEquals(4,ProfessionStore.level(300));assertEquals(50,ProfessionStore.nextXp(1));assertEquals(450,ProfessionStore.nextXp(6));assertEquals(1000,ProfessionStore.nextXp(10));
    }
}
