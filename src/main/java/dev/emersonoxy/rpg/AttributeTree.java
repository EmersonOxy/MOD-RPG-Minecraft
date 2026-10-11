package dev.emersonoxy.rpg;

import java.util.*;

/** Stable UUID-seeded layout. Old allocations remain bonuses outside this new graph. */
public final class AttributeTree {
    public static final int COUNT=160, SPOKES=8;
    private AttributeTree(){}
    public static int type(UUID owner,int id){
        if(id<0||id>=COUNT)throw new IllegalArgumentException("Nó de atributo inválido.");
        int[] values=new int[COUNT];for(int i=0;i<COUNT;i++)values[i]=i%5;
        var random=new Random(owner.getMostSignificantBits()^owner.getLeastSignificantBits()^0x415649415259L);
        for(int i=COUNT-1;i>0;i--){int j=random.nextInt(i+1),v=values[i];values[i]=values[j];values[j]=v;}
        return values[id];
    }
    public static BitSet decode(String value){
        var bits=new BitSet(COUNT);if(value.isEmpty())return bits;
        for(String token:value.split(",")){int id=Integer.parseInt(token);if(id<0||id>=COUNT||bits.get(id))throw new IllegalArgumentException("Árvore de atributos inválida.");bits.set(id);}
        return bits;
    }
    public static String encode(BitSet bits){return bits.stream().mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","));}
    public static int[] parents(int id){
        if(id<SPOKES)return new int[0];
        int ring=id/SPOKES,spoke=id%SPOKES;
        return ring%3==0?new int[]{id-SPOKES,(ring-1)*SPOKES+(spoke+1)%SPOKES}:new int[]{id-SPOKES};
    }
    public static boolean available(BitSet owned,int id){if(id<0||id>=COUNT||owned.get(id))return false;if(id<SPOKES)return true;for(int p:parents(id))if(owned.get(p))return true;return false;}
    public static double x(int id){return Math.cos(angle(id))*(58+id/SPOKES*48);}
    public static double y(int id){return Math.sin(angle(id))*(58+id/SPOKES*48);}
    private static double angle(int id){return (id%SPOKES)*Math.PI/4+(id/SPOKES%2)*.09;}
}
