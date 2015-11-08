package core;

import java.awt.geom.Rectangle2D;
/**
 * 
 * Interface que posibilita a colisão de um Elemento.
 * 
 */
public interface Colisao {
	
	/**
	 * 
	 * retorna a area de colisão do elemento. 
	 * 
	 * @return Rectangle2D.Double
	 */
	Rectangle2D.Double getColisao();

}
