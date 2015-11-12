package coreBase;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * 
 * Classe que representa um obstaculo para algum elemento.
 * 
 */
public class Obstaculo extends Elemento {

	/**
	 * Crie um novo Obstaculo.
	 */
	public Obstaculo(int x, int y, int width, int height, int id) {
		super(x, y, width, height);
		this.id = id;
		this.setAtivo(true);
		this.visivel = true;
	}

	@Override
	public Rectangle2D.Double getColisao() {
		return getPos();
	}

	@Override
	public void mover(Interacao i) {

	}

	@Override
	public void update(int currentTick) {

	}

	@Override
	public void render(Graphics2D g) {

	}

}
