package com.luolian.stellarmod.server.block.entity;

import com.luolian.stellarmod.client.screen.craftingArea.CraftingAreaBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CraftingAreaBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(8);   //创建8个物品槽位

    private static final int[] INPUT_SLOTS = new int[] { 0, 1 ,2 ,3 ,4 ,5 ,6};  //设置物品输入槽位索引
    private static final int OUTPUT_SLOTS = 7;  //设置物品输出槽位索引

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 78;

    public CraftingAreaBlockEntity(BlockPos pos, BlockState blockstate) {
        super(StellarBlockEntities.CRAFTING_AREA_BE.get(), pos, blockstate);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> CraftingAreaBlockEntity.this.progress;
                    case 1 -> CraftingAreaBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> CraftingAreaBlockEntity.this.progress = pValue;
                    case 1 -> CraftingAreaBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    //当外部（如管道、漏斗）查询 ITEM_HANDLER 能力时，返回 lazyItemHandler 的 cast 版本，即转换为正确的类型。否则，调用父类处理
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    //onLoad 在方块实体加载到世界中时调用。这里用 LazyOptional.of 将 itemHandler 包装成一个有效的 LazyOptional 实例，供 getCapability 使用
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    //当方块实体被移除或能力需要失效时（如方块被破坏），调用 invalidateCaps 使 lazyItemHandler 失效，释放相关资源。
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.stellarmod.crafting_area_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CraftingAreaBlockMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());   //保存自定义数据
        tag.putInt("crafting_area_block.progress", progress);

        super.saveAdditional(tag);  //调用父类保存基类数据
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);    //加载基类数据
        itemHandler.deserializeNBT(tag.getCompound("inventory"));   //反序列化物品栏
        progress = tag.getInt("crafting_area_block.progress");
    }
}
