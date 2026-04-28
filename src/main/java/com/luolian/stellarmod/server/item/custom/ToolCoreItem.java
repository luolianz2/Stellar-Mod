package com.luolian.stellarmod.server.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.modifier.StellarModifierEffect;
import com.luolian.stellarmod.client.screen.toolCore.ToolCoreModifierSettingsScreen;
import com.luolian.stellarmod.server.data.modifier.StellarModifierRegistry;
import com.luolian.stellarmod.server.data.toolCore.Material;
import com.luolian.stellarmod.server.data.toolCore.MaterialManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ToolCoreItem extends Item {

    //类结构与常量定义
    public static final String TAG_MATERIALS = "Materials";     //一个列表，存放已装配材料的 ResourceLocation 字符串
    public static final String TAG_ACTIVE_TYPE = "ActiveType";  //当前激活的工具形态ID（如 "pickaxe"）
    //耐久相关 NBT 键
    public static final String TAG_DAMAGE = "Damage";
    public static final String TAG_MAX_DAMAGE = "MaxDamage";
    //副词条等级存储键（累计总等级）
    public static final String TAG_MODIFIER_LEVELS = "ToolCoreModifierLevels";
    //副词条生效等级存储键（玩家在设置中调节后的当前生效等级）
    public static final String TAG_MODIFIER_ACTIVE_LEVELS = "ToolCoreModifierActiveLevels";

    //自定义标签：需要 5 级工具才能挖掘的方块
    public static final TagKey<Block> NEEDS_STELLAR_TOOL =
            TagKey.create(Registries.BLOCK, StellarMod.location("needs_stellar_tool"));

    //自定义标签：需要 4 级工具（下界合金）才能挖掘的方块
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "needs_netherite_tool"));

    //挖掘等级 → 对应的“需要该等级工具”标签
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

    //属性汇总记录 ToolProperties
    //存储从材料计算得到的累计属性。它不包含形态基础值，只包含材料提供的部分
    public record ToolProperties(
            int miningLevel,
            float miningSpeed,
            float attackDamage,
            int durability //耐久值
    ) {
        public static final ToolProperties EMPTY = new ToolProperties(0, 0.0f, -3.0f, 0);
    }

    //工具类型枚举ToolType，每个枚举值包含3个字段
    //id：唯一标识符，用于 NBT 存储和显示
    //baseSpeed：该形态的 基础挖掘速度
    //baseAttack：该形态的 基础攻击伤害
    public enum ToolType {
        PICKAXE("pickaxe", 1.2f, 1.0f),
        AXE("axe", 0.8f, 6.0f),
        SHOVEL("shovel", 1.0f, 0.5f),
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
        ToolProperties props = getProperties(stack);

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
        int max = getStoredMaxDamage(stack);
        int damage = getStoredDamage(stack);
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
     * 收集工具当前所有已添加材料中的副词条条目（已去重，仅保留唯一 ID）。
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
                     entries 需要收集的是当前工具核心所拥有的所有“副词条 ID”，而等级数据已经完全移交给 getModifierLevel 统一管理。

                        在 getAllModifiers 中，entry.level() 已经失去了意义，所以故意写成 0，目的是：
                            1.满足构造器语法
                            2.消除歧义，强迫后续代码必须用 getModifierLevel 来读取等级

                        getAllModifiers 的职责现在是“返回所有已装配的副词条 ID”，而不是“返回所有副词条及其等级”。

                        真正决定等级的是 getModifierLevel，所以这里的 0 只是一个占位符。

                        如果想彻底去掉 level，可以把 StellarModifierEntry 改成只包含 id 而不包含 level，
                     但那样需要同时调整 Material、MaterialDataLoader、addMaterialToStack 等多个类。目前保留 level 只是为了兼容性，实际逻辑中它已经被“架空”了
                    */
                    entries.add(new Material.StellarModifierEntry(entry.id(), 0));
                }
            }
        }
        return entries;
    }

    /**
     * 玩家手持工具破坏方块时调用。在此消耗 1 点耐久，并触发所有已开启副词条的挖掘后效果（使用玩家调节后的生效等级）。
     */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            int newDamage = getDamage(stack) + 1;
            setStoredDamage(stack, newDamage);
            if (newDamage >= getMaxDamage(stack)) {
                playBreakSound(entityLiving);
            }

            //触发副词条效果（仅当开关开启，并传入玩家调节后的生效等级）
            for (Material.StellarModifierEntry entry : getAllModifiers(stack)) {
                if (!ToolCoreModifierSettingsScreen.isModifierEnabled(stack, entry.id())) {
                    continue; //该副词条已被玩家禁用，跳过
                }
                StellarModifierEffect effect = StellarModifierRegistry.get(entry.id());
                if (effect != null) {
                    //使用玩家在设置中调节后的生效等级，而非累计总等级
                    int modifierLevel = getModifierActiveLevel(stack, entry.id());
                    effect.onBlockMined(stack, level, state, pos, entityLiving, modifierLevel);
                }
            }
        }
        return true;
    }

    /**
     * 玩家手持工具攻击实体时调用。在此消耗 2 点耐久，并触发所有已开启副词条的攻击后效果（使用玩家调节后的生效等级）。
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int newDamage = getDamage(stack) + 2;
        setStoredDamage(stack, newDamage);
        if (newDamage >= getMaxDamage(stack)) {
            playBreakSound(attacker);
        }

        //触发副词条效果（仅当开关开启，并传入玩家调节后的生效等级）
        for (Material.StellarModifierEntry entry : getAllModifiers(stack)) {
            if (!ToolCoreModifierSettingsScreen.isModifierEnabled(stack, entry.id())) {
                continue; //该副词条已被玩家禁用，跳过
            }
            StellarModifierEffect effect = StellarModifierRegistry.get(entry.id());
            if (effect != null) {
                // 使用玩家在设置中调节后的生效等级，而非累计总等级
                int modifierLevel = getModifierActiveLevel(stack, entry.id());
                effect.onEntityHurt(stack, target, attacker, modifierLevel);
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

    //材料属性与形态相关逻辑

    //属性获取+计算
    // - 挖掘速度/攻击伤害的基础值 = 所有已添加材料中的最高属性值
    // - 多样性加成 = Σ(每种首次添加材料的挖掘等级 × 1%)
    // - 最终值 = 基础值 × (1 + 多样性加成)
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

    //工具形态的读
    public static ToolType getActiveType(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String typeId = tag.getString(TAG_ACTIVE_TYPE);
        return typeId.isEmpty() ? ToolType.PICKAXE : ToolType.fromId(typeId);
    }

    //工具形态的写
    public static void setActiveType(ItemStack stack, ToolType type) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_ACTIVE_TYPE, type.id);
    }

    //获取最终挖掘速度
    public static float getTotalMiningSpeed(ItemStack stack, ToolType type) {
        ToolProperties props = getProperties(stack);
        float total = type.baseSpeed + props.miningSpeed();
        //四舍五入保留一位小数
        return Math.round(total * 10.0f) / 10.0f;
    }

    //获取最终攻击伤害
    public static float getTotalAttackDamage(ItemStack stack, ToolType type) {
        ToolProperties props = getProperties(stack);
        float total = type.baseAttack + props.attackDamage();
        //四舍五入保留一位小数
        return Math.round(total * 10.0f) / 10.0f;
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
                    Material mat = MaterialManager.getMaterial(id);
                    if (mat != null) materials.add(mat);
                }
            }
        }
        return materials;
    }

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
            //.getInt(modifierId)：读取该副词条对应的等级值（NBT 中以 IntTag 存储）。如果该键不存在，getInt 返回 0，这就是“从未添加过”时的默认值
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
        ToolCoreModifierSettingsScreen.setModifierEnabledDirect(stack, modifierId, level > 0);
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

    //物品提示信息
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        //检测是否按下了 Ctrl 键，若是则仅显示副词条详情
        if (Screen.hasControlDown()) {
            List<Material.StellarModifierEntry> modifiers = getAllModifiers(stack);
            boolean hasAnyActive = false; //是否至少有一个生效的词条
            if (!modifiers.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.modifiers_header"));
                for (Material.StellarModifierEntry entry : modifiers) {
                    StellarModifierEffect effect = StellarModifierRegistry.get(entry.id());
                    if (effect != null) {
                        int activeLevel = getModifierActiveLevel(stack, entry.id());
                        //生效等级为 0 表示已关闭，隐藏该条目
                        if (activeLevel == 0) continue;
                        hasAnyActive = true;
                        int maxLevel = getModifierLevel(stack, entry.id());
                        //显示格式：名称 Lv.当前/最大，例如“电磁力 Lv.2/5”
                        Component nameWithLevel = effect.getDisplayName().copy()
                                .append(" Lv." + activeLevel + "/" + maxLevel);
                        tooltip.add(nameWithLevel);
                        for (Component desc : effect.getDescription()) {
                            //Component.literal(" ")	创建一个纯文本组件，内容为两个空格
                            //.append(desc)	将副词条描述组件 desc 拼接到两个空格之后
                            //tooltip.add(...)	将拼接好的组件添加到工具提示列表中，最终渲染在游戏界面上
                            tooltip.add(Component.literal("  ").append(desc));
                        }
                        tooltip.add(Component.literal("  ").append(effect.getAuthorNote()));
                    }
                }
            }
            //如果没有任何副词条生效，显示“暂无副词条”
            if (!hasAnyActive) {
                tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.no_modifiers"));
            }
            return; //按 Ctrl 时不显示其他信息
        }

        //正常提示信息
        ToolType type = getActiveType(stack);

        //激活形态
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.active",
                Component.translatable("tooltip.stellarmod_item.tool_core.type." + type.id)));

        List<Material> mats = getMaterialsFromStack(stack);
        if (!mats.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.materials"));
            Map<ResourceLocation, Integer> countMap = new LinkedHashMap<>();
            for (Material mat : mats) {
                countMap.put(mat.itemId(), countMap.getOrDefault(mat.itemId(), 0) + 1);
            }
            for (Map.Entry<ResourceLocation, Integer> entry : countMap.entrySet()) {
                ResourceLocation itemId = entry.getKey();
                int count = entry.getValue();

                //使用物品注册名自动生成翻译键
                Component itemName = Component.translatable("item." + itemId.getNamespace() + "." + itemId.getPath());
                Component displayLine;
                if (count > 1) {
                    displayLine = Component.translatable("tooltip.stellarmod_item.tool_core.material_count", itemName, count);
                } else {
                    displayLine = itemName;
                }
                tooltip.add(Component.literal(" §8- ").append(displayLine));
            }
        }

        int maxDmg = getStoredMaxDamage(stack);
        if (maxDmg > 0) {
            int currentDmg = getStoredDamage(stack);
            tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.durability", maxDmg - currentDmg, maxDmg));
        }

        if (stack.hasTag() && stack.getTag().contains("PreviewRepair")) {
            int repair = stack.getTag().getInt("PreviewRepair");
            tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.will_repair", repair));
        }

        ToolProperties props = getProperties(stack);
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.mining_level", props.miningLevel()));
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.speed", getTotalMiningSpeed(stack, type)));
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.attack", getTotalAttackDamage(stack, type)));

        //提示按住 Ctrl 查看副词条
        tooltip.add(Component.translatable("tooltip.stellarmod_item.tool_core.press_ctrl"));
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

    //在工具损坏时播放音效
    private void playBreakSound(LivingEntity entity) {
        if (entity instanceof Player player) {
            player.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F + player.level().random.nextFloat() * 0.4F);
        }
    }
}