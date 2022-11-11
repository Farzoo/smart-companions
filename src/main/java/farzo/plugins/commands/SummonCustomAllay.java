package farzo.plugins.commands;

import farzo.plugins.world.entities.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SummonCustomAllay implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        CraftPlayer player = (CraftPlayer) sender;
        Level world = player.getHandle().getLevel();
        CraftAllayCompanion allayCompanion = new CraftAllayCompanion(
                (CraftServer) sender.getServer(),
                new AllayCompanion(
                        CustomEntities.ALLAY_COMPANION.getEntityType(),
                        world,
                        player.getHandle()),
                player
        );
        CustomEntitiesUtils.spawnEntity(() -> allayCompanion,  player.getLocation());
        Bukkit.getLogger().log(java.util.logging.Level.INFO, "Summoning allay companion for player " + player.getDisplayName() + ".");
        return true;
    }
}
