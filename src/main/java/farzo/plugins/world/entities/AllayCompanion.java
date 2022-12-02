package farzo.plugins.world.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import farzo.plugins.world.PreventChunkUnloading;
import farzo.plugins.world.entities.ai.goal.StayCloseToOwner;
import farzo.plugins.world.menu.AllayCompanionMenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.schedule.Activity;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.*;

public class AllayCompanion extends Allay implements OwnableEntity, HasCustomInventoryScreen, RangedAttackMob {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(AllayCompanion.class, EntityDataSerializers.OPTIONAL_UUID);
    private final AllayCompanionMenuProvider menu = new AllayCompanionMenuProvider(this);
    private final SimpleContainer inventory = new SimpleContainer(54);

    protected static final ImmutableList<SensorType<? extends Sensor<? super AllayCompanion>>> SENSOR_TYPES;
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;

    static {
        SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
        MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.LIKED_NOTEBLOCK_POSITION, MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.IS_PANICKING);
    }

    private PreventChunkUnloading pcu;

    @Override
    public void travel(Vec3 vec3d) {
        super.travel(vec3d);
    }

    private void init() {
        this.setPersistenceRequired();
        if(this.hasOwner()) this.pcu = new PreventChunkUnloading(this);
    }

    public AllayCompanion(EntityType<? extends Allay> entitytypes, Level world) {
        super(CustomEntities.ALLAY_COMPANION.getNmsEntityType(), world);
        this.init();
    }

    public AllayCompanion(EntityType<? extends Allay> entitytypes, Level world, Player player) {
        this(CustomEntities.ALLAY_COMPANION.getNmsEntityType(), world);
        if (player != null) this.setOwnerUUID(player.getUUID());
        this.init();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public void tick() {
        if(this.pcu != null) this.pcu.tick();
        super.tick();
    }

    public void addAdditionalSaveData(CompoundTag nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.put("CustomInventory", this.inventory.createTag());
        if (this.getOwnerUUID() != null) {
            nbttagcompound.putUUID("Owner", this.getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.inventory.fromTag(nbttagcompound.getList("CustomInventory", 10));
        if(nbttagcompound.contains("Owner")) this.setOwnerUUID(nbttagcompound.getUUID("Owner"));
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.25D, 20, 20.0F));
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<AllayCompanion> b = Brain.provider(MEMORY_TYPES, SENSOR_TYPES).makeBrain(dynamic);
        b.addActivity(Activity.CORE, 0, ImmutableList.of(new StayCloseToOwner<>(this, 3, 5, 2, true), new MoveToTargetSink(), new Swim(0.8F), new AnimalPanic(2.5F), new LookAtTargetSink(10, 90)));
        b.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, new SetWalkTargetFromLookTarget(1.0F, 2)), Pair.of(1, new FlyingRandomStroll(0.6F))));
        b.setCoreActivities(ImmutableSet.of(Activity.CORE));
        b.setDefaultActivity(Activity.CORE);
        return b;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(ownerUUID));
        if(this.getOwner() != null) this.setCustomName(this.getOwner().getDisplayName());
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return Optional.ofNullable(this.getServer())
                .map(MinecraftServer::getPlayerList)
                .map(playerList -> playerList.getPlayer(this.getOwnerUUID()))
                .orElse(null);
    }

    public boolean hasOwner() {
        return this.getOwner() != null;
    }

    @Override
    public EntityType<? extends Entity> getType() {
        return CustomEntities.ALLAY_COMPANION.getEntityType();
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    protected InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
        if(!this.level.isClientSide()) {
            if(this.hasOwner()) {
                if (entityhuman.isSecondaryUseActive()) {
                    this.playSound(SoundEvents.ENDER_CHEST_OPEN, this.distanceTo(entityhuman), this.level.random.nextFloat() * 0.1F + 0.9F);
                    this.openCustomInventoryScreen(entityhuman);
                    return InteractionResult.SUCCESS;
                }
            } else if(!this.hasOwner() && entityhuman.getItemInHand(enumhand).is(Items.DIAMOND)) {
                boolean success = this.level.random.nextIntBetweenInclusive(0, 10) == 0;
                entityhuman.getItemInHand(enumhand).shrink(1);
                if(success) this.tame(entityhuman);
                this.playTamingScene(success);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private void tame(Entity owner) {
        this.setOwnerUUID(owner.getUUID());
        this.setCustomName(owner.getDisplayName());
        this.pcu = new PreventChunkUnloading(this);
    }

    private void playTamingScene(boolean success) {
        Particle particle = Particle.SMOKE_NORMAL;

        if (success) {
            particle = Particle.HEART;
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LARGE_AMETHYST_BUD_PLACE, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        }


        for(int i = 0; i < 7; i++) {
            double offsetX = this.random.nextGaussian() * 0.02D;
            double offsetY = this.random.nextGaussian() * 0.02D;
            double offsetZ = this.random.nextGaussian() * 0.02D;
            this.level.getWorld().spawnParticle(particle, this.getBukkitEntity().getLocation(), 1, offsetX, offsetY, offsetZ);
        }
    }


    @Override
    protected void dropEquipment() {
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
    }

    public void openCustomInventoryScreen(Player entityhuman) {
        if (!this.level.isClientSide() && this.hasOwner()) {
            entityhuman.openMenu(this.menu);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float v) {
        SpectralArrow projectile = new SpectralArrow(EntityType.SPECTRAL_ARROW, this.getLevel());
        projectile.setOnGround(false);
        projectile.setPos(this.getX() - (double)(this.getBbWidth() + 1F) * 0.5D * (double) Mth.sin(this.yBodyRot * 0.017453292F), this.getEyeY() - 0.10000000149011612D, this.getZ() + (double)(this.getBbWidth() + 1F) * 0.5D * (double)Mth.cos(this.yBodyRot * 0.017453292F));
        double d0 = livingEntity.getX() - this.getX();
        double d1 = livingEntity.getY(0.3333333333333333D) - projectile.getY();
        double d2 = livingEntity.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.20000000298023224D;
        projectile.shoot(d0, d1 + d3, d2, 1.5F, 0F);
        if (!this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ALLAY_THROW, this.getSoundSource(), 0.25F, 1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        }
        this.level.addFreshEntity(projectile);
    }
}





