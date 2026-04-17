package com.luolian.stellarmod.server.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.luolian.stellarmod.server.data.itemcore.Material;
import com.luolian.stellarmod.server.data.itemcore.MaterialManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ToolCoreItem extends Item {

    //类结构与常量定义
    public static final String TAG_MATERIALS = "Materials";     //一个列表，存放已装配材料的 ResourceLocation 字符串
    public static final String TAG_ACTIVE_TYPE = "ActiveType";  //当前激活的工具形态ID（如 "pickaxe"）
    //耐久相关 NBT 键
    public static final String TAG_DAMAGE = "Damage";
    public static final String TAG_MAX_DAMAGE = "MaxDamage";

    public ToolCoreItem(Properties properties) {
        super(properties);
    }

    //属性汇总记录 ToolProperties
    //存储从材料计算得到的累计属性。它不包含形态基础值，只包含材料提供的部分
    public record ToolProperties(
            int miningLevel,
            float miningSpeed,
            float attackDamage,
            int durability, //耐久值
            int enchantAbility,
            int color
    ) {
        public static final ToolProperties EMPTY = new ToolProperties(0, 1.0f, 0.0f,
                0, 0, 0xFFFFFF);
    }

    //工具类型枚举ToolType，每个枚举值包含3个字段
    //id：唯一标识符，用于 NBT 存储和显示
    //baseSpeed：该形态的 基础挖掘速度
    //baseAttack：该形态的 基础攻击伤害
    public enum ToolType {
        PICKAXE("pickaxe", 1.0f, 0.0f),
        AXE("axe", 1.0f, 1.0f),
        SHOVEL("shovel", 1.0f, 0.5f),
        SWORD("sword", 1.0f, 3.0f), // 基础伤害
        HOE("hoe", 1.0f, 0.0f);

        public final String id;
        public final float baseSpeed;
        public final float baseAttack;

        ToolType(String id, float baseSpeed, float baseAttack) {
            this.id = id;
            this.baseSpeed = baseSpeed;
            this.baseAttack = baseAttack;
        }

        //用于根据字符串标识符（如 "pickaxe"、"axe"）找到对应的 ToolType 枚举实例
        //如果没匹配上就返回稿子（默认值）
        public static ToolType fromId(String id) {
            for (ToolType type : values()) {
                if (type.id.equalsIgnoreCase(id)) return type;
            }
            return PICKAXE;
        }
    }

    //静态耐久存取方法（内部使用，避免与实例方法签名冲突）

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

    //重写实例方法以满足 Forge 接口要求

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getStoredMaxDamage(stack);
    }

    @Override
    public int getDamage(ItemStack stack) {
        return getStoredDamage(stack);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        setStoredDamage(stack, damage);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredMaxDamage(stack) > 0 && getStoredDamage(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = getStoredMaxDamage(stack);
        if (max == 0) return 0;
        //耐久条是从左到右缩短的，满耐久时显示完整的 13 像素绿色条
        //随着工具使用，getStoredDamage 增加，损伤宽度 变大，剩余宽度 变小。
        //例如：最大耐久 100，当前损伤 30，则剩余宽度 = 13 - (30 * 13 / 100) = 13 - 3.9 = 9.1 → 四舍五入为 9 像素。
        return Math.round(13.0f - (float) getStoredDamage(stack) * 13.0f / (float) max);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00; //绿色耐久条
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1; //工具核心不可堆叠
    }

    //属性获取+计算
    //注意：只累加每种材料第一次添加时的属性，重复添加不再提供属性加成
    public static ToolProperties getProperties(ItemStack stack) {
        List<Material> materials = getMaterialsFromStack(stack);
        //基础属性（核心本身不给加成，只给类型基础，类型基础在外层处理）
        int miningLevel = 0;
        float miningSpeed = 0;
        float attackDamage = 0;
        int durability = 0;
        int enchantAbility = 0;
        int color = 0xFFFFFF;

        Set<ResourceLocation> seen = new HashSet<>(); //用于追踪已处理的材料（按物品ID）

        //遍历所有已添加的材料，仅累加每种材料第一次出现的属性
        for (Material mat : materials) {
            if (seen.add(mat.itemId())) {
                //挖掘等级取所有材料的最高值
                miningLevel = Math.max(miningLevel, mat.miningLevel());
                miningSpeed += mat.miningSpeed();
                attackDamage += mat.attackDamage();
                durability += mat.durability();
                enchantAbility += mat.enchantAbility();
                color = mat.color(); //最后一个材料颜色
            }
        }

        //材料联动示例
        boolean hasIron = materials.stream().anyMatch(m -> m.id().getPath().contains("iron"));
        boolean hasDiamond = materials.stream().anyMatch(m -> m.id().getPath().contains("diamond"));
        if (hasIron && hasDiamond) {
            miningSpeed *= 1.2f;
            attackDamage *= 1.2f;
        }

        return new ToolProperties(miningLevel, miningSpeed, attackDamage, durability, enchantAbility, color);
    }

    //工具形态的读
    public static ToolType getActiveType(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String typeId = tag.getString(TAG_ACTIVE_TYPE);
        return typeId.isEmpty() ? ToolType.PICKAXE : ToolType.fromId(typeId);   //默认形态为 PICKAXE，fromId 方法将字符串映射回枚举值
    }

    //工具形态的写
    public static void setActiveType(ItemStack stack, ToolType type) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_ACTIVE_TYPE, type.id);
    }

    //获取最终挖掘速度
    public static float getTotalMiningSpeed(ItemStack stack, ToolType type) {
        ToolProperties props = getProperties(stack);
        return type.baseSpeed + props.miningSpeed();
    }

    //获取最终攻击伤害
    public static float getTotalAttackDamage(ItemStack stack, ToolType type) {
        ToolProperties props = getProperties(stack);
        return type.baseAttack + props.attackDamage();
    }

    //获取最终挖掘等级
    public static int getTotalMiningLevel(ItemStack stack) {
        return getProperties(stack).miningLevel();
    }

    //材料列表 NBT 读写
    //从 NBT 读取并调用 MaterialManager.getMaterial 获取实际 Material 对象
    public static List<Material> getMaterialsFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_MATERIALS, Tag.TAG_STRING);
        List<Material> materials = new ArrayList<>();
        for (Tag t : list) {
            if (t instanceof StringTag st) {
                ResourceLocation id = ResourceLocation.tryParse(st.getAsString());
                if (id != null) {
                    //此时 id 是物品 ID，可以正确从 MaterialManager 获取
                    Material mat = MaterialManager.getMaterial(id);
                    if (mat != null) materials.add(mat);
                }
            }
        }
        return materials;
    }

    //用于组装台将材料添加到核心
    //首次添加时自动初始化最大耐久为材料提供的耐久值，并将当前损伤设为0
    public static void addMaterialToStack(ItemStack stack, Material material) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_MATERIALS, Tag.TAG_STRING);
        //此时 id 是物品 ID，可以正确从 MaterialManager 获取
        list.add(StringTag.valueOf(material.itemId().toString()));
        tag.put(TAG_MATERIALS, list);

        //如果当前最大耐久为0，说明是第一次添加材料，初始化耐久
        if (getStoredMaxDamage(stack) == 0) {
            setStoredMaxDamage(stack, material.durability());
            setStoredDamage(stack, 0); // 满耐久
        }
    }

    //检查某个材料是否已经添加过（通过物品 ID 判断）
    public static boolean hasMaterial(ItemStack stack, ResourceLocation itemId) {
        List<Material> materials = getMaterialsFromStack(stack);
        return materials.stream().anyMatch(m -> m.itemId().equals(itemId));
    }

    //获取材料添加次数（通过 NBT 列表中的出现次数）
    public static int getMaterialCount(ItemStack stack, ResourceLocation itemId) {
        List<Material> materials = getMaterialsFromStack(stack);
        return (int) materials.stream().filter(m -> m.itemId().equals(itemId)).count();
    }

    /**
     * 尝试使用材料修复工具核心（仅当材料已添加过时有效）。
     * 修复量基于材料耐久属性的 50%，并受工具当前挖掘等级与材料等级之差衰减。
     * 若修复后耐久值不足 10 则不予修复。
     *
     * @return 实际恢复的耐久值，若无法修复则返回 0
     */
    public static int tryRepairWithMaterial(ItemStack stack, Material material) {
        int currentMax = getStoredMaxDamage(stack);
        if (currentMax <= 0) return 0;

        //只有已经添加过的材料才能用于修复
        if (!hasMaterial(stack, material.itemId())) return 0;

        int currentDamage = getStoredDamage(stack);
        if (currentDamage <= 0) return 0; //未受损，无需修复

        int toolLevel = getTotalMiningLevel(stack);
        int matLevel = material.miningLevel();

        //基础恢复量 = 材料耐久属性的 50%
        int baseRepair = material.durability() / 2;
        if (baseRepair <= 0) return 0;

        //等级差衰减：工具等级高于材料时，每高一级减半
        if (toolLevel > matLevel) {
            int diff = toolLevel - matLevel;
            baseRepair = (int) (baseRepair * Math.pow(0.5, diff));
        }

        if (baseRepair < 10) return 0; //不足 10 点不予修复

        int newDamage = Math.max(0, currentDamage - baseRepair);
        setStoredDamage(stack, newDamage);
        return baseRepair;
    }

    //用于形态切换
    public static void switchToNextType(ItemStack stack) {
        ToolType current = getActiveType(stack);
        ToolType[] values = ToolType.values();
        int nextOrdinal = (current.ordinal() + 1) % values.length;
        setActiveType(stack, values[nextOrdinal]);
    }

    //用于形态切换
    public static void switchToPreviousType(ItemStack stack) {
        ToolType current = getActiveType(stack);
        ToolType[] values = ToolType.values();
        int prevOrdinal = (current.ordinal() - 1 + values.length) % values.length;
        setActiveType(stack, values[prevOrdinal]);
    }

    //物品提示信息
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ToolType type = getActiveType(stack);
        tooltip.add(Component.literal("§7Active: §f" + type.id));

        //显示已添加的材料列表
        List<Material> mats = getMaterialsFromStack(stack);
        if (!mats.isEmpty()) {
            tooltip.add(Component.literal("§7Materials:"));
            //统计每种材料的次数，便于显示
            Map<ResourceLocation, Integer> countMap = new LinkedHashMap<>();
            for (Material mat : mats) {
                countMap.put(mat.itemId(), countMap.getOrDefault(mat.itemId(), 0) + 1);
            }
            for (Map.Entry<ResourceLocation, Integer> entry : countMap.entrySet()) {
                Material mat = MaterialManager.getMaterial(entry.getKey());
                if (mat != null) {
                    String name = mat.id().toString();
                    if (entry.getValue() > 1) {
                        name += " x" + entry.getValue();
                    }
                    tooltip.add(Component.literal(" §8- §f" + name));
                }
            }
        }

        //显示耐久信息
        int maxDmg = getStoredMaxDamage(stack);
        if (maxDmg > 0) {
            int currentDmg = getStoredDamage(stack);
            tooltip.add(Component.literal("§7Durability: §f" + (maxDmg - currentDmg) + " / " + maxDmg));
        }

        //显示预览修复量（如果存在）
        if (stack.hasTag() && stack.getTag().contains("PreviewRepair")) {
            int repair = stack.getTag().getInt("PreviewRepair");
            tooltip.add(Component.literal("§aWill repair: +" + repair));
        }

        ToolProperties props = getProperties(stack);
        tooltip.add(Component.literal("§7Mining Level: §f" + props.miningLevel()));
        tooltip.add(Component.literal("§7Speed: §f" + getTotalMiningSpeed(stack, type)));
        tooltip.add(Component.literal("§7Attack: §f" + getTotalAttackDamage(stack, type)));
    }

    //动态属性修改器
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();

        // 1. 获取父类的默认修饰符（通常为空，除非物品注册时加了默认属性）
        modifiers.putAll(super.getAttributeModifiers(slot, stack));

        if (slot == EquipmentSlot.MAINHAND) {
            // 2. 移除原有的攻击伤害修饰符（确保完全自定义）
            modifiers.removeAll(Attributes.ATTACK_DAMAGE);

            // 3. 计算当前形态下的最终攻击伤害
            ToolType type = getActiveType(stack);
            float damage = getTotalAttackDamage(stack, type);

            // 4. 添加新的修饰符
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID,
                    "ToolCore attack modifier",
                    damage,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        return modifiers;
    }
}