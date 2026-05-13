package com.luolian.stellarmod.client.config;

import java.util.List;

/**
 * 工具核心轮盘运行时状态管理。
 * 仅维护当前活跃的轮盘索引（不持久化到文件），持久化配置由 {@link RadialConfigStorage} 负责。
 */
public class ToolCoreRadialState {

    /** 当前活跃的轮盘索引 (运行时状态, 不持久化) */
    private static int currentRadialIndex = RadialConfigStorage.ROOT_INDEX;

    /**
     * 获取当前活跃的轮盘索引。
     */
    public static int getCurrentRadialIndex() {
        return currentRadialIndex;
    }

    /**
     * 设置当前活跃的轮盘索引。
     */
    public static void setCurrentRadialIndex(int index) {
        if (index >= RadialConfigStorage.ROOT_INDEX && index <= RadialConfigStorage.MAX_RADIAL_INDEX) {
            currentRadialIndex = index;
        }
    }

    /**
     * 将当前索引切换到下一个已存在的轮盘（循环）。
     * 仅遍历有内容的轮盘，不会自动创建空轮盘（这个是控制玩家默认按R键打开的页面的）。
     */
    public static void switchToNextRadial() {
        List<Integer> indices = RadialConfigStorage.getRadialIndices();
        if (indices.isEmpty()) {
            currentRadialIndex = RadialConfigStorage.ROOT_INDEX;
            return;
        }
        int pos = indices.indexOf(currentRadialIndex);
        if (pos < 0 || pos >= indices.size() - 1) {
            //当前是最后一个或未找到，回到第一个
            currentRadialIndex = indices.get(0);
        } else {
            currentRadialIndex = indices.get(pos + 1);
        }
    }
}
