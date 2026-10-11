package dev.emersonoxy.rpg.client;
/** Wall-clock easing stays smooth when the integrated world is paused. */
final class UiTransitions {
    private UiTransitions(){}
    static float opacity(long start,int milliseconds){return Math.max(.03f,io.wispforest.owo.ui.core.Easing.CUBIC.apply(Math.clamp((System.nanoTime()-start)/(milliseconds*1000000f),0,1)));}
}
