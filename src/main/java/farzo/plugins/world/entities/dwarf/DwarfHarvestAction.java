package farzo.plugins.world.entities.dwarf;

import farzo.plugins.world.entities.ai.goal.MineBlockGoal;
import farzo.plugins.world.entities.ai.goal.NearestMineableBlock;
import farzo.plugins.world.menu.MenuContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class DwarfHarvestAction extends DwarfAction {

    private final Goal targetBlockGoal;
    private final Goal mineBlockGoal;
    private final ItemStack price;
    private boolean hasJob;

    public DwarfHarvestAction(Dwarf menuHolder, Player interactor, ItemStack price, Set<Block> toHarvest, Set<Block> breakable) {
        super(menuHolder, interactor);
        this.targetBlockGoal = new NearestMineableBlock<>(this.getMenuHolder(), 2, (entity, blockpos) -> true, toHarvest.toArray(new Block[0]));
        this.mineBlockGoal = new MineBlockGoal<>(this.getMenuHolder(), 1, breakable.toArray(new Block[0]));
        this.price = price;
        this.hasJob = false;
    }

    @Override
    public void accept(MenuContainer menuContainer, Integer i) {
        if(!hasJob && tryRemoveItemStackFromContainer(this.getInteractor().getInventory(), this.price)) {
            this.getMenuHolder().targetSelector.addGoal(0, this.targetBlockGoal);
            this.getMenuHolder().goalSelector.addGoal(0, this.mineBlockGoal);
            this.hasJob = true;
        } else {
            this.getMenuHolder().targetSelector.removeGoal(this.targetBlockGoal);
            this.getMenuHolder().goalSelector.removeGoal(this.mineBlockGoal);
            this.hasJob = false;
        }
    }

    private static boolean tryRemoveItemStackFromContainer(Container container, ItemStack itemStack) {
        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack cursor = container.getItem(i);
            if (cursor.is(itemStack.getItem()) && cursor.getCount() >= itemStack.getCount()) {
                if(cursor.getCount() == itemStack.getCount()) cursor.setItem(Items.AIR);
                else cursor.setCount(cursor.getCount() - itemStack.getCount());
                return true;
            }
        }
        return false;
    }
}
