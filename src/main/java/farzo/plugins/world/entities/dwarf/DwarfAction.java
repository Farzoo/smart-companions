package farzo.plugins.world.entities.dwarf;

import farzo.plugins.world.menu.MenuContainer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class DwarfAction implements BiConsumer<MenuContainer<Dwarf, Player>, Integer> {

    private final Dwarf menuHolder;
    private final Player interactor;


    public DwarfAction(Dwarf menuHolder, Player interactor) {
        this.menuHolder = menuHolder;
        this.interactor = interactor;
    }


    public Dwarf getMenuHolder() {
        return this.menuHolder;
    }

    public Player getInteractor() {
        return this.interactor;
    }
}
