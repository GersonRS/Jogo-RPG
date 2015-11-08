package game;

import java.io.IOException;

import core.Interacao;
import core.Personagem;

public class Rival extends Personagem {

	public Rival(int x, int y, int width, int height, int numFrames, String img) throws IOException {
		super(x, y, width, height, numFrames, img);
	}

	@Override
	public void mover(Interacao i) {

	}

}
