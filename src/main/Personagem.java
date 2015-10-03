package main;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

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
	protected int strength;
	protected int intelligence;
	protected int agility;
	protected int dexterity;
	protected int hp, hpMax;
	protected int mp, mpMax;
	protected int level;
	protected int exp, expMax;
	protected final int STATE_STANDING = 0;
	protected final int STATE_WALKING = 1;
	protected final int STATE_ATACK = 2;
	private int stepInterval;
	private int lastStepTick;

	public Personagem(int x, int y, int width, int height, String img) {
		super(x, y, width, height);
		this.level = 1;
		this.expMax = 100;
		this.hpMax = 100;
		this.mpMax = 100;
		this.friction = 0.3;
		this.stepInterval = 20;
		this.ativo = true;
		this.visivel = true;
		speed = new Point2D.Double();
		acceleration = new Point2D.Double();
		maxSpeed = new Point2D.Double(2, 2);
		carregaImagem(img);
	}
	
	private void carregaImagem(String img) {
		try {
			image = ImageIO.read(getClass().getClassLoader().getResource(
					"images/" + img));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			if (collidingEntities[ABOVE] != null) {
				pos.y = collidingEntities[ABOVE].getColisao().getY()
						+ collidingEntities[ABOVE].getColisao().getHeight() - getColisao().getHeight() - 0.1;
				speed.y = 0;
				acceleration.y = 0;
			} else {
				speed.y += friction;
				if (speed.y > 0) {
					speed.y = 0;
				}
			}
		} else if (speed.y > 0) {
			if (collidingEntities[BELOW] != null) {
				pos.y = collidingEntities[BELOW].getColisao().getY() - pos.height + 0.1;
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
			if (collidingEntities[LEFT] != null) {
				pos.x = collidingEntities[LEFT].pos.x
						+ collidingEntities[LEFT].pos.width - 0.1;
				speed.x = 0;
				acceleration.x = 0;
			} else {
				speed.x += friction;
				if (speed.x > 0) {
					speed.x = 0;
				}
			}
		} else if (speed.x > 0) {
			if (collidingEntities[RIGHT] != null) {
				pos.x = collidingEntities[RIGHT].pos.x - pos.width + 0.1;
				speed.x = 0;
				acceleration.x = 0;
			} else {
				speed.x -= friction;
				if (speed.x < 0) {
					speed.x = 0;
				}
			}
		}
		pos.x += speed.x;
		pos.y += speed.y;
		acceleration.x = 0;
		acceleration.y = 0;

		if (speed.y != 0 || speed.x != 0) {
			if (speed.y < 0)
				direction = (ABOVE);
			else if (speed.y > 0)
				direction = (BELOW);
			else if (speed.x < 0)
				direction = (LEFT);
			else if (speed.x > 0)
				direction = (RIGHT);
			if (currentTick - lastStepTick > stepInterval) {
				lastStepTick = currentTick;
			}
			state = (STATE_WALKING);
		} else {
			state = (STATE_STANDING);
			animates = (2);
		}
		if (currentTick % 8 == 0 && state == STATE_WALKING)
			animates++;
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(image, (int) (pos.x), (int) (pos.y),
				(int) (pos.x + pos.width), (int) (pos.y + pos.height),
				(int) ((animates % numFrames) * pos.width),
				(int) (direction * pos.height),
				(int) (((animates % numFrames) * pos.width) + pos.width),
				(int) ((direction * pos.height) + pos.height), null);
	}
	
	@Override
	public Rectangle2D getColisao() {
		Rectangle2D.Double r = new Rectangle2D.Double();
		r.x = pos.x;
		r.y = pos.y+pos.height/2;
		r.width = pos.width;
		r.height = pos.height/2;
		return r;
	}
}