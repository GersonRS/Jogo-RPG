package main;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public interface Iteracao extends KeyListener, MouseListener{
	
	final int KEY_RELEASED = 0;
	final int KEY_JUST_PRESSED = 1;
	final int KEY_PRESSED = 2;
	
	boolean isPressed(int keyId);

	boolean isJustPressed(int keyId);

}
