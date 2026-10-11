package dev.emersonoxy.rpg;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Post-damage cosmetics: misses, invulnerability and fully blocked hits never bleed. */
public final class CombatParticles {
    private record Budget(long tick,int count){}
    private static final Map<LivingEntity,Long> BLOOD=new WeakHashMap<>();
    private static final Map<Player,Long> BLOCK_HITS=new WeakHashMap<>();
    private static final Map<ServerLevel,Budget> BUDGET=new WeakHashMap<>();
    private CombatParticles(){}
    private static boolean reserve(ServerLevel level){
        long now=level.getGameTime();var old=BUDGET.get(level);
        int count=old!=null&&old.tick==now?old.count:0;
        if(count>=64)return false;BUDGET.put(level,new Budget(now,count+1));return true;
    }
    public static void damage(LivingDamageEvent.Post event){
        var target=event.getEntity();var source=event.getSource();float damage=event.getNewDamage();
        if(!(target.level() instanceof ServerLevel level)||!(target instanceof Player||target instanceof Mob)||!Float.isFinite(damage)||damage<=0)return;
        if(source.getEntity()==null&&!source.is(DamageTypeTags.IS_EXPLOSION)&&!source.is(DamageTypeTags.IS_FALL))return;
        long now=level.getGameTime();Long previous=BLOOD.get(target);
        if(previous!=null&&now-previous<4||!reserve(level))return;BLOOD.put(target,now);
        Vec3 at=target.position().add(0,target.getBbHeight()*.55,0);
        var origin=source.getSourcePosition();Vec3 direction=origin==null?Vec3.ZERO:at.subtract(origin).normalize();
        PacketDistributor.sendToPlayersNear(level,null,at.x,at.y,at.z,48,
            new RpgNetwork.BloodFx(at.x,at.y,at.z,(float)direction.x,(float)direction.y,(float)direction.z,Math.min(40,damage)));
        if(damage>=6&&target.onGround()&&source.getDirectEntity() instanceof LivingEntity)
            SkillEffects.impact(level,target.position(),Math.min(2,.65+damage*.04),8);
    }
    public static void explosion(ExplosionEvent.Detonate event){
        if(event.getLevel() instanceof ServerLevel level&&reserve(level))
            SkillEffects.impact(level,event.getExplosion().center(),event.getExplosion().radius(),1);
    }
    public static void block(PlayerInteractEvent.LeftClickBlock event){
        if(event.isCanceled()||event.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(event.getEntity() instanceof ServerPlayer p))return;
        var stack=p.getMainHandItem();
        if(!(stack.is(ItemTags.SWORDS)||stack.is(ItemTags.AXES)||stack.is(Items.MACE))||p.getAttackStrengthScale(0)<.85f)return;
        var level=p.level();long now=level.getGameTime();Long last=BLOCK_HITS.get(p);
        if(last!=null&&now-last<12||!level.hasChunkAt(event.getPos())||level.getBlockState(event.getPos()).getCollisionShape(level,event.getPos()).isEmpty()||!reserve(level))return;
        BLOCK_HITS.put(p,now);var face=event.getFace();
        var at=Vec3.atCenterOf(event.getPos());if(face!=null)at=at.add(face.getStepX()*.51,face.getStepY()*.51,face.getStepZ()*.51);
        SkillEffects.impact(level,at,stack.is(Items.MACE)?1.8:.8,9);
    }
    public static void stop(ServerStoppedEvent event){BLOOD.clear();BLOCK_HITS.clear();BUDGET.clear();}
}
