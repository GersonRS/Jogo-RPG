package main;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

/**
 * Controla o loop principal. Esta classe tentará atualizar as lógicas em uma
 * taxa fixa por segundo (UPS). Isso garante consistência entre animações tanto
 * em computadores de alta e baixa velocidade.
 * <p>
 * O número de quadros por segundo desenhar irá variar de acordo com o hardware
 * e com o tempo de processamento do jogo. Se o hardware é rápido o suficiente
 * um quadro será atualizado por lógicas atualização (isto é, fps == ups). O
 * tempo de sono entre os quadros serão calculados e mantidos automaticamente.
 * <p>
 * Se o hardware não é rápido o suficiente, não haverá tempo de sono entre os
 * quadros. Uma vez que esta poderia cair todos os outros segmentos, o ciclo irá
 * gerar um sinal de rendimento de cada vez que a contagem é obtida
 * noDelaysPerYield.
 * <p>
 * O excesso de tempo para renderizar um quadro serão acumulados a cada ciclo.
 * Quando isso representa um erro grande o suficiente para pular um quadro, o
 * quadro será automaticamente skiped. O número máximo de quadros skiped num
 * único passo é dado pelo atributo maxFrameSkips.
 * <p>
 * Para iniciar o MainLoop chamar o método run (). Por conveniência, a classe
 * <code> Game </ code> é umaclasse abstrata.
 */

public abstract class Game implements Runnable, Iteracao {

	private HashMap<Integer, Integer> keyCache;
	private ArrayList<Integer> pressedKeys;
	private ArrayList<Integer> releasedKeys;
	private boolean mouseButton1;
	private boolean mouseButton2;
	private Point mousePos;
	private volatile float alpha = 0.0f;
	private float add = 0.01f;
	private String nextCenerio;
	private Point2D.Double nextPos;

	// --------atributos---------//
	private final int ABOVE = 0;
	private final int RIGHT = 1;
	private final int BELOW = 2;
	private final int LEFT = 3;

	protected JFrame mainWindow;
	private BufferStrategy bufferStrategy;
	protected BufferedImage tela;
	protected int width = 800;
	protected int height = 600;

	private int expectedTPS;
	private double expectedNanosPerTick;
	private int maxFrameSkip;

	static public final double NANOS_IN_ONE_SECOND = 1e9;
	protected int ticksPerSecond;
	protected long previousNanotime;
	protected int countedTicks;
	protected int totalTicks;
	private boolean running;

	protected Elemento elemento;
	protected HashMap<String, ArrayList<Elemento>> elementos;
	protected HashMap<String, Scenery> cenarios;
	protected String currentCenario;

	/**
	 * Crie um novo objeto Game e uma atualização por segundo de 80 frames.
	 */
	public Game() {
		this.running = true;
		this.keyCache = new HashMap<Integer, Integer>();
		this.pressedKeys = new ArrayList<Integer>();
		this.releasedKeys = new ArrayList<Integer>();
		this.elementos = new HashMap<String, ArrayList<Elemento>>();
		this.cenarios = new HashMap<String, Scenery>();

	}

	/**
	 * Executa o loop principal. Este método não é thread-safe e não deve ser
	 * chamado mais de uma vez .
	 */
	public void run() {
		running = true;
		Graphics2D g;
		// int tick = 0;
		inicializacao();
		expectedTPS = 60;
		expectedNanosPerTick = NANOS_IN_ONE_SECOND / expectedTPS;
		maxFrameSkip = 10;
		long nanoTimeAtNextTick = System.nanoTime();
		int skippedFrames = 0;
		while (running) {
			updateTime();
			if (System.nanoTime() > nanoTimeAtNextTick
					&& skippedFrames < maxFrameSkip) {
				nanoTimeAtNextTick += expectedNanosPerTick;
				updateKeys();
				logica(totalTicks);
				testaColisao();
				skippedFrames++;

			} else {
				g = (Graphics2D) tela.getGraphics();
				paint(g);
				skippedFrames = 0;
			}
		}
	}

