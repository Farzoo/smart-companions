package farzo.plugins.world.menu;

import farzo.plugins.world.entities.ai.goal.FollowPlayerGoal;
import farzo.plugins.world.entities.dwarf.Dwarf;
import farzo.plugins.world.entities.dwarf.DwarfFollowAction;
import farzo.plugins.world.entities.dwarf.DwarfHarvestAction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DwarfMenuProvider implements MenuProvider {

    private final Container container;
    private final MenuType<ChestMenu> menuType;

    private DwarfMenuProvider(Container container, MenuType<ChestMenu> menuType) {
        this.container = container;
        this.menuType = menuType;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Dwarf interaction menu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DwarfMenu(this.menuType, i, player.getInventory(), this.container, this.container.getContainerSize() / 9);
    }

    public static DwarfMenuProvider createHomeMenu(Dwarf menuHolder, Player interactor) {
        MenuContainer<Dwarf, Player> container = new MenuContainer<>(9, menuHolder, interactor);

        ItemStack jobsItem = new ItemStack(Material.EMERALD);
        ItemMeta meta = jobsItem.getItemMeta(); meta.setDisplayName(ChatColor.BLUE + "Jobs");
        meta.setLore(List.of("Louer les services de " + ChatColor.BOLD + menuHolder.getBukkitEntity().getCustomName()));
        jobsItem.setItemMeta(meta);

        container.setItem(0,
                CraftItemStack.asNMSCopy(jobsItem),
                (menu, i) -> interactor.openMenu(DwarfMenuProvider.createOrderMenu(menuHolder, interactor))
        );

        return new DwarfMenuProvider(container, MenuType.GENERIC_9x1);
    }

    public static DwarfMenuProvider createOrderMenu(Dwarf menuHolder, Player interactor) {
        MenuContainer<Dwarf, Player> container = new MenuContainer<>(9, menuHolder, interactor);

        ItemStack followItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta followItemMeta = followItem.getItemMeta();
        followItemMeta.setDisplayName(ChatColor.YELLOW + "Follow");
        followItemMeta.setLore(List.of("Demander à " + ChatColor.BOLD + menuHolder.getBukkitEntity().getCustomName() + " de vous suivre"));
        followItem.setItemMeta(followItemMeta);
        container.setItem(0,
                CraftItemStack.asNMSCopy(followItem),
                new DwarfFollowAction(menuHolder, interactor)
        );


        ItemStack harvestWoodItem = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta harvestWoodItemMeta = harvestWoodItem.getItemMeta();
        harvestWoodItemMeta.setDisplayName(ChatColor.GREEN + "Harvest woods");
        harvestWoodItemMeta.setLore(List.of("Demander à " + ChatColor.BOLD + menuHolder.getBukkitEntity().getCustomName() + " de récupérer le bois autour.", "Prix : " + ChatColor.UNDERLINE + " 15 émeraudes"));
        harvestWoodItem.setItemMeta(harvestWoodItemMeta);

        container.setItem(1,
                CraftItemStack.asNMSCopy(harvestWoodItem),
                new DwarfHarvestAction(
                        menuHolder,
                        interactor,
                        new net.minecraft.world.item.ItemStack(Items.EMERALD, 15),
                        Set.of(Blocks.ACACIA_LOG, Blocks.OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.SPRUCE_LOG, Blocks.MANGROVE_LOG),
                        Set.of(Blocks.ACACIA_LOG, Blocks.OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.SPRUCE_LOG, Blocks.MANGROVE_LOG, Blocks.ACACIA_LEAVES, Blocks.AZALEA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES))
        );
        return new DwarfMenuProvider(container, MenuType.GENERIC_9x1);
    }
}
