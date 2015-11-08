package game;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import core.Interacao;
import core.Personagem;

public class Principal extends Personagem {

	private Inventario inventario;
	protected BufferedImage rosto;
	protected int strength;
	protected int intelligence;
	protected int agility;
	protected int dexterity;
	protected int hp, hpMax;
	protected int mp, mpMax;
	protected int level;
	protected int exp, expMax;
	

	public Principal(int x, int y, int width, int height, int numFrames,
			String img) throws IOException {
		super(x, y, width, height, numFrames, img);
		this.hpMax = 250;
		this.hp = 250;
		this.mpMax = 50;
		this.mp = 50;
		this.level = 1;
		this.expMax = 100;
		this.exp = 0;
		inventario = new Inventario();

		try {
			rosto = ImageIO.read(getClass().getClassLoader().getResource(
					"images/" + img + "Rosto.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mover(Interacao i) {
		if (i.isPressed(KeyEvent.VK_RIGHT))
			acceleration.x = 0.4;
		else if (i.isPressed(KeyEvent.VK_LEFT))
			acceleration.x = -0.4;
		else if (i.isPressed(KeyEvent.VK_DOWN))
			acceleration.y = 0.4;
		else if (i.isPressed(KeyEvent.VK_UP))
			acceleration.y = -0.4;
	}

	public void maisExp(int mais) {
		exp += mais;
		if(exp > expMax){
			expMax *= 2;
		}
	}

	public Inventario getInventario() {
		return inventario;
	}

}
