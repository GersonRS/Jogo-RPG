package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import coreBase.Elemento;

public abstract class PecaGeometrica extends Elemento {

	protected int id;
	protected Polygon poligono;
	protected final static Stroke stroke = new BasicStroke(1.5f);
	protected Color cor;
	private Random r;
	protected BufferedImage image;
	protected int tamanho;

	public PecaGeometrica(int x, int y, int id) {
		super(x, y, 32, 32);
		this.r = new Random();
		this.poligono = new Polygon();
		this.tamanho = r.nextInt(10) + 1;
		this.id = id;
		this.setAtivo(true);
		this.visivel = true;
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
			cor = (Color.GREEN);
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
		image = new BufferedImage((int) getPos().width, (int) getPos().height,
				BufferedImage.TYPE_INT_ARGB);

	}

	protected abstract double calculaArea();

	public int getTamanho() {
		return tamanho;
	}

	public int getId() {
		return id;
	}

	@Override
	public Rectangle2D.Double getColisao() {
		return getPos();
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(image, (int) getPos().x, (int) getPos().y, null);
	}
}