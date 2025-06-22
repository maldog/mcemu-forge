package net.mcemu;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MCEmuMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCEmuMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MCEMU_TAB =
            TABS.register("mcemu_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("MCEmu"))
                    .icon(() -> new ItemStack(ModRegistry.CONSOLE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModRegistry.CONSOLE_ITEM.get());
                        output.accept(ModRegistry.TELEVISION_ITEM.get());

                        for (int i = 0; i < RomManager.ROM_FILE_NAMES.size(); i++) {
                            ItemStack stack = new ItemStack(ModRegistry.CARTRIDGE_ITEM.get());
                            stack.getOrCreateTag().putInt("CustomModelData", i + 1);
                            output.accept(stack);
                        }
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
