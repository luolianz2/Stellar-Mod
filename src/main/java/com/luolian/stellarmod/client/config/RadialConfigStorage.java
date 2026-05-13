package com.luolian.stellarmod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.luolian.stellarmod.StellarMod;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 客户端轮盘槽位配置持久化管理。
 * 存储路径: .minecraft/config/stellarmod/radial-slots.json
 * <p>
 * 数据结构: Map&lt;Integer, Map&lt;Integer, String&gt;&gt;
 * - 外层 key: 轮盘索引 (1~10)
 * - 内层 key: 槽位索引 (0~7)
 * - value: 功能 ID (如 "pickaxe", "modifiers", "teleport" 等)
 * <p>
 * 默认仅索引 1 存在，索引 2~10 按需由玩家通过 + 按钮动态创建。
 * 保存时自动清理空轮盘（索引 > 1 且无任何已配置槽位）。
 */
public class RadialConfigStorage {

    //创建一个日志记录器，LogUtils.getLogger()自动获取“当前类”的 Logger
    private static final Logger LOGGER = LogUtils.getLogger();

    /*
        Gson 是 Google 提供的一个库，能把 Java 对象和 JSON 字符串互相转换。
        new GsonBuilder() 先配置再创建 Gson 实例。
        .setPrettyPrinting() 开启“美化输出”，这样存到文件里的 JSON 是缩进好的，方便人直接打开看，而不是挤成一行。
        .create() 最终生成一个配置好的 Gson 对象
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //FMLPaths.CONFIGDIR.get()：获取游戏配置目录的路径，.resolve(...)：拼接路径，类似“进入某个子文件夹”
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get()
            .resolve(StellarMod.MOD_ID)
            .resolve("radial-slots.json");

    /** 最大槽位数 */
    public static final int SLOT_COUNT = 8;
    /** 最大轮盘索引 */
    public static final int MAX_RADIAL_INDEX = 10;
    /** 永远存在的根轮盘索引 */
    public static final int ROOT_INDEX = 1;

    /** 已加载的轮盘配置: 索引 → (槽位 → 功能ID) */
    //Map是键值对的集合
    //HashMap —— 遍历时顺序不可预测，取决于哈希算法和容量，不是插入顺序
    //LinkedHashMap —— 内部额外维护了一条双向链表，迭代时的顺序就是元素加入的顺序
    private static Map<Integer, Map<Integer, String>> radials = new LinkedHashMap<>();

    private static boolean loaded = false;

    /**
     * 获取索引 1 的默认槽位配置。
     * 0~4: 工具形态, 5: 副词条, 6: 矩阵, 7: 设置
     */
    private static Map<Integer, String> createDefaultRadial() {
        Map<Integer, String> defaults = new LinkedHashMap<>();
        defaults.put(0, "pickaxe");
        defaults.put(1, "axe");
        defaults.put(2, "shovel");
        defaults.put(3, "sword");
        defaults.put(4, "hoe");
        defaults.put(5, "modifiers");
        defaults.put(6, "matrix");
        defaults.put(7, "settings");
        return defaults;
    }

    /**
     * 确保配置已加载。首次调用时从文件读取，文件不存在则创建默认。
     */
    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        try {
            //取配置文件所在目录，目录不存在则创建（连带所有不存在父目录）
            Files.createDirectories(CONFIG_FILE.getParent());

            //exists检查给定路径对应的文件或目录是否真实存在
            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);

                //使用之前创建的 GSON 实例（带格式化）把 JSON 字符串解析成 Java 对象
                //通过匿名子类保留泛型（让类、接口或方法可以操作“某种不特定的类型”，等到真正使用时才指定具体类型），Gson才知道要把键解析为Integer，值解析为 Map<Integer, String>
                //解析结果赋值给 parsed，如果 JSON 结构不匹配，parsed 可能为 null 或抛出异常
                Map<Integer, Map<Integer, String>> parsed = GSON.fromJson(
                        json, new TypeToken<Map<Integer, Map<Integer, String>>>() {}.getType()
                );

                if (parsed != null) {

                    //如果解析成功（非 null），就把解析出来的内容复制到一个新的 LinkedHashMap 里，赋值给 radials。
                    //为什么要复制一份？
                    //Gson 解析出的 Map 内部可能是 HashMap（无序），而我们希望后续遍历时保持“插入顺序”，因此用 LinkedHashMap 包装。
                    //另外也确保 radials 始终是同一个具体类型，后续清理、重新编号等方法依赖它是有序的。
                    radials = new LinkedHashMap<>(parsed);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load radial config, using defaults", e);
        }

        //确保索引 1 始终存在且包含完整默认配置
        if (!radials.containsKey(ROOT_INDEX) || radials.get(ROOT_INDEX).isEmpty()) {
            radials.put(ROOT_INDEX, createDefaultRadial());
        }
    }

    /**
     * 将当前配置写入文件。
     */
    public static void save() {
        ensureLoaded();
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            //保存前清理空轮盘
            cleanupEmptyRadials();
            String json = GSON.toJson(radials);
            Files.writeString(CONFIG_FILE, json, StandardCharsets.UTF_8);
            //只有文件写入可能出错，所以精确捕获 IOException 就够了
        } catch (IOException e) {
            LOGGER.error("Failed to save radial config", e);
        }
    }

    /**
     * 清理空轮盘: 索引 > 1 且所有槽位均为 null 的轮盘会被移除，后续索引前移。
     */
    private static void cleanupEmptyRadials() {
        /*
                List 是一个定义了列表行为规范的接口，本身不干活，只是规定要实现哪些方法；ArrayList 是一个类是 List 接口的一种具体实现，底层用动态数组来存储数据，创建一个列表对象
            List 是“标准”，制定规则，ArrayList 是“实现”，靠动态数组完成这些规则
                Java 的集合类（List、Map、Set 等）只能装对象，不能装基本数据类型。int 是基本数据类型，它不是对象，无法放进 ArrayList 或作为 Map 的键。
            Integer 是 int 的对象版本，是一个类，所以可以放进集合里
         */
        List<Integer> sortedIndices = new ArrayList<>(radials.keySet());

        /*
            .sort(...) 是 List 的方法，需要一个 Comparator（比较器） 来告诉它怎样比较两个元素的大小，才能排序，Integer::compareTo 就是那个比较器。它的完整写法是：
                sortedIndices.sort((a, b) -> a.compareTo(b));
                也就是：对于列表中的两个整数 a 和 b，直接调用 a.compareTo(b) 来判断谁大谁小。Integer 已经自带了这个比较方法，所以可以用方法引用简写。
                List.sort(Comparator) 默认使用的是 TimSort（一种源自归并排序和插入排序的混合稳定排序算法）
                排序结果：升序（从小到大）。这样后面的代码就能按顺序处理轮盘，比如压缩索引时从 1 开始依次重新编号，不会乱。
                需要排序的原因是在读取配置文件以及其他可能下，数据存储顺序不一定是从小到大排列的
         */
        sortedIndices.sort(Integer::compareTo);

        //收集需要删除的索引
        Set<Integer> toRemove = new HashSet<>();
        for (int idx : sortedIndices) {
            if (idx <= ROOT_INDEX) continue;
            Map<Integer, String> slots = radials.get(idx);
            if (slots == null || slots.isEmpty()) {
                toRemove.add(idx);
            }
        }

        if (!toRemove.isEmpty()) {
            //构建新的有序映射
            Map<Integer, Map<Integer, String>> compacted = new LinkedHashMap<>();
            int newIndex = ROOT_INDEX;
            for (int oldIdx : sortedIndices) {
                if (toRemove.contains(oldIdx)) continue;
                if (oldIdx == ROOT_INDEX) {
                    compacted.put(ROOT_INDEX, radials.get(ROOT_INDEX));
                } else {
                    newIndex++;
                    compacted.put(newIndex, radials.get(oldIdx));
                }
            }
            radials = compacted;
        }
    }

    /**
     * 获取指定轮盘索引、指定槽位的功能 ID。
     * 若该轮盘或槽位未配置，返回 null（表示空槽位）。
     */
    public static String getSlotAction(int radialIndex, int slotIndex) {
        ensureLoaded();
        Map<Integer, String> slots = radials.get(radialIndex);
        if (slots == null) return null;
        return slots.get(slotIndex);
    }

    /**
     * 设置指定轮盘索引、指定槽位的功能 ID。
     */
    public static void setSlotAction(int radialIndex, int slotIndex, String actionId) {
        ensureLoaded();
        //.computeIfAbsent:如果 radials 中 已经存在 radialIndex 这个键 → 直接返回对应的内层 Map。
        //如果不存在 → 调用 k -> new LinkedHashMap<>() 创建一个新的空 LinkedHashMap，把它和 radialIndex 关联并放入 radials，然后返回这个新 Map
        //k 是 lambda 表达式的参数名，可以把它当成一个临时变量名
        //.put(slotIndex, actionId)拿到上一步返回的内层 Map 后，直接往里面放键值对：槽位索引 → 功能 ID
        radials.computeIfAbsent(radialIndex, k -> new LinkedHashMap<>())
                .put(slotIndex, actionId);
    }

    /**
     * 移除指定轮盘索引、指定槽位的功能（设为空槽位）。
     */
    public static void removeSlotAction(int radialIndex, int slotIndex) {
        ensureLoaded();
        Map<Integer, String> slots = radials.get(radialIndex);
        if (slots != null) {
            slots.remove(slotIndex);
        }
    }

    /**
     * 获取所有存在的轮盘索引列表（升序）。
     */
    public static List<Integer> getRadialIndices() {
        ensureLoaded();
        //.keySet()返回这个映射中所有键组成的一个 Set 集合
        List<Integer> indices = new ArrayList<>(radials.keySet());
        indices.sort(Integer::compareTo);
        return indices;
    }

    /**
     * 检查指定索引的轮盘是否存在。
     */
    public static boolean hasRadial(int index) {
        ensureLoaded();
        return radials.containsKey(index) && !radials.get(index).isEmpty();
    }

    /**
     * 获取轮盘总数。
     */
    public static int getRadialCount() {
        ensureLoaded();
        //使用 keySet 大小而非实际条目数，因为空轮盘在保存前可能存在
        //filter 是 Java Stream（流） 提供的一个中间操作方法。它不修改原始集合，而是生成一个新的流，里面只保留满足条件的元素
        return (int) radials.keySet().stream()
                .filter(idx -> radials.get(idx) != null && !radials.get(idx).isEmpty())
                .count();
    }

    /**
     * 添加一个新的空轮盘（在指定索引处创建）。
     * @return true 成功创建，false 索引已存在或超出范围
     */
    public static boolean addRadial(int index) {
        ensureLoaded();
        if (index < ROOT_INDEX || index > MAX_RADIAL_INDEX) return false;
        if (radials.containsKey(index) && !radials.get(index).isEmpty()) return false;
        radials.put(index, new LinkedHashMap<>());
        return true;
    }

    /**
     * 清空指定轮盘的所有槽位配置。
     * 索引 1 会被重置为默认配置，而非清空。
     */
    public static void clearRadial(int radialIndex) {
        ensureLoaded();
        if (radialIndex == ROOT_INDEX) {
            radials.put(ROOT_INDEX, createDefaultRadial());
        } else {
            Map<Integer, String> slots = radials.get(radialIndex);
            if (slots != null) {
                slots.clear();
            }
        }
    }

    /**
     * 重置所有配置为默认（仅保留索引 1 的默认配置）。
     */
    public static void resetToDefault() {
        radials.clear();
        radials.put(ROOT_INDEX, createDefaultRadial());
        save();
    }

    /**
     * 编译指定轮盘的所有功能 ID 集合（用于候选区展示）。
     */
    public static Set<String> getAssignedActions(int radialIndex) {
        ensureLoaded();
        Map<Integer, String> slots = radials.get(radialIndex);
        if (slots == null) return Collections.emptySet();
        return new HashSet<>(slots.values());
    }

    /**
     * 检查"设置"功能是否至少被分配到了某个轮盘的某个槽位。
     * 若未分配，则返回false，玩家无法从轮盘进入设置面板，配置将被锁定。
     */
    public static boolean hasSettingsAssigned() {
        ensureLoaded();
        for (Map<Integer, String> slots : radials.values()) {
            if (slots != null && slots.containsValue("settings")) {
                return true;
            }
        }
        return false;
    }
}
