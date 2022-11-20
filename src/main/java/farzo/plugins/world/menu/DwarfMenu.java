package farzo.plugins.world.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class DwarfMenu extends ChestMenu {

    public DwarfMenu(MenuType<?> containers, int i, Inventory playerInventory, Container container,  int j) {
        super(containers, i, playerInventory, container, j);
    }
}
