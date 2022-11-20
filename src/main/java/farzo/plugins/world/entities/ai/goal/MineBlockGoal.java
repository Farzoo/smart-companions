package farzo.plugins.world.entities.ai.goal;

import com.google.common.collect.Sets;
import farzo.plugins.world.entities.BlockTargeter;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;

import java.util.Set;

public class MineBlockGoal<T extends PathfinderMob & InventoryCarrier & BlockTargeter> extends Goal {

    private final T entity;
    private final float speedMultiplier;
    private Path path;
    private long currentTime;
    private static final long MIN_TIME = 10;
    private SoundEvent targetBlockSoundHit;
    private int ticksToBreakTargetBlock;
    private int ticksBreakSound;
    private final Set<Block> breakableBlocks;
    private BlockPos reservedBlockPos;
    private BlockPos inBetweenBlockPos;
    private BlockPos targetBlock;

    public MineBlockGoal(T entity, float speedMultiplier, Block... breakableBblocks) {
        this.entity = entity;
        this.speedMultiplier = speedMultiplier > 0 ? speedMultiplier : 1;
        this.breakableBlocks = Sets.newHashSet(breakableBblocks);
    }

    @Override
    public boolean canContinueToUse() {
        BlockHitResult result = this.rayTraceToBlockPos();
        BlockPos hitBlockPos = result.getBlockPos();
        int distMine = 0;
        if(result.getBlockPos().compareTo(this.targetBlock) == 0) {
            distMine = (int) Math.sqrt(entity.position().add(0, entity.getEyeHeight(), 0).distanceTo(Vec3.atCenterOf(hitBlockPos)));
        } else {
            Bukkit.getLogger().info(result.getBlockPos() + " " + this.entity.getLevel().getBlockState(hitBlockPos).getBlock());
            if(this.breakableBlocks.contains(this.entity.getLevel().getBlockState(hitBlockPos).getBlock())) {
                this.inBetweenBlockPos = result.getBlockPos();
                this.reservedBlockPos = this.targetBlock;
                return false;
            }
        }

        boolean canMine = distMine >= 0 && distMine < 4;
        return this.currentTime <= MIN_TIME || (this.ticksToBreakTargetBlock <= 0 && (!this.entity.getNavigation().isDone() || canMine) && !canMine);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.path, this.speedMultiplier);
        this.currentTime = 0;
        this.ticksToBreakTargetBlock = 15;
        this.targetBlockSoundHit = this.getTargetBlockSound();
    }

    @Override
    public void tick() {
        int distMine = (int) Math.sqrt(this.rayTraceToBlockPos().distanceTo(this.entity));
        if(distMine < 6 && distMine >= 0) {
            this.entity.getLookControl().setLookAt(Vec3.atCenterOf(this.targetBlock));
            this.entity.setAggressive(true);
            if(distMine < 4) {
                if(this.ticksBreakSound < 0) {
                    this.entity.swing(InteractionHand.MAIN_HAND, true);
                    this.entity.playSound(this.targetBlockSoundHit, 0.3f, 1);
                    ticksBreakSound = 15;
                }
                this.ticksToBreakTargetBlock--;
            }
        }
        this.ticksBreakSound--;
        this.currentTime++;
    }

    private SoundEvent getTargetBlockSound() {
        return this.entity.getLevel().getBlockState(this.targetBlock).getSoundType().getHitSound();
    }

    @Override
    public void stop() {
        Level entityLevel = entity.getLevel();
        if(this.targetBlock != null) {
            BlockHitResult result = this.rayTraceToBlockPos();
            if(this.targetBlock.compareTo(result.getBlockPos()) == 0) {
                int distMine = (int) Math.sqrt(result.distanceTo(this.entity));
                if(distMine >= 0 && distMine < 4) {
                    entityLevel.destroyBlock(this.targetBlock, true);
                }
            }
        }
        this.entity.setAggressive(false);
        this.targetBlock = null;
    }

    private BlockHitResult rayTraceToBlockPos() {
        BlockPos blockPos = this.targetBlock;
        Vec3 vec3BlockPos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3 headPos = new Vec3(entity.getX(), entity.getY(), entity.getZ()).add(0, entity.getEyeHeight(), 0);
        return entity.getLevel().clip(new ClipContext(headPos, vec3BlockPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
    }

    /*@Override
    public boolean canUse() {
        if(this.entity.valid && this.entity.isChunkLoaded() && this.entity.isOnGround()) return false;
        if(this.entity.getTargetBlock() == null) return false;
        this.path = this.entity.getNavigation().createPath(this.entity.getTargetBlock(), 1);
        if(this.path == null) {
            this.entity.setTargetBlock(null);
            return false;
        }
        return true;
    }*/

    @Override
    public boolean canUse() {
        if(this.entity.valid && this.entity.isChunkLoaded()) {
            if(this.inBetweenBlockPos != null) {
                this.targetBlock = this.inBetweenBlockPos;
                this.inBetweenBlockPos = null;
            } else if(this.reservedBlockPos != null) {
                this.targetBlock = this.reservedBlockPos;
                this.reservedBlockPos = null;
            } else if(this.entity.getTargetBlock() != null) {
                this.targetBlock = this.entity.getTargetBlock();
                this.entity.setTargetBlock(null);
            } else {
                return false;
            }
            this.path = this.entity.getNavigation().createPath(this.targetBlock, 0);
            return this.path != null;
        }
        return false;
    }
}
