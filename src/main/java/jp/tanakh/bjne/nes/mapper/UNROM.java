package jp.tanakh.bjne.nes.mapper;

import jp.tanakh.bjne.nes.MapperAdapter;
import jp.tanakh.bjne.nes.Nes;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class UNROM extends MapperAdapter {
	public UNROM(Nes n) {
		nes = n;
		reset();
	}

	private int currentBank;
	private byte[] chrRam = new byte[8192]; // UNROM typically uses 8KB CHR-RAM

	@Override
	public void saveTo(DataOutputStream out) throws IOException {
		out.writeInt(currentBank);
		out.write(nes.getMbc().getChrRam()); // ✅ Save CHR-RAM directly from MBC
	}

	@Override
	public void loadFrom(DataInputStream in) throws IOException {
		currentBank = in.readInt();

		byte[] chrRam = nes.getMbc().getChrRam();
		if (chrRam != null) {
			in.readFully(chrRam); // ✅ Restore CHR-RAM into MBC safely
		} else {
			System.out.println("⚠️ Warning: CHR-RAM is null during UNROM load.");
		}

		// Restore PRG-ROM banks
		nes.getMbc().mapRom(0, currentBank * 2);
		nes.getMbc().mapRom(1, currentBank * 2 + 1);

		int romSize = nes.getRom().romSize();
		nes.getMbc().mapRom(2, (romSize - 1) * 2);
		nes.getMbc().mapRom(3, (romSize - 1) * 2 + 1);
	}





	@Override
	public int mapperNo() {
		return 2;
	}

	@Override
	public void reset() {
		int romSize = nes.getRom().romSize();
		currentBank = 0;
		nes.getMbc().mapRom(0, currentBank * 2);
		nes.getMbc().mapRom(1, currentBank * 2 + 1);
		nes.getMbc().mapRom(2, (romSize - 1) * 2);
		nes.getMbc().mapRom(3, (romSize - 1) * 2 + 1);
	}


	@Override
	public void write(short adr, byte dat) {
		currentBank = dat & 0xff;
		nes.getMbc().mapRom(0, dat * 2);
		nes.getMbc().mapRom(1, dat * 2 + 1);
	}

	private Nes nes;
}
