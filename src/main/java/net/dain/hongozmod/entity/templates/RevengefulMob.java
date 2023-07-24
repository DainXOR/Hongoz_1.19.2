package net.dain.hongozmod.entity.templates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.UUID;

/*
@ApiStatus.Experimental
public interface RevengefulMob extends NeutralMob {
    int MAX_TARGETS = 5;
    int FORGET_ENEMY_TIME = 300; // ticks

    String TAG_TARGETS = "AngryAt";
    String TAG_ANGER_TIME = "AngerTime";
    String TAG_ANGRY_AT = "AngryAt";

    <T extends LivingEntity> List<Class<T>> getAngryList();
    <T extends LivingEntity> List<Class<T>> setAngryList(Class<T>... targetTypes);

    int getRemainingPersistentAngerTime();
    void setRemainingPersistentAngerTime(int pTime);

    @Nullable
    UUID getPersistentAngerTarget();
    void setPersistentAngerTarget(@Nullable UUID pTarget);
    void startPersistentAngerTimer();

    default boolean getRageStatus(){
        return getAngryList().size() == MAX_TARGETS;
    }
    boolean setRageStatus(boolean rageStatus);

    private <T extends LivingEntity> byte[] getByteAngryList() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getAngryList());
        return bos.toByteArray();
    }

    default <T extends LivingEntity> boolean addTarget(Class<T> newTarget){
        boolean forgotTarget = false;
        if(getAngryList().size() >= MAX_TARGETS - 1) {
            getAngryList().remove(0);
            forgotTarget = true;
        }

        if(!getAngryList().contains(newTarget)) {
            getAngryList().add((Class<LivingEntity>) newTarget);
        }

        return forgotTarget;
    }
    default <T extends LivingEntity> boolean removeTarget(Class<T> target){
        return getAngryList().remove(target);
    }

    default <T extends LivingEntity> Class<T> getFirstTarget(){ return (Class<T>) getAngryList().get(0); }
    default <T extends LivingEntity> Class<T> getLastTarget(){ return (Class<T>) getAngryList().get(MAX_TARGETS - 1);}




    default void addTargetsSaveData(CompoundTag pNbt) {
        try {
            pNbt.putByteArray(TAG_TARGETS, this.getByteAngryList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void addPersistentAngerSaveData(CompoundTag pNbt) {
        pNbt.putInt(TAG_ANGER_TIME, this.getRemainingPersistentAngerTime());
        if (this.getPersistentAngerTarget() != null) {
            pNbt.putUUID(TAG_ANGRY_AT, this.getPersistentAngerTarget());
        }

    }

    default void readPersistentAngerSaveData(Level pLevel, CompoundTag pTag) {
        this.setRemainingPersistentAngerTime(pTag.getInt(TAG_ANGER_TIME));
        if (pLevel instanceof ServerLevel) {
            if (!pTag.hasUUID(TAG_ANGRY_AT)) {
                this.setPersistentAngerTarget((UUID)null);
            } else {
                UUID uuid = pTag.getUUID(TAG_ANGRY_AT);
                this.setPersistentAngerTarget(uuid);
                Entity entity = ((ServerLevel)pLevel).getEntity(uuid);
                if (entity != null) {
                    if (entity instanceof Mob) {
                        this.setLastHurtByMob((Mob)entity);
                    }

                    if (entity.getType() == EntityType.PLAYER) {
                        this.setLastHurtByPlayer((Player)entity);
                    }

                }
            }
        }
    }

    @Override
    default boolean canAttack(LivingEntity pEntity){
        return this.getRageStatus() || getAngryList().contains(pEntity.getClass());
    }

}
*/