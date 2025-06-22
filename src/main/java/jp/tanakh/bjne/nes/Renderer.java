package jp.tanakh.bjne.nes;

public interface Renderer {
	class ScreenInfo {
		public byte[] buf;
		public int width;
		public int height;
		public int pitch;
		public int bpp;
	}

	class SoundInfo {
		public byte[] buf;
		public int freq;
		public int bps;
		public int ch;
		public int sample;
	}

	class InputInfo {
		public int[] buf;
	}

	ScreenInfo requestScreen(int width, int height);

	SoundInfo requestSound();

	InputInfo requestInput(int padCount, int buttonCount);

	void outputScreen(ScreenInfo info);

	void outputSound(SoundInfo info);

	void outputMessage(String msg);
}
