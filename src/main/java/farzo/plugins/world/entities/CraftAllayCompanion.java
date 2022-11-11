package farzo.plugins.world.entities;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftAllay;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;

public class CraftAllayCompanion extends CraftAllay {

    public CraftAllayCompanion(CraftServer server, AllayCompanion entity) {
        super(server, entity);
    }

    public CraftAllayCompanion(CraftServer server, AllayCompanion entity, CraftPlayer player) {
        super(server, entity);
        this.setCustomName(player.getDisplayName());
    }
}
