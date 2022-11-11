package farzo.plugins.world.entities;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftRegionAccessor;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class CustomEntitiesUtils {
    public static <T extends CraftEntity> T spawnEntity(Supplier<T> supplier, Location location) {
        final T entityType = supplier.get();

        final Entity entity = ((CraftRegionAccessor) Objects.requireNonNull(
                location.getWorld())).addEntity(entityType.getHandle(),
                CreatureSpawnEvent.SpawnReason.CUSTOM); // Only way i found to spawn entity
        // without entity instance check by bukkit
        entity.teleport(location);
        return entityType;
    }
}
