package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public abstract class PecaGeometrica extends Elemento {

	protected int id;
	protected Polygon poligono;
	protected final static Stroke stroke = new BasicStroke(1.5f);
	protected Color cor;
	private Random r;
	protected BufferedImage image;

	public PecaGeometrica(int x, int y, int id) {
		super(x, y, 32, 32);
		this.poligono = new Polygon();
		this.id = id;
		this.ativo = true;
		this.visivel = true;
		this.r = new Random();
		switch (r.nextInt(10)) {
		case 0: {
			cor = (Color.BLUE);
			break;
		}
		case 1: {
			cor = (Color.RED);
			break;
		}
		case 2: {
			cor = (Color.LIGHT_GRAY);
			break;
		}
		case 3: {
			cor = (Color.WHITE);
			break;
		}
		case 4: {
			cor = (Color.YELLOW);
			break;
		}
		case 5: {
			cor = (Color.CYAN);
			break;
		}
		case 6: {
			cor = (Color.DARK_GRAY);
			break;
		}
		case 7: {
			cor = (Color.MAGENTA);
			break;
		}
		case 8: {
			cor = (Color.PINK);
			break;
		}
		case 9: {
			cor = (Color.ORANGE);
			break;
		}
		default:
			break;
		}
		image = new BufferedImage((int) pos.width,
				(int) pos.height, BufferedImage.TYPE_INT_ARGB);
		
	}

	@Override
	public Rectangle2D.Double getColisao() {
		return pos;
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(image, (int) pos.x, (int) pos.y, null);
	}
}