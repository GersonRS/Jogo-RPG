package core;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * 
 * Elemento base do jogo. Esta classe prove os principais atributos 
 * para os demais elementos especializados deste elemento.
 * 
 */
public abstract class Elemento implements Acoes {

	private Rectangle2D.Double pos;
	private Elemento[] collidingEntities;
	private boolean ativo;
	protected boolean visivel;
	protected int id;

	/**
	 * 
	 * Crie um novo Elemento.
	 * 
	 * @param x
	 *            posi��o x do elemento
	 * @param y
	 *            posi��o y do elemento
	 * @param width
	 *            largura do elemento
	 * @param height
	 *            altura do elemento
	 *            
	 */
	public Elemento(int x, int y, int width, int height) {
		this.pos = new Rectangle2D.Double(x, y, width, height);
		this.collidingEntities = new Elemento[4];
	}

	/**
	 * 
	 * metodo de atualiza��o do elemento.
	 * 
	 * @param currentTick
	 *            numero de vezes que o jogo fez itera��es.
	 * 
	 */
	public abstract void update(int currentTick);

	/**
	 * 
	 * metodo que desenha o elemento na tela.
	 * 
	 * @param g
	 *            Graphics2D no qual o elemento ira ser desenhado.
	 * 
	 */
	public abstract void render(Graphics2D g);

	/**
	 * 
	 * retorna a posi��o atual do elemento.
	 * 
	 * @return Rectangle2D.Double
	 */
	public Rectangle2D.Double getPos() {
		return pos;
	}

	/**
	 * 
	 * retorna outros elementos com os quais este elemento esta colidindo.
	 * 
	 * @return Elemento[]
	 */
	public Elemento[] getCollidingEntities() {
		return collidingEntities;
	}

	/**
	 * 
	 * retorna o estado ativo do elemento.
	 * 
	 * @return boolean
	 */
	public boolean isAtivo() {
		return ativo;
	}

	/**
	 * 
	 * altera o estado ativo do elemento.
	 * 
	 * @param ativo
	 *            valor do novo estado ativo do elemento.
	 * 
	 */
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	/**
	 * 
	 * retorna o estado visivel do elemento.
	 * 
	 * @return boolean
	 */
	public boolean isVisivel() {
		return visivel;
	}

	/**
	 * 
	 * altera o estado visivel do elemento.
	 * 
	 * @param ativo
	 *            valor do novo estado visivel do elemento.
	 * 
	 */
	public void setVisivel(boolean visivel) {
		this.visivel = visivel;
	}

}