package farzo.plugins.commands;

import farzo.plugins.world.entities.*;
import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        CraftPlayer craftPlayer;
        Player player;
        if(args.length == 1) {
            player = Bukkit.getPlayerExact(args[0]);
            if (player == null) return false;
            craftPlayer = (CraftPlayer) player;
            if (!craftPlayer.isOnline()) return false;
        } else {
            craftPlayer = ((CraftPlayer) sender);
        }
        ServerLevel world = craftPlayer.getHandle().getLevel();
        Location loc = craftPlayer.getLocation();
        AllayCompanion allayCompanion = new AllayCompanion(CustomEntities.ALLAY_COMPANION.getEntityType(), world, craftPlayer.getHandle());
        CustomEntitiesUtils.spawnEntity(allayCompanion,  world, new PositionImpl(loc.getX(), loc.getY(), loc.getZ()));
        Bukkit.getLogger().log(java.util.logging.Level.INFO, "Summoning allay companion for player " + craftPlayer.getDisplayName() + ".");
        return true;
    }
}
