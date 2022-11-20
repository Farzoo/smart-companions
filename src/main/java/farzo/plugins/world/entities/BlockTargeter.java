package farzo.plugins.world.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public interface BlockTargeter {
    void setTargetBlock(@Nullable BlockPos pos);
    @Nullable
    BlockPos getTargetBlock();
}
