package main;

import java.util.Random;

public class Cacadores extends Personagem {

	int num = 5;
	Random r;

	public Cacadores(int x, int y, int width, int height, int numFrames) {
		super(x, y, width, height, numFrames, "Monstro.png");
		r = new Random();
		direction = 2;
	}

	@Override
	public void update(int currentTick) {
		if (currentTick % 160 == 0) {
			num = r.nextInt(10);
		}
		switch (num) {
		case 0: {
			acceleration.x = 0.4;
			break;
		}
		case 1: {
			acceleration.x = -0.4;
			break;
		}
		case 2: {
			acceleration.y = 0.4;
			break;
		}
		case 3: {
			acceleration.y = -0.4;
			break;
		}

		default:
			break;
		}
		super.update(currentTick);
	}

	@Override
	public void mover(Iteracao i) {

	}
}
