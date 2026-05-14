package com.luolian.stellarmod.server.item.custom.toolcore;

import com.luolian.stellarmod.server.data.toolcore.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * 合成预览 NBT 标记助手。
 * 集中管理预览物品上的临时 NBT 标记的写入和读取，
 * 供 {@code CraftingAreaBlockEntity}（写入端）和 {@code ToolCoreItem.appendHoverText}（读取端）共用。
 */
public class ToolCorePreviewHelper {

    // ═══════════════════════════════════════════════════════════════
    // 读取方法
    // ═══════════════════════════════════════════════════════════════

    /** 检查物品是否为合成预览产物 */
    public static boolean isPreview(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(ToolCoreNBT.TAG_PREVIEW);
    }

    /** 读取预览修复量，若非预览或未设置则返回 0 */
    public static int getPreviewRepair(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ToolCoreNBT.TAG_PREVIEW_REPAIR)) {
            return stack.getTag().getInt(ToolCoreNBT.TAG_PREVIEW_REPAIR);
        }
        return 0;
    }

    /** 读取本次合成应用的矩阵效果 ID，若非预览或未设置则返回 null */
    @Nullable
    public static String getPreviewMatrixId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ToolCoreNBT.TAG_PREVIEW_MATRIX_ID, Tag.TAG_STRING)) {
            return stack.getTag().getString(ToolCoreNBT.TAG_PREVIEW_MATRIX_ID);
        }
        return null;
    }

    /** 读取本次合成消耗的材料物品 ID，若非预览或未设置则返回 null */
    @Nullable
    public static String getPreviewMaterialId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ToolCoreNBT.TAG_PREVIEW_MATERIAL_ID, Tag.TAG_STRING)) {
            return stack.getTag().getString(ToolCoreNBT.TAG_PREVIEW_MATERIAL_ID);
        }
        return null;
    }

    /** 判断本次合成是否为首次添加该材料 */
    public static boolean isPreviewNewMaterial(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ToolCoreNBT.TAG_PREVIEW_IS_NEW_MATERIAL);
    }

    /** 读取本次合成新增的副词条 ID 集合，若非预览或未设置则返回 null */
    @Nullable
    public static Set<String> getPreviewNewModifiers(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ToolCoreNBT.TAG_PREVIEW_NEW_MODIFIERS, Tag.TAG_LIST)) {
            ListTag list = stack.getTag().getList(ToolCoreNBT.TAG_PREVIEW_NEW_MODIFIERS, Tag.TAG_STRING);
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < list.size(); i++) {
                ids.add(list.getString(i));
            }
            return ids;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    // 写入方法
    // ═══════════════════════════════════════════════════════════════

    /**
     * 在预览物品上写入矩阵合成预览标记。
     * 调用前应确保 preview 是 core 的副本且已设置 count=1。
     */
    public static void writeMatrixPreview(ItemStack preview, String effectId) {
        preview.getOrCreateTag().putBoolean(ToolCoreNBT.TAG_PREVIEW, true);
        preview.getOrCreateTag().putString(ToolCoreNBT.TAG_PREVIEW_MATRIX_ID, effectId);
    }

    /**
     * 在预览物品上写入材料合成预览标记（首次添加材料时）。
     * 自动对比 core 的现有副词条与合成后的副词条，将新增词条写入 TAG_PREVIEW_NEW_MODIFIERS。
     * 调用方应在调用前自行调用 addMaterialToStack 以模拟合成结果。
     */
    public static void writeNewMaterialPreview(ItemStack preview, Material material, ItemStack core) {
        CompoundTag previewTag = preview.getOrCreateTag();

        //记录本次消耗的材料ID，供提示高亮使用
        previewTag.putString(ToolCoreNBT.TAG_PREVIEW_MATERIAL_ID, material.itemId().toString());

        //首次添加前快照已有副词条，用于判定哪些是本次新增的
        //这里是从工具核心获取的工具副词条，当前没有合成，新材料不在这个集合内
        Set<String> beforeModIds = new HashSet<>();
        CompoundTag coreTag = core.getTag();
        if (coreTag != null && coreTag.contains(ToolCoreNBT.TAG_MODIFIER_LEVELS, CompoundTag.TAG_COMPOUND)) {
            beforeModIds.addAll(coreTag.getCompound(ToolCoreNBT.TAG_MODIFIER_LEVELS).getAllKeys());
        }

        //对比找出本次新增的副词条（之前不存在，现在有了）
        //这里是从工具核心合成预览获取的工具副词条，新材料在这个集合内
        ListTag newMods = new ListTag();
        if (previewTag.contains(ToolCoreNBT.TAG_MODIFIER_LEVELS, CompoundTag.TAG_COMPOUND)) {
            for (String key : previewTag.getCompound(ToolCoreNBT.TAG_MODIFIER_LEVELS).getAllKeys()) {
                if (!beforeModIds.contains(key)) {
                    newMods.add(StringTag.valueOf(key));
                }
            }
        }
        if (!newMods.isEmpty()) {
            previewTag.put(ToolCoreNBT.TAG_PREVIEW_NEW_MODIFIERS, newMods);
        }
        previewTag.putBoolean(ToolCoreNBT.TAG_PREVIEW_IS_NEW_MATERIAL, true);

        //添加预览标记，便于客户端显示提示
        previewTag.putBoolean(ToolCoreNBT.TAG_PREVIEW, true);
    }

    // ═══════════════════════════════════════════════════════════════
    // 清除方法
    // ═══════════════════════════════════════════════════════════════

    /**
     * 清除物品上所有预览相关 NBT 标记（实际合成后调用）。
     */
    public static void clearPreviewTags(ItemStack stack) {
        stack.removeTagKey(ToolCoreNBT.TAG_PREVIEW);
        stack.removeTagKey(ToolCoreNBT.TAG_PREVIEW_REPAIR);
        stack.removeTagKey(ToolCoreNBT.TAG_PREVIEW_MATRIX_ID);
        stack.removeTagKey(ToolCoreNBT.TAG_PREVIEW_MATERIAL_ID);
        stack.removeTagKey(ToolCoreNBT.TAG_PREVIEW_IS_NEW_MATERIAL);
        stack.removeTagKey(ToolCoreNBT.TAG_PREVIEW_NEW_MODIFIERS);
    }
}
