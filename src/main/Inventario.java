package main;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Inventario {

	private final int NUM_MAX = 6;
	private ArrayList<PecaGeometrica> pecasgeometricas;
	private ArrayList<Point2D> posicoes;
	private BufferedImage image;

	public Inventario() {
		pecasgeometricas = new ArrayList<PecaGeometrica>();
		posicoes = new ArrayList<Point2D>();
		posicoes.add(new Point(621, 235));
		posicoes.add(new Point(621, 235 + 85));
		posicoes.add(new Point(621, 235 + 85 + 83));
		posicoes.add(new Point(621 + 75, 235));
		posicoes.add(new Point(621 + 75, 235 + 85));
		posicoes.add(new Point(621 + 75, 235 + 85 + 83));
		try {
			image = ImageIO.read(getClass().getClassLoader().getResource(
					"images/inventario.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(PecaGeometrica peca) {
		if (pecasgeometricas.size() < NUM_MAX) {
			pecasgeometricas.add(peca);
		}
	}
	
	public void remove(int id){
		for (int i = 0; i < pecasgeometricas.size(); i++) {
			PecaGeometrica p = pecasgeometricas.get(i);
			if(p.id==id){
				pecasgeometricas.remove(i);
			}
		}
	}

	public ArrayList<PecaGeometrica> getPecasgeometricas() {
		return pecasgeometricas;
	}

	public void renderInventario(Graphics2D g) {
		for (int i = 0; i < pecasgeometricas.size(); i++) {
			g.drawImage(pecasgeometricas.get(i).image, (int) posicoes.get(i)
					.getX(), (int) posicoes.get(i).getY(), null);
		}
		g.drawImage(image, 596, 211, null);
	}

	public int getNUM_MAX() {
		return NUM_MAX;
	}

}
