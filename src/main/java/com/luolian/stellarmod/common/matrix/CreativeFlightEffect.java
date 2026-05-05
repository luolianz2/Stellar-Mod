package com.luolian.stellarmod.common.matrix;

import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创造飞行矩阵效果。允许玩家像创造模式一样自由飞行，并根据等级提高飞行速度。
 * 最高等级：10级。
 * <p>
 * 飞行加速档位（独立于基础速度的额外倍率）：
 * <ul>
 *   <li>0 档 — 无加速（默认）</li>
 *   <li>1 档 — 1.5x 倍率，需矩阵 ≥2 级解锁</li>
 *   <li>2 档 — 2.5x 倍率，需矩阵 ≥5 级解锁</li>
 *   <li>3 档 — 4.0x 倍率，需矩阵 ≥10 级解锁</li>
 * </ul>
 * 加速档位通过快捷键切换，不影响基础飞行速度，仅在启用时额外加成。
 */
public class CreativeFlightEffect implements StellarMatrixEffect {

    /** 矩阵效果唯一标识符，与 StellarItems 注册的 effectId 保持一致 */
    public static final String ID = "stellarmod:creative_flight";

    /** 加速档位对应的速度倍率，索引即档位 */
    private static final float[] BOOST_MULTIPLIERS = {1.0f, 1.5f, 2.5f, 4.0f};

    /** 各档位对应的 ActionBar 消息翻译键，索引即档位 */
    private static final String[] BOOST_TIER_TRANSLATION_KEYS = {
            "message.stellarmod.flight_boost.off",
            "message.stellarmod.flight_boost.on",
            "message.stellarmod.flight_boost.tier2",
            "message.stellarmod.flight_boost.max"
    };

    /**
     * 玩家飞行加速档位状态映射。
     * Key: 玩家 UUID，Value: 当前加速档位 (0~3)
     * 使用 ConcurrentHashMap 确保跨线程安全（客户端 Tick 与网络包回调可能并发）。
     */
    /*
        ConcurrentHashMap 是 Java 并发包（java.util.concurrent）中提供的一个线程安全的哈希表实现。它专为高并发场景设计，
            在多线程环境下无需外部同步即可安全地执行读写操作，同时比传统的 Hashtable 或 Collections.synchronizedMap 拥有更好的性能
        玩家数据可能在客户端 Tick 线程和网络包回调线程中并发访问。如果使用普通的 HashMap，多线程同时修改会导致数据错乱、CPU 100% 甚至 ConcurrentModificationException；
            如果使用 Hashtable，虽然线程安全，但所有方法都用 synchronized 锁住整个表，性能极差。
            ConcurrentHashMap 正好解决了这个问题——它允许多个线程并发读，并且在写操作时只锁住局部（桶），而非整个表
    */
    private static final Map<UUID, Integer> PLAYER_BOOST_TIERS = new ConcurrentHashMap<>();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("matrix.stellarmod_item.tool_core.creative_flight.name")
                .withStyle(ChatFormatting.GOLD);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("matrix.stellarmod_item.tool_core.creative_flight.desc")
        );
    }

    @Override
    public Component getAuthorNote() {
        return Component.translatable("matrix.stellarmod_item.tool_core.creative_flight.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    //加速档位静态方法

    /**
     * 扫描玩家背包，根据已启用的最高飞行矩阵等级，返回可用的最大加速档位。
     * <ul>
     *   <li>矩阵 ≥10 级 → 3 档 (MAX)</li>
     *   <li>矩阵 ≥5 级  → 2 档</li>
     *   <li>矩阵 ≥2 级  → 1 档</li>
     *   <li>不足 2 级   → 0 档（无加速）</li>
     * </ul>
     *
     * @param player 目标玩家
     * @return 0~3 的最大可用档位
     */
    public static int getMaxBoostTier(Player player) {
        int maxMatrixLevel = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ToolCoreItem) {
                if (ToolCoreItem.isMatrixEnabled(stack, ID)) {
                    int level = ToolCoreItem.getMatrixActiveLevel(stack, ID);
                    if (level > maxMatrixLevel) maxMatrixLevel = level;
                }
            }
        }
        if (maxMatrixLevel >= 10) return 3;
        if (maxMatrixLevel >= 5) return 2;
        if (maxMatrixLevel >= 2) return 1;
        return 0;
    }

    /**
     * 获取玩家当前的加速档位。
     *
     * @param player 目标玩家
     * @return 0~3 的当前档位，默认 0
     */
    public static int getBoostTier(Player player) {
        return PLAYER_BOOST_TIERS.getOrDefault(player.getUUID(), 0);
    }

    /**
     * 设置玩家的加速档位（会自动钳制到有效范围 0~3）。
     *
     * @param player 目标玩家
     * @param tier   目标档位
     */
    public static void setBoostTier(Player player, int tier) {
        PLAYER_BOOST_TIERS.put(player.getUUID(), Math.max(0, Math.min(3, tier)));
    }

/**
     * 获取指定档位的 ActionBar 消息翻译键。
     *
     * @param tier 档位 (0~3)
     * @return 对应的翻译键
     */
    public static String getBoostTierTranslationKey(int tier) {
        return BOOST_TIER_TRANSLATION_KEYS[Math.max(0, Math.min(3, tier))];
    }

    //Tick 逻辑

    @Override
    public void onPlayerTick(Player player, int activeLevel) {
        if (activeLevel <= 0) return;

        //允许飞行但不强制飞行状态，由玩家双击空格自行控制起飞/落地
        player.getAbilities().mayfly = true;

        //基础飞行速度（由矩阵等级决定）
        float baseSpeed = 0.05f + activeLevel * 0.01f;

        /*
         * 飞行加速档位加成。
         * 取当前档位与可用上限的较小值，防止矩阵降级后档位溢出
         * （例如玩家将矩阵从 10 级降到 1 级，档位自动从 3 降为 0）。
         */
        int maxTier = getMaxBoostTier(player);
        int currentTier = getBoostTier(player);
        if (currentTier > maxTier) {
            currentTier = maxTier;
            setBoostTier(player, currentTier);
        }
        float finalSpeed = baseSpeed * BOOST_MULTIPLIERS[currentTier];

        player.getAbilities().setFlyingSpeed(finalSpeed);

        //此处不调用 onUpdateAbilities()，因为：
        //mayfly 只是权限标记，MC 自身有定时同步机制，无需每 tick 发包
        //不再强制 flying = true，没有需要立即同步的状态变化
        //若每 tick 调用会导致每秒 20 个不必要的网络包，浪费带宽
    }
}
