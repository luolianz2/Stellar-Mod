package com.luolian.stellarmod.common.modifier;

import com.luolian.stellarmod.api.toolcore.StellarModifierEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ElectromagneticEffect implements StellarModifierEffect {

    //吸取半径由等级决定：基础半径 2，每级 +1，即等级1的半径为3，等级2的半径为4...
    private static int getRadius(int level) {
        return 2 + level;
    }

    @Override
    public String getId() {
        return "stellarmod:electromagnetic";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("modifier.stellarmod_item.tool_core.electromagnetic.name")
                .withStyle(ChatFormatting.AQUA);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("modifier.stellarmod_item.tool_core.electromagnetic.desc")
        );
    }

    @Override
    public Component getAuthorNote() {
        //ChatFormatting.GRAY：设置文本颜色为灰色。
        //ChatFormatting.ITALIC：设置文本为斜体。
        return Component.translatable("modifier.stellarmod_item.tool_core.electromagnetic.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    //带等级的挖掘触发
    @Override
    public void onBlockMined(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner, int modifierLevel) {
        if (!level.isClientSide && miner instanceof Player player) {
            int radius = getRadius(modifierLevel);
            AABB area = new AABB(pos).inflate(radius);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
            for (ItemEntity item : items) {
                item.setPos(player.getX(), player.getY(), player.getZ());
                //设置该物品可被玩家拾取前的延迟时间
                item.setPickUpDelay(0);
            }
        }
    }

    //带等级的攻击触发
    @Override
    public void onEntityHurt(ItemStack stack, LivingEntity target, LivingEntity attacker, int modifierLevel) {
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            int radius = getRadius(modifierLevel);
            AABB area = new AABB(target.blockPosition()).inflate(radius);
            List<ItemEntity> items = attacker.level().getEntitiesOfClass(ItemEntity.class, area);
            for (ItemEntity item : items) {
                item.setPos(player.getX(), player.getY(), player.getZ());
                item.setPickUpDelay(0);
            }
        }
    }
}