package farzo.plugins.world.entities.ai.goal;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Sets;
import farzo.plugins.world.entities.BlockTargeter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.stream.StreamSupport;

public class NearestMineableBlock<T extends PathfinderMob & BlockTargeter> extends Goal {

    private final static int MAX_CHUNK_DISTANCE = 4;
    private final T entity;
    private final Set<Block> wantedBlocks;
    private final int chunkDistance;
    private final BiPredicate<T, BlockPos> checkDistance;
    private TreeMap<BlockPos, Block> cachedBlockPos;
    private BlockPos lastScanEntityPos;
    private Level cachedEntityLevel;
    private int ticksUntilNewLongScan;

    public NearestMineableBlock(T entity, int chunkDistance, BiPredicate<T, BlockPos> checkDistance, Block... wantedBlocks) {
        this.entity = entity;
        this.chunkDistance = Math.min(chunkDistance, MAX_CHUNK_DISTANCE);
        this.checkDistance = checkDistance;
        this.wantedBlocks = Sets.newHashSet(wantedBlocks);
        this.cachedBlockPos = new TreeMap<>(Comparator.comparingDouble(b -> b.distManhattan(this.entity.getOnPos())));
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }


    private boolean shouldPerformLongRangeScan() {
        return this.entity.position().distanceTo(Vec3.atCenterOf(this.lastScanEntityPos)) > (this.chunkDistance << 4) || this.ticksUntilNewLongScan <= 0;
    }

    private void scanWantedBlocks(long width) {
        long startTime = System.nanoTime();
        this.lastScanEntityPos = new BlockPos(this.entity.getX(), this.entity.getY(), this.entity.getZ());
        Level entityLevel = this.entity.getLevel();
        AtomicReference<Long> somme = new AtomicReference<>(0L);

        StreamSupport.stream(NearestMineableBlock.nearestBlocks(this.lastScanEntityPos, width).spliterator(), false)
                .peek(blockPos -> somme.set(somme.get() + 1))
                .filter(blockPos -> this.wantedBlocks.contains(entityLevel.getBlockState(blockPos).getBlock()))
                /*.filter(blockPos ->
                        isAir(blockPos.above(), entityLevel) ||
                        isAir(blockPos.below(), entityLevel) ||
                        isAir(blockPos.north(), entityLevel) ||
                        isAir(blockPos.south(), entityLevel) ||
                        isAir(blockPos.west(), entityLevel) ||
                        isAir(blockPos.east(), entityLevel)
                )*/
                .map(BlockPos::new)
                .forEach(b -> this.cachedBlockPos.put(b, entityLevel.getBlockState(b).getBlock()));

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Bukkit.getLogger().info("Scan duration : " + duration/Math.pow(10, 6) + " secondes.  Total blocks : " + somme.get());
    }

    private void filterCachedBlocks() {
        Level entityLevel = this.entity.getLevel();
        for (Map.Entry<BlockPos, Block> entry : this.cachedBlockPos.entrySet()) {
            if(!(entityLevel.getBlockState(entry.getKey()).is(entry.getValue()))) this.cachedBlockPos.remove(entry.getKey());
        }
    }

    private static boolean isAir(BlockPos block, Level level) {
        BlockState s = level.getBlockState(block);
        return s.is(Blocks.AIR) || s.is(Blocks.AIR);
    }

    @Override
    public boolean canContinueToUse() {
        if(this.entity.getLevel() != this.cachedEntityLevel) return false;
        return !this.cachedBlockPos.isEmpty();
    }

    @Override
    public void start() {
        this.cachedEntityLevel = this.entity.getLevel();
        this.entity.setTargetBlock(this.cachedBlockPos.pollFirstEntry().getKey());
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void tick() {
        this.ticksUntilNewLongScan--;
        if(this.shouldPerformLongRangeScan()) {
            this.cachedBlockPos.clear();
            this.ticksUntilNewLongScan = 1000;
            this.scanWantedBlocks((long) this.chunkDistance << 4);
        } else {
            this.scanWantedBlocks(4L);
        }
        if(this.entity.getTargetBlock() == null) {
            BlockPos bp;
            do {
                bp = this.cachedBlockPos.pollFirstEntry().getKey();
            } while (!this.cachedBlockPos.isEmpty() && !this.isBlockValid(bp));
            TreeMap<BlockPos, Block> temp = new TreeMap<>(this.cachedBlockPos.comparator());
            temp.putAll(this.cachedBlockPos);
            this.cachedBlockPos = temp;
            if(bp != null) this.entity.setTargetBlock(bp);
        }
    }

    private boolean isBlockValid(BlockPos blockPos) {
        return blockPos != null && this.wantedBlocks.contains(this.entity.getLevel().getBlockState(blockPos).getBlock());
    }

    public static Iterable<BlockPos> nearestBlocks(BlockPos origin, long width) {
        return () -> new AbstractIterator<>() {

            private long i = 0;
            private long x = origin.getX();
            private long y = origin.getY();
            private long z = origin.getZ();
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int increment = 1;
            private boolean mirror = false;

            @Nullable
            @Override
            protected BlockPos computeNext() {
                if(z > origin.getZ() + i) {
                    if(y <= origin.getY() - i) {
                        i++;
                        y = origin.getY();
                    } else {
                        y = origin.getY() + origin.getY() - y;
                    }
                    x = origin.getX() - i;
                    z = origin.getZ() - i;
                }
                this.cursor.set(x, y, z);
                if(i > width) return this.endOfData();
                if(y < origin.getY() + i && y > origin.getY() - i) {
                    if(mirror) {
                        if(x > origin.getX() - i) {
                            x--;
                        } else if(z > origin.getZ() - i) {
                            z--;
                        }
                        if(x <= origin.getX() - i && z <= origin.getZ() - i) {
                            mirror = false;
                            y = y - increment;
                            y = origin.getY() + origin.getY() - y;
                            increment = increment == 1 ? 0 : 1;
                        }
                    } else {
                        if(x < origin.getX() + i) {
                            x++;
                        } else if(z < origin.getZ() + i) {
                            z++;
                        }
                        if(x >= origin.getX() + i && z >= origin.getZ() + i) mirror = true;
                    }
                } else {
                    if(z <= origin.getZ() + i) {
                        if(x < origin.getX() + i) {
                            x++;
                        } else {
                            z++;
                            x = origin.getX() - i;
                        }
                    }
                }
                return this.cursor;
            }
        };
    }


    @Override
    public boolean canUse() {
        if(!(this.entity.valid && this.entity.isChunkLoaded())) return false;
        if(this.cachedBlockPos.isEmpty()) this.scanWantedBlocks((long) this.chunkDistance << 4);
        return this.entity.getTargetBlock() == null && !this.cachedBlockPos.isEmpty();
    }
}
