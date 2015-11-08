package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import core.Elemento;
import core.Game;

public class Aplicacao extends Game {

	ArrayList<Missao> missoes;
	Hud hud;
	boolean isDialogo = false;
	ArrayList<String> dialogo;
	BufferedImage rosto, minimap;
	Rectangle2D.Double minimapPos;
	Rectangle2D.Double posAnterior;
	
	public Aplicacao() {
		missoes = new ArrayList<Missao>();
		minimapPos = new Rectangle2D.Double();
		posAnterior = new Rectangle2D.Double(0,0,0,0);
		minimap = new BufferedImage(40*32, 40*32, BufferedImage.TYPE_4BYTE_ABGR);
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
		addTeleport("floresta 4", "caverna 1", 3);
		addTeleport("floresta 4", "caverna 1", 6);
		addTeleport("floresta 5", "floresta 1", 5);
		addTeleport("floresta 5", "floresta 4", 7);
		addTeleport("caverna 1", "floresta 4", 3);
		addTeleport("caverna 1", "floresta 4", 6);
		try {
			addElementoPrincipal(new Principal(196, 100, 23, 55, 6, "personagem"));
			addNPCs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		configLayers();
		addPecasGeometricas();
		hud = new Hud((Principal) getElementoPrincipal());
		dialogo = new ArrayList<String>();
		currentCenario("cidade");
		playSound("som.wav",false);
	}

	@Override
	public void onUpdate(int currentTick) {
		double x = elemento.getPos().x;
		double y = elemento.getPos().y;
		if (!isDialogo) {
			for (int j = 0; j < getElementoPrincipal().getCollidingEntities().length; j++) {
				if (getElementoPrincipal().getCollidingEntities()[j] != null)
					if (getElementoPrincipal().getCollidingEntities()[j] instanceof NPC) {
						if (isJustPressed(KeyEvent.VK_ENTER)) {
							isDialogo = true;
							rosto = ((NPC) getElementoPrincipal().getCollidingEntities()[j]).rosto;
							String s = ((NPC) getElementoPrincipal().getCollidingEntities()[j])
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
					} else if (getElementoPrincipal().getCollidingEntities()[j] instanceof PecaGeometrica) {
						if (isJustPressed(KeyEvent.VK_ENTER)) {
							if (((Principal) getElementoPrincipal())
									.getInventario().getPecasgeometricas()
									.size() < ((Principal) getElementoPrincipal())
									.getInventario().getNUM_MAX()) {
								((Principal) getElementoPrincipal())
										.getInventario()
										.add((PecaGeometrica) getElementoPrincipal().getCollidingEntities()[j]);
								getElementoPrincipal().getCollidingEntities()[j].setAtivo(false);
							}
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
		
		if (elemento.getPos().x >= (cenarios.get(currentCenario).getPos().width*0.80)/2
				&& elemento.getPos().x <= (cenarios.get(currentCenario).getPos().width*0.80)/2
						+ cenarios.get(currentCenario).getPos().width*0.20){
			minimapPos.x += (elemento.getPos().x - x);
		}
		if (elemento.getPos().y >= (cenarios.get(currentCenario).getPos().height*0.80)/2
				&& elemento.getPos().y <= (cenarios.get(currentCenario).getPos().height*0.80)/2
						+ cenarios.get(currentCenario).getPos().height*0.20){
			minimapPos.y += (elemento.getPos().y - y);
		}
		
		minimapPos.width = minimapPos.x+cenarios.get(currentCenario).getPos().width*0.80;
		minimapPos.height = minimapPos.y+cenarios.get(currentCenario).getPos().height*0.80;
		
		if(isTransition()){
			minimapPos.x = (elemento.getPos().x - (cenarios.get(currentCenario).getPos().width*0.80) / 2);
			minimapPos.y = (elemento.getPos().y - (cenarios.get(currentCenario).getPos().height*0.80) / 2);
			//ajeitar minimapa
			if (minimapPos.x < 0) {
				minimapPos.x = 0;
			}
			if (minimapPos.y < 0) {
				minimapPos.y = 0;
			}
			if(minimapPos.x > (cenarios.get(currentCenario).getPos().width- (cenarios.get(currentCenario).getPos().width*0.80))){
				minimapPos.x= (cenarios.get(currentCenario).getPos().width- (cenarios.get(currentCenario).getPos().width*0.80));
			}
			if(minimapPos.y > (cenarios.get(currentCenario).getPos().height- (cenarios.get(currentCenario).getPos().height*0.80))){
				minimapPos.y= (cenarios.get(currentCenario).getPos().height- (cenarios.get(currentCenario).getPos().height*0.80));
			}
			// ajeitar o cenario
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
		}

		if (elemento.getPos().x >= width / 3
				&& elemento.getPos().x <= cenarios.get(currentCenario).getPos().width
						- width / 3 - 14){
			cenarios.get(currentCenario).getPos().x -= elemento.getPos().x - x;			
		}
		if (elemento.getPos().y >= height / 3
				&& elemento.getPos().y <= cenarios.get(currentCenario).getPos().height
						- height / 3 - 23){
			cenarios.get(currentCenario).getPos().y -= elemento.getPos().y - y;
		}
		

		// sair do mapa
		if (elemento.getPos().x < 0
				|| elemento.getPos().x + elemento.getPos().width > cenarios.get(
						currentCenario).getPos().width) {
			elemento.getPos().x = x;
		}
		if (elemento.getPos().y < 0
				|| elemento.getPos().y + elemento.getPos().height > cenarios.get(
						currentCenario).getPos().height) {
			elemento.getPos().y = y;
		}
	}

	@Override
	public void onRender(Graphics2D g) {
		renderCenarioBase(g);
		renderElementos(g);
		renderCenarioSuperficie(g);
		Graphics2D mini = (Graphics2D) minimap.getGraphics();
		mini.setColor(Color.BLACK);
		mini.fillRect(0, 0, 40*32, 40*32);
		for (Elemento elemento : elementos.get(currentCenario)) {
			if (elemento instanceof Principal) {
				mini.setColor(Color.BLUE);
			} else if (elemento instanceof NPC) {
				mini.setColor(Color.GREEN);
			} else if (elemento instanceof PecaGeometrica) {
				mini.setColor(Color.RED);
			}
			mini.fill(elemento.getColisao());
		}
	}

	@Override
	protected void onRenderHud(Graphics2D g) {
		 g.drawImage(minimap, 580, 42, 770, 200, (int) minimapPos.x,
		 (int) minimapPos.y, (int) minimapPos.width,
		 (int) minimapPos.height, null);
		hud.pintaHud(g);
		g.setColor(Color.WHITE);

		if (rosto != null)
			g.drawImage(rosto, 11, 485, null);

		for (int i = 0; i < dialogo.size(); i++) {
			g.drawString(dialogo.get(i), 140, 500 + (20 * i));
		}
	}

	public void configLayers() {
		// configuração do cenario caverna 1
		configLayerBase("caverna 1", "vacuo");
		configLayerBase("caverna 1", "chao");
		configLayerBase("caverna 1", "pedras");
		configLayerBase("caverna 1", "paredes");
		configLayerSuperficie("caverna 1", "folhas");
		// configuração do cenario cidade
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
		// configuração do cenario floresta 2
		configLayerBase("floresta 2", "grama");
		configLayerBase("floresta 2", "penhasco 1");
		configLayerBase("floresta 2", "penhasco 2");
		configLayerBase("floresta 2", "areia");
		configLayerBase("floresta 2", "morros 1");
		configLayerBase("floresta 2", "troncos");
		configLayerSuperficie("floresta 2", "folhas 1");
		configLayerSuperficie("floresta 2", "folhas 2");
		// configuração do cenario floresta 3
		configLayerBase("floresta 3", "grama");
		configLayerBase("floresta 3", "areia");
		configLayerBase("floresta 3", "penhasco 1");
		configLayerBase("floresta 3", "penhasco 2");
		configLayerBase("floresta 3", "ponte");
		configLayerBase("floresta 3", "troncos");
		configLayerBase("floresta 3", "pedras");
		configLayerBase("floresta 3", "morros 1");
		configLayerSuperficie("floresta 3", "folhas 1");
		configLayerSuperficie("floresta 3", "folhas 2");
		// configuração do cenario floresta 4
		configLayerBase("floresta 4", "grama");
		configLayerBase("floresta 4", "penhasco 1");
		configLayerBase("floresta 4", "penhasco 2");
		configLayerBase("floresta 4", "areia");
		configLayerBase("floresta 4", "morros 1");
		configLayerBase("floresta 4", "morros 2");
		configLayerBase("floresta 4", "ponte");
		configLayerBase("floresta 4", "troncos");
		configLayerBase("floresta 4", "pedras");
		configLayerSuperficie("floresta 4", "folhas 1");
		configLayerSuperficie("floresta 4", "folhas 2");
		// configuração do cenario floresta 1
		configLayerBase("floresta 5", "grama");
		configLayerBase("floresta 5", "areia");
		configLayerBase("floresta 5", "morros");
		configLayerBase("floresta 5", "troncos");
		configLayerBase("floresta 5", "pedras");
		configLayerSuperficie("floresta 5", "folhas 1");
		configLayerSuperficie("floresta 5", "folhas 2");

	}

	public void addPecasGeometricas() {
		addElemento("floresta 1", new Triangulo(10, 233, 1));
		addElemento("cidade", new Quadrado(1100, 1138, 2));
		addElemento("cidade", new Triangulo(1056, 170, 3));
		addElemento("cidade", new Quadrado(50, 832, 4));
		addElemento("floresta 1", new Triangulo(32, 470, 5));
		addElemento("floresta 1", new Quadrado(766, 490, 6));
		addElemento("floresta 1", new Triangulo(10, 1024, 7));
		addElemento("floresta 2", new Quadrado(180, 360, 8));
		addElemento("floresta 2", new Triangulo(570, 128, 9));
		addElemento("floresta 2", new Quadrado(200, 660, 10));
		addElemento("floresta 2", new Triangulo(232, 660, 11));
		addElemento("floresta 3", new Quadrado(320, 192, 12));
		addElemento("floresta 3", new Triangulo(1216, 256, 13));
		addElemento("floresta 3", new Quadrado(1216, 736, 14));
		addElemento("floresta 3", new Triangulo(928, 576, 15));
		addElemento("floresta 4", new Quadrado(128, 960, 16));
		addElemento("floresta 4", new Triangulo(512, 320, 17));
		addElemento("floresta 4", new Quadrado(960, 800, 18));
		addElemento("floresta 5", new Triangulo(928, 224, 19));
		addElemento("floresta 5", new Quadrado(160, 480, 20));
		addElemento("caverna 1", new Triangulo(540, 864, 21));
		addElemento("caverna 1", new Quadrado(160, 704, 22));
		addElemento("caverna 1", new Triangulo(448, 96, 23));
		addElemento("cidade", new Quadrado(768, 0, 24));
	}

	public void addNPCs() throws IOException {
		int[] mis10 = { 23 };
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
								10,
								"Que bom que você esta aqui, eu esqueci um objeto la na floresta,\npor favor pegue-o para mim.\nsó pra você saber é um objeto na forma de um tringulo",
								100, mis10, 0, "")));
		int[] mis1 = { 1, 3 };
		addElemento(
				"cidade",
				new NPC(
						256,
						736,
						27,
						57,
						2,
						4,
						"Monstro",
						new Missao(
								1,
								"Hummm, sera que você pode me arranjar dois TRIANGULOS?\n eu estou precisando muito se você\n conseguir eu te dou um QUADRADO em troca.",
								100, mis1, -30, "ponte A")));
		int[] mis3 = { 8, 5 };
		addElemento(
				"cidade",
				new NPC(
						896,
						544,
						27,
						57,
						2,
						4,
						"Monstro",
						new Missao(
								3,
								"Ola Naruto, que bom que você apareceu estou precisando de um favor seu.\neu tenho uma encomenda para fazer mas não tenho o material\n você por favor poderia me arranjar um QUADRADO e um TRIANGULO da Floresta?",
								100, mis3, 102, "ponte A")));
		int[] mis2 = { 2, 30 };
		addElemento(
				"floresta 1",
				new NPC(
						396,
						233,
						27,
						57,
						0,
						4,
						"Monstro",
						new Missao(
								1,
								"Desculpa a ponte ta quebrada, mas se você\n me conseguir dois QUADRADOS eu consigo \nconserta-la para vc poder passar.",
								0, mis2, 102, "ponte A")));
		int[] mis100 = { 40, 50 };
		addElemento(
				"floresta 1",
				new NPC(
						660,
						233,
						27,
						57,
						0,
						4,
						"Monstro",
						new Missao(
								1,
								"Me entregue 2 peça TRIANGULARES para que\neu possa consertar esta ponte.",
								0, mis100, 103, "ponte B")));
	}

	public static void main(String[] args) {
		new Thread(new Aplicacao()).start();
	}
}
