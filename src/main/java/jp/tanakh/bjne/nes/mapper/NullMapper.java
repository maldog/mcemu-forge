package jp.tanakh.bjne.nes.mapper;

import jp.tanakh.bjne.nes.MapperAdapter;
import jp.tanakh.bjne.nes.Nes;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class NullMapper extends MapperAdapter {
	public NullMapper(Nes n){
	}

	@Override
	public void saveTo(DataOutputStream out) throws IOException {
		// Nothing to save
	}

	@Override
	public void loadFrom(DataInputStream in) throws IOException {
		// Nothing to load
	}

	@Override
	public int mapperNo() {
		return 0;
	}
}
