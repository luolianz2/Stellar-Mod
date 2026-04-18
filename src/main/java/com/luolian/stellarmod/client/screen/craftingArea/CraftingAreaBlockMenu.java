package com.luolian.stellarmod.client.screen.craftingArea;

import com.luolian.stellarmod.client.screen.StellarMenuTypes;
import com.luolian.stellarmod.server.block.StellarBlocks;
import com.luolian.stellarmod.server.block.entity.CraftingAreaBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CraftingAreaBlockMenu extends AbstractContainerMenu {

    public final CraftingAreaBlockEntity blockEntity;
    private final Level level;
    private final ContainerData containerData;

    //客户端构造器（从网络包创建）
    public CraftingAreaBlockMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(0));
    }

    //服务端构造器
    public CraftingAreaBlockMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerData containerData) {
        super(StellarMenuTypes.CRAFTING_AREA_MENU.get(), containerId);
        checkContainerSize(inventory, 8); //原版方法，实际未使用但保留
        this.blockEntity = (CraftingAreaBlockEntity) entity;
        this.level = inventory.player.level();
        this.containerData = containerData;

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        //添加方块实体槽位
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // 输入槽 0~6
            this.addSlot(new SlotItemHandler(handler, 0, 22, 17));
            this.addSlot(new SlotItemHandler(handler, 1, 52, 17));
            this.addSlot(new SlotItemHandler(handler, 2, 10, 41));
            this.addSlot(new SlotItemHandler(handler, 3, 37, 41)); //核心槽
            this.addSlot(new SlotItemHandler(handler, 4, 64, 41));
            this.addSlot(new SlotItemHandler(handler, 5, 22, 65));
            this.addSlot(new SlotItemHandler(handler, 6, 52, 65)); //工具槽

            //输出槽（索引7），使用自定义槽位
            this.addSlot(new OutputSlot(handler, 7, 139, 41, blockEntity));
        });
        addDataSlots(containerData);
    }

    //自定义输出槽：处理点击合成与放置限制
    private static class OutputSlot extends SlotItemHandler {
        private final CraftingAreaBlockEntity blockEntity;

        public OutputSlot(IItemHandler handler, int index, int x, int y, CraftingAreaBlockEntity blockEntity) {
            super(handler, index, x, y);
            this.blockEntity = blockEntity;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; //禁止放入任何物品
        }

        @Override
        public boolean mayPickup(Player player) {
            return true;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            //执行实际合成，获得新核心
            ItemStack result = blockEntity.assemble();
            if (!result.isEmpty()) {
                //播放铁砧使用音效
                player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                //将合成结果设置为玩家当前手持的物品（鼠标拿着的物品）
                player.containerMenu.setCarried(result);
                //清空输出槽的预览物品
                blockEntity.getItemHandler().extractItem(getSlotIndex(), 64, false);
            }
            //更新预览（可能还有下一个候选材料）
            blockEntity.updatePreview();
            blockEntity.setChanged();
        }
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 8;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        // 特殊处理：如果点击的是输出槽（索引7），阻止 Shift 快速移动，防止绕过合成逻辑
        if (pIndex == TE_INVENTORY_FIRST_SLOT_INDEX + 7) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    //判断打开该容器的玩家是否仍然满足继续使用该容器的条件，不满足（距离过远或方块被破坏）则关闭GUI
    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, StellarBlocks.CRAFTING_AREA_BLOCK.get());
    }

    //玩家物品栏槽位
    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 10 + col * 18, 90 + row * 18));
            }
        }
    }

    //玩家快捷栏槽位
    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 10 + i * 18, 148));
        }
    }
}