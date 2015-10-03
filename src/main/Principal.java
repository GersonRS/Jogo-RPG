package main;

import java.awt.event.KeyEvent;

public class Principal extends Personagem{

	public Principal(int x, int y, int width, int height) {
		super(x, y, width, height, "personagem.png");
		numFrames = 6;
	}

	@Override
	public void mover(Iteracao i) {
		if (i.isPressed(KeyEvent.VK_RIGHT))
			acceleration.x = 0.4;
		else if (i.isPressed(KeyEvent.VK_LEFT))
			acceleration.x = -0.4;
		else if (i.isPressed(KeyEvent.VK_DOWN))
			acceleration.y = 0.4;
		else if (i.isPressed(KeyEvent.VK_UP))
			acceleration.y = -0.4;
	}
}
