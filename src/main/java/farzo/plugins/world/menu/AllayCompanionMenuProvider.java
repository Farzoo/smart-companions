package farzo.plugins.world.menu;

import farzo.plugins.world.entities.AllayCompanion;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;

import javax.annotation.Nullable;

public class AllayCompanionMenuProvider implements MenuProvider {

    private final AllayCompanion entity;

    public AllayCompanionMenuProvider(AllayCompanion entity) {
        this.entity = entity;
    }
    @Override
    public Component getDisplayName() {
        return entity.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return ChestMenu.sixRows(i, inventory, this.entity.getInventory());
    }
}
