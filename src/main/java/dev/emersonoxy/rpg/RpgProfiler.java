package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.util.profiling.Profiler;

/** Native profiler sections plus optional local timing counters; no telemetry or uploads. */
public final class RpgProfiler {
    private static final Map<String,long[]> TIMES=new java.util.concurrent.ConcurrentHashMap<>();
    private RpgProfiler(){}
    public static void run(String section,Runnable action){
        var profiler=Profiler.get();profiler.push("oxyrpg_"+section);boolean measure=RpgConfig.profiling();long start=measure?System.nanoTime():0;
        try{action.run();}finally{profiler.pop();if(measure){long elapsed=System.nanoTime()-start;var values=TIMES.computeIfAbsent(section,k->new long[3]);synchronized(values){values[0]++;values[1]+=elapsed;values[2]=Math.max(values[2],elapsed);}}}
    }
    public static List<String> report(){var lines=new ArrayList<String>();for(var entry:TIMES.entrySet()){var n=entry.getValue();synchronized(n){lines.add(String.format(Locale.ROOT,"%s: média %.3f ms • pico %.3f ms • %d chamadas",entry.getKey(),n[1]/Math.max(1d,n[0])/1e6,n[2]/1e6,n[0]));}}Collections.sort(lines);if(lines.isEmpty())lines.add("Sem amostras. Ative development.profiling nas configurações.");return lines;}
    public static void clear(){TIMES.clear();}
}
