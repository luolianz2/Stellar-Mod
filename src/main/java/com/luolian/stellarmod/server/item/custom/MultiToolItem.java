package com.luolian.stellarmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import java.util.List;

public class MultiToolItem extends Item {

    //能量参数
    public static final int CAPACITY = 20000;       //总能量
    public static final int MAX_RECEIVE = 1000;     //每tick最多充1000FE
    public static final int MAX_EXTRACT = 1000;     //每tick最多放1000FE
    public static final int ENERGY_PER_MINE = 100;  //采掘方块消耗电量
    public static final int ENERGY_PER_ATTACK = 50; //攻击实体消耗电量

    //挖掘速度（根据模式动态返回）
    private static final float DEFAULT_SPEED = 1.0F;    //默认速度
    private static final float PICKAXE_SPEED = 6.0F;    //稿子挖掘速度
    private static final float SHOVEL_SPEED = 4.5F;     //铲子挖掘速度
    private static final float AXE_SPEED = 6.0F;        //斧子挖掘速度

    //攻击伤害（通过事件实现）
    public static final int SWORD_DAMAGE = 10;

    //工具等级（钻石级）
    private static final int TOOL_LEVEL = 3;

    public MultiToolItem(Properties properties) {
        super(properties);
    }

    //挖掘速度
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (energy == null || energy.getEnergyStored() < ENERGY_PER_MINE) {
            return DEFAULT_SPEED;
        }
        //根据方块类型返回对应速度
        if (isPickaxeMineable(state)) {
            return PICKAXE_SPEED;
        } else if (isShovelMineable(state)) {
            return SHOVEL_SPEED;
        } else if (isAxeMineable(state)) {
            return AXE_SPEED;
        }
        return DEFAULT_SPEED;
    }

    //正确工具判断（影响掉落）
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (energy == null || energy.getEnergyStored() < ENERGY_PER_MINE) {
            return false;
        }

        //检查工具类型
        boolean isCorrectType = isPickaxeMineable(state) || isShovelMineable(state) || isAxeMineable(state);
        if (!isCorrectType) {
            return false;
        }

        // 如果是对应镐的方块，还需要检查等级
        if (isPickaxeMineable(state)) {
            return hasRequiredMiningLevel(state);
        }
        return true;
    }

    //判断工具的等级是否足够挖掘当前方块
    private boolean hasRequiredMiningLevel(BlockState state) {
        //获取方块需要的等级（通过标签）
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return TOOL_LEVEL >= 3;  //钻石级或更高
        } else if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return TOOL_LEVEL >= 2;  //铁级或更高
        } else if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return TOOL_LEVEL >= 1;  //石级或更高
        }
        return true; // 不需要特定等级的方块（如泥土、木头）永远可以挖掘
    }

    //挖掘方块消耗能量
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!level.isClientSide && entityLiving instanceof Player) {
            IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (energy != null && energy.getEnergyStored() >= ENERGY_PER_MINE) {
                energy.extractEnergy(ENERGY_PER_MINE, false);
                return true;
            } else {
                return false; //能量不足无法挖掘
            }
        }
        return true;
    }

    //攻击实体消耗能量
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (energy != null && energy.getEnergyStored() >= ENERGY_PER_ATTACK) {
                energy.extractEnergy(ENERGY_PER_ATTACK, false);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    //工具电力提示
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            tooltip.add(Component.literal(energy.getEnergyStored() + " / " + energy.getMaxEnergyStored() + " FE"));
        });
    }

    //能量Capability提供
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new MultiToolCapabilityProvider(stack);
    }

    //辅助判断方法
    private boolean isPickaxeMineable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
                state.is(BlockTags.NEEDS_STONE_TOOL) ||
                state.is(BlockTags.NEEDS_IRON_TOOL) ||
                state.is(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    private boolean isShovelMineable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    private boolean isAxeMineable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_AXE);
    }
}