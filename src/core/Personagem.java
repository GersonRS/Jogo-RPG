package core;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Elemento Personagem especializado do jogo.
 * 
 */
public abstract class Personagem extends Elemento {

	protected BufferedImage image;
	protected final int ABOVE = 0;
	protected final int RIGHT = 1;
	protected final int BELOW = 2;
	protected final int LEFT = 3;
	protected Point2D.Double speed;
	protected Point2D.Double acceleration;
	protected Point2D.Double maxSpeed;
	protected double friction;
	protected int animates;
	protected int numFrames;
	protected int direction;
	protected int state;
	protected final int STATE_STANDING = 0;
	protected final int STATE_WALKING = 1;
	protected final int STATE_ATACK = 2;
	private int stepInterval;
	private int lastStepTick;

	/**
	 * 
	 * Crie um novo Elemento.
	 * 
	 * @param x
	 *            posi��o x do personagem
	 * @param y
	 *            posi��o y do personagem
	 * @param width
	 *            largura do personagem
	 * @param height
	 *            altura do personagem
	 * @param numframes
	 *            numero de frames que existe na sprite do personagem
	 * @param img
	 *            nome da imagem da sprite do personagem
	 *            
	 */
	public Personagem(int x, int y, int width, int height, int numFrames,
			String img) throws IOException {
		super(x, y, width, height);
		this.numFrames = numFrames;
		this.friction = 0.3;
		this.stepInterval = 20;
		this.setAtivo(true);
		this.visivel = true;
		speed = new Point2D.Double();
		acceleration = new Point2D.Double();
		maxSpeed = new Point2D.Double(2, 2);
		carregaImagem(img);
	}

	/**
	 * 
	 * metodo que carrega a imagem da sprite do personagem.
	 * 
	 * @param img
	 *            nome da imagem.
	 * 
	 */
	private void carregaImagem(String img) throws IOException {
		image = ImageIO.read(getClass().getClassLoader().getResource(
				"images/" + img + ".png"));
	}

	@Override
	public void update(int currentTick) {
		speed.x += acceleration.x;
		if (speed.x < -maxSpeed.x) {
			speed.x = -maxSpeed.x;
		} else if (speed.x > maxSpeed.x) {
			speed.x = maxSpeed.x;
		}
		speed.y += acceleration.y;
		if (speed.y < -maxSpeed.y) {
			speed.y = -maxSpeed.y;
		} else if (speed.y > maxSpeed.y) {
			speed.y = maxSpeed.y;
		}
		//
		if (speed.y < 0) {
			if (getCollidingEntities()[ABOVE] != null) {
				getPos().y = getCollidingEntities()[ABOVE].getColisao().getY()
						+ getCollidingEntities()[ABOVE].getColisao()
								.getHeight() - getColisao().getHeight() - 0.1;
				speed.y = 0;
				acceleration.y = 0;
			} else {
				speed.y += friction;
				if (speed.y > 0) {
					speed.y = 0;
				}
			}
		} else if (speed.y > 0) {
			if (getCollidingEntities()[BELOW] != null) {
				getPos().y = getCollidingEntities()[BELOW].getColisao().getY()
						- getPos().height + 0.1;
				speed.y = 0;
				acceleration.y = 0;
			} else {
				speed.y -= friction;
				if (speed.y < 0) {
					speed.y = 0;
				}
			}
		}
		if (speed.x < 0) {
			if (getCollidingEntities()[LEFT] != null) {
				getPos().x = getCollidingEntities()[LEFT].getPos().x
						+ getCollidingEntities()[LEFT].getPos().width - 0.1;
				speed.x = 0;
				acceleration.x = 0;
			} else {
				speed.x += friction;
				if (speed.x > 0) {
					speed.x = 0;
				}
			}
		} else if (speed.x > 0) {
			if (getCollidingEntities()[RIGHT] != null) {
				getPos().x = getCollidingEntities()[RIGHT].getPos().x
						- getPos().width + 0.1;
				speed.x = 0;
				acceleration.x = 0;
			} else {
				speed.x -= friction;
				if (speed.x < 0) {
					speed.x = 0;
				}
			}
		}
		getPos().x += speed.x;
		getPos().y += speed.y;
		acceleration.x = 0;
		acceleration.y = 0;

		if (speed.y != 0 || speed.x != 0) {
			if (speed.y < 0)
				direction = ABOVE;
			else if (speed.y > 0)
				direction = BELOW;
			else if (speed.x < 0)
				direction = LEFT;
			else if (speed.x > 0)
				direction = RIGHT;
			if (currentTick - lastStepTick > stepInterval) {
				lastStepTick = currentTick;
			}
			state = (STATE_WALKING);
		} else {
			state = (STATE_STANDING);
			animates = (2);
		}
		if (currentTick % 4 == 0 && state == STATE_WALKING)
			animates++;
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(
				image,
				(int) (getPos().x),
				(int) (getPos().y),
				(int) (getPos().x + getPos().width),
				(int) (getPos().y + getPos().height),
				(int) ((animates % numFrames) * getPos().width),
				(int) (direction * getPos().height),
				(int) (((animates % numFrames) * getPos().width) + getPos().width),
				(int) ((direction * getPos().height) + getPos().height), null);
	}

	@Override
	public Rectangle2D.Double getColisao() {
		Rectangle2D.Double r = new Rectangle2D.Double();
		r.x = getPos().x;
		r.y = getPos().y + getPos().height / 2;
		r.width = getPos().width;
		r.height = getPos().height / 2;
		return r;
	}

}