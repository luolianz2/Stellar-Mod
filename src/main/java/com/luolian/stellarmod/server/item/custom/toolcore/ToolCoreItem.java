package com.luolian.stellarmod.server.item.custom.toolcore;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import com.luolian.stellarmod.api.toolcore.StellarModifierEffect;
import com.luolian.stellarmod.server.data.toolcore.StellarMatrixRegistry;
import com.luolian.stellarmod.server.data.toolcore.StellarModifierRegistry;
import com.luolian.stellarmod.server.data.toolcore.Material;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ToolCoreItem extends Item {

    //自定义标签：需要 5 级工具才能挖掘的方块
    public static final TagKey<Block> NEEDS_STELLAR_TOOL =
            TagKey.create(Registries.BLOCK, StellarMod.location("needs_stellar_tool"));

    //自定义标签：需要 4 级工具（下界合金）才能挖掘的方块
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "needs_netherite_tool"));

    //挖掘等级 → 对应的"需要该等级工具"标签
    private static final Map<Integer, TagKey<Block>> MINING_LEVEL_TAGS = Map.of(
            1, BlockTags.NEEDS_STONE_TOOL,
            2, BlockTags.NEEDS_IRON_TOOL,
            3, BlockTags.NEEDS_DIAMOND_TOOL,
            4, NEEDS_NETHERITE_TOOL,
            5, NEEDS_STELLAR_TOOL
    );

    public ToolCoreItem(Properties properties) {
        super(properties);
    }

    //模型属性覆盖的键名
    public static final ResourceLocation ACTIVE_TYPE_PREDICATE = StellarMod.location("active_type");

    //工具类型枚举ToolType，每个枚举值包含3个字段
    //id：唯一标识符，用于 NBT 存储和显示
    //baseSpeed：该形态的 基础挖掘速度
    //baseAttack：该形态的 基础攻击伤害
    public enum ToolType {
        PICKAXE("pickaxe", 1.2f, 1.0f),
        AXE("axe", 0.8f, 6.0f),
        SHOVEL("shovel", 1.0f, 1.5f),
        SWORD("sword", 1.6f, 3.0f), // 基础伤害
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

    //重写实例方法以满足 Forge 接口要求

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ToolCoreNBT.getStoredMaxDamage(stack);
    }

    @Override
    public int getDamage(ItemStack stack) {
        return ToolCoreNBT.getStoredDamage(stack);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        ToolCoreNBT.setStoredDamage(stack, damage);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ToolCoreNBT.getStoredMaxDamage(stack) > 0 && ToolCoreNBT.getStoredDamage(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = ToolCoreNBT.getStoredMaxDamage(stack);
        if (max == 0) return 0;
        //耐久条是从左到右缩短的，满耐久时显示完整的 13 像素绿色条
        //随着工具使用，getStoredDamage 增加，损伤宽度 变大，剩余宽度 变小。
        //例如：最大耐久 100，当前损伤 30，则剩余宽度 = 13 - (30 * 13 / 100) = 13 - 3.9 = 9.1 → 四舍五入为 9 像素。
        return Math.round(13.0f - (float) ToolCoreNBT.getStoredDamage(stack) * 13.0f / (float) max);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00; //绿色耐久条
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1; //工具核心不可堆叠
    }

    //右键交互逻辑（锄地、铲土径、斧去皮等）

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        ToolType type = getActiveType(stack);

        //根据当前形态处理不同的右键交互
        return switch (type) {
            case HOE -> handleHoeAction(context, state, pos, level, player, stack);
            case SHOVEL -> handleShovelAction(context, state, pos, level, player, stack);
            case AXE -> handleAxeAction(context, state, pos, level, player, stack);
            default -> InteractionResult.PASS;
        };
    }

    /**
     * 处理锄头右键交互：将草方块、土块等转变为耕地
     */
    private InteractionResult handleHoeAction(UseOnContext context, BlockState state, BlockPos pos,
                                              Level level, Player player, ItemStack stack) {
        //检查是否为可耕地方块
        BlockState farmland = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
        if (farmland != null) {
            if (!level.isClientSide) {
                level.setBlock(pos, farmland, 11);
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    /**
     * 处理铲子右键交互：将草方块转变为土径
     */
    private InteractionResult handleShovelAction(UseOnContext context, BlockState state, BlockPos pos,
                                                 Level level, Player player, ItemStack stack) {
        //制造土径
        BlockState pathState = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
        if (pathState != null && pathState.getBlock() != state.getBlock()) {
            if (!level.isClientSide) {
                level.setBlock(pos, pathState, 11);
                level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    /**
     * 处理斧头右键交互：原木去皮、铜块除锈/去蜡等
     */
    private InteractionResult handleAxeAction(UseOnContext context, BlockState state, BlockPos pos,
                                              Level level, Player player, ItemStack stack) {
        //1. 原木去皮
        BlockState strippedState = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
        if (strippedState != null) {
            if (!level.isClientSide) {
                level.setBlock(pos, strippedState, 11);
                level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        //2. 铜块刮锈 (AXE_SCRAPE)
        BlockState scrapedState = state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
        if (scrapedState != null) {
            if (!level.isClientSide) {
                level.setBlock(pos, scrapedState, 11);
                level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        //3. 铜块去蜡 (AXE_WAX_OFF)
        BlockState waxOffState = state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
        if (waxOffState != null) {
            if (!level.isClientSide) {
                level.setBlock(pos, waxOffState, 11);
                level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    //工具行为重写（挖掘、攻击）

    /**
     * 获取工具的挖掘速度。如果当前形态能有效挖掘该方块，则返回总挖掘速度，否则返回 1.0（空手速度），
     * 剑形态对蜘蛛网等特殊方块有额外高速加成（固定10.0），并叠加材料挖掘速度
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        ToolType type = getActiveType(stack);
        ToolCoreNBT.ToolProperties props = ToolCoreNBT.getProperties(stack);

        //剑形态特殊处理：蜘蛛网或属于 SWORD_EFFICIENT 标签的方块
        if (type == ToolType.SWORD) {
            //显式检查蜘蛛网（解决标签加载问题）以及标签匹配
            if (state.is(Blocks.COBWEB) || state.is(BlockTags.SWORD_EFFICIENT)) {
                return 10.0f + props.miningSpeed();
            }
        }

        //正常挖掘速度计算
        if (isCorrectToolForDrops(stack, state)) {
            return getTotalMiningSpeed(stack, type);
        }
        return 1.0F;
    }

    /**
     * 判断该工具是否能采集目标方块（即是否能使其掉落物品）。
     * 同时检查工具类型标签和挖掘等级标签。
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        //工具已损坏，无法采集任何方块掉落
        int max = ToolCoreNBT.getStoredMaxDamage(stack);
        int damage = ToolCoreNBT.getStoredDamage(stack);
        if (max > 0 && damage >= max) {
            return false;
        }

        ToolType type = getActiveType(stack);

        //剑形态特殊处理：蜘蛛网可以直接掉落（即使标签失效）
        if (type == ToolType.SWORD && state.is(Blocks.COBWEB)) {
            return true;
        }

        //1. 检查工具类型是否匹配
        if (!state.is(getHarvestTagForType(type))) {
            return false;
        }
        //2. 检查挖掘等级是否足够
        int level = getTotalMiningLevel(stack);
        return isMiningLevelSufficient(state, level);
    }

    /**
     * 检查当前挖掘等级是否足以采集该方块。
     * 使用 NEEDS_*_TOOL 标签判断。
     */
    private boolean isMiningLevelSufficient(BlockState state, int level) {
        //如果工具等级 >= 5，可采集任意方块
        if (level >= 5) return true;

        //如果方块和工具等级对应的 "NEEDS" 标签相同，则可以采集
        TagKey<Block> neededTag = MINING_LEVEL_TAGS.get(level);
        if (neededTag != null && state.is(neededTag)) {
            return true;
        }

        //遍历更高等级的标签，如果方块需要更高等级的工具，则当前工具无法采集
        for (int i = level + 1; i <= 5; i++) {
            TagKey<Block> higherTag = MINING_LEVEL_TAGS.get(i);
            if (higherTag != null && state.is(higherTag)) {
                return false;
            }
        }

        //方块没有任何挖掘等级要求，可以采集
        return true;
    }

    /**
     * 辅助方法：根据工具形态获取对应的挖掘标签（如 MINEABLE_WITH_PICKAXE）。
     */
    private TagKey<Block> getHarvestTagForType(ToolType type) {
        return switch (type) {
            case AXE -> BlockTags.MINEABLE_WITH_AXE;
            case SHOVEL -> BlockTags.MINEABLE_WITH_SHOVEL;
            case HOE -> BlockTags.MINEABLE_WITH_HOE;
            case SWORD -> BlockTags.SWORD_EFFICIENT;
            default -> BlockTags.MINEABLE_WITH_PICKAXE;
        };
    }

    /**
     * 玩家手持工具破坏方块时调用。在此消耗 1 点耐久，并触发所有已开启副词条的挖掘后效果（使用玩家调节后的生效等级）。
     * 若任意已启用的副词条返回跳过耐久（如耐用），则不消耗本次耐久。
     */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            /*
             * 先遍历副词条：一方面收集"是否跳过耐久"，另一方面触发 after-break 回调。
             * 将跳过耐久的判断与回调合并到一个循环中，避免额外遍历开销。
             */
            boolean skipDurability = false;
            for (Material.StellarModifierEntry entry : ToolCoreNBT.getAllModifiers(stack)) {
                //触发副词条效果（仅当开关开启，并传入玩家调节后的生效等级）
                if (!ToolCoreNBT.isModifierEnabled(stack, entry.id())) {
                    continue; //该副词条已被玩家禁用，跳过
                }
                StellarModifierEffect effect = StellarModifierRegistry.get(entry.id());
                if (effect != null) {
                    //使用玩家在设置中调节后的生效等级，而非累计总等级
                    int modifierLevel = ToolCoreNBT.getModifierActiveLevel(stack, entry.id());
                    if (effect.shouldSkipDurability(modifierLevel)) {
                        skipDurability = true;
                    }
                    effect.onBlockMined(stack, level, state, pos, entityLiving, modifierLevel);
                }
            }

            //只有未被跳过时才消耗耐久
            if (!skipDurability) {
                int newDamage = getDamage(stack) + 1;
                ToolCoreNBT.setStoredDamage(stack, newDamage);
                if (newDamage >= getMaxDamage(stack)) {
                    playBreakSound(entityLiving);
                }
            }
        }
        return true;
    }

    /**
     * 玩家手持工具攻击实体时调用。在此消耗 2 点耐久，并触发所有已开启副词条的攻击后效果（使用玩家调节后的生效等级）。
     * 若任意已启用的副词条返回跳过耐久（如耐用），则不消耗本次耐久。
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        /*
         * 先遍历副词条：一方面收集"是否跳过耐久"，另一方面触发 after-hit 回调。
         */
        boolean skipDurability = false;
        for (Material.StellarModifierEntry entry : ToolCoreNBT.getAllModifiers(stack)) {
            //触发副词条效果（仅当开关开启，并传入玩家调节后的生效等级）
            if (!ToolCoreNBT.isModifierEnabled(stack, entry.id())) {
                continue; //该副词条已被玩家禁用，跳过
            }
            StellarModifierEffect effect = StellarModifierRegistry.get(entry.id());
            if (effect != null) {
                //使用玩家在设置中调节后的生效等级，而非累计总等级
                int modifierLevel = ToolCoreNBT.getModifierActiveLevel(stack, entry.id());
                if (effect.shouldSkipDurability(modifierLevel)) {
                    skipDurability = true;
                }
                effect.onEntityHurt(stack, target, attacker, modifierLevel);
            }
        }

        //只有未被跳过时才消耗耐久
        if (!skipDurability) {
            int newDamage = getDamage(stack) + 2;
            ToolCoreNBT.setStoredDamage(stack, newDamage);
            if (newDamage >= getMaxDamage(stack)) {
                playBreakSound(attacker);
            }
        }
        return true;
    }

    /**
     * 声明工具支持哪些右键交互动作（如锄地、剥皮、制造土径等）。
     */
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        ToolType type = getActiveType(stack);
        return switch (type) {
            case AXE -> ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction);
            case SHOVEL -> ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction);
            case SWORD -> ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
            case HOE -> ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction);
            default -> false;
        };
    }

    //工具形态的读
    public static ToolType getActiveType(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String typeId = tag.getString(ToolCoreNBT.TAG_ACTIVE_TYPE);
        return typeId.isEmpty() ? ToolType.PICKAXE : ToolType.fromId(typeId);
    }

    //工具形态的写
    public static void setActiveType(ItemStack stack, ToolType type) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ToolCoreNBT.TAG_ACTIVE_TYPE, type.id);
    }

    //获取最终挖掘速度
    public static float getTotalMiningSpeed(ItemStack stack, ToolType type) {
        ToolCoreNBT.ToolProperties props = ToolCoreNBT.getProperties(stack);
        float total = type.baseSpeed + props.miningSpeed();
        //四舍五入保留一位小数
        return Math.round(total * 10.0f) / 10.0f;
    }

    //获取最终攻击伤害
    public static float getTotalAttackDamage(ItemStack stack, ToolType type) {
        ToolCoreNBT.ToolProperties props = ToolCoreNBT.getProperties(stack);
        float total = type.baseAttack + props.attackDamage();
        //四舍五入保留一位小数
        return Math.round(total * 10.0f) / 10.0f;
    }

    //获取最终挖掘等级（委托给 ToolCoreNBT）
    public static int getTotalMiningLevel(ItemStack stack) {
        return ToolCoreNBT.getTotalMiningLevel(stack);
    }

    //物品提示信息
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        //检测是否只按下了 Shift 键，若是则显示矩阵效果
        if (Screen.hasShiftDown() && !Screen.hasControlDown()) {
            //读取预览标记，用于高亮本次合成新增的矩阵
            String previewMatrixId = ToolCorePreviewHelper.getPreviewMatrixId(stack);

            Set<String> matrixIds = ToolCoreNBT.getAttachedMatrixEffects(stack);
            //先统计已启用的总数（不构建完整列表）
            int total = 0;
            for (String id : matrixIds) {
                if (ToolCoreNBT.getMatrixActiveLevel(stack, id) > 0) total++;
            }
            if (total > 0) {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.matrix_header"));
                int shown = 0;
                for (String id : matrixIds) {
                    if (shown >= 5) break;
                    if (ToolCoreNBT.getMatrixActiveLevel(stack, id) <= 0) continue;
                    StellarMatrixEffect effect = StellarMatrixRegistry.get(id);
                    if (effect != null) {
                        Component displayName = effect.getDisplayName();
                        shown++;
                        int totalLevel = ToolCoreNBT.getMatrixTotalLevel(stack, id);
                        int activeLevel = ToolCoreNBT.getMatrixActiveLevel(stack, id);
                        //MutableComponent是可修改的文本组件，允许在创建后动态追加内容、修改样式或添加事件
                        //此处作用是复制一个已有的 displayName 组件，然后追加类似 "Lv.5/10" 的等级文本
                        MutableComponent nameLine = displayName.copy()
                                .append("Lv." + activeLevel + "/" + totalLevel);
                        //检查是否为本次合成新增的矩阵，高亮显示
                        if (id.equals(previewMatrixId)) {
                            nameLine = nameLine.withStyle(ChatFormatting.GOLD)
                                    .append(Component.translatable("tooltip.stellarmod_item.tool_core.new_tag"));
                        }
                        tooltip.add(nameLine);
                        List<Component> descList = effect.getDescription();
                        for (Component desc : descList) {
                            //Component.literal(" ")创建一个仅包含两个空格的纯文本组件（MutableComponent）
                            tooltip.add(Component.literal("  ").append(desc));
                        }
                        Component note = effect.getAuthorNote();
                        tooltip.add(Component.literal("  ").append(note));
                    }
                }
                if (total > 5) {
                    tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.more_matrix", total - 5));
                }
            } else {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.no_matrix"));
            }
            return;
        }

        //检测是否按下了 Ctrl 键，若是则仅显示副词条详情
        if (Screen.hasControlDown()) {
            //读取预览标记，找出本次合成新增的副词条ID集合
            Set<String> newModifierIds = ToolCorePreviewHelper.getPreviewNewModifiers(stack);

            List<Material> materials = ToolCoreNBT.getMaterialsFromStack(stack);
            List<String> modifierIds = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (Material mat : materials) {
                for (Material.StellarModifierEntry entry : mat.modifiers()) {
                    if (seen.add(entry.id())) {
                        modifierIds.add(entry.id());
                    }
                }
            }
            //先统计已启用的总数（不构建完整列表）
            int total = 0;
            for (String modifierId : modifierIds) {
                if (ToolCoreNBT.getModifierActiveLevel(stack, modifierId) > 0) total++;
            }
            if (total > 0) {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.modifiers_header"));
                int shown = 0;
                for (String modifierId : modifierIds) {
                    if (shown >= 5) break;
                    if (ToolCoreNBT.getModifierActiveLevel(stack, modifierId) <= 0) continue;
                    StellarModifierEffect effect = StellarModifierRegistry.get(modifierId);
                    if (effect != null) {
                        Component displayName = effect.getDisplayName();
                        shown++;
                        int maxLevel = ToolCoreNBT.getModifierLevel(stack, modifierId);
                        int activeLevel = ToolCoreNBT.getModifierActiveLevel(stack, modifierId);
                        //显示格式：名称 Lv.当前/最大，例如"电磁力 Lv.2/5"
                        MutableComponent nameWithLevel = displayName.copy()
                                .append("Lv." + activeLevel + "/" + maxLevel);
                        //检查是否为本次合成新增的副词条，高亮显示
                        if (newModifierIds != null && newModifierIds.contains(modifierId)) {
                            nameWithLevel = nameWithLevel.withStyle(ChatFormatting.GOLD)
                                    .append(Component.translatable("tooltip.stellarmod_item.tool_core.new_tag"));
                        }
                        tooltip.add(nameWithLevel);
                        List<Component> descList = effect.getDescription();
                        for (Component desc : descList) {
                            tooltip.add(Component.literal("  ").append(desc));
                        }
                        Component note = effect.getAuthorNote();
                        tooltip.add(Component.literal("  ").append(note));
                    }
                }
                if (total > 5) {
                    tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.more_modifiers", total - 5));
                }
            } else {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.no_modifiers"));
            }
            return; //按 Ctrl 时不显示其他信息
        }

        //正常提示信息
        ToolType type = getActiveType(stack);

        //激活形态
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.active",
                Component.translatable("tooltip.stellarmod_item.tool_core.type." + type.id)));

        List<Material> mats = ToolCoreNBT.getMaterialsFromStack(stack);
        if (!mats.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.materials"));
            //读取预览标记，用于高亮本次合成新增的材料
            boolean isPreview = ToolCorePreviewHelper.isPreview(stack);
            String previewMatId = null;
            boolean isNewMaterial = false;
            if (isPreview) {
                previewMatId = ToolCorePreviewHelper.getPreviewMaterialId(stack);
                isNewMaterial = ToolCorePreviewHelper.isPreviewNewMaterial(stack);
            }

            int totalMaterials = mats.size();
            int maxShow = Math.min(totalMaterials, 5);

            //收集前5个材料，若预览新材料在5个之后则替换最后一个确保可见
            List<Material> showMats = new ArrayList<>();
            Material previewMat = null;
            for (Material mat : mats) {
                if (showMats.size() < maxShow) {
                    showMats.add(mat);
                }
                if (isNewMaterial && mat.itemId().toString().equals(previewMatId)) {
                    previewMat = mat;
                }
            }
            if (previewMat != null && !showMats.contains(previewMat) && !showMats.isEmpty()) {
                showMats.set(showMats.size() - 1, previewMat);
            }

            for (Material mat : showMats) {
                ResourceLocation itemId = mat.itemId();
                Component itemName = Component.translatable("item." + itemId.getNamespace() + "." + itemId.getPath());
                //检查是否为本次合成新增的材料，高亮显示
                boolean isThisNew = isNewMaterial && itemId.toString().equals(previewMatId);
                if (isThisNew) {
                    //§6-这是材料名前的-号，为金色，§8-为灰色-号
                    tooltip.add(Component.literal(" §6- ").append(itemName.copy().withStyle(ChatFormatting.GOLD))
                            .append(Component.translatable("tooltip.stellarmod_item.tool_core.new_tag")));
                } else {
                    tooltip.add(Component.literal(" §8- ").append(itemName));
                }
            }
            if (totalMaterials > maxShow) {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.more_materials", totalMaterials - maxShow));
            }
        }

        int maxDmg = ToolCoreNBT.getStoredMaxDamage(stack);
        if (maxDmg > 0) {
            int currentDmg = ToolCoreNBT.getStoredDamage(stack);
            tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.durability", maxDmg - currentDmg, maxDmg));
        }

        int repair = ToolCorePreviewHelper.getPreviewRepair(stack);
        if (repair > 0) {
            tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.will_repair", repair));
        }

        ToolCoreNBT.ToolProperties props = ToolCoreNBT.getProperties(stack);
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.mining_level", props.miningLevel()));
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.speed", getTotalMiningSpeed(stack, type)));
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.attack", getTotalAttackDamage(stack, type)));

        //提示按住 Ctrl 查看副词条
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.press_ctrl"));
        //补充按住 Shift 查看矩阵效果的引导
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.press_shift"));
    }

    /**
     * 动态属性修改器。根据当前工具形态和材料属性，向游戏注册攻击伤害加成。
     * 该方法在物品被手持或装备时由原版调用，返回的 Multimap 会被应用到玩家属性上。
     *
     * @param slot  物品所在的装备槽位
     * @param stack 当前物品堆叠
     * @return 包含攻击伤害修饰符的属性 multimap
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        //创建一个新的 multimap，用于存放最终要返回的属性修饰符
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        //先加入父类（Item 基类）提供的默认属性修饰符（通常为空，但保留以防将来变化）
        modifiers.putAll(super.getAttributeModifiers(slot, stack));

        //只有当物品在主手时才应用攻击伤害加成
        if (slot == EquipmentSlot.MAINHAND) {
            //移除原版的攻击伤害修饰符，确保完全由自定义伤害覆盖
            modifiers.removeAll(Attributes.ATTACK_DAMAGE);

            //获取当前激活的工具形态
            ToolType type = getActiveType(stack);
            //计算基于材料属性和形态基础值的最终攻击伤害
            float damage = getTotalAttackDamage(stack, type);

            //将计算出的攻击伤害以 ADDITION（加法）模式注册到属性系统中
            //使用原版的 BASE_ATTACK_DAMAGE_UUID，确保与其他修饰符正确交互
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID,          //唯一标识符，用于识别和替换修饰符
                    "ToolCore attack modifier",       //修饰符名称（调试用）
                    damage,                           //伤害值
                    AttributeModifier.Operation.ADDITION //加法运算：最终伤害 = 基础(玩家基础攻击力) + 此值(damage 值)
            ));
        }
        return modifiers;
    }

    /**
     * 覆写 Forge IForgeItem 接口方法，返回物品上所有附魔的映射。
     * <p>
     * loot table 的 match_tool 条件通过 {@link ItemStack#getAllEnchantments()}
     * 获取附魔 Map 后交由 EnchantmentPredicate 匹配，而非调用 hasSilkTouch。
     * 因此需要在此方法中将精准采集副词条注入为 Silk Touch 附魔。
     */
    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        //super.getAllEnchantments(stack)把物品的附魔放到map里
        Map<Enchantment, Integer> map = new HashMap<>(super.getAllEnchantments(stack));
        //仅在没有真实 Silk Touch 附魔时，用精准采集副词条替代
        if (!map.containsKey(Enchantments.SILK_TOUCH)) {
            if (ToolCoreNBT.isModifierEnabled(stack, "stellarmod:precision_collection")) {
                int level = ToolCoreNBT.getModifierActiveLevel(stack, "stellarmod:precision_collection");
                if (level > 0) {
                    map.put(Enchantments.SILK_TOUCH, 1);
                }
            }
        }
        return map;
    }

    /**
     * 覆写 Forge IForgeItem 接口方法，使得精准采集副词条在 hasSilkTouch 等单附魔等级查询中
     * 被识别为原版 Silk Touch。与 {@link #getAllEnchantments(ItemStack)} 配合覆盖两条链路。
     *
     * @return 1 表示拥有精准采集，0 表示交由 EnchantmentHelper 继续检查 NBT 标签
     */
    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        //精准采集副词条充当原版 Silk Touch
        if (enchantment == Enchantments.SILK_TOUCH) {
            if (ToolCoreNBT.isModifierEnabled(stack, "stellarmod:precision_collection")) {
                int level = ToolCoreNBT.getModifierActiveLevel(stack, "stellarmod:precision_collection");
                if (level > 0) {
                    return 1;
                }
            }
        }
        //返回 0 让 EnchantmentHelper 继续走原版 NBT 标签检查逻辑
        return 0;
    }

    //在工具损坏时播放音效
    private void playBreakSound(LivingEntity entity) {
        if (entity instanceof Player player) {
            player.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F + player.level().random.nextFloat() * 0.4F);
        }
    }
}