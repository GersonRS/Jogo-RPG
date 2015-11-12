package coreBase;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * 
 * Controla todo audio do jogo. esta classe 
 * prover um controle para executar algum audio.
 * 
 */
public class Audio {
	static private Audio instance;
	private HashMap<String, AudioClip> clips;

	/**
	 * Crie um novo Audio.
	 */
	private Audio() {
		clips = new HashMap<String, AudioClip>();
	}

	/**
	 * 
	 * metodo Singleton que retorna uma �nica inst�ncia de um Audio. 
	 * 
	 * @return Audio
	 */
	static public Audio getInstance() {
		if (instance == null)
			instance = new Audio();
		return instance;
	}

	/**
	 * 
	 * carrega um arquivo de audio. 
	 * 
	 * @param fileName
	 * 		nome do arquivo de audio.
	 * 
	 * @return AudioClip
	 */
	public AudioClip loadAudio(String fileName) throws IOException {
		URL url = getClass().getClassLoader().getResource("audios/" + fileName);
		if (url == null) {
			throw new RuntimeException("O Audio " + fileName
					+ " n�o foi encontrado.");
		} else {
			if (clips.containsKey(fileName)) {
				return clips.get(fileName);
			} else {
				AudioClip clip = Applet.newAudioClip(getClass()
						.getClassLoader().getResource("audios/" + fileName));
				clips.put(fileName, clip);
				return clip;
			}
		}
	}
}
