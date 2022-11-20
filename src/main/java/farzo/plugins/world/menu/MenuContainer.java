package farzo.plugins.world.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class MenuContainer<T extends Mob, V extends Player> implements Container {

    private final int size;
    public final NonNullList<ItemStack> items;
    private final List<ContainerListener> listeners;
    public List<HumanEntity> transaction;
    private int maxStack;
    private final List<BiConsumer<MenuContainer<T, V>, Integer>> actionBinder;
    private final T menuHolder;
    private final V interactor;
    private final ItemStack defaultItem = ItemStack.EMPTY;

    public MenuContainer(int size, T menuHolder, V interactor) {
        this.size = size;
        this.menuHolder = menuHolder;
        this.interactor = interactor;
        this.items = NonNullList.withSize(size, this.defaultItem);
        this.listeners = new Stack<>();
        this.transaction = new LinkedList<>();
        this.maxStack = MAX_STACK;
        this.actionBinder = new ArrayList<>(Collections.nCopies(size, (menuHolder1, interactor1) -> {}));
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        this.actionBinder.get(i).accept(this, i);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        this.actionBinder.get(i).accept(this, i);
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    public void setItem(int i, ItemStack itemStack, BiConsumer<MenuContainer<T, V>, Integer> action) {
        this.setItem(i, itemStack);
        this.actionBinder.set(i, action);
    }

    public void setAction(int i, BiConsumer<MenuContainer<T, V>, Integer> action) {
        this.actionBinder.set(i, action);
    }

    public void removeAction(int i) {
        this.actionBinder.set(i, (entity, player) -> {});
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStack;
    }

    @Override
    public void setChanged() {
        for (ContainerListener listener : this.listeners) {
            listener.containerChanged(this);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity craftHumanEntity) {
        this.transaction.add(craftHumanEntity);
    }

    @Override
    public void onClose(CraftHumanEntity craftHumanEntity) {
        this.transaction.remove(craftHumanEntity);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    @Nullable
    @Override
    public InventoryHolder getOwner() {
        return null;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Nullable
    @Override
    public Location getLocation() {
        return null;
    }

    @Nullable
    @Override
    public Recipe getCurrentRecipe() {
        return null;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.actionBinder.clear();
        Collections.fill(this.actionBinder, (menuHolder, interactor) -> {});
    }
}
