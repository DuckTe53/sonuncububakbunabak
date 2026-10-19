package com.silahmod.vehicle;

import com.silahmod.SilahMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CarEntity extends Entity {
    public static final EntityDataAccessor<Float> HP = SynchedEntityData.defineId(CarEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> CAR_CLASS = SynchedEntityData.defineId(CarEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DAMAGE_STAGE = SynchedEntityData.defineId(CarEntity.class, EntityDataSerializers.INT);
    private float speed;
    private float steering;
    private double oldX, oldY, oldZ;
    private boolean collisionCheck;

    public CarEntity(EntityType<? extends CarEntity> type, Level level) { super(type, level); this.noPhysics = false; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HP, 400.0f); builder.define(CAR_CLASS, 0); builder.define(DAMAGE_STAGE, 0);
    }
    public void setCarClass(int value) { entityData.set(CAR_CLASS, Math.max(0, Math.min(2, value))); }
    public float getHp() { return entityData.get(HP); }
    public void damageCar(float amount) {
        if (level().isClientSide() || isRemoved() || amount <= 0) return;
        float applied = entityData.get(CAR_CLASS) == 1 ? amount * 0.55f : amount; // Skoda zırhlı gövde
        float hp = Math.max(0, getHp() - applied); entityData.set(HP, hp);
        entityData.set(DAMAGE_STAGE, Math.min(2, (int)((400 - hp) / 150)));
        if (hp <= 0) explode();
    }
    private void explode() {
        if (!(level() instanceof ServerLevel server)) return;
        // TNT-benzeri oyuncu hasari; block interaction kapali, bloklar korunur.
        for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(4.0))) {
            double distance = player.distanceTo(this);
            if (distance <= 4.0) player.hurtServer(server, server.damageSources().explosion(this, getControllingPassenger()), (float)(12.0 * (1.0 - distance / 5.0)));
        }
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
        server.playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.BLOCKS, 4.0f, 1.0f);
        discard();
    }
    public void applyControls(float throttle, float turn) {
        if (level().isClientSide()) return;
        speed += throttle * (0.006f + entityData.get(CAR_CLASS) * 0.0025f);
        if (throttle < -0.1f) speed *= 0.94f;
        if (Math.abs(throttle) < 0.01f) speed *= 0.985f;
        float max = 0.22f + entityData.get(CAR_CLASS) * 0.10f;
        speed = Math.max(-0.08f, Math.min(max, speed));
        steering = turn;
        setYRot(getYRot() - steering * Math.min(2.4f, Math.abs(speed) * 12.0f) * (speed < 0 ? -1 : 1));
        double rad = Math.toRadians(getYRot());
        setDeltaMovement(-Math.sin(rad) * speed, getDeltaMovement().y, Math.cos(rad) * speed);
    }
    @Override public void tick() {
        super.tick();
        oldX = getX(); oldY = getY(); oldZ = getZ();
        if (!onGround()) setDeltaMovement(getDeltaMovement().add(0, -0.08, 0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.92, 0.98, 0.92));
        if (!level().isClientSide() && hasImpulse && horizontalCollision) damageCar(Math.max(1, (float)(Math.abs(speed) * 260)));
        if (!level().isClientSide() && tickCount % 5 == 0 && getControllingPassenger() instanceof Player) {
            // Passenger input is supplied by the client packet.
        }
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { damageCar(amount); return true; }
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) { entityData.set(HP, tag.getFloat("CarHP")); entityData.set(CAR_CLASS, tag.getInt("CarClass")); entityData.set(DAMAGE_STAGE, tag.getInt("DamageStage")); }
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) { tag.putFloat("CarHP", getHp()); tag.putInt("CarClass", entityData.get(CAR_CLASS)); tag.putInt("DamageStage", entityData.get(DAMAGE_STAGE)); }
    @Override public boolean canCollideWith(Entity other) { return other != getControllingPassenger() && super.canCollideWith(other); }
    @Override public boolean isPickable() { return true; }
    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isPushable() { return true; }
    @Override public net.minecraft.world.InteractionResult interact(Player player, net.minecraft.world.InteractionHand hand) {
        if (!level().isClientSide() && getPassengers().isEmpty()) player.startRiding(this);
        return net.minecraft.world.InteractionResult.SUCCESS;
    }
    @Override protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) { moveFunction.accept(passenger, getX(), getY() + 0.55, getZ()); }
    @Override public double getPassengersRidingOffset() { return 0.45; }
    @Override public boolean isEffectiveAi() { return false; }
    @Override public void remove(RemovalReason reason) { super.remove(reason); }
    @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling() { return super.getBoundingBoxForCulling().inflate(1.0); }
    @Override public net.minecraft.world.entity.EntityDimensions getDimensions(Pose pose) { return net.minecraft.world.entity.EntityDimensions.scalable(1.8f, 1.1f); }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 4096; }
}
