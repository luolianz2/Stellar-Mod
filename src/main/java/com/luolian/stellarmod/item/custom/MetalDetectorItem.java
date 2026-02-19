//探矿杖
package com.luolian.stellarmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class MetalDetectorItem extends Item {
    private static final int DETECTION_RADIUS = 32;
    int searchDepth = 64; //向下搜索64格

    public MetalDetectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        if(!p_41427_.getLevel().isClientSide()) {
            BlockPos positionClicked = p_41427_.getClickedPos();
            Player player = p_41427_.getPlayer();

            if (player == null) {
                return InteractionResult.FAIL; //玩家为空返回交互失败
            }

            boolean foundBlock = false;

            searchLoop:
            for (int dx = -DETECTION_RADIUS; dx <= DETECTION_RADIUS; dx++) {
                for (int dz = -DETECTION_RADIUS; dz <= DETECTION_RADIUS; dz++) {
                    for (int dy = 0; dy <= searchDepth; dy++) {
                        BlockState state = p_41427_.getLevel().getBlockState(positionClicked.offset(dx, -dy, dz));
                        if(isValuableBlock(state)) {
                            outputValuableCoordinates(positionClicked.offset(dx, -dy, dz), player, state.getBlock());
                            foundBlock = true;
                            break searchLoop;
                        }
                        if(state.is(Blocks.VOID_AIR)){
                            break;
                        }
                    }
                }
            }

            if(!foundBlock) {
                player.sendSystemMessage(Component.literal("未找到相应方块！"));
            }
        }
        p_41427_.getItemInHand().hurtAndBreak(1, p_41427_.getPlayer(),  //耐久损耗
                player1 -> player1.broadcastBreakEvent(player1.getUsedItemHand()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.stellarmod_item.metal_detector"));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    private void outputValuableCoordinates(BlockPos blockPos, Player player, Block block) {      //找到对应方块时发送信息至聊天栏
        Component message = Component.translatable("item.stellarmod.metal_detected.found",
                 blockPos.getX(), blockPos.getY(), blockPos.getZ(),block.getName());
        player.sendSystemMessage(message);
    }

    private boolean isValuableBlock(BlockState state) {     //对应方块判断
        return state.is(Blocks.IRON_ORE) || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DIAMOND_ORE);
    }

}