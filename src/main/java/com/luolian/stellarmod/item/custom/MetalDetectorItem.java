//探矿杖
package com.luolian.stellarmod.item.custom;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MetalDetectorItem extends Item {
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

            for(int i = 0; i <= positionClicked.getY() + 64; i++) {
                BlockState state = p_41427_.getLevel().getBlockState(positionClicked.below(i));
                if(isValuableBlock(state)) {
                    outputValuableCoordinates(positionClicked.below(i), player, state.getBlock());
                    foundBlock = true;

                    break;
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

    private void outputValuableCoordinates(BlockPos blockPos, Player player, Block block) {      //找到对应方块时发送信息至聊天栏
        player.sendSystemMessage(Component.literal("已发现" + I18n.get(block.getDescriptionId()) + "，其位于" +
                "(" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + ")"));
    }

    private boolean isValuableBlock(BlockState state) {     //对应方块判断
        return state.is(Blocks.IRON_ORE) || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DIAMOND_ORE);
    }
}
