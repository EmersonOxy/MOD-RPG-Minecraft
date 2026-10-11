package dev.emersonoxy.rpg;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class RpgNetworkTest {
    @Test void profileAndFeedbackSurviveTransmission() {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            var state = new RpgNetwork.State(1, 4, 100, 20, 599, 125, 900, 4, false, 0, 0, 0, "Habilidade ativada! Caçada concluída.");
            RpgNetwork.State.CODEC.encode(buf, state);
            assertEquals(state, RpgNetwork.State.CODEC.decode(buf));
            assertEquals(0, buf.readableBytes());
        } finally { buf.release(); }
    }
    @Test void explorationAndDatapackPayloadsSurviveTransmission(){
        var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{var energy=new RpgNetwork.EvasionState(3,1.25f,120);RpgNetwork.EvasionState.CODEC.encode(buf,energy);assertEquals(energy,RpgNetwork.EvasionState.CODEC.decode(buf));
            var text=new RpgNetwork.CombatText(4,1,2,3,"Errou",0x75BFFF);RpgNetwork.CombatText.CODEC.encode(buf,text);assertEquals(text,RpgNetwork.CombatText.CODEC.decode(buf));
            var content=new RpgNetwork.ContentState("{\"classes\":{}}");RpgNetwork.ContentState.CODEC.encode(buf,content);assertEquals(content,RpgNetwork.ContentState.CODEC.decode(buf));assertEquals(0,buf.readableBytes());
        }finally{buf.release();}
    }
    @Test void everyMenuActionSurvivesTransmission() {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            for (int action : new int[] {0, 1, 2, 3, 4, 5, 6, 102, 103, 107, 110, 117, 120}) {
                buf.clear();
                var request = new RpgNetwork.Request(action, 7);
                RpgNetwork.Request.CODEC.encode(buf, request);
                assertEquals(request, RpgNetwork.Request.CODEC.decode(buf));
            }
        } finally { buf.release(); }
    }
}


