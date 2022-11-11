package farzo.plugins.world;

import farzo.plugins.SmartCompanions;
import net.minecraft.world.entity.Entity;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;

// Fonctionne mais à revoir
public class PreventChunkUnloading {

    private final CraftEntity entity;
    private Chunk lastChunk;

    public PreventChunkUnloading(Entity entity) {
        this.entity = entity.getBukkitEntity();
        this.lastChunk = this.entity.getWorld().getChunkAt(this.entity.getLocation());
        this.lastChunk.addPluginChunkTicket(SmartCompanions.getInstance());
    }

    /**
     * Should be called every entity tick
     */
    public void setLastChunk() {
        Chunk chunk = this.entity.getWorld().getChunkAt(this.entity.getLocation());
        if(this.lastChunk != chunk) {
            this.lastChunk = chunk;
            this.lastChunk.removePluginChunkTicket(SmartCompanions.getInstance());
            this.lastChunk.addPluginChunkTicket(SmartCompanions.getInstance());
        }
    }
}
