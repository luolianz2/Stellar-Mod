package com.luolian.stellarmod.event;

import com.luolian.stellarmod.item.custom.MultiToolItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MultiToolDamageHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof MultiToolItem) {
                IEnergyStorage energy = mainHand.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energy == null || energy.getEnergyStored() < MultiToolItem.ENERGY_PER_ATTACK) {
                    event.setCanceled(true);
                    return;
                }
                // 这里可以根据需要设置固定伤害，或者根据当前手持物品动态决定
                // 因为我们移除了模式，可以简单地给一个固定伤害，比如3点（剑伤害）
                event.setAmount(MultiToolItem.SWORD_DAMAGE);
            }
        }
    }
}