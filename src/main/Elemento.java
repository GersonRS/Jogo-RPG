package main;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class Elemento implements Acoes{

	protected Rectangle2D.Double pos;
	protected Elemento[] collidingEntities;
	protected boolean ativo,visivel;
	protected int id;

	public Elemento(int x, int y, int width, int height) {
		pos = new Rectangle2D.Double(x, y, width, height);
		collidingEntities = new Elemento[4];
	}

	public abstract void update(int currentTick);

	public abstract void render(Graphics2D g);
	
}