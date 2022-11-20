package farzo.plugins.world.entities;

import net.minecraft.core.BlockPos;

public interface HasHome {
    BlockPos getHome();
    void setHome(BlockPos home);
}
