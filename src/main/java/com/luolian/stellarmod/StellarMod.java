package com.luolian.stellarmod;

//import com.luolian.stellarmod.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StellarMod.MODID)
public class StellarMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "stellarmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Block> test_BLOCK = BLOCKS.register("test", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.COPPER)));
    public static final RegistryObject<Item> testblockitem = ITEMS.register("test",() -> new BlockItem(test_BLOCK.get(),new Item.Properties()));
    public static final RegistryObject<Block> dimension_block = BLOCKS.register("dimension_block", () -> new Block(BlockBehaviour.Properties.of().strength(50.0f).sound(SoundType.STONE)));
    public static final RegistryObject<Item> dimension_block_item = ITEMS.register("dimension_block",() -> new BlockItem(dimension_block.get(),new Item.Properties()));
    public StellarMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
//    public StellarMod(FMLJavaModLoadingContext context) {
//        IEventBus modEventBus = context.getModEventBus();
//        ModItems.register(modEventBus);
//    }
}
