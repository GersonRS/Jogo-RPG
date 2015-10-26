package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Hud {

	private BufferedImage hud, hp, mp;
	private Principal principal;
	private final static Stroke stroke = new BasicStroke(1.5f);

	public Hud(Principal principal) {
		this.principal = principal;
		hp = new BufferedImage(110, 110, BufferedImage.TYPE_4BYTE_ABGR);
		mp = new BufferedImage(110, 110, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g2 = hp.getGraphics();
		g2.setColor(Color.red);
		g2.fillRect(0, 0, hp.getWidth(), hp.getHeight());
		g2 = mp.getGraphics();
		g2.setColor(Color.blue);
		g2.fillRect(0, 0, mp.getWidth(), mp.getHeight());

		try {
			hud = ImageIO.read(getClass().getClassLoader().getResource(
					"images/hud inventario.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pintaHud(Graphics2D g) {		
		g.drawImage(hud, 0, 0, null);
		principal.getInventario().renderInventario(g);
	}

	public static Image getRoundImage(Image imageSource, int x, int y) {
		int width = imageSource.getWidth(null);
		int height = imageSource.getHeight(null);

		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Ellipse2D elipc = new Ellipse2D.Double(0, 0, 110, 110);

		Graphics2D g2 = (Graphics2D) image.getGraphics();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.clip(elipc);
		g2.drawImage(imageSource, x, y, null);

		g2.setStroke(stroke);
		g2.setColor(Color.LIGHT_GRAY);
		g2.draw(elipc);

		return image;
	}
}