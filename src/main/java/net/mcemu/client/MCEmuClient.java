package net.mcemu.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

import net.mcemu.ModRegistry;
import net.mcemu.client.renderer.TelevisionEntityRenderer;
import net.mcemu.model.EmuRenderer;
import net.mcemu.model.MCEmu;

@Mod.EventBusSubscriber(modid = "mcemu", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MCEmuClient {

    public static MCEmu model;

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModRegistry.TELEVISION_BLOCK_ENTITY.get(), TelevisionEntityRenderer::new);
            ItemBlockRenderTypes.setRenderLayer(ModRegistry.CONSOLE_BLOCK.get(), RenderType.cutout());
            model = new MCEmu();
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 8; j++) {
                    EmuRenderer.inpi.buf[i * 8 + j] = net.mcemu.MCEmuMod.keyDef[i][j].isDown() ? 1 : 0;
                }
            }
        }
    }
}

