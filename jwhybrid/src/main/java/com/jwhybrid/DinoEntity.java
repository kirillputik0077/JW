package com.jwhybrid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class DinoEntity extends TamableAnimal {
    private static final EntityDataAccessor<Integer> SPECIES =
            SynchedEntityData.defineId(DinoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LEVEL =
            SynchedEntityData.defineId(DinoEntity.class, EntityDataSerializers.INT);
    private int xp;

    public DinoEntity(EntityType<? extends DinoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPECIES, 0);
        entityData.define(LEVEL, 1);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.1, 8f, 2f, false));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8f));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                e -> !isTame() && getSpecies().aggressive));
    }

    // ---- вид и уровень ----
    public Species getSpecies() { return Species.byOrdinal(entityData.get(SPECIES)); }
    public int dinoLevel() { return entityData.get(LEVEL); }

    public void setSpecies(Species s) {
        entityData.set(SPECIES, s.ordinal());
        applyStats();
        refreshDimensions();
        setHealth(getMaxHealth());
    }

    public void setDinoLevel(int lvl) {
        entityData.set(LEVEL, Math.max(1, Math.min(10, lvl)));
        applyStats();
    }

    private void applyStats() {
        Species s = getSpecies();
        double mult = 1.0 + 0.1 * (dinoLevel() - 1);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(s.hp * mult);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(s.dmg * mult);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(s.speed);
    }

    private void gainXp(int n) {
        xp += n;
        while (xp >= 5 && dinoLevel() < 10) {
            xp -= 5;
            setDinoLevel(dinoLevel() + 1);
            if (getOwner() instanceof ServerPlayer p) {
                p.displayClientMessage(Component.literal("§a" + getSpecies().ru + " достиг уровня " + dinoLevel() + "!"), true);
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(getSpecies().width, getSpecies().height);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (SPECIES.equals(key)) refreshDimensions();
        super.onSyncedDataUpdated(key);
    }

    @Override
    public Component getName() {
        Component custom = getCustomName();
        if (custom != null) return custom;
        return Component.literal(getSpecies().ru + " ур." + dinoLevel());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor lvl, DifficultyInstance diff, MobSpawnType reason,
                                        SpawnGroupData data, CompoundTag tag) {
        SpawnGroupData r = super.finalizeSpawn(lvl, diff, reason, data, tag);
        setSpecies(Species.random(getRandom()));
        return r;
    }

    // ---- экономика: прирост монет владельцу ----
    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && isTame() && tickCount > 0 && tickCount % 1200 == 0) {
            if (getOwner() instanceof ServerPlayer p) {
                int income = getSpecies().income * dinoLevel();
                JwData.addCoins(p, income);
                p.displayClientMessage(Component.literal("§6+" + income + " монет §7(" + getSpecies().ru + ")"), true);
            }
        }
    }

    // ---- взаимодействие ----
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack st = player.getItemInHand(hand);
        if (isTame() && isOwnedBy(player)) {
            FoodProperties fp = st.getItem().getFoodProperties();
            if (fp != null && fp.isMeat()) {
                if (!level().isClientSide) {
                    heal(fp.getNutrition() * 2);
                    if (!player.getAbilities().instabuild) st.shrink(1);
                    gainXp(1);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (player.isSecondaryUseActive() && st.isEmpty()) {
                if (!level().isClientSide) {
                    ItemStack cap = DinoCapsuleItem.make(getSpecies(), dinoLevel());
                    if (!player.getInventory().add(cap)) player.drop(cap, false);
                    discard();
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !isTame() && !hasCustomName();
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putInt("Species", getSpecies().ordinal());
        t.putInt("DinoLevel", dinoLevel());
        t.putInt("DinoXp", xp);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        entityData.set(LEVEL, Math.max(1, t.getInt("DinoLevel")));
        setSpecies(Species.byOrdinal(t.getInt("Species")));
        xp = t.getInt("DinoXp");
    }
}
