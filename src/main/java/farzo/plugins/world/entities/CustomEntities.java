package farzo.plugins.world.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.allay.Allay;

public class CustomEntities {
    public static final CustomEntity<Allay, AllayCompanion> ALLAY_COMPANION =
            new CustomEntity<>("allay",
                    Allay.class,
                    EntityType.ALLAY,
                    AllayCompanion.class,
                    EntityType.Builder.<AllayCompanion>of(AllayCompanion::new, MobCategory.CREATURE).sized(0.35F, 0.6F).clientTrackingRange(8).updateInterval(2)
            );
}
