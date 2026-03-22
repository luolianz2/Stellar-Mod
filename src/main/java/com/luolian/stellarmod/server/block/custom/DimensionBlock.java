package com.luolian.stellarmod.server.block.custom;

import com.luolian.stellarmod.worldgen.dimension.StellarDimensions;
import com.luolian.stellarmod.worldgen.portal.StellarTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DimensionBlock extends Block {
    public DimensionBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.canChangeDimensions()) {
            handleStellarPortal(player, pos);
            return InteractionResult.SUCCESS;
        } else  {
            return InteractionResult.CONSUME;
        }
    }

    private void handleStellarPortal(Entity player, BlockPos pos) {
        if (player.level() instanceof ServerLevel serverLevel) {
            MinecraftServer minecraftServer = serverLevel.getServer();
            ResourceKey<Level> resourceKey = player.level().dimension() == StellarDimensions.STELLAR_SPACE_LEVEL_KEY ?
                    Level.OVERWORLD : StellarDimensions.STELLAR_SPACE_LEVEL_KEY;

            ServerLevel portalDimension = minecraftServer.getLevel(resourceKey);
            if (portalDimension != null && !player.isPassenger()) {
                player.changeDimension(portalDimension, new StellarTeleporter(pos, true));
            } else {
                player.changeDimension(portalDimension, new StellarTeleporter(pos, false));
            }
        }
    }
}
