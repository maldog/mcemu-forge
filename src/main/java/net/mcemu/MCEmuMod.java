package net.mcemu;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

@Mod(MCEmuMod.MOD_ID)
public class MCEmuMod {
    public static final String MOD_ID = "mcemu";
    public static final Logger logger = LogManager.getLogger(MOD_ID);

    public static final String KEY_CATEGORY = "key.categories.mcemu";

    public static final KeyMapping P1NES_LEFT    = new KeyMapping("key.mcemu.p1_left",    GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_RIGHT   = new KeyMapping("key.mcemu.p1_right",   GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_UP      = new KeyMapping("key.mcemu.p1_up",      GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_DOWN    = new KeyMapping("key.mcemu.p1_down",    GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_A       = new KeyMapping("key.mcemu.p1_a",       GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_B       = new KeyMapping("key.mcemu.p1_b",       GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_START   = new KeyMapping("key.mcemu.p1_start",   GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P1NES_SELECT  = new KeyMapping("key.mcemu.p1_select",  GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);

    public static final KeyMapping P2NES_LEFT    = new KeyMapping("key.mcemu.p2_left",    GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_RIGHT   = new KeyMapping("key.mcemu.p2_right",   GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_UP      = new KeyMapping("key.mcemu.p2_up",      GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_DOWN    = new KeyMapping("key.mcemu.p2_down",    GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_A       = new KeyMapping("key.mcemu.p2_a",       GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_B       = new KeyMapping("key.mcemu.p2_b",       GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_START   = new KeyMapping("key.mcemu.p2_start",   GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping P2NES_SELECT  = new KeyMapping("key.mcemu.p2_select",  GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);

    public static final KeyMapping[][] keyDef = {
            { P1NES_A, P1NES_B, P1NES_SELECT, P1NES_START, P1NES_UP, P1NES_DOWN, P1NES_LEFT, P1NES_RIGHT },
            { P2NES_A, P2NES_B, P2NES_SELECT, P2NES_START, P2NES_UP, P2NES_DOWN, P2NES_LEFT, P2NES_RIGHT }
    };

    public MCEmuMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegistry.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        //modEventBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        // MCEmuMod.java, inside constructor
        Path romPath = Path.of(FMLPaths.CONFIGDIR.get().toString(), "mcemu", "roms", "nes");
        try {
            Files.createDirectories(romPath);
        } catch (IOException e) {
            logger.error("Failed to create NES ROM directory", e);
        }

        RomManager.scanRomFolder();
    }


/*
    @Mod.EventBusSubscriber(modid = MCEmuMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class ClientModEvents {

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            for (KeyMapping[] row : MCEmuMod.keyDef) {
                for (KeyMapping key : row) {
                    event.register(key);
                }
            }
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(ModRegistry.TELEVISION_BLOCK.get(), RenderType.translucent());
            });
        }
    }
    */


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        for (int i = 0; i < keyDef.length; i++) {
            for (int j = 0; j < keyDef[i].length; j++) {
                KeyMapping key = keyDef[i][j];
                if (key != null && key.isDown()) {
                    // handle key press
                }
            }
        }
    }
}