	/**
	 * Este evento ocorre antes da primeira iteração do ciclo, e apenas uma vez.
	 */
	private void inicializacao() {
		mainWindow = new JFrame("Desenvolvimento de Jogos Digitais");
		mainWindow.setSize(width, height);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setUndecorated(true);
		mainWindow.setResizable(false);
		mainWindow.setIgnoreRepaint(true);
		mainWindow.setPreferredSize(new Dimension(width, height));
		mainWindow.setVisible(true);
		mainWindow.createBufferStrategy(2);
		mainWindow.setFocusable(true);
		mainWindow.requestFocus();

		previousNanotime = System.nanoTime();
		countedTicks = 0;
		ticksPerSecond = 0;
		totalTicks = 0;

		ArrayList<Elemento> obs = new ArrayList<Elemento>();
		ArrayList<Elemento> out = new ArrayList<Elemento>();
		nextPos = new Point2D.Double();

		elementos.put("obstaculos", obs);
		elementos.put("out", out);

		bufferStrategy = mainWindow.getBufferStrategy();

		mainWindow.addKeyListener(this);

		onLoad();
	}

	/**
	 * O método logica atualiza todos os estados jogo de objetos. O jogo irá
	 * processá-lo do modelo de dados e definir a próxima situação de jogo que
	 * irá ser pintado na tela.
	 */
	private void logica(int tick) {

		countedTicks++;
		totalTicks++;
		updateTime();

		if (nextCenerio != "" && nextCenerio != null) {
			if (alpha > 0.9) {
				add = add * -1;
				currentCenario(nextCenerio);
				elemento.pos.x = nextPos.x;
				elemento.pos.y = nextPos.y;
				cenarios.get(currentCenario).getPos().y = (elemento.pos.y - height / 3)
						* -1;
				cenarios.get(currentCenario).getPos().x = (elemento.pos.x - width / 3)
						* -1;
				onUpdate(totalTicks);
			}
			alpha += add;
			if (alpha < 0.0) {
				add = 0.01f;
				alpha = 0.0f;
				nextCenerio = "";
			}
		} else {
			onUpdate(totalTicks);
		}

		Thread.yield();
	}

	private void updateTime() {
		if (System.nanoTime() - previousNanotime > NANOS_IN_ONE_SECOND) {
			ticksPerSecond = countedTicks;
			countedTicks = 0;
			previousNanotime = System.nanoTime();
		}
	}

	/**
	 * Tintas os gráficos prestados na tela.
	 */
	private void paint(Graphics2D g) {

		onRender(g);

		Graphics2D g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
		g2d.drawImage(tela, (int) cenarios.get(currentCenario).getPos().x,
				(int) cenarios.get(currentCenario).getPos().y, null);
		onRenderHud(g2d);
		g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();
		bufferStrategy.show();
	}

