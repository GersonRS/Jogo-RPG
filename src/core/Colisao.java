package core;

import java.awt.geom.Rectangle2D;
/**
 * 
 * Interface que posibilita a colis�o de um Elemento.
 * 
 */
public interface Colisao {
	
	/**
	 * 
	 * retorna a area de colis�o do elemento. 
	 * 
	 * @return Rectangle2D.Double
	 */
	Rectangle2D.Double getColisao();

}
