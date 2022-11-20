package farzo.plugins.world.entities.ai.goal;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;

public class FindWater extends Behavior<PathfinderMob> {
    private final int range;
    private final float speedModifier;
    private long nextOkStartTime;

    public FindWater(int var0, float var1) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
        this.range = var0;
        this.speedModifier = var1;
    }

    protected void stop(ServerLevel var0, PathfinderMob var1, long var2) {
        this.nextOkStartTime = var2 + 20L + 2L;
    }

    protected boolean checkExtraStartConditions(ServerLevel var0, PathfinderMob var1) {
        return !var1.level.getFluidState(var1.blockPosition()).is(FluidTags.WATER);
    }

    protected void start(ServerLevel var0, PathfinderMob var1, long var2) {
        if (var2 >= this.nextOkStartTime) {
            BlockPos var4 = null;
            BlockPos var5 = null;
            BlockPos var6 = var1.blockPosition();
            Iterable<BlockPos> var7 = BlockPos.withinManhattan(var6, this.range, this.range, this.range);
            Iterator iterator = var7.iterator();

            while(iterator.hasNext()) {
                BlockPos var9 = (BlockPos)iterator.next();
                if (var9.getX() != var6.getX() || var9.getZ() != var6.getZ()) {
                    BlockState var10 = var1.level.getBlockState(var9.above());
                    BlockState var11 = var1.level.getBlockState(var9);
                    if (var11.is(Blocks.WATER)) {
                        if (var10.isAir()) {
                            var4 = var9.immutable();
                            break;
                        }

                        if (var5 == null && !var9.closerToCenterThan(var1.position(), 1.5D)) {
                            var5 = var9.immutable();
                        }
                    }
                }
            }

            if (var4 == null) {
                var4 = var5;
            }

            if (var4 != null) {
                this.nextOkStartTime = var2 + 40L;
                BehaviorUtils.setWalkAndLookTargetMemories(var1, var4, this.speedModifier, 0);
            }

        }
    }
}

