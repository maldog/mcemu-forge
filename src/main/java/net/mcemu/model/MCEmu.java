package net.mcemu.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.tanakh.bjne.nes.Nes;
import net.minecraftforge.fml.loading.FMLPaths;

public class MCEmu {

    private Nes nes;
    private EmuRenderer renderer;
    private Path romPath; // NEW: keep track of current ROM path

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> f;

    private byte[] lastState;

    private volatile int frameDelay = 0;  // volatile = safe across threads


    public MCEmu() {
        renderer = new EmuRenderer();
        nes = new Nes(renderer);
        paintUsage();
    }

    public boolean isRunning() {
        return f != null && !f.isCancelled();
    }

    public void start(Path path) {
        if (load(path)) {
            romPath = path; // store the ROM path
            f = executorService.scheduleAtFixedRate(() -> {
                try {
                    if (frameDelay-- > 0) {
                        return;  // 🔁 wait until delay expires
                    }
                    nes.execFrame();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, 0, 16, TimeUnit.MILLISECONDS);
        }
    }

    private boolean load(Path path) {
        try {
            nes.load(path.toAbsolutePath().toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            paintUsage();
            return false;
        }
    }

    public void reset(Path path) {
        if (load(path)) {
            romPath = path;
            nes.reset();
        }
    }

    public void stop() {
        if (isRunning()) {
            f.cancel(true);
        }
    }

    public BufferedImage getImage() {
        return renderer.getImage();
    }

    private void paintUsage() {
        Graphics g = renderer.getImage().getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, renderer.getImage().getWidth(), renderer.getImage().getHeight());
        g.setColor(Color.WHITE);
        g.drawString("No cartridge inserted", 8, 20);
        g.drawString("Get cartridges from your creative menu", 8, 40);
        g.drawString("or put roms into the folder if you haven't.", 8, 56);
        g.drawString("ROM Folder: $MC/config/mcenu/roms/nes/", 8, 76);
        g.setFont(g.getFont().deriveFont(48f));
        g.drawString("MCEmuMod", 8, 222);
    }

    // --- Save State System ---

    private Path getSaveStatePath(int slot) {
        if (romPath == null) {
            return null;
        }

        String fileName = romPath.getFileName().toString();
        String baseName = fileName.replaceFirst("\\.nes$", "");
        String safeRomId = baseName.replaceAll("[^a-zA-Z0-9]", "_");
        Path savePath = FMLPaths.CONFIGDIR.get()
                .resolve("mcemu")
                .resolve("savestates")
                .resolve(safeRomId)
                .resolve(Integer.toString(slot));

        return savePath.resolve("state.bin");
    }


    public void saveStateToDisk(int slot) {
        Path path = getSaveStatePath(slot);
        if (path == null) {
            System.out.println("No ROM loaded — can't save state.");
            return;
        }

        try {
            Files.createDirectories(path.getParent());
            nes.saveState(path.toString());
            System.out.println("Saved state to " + path);
        } catch (IOException e) {
            System.err.println("Failed to save state to " + path);
            e.printStackTrace();
        }
    }

    private void paintMessage(String msg) {
        Graphics g = renderer.getImage().getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, renderer.getImage().getWidth(), renderer.getImage().getHeight());
        g.setColor(Color.RED);
        g.drawString(msg, 8, 20);
    }


    public void loadStateFromDisk(int slot) {
        Path path = getSaveStatePath(slot);
        if (path == null || !Files.exists(path)) {
            paintMessage("No save state found");
            return;
        }

        try {
            nes.loadState(path.toString());
            frameDelay = 1;  // 🔁 Skip 1 frame before resuming
            System.out.println("✅ State loaded. Will resume next tick.");
        } catch (Exception e) {
            System.err.println("Failed to load state from " + path);
            e.printStackTrace();
        }
    }


}
