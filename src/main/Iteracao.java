package main;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

/**
 * 
 * Interface que posibilita iteração com o usuario.
 * 
 */
public interface Iteracao extends KeyListener, MouseListener{
	
	// constantes 
	final int KEY_RELEASED = 0;
	final int KEY_JUST_PRESSED = 1;
	final int KEY_PRESSED = 2;
	
	/**
	 * 
	 * verifica se uma tecla especifica keyId esta precionada. 
	 * 
	 * @param keyId
	 * 		numero keycode da tecla presionada
	 * 
	 * @return boolean
	 */
	boolean isPressed(int keyId);

	/**
	 * 
	 * verifica se uma tecla especifica keyId foi precionada. 
	 * 
	 * @param keyId
	 * 		numero keycode da tecla que foi precionada
	 * 
	 * @return boolean
	 */
	boolean isJustPressed(int keyId);

}
