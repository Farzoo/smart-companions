package farzo.plugins;

import farzo.plugins.commands.SummonCustomAllay;
import farzo.plugins.commands.SummonDwarf;
import farzo.plugins.world.entities.CustomEntities;
import farzo.plugins.world.entities.CustomEntity;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SmartCompanions extends JavaPlugin {

    private static SmartCompanions instance;

    @Override
    public void onLoad() {
        super.onLoad();
        SmartCompanions.instance = this;
        Bukkit.getLogger().info("[smart-companions] Custom entity " + CustomEntities.ALLAY_COMPANION.getName());
        Bukkit.getLogger().info("[smart-companions] Custom entity " + CustomEntities.DWARF.getName());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.getCommand("summonAllay").setExecutor(new SummonCustomAllay());
        this.getCommand("summonDwarf").setExecutor(new SummonDwarf());
    }

    public static SmartCompanions getInstance() {
        return SmartCompanions.instance;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Plugin shutdown logic
    }
}
