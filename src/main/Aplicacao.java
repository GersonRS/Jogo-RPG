package main;

import java.awt.Graphics2D;

public class Aplicacao extends Game {

	public static void main(String[] args) {
		new Thread(new Aplicacao()).start();
	}

	@Override
	public void onLoad() {
		loadCenario("se","tileset.png");
		addElementoPrincipal(new Principal(100, 100, 23, 55));
		addElemento(new Inimigo(200, 200, 27, 57));
		addElemento(new Inimigo(300, 300, 27, 57));
		currentCenario("se");
		configLayerBase("se", "Base 1");
		configLayerBase("se", "Base 2");
		configLayerSuperficie("se", "Superficie 1");
		playSound("som.wav");
	}

	@Override
	public void onUpdate(int currentTick) {
		updateElementos(currentTick);
	}

	@Override
	public void onRender(Graphics2D g) {
		renderCenarioBase(g);
		renderElementos(g);
		renderCenarioSuperficie(g);
	}
}
