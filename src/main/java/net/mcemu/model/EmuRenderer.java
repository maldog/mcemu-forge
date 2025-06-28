package net.mcemu.model;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.mcemu.MCEmuMod;
import jp.tanakh.bjne.nes.Nes;
import jp.tanakh.bjne.nes.Renderer;


public class EmuRenderer implements Renderer {
    private static final int SCREEN_WIDTH = 256;
    private static final int SCREEN_HEIGHT = 240;

    private static final int SAMPLE_RATE = 48000;
    private static final int BPS = 16;
    private static final int CHANNELS = 2;
    private static final int BUFFER_FRAMES = 2;

    private static final int FPS = 60;
    private static final int SAMPLES_PER_FRAME = SAMPLE_RATE / FPS;

    private ScreenInfo scri = new ScreenInfo();
    private SoundInfo sndi = new SoundInfo();
    public static InputInfo inpi = new InputInfo();

    static {
        inpi.buf = new int[16];
    }

    //private BufferedImage image = new BufferedImage(SCREEN_WIDTH,
    //        SCREEN_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);

    //private BufferedImage image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);

    private BufferedImage image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);


    public BufferedImage getImage() {
        return image;
    }

    private int lineBufferSize;

    public EmuRenderer() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, BPS, CHANNELS, true,
                false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        int bufSamples = SAMPLES_PER_FRAME;

        sndi.bps = 16;
        sndi.buf = new byte[bufSamples * (BPS / 8) * CHANNELS];
        sndi.ch = 2;
        sndi.freq = SAMPLE_RATE;
        sndi.sample = bufSamples;
    }

    @Override
    public void outputMessage(String msg) {
        MCEmuMod.logger.debug(msg);
    }

    @Override
    public ScreenInfo requestScreen(int width, int height) {
        if (!(scri.width == width && scri.height == height)) {
            scri.width = width;
            scri.height = height;
            scri.buf = new byte[3 * width * height];
            scri.pitch = 3 * width;
            scri.bpp = 24;
        }
        return scri;
    }

    @Override
    public void outputScreen(ScreenInfo info) {
        int[] pixels = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                int srcIndex = y * SCREEN_WIDTH + x;
                int destIndex = y * SCREEN_WIDTH + (SCREEN_WIDTH - 1 - x); // flip X

                // TRY THIS ORDER FIRST (Standard RGB)
                //int r = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 0]);
                //int g = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 1]);
                //int b = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 2]);

                int b = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 0]);
                int g = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 1]);
                int r = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 2]);

                // OR TRY THIS ORDER if the colors look weird (BGR fallback)
                // int b = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 0]);
                // int g = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 1]);
                // int r = Byte.toUnsignedInt(info.buf[srcIndex * 3 + 2]);

                pixels[destIndex] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }

    }


    @Override
    public SoundInfo requestSound() {
        if (getSoundBufferState() <= 0)
            return sndi;
        else
            return null;
    }

    @Override
    public void outputSound(SoundInfo info) {
        //line.write(info.buf, 0, info.sample * (info.bps / 8) * info.ch);
    }

    public int getSoundBufferState() {
		/*int rest = (lineBufferSize - line.available()) / (sndi.bps / 8)
				/ sndi.ch;
		if (rest < SAMPLES_PER_FRAME * BUFFER_FRAMES)
			return -1;
		if (rest == SAMPLES_PER_FRAME * BUFFER_FRAMES)
			return 0*/
        return 1;
    }

    @Override
    public InputInfo requestInput(int padCount, int buttonCount) {
        return inpi;
    }

    public static void main(String[] args) throws Exception {
        EmuRenderer renderer = new EmuRenderer();
        Nes nes = new Nes(renderer);
        nes.load(Paths.get(args[0]).toAbsolutePath().toString());

        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(renderer.image, 0, 0, null);
            }
        };

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> { nes.execFrame(); panel.repaint(); }, 0, (int) (1000f / FPS), TimeUnit.MICROSECONDS);

        panel.setPreferredSize(new Dimension(renderer.image.getWidth(), renderer.image.getHeight()));
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
