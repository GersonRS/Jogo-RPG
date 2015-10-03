package main;

import java.util.Random;

public class Inimigo extends Personagem {

	int num;
	Random r;

	public Inimigo(int x, int y, int width, int height) {
		super(x, y, width, height, "Monstro.png");
		r = new Random();
		numFrames = 4;
		direction = 2;
	}

	@Override
	public void update(int currentTick) {
		if (currentTick % 160 == 0) {
			num = r.nextInt(10);
		}
		switch (num) {
		case 0:{
			acceleration.x = 0.4;
			break;			
		}
		case 1:{
			acceleration.x = -0.4;
			break;			
		}
		case 2:{
			acceleration.y = 0.4;
			break;			
		}
		case 3:{
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
