package farzo.plugins.world.entities.ai;

import farzo.plugins.world.entities.AllayCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.Map;

public class StayCloseToOwner<E extends Mob & OwnableEntity> extends Behavior<E> {

    private final int closeEnough;
    private final int speedModifier;
    private E pet;
    private int tooFar;
    private boolean canFly;

    public StayCloseToOwner(E pet, int closeEnough, int tooFar, int speedModifier, boolean canFly) {
        super(Map.of());
        this.pet = pet;
        this.closeEnough = closeEnough;
        this.tooFar = tooFar;
        this.speedModifier = speedModifier;
        this.canFly = canFly;
    }

    protected void tick(ServerLevel var0, E var1, long var2) {
    }

    protected boolean checkExtraStartConditions(ServerLevel var0, E ownedEntity) {
        return this.pet.getOwner() != null && !this.pet.getOwner().position().closerThan(ownedEntity.position(), this.tooFar);
    }



    protected void start(ServerLevel var0, E ownedEntity, long var2) {
        if(this.pet.distanceTo(this.pet.getOwner()) > 20) {
            this.teleportToOwner();
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(ownedEntity, this.pet.getOwner(), this.speedModifier, this.closeEnough);
        }
    }

    private void teleportToOwner() {
        BlockPos blockposition = this.pet.getOwner().blockPosition();

        for(int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int i, int j, int k) {
        Entity owner = this.pet.getOwner();
        boolean sameDimension = owner.getLevel().dimensionType() != this.pet.getLevel().dimensionType();
        if (Math.abs((double)i - owner.getX()) < 2.0D && Math.abs((double)k - owner.getX()) < 2.0D && sameDimension) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(i, j, k))) {
            return false;
        } else {
            CraftEntity entity = this.pet.getBukkitEntity();
            Location to = new Location(owner.getLevel().getWorld(), (double)i + 0.5D, (double)j, (double)k + 0.5D, this.pet.getYRot(), this.pet.getXRot());
            EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
            this.pet.getBukkitEntity().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            } else {
                to = event.getTo();
                Entity new_entity = this.pet.teleportTo((ServerLevel) owner.getLevel(), new PositionImpl(to.getX(), to.getY(), to.getZ()));
                this.pet = new_entity == null ? this.pet : (E) new_entity;
                this.pet.getNavigation().stop();
                return true;
            }
        }
    }

    private boolean canTeleportTo(BlockPos blockposition) {
        BlockPathTypes pathtype = WalkNodeEvaluator.getBlockPathTypeStatic(this.pet.getOwner().getLevel(), blockposition.mutable());
        if (pathtype != BlockPathTypes.WALKABLE && !this.canFly) {
            return false;
        } else {
            BlockState iblockdata = this.pet.getOwner().getLevel().getBlockState(blockposition.below());
            if (!this.canFly && iblockdata.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockposition1 = blockposition.subtract(this.pet.blockPosition());
                return this.pet.getOwner().getLevel().noCollision(this.pet, this.pet.getBoundingBox().move(blockposition1));
            }
        }
    }

    private int randomIntInclusive(int i, int j) {
        return this.pet.getRandom().nextInt(j - i + 1) + i;
    }
}
