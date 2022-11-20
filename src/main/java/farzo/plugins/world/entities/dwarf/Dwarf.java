package farzo.plugins.world.entities.dwarf;

import farzo.plugins.world.PreventChunkUnloading;
import farzo.plugins.world.entities.CustomEntities;
import farzo.plugins.world.entities.HasHome;
import farzo.plugins.world.entities.BlockTargeter;
import farzo.plugins.world.menu.DwarfMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class Dwarf extends Pillager implements Npc, HasCustomInventoryScreen, InventoryCarrier, OwnableEntity, HasHome, BlockTargeter {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(Dwarf.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Optional<BlockPos>> DATA_HOME_POS = SynchedEntityData.defineId(Dwarf.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private PreventChunkUnloading pcu;
    private final SimpleContainer inventory = new SimpleContainer(54);
    private BlockPos home = null;
    private BlockPos targetBlock;

    // goal minage (class block..., maxDistance,
    // Goal bring items
    // target home selector
    // Goal go home


    public Dwarf(EntityType<? extends PathfinderMob> entitytypes, Level world) {
        super(CustomEntities.DWARF.getNmsEntityType(), world);
        this.setPersistenceRequired(true);
        this.setCustomName(Component.literal("[Dwarf]").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE));
        this.setCustomNameVisible(true);
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_PICKAXE));
        //this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(3000D);
    }


    @Override
    protected void registerGoals() {
        this.makeDefaultGoals();
    }

    public void makeDefaultGoals() {
        this.goalSelector.removeAllGoals();
        this.targetSelector.removeAllGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8F, 60));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, LivingEntity.class, 4F, 0.1F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, this.getClass()).setAlertOthers(this.getClass()));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true, livingEntity -> livingEntity.getType() != EntityType.CREEPER));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.put("CustomInventory", this.inventory.createTag());
        if(this.getHome() != null) {
            BlockPos pos = this.getHome();
            nbttagcompound.putIntArray("Home", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.inventory.fromTag(nbttagcompound.getList("CustomInventory", 10));
        if(nbttagcompound.contains("Home")) {
            int[] vec3 = nbttagcompound.getIntArray("Home");
            this.setHome(new BlockPos(vec3[0], vec3[1], vec3[2]));
        }
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level.isClientSide()) {
            this.playSound(SoundEvents.ANVIL_PLACE, this.distanceTo(player), this.level.random.nextFloat() * 0.1F + 0.9F);
            player.openMenu(DwarfMenuProvider.createHomeMenu(this, player));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
        if(!this.level.isClientSide()) {
            this.openCustomInventoryScreen(entityhuman);
        }
        return InteractionResult.FAIL;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        //this.entityData.define(DATA_HOME_POS, Optional.empty());
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }


    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(ownerUUID));
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
    public EntityType<?> getType() {
        return CustomEntities.DWARF.getEntityType();
    }

    @Override
    public BlockPos getHome() {
        return this.home;
    }

    @Override
    public void setHome(BlockPos home) {
        this.home = home;
    }

    @Override
    public void setTargetBlock(@Nullable BlockPos pos) {
        this.targetBlock = pos != null ? new BlockPos(pos) : null;
    }

    @Nullable
    @Override
    public BlockPos getTargetBlock() {
        return this.targetBlock;
    }
}
