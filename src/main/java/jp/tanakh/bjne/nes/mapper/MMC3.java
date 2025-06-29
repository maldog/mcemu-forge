package jp.tanakh.bjne.nes.mapper;

import jp.tanakh.bjne.nes.MapperAdapter;
import jp.tanakh.bjne.nes.Nes;
import jp.tanakh.bjne.nes.Ppu;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
/**
 * Mapper 4: MMC3
 */
public class MMC3 extends MapperAdapter {
	public MMC3(Nes n) {
		nes = n;
	}

	@Override
	public void saveTo(DataOutputStream out) throws IOException {
		romSize = nes.getRom().romSize(); // ✅ ensure it's set
		out.writeInt(romSize);
		out.writeInt(cmd);
		out.writeBoolean(prgSwap);
		out.writeBoolean(chrSwap);
		out.writeInt(irqCounter);
		out.writeInt(irqLatch);
		out.writeBoolean(irqEnable);
		for (int i = 0; i < 2; i++) out.writeInt(prgPage[i]);
		for (int i = 0; i < 8; i++) out.writeInt(chrPage[i]);
	}

	@Override
	public void loadFrom(DataInputStream in) throws IOException {
		romSize = in.readInt();

		if (romSize <= 0 || romSize > 1024) {
			System.out.println("❌ Invalid romSize loaded: " + romSize);
			romSize = 1;  // fallback to prevent crash
		}

		cmd = in.readInt();
		prgSwap = in.readBoolean();
		chrSwap = in.readBoolean();
		irqCounter = in.readInt();
		irqLatch = in.readInt();
		irqEnable = in.readBoolean();

		for (int i = 0; i < 2; i++) {
			prgPage[i] = in.readInt();
			if (prgPage[i] < 0 || prgPage[i] >= romSize * 2) {
				System.out.println("⚠️ Clamping invalid prgPage[" + i + "] from " + prgPage[i]);
				prgPage[i] = 0;
			}
		}
		for (int i = 0; i < 8; i++) {
			chrPage[i] = in.readInt();
			if (chrPage[i] < 0) chrPage[i] = 0;
		}

		setRom();
		setVrom();

		System.out.printf("✅ MMC3 restored: PC=$%04X, PRG=[%d,%d], CMD=%d\n",
				nes.getCpu().regPC & 0xFFFF, prgPage[0], prgPage[1], cmd);
	}




	@Override
	public int mapperNo() {
		return 4;
	}

	@Override
	public void reset() {
		romSize = nes.getRom().romSize();
		prgPage[0] = 0;
		prgPage[1] = 1;
		chrPage[0] = 0;
		chrPage[1] = 1;
		chrPage[2] = 2;
		chrPage[3] = 3;
		chrPage[4] = 4;
		chrPage[5] = 5;
		chrPage[6] = 6;
		chrPage[7] = 7;

		prgSwap = false;
		chrSwap = false;

		setRom();
		setVrom();
	}

	private static boolean _bit(int x, int n) {
		return ((x >> n) & 1) != 0;
	}

	@Override
	public void write(short adr, byte bdat) {
		int dat = bdat & 0xff;
		switch (adr & 0xE001) {
		case 0x8000:
			cmd = dat & 7;
			prgSwap = _bit(dat, 6);
			chrSwap = _bit(dat, 7);
			break;

		case 0x8001:
			switch (cmd) {
			case 0: // Select 2 1K VROM pages at PPU $0000
				chrPage[0] = dat & 0xfe;
				chrPage[1] = (dat & 0xfe) + 1;
				setVrom();
				break;
			case 1: // Select 2 1K VROM pages at PPU $0800
				chrPage[2] = dat & 0xfe;
				chrPage[3] = (dat & 0xfe) + 1;
				setVrom();
				break;
			case 2: // Select 1K VROM pages at PPU $1000
				chrPage[4] = dat;
				setVrom();
				break;
			case 3: // Select 1K VROM pages at PPU $1400
				chrPage[5] = dat;
				setVrom();
				break;
			case 4: // Select 1K VROM pages at PPU $1800
				chrPage[6] = dat;
				setVrom();
				break;
			case 5: // Select 1K VROM pages at PPU $1C00
				chrPage[7] = dat;
				setVrom();
				break;
			case 6: // Select first switchable ROM page
				prgPage[0] = dat;
				setRom();
				break;
			case 7: // Select second switchable ROM page
				prgPage[1] = dat;
				setRom();
				break;
			}
			break;

		case 0xA000:
			if (!nes.getRom().isFourScreen())
				nes.getPpu().setMirroring(
						(dat & 1) != 0 ? Ppu.MirrorType.HOLIZONTAL
								: Ppu.MirrorType.VERTICAL);
			break;
		case 0xA001:
			if ((dat & 0x80) != 0)
				; // enable SRAM
			else
				; // disable SRAM
			break;

		case 0xC000:
			irqCounter = dat;
			break;
		case 0xC001:
			irqLatch = dat;
			break;

		case 0xE000:
			irqEnable = false;
			irqCounter = irqLatch;
			break;
		case 0xE001:
			irqEnable = true;
			break;
		}
	}

	@Override
	public void hblank(int line) {
		if (irqEnable && line >= 0 && line < 239 && nes.getRegs().drawEnabled()) {

			if ((irqCounter--) == 0) {
				irqCounter = irqLatch;
				nes.getCpu().setIrq(true);
			}
		}
	}

	private void setRom() {
		if (romSize <= 0) {
			System.out.println("setRom() called with invalid romSize=" + romSize);
			return;
		}
		int rom0, rom1, rom2, rom3;
		if (prgSwap) {
			rom0 = (romSize - 1) * 2;
			rom1 = prgPage[1];
			rom2 = prgPage[0];
			rom3 = (romSize - 1) * 2 + 1;
		} else {
			rom0 = prgPage[0];
			rom1 = prgPage[1];
			rom2 = (romSize - 1) * 2;
			rom3 = (romSize - 1) * 2 + 1;
		}

		System.out.printf("🧩 setRom() PRG=[%d %d %d %d]\n", rom0, rom1, rom2, rom3);

		nes.getMbc().mapRom(0, rom0);
		nes.getMbc().mapRom(1, rom1);
		nes.getMbc().mapRom(2, rom2);
		nes.getMbc().mapRom(3, rom3);
	}


	private void setVrom() {
		for (int i = 0; i < 8; i++)
			nes.getMbc().mapVrom((i + (chrSwap ? 4 : 0)) % 8, chrPage[i]);
	}

	private int romSize;

	private int cmd;
	private boolean prgSwap, chrSwap;

	private int irqCounter, irqLatch;
	private boolean irqEnable;

	private int[] prgPage = new int[2];
	private int[] chrPage = new int[8];

	private Nes nes;
}
