package main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Quadrado extends PecaGeometrica {

	public Quadrado(int x, int y, int id) {
		super(x, y, id);
		poligono.addPoint(5, 5);
		poligono.addPoint(26, 5);
		poligono.addPoint(26, 26);
		poligono.addPoint(5, 26);
		Graphics2D g2 = (Graphics2D) image.getGraphics();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.clip(poligono);
		g2.setColor(cor);
		g2.fillRect(0, 0, 32, 32);
		g2.setStroke(stroke);
	}
	

	@Override
	protected double calculaArea() {
		return Math.pow(tamanho, 2);
	}


	@Override
	public void mover(Iteracao i) {

	}

	@Override
	public void update(int currentTick) {

	}

}
