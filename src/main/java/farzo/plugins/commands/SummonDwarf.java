package farzo.plugins.commands;

import farzo.plugins.world.entities.AllayCompanion;
import farzo.plugins.world.entities.CraftAllayCompanion;
import farzo.plugins.world.entities.CustomEntities;
import farzo.plugins.world.entities.CustomEntitiesUtils;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SummonDwarf implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;

        return true;
    }
}
