package core;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class Elemento implements Acoes{

	private Rectangle2D.Double pos;
	private Elemento[] collidingEntities;
	private boolean ativo;
	protected boolean visivel;
	protected int id;

	public Elemento(int x, int y, int width, int height) {
		this.pos = new Rectangle2D.Double(x, y, width, height);
		this.collidingEntities = new Elemento[4];
	}

	public abstract void update(int currentTick);

	public abstract void render(Graphics2D g);

	public Rectangle2D.Double getPos() {
		return pos;
	}

	public Elemento[] getCollidingEntities() {
		return collidingEntities;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	
}