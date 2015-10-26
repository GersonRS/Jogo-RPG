package main;

import java.awt.event.KeyEvent;

public class Principal extends Personagem {

	private Inventario inventario;

	public Principal(int x, int y, int width, int height, int numFrames,
			String img) {
		super(x, y, width, height, numFrames, img);
		numFrames = 6;
		direction = 2;
		inventario = new Inventario();
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

	public void maisExp(int mais) {
		exp += mais;
		if(exp > expMax){
			expMax *= 2;
		}
	}

	public Inventario getInventario() {
		return inventario;
	}

}
