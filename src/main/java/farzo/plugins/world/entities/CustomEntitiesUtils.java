package farzo.plugins.world.entities;

import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntitiesUtils {
    public static <T extends Entity> T spawnEntity(T entity, ServerLevel level, PositionImpl position) {
        level.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        entity.teleportTo(level, position);
        return entity;
    }
}
