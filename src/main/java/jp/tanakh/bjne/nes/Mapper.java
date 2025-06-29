package jp.tanakh.bjne.nes;

import jp.tanakh.bjne.nes.Renderer.SoundInfo;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public interface Mapper {
	int mapperNo();

	void reset();

	void write(short adr, byte dat);

	void hblank(int line);

	void audio(SoundInfo info);

	default void saveTo(DataOutputStream out) throws IOException {}
	default void loadFrom(DataInputStream in) throws IOException {}
}