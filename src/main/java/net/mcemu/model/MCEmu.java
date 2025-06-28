package net.mcemu.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.tanakh.bjne.nes.Nes;


public class MCEmu {

    private Nes nes;
    private EmuRenderer renderer;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> f;

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
            //sort out a speed issue after resume from sleep.
            //f = executorService.scheduleAtFixedRate(nes::execFrame, 0, (int)(1000f / 60), TimeUnit.MILLISECONDS);
            //executorService.scheduleAtFixedRate(nes::execFrame, 0, 16, TimeUnit.MILLISECONDS);
            //f = executorService.scheduleWithFixedDelay(nes::execFrame, 0, 16, TimeUnit.MILLISECONDS);
            f = executorService.scheduleAtFixedRate(() -> {
                try {
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
}
