package coreBase;

import java.awt.Point;
import java.awt.event.KeyListener;

import javax.swing.event.MouseInputListener;

/**
 * 
 * Interface que posibilita interação com o usuario.
 * 
 */
public interface Interacao extends KeyListener, MouseInputListener{
	
	/**
	 * 
	 * Constantes usadas para verificar o estado da interação. 
	 * 
	 */
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

	/**
	 * 
	 * verifica se uma tecla especifica keyId foi solta. 
	 * 
	 * @param keyId
	 * 		numero keycode da tecla que foi precionada
	 * 
	 * @return boolean
	 */
	boolean isReleased(int keyId);
	
	/**
	 * 
	 * verifica se um botão do mouse especifico buttonId foi precionado. 
	 * 
	 * @param buttonId
	 * 		numero do button do mouse que foi precionado
	 * 
	 * @return boolean
	 */
	boolean isMousePressed(int buttonId);
	
	/**
	 * 
	 * retora a posição em que o mouse esta. 
	 * 
	 * @return Point
	 */
	Point getMousePos();
}
