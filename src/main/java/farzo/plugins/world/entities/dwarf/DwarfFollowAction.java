package farzo.plugins.world.entities.dwarf;

import farzo.plugins.world.entities.ai.goal.FollowPlayerGoal;
import farzo.plugins.world.menu.MenuContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public class DwarfFollowAction extends DwarfAction {

    private final Goal followGoal;

    public DwarfFollowAction(Dwarf menuHolder, Player interactor) {
        super(menuHolder, interactor);
        this.followGoal = new FollowPlayerGoal<>(this.getMenuHolder(), 1, 5, 1, false);
    }

    @Override
    public void accept(MenuContainer menuContainer, Integer i) {
        if(this.getMenuHolder().hasOwner() && this.getInteractor().getUUID() == this.getMenuHolder().getOwnerUUID()) {
            this.getMenuHolder().goalSelector.removeGoal(this.followGoal);
            this.getMenuHolder().setOwnerUUID(null);
        } else if(!this.getMenuHolder().hasOwner()) {
            this.getMenuHolder().setOwnerUUID(this.getInteractor().getUUID());
            this.getMenuHolder().goalSelector.addGoal(0, new FollowPlayerGoal<>(this.getMenuHolder(), 1, 5, 1, false));
        }
        this.getInteractor().closeContainer();
    }
}
