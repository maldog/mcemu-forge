package net.mcemu.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;

import net.mcemu.ModRegistry;
import net.mcemu.model.MCEmu;

public class ConsoleBlockEntity extends BlockEntity {

    public ConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.CONSOLE_BLOCK_ENTITY.get(), pos, state);
    }

    private MCEmu emulator;

    public MCEmu getEmulator() {
        return emulator;
    }

    public void insertCartridge(Path romPath) {
        System.out.println("🎮 insertCartridge called with: " + romPath);
        if (emulator != null) emulator.stop();
        emulator = new MCEmu();
        emulator.stop();
        emulator.start(romPath); // this begins NES emulation
    }


    public void tickServer() {
        // This is where NES emulation logic or state updates will go
    }
}
