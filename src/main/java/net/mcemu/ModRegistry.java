package net.mcemu;

import net.mcemu.block.ConsoleBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.mcemu.block.TelevisionBlock;
import net.mcemu.block.ConsoleBlock;
import net.mcemu.item.CartridgeItem;
import net.mcemu.block.entity.TelevisionEntity;

public class ModRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MCEmuMod.MOD_ID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MCEmuMod.MOD_ID);

    public static final RegistryObject<Block> CONSOLE_BLOCK =
            BLOCKS.register("console_block", () ->
                    new ConsoleBlock(BlockBehaviour.Properties.of()
                            .strength(5.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));


    public static final RegistryObject<Item> CONSOLE_ITEM =
            ITEMS.register("console_block", () ->
                    new BlockItem(CONSOLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> CARTRIDGE_ITEM =
            ITEMS.register("cartridge", () ->
                    new CartridgeItem(new Item.Properties()));

    //**** ENTITIES SECTION
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MCEmuMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ConsoleBlockEntity>> CONSOLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("console_block", () ->
                    BlockEntityType.Builder.of(ConsoleBlockEntity::new, CONSOLE_BLOCK.get())
                            .build(null));

    //End of Entities

    public static final RegistryObject<Block> TELEVISION_BLOCK =
            BLOCKS.register("television", () ->
                    new TelevisionBlock(BlockBehaviour.Properties.of()
                            .strength(5.0F, 6.0F)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()));

    public static final RegistryObject<BlockEntityType<TelevisionEntity>> TELEVISION_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("television", () ->
                    BlockEntityType.Builder.of(TelevisionEntity::new, TELEVISION_BLOCK.get())
                            .build(null));

    public static final RegistryObject<Item> TELEVISION_ITEM =
            ITEMS.register("television", () ->
                    new BlockItem(TELEVISION_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus); // 👈 this is new
    }
}