	private void testaColisao() {
		ArrayList<Elemento> element = this.elementos.get(currentCenario);
		ArrayList<Elemento> obstaculo = this.elementos.get("obstaculos");
		ArrayList<Elemento> out = this.elementos.get("out");

		for (Elemento o : element) {
			for (int i = 0; i < o.collidingEntities.length; i++) {
				o.collidingEntities[i] = null;
			}
		}
		for (int i1 = 0; i1 < element.size() - 1; i1++) {
			Elemento o1 = element.get(i1);
			for (int i2 = i1 + 1; i2 < element.size(); i2++) {
				Elemento o2 = element.get(i2);
				double difX = (o1.getColisao().getX() + (o1.getColisao()
						.getWidth() / 2))
						- (o2.getColisao().getX() + (o1.getColisao().getWidth() / 2));
				double difY = (o1.getColisao().getY() + (o1.getColisao()
						.getHeight() / 2))
						- (o2.getColisao().getY() + (o1.getColisao()
								.getHeight() / 2));
				double distancia = Math.sqrt((difX * difX) + (difY * difY));
				if (distancia < 64)
					if (o1.getColisao().intersects(o2.getColisao())) {
						Rectangle2D rect = o1.getColisao().createIntersection(
								o2.getColisao());
						if (rect.getWidth() > rect.getHeight()) {
							if (o1.getColisao().getCenterY() < o2.getColisao()
									.getCenterY()) {
								o1.collidingEntities[BELOW] = o2;
								o2.collidingEntities[ABOVE] = o1;
							} else {
								o1.collidingEntities[ABOVE] = o2;
								o2.collidingEntities[BELOW] = o1;
							}
						} else {
							if (o1.getColisao().getCenterX() < o2.getColisao()
									.getCenterX()) {
								o1.collidingEntities[RIGHT] = o2;
								o2.collidingEntities[LEFT] = o1;
							} else {
								o1.collidingEntities[LEFT] = o2;
								o2.collidingEntities[RIGHT] = o1;
							}
						}
					}
			}
		}
		for (int i1 = 0; i1 < element.size(); i1++) {
			Elemento o1 = element.get(i1);
			for (int i2 = 0; i2 < obstaculo.size(); i2++) {
				Elemento o2 = obstaculo.get(i2);
				if (o2.ativo) {
					double difX = (o1.getColisao().getX() + (o1.getColisao()
							.getWidth() / 2))
							- (o2.getColisao().getX() + (o1.getColisao()
									.getWidth() / 2));
					double difY = (o1.getColisao().getY() + (o1.getColisao()
							.getHeight() / 2))
							- (o2.getColisao().getY() + (o1.getColisao()
									.getHeight() / 2));
					double distancia = Math.sqrt((difX * difX) + (difY * difY));
					if (distancia < 64)
						if (o1.getColisao().intersects(o2.getColisao())) {
							Rectangle2D rect = o1.getColisao()
									.createIntersection(o2.getColisao());
							if (rect.getWidth() > rect.getHeight()) {
								if (o1.getColisao().getCenterY() < o2
										.getColisao().getCenterY()) {
									o1.collidingEntities[BELOW] = o2;
									o2.collidingEntities[ABOVE] = o1;
								} else {
									o1.collidingEntities[ABOVE] = o2;
									o2.collidingEntities[BELOW] = o1;
								}
							} else {
								if (o1.getColisao().getCenterX() < o2
										.getColisao().getCenterX()) {
									o1.collidingEntities[RIGHT] = o2;
									o2.collidingEntities[LEFT] = o1;
								} else {
									o1.collidingEntities[LEFT] = o2;
									o2.collidingEntities[RIGHT] = o1;
								}
							}
						}

				}
			}
		}
		for (int i = 0; i < out.size(); i++) {
			if (out.get(i).ativo) {
				double difX = (out.get(i).getColisao().getX() + (out.get(i)
						.getColisao().getWidth() / 2))
						- (elemento.getColisao().getX() + (out.get(i)
								.getColisao().getWidth() / 2));
				double difY = (out.get(i).getColisao().getY() + (out.get(i)
						.getColisao().getHeight() / 2))
						- (elemento.getColisao().getY() + (out.get(i)
								.getColisao().getHeight() / 2));
				double distancia = Math.sqrt((difX * difX) + (difY * difY));
				if (distancia < 64)
					if (out.get(i).getColisao()
							.intersects(elemento.getColisao())) {
						String destino = cenarios.get(currentCenario)
								.getDestino(out.get(i).id);
						if (destino != "")
							for (Elemento in : cenarios.get(destino).getIn()) {
								if (in.id == out.get(i).id) {
									nextPos.y = in.pos.y - in.pos.height - 5;
									nextPos.x = in.pos.x + 5;
									nextCenerio = destino;
								}
							}
					}
			}
		}
	}

	public boolean isPressed(int keyId) {
		return keyCache.containsKey(keyId)
				&& !keyCache.get(keyId).equals(KEY_RELEASED);
	}

	public boolean isJustPressed(int keyId) {
		return keyCache.containsKey(keyId)
				&& keyCache.get(keyId).equals(KEY_JUST_PRESSED);
	}

	public boolean isReleased(int keyId) {
		return !keyCache.containsKey(keyId)
				|| keyCache.get(keyId).equals(KEY_RELEASED);
	}

	public boolean isMousePressed(int buttonId) {
		if (buttonId == MouseEvent.BUTTON1) {
			return mouseButton1;
		}
		if (buttonId == MouseEvent.BUTTON2) {
			return mouseButton2;
		}
		return false;
	}

	public Point getMousePos() {
		return mousePos;
	}

	public void updateKeys() {
		try {
			for (Integer keyCode : keyCache.keySet()) {
				if (isJustPressed(keyCode)) {
					keyCache.put(keyCode, KEY_PRESSED);
				}
			}
			for (int i = 0; i < releasedKeys.size(); i++) {
				keyCache.put(releasedKeys.get(i), KEY_RELEASED);
			}
			for (int i = 0; i < pressedKeys.size(); i++) {
				if (isReleased(pressedKeys.get(i))) {
					keyCache.put(pressedKeys.get(i), KEY_JUST_PRESSED);
				} else {
					keyCache.put(pressedKeys.get(i), KEY_PRESSED);
				}
			}
		} catch (NullPointerException e) {
			System.out.println("aff");
		}
		pressedKeys.clear();
		releasedKeys.clear();
	}

