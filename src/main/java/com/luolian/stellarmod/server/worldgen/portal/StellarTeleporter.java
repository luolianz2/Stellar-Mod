package com.luolian.stellarmod.server.worldgen.portal;

import com.luolian.stellarmod.server.block.StellarBlocks;
import com.luolian.stellarmod.server.block.custom.DimensionBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class StellarTeleporter implements ITeleporter {
    private final BlockPos targetPos;
    private final boolean insideDimension;

    /**
     * 构造函数
     * @param pos 目标维度中期望的传送点坐标（通常是传送门对应的位置）
     * @param insideDim true 表示传送到维度内部，false 表示从维度返回主世界
     */
    public StellarTeleporter(BlockPos pos, boolean insideDim) {
        this.targetPos = pos;
        this.insideDimension = insideDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);

        BlockPos finalPos = null;
        boolean foundExistingPortal = false;

        // 如果是传送到维度内部，先尝试查找已有的传送方块
        if (insideDimension) {
            BlockPos existingPortalPos = findExistingPortal(destinationWorld, targetPos);
            if (existingPortalPos != null) {
                finalPos = existingPortalPos.above(); // 站在方块上方
                foundExistingPortal = true;
            }
        }

        // 如果没有找到现有传送方块，执行安全位置搜索
        if (finalPos == null) {
            int baseY = insideDimension ? targetPos.getY() : 61;
            BlockPos startPos = new BlockPos(targetPos.getX(), baseY, targetPos.getZ());
            finalPos = findSafePos(destinationWorld, startPos);
        }

        // 设置实体位置
        entity.setPos(finalPos.getX(), finalPos.getY(), finalPos.getZ());

        // 如果是传送到维度内部且没有找到现有传送方块，则在脚下放置一个新的传送方块
        if (insideDimension && !foundExistingPortal) {
            placePortalIfNeeded(destinationWorld, finalPos);
        }

        return entity;
    }

    /**
     * 在目标维度中查找附近的 DimensionBlock
     * @param level 目标维度
     * @param center 搜索中心
     * @return 找到的第一个 DimensionBlock 的位置，如果没有则返回 null
     */
    @Nullable
    private BlockPos findExistingPortal(ServerLevel level, BlockPos center) {
        // 搜索范围：21×5×21 的立方体（可根据需要调整）
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-10, -2, -10),
                center.offset(10, 2, 10))) {
            if (level.getBlockState(pos).getBlock() instanceof DimensionBlock) {
                return pos.immutable();
            }
        }
        return null;
    }

    /**
     * 寻找一个安全的站立位置
     * @param level 目标维度
     * @param startPos 起始搜索位置
     * @return 安全的位置
     */
    private BlockPos findSafePos(ServerLevel level, BlockPos startPos) {
        BlockPos.MutableBlockPos mutable = startPos.mutable();

        // 优先检查起始位置是否安全
        if (isSafeStandingPos(level, mutable)) {
            return mutable.immutable();
        }

        // 向上搜索 50 格
        for (int i = 1; i <= 50; i++) {
            mutable.move(0, 1, 0);
            if (isSafeStandingPos(level, mutable)) {
                return mutable.immutable();
            }
        }

        // 向下搜索 50 格
        mutable.set(startPos);
        for (int i = 1; i <= 50; i++) {
            mutable.move(0, -1, 0);
            if (isSafeStandingPos(level, mutable)) {
                return mutable.immutable();
            }
        }

        // 实在找不到，返回起始位置
        return startPos;
    }

    /**
     * 判断一个位置是否适合实体站立（脚下有支撑，头顶有空间）
     */
    private boolean isSafeStandingPos(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockPos above = pos.above();

        // 脚下方块必须是固体（不能是空气、流体、非固体）
        BlockState belowState = level.getBlockState(below);
        if (belowState.isAir() || belowState.canBeReplaced(Fluids.WATER) || !belowState.isSolid()) {
            return false;
        }

        // 站立点和头顶必须是空气或可替换方块
        BlockState standingState = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(above);
        return (standingState.isAir() || standingState.canBeReplaced(Fluids.WATER)) &&
                (aboveState.isAir() || aboveState.canBeReplaced(Fluids.WATER));
    }

    /**
     * 在安全位置下方放置一个传送方块（如果附近没有重复）
     */
    private void placePortalIfNeeded(ServerLevel level, BlockPos safePos) {
        // 放置前再次检查附近是否已有传送方块（双重确认）
        boolean exists = BlockPos.betweenClosedStream(
                safePos.offset(-10, -2, -10),
                safePos.offset(10, 2, 10)
        ).anyMatch(p -> level.getBlockState(p).getBlock() instanceof DimensionBlock);

        if (!exists) {
            BlockPos placePos = safePos.below();
            level.setBlock(placePos, StellarBlocks.DIMENSION_BLOCK.get().defaultBlockState(), 3);
        }
    }
}