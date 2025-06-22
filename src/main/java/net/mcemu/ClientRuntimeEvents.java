package net.mcemu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mcemu.block.entity.TelevisionEntity;
import net.mcemu.block.ConsoleBlockEntity;

import java.awt.image.BufferedImage;




@Mod.EventBusSubscriber(modid = MCEmuMod.MOD_ID, value = Dist.CLIENT)
public class ClientRuntimeEvents {

    //To improve framerate
    private static final long NANOS_PER_FRAME = (long)(1_000_000_000.0 / 60.0988);
    private static long lastTime = System.nanoTime();
    private static long accumulatedTime = 0;

    /*
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        BlockPos center = mc.player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            BlockEntity be = mc.level.getBlockEntity(pos);

            if (be instanceof ConsoleBlockEntity console && console.getEmulator() != null && console.getEmulator().isRunning()) {
                BufferedImage img = console.getEmulator().getImage();
                if (img == null) continue;

                int[] frame = new int[256 * 240];
                img.getRGB(0, 0, 256, 240, frame, 0, 256);

                // Find a nearby TV to send this frame to
                for (BlockPos tvPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
                    BlockEntity tvBe = mc.level.getBlockEntity(tvPos);
                    if (tvBe instanceof TelevisionEntity tv) {
                        tv.updateFrame(frame);
                    }
                }
            }
        }
    }
    */

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long now = System.nanoTime();
        long delta = now - lastTime;
        lastTime = now;
        accumulatedTime += delta;

        BlockPos center = mc.player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            BlockEntity be = mc.level.getBlockEntity(pos);

            if (be instanceof ConsoleBlockEntity console && console.getEmulator() != null && console.getEmulator().isRunning()) {
                BufferedImage img = null;

                // Step emulator as many frames as needed to catch up to real time
                while (accumulatedTime >= NANOS_PER_FRAME) {
                    img = console.getEmulator().getImage(); // This also steps the emulator
                    accumulatedTime -= NANOS_PER_FRAME;
                }

                if (img == null) continue;

                int[] frame = new int[256 * 240];
                img.getRGB(0, 0, 256, 240, frame, 0, 256);

                // Send to nearby TVs
                for (BlockPos tvPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
                    BlockEntity tvBe = mc.level.getBlockEntity(tvPos);
                    if (tvBe instanceof TelevisionEntity tv) {
                        tv.updateFrame(frame);
                    }
                }
            }
        }
    }



}
