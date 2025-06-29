
package jp.tanakh.bjne.nes;

import java.io.IOException;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Nes {
	public Nes(Renderer r) {
		renderer = r;
		cpu = new Cpu(this);
		apu = new Apu(this);
		ppu = new Ppu(this);
		mbc = new Mbc(this);
		regs = new Regs(this);
		rom = new Rom(this);
		mapper = null;
	}

	public void load(String fname) throws IOException {
		rom.load(fname);
		mapper = MapperMaker.makeMapper(rom.mapperNo(), this);
		if (mapper == null)
			throw new IOException(String.format("unsupported mapper: #%d", rom.mapperNo()));
		reset();
	}

	public boolean checkMapper() {
		return mapper != null;
	}

	public void saveSram(String fname) {
		rom.saveSram(fname);
	}

	public void loadSram(String fname) {
		rom.loadSram(fname);
	}

	public void saveState(String fname) {
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fname))) {
			// Write 4-byte header and 1-byte version
			out.writeBytes("MCES"); // 4-byte magic
			out.writeByte(1);       // version 1 (1 byte)

			out.writeShort(cpu.regPC);
			out.writeByte(cpu.regA);
			out.writeByte(cpu.regX);
			out.writeByte(cpu.regY);
			out.writeByte(cpu.regS);

			out.writeByte(cpu.cFlag);
			out.writeByte(cpu.zFlag);
			out.writeByte(cpu.iFlag);
			out.writeByte(cpu.dFlag);
			out.writeByte(cpu.bFlag);
			out.writeByte(cpu.vFlag);
			out.writeByte(cpu.nFlag);

			out.write(mbc.getRam());

			apu.saveTo(out);
			ppu.saveTo(out);
			regs.saveTo(out);

			if (mapper != null) {
				mapper.saveTo(out);
			}

			System.out.println("✅ Saved emulator state to " + fname);
		} catch (IOException e) {
			System.err.println("❌ Failed to save state to " + fname);
			e.printStackTrace();
		}
	}



	public void loadState(String fname) {
		try (DataInputStream in = new DataInputStream(new FileInputStream(fname))) {
			// Read 4-byte magic
			byte[] magic = new byte[4];
			in.readFully(magic);
			if (!new String(magic).equals("MCES")) {
				throw new IOException("Invalid savestate header");
			}

			int version = in.readUnsignedByte(); // read 1 byte
			if (version != 1) {
				throw new IOException("Unsupported savestate version: " + version);
			}

			cpu.regPC = (short) in.readUnsignedShort();
			cpu.regA = in.readByte();
			cpu.regX = in.readByte();
			cpu.regY = in.readByte();
			cpu.regS = in.readByte();

			cpu.cFlag = in.readByte();
			cpu.zFlag = in.readByte();
			cpu.iFlag = in.readByte();
			cpu.dFlag = in.readByte();
			cpu.bFlag = in.readByte();
			cpu.vFlag = in.readByte();
			cpu.nFlag = in.readByte();

			in.readFully(mbc.getRam());

			apu.loadFrom(in);
			ppu.loadFrom(in);
			regs.loadFrom(in);

			if (mapper != null) {
				mapper.loadFrom(in);
			}

			int op = mbc.read(cpu.regPC) & 0xFF;
			System.out.printf("🧠 Resume at PC=$%04X, opcode=$%02X\n", cpu.regPC & 0xFFFF, op);
			if (op == 0x02 || op == 0x4F || op == 0xFF) {
				System.out.println("🚨 WARNING: CPU will execute invalid opcode!");
			}

			int pc = cpu.regPC & 0xFFFF;
			if (pc < 0x8000 || pc > 0xFFFF) {
				System.out.printf("🚨 PC out of range after load: $%04X\n", pc);
				cpu.regPC = (short) 0x8000;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void reset() {
		// reset rom & mbc first
		rom.reset();
		mbc.reset();

		// reset mapper
		mapper.reset();

		// reset rest
		cpu.reset();
		apu.reset();
		ppu.reset();
		regs.reset();

		renderer.outputMessage("Reset virtual machine ...");
	}

	public void execFrame() {
		// CPU clock is 1.7897725MHz
		// 1789772.5 / 60 / 262 = 113.85...
		// 114 cycles per line?
		// 1789772.5 / 262 / 114 = 59.922 fps ?

		Renderer.ScreenInfo scri = renderer.requestScreen(256, 240);
		Renderer.SoundInfo sndi = renderer.requestSound();
		Renderer.InputInfo inpi = renderer.requestInput(2, 8);

		if (sndi != null) {
			apu.genAudio(sndi);
			renderer.outputSound(sndi);
		}
		if (inpi != null)
			regs.setInput(inpi.buf);

		regs.setVBlank(false, true);
		regs.startFrame();
		for (int i = 0; i < 240; i++) {
			if (mapper != null)
				mapper.hblank(i);
			regs.startScanline();
			if (scri != null)
				ppu.render(i, scri);
			ppu.spriteCheck(i);
			apu.sync();
			cpu.exec(114);
			regs.endScanline();
		}

		if ((regs.getFrameIrq() & 0xC0) == 0)
			cpu.setIrq(true);

		for (int i = 240; i < 262; i++) {
			if (mapper != null)
				mapper.hblank(i);
			apu.sync();
			if (i == 241) {
				regs.setVBlank(true, false);
				cpu.exec(0); // one extra op will execute after VBLANK
				regs.setVBlank(regs.getIsVBlank(), true);
				cpu.exec(114);
			} else
				cpu.exec(114);
		}

		if (scri != null)
			renderer.outputScreen(scri);
	}

	public Rom getRom() {
		return rom;
	}

	public Cpu getCpu() {
		return cpu;
	}

	public Ppu getPpu() {
		return ppu;
	}

	public Apu getApu() {
		return apu;
	}

	public Mbc getMbc() {
		return mbc;
	}

	public Regs getRegs() {
		return regs;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public Mapper getMapper() {
		return mapper;
	}

	private Rom rom;
	private Cpu cpu;
	private Ppu ppu;
	private Apu apu;
	private Mbc mbc;
	private Regs regs;
	private Mapper mapper;

	private Renderer renderer;
}
