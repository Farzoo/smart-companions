package farzo.plugins.world;

import farzo.plugins.SmartCompanions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;

import java.awt.geom.Point2D;

// Fonctionne mais à revoir
public class PreventChunkUnloading {

    private final Entity entity;
    private ChunkPos lastChunkPos;

    public PreventChunkUnloading(Entity entity) {
        this.entity = entity;
        this.lastChunkPos = this.entity.chunkPosition();
    }

    /**
     * Should be called every entity tick
     */
    public void tick() {
        ChunkPos newChunkPos = this.entity.chunkPosition();
        Level level = this.entity.getLevel();
        ChunkPos.rangeClosed(this.lastChunkPos, 1).forEach(
                chunkPos -> level.getChunkAt(chunkPos.getWorldPosition()).getBukkitChunk().removePluginChunkTicket(SmartCompanions.getInstance())
        );
        ChunkPos.rangeClosed(newChunkPos, 1).forEach(
                chunkPos -> level.getChunkAt(chunkPos.getWorldPosition()).getBukkitChunk().addPluginChunkTicket(SmartCompanions.getInstance())
        );
        this.lastChunkPos = newChunkPos;
    }
}
