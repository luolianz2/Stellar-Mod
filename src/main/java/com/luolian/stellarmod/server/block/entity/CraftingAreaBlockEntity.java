package com.luolian.stellarmod.server.block.entity;

import com.luolian.stellarmod.client.screen.craftingArea.CraftingAreaBlockMenu;
import com.luolian.stellarmod.server.data.itemcore.Material;
import com.luolian.stellarmod.server.data.itemcore.MaterialManager;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import com.luolian.stellarmod.server.recipe.CraftingAreaRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingAreaBlockEntity extends BlockEntity implements MenuProvider {

    //槽位处理
    private final ItemStackHandler itemHandler = new ItemStackHandler(8) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            //输出槽（索引7）禁止放入任何物品
            if (slot == 7) return false;
            //核心槽（索引3）只允许放入工具核心
            if (slot == 3) return stack.getItem() instanceof ToolCoreItem;
            //特殊材料槽（索引6）只允许放入钻石
            if (slot == 6) return stack.is(Items.DIAMOND);
            //其他输入槽无限制
            return true;
        }

        @Override
        public int getSlotLimit(int slot) {
            //核心槽最多放入 1 个
            if (slot == 3) return 1;
            return super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            //当任意槽位内容变化时，标记方块实体已更改并触发客户端同步
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);//3 = 1 | 2：同时通知邻居数据更新 + 客户端重绘
            }
        }
    };

    //常量：输出槽索引
    private static final int OUTPUT_SLOT = 7;

    //用于向外部暴露物品能力的 LazyOptional
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    //用于菜单同步数据的 ContainerData（无进度条，仅占位）
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 0;
        }
    };

    //用于记录上一次输入槽状态的哈希值，判断是否需要更新预览
    private long lastInputHash = -1;

    public CraftingAreaBlockEntity(BlockPos pos, BlockState state) {
        super(StellarBlockEntities.CRAFTING_AREA_BE.get(), pos, state);
    }

    //每 tick 更新逻辑
    public static void tick(Level level, BlockPos pos, BlockState state, CraftingAreaBlockEntity entity) {
        if (level.isClientSide()) return;

        long currentHash = entity.getInputHash();
        if (currentHash != entity.lastInputHash) {  //如果和上一次状态不同
            entity.updatePreview();                //更新输出槽预览物品
            entity.lastInputHash = currentHash;
            setChanged(level, pos, state);
        }
    }

    //计算输入槽（0~6）的简单哈希值，用于判断内容是否变化
    private long getInputHash() {
        long hash = 0;  //初始化哈希值为 0
        for (int i = 0; i < 7; i++) {   //遍历前 7 个槽位
            ItemStack stack = itemHandler.getStackInSlot(i);    //获取当前槽位的物品栈
            if (!stack.isEmpty()) { //如果槽位不为空，则参与哈希计算。空槽位不贡献任何信息，这意味着清空一个槽位也会改变哈希值（因为原本有物品现在没有了）
                //将当前哈希值乘以 31，然后加上物品注册 ID 的哈希码，ForgeRegistries.ITEMS.getKey(stack.getItem()):获取该物品的唯一标识符，如minecraft:iron_ingot
                //再取其 hashCode()，确保不同物品产生不同的哈希贡献
                hash = 31 * hash + ForgeRegistries.ITEMS.getKey(stack.getItem()).hashCode();
                //再次乘以 31，加上物品的数量。这保证了同一物品的不同堆叠数量也会产生不同的哈希值
                hash = 31 * hash + stack.getCount();
            }
        }
        //返回最终计算出的 64 位长整型哈希值
        return hash;
    }

    //更新输出槽的预览物品（带有预览标记的工具核心）
    public void updatePreview() {
        Optional<CraftingAreaRecipe> recipe = getCurrentRecipe();
        //如果没有匹配配方或核心槽没有工具核心，清空输出槽
        if (recipe.isEmpty() || !(itemHandler.getStackInSlot(3).getItem() instanceof ToolCoreItem)) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, ItemStack.EMPTY);
            return;
        }

        CraftingAreaRecipe r = recipe.get();
        ItemStack preview = itemHandler.getStackInSlot(3).copy();
        preview.setCount(1);

        //模拟添加材料属性（不实际消耗）
        for (int i = 0; i < 7; i++) {
            if (i == 3) continue; //跳过核心槽
            int consume = r.getConsumeCount(i);
            if (consume <= 0) continue;

            ItemStack materialStack = itemHandler.getStackInSlot(i);
            if (materialStack.isEmpty()) continue;

            ResourceLocation materialId = ForgeRegistries.ITEMS.getKey(materialStack.getItem());
            if (materialId == null) continue;

            Material material = MaterialManager.getMaterial(materialId);
            if (material != null) {
                for (int t = 0; t < consume; t++) {
                    ToolCoreItem.addMaterialToStack(preview, material);
                }
            }
        }

        //添加预览标记，便于客户端显示提示
        preview.getOrCreateTag().putBoolean("Preview", true);
        itemHandler.setStackInSlot(OUTPUT_SLOT, preview);
    }

    //实际执行合成：消耗材料，生成带有新属性的工具核心
    public ItemStack assemble() {
        Optional<CraftingAreaRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty() || !(itemHandler.getStackInSlot(3).getItem() instanceof ToolCoreItem)) {
            return ItemStack.EMPTY;
        }

        CraftingAreaRecipe r = recipe.get();

        //第一步：收集要添加的材料（在消耗物品之前）
        List<Material> materialsToAdd = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (i == 3) continue;
            int consume = r.getConsumeCount(i);
            if (consume <= 0) continue;

            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id == null) continue;

            Material mat = MaterialManager.getMaterial(id);
            if (mat != null) {
                for (int t = 0; t < consume; t++) {
                    materialsToAdd.add(mat);
                }
            }
        }

        //第二步：消耗材料
        for (int i = 0; i < 7; i++) {
            if (i == 3) continue;
            int consume = r.getConsumeCount(i);
            if (consume > 0) {
                itemHandler.extractItem(i, consume, false);
            }
        }

        //第三步：生成新的工具核心
        ItemStack result = itemHandler.getStackInSlot(3).copy();
        result.setCount(1);
        for (Material mat : materialsToAdd) {
            ToolCoreItem.addMaterialToStack(result, mat);
        }
        result.removeTagKey("Preview"); // 移除预览标记
        return result;
    }

    //检查当前是否存在有效配方（核心槽存在且输出槽为空）
    private boolean hasRecipe() {
        Optional<CraftingAreaRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return false;
        if (!(itemHandler.getStackInSlot(3).getItem() instanceof ToolCoreItem)) return false;
        //输出槽必须为空，因为生成的是不可堆叠的工具核心
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty();
    }

    //获取当前匹配的配方
    private Optional<CraftingAreaRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(7);
        for (int i = 0; i < 7; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return level.getRecipeManager().getRecipeFor(CraftingAreaRecipe.Type.INSTANCE, inventory, level);
    }

    //物品掉落
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    //Capability 暴露（能力系统，允许方块、实体、物品等动态地提供某些功能接口，如物品存储、能量存储、流体处理，供其他模组或原版机制，如漏斗、管道调用）
    //当漏斗、管道或玩家打开 GUI 时，它们会调用 getCapability 来获取对应的接口实例
    //LazyOptional 允许安全地延迟提供能力实例，并在方块被破坏时集体失效，避免外部持有过期的引用
    //此处具体为:外部请求某个能力时调用，如果请求的是 ITEM_HANDLER，则返回包裹了 itemHandler 的 LazyOptional；否则交给父类处理（可能返回其他能力或无）
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {    //ITEM_HANDLER代表“物品处理器”能力，即该方块拥有一个可以被外部访问的物品栏
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    //方块实体被加载到世界时调用。这里初始化 lazyItemHandler，使用 LazyOptional.of(() -> itemHandler) 创建一个延迟求值的包装器
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    //方块实体被移除或卸载时调用。必须调用 lazyItemHandler.invalidate() 使之前分发的 LazyOptional 失效，防止内存泄漏或对已卸载方块的非法访问
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    //当玩家打开 GUI 时，游戏会调用此方法获取界面标题
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.stellarmod.crafting_area_block");
    }

    /**当玩家右键点击方块时，服务端会调用此方法创建对应的容器菜单
     * @param containerId 服务端分配的唯一容器 ID
     * @param playerInventory 打开 GUI 的玩家的物品栏
     * @param player 玩家实体
     * @return 返回自定义的 CraftingAreaBlockMenu 实例，将方块实体自身（this）和同步数据 data 传入，以便菜单可以访问物品栏和配方逻辑
     */
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CraftingAreaBlockMenu(containerId, playerInventory, this, this.data);
    }

    //暴露物品栏处理器给菜单
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    //NBT 持久化
    //BlockEntity 基类会自动处理一些基础数据，但自定义的物品栏内容不会自动保存，必须通过重写这两个方法来读写 NBT

    //当区块被卸载或世界保存时，游戏会调用此方法
    //tag 是即将写入磁盘的 NBT 复合标签
    //itemHandler.serializeNBT() 将整个物品栏（8 个槽位）序列化为一个 CompoundTag，包含每个槽位的物品、数量、NBT 数据
    //tag.put("inventory", ...) 将物品栏数据存入父标签的 "inventory" 键下
    //必须调用 super.saveAdditional(tag)，确保基类保存其自身数据
    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    //当区块被加载或方块实体从 NBT 创建时调用。
    //tag 是从磁盘读取的 NBT 复合标签。
    //调用 super.load(tag) 让基类先恢复其数据。
    //tag.getCompound("inventory") 提取之前保存的 "inventory" 子标签，然后通过 itemHandler.deserializeNBT(...) 恢复物品栏内容。
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }
}