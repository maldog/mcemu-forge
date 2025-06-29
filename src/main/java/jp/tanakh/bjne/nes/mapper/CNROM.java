package jp.tanakh.bjne.nes.mapper;

import jp.tanakh.bjne.nes.MapperAdapter;
import jp.tanakh.bjne.nes.Nes;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class CNROM extends MapperAdapter {
	public CNROM(Nes n) {
		nes = n;
		reset();
	}

	private int romSize;
	private int chrBank;

	@Override
	public void saveTo(DataOutputStream out) throws IOException {
		out.writeInt(romSize);
		out.writeInt(chrBank);
	}

	@Override
	public void loadFrom(DataInputStream in) throws IOException {
		romSize = in.readInt();
		chrBank = in.readInt();
		for (int i = 0; i < 8; i++)
			nes.getMbc().mapVrom(i, chrBank * 8 + i);
	}


	@Override
	public int mapperNo() {
		return 3;
	}

	@Override
	public void reset() {
		romSize = nes.getRom().romSize();
		for (int i = 0; i < 4; i++)
			nes.getMbc().mapRom(i, i);
		for (int i = 0; i < 8; i++)
			nes.getMbc().mapVrom(i, i);
	}

	@Override
	public void write(short adr, byte dat) {
		chrBank = dat & 0xff;
		for (int i = 0; i < 8; i++)
			nes.getMbc().mapVrom(i, (dat & 0xff) * 8 + i);
	}

	private Nes nes;
}
