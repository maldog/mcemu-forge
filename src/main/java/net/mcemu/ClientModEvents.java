package net.mcemu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // ✅ correct
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mcemu.client.renderer.TelevisionRenderer;

@Mod.EventBusSubscriber(modid = MCEmuMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {


    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModRegistry.TELEVISION_BLOCK.get(), RenderType.translucent());

            // Register the renderer manually (Forge 1.20.1 method)
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                    ModRegistry.TELEVISION_BLOCK_ENTITY.get(), TelevisionRenderer::new
            );
        });
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping[] row : MCEmuMod.keyDef) {
            for (KeyMapping key : row) {
                event.register(key);
            }
        }
    }





}
