package com.luolian.stellarmod.item.custom;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiToolCapabilityProvider implements ICapabilityProvider, IEnergyStorage {

    private final ItemStack stack;
    private int energy;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;
    private final LazyOptional<IEnergyStorage> energyCapability;

    public MultiToolCapabilityProvider(ItemStack stack) {
        this.stack = stack;
        // 从 MultiToolItem 中获取能量参数常量
        this.capacity = MultiToolItem.CAPACITY;
        this.maxReceive = MultiToolItem.MAX_RECEIVE;
        this.maxExtract = MultiToolItem.MAX_EXTRACT;
        this.energyCapability = LazyOptional.of(() -> this);
        // 从 NBT 加载能量值
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Energy")) {
            this.energy = tag.getInt("Energy");
        } else {
            this.energy = 0;
        }
    }

    // 保存能量到物品 NBT
    private void saveEnergy() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Energy", this.energy);
    }

    // ========== IEnergyStorage 方法 ==========
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(this.capacity - this.energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate && received > 0) {
            this.energy += received;
            saveEnergy();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(this.energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate && extracted > 0) {
            this.energy -= extracted;
            saveEnergy();
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return true;  // 允许其他设备抽取能量
    }

    @Override
    public boolean canReceive() {
        return true;  // 允许接收能量（充电）
    }

    // ========== ICapabilityProvider 方法 ==========
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        return LazyOptional.empty();
    }

    // 可选：当物品被丢弃或能力失效时调用，避免内存泄漏
    public void invalidate() {
        energyCapability.invalidate();
    }
}