	public void keyTyped(KeyEvent e) {
		// Rotina nÃ£o utilizada. Evento de tecla teclada.
	}

	public void keyPressed(KeyEvent e) {
		pressedKeys.add(e.getKeyCode());
	}

	public void keyReleased(KeyEvent e) {
		releasedKeys.add(e.getKeyCode());
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseButton1 = true;
		}
		if (e.getButton() == MouseEvent.BUTTON2) {
			mouseButton2 = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseButton1 = false;
		}
		if (e.getButton() == MouseEvent.BUTTON2) {
			mouseButton2 = false;
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		mousePos.setLocation(e.getPoint());
	}

	public void mouseMoved(MouseEvent e) {
		mousePos.setLocation(e.getPoint());
	}

	abstract protected void onLoad();

	abstract protected void onUpdate(int currentTick);

	abstract protected void onRender(Graphics2D g);

	abstract protected void onRenderHud(Graphics2D g);

	protected void playSoundLoop(String fileName) {
		try {
			Audio.getInstance().playLoop();
		} catch (IOException ioe) {
		} catch (UnsupportedAudioFileException e) {
		} catch (LineUnavailableException e) {
		}
	}

	protected void addElemento(String cenario, Elemento e) {
		elementos.get(cenario).add(e);
	}

	protected void addElementoPrincipal(String cenario, Elemento e) {
		this.elemento = e;
		// addElemento(cenario, e);
	}

	protected void loadCenario(String cenario) {
		if (!cenarios.containsKey(cenario)) {
			Scenery scenery = new Scenery(cenario);
			this.cenarios.put(cenario, scenery);
			ArrayList<Elemento> elements = new ArrayList<Elemento>();
			elementos.put(cenario, elements);
		}
	}

	protected void currentCenario(String current) {
		if (cenarios.containsKey(current)) {
			Scenery scenery = cenarios.get(current);
			tela = new BufferedImage((int) scenery.getPos().width,
					(int) scenery.getPos().height,
					BufferedImage.TYPE_4BYTE_ABGR);
			elementos.put("obstaculos", scenery.getObstaculos());
			elementos.put("out", scenery.getOut());
			for (ArrayList<Elemento> elements : elementos.values()) {
				elements.remove(elemento);
			}
			elementos.get(current).add(elemento);
			this.currentCenario = current;
		}
	}

	protected void configLayerBase(String cenario, String layer) {
		cenarios.get(cenario).configLayerBase(layer);
	}

	protected void configLayerBase(String layer) {
		cenarios.get(currentCenario).configLayerBase(layer);
	}

	protected void configLayerSuperficie(String cenario, String layer) {
		cenarios.get(cenario).configLayerSuperficie(layer);
	}

	protected void renderCenario(Graphics2D g) {
		cenarios.get(currentCenario).render(g);
	}

	protected void renderCenarioBase(Graphics2D g) {
		cenarios.get(currentCenario).renderBase(g);
	}

	protected void renderCenarioSuperficie(Graphics2D g) {
		cenarios.get(currentCenario).renderSuperficie(g);
	}

	protected void renderCenario(Graphics2D g, String camada) {
		cenarios.get(currentCenario).render(g, camada);
	}

	protected void removerObstaculos(int id) {
		ArrayList<Elemento> o = elementos.get("obstaculos");
		ArrayList<Elemento> r = new ArrayList<Elemento>();
		for (int i = 0; i < o.size(); i++) {
			if (o.get(i).id == id) {
				r.add(o.get(i));
			}
		}
		for (int i = 0; i < r.size(); i++) {
			o.remove(r.get(i));
		}
	}

	protected void updateElementos(int tick) {
		for (int i = 0; i < elementos.get(currentCenario).size(); i++) {
			Elemento e = elementos.get(currentCenario).get(i);
			if (e.ativo) {
				e.mover(this);
				e.update(tick);
			} else {
				elementos.get(currentCenario).remove(e);
			}
		}
	}

	protected void renderElementos(Graphics2D g) {
		for (Elemento elemento : elementos.get(currentCenario)) {
			if (elemento.visivel) {
				elemento.render(g);
			}
		}
	}

	public Principal getElementoPrincipal() {
		return (Principal) elemento;
	}

	public void addTeleport(String cenariOrigem, String cenarioDestino,
			int local) {
		if (cenarios.containsKey(cenariOrigem)
				&& cenarios.containsKey(cenarioDestino)) {
			cenarios.get(cenariOrigem).addTeleport(cenarioDestino, local);
		}
	}
}