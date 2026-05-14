package com.luolian.stellarmod.server.item.custom.toolcore;

import com.luolian.stellarmod.server.data.toolcore.Material;
import com.luolian.stellarmod.server.data.toolcore.MaterialManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 工具核心 NBT 持久化助手。
 * 集中管理工具核心物品的所有 NBT 标签键常量和读写方法。
 * 所有方法均为静态方法，可从客户端和服务端安全调用。
 */
public class ToolCoreNBT {

    // ═══════════════════════════════════════════════════════════════
    // NBT 键常量
    // ═══════════════════════════════════════════════════════════════

    /** 材料列表存储键，一个列表，存放已装配材料的 ResourceLocation 字符串 */
    public static final String TAG_MATERIALS = "Materials";
    /** 当前激活的工具形态ID（如 "pickaxe"） */
    public static final String TAG_ACTIVE_TYPE = "ActiveType";
    /** 耐久损伤值 */
    public static final String TAG_DAMAGE = "Damage";
    /** 最大耐久值 */
    public static final String TAG_MAX_DAMAGE = "MaxDamage";
    /** 副词条累计总等级存储键 */
    public static final String TAG_MODIFIER_LEVELS = "ToolCoreModifierLevels";
    /** 副词条生效等级存储键（玩家调节后的当前生效等级） */
    public static final String TAG_MODIFIER_ACTIVE_LEVELS = "ToolCoreModifierActiveLevels";
    /** 副词条开关状态存储键 */
    public static final String TAG_MODIFIER_SETTINGS = "ToolCoreModifierSettings";
    /** 矩阵效果累计总等级存储键 */
    public static final String TAG_MATRIX_LEVELS = "ToolCoreMatrixLevels";
    /** 矩阵效果生效等级存储键（玩家调节后的当前等级） */
    public static final String TAG_MATRIX_ACTIVE_LEVELS = "ToolCoreMatrixActiveLevels";
    /** 矩阵效果开关状态存储键 */
    public static final String TAG_MATRIX_SETTINGS = "ToolCoreMatrixSettings";
    /** 标记该物品为合成预览产物 */
    public static final String TAG_PREVIEW = "Preview";
    /** 预览修复量（非首次材料添加时） */
    public static final String TAG_PREVIEW_REPAIR = "PreviewRepair";
    /** 本次合成应用的矩阵效果 ID */
    public static final String TAG_PREVIEW_MATRIX_ID = "PreviewMatrixId";
    /** 本次合成消耗的材料物品 ID */
    public static final String TAG_PREVIEW_MATERIAL_ID = "PreviewMaterialId";
    /** 本次合成是否为首次添加该材料 */
    public static final String TAG_PREVIEW_IS_NEW_MATERIAL = "PreviewIsNewMaterial";
    /** 本次合成新增的副词条 ID 列表 */
    public static final String TAG_PREVIEW_NEW_MODIFIERS = "PreviewNewModifiers";

    // ═══════════════════════════════════════════════════════════════
    // 属性汇总记录
    // ═══════════════════════════════════════════════════════════════

    /**
     * 存储从材料计算得到的累计属性。它不包含形态基础值，只包含材料提供的部分。
     */
    public record ToolProperties(
            int miningLevel,
            float miningSpeed,
            float attackDamage,
            int durability
    ) {
        public static final ToolProperties EMPTY = new ToolProperties(0, 0.0f, -3.0f, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    // 耐久读写
    // ═══════════════════════════════════════════════════════════════

    /**
     * 从物品堆叠中读取存储的最大耐久值
     */
    public static int getStoredMaxDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_MAX_DAMAGE) ? tag.getInt(TAG_MAX_DAMAGE) : 0;
    }

    /**
     * 将最大耐久值写入物品堆叠
     */
    public static void setStoredMaxDamage(ItemStack stack, int maxDamage) {
        stack.getOrCreateTag().putInt(TAG_MAX_DAMAGE, maxDamage);
    }

    /**
     * 从物品堆叠中读取当前耐久损伤值
     */
    public static int getStoredDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_DAMAGE) ? tag.getInt(TAG_DAMAGE) : 0;
    }

    /**
     * 设置当前耐久损伤值，自动限制不超过最大耐久
     */
    public static void setStoredDamage(ItemStack stack, int damage) {
        int max = getStoredMaxDamage(stack);
        stack.getOrCreateTag().putInt(TAG_DAMAGE, Math.min(damage, max));
    }

    // ═══════════════════════════════════════════════════════════════
    // 属性计算
    // ═══════════════════════════════════════════════════════════════

    /**
     * 获取工具当前所有材料的累计属性。
     * - 挖掘速度/攻击伤害的基础值 = 所有已添加材料中的最高属性值
     * - 多样性加成 = Σ(每种首次添加材料的挖掘等级 × 1%)
     * - 最终值 = 基础值 × (1 + 多样性加成)
     */
    public static ToolProperties getProperties(ItemStack stack) {
        //若工具已损坏（耐久为0），则所有材料加成失效
        int max = getStoredMaxDamage(stack);
        int damage = getStoredDamage(stack);
        if (max > 0 && damage >= max) {
            return ToolProperties.EMPTY;
        }

        List<Material> materials = getMaterialsFromStack(stack);
        int miningLevel = 0;
        float miningSpeed = 0;
        float attackDamage = 0;
        int durability = 0;

        Set<ResourceLocation> seen = new HashSet<>(); //用于追踪已处理的材料（按物品ID）
        Map<ResourceLocation, Integer> uniqueMaterialLevels = new HashMap<>(); //记录每种首次材料及其等级

        //遍历所有已添加的材料，仅统计每种材料第一次出现的属性
        for (Material mat : materials) {
            if (seen.add(mat.itemId())) {
                uniqueMaterialLevels.put(mat.itemId(), mat.miningLevel());
                //挖掘等级取所有材料的最高值
                miningLevel = Math.max(miningLevel, mat.miningLevel());
                //耐久累加
                durability += mat.durability();
                //记录最高速度和攻击
                miningSpeed = Math.max(miningSpeed, mat.miningSpeed());
                attackDamage = Math.max(attackDamage, mat.attackDamage());
            }
        }

        //多样性加成：每种首次材料的挖掘等级 × 1% 的总和
        float diversityBonus = 1.0f;
        for (int level : uniqueMaterialLevels.values()) {
            diversityBonus += level * 0.01f;
        }

        miningSpeed *= diversityBonus;
        attackDamage *= diversityBonus;

        // 材料联动示例：同时拥有铁和钻石时，额外提升20%速度和攻击
        boolean hasIron = materials.stream().anyMatch(m -> m.id().getPath().contains("iron"));
        boolean hasDiamond = materials.stream().anyMatch(m -> m.id().getPath().contains("diamond"));
        if (hasIron && hasDiamond) {
            miningSpeed *= 1.2f;
            attackDamage *= 1.2f;
        }

        //保留一位小数（四舍五入）
        miningSpeed = Math.round(miningSpeed * 10.0f) / 10.0f;
        attackDamage = Math.round(attackDamage * 10.0f) / 10.0f;

        return new ToolProperties(miningLevel, miningSpeed, attackDamage, durability);
    }

    /**
     * 获取工具最终挖掘等级（仅材料提供的部分）
     */
    public static int getTotalMiningLevel(ItemStack stack) {
        return getProperties(stack).miningLevel();
    }

    // ═══════════════════════════════════════════════════════════════
    // 材料列表 NBT 读写
    // ═══════════════════════════════════════════════════════════════

    /**
     * 从 NBT 读取材料列表，并调用 MaterialManager 获取实际 Material 对象。
     */
    public static List<Material> getMaterialsFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_MATERIALS, Tag.TAG_STRING);
        List<Material> materials = new ArrayList<>();
        for (Tag t : list) {
            if (t instanceof StringTag st) {
                ResourceLocation id = ResourceLocation.tryParse(st.getAsString());
                if (id != null) {
                    Material mat = MaterialManager.getMaterial(id);
                    if (mat != null) materials.add(mat);
                }
            }
        }
        return materials;
    }

    /**
     * 向工具核心添加材料：记录材料 ID、累加耐久、累加副词条等级。
     * 若该材料已存在，则只累加副词条等级，不重复记录材料 ID。
     */
    public static void addMaterialToStack(ItemStack stack, Material material) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_MATERIALS, Tag.TAG_STRING);

        //只有新材料才追加到材料列表，并增加最大耐久
        if (!hasMaterial(stack, material.itemId())) {
            list.add(StringTag.valueOf(material.itemId().toString()));
            tag.put(TAG_MATERIALS, list);

            //累加材料提供的耐久值到最大耐久
            int currentMax = getStoredMaxDamage(stack);
            int newMax = currentMax + material.durability();
            setStoredMaxDamage(stack, newMax);
            //确保当前损伤不超过新最大耐久
            int currentDamage = getStoredDamage(stack);
            setStoredDamage(stack, Math.min(currentDamage, newMax));
        }

        //处理副词条：累加等级（无论材料是否重复，等级都会累加）
        for (Material.StellarModifierEntry entry : material.modifiers()) {
            mergeModifierLevel(stack, entry.id(), entry.level());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 材料检测
    // ═══════════════════════════════════════════════════════════════

    /**
     * 检查某个材料是否已经添加过（通过物品 ID 判断，支持别名解析）。
     */
    public static boolean hasMaterial(ItemStack stack, ResourceLocation itemId) {
        Material material = MaterialManager.getMaterial(itemId);
        if (material == null) return false;
        List<Material> materials = getMaterialsFromStack(stack);

        //判断一个材料（Material）是否已经存在于一个材料集合中，判断依据是两者的 itemId（资源标识符）是否相等
        //.stream()将集合转为Stream(支持顺序和并行聚合操作的元素序列，可理解为一个高级迭代器，但它的目的不是存储数据，而是对数据源（集合、数组、I/O 资源等）进行函数式风格的计算
        //.anyMatch判断是否有任意元素满足条件
        //m.itemId()：从当前遍历到的材料对象 m 中获取它的 itemId
        //material.itemId()：从外部传入的 material 对象中获取它的 itemId（相同类型）。
        //m.itemId().equals(material.itemId())：调用前者的 equals 方法，将后者作为参数传入，判断两者是否逻辑相等（即内容相同）
        return materials.stream().anyMatch(m -> m.itemId().equals(material.itemId()));
    }

    /**
     * 获取材料添加次数（通过 NBT 列表中的出现次数，支持别名解析）。
     */
    public static int getMaterialCount(ItemStack stack, ResourceLocation itemId) {
        Material material = MaterialManager.getMaterial(itemId);
        if (material == null) return 0;
        List<Material> materials = getMaterialsFromStack(stack);
        //.filter筛选出满足指定条件的元素
        return (int) materials.stream().filter(m -> m.itemId().equals(material.itemId())).count();
    }

    // ═══════════════════════════════════════════════════════════════
    // 副词条 NBT 读写
    // ═══════════════════════════════════════════════════════════════

    /**
     * 收集工具当前所有已添加材料中的副词条条目（已去重，仅保留唯一 ID）。
     * 返回条目中的 level 已无实际用途（统一交由 getModifierLevel 管理），此处填 0 占位。
     */
    public static List<Material.StellarModifierEntry> getAllModifiers(ItemStack stack) {
        List<Material> materials = getMaterialsFromStack(stack);
        List<Material.StellarModifierEntry> entries = new ArrayList<>();
        //Set<String>	声明一个字符串类型的集合，不允许存储重复元素
        //new HashSet<>()	创建一个 HashSet 实例，基于哈希表实现，查找和插入的速度都非常快
        //创建一个只能存字符串、不允许重复、查询超快地集合，名字叫 seen
        //用 HashSet 作为一个容器来实现 seen 这个集合，举个例子：杯子 水杯 = new 陶瓷杯();
        Set<String> seen = new HashSet<>();
        for (Material mat : materials) {
            for (Material.StellarModifierEntry entry : mat.modifiers()) {
                if (seen.add(entry.id())) {
                    /*
                        level 已无实际用途，因等级由 getModifierLevel 统一管理，此处置0，
                     entries 需要收集的是当前工具核心所拥有的所有"副词条 ID"，而等级数据已经完全移交给 getModifierLevel 统一管理。

                        在 getAllModifiers 中，entry.level() 已经失去了意义，所以故意写成 0，目的是：
                            1.满足构造器语法
                            2.消除歧义，强迫后续代码必须用 getModifierLevel 来读取等级

                        getAllModifiers 的职责现在是"返回所有已装配的副词条 ID"，而不是"返回所有副词条及其等级"。

                        真正决定等级的是 getModifierLevel，所以这里的 0 只是一个占位符。

                        如果想彻底去掉 level，可以把 StellarModifierEntry 改成只包含 id 而不包含 level，
                     但那样需要同时调整 Material、MaterialDataLoader、addMaterialToStack 等多个类。目前保留 level 只是为了兼容性，实际逻辑中它已经被"架空"了
                    */
                    entries.add(new Material.StellarModifierEntry(entry.id(), 0));
                }
            }
        }
        return entries;
    }

    /**
     * 将指定副词条的等级累加到工具核心 NBT 中（新等级 = 原有等级 + addLevel）。
     */
    public static void mergeModifierLevel(ItemStack stack, String modifierId, int addLevel) {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag levels = root.getCompound(TAG_MODIFIER_LEVELS);
        //没加之前的等级
        //levels.getInt(modifierId); 默认返回0
        //CompoundTag 内部存储着键值对。当调用 getInt(key) 时：
        //如果该键存在且值类型为 IntTag，则返回其整数值。
        //如果该键不存在，getInt 会返回 0，而不是抛出异常或返回 null
        int current = levels.getInt(modifierId);
        levels.putInt(modifierId, current + addLevel);
        root.put(TAG_MODIFIER_LEVELS, levels);
    }

    /**
     * 从工具核心 NBT 中读取某个副词条的当前累计总等级。
     * 若从未存储过，返回 0。
     */
    public static int getModifierLevel(ItemStack stack, String modifierId) {
        CompoundTag root = stack.getTag();
        //root != null：确保物品有 NBT 数据。如果物品从未被修改过，root 会是 null，直接跳到末尾返回 0
        //root.contains(TAG_MODIFIER_LEVELS, Tag.TAG_COMPOUND)：检查根标签中是否存在键名为 "ToolCoreModifierLevels" 的复合标签，且值类型确实是 CompoundTag
        //如果玩家从未合成过任何副词条材料，这个标签不会存在，条件为 false，直接返回 0
        if (root != null && root.contains(TAG_MODIFIER_LEVELS, Tag.TAG_COMPOUND)) {
            //root.getCompound(TAG_MODIFIER_LEVELS)：获取等级存储容器 CompoundTag。这个容器内部以 modifierId -> 等级 的键值对形式存储各个副词条的累计等级
            //.getInt(modifierId)：读取该副词条对应的等级值（NBT 中以 IntTag 存储）。如果该键不存在，getInt 返回 0，这就是"从未添加过"时的默认值
            return root.getCompound(TAG_MODIFIER_LEVELS).getInt(modifierId);
        }
        return 0;
    }

    /**
     * 获取副词条的当前生效等级（玩家在设置中调节后的值）。
     * 如果玩家未手动设定，则返回累计总等级（即完全返回）。
     */
    public static int getModifierActiveLevel(ItemStack stack, String modifierId) {
        CompoundTag root = stack.getOrCreateTag();
        if (root.contains(TAG_MODIFIER_ACTIVE_LEVELS, Tag.TAG_COMPOUND)) {
            CompoundTag act = root.getCompound(TAG_MODIFIER_ACTIVE_LEVELS);
            if (act.contains(modifierId, Tag.TAG_INT)) {
                return act.getInt(modifierId);
            }
        }
        //默认返回总等级，即不限制
        return getModifierLevel(stack, modifierId);
    }

    /**
     * 设置副词条的当前生效等级，并自动联动开关状态（0 关，>0 开）。
     * 范围 [0, 总等级]。若设为 0，自动关闭该词条；若从 0 调高，自动开启。
     */
    public static void setModifierActiveLevel(ItemStack stack, String modifierId, int level) {
        int maxLevel = getModifierLevel(stack, modifierId);
        if (level < 0) level = 0;
        if (level > maxLevel) level = maxLevel;

        CompoundTag root = stack.getOrCreateTag();
        CompoundTag act = root.getCompound(TAG_MODIFIER_ACTIVE_LEVELS);
        if (level == maxLevel) {
            //设为最大值时移除记录，表示无限制
            act.remove(modifierId);
        } else {
            act.putInt(modifierId, level);
        }
        if (act.isEmpty()) {
            root.remove(TAG_MODIFIER_ACTIVE_LEVELS);
        } else {
            root.put(TAG_MODIFIER_ACTIVE_LEVELS, act);
        }

        //联动开关状态：等级 > 0 则开启，否则关闭
        setModifierEnabledDirect(stack, modifierId, level > 0);
    }

    // ═══════════════════════════════════════════════════════════════
    // 副词条开关状态
    // ═══════════════════════════════════════════════════════════════

    /**
     * 判断某个副词条是否处于开启状态。默认为开启。
     * （原先位于 ToolCoreModifierSettingsScreen，迁移至此以保证服务端安全访问）
     */
    public static boolean isModifierEnabled(ItemStack stack, String modifierId) {
        CompoundTag root = stack.getOrCreateTag();
        if (root.contains(TAG_MODIFIER_SETTINGS, Tag.TAG_COMPOUND)) {
            CompoundTag settings = root.getCompound(TAG_MODIFIER_SETTINGS);
            //仅当该词条在设置中存在显式记录时才读取，否则视为默认启用
            if (settings.contains(modifierId, Tag.TAG_BYTE)) {
                return settings.getBoolean(modifierId);
            }
        }
        return true; //默认开启
    }

    /**
     * 直接设置某个副词条的开关状态，不触发界面刷新（由外部调用）。
     * 供 setModifierActiveLevel 调用以自动联动开关。
     * （原先位于 ToolCoreModifierSettingsScreen，迁移至此以保证服务端安全访问）
     */
    public static void setModifierEnabledDirect(ItemStack stack, String modifierId, boolean enabled) {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag settings = root.getCompound(TAG_MODIFIER_SETTINGS);
        if (enabled) {
            settings.remove(modifierId); //默认开启，移除记录即可
        } else {
            settings.putBoolean(modifierId, false);
        }
        if (settings.isEmpty()) {
            root.remove(TAG_MODIFIER_SETTINGS);
        } else {
            root.put(TAG_MODIFIER_SETTINGS, settings);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 矩阵效果 NBT 读写
    // ═══════════════════════════════════════════════════════════════

    /**
     * 将指定矩阵效果的等级累加到工具核心 NBT 中（新等级 = 原有等级 + addLevel），但不超过最大等级。
     * 调用方应在调用前自行检查是否已达最大等级，避免无效调用。
     */
    public static void mergeMatrixLevel(ItemStack stack, String effectId, int addLevel, int maxLevel) {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag levels = root.getCompound(TAG_MATRIX_LEVELS);
        int current = levels.getInt(effectId);
        int newLevel = Math.min(current + addLevel, maxLevel);
        levels.putInt(effectId, newLevel);
        root.put(TAG_MATRIX_LEVELS, levels);
    }

    /**
     * 从工具核心 NBT 中读取某个矩阵效果的当前累计总等级。
     * 若从未存储过，返回 0。
     */
    public static int getMatrixTotalLevel(ItemStack stack, String effectId) {
        if (stack.hasTag() && stack.getTag().contains(TAG_MATRIX_LEVELS, Tag.TAG_COMPOUND)) {
            return stack.getTag().getCompound(TAG_MATRIX_LEVELS).getInt(effectId);
        }
        return 0;
    }

    /**
     * 获取矩阵效果的当前生效等级（玩家在设置中调节后的值）。
     * 如果玩家未手动设定，则返回累计总等级。
     */
    public static int getMatrixActiveLevel(ItemStack stack, String effectId) {
        CompoundTag root = stack.getOrCreateTag();
        if (root.contains(TAG_MATRIX_ACTIVE_LEVELS, Tag.TAG_COMPOUND)) {
            CompoundTag act = root.getCompound(TAG_MATRIX_ACTIVE_LEVELS);
            if (act.contains(effectId, Tag.TAG_INT)) {
                return act.getInt(effectId);
            }
        }
        return getMatrixTotalLevel(stack, effectId);
    }

    /**
     * 设置矩阵效果的当前生效等级，并自动联动开关状态（0 关，>0 开）。
     * 范围 [0, 总等级]。若设为 0，自动关闭该效果；若从 0 调高，自动开启。
     */
    public static void setMatrixActiveLevel(ItemStack stack, String effectId, int level) {
        int maxLevel = getMatrixTotalLevel(stack, effectId);
        if (level < 0) level = 0;
        if (level > maxLevel) level = maxLevel;

        CompoundTag root = stack.getOrCreateTag();
        CompoundTag act = root.getCompound(TAG_MATRIX_ACTIVE_LEVELS);
        if (level == maxLevel) {
            act.remove(effectId);
        } else {
            act.putInt(effectId, level);
        }
        if (act.isEmpty()) {
            root.remove(TAG_MATRIX_ACTIVE_LEVELS);
        } else {
            root.put(TAG_MATRIX_ACTIVE_LEVELS, act);
        }

        //联动开关状态
        setMatrixEnabledDirect(stack, effectId, level > 0);
    }

    // ═══════════════════════════════════════════════════════════════
    // 矩阵效果开关状态
    // ═══════════════════════════════════════════════════════════════

    /**
     * 判断某个矩阵效果是否处于开启状态。默认为开启。
     */
    public static boolean isMatrixEnabled(ItemStack stack, String effectId) {
        CompoundTag root = stack.getOrCreateTag();
        if (root.contains(TAG_MATRIX_SETTINGS, Tag.TAG_COMPOUND)) {
            CompoundTag settings = root.getCompound(TAG_MATRIX_SETTINGS);
            //仅当该效果在设置中存在显式记录时才读取，否则视为默认启用
            if (settings.contains(effectId)) {
                return settings.getBoolean(effectId);
            }
        }
        return true;
    }

    /**
     * 直接设置某个矩阵效果的开关状态，不触发界面刷新（由外部调用）。
     */
    public static void setMatrixEnabledDirect(ItemStack stack, String effectId, boolean enabled) {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag settings = root.getCompound(TAG_MATRIX_SETTINGS);
        if (enabled) {
            settings.remove(effectId);
        } else {
            settings.putBoolean(effectId, false);
        }
        if (settings.isEmpty()) {
            root.remove(TAG_MATRIX_SETTINGS);
        } else {
            root.put(TAG_MATRIX_SETTINGS, settings);
        }
    }

    /**
     * 获取工具核心所有已附加的矩阵效果 ID 集合。
     */
    public static Set<String> getAttachedMatrixEffects(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root != null && root.contains(TAG_MATRIX_LEVELS, Tag.TAG_COMPOUND)) {
            return root.getCompound(TAG_MATRIX_LEVELS).getAllKeys();
        }
        //返回一个不可变的空 Set 对象
        return Collections.emptySet();
    }

    // ═══════════════════════════════════════════════════════════════
    // 修复
    // ═══════════════════════════════════════════════════════════════

    /**
     * 尝试使用材料修复工具核心（仅当材料已添加过时有效）。
     * 修复量基于材料耐久属性的 50%，并受工具当前挖掘等级与材料等级之差衰减。
     * 若修复耐久值为 0 则不予修复。
     *
     * @return 实际恢复的耐久值，若无法修复则返回 0
     */
    public static int tryRepairWithMaterial(ItemStack stack, Material material) {
        int currentMax = getStoredMaxDamage(stack);
        if (currentMax <= 0) return 0;

        if (!hasMaterial(stack, material.itemId())) return 0;

        int currentDamage = getStoredDamage(stack);
        if (currentDamage <= 0) return 0; //未受损，无需修复

        int toolLevel = getTotalMiningLevel(stack);
        int matLevel = material.miningLevel();

        int baseRepair = material.durability() / 2;
        if (baseRepair <= 0) return 0;

        //等级差衰减
        if (toolLevel > matLevel) {
            int diff = toolLevel - matLevel;
            baseRepair = (int) (baseRepair * Math.pow(0.5, diff));
        }

        //如果修复量小于等于 0，不予修复
        if (baseRepair <= 0) {
            return 0;
        }

        int newDamage = Math.max(0, currentDamage - baseRepair);
        setStoredDamage(stack, newDamage);
        return baseRepair;
    }
}
