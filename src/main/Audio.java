package main;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Audio {
	static private Audio instance;
	private HashMap<String, AudioClip> clips;

	private Audio() {
		clips = new HashMap<String, AudioClip>();
	}

	static public Audio getInstance() {
		if (instance == null) {
			instance = new Audio();
		}
		return instance;
	}

	public AudioClip loadAudio(String fileName) throws IOException {
		URL url = getClass().getClassLoader().getResource("audios/" + fileName);
		if (url == null) {
			throw new RuntimeException("O áudio /" + fileName
					+ " não foi encontrado.");
		} else {
			if (clips.containsKey(fileName)) {
				return clips.get(fileName);
			} else {
				AudioClip clip = Applet.newAudioClip(getClass().getClassLoader().getResource(
						"audios/" + fileName));
				clips.put(fileName, clip);
				return clip;
			}
		}
	}

	public void audio() throws UnsupportedAudioFileException, IOException,
			LineUnavailableException {
		URL url = getClass().getClassLoader()
				.getResource("audios/" + "som.wav");
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(url);
		Clip clip = AudioSystem.getClip();
		clip.open(audioInputStream);
		FloatControl gainControl = (FloatControl) clip
				.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(-20.0f);
		clip.start();
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
}
