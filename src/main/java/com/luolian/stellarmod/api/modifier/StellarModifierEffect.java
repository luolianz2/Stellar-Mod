package com.luolian.stellarmod.api.modifier;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

//效果接口
public interface StellarModifierEffect {
    String getId();

    //获取显示名称（含颜色格式）
    Component getDisplayName();

    //获取效果描述（用于Ctrl显示）
    List<Component> getDescription();

    //获取作者吐槽（用于Ctrl显示）
    Component getAuthorNote();

    //default提供可选实现
    //接口中的方法通常需要所有实现类强制重写
    //加了 default 后，方法就有了一个默认的空实现。实现类可以选择是否重写它：如果不重写，就使用这个默认的空逻辑；如果重写，就执行自己的代码
    //挖掘方块后触发
    default void onBlockMined(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {}

    //攻击实体后触发
    default void onEntityHurt(ItemStack stack, LivingEntity target, LivingEntity attacker) {}

    //自定义配置解析
    default void parseConfig(JsonObject config) {}
}