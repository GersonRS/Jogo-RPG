package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Aplicacao extends Game1 {

	ArrayList<Missao> missoes;
	Hud hud;
	boolean isDialogo = false;
	ArrayList<String> dialogo;
	BufferedImage rosto;

	public Aplicacao() {
		missoes = new ArrayList<Missao>();
	}

	@Override
	public void onLoad() {
		loadCenario("cidade");
		loadCenario("floresta 1");
		addElementoPrincipal("cidade", new Principal(180, 100, 23, 55, 6,
				"personagem"));
		// addElemento("cidade", new NPC(470, 180, 27, 57, 4, "Monstro.png",
		// new Missao(1, "aaaaaaaaaaaaaaaa", 100, 1, new Triangulo(-32,
		// -32, 1),1280,128,"cidade")));
//		addElemento("cidade", new Triangulo(180, 180, 1));
//		addElemento("cidade", new Quadrado(180, 200, 2));
		addElemento("cidade", new NPC(200, 250, 27, 57, 4, "Monstro",
				new Missao(1, "aaaaaaaaaaaaaaaa", 100, 1, "quadrado", 2, 300,
						350, "cidade")));
		// addElemento("cidade", new NPC(480, 640, 27, 57, 4, "Monstro.png",
		// new Missao(6, "bbbbbbbbbbbb", 100, 2, new Triangulo(-32, -32,
		// 2),0,0,"")));
		// addElemento("cidade", new NPC(150, 670, 27, 57, 4, "Monstro.png",
		// new Missao(2, "ccccccccccccccc", 100, 3, new Triangulo(-32,
		// -32, 3),0,0,"")));
		// addElemento("cidade", new NPC(750, 610, 27, 57, 4, "Monstro.png",
		// new Missao(3, "dddddddddddd", 100, 4, new Triangulo(-32, -32,
		// 4),0,0,"")));
		// addElemento("cidade", new NPC(1010, 620, 27, 57, 4, "Monstro.png",
		// new Missao(4, "eeeeeeeeeeeeeeeee", 100, 5, new Triangulo(-32,
		// -32, 5),0,0,"")));
		// addElemento("cidade", new NPC(1140, 180, 27, 57, 4, "Monstro.png",
		// new Missao(4, "fffffffffffffffffff", 100, 6, new Triangulo(-32,
		// -32, 6),0,0,"")));
		configLayerBase("cidade", "grama");
		configLayerBase("cidade", "areia");
		configLayerBase("cidade", "casas");
		configLayerBase("cidade", "troncos");
		configLayerBase("cidade", "muros");
		configLayerSuperficie("cidade", "telhado");
		configLayerSuperficie("cidade", "folhas");
		//configuração do cenario floresta 1
		configLayerBase("floresta 1", "grama");
		configLayerBase("floresta 1", "areia");
		configLayerBase("floresta 1", "penhasco 1");
		configLayerBase("floresta 1", "penhasco 2");
		configLayerBase("floresta 1", "pedras");
		configLayerBase("floresta 1", "morros 1");
		configLayerBase("floresta 1", "troncos");
		configLayerSuperficie("floresta 1", "morros 2");
		configLayerSuperficie("floresta 1", "folhas 1");
		configLayerSuperficie("floresta 1", "folhas 2");
		
		currentCenario("floresta 1");
		hud = new Hud(getElementoPrincipal());
		dialogo = new ArrayList<String>();
		// playSoundLoop("som.wav");
	}

	@Override
	public void onUpdate(int currentTick) {
		if (!isDialogo) {
			for (int j = 0; j < getElementoPrincipal().collidingEntities.length; j++) {
				if (getElementoPrincipal().collidingEntities[j] != null)
					if (getElementoPrincipal().collidingEntities[j] instanceof NPC) {
						if (isJustPressed(KeyEvent.VK_ENTER)) {
							isDialogo = true;
							rosto = ((NPC) getElementoPrincipal().collidingEntities[j]).rosto;
							String s = ((NPC) getElementoPrincipal().collidingEntities[j])
									.getDialogo(this);
							if(s.contains("\n")){
								StringTokenizer linhas = new StringTokenizer(
										s, "\n");
								while (linhas.hasMoreTokens()) {
									dialogo.add(linhas.nextToken());
								}
							}else
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
	}

	@Override
	public void onRender(Graphics2D g) {
		renderCenarioBase(g);
		renderElementos(g);
		renderCenarioSuperficie(g);
	}

	@Override
	protected void onRenderHud(Graphics2D g) {
		hud.pintaHud(g);
		g.setColor(Color.WHITE);
		
		if(rosto!=null)
			g.drawImage(rosto, 11, 485, null);
		
		for (int i = 0; i < dialogo.size(); i++) {
			g.drawString(dialogo.get(i), 140, 500+(20*i));			
		}
	}

	public static void main(String[] args) {
		new Thread(new Aplicacao()).start();
	}
}
