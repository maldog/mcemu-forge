package net.mcemu;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import net.minecraftforge.fml.loading.FMLPaths;

public class RomManager {
    public static final List<String> ROM_FILE_NAMES = new ArrayList<>();

    public static void scanRomFolder() {
        Path romPath = FMLPaths.CONFIGDIR.get().resolve("mcemu/roms/nes");
        try {
            Files.createDirectories(romPath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(romPath, "*.nes")) {
                for (Path path : stream) {
                    ROM_FILE_NAMES.add(path.getFileName().toString());
                }
            }
        } catch (IOException e) {
            MCEmuMod.logger.error("Failed to scan ROMs", e);
        }
    }
}
