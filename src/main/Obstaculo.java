package main;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class Obstaculo extends Elemento {

	public Obstaculo(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.ativo = true;
		this.visivel = true;
	}

	@Override
	public Rectangle2D.Double getColisao() {
		return pos;
	}

	@Override
	public void mover(Iteracao i) {

	}

	@Override
	public void update(int currentTick) {

	}

	@Override
	public void render(Graphics2D g) {

	}

}
