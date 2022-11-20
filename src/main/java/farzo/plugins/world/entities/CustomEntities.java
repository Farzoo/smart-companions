package farzo.plugins.world.entities;

import farzo.plugins.world.entities.dwarf.Dwarf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Pillager;

public class CustomEntities {
    public static final CustomEntity<Allay, AllayCompanion> ALLAY_COMPANION =
            new CustomEntity<>(
                    "allay",
                        Allay.class,
                        EntityType.ALLAY,
                        AllayCompanion.class,
                        EntityType.Builder.<AllayCompanion>of(AllayCompanion::new, MobCategory.CREATURE).sized(0.35F, 0.6F).clientTrackingRange(8).updateInterval(2)
            );

    public static final CustomEntity<Pillager, Dwarf> DWARF =
            new CustomEntity<>(
                    "smartcompanions:dwarf",
                    Pillager.class,
                    EntityType.PILLAGER,
                    Dwarf.class,
                    EntityType.Builder.<Dwarf>of(Dwarf::new, MobCategory.CREATURE).sized(0.6F, 0.5F).clientTrackingRange(10).canSpawnFarFromPlayer().fireImmune()
            );
}
