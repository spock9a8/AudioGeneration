package com.gb.main;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Main {

	private static int SAMPLE_RATE = 22050;

	public static void main(String[] args) {
		createOutput();
//		play(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		play(200, 10000);
	}

	private static AudioFormat format = null;
	private static SourceDataLine line = null;
	public static boolean playing = true;

	private static void createOutput() {
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false);
		System.out.println("Audio format: " + format);
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				System.out.println("Line does not support: " + format);
				System.exit(0);
			}
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	private static void play(int fre, int time) {
		int timeLength;
		timeLength = time;
		int freqToPlay;
		freqToPlay = fre;

		int maxSize = (int) Math.round((SAMPLE_RATE * format.getFrameSize()) / MIN_FREQ);
		byte[] samples = new byte[maxSize];
		line.start();
		double volume;
		float timer = System.nanoTime() / 1000000;
		while (playing) {
			if ((System.nanoTime() / 1000000) + timeLength > timer) {
				timer += timeLength;
				sendNote(freqToPlay, 1.0, samples);
				playing = false;
			}
		}

		line.drain();
		line.stop();
		line.close();

	}

	private static double MAX_AMPLITUDE = 32760;
	private static int MIN_FREQ = 250;
	private static int MAX_FREQ = 2000;

	private static void sendNote(int freq, double volLevel, byte[] samples) {
		if ((volLevel < 0.0) || (volLevel > 1.0)) {
			System.out.println("Volume level should be between 0 and 1, using 0.9");
			volLevel = 0.9;
		}
		double amplitude = volLevel * MAX_AMPLITUDE;
		int numSamplesInWave = (int) Math.round(((double) SAMPLE_RATE) / freq);
		int idx = 0;
		for (int i = 0; i < numSamplesInWave; i++) {
			double sine = Math.sin(((double) i / numSamplesInWave) * 2.0 * Math.PI);
			int sample = (int) (sine * amplitude);
			// left sample of stereo
			samples[idx + 0] = (byte) (sample & 0xFF); // low byte
			samples[idx + 1] = (byte) ((sample >> 8) & 0xFF); // high byte
			// right sample of stereo (identical to left)
			samples[idx + 2] = (byte) (sample & 0xFF);
			samples[idx + 3] = (byte) ((sample >> 8) & 0xFF);
			idx += 4;
		}
		// send out the samples (the single note)
		int offset = 0;
		while (offset < idx)
			offset += line.write(samples, offset, idx - offset);
	}

}
