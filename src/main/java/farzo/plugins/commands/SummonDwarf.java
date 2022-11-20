package farzo.plugins.commands;

import farzo.plugins.world.entities.*;
import farzo.plugins.world.entities.dwarf.Dwarf;
import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SummonDwarf implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        CraftPlayer player = (CraftPlayer) sender;
        ServerLevel world = player.getHandle().getLevel();
        Dwarf dwarf = new Dwarf(CustomEntities.DWARF.getEntityType(), world);
        Location loc = player.getLocation();
        CustomEntitiesUtils.spawnEntity(dwarf, world, new PositionImpl(loc.getX(), loc.getY(), loc.getZ()));
        Bukkit.getLogger().log(java.util.logging.Level.INFO, "Summoning dwarf for player.");
        return true;
    }
}
