package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Aplicacao extends Game1 {

	ArrayList<Missao> missoes;
	Hud hud;
	boolean isDialogo = false;
	ArrayList<String> dialogo;
	BufferedImage rosto;
	Rectangle2D.Double minimap;

	public Aplicacao() {
		missoes = new ArrayList<Missao>();
		minimap = new Rectangle2D.Double();
	}

	@Override
	public void onLoad() {
		loadCenario("cidade");
		loadCenario("floresta 1");
		loadCenario("floresta 2");
		loadCenario("floresta 3");
		loadCenario("floresta 4");
		loadCenario("floresta 5");
		loadCenario("caverna 1");
		addTeleport("cidade", "floresta 1", 6);
		addTeleport("floresta 1", "cidade", 6);
		addTeleport("floresta 1", "floresta 5", 5);
		addTeleport("floresta 1", "floresta 2", 3);
		addTeleport("floresta 2", "floresta 1", 3);
		addTeleport("floresta 2", "floresta 4", 4);
		addTeleport("floresta 2", "floresta 3", 5);
		addTeleport("floresta 3", "floresta 2", 5);
		addTeleport("floresta 3", "floresta 4", 2);
		addTeleport("floresta 4", "floresta 3", 2);
		addTeleport("floresta 4", "floresta 2", 4);
		addTeleport("floresta 4", "floresta 5", 7);
		addTeleport("floresta 4", "caverna 1", 6);
		addTeleport("floresta 4", "caverna 1", 3);
		addTeleport("floresta 5", "floresta 1", 5);
		addTeleport("floresta 5", "floresta 4", 7);
		addElementoPrincipal("cidade", new Principal(180, 100, 23, 55, 6,
				"personagem"));
		configLayers();
		// addNPCs();
		// addPecasGeometricas();
		hud = new Hud(getElementoPrincipal());
		dialogo = new ArrayList<String>();
		currentCenario("cidade");
		// playSoundLoop("som.wav");
	}

	@Override
	public void onUpdate(int currentTick) {
		double x = elemento.pos.x;
		double y = elemento.pos.y;
		if (!isDialogo) {
			for (int j = 0; j < getElementoPrincipal().collidingEntities.length; j++) {
				if (getElementoPrincipal().collidingEntities[j] != null)
					if (getElementoPrincipal().collidingEntities[j] instanceof NPC) {
						if (isJustPressed(KeyEvent.VK_ENTER)) {
							isDialogo = true;
							rosto = ((NPC) getElementoPrincipal().collidingEntities[j]).rosto;
							String s = ((NPC) getElementoPrincipal().collidingEntities[j])
									.getDialogo(this);
							if (s.contains("\n")) {
								StringTokenizer linhas = new StringTokenizer(s,
										"\n");
								while (linhas.hasMoreTokens()) {
									dialogo.add(linhas.nextToken());
								}
							} else
								dialogo.add(s);
						}
					} else if (getElementoPrincipal().collidingEntities[j] instanceof PecaGeometrica) {
						if (isJustPressed(KeyEvent.VK_ENTER)) {
							if (getElementoPrincipal().getInventario()
									.getPecasgeometricas().size() < getElementoPrincipal()
									.getInventario().getNUM_MAX())
								getElementoPrincipal()
										.getInventario()
										.add((PecaGeometrica) getElementoPrincipal().collidingEntities[j]);
							getElementoPrincipal().collidingEntities[j].ativo = false;
						}
					}
			}
			updateElementos(currentTick);
		} else {
			if (isJustPressed(KeyEvent.VK_ESCAPE)) {
				isDialogo = false;
				dialogo.clear();
				rosto = null;
			}
		}

		minimap.x = cenarios.get(currentCenario).getPos().x * -1;
		minimap.y = cenarios.get(currentCenario).getPos().y * -1;
		minimap.width = cenarios.get(currentCenario).getPos().x * -1 + width
				- width / 3;
		minimap.height = cenarios.get(currentCenario).getPos().y * -1 + height
				- height / 3;

		if (elemento.pos.x >= width / 3
				&& elemento.pos.x <= cenarios.get(currentCenario).getPos().width
						- width / 3 - 14)
			cenarios.get(currentCenario).getPos().x -= elemento.pos.x - x;
		if (elemento.pos.y >= height / 3
				&& elemento.pos.y <= cenarios.get(currentCenario).getPos().height
						- height / 3 - 23)
			cenarios.get(currentCenario).getPos().y -= elemento.pos.y - y;

		// ajeitar o maapa
		if (cenarios.get(currentCenario).getPos().x > 0) {
			cenarios.get(currentCenario).getPos().x = 0;
		}
		if (cenarios.get(currentCenario).getPos().y > 0) {
			cenarios.get(currentCenario).getPos().y = 0;
		}
		if (cenarios.get(currentCenario).getPos().x < (cenarios.get(
				currentCenario).getPos().width
				- width / 3 - 14 - width / 3)
				* -1) {
			cenarios.get(currentCenario).getPos().x = (cenarios.get(
					currentCenario).getPos().width
					- width / 3 - 14 - width / 3)
					* -1;
		}
		if (cenarios.get(currentCenario).getPos().y < (cenarios.get(
				currentCenario).getPos().height
				- height / 3 - 23 - height / 3)
				* -1) {
			cenarios.get(currentCenario).getPos().y = (cenarios.get(
					currentCenario).getPos().height
					- height / 3 - 23 - height / 3)
					* -1;
		}

		// sair do mapa
		if (elemento.pos.x < 0
				|| elemento.pos.x + elemento.pos.width > cenarios.get(
						currentCenario).getPos().width) {
			elemento.pos.x = x;
		}
		if (elemento.pos.y < 0
				|| elemento.pos.y + elemento.pos.height > cenarios.get(
						currentCenario).getPos().height) {
			elemento.pos.y = y;
		}
	}

	@Override
	public void onRender(Graphics2D g) {
		renderCenarioBase(g);
		renderElementos(g);
		renderCenarioSuperficie(g);
	}

	@Override
	protected void onRenderHud(Graphics2D g) {
		g.drawImage(tela, 580, 42, 770, 200, (int) minimap.x, (int) minimap.y,
				(int) minimap.width, (int) minimap.height, null);
		hud.pintaHud(g);
		g.setColor(Color.WHITE);

		if (rosto != null)
			g.drawImage(rosto, 11, 485, null);

		for (int i = 0; i < dialogo.size(); i++) {
			g.drawString(dialogo.get(i), 140, 500 + (20 * i));
		}

	}

	public void addPecasGeometricas() {
		addElemento("cidade", new Triangulo(0, 233, 1));
	}

	public void configLayers() {
		// configuração do cenario floresta 1
		configLayerBase("cidade", "grama");
		configLayerBase("cidade", "areia");
		configLayerBase("cidade", "casas");
		configLayerBase("cidade", "troncos");
		configLayerBase("cidade", "muros");
		configLayerSuperficie("cidade", "telhado");
		configLayerSuperficie("cidade", "folhas");
		// configuração do cenario floresta 1
		configLayerBase("floresta 1", "grama");
		configLayerBase("floresta 1", "areia");
		configLayerBase("floresta 1", "penhasco 1");
		configLayerBase("floresta 1", "penhasco 2");
		configLayerBase("floresta 1", "pedras");
		configLayerBase("floresta 1", "morros 1");
		configLayerBase("floresta 1", "troncos");
		configLayerBase("floresta 1", "ponte");
		configLayerSuperficie("floresta 1", "morros 2");
		configLayerSuperficie("floresta 1", "folhas 1");
		configLayerSuperficie("floresta 1", "folhas 2");
	}

	public void addNPCs() {
		int[] mis1 = { 1 };
		addElemento(
				"cidade",
				new NPC(
						508,
						180,
						27,
						57,
						2,
						4,
						"Monstro",
						new Missao(
								1,
								"Que bom que você esta aqui, eu esqueci um objeto la na floresta,\npor favor pegou pra mim.\n só pra você saber é um objeto na forma de um tringulo",
								100, mis1, -1, "")));
		int[] mis5 = { 2, 3 };
		addElemento(
				"floresta 1",
				new NPC(
						460,
						233,
						27,
						57,
						3,
						4,
						"Monstro",
						new Missao(
								1,
								"Me entregue 2 peças QUADRADAS para que\neu possa consertar a ponte.",
								0, mis5, 104, "ponte B")));
		int[] mis6 = { 4, 5 };
		addElemento(
				"floresta 1",
				new NPC(
						230,
						233,
						27,
						57,
						0,
						4,
						"Monstro",
						new Missao(
								1,
								"Me entregue 2 peça TRIANGULARES para que\neu possa consertar esta ponte.",
								0, mis6, 106, "ponte A")));
	}

	public static void main(String[] args) {
		new Thread(new Aplicacao()).start();
	}
}
