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

import java.util.ArrayList;
import java.util.List;

public class ToolCoreItem extends Item {

    //类结构与常量定义
    public static final String TAG_MATERIALS = "Materials";     //一个列表，存放已装配材料的 ResourceLocation 字符串
    public static final String TAG_ACTIVE_TYPE = "ActiveType";  //当前激活的工具形态ID（如 "pickaxe"）

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
            int enchantability,
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

    //属性获取+计算
    public static ToolProperties getProperties(ItemStack stack) {
        List<Material> materials = getMaterialsFromStack(stack);
        //基础属性（核心本身不给加成，只给类型基础，类型基础在外层处理）
        int miningLevel = 0;
        float miningSpeed = 0;
        float attackDamage = 0;
        int durability = 0;
        int enchantability = 0;
        int color = 0xFFFFFF;

        //遍历所有已添加的材料，累加各项属性
        for (Material mat : materials) {
            miningLevel += mat.miningLevel();
            miningSpeed += mat.miningSpeed();
            attackDamage += mat.attackDamage();
            durability += mat.durability();
            enchantability += mat.enchantability();
            color = mat.color(); // 最后一个材料颜色
        }

        // 材料联动示例
        boolean hasIron = materials.stream().anyMatch(m -> m.id().getPath().contains("iron"));
        boolean hasDiamond = materials.stream().anyMatch(m -> m.id().getPath().contains("diamond"));
        if (hasIron && hasDiamond) {
            miningSpeed *= 1.2f;
            attackDamage *= 1.2f;
        }

        return new ToolProperties(miningLevel, miningSpeed, attackDamage, durability, enchantability, color);
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
        ListTag list = tag.getList(TAG_MATERIALS, Tag.TAG_STRING); //材料以 ResourceLocation 字符串形式存入 ListTag
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

    //用于组装台将材料添加到核心
    public static void addMaterialToStack(ItemStack stack, Material material) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_MATERIALS, Tag.TAG_STRING);
        list.add(StringTag.valueOf(material.id().toString()));
        tag.put(TAG_MATERIALS, list);
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
        List<Material> mats = getMaterialsFromStack(stack);
        if (!mats.isEmpty()) {
            tooltip.add(Component.literal("§7Materials:"));
            for (Material mat : mats) {
                tooltip.add(Component.literal(" §8- §f" + mat.id().toString()));
            }
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

    // 工具耐久由材料累加，但核心本身不显示耐久条（可自行决定）
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }
}