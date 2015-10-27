package main;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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

	// --------atributos---------//
	private final int ABOVE = 0;
	private final int RIGHT = 1;
	private final int BELOW = 2;
	private final int LEFT = 3;

	private static final int DEFAULT_UPS = 60;
	private static final int DEFAULT_NO_DELAYS_PER_YIELD = 16;
	private static final int DEFAULT_MAX_FRAME_SKIPS = 5;
	private JFrame mainWindow;
	private BufferStrategy bufferStrategy;
	private BufferedImage tela;
	private int width = 800;
	private int height = 600;

	private long desiredUpdateTime;
	private boolean running;

	private long afterTime;
	private long beforeTime = System.currentTimeMillis();

	private long overSleepTime = 0;
	private long excessTime = 0;

	private int noDelaysPerYield = DEFAULT_NO_DELAYS_PER_YIELD;
	private int maxFrameSkips = DEFAULT_MAX_FRAME_SKIPS;

	private int noDelays = 0;

	private Elemento elemento;
	private HashMap<String, ArrayList<Elemento>> elementos;
	private HashMap<String, Scenery> cenarios;
	private String currentCenario;

	/**
	 * Cria um novo objeto Game.
	 * 
	 * @param ups
	 *            Número de atualizações desejadas por segundo.
	 * @param maxFrameSkips
	 *            Número máximo de quadro que pode ser ignorado se o hardware
	 *            gráfico não é rápido o suficiente para acompanhar os altos
	 *            desejados.
	 * @param noDelaysPerYield
	 *            Se o hardware não é rápido o suficiente para permitir uma
	 *            dellay entre dois quadros , o atraso será aplicada neste
	 *            balcão, portanto, outros segmentos podem processar suas ações.
	 */
	public Game(int ups, int maxFrameSkips, int noDelaysPerYield) {
		super();

		if (ups < 1)
			throw new IllegalArgumentException(
					"Você deve exibir, pelo menos, um quadro por segundo!");

		if (ups > 1000)
			ups = 1000;

		this.desiredUpdateTime = 1000000000L / ups;
		this.running = true;

		this.maxFrameSkips = maxFrameSkips;
		this.noDelaysPerYield = noDelaysPerYield;
		this.keyCache = new HashMap<Integer, Integer>();
		this.pressedKeys = new ArrayList<Integer>();
		this.releasedKeys = new ArrayList<Integer>();
		this.elementos = new HashMap<String, ArrayList<Elemento>>();
		this.cenarios = new HashMap<String, Scenery>();

	}

	/**
	 * Cria um novo objeto Game
	 * 
	 * @param ups
	 *            Número de atualizações desejadas por segundo.
	 */
	public Game(int ups) {
		this(ups, DEFAULT_MAX_FRAME_SKIPS, DEFAULT_NO_DELAYS_PER_YIELD);
	}

	/**
	 * Crie um novo objeto Game e uma atualização por segundo de 80 frames.
	 * 
	 */
	public Game() {
		this(DEFAULT_UPS);
	}

	/**
	 * Dormir a quantidade de tempo determinado. Como o método sleep () da
	 * classe thread não é preciso, o overSleepTime será calculado.
	 * 
	 * @param nanos
	 *            Número de nanossegundos para dormir.
	 * @throws InterruptedException
	 *             Se o segmento foi interrompida.
	 */
	private void sleep(long nanos) throws InterruptedException {
		noDelays = 0;
		long beforeSleep = System.nanoTime();
		Thread.sleep(nanos / 1000000L, (int) (nanos % 1000000L));
		overSleepTime = System.nanoTime() - beforeSleep - nanos;
	}

	/**
	 * Se o número de quadros sem um atraso é atingido , forçar a Thread a
	 * ceder, permitindo que outros segmentos para processar .
	 */
	private void yieldIfNeed() {
		if (++noDelays == noDelaysPerYield) {
			Thread.yield();
			noDelays = 0;
		}
	}

	/**
	 * Calcula o tempo de sono com base no cálculo do loop anterior. Para
	 * atingir este tempo , o tempo de apresentação de imagem serão subtraídos
	 * pelo tempo decorrido no último cálculo (afterTime - beforeTime). Então ,
	 * se no circuito anterior, houve um tempo oversleep , este também será
	 * subtraído , de modo que o sistema pode compensar este prolongamento.
	 */
	private long calculateSleepTime() {
		return desiredUpdateTime - (afterTime - beforeTime) - overSleepTime;
	}

	/**
	 * Ir o número de quadros de acordo com o excesso determinado momento. Isso
	 * permite que o jogo seja executado com a mesma velocidade, mesmo se o
	 * computador tem uma taxa de quadros menor do que o necessário. O número
	 * total de saltos estão limitados a MAX_FRAME_SKIPS .
	 * 
	 * @param exceededTime
	 *            O tempo excedido . Se o tempo é suficiente maior para ignorar
	 *            um ou mais quadros , eles serão ignorados.
	 * @return O tempo de excesso remanescente , após os pulos.
	 */
	private void skipFramesInExcessTime(int tick) {
		int skips = 0;
		while ((excessTime > desiredUpdateTime) && (skips < maxFrameSkips)) {
			excessTime -= desiredUpdateTime;
			updateKeys();
			logica(tick);
			testaColisao();
			skips++;
		}
	}

	/**
	 * Executa o loop principal. Este método não é thread-safe e não deve ser
	 * chamado mais de uma vez .
	 */
	public void run() {
		running = true;
		Graphics2D g;
		int tick = 0;
		try {
			inicializacao();
			while (running) {
				g = (Graphics2D) tela.getGraphics();
				tick++;
				beforeTime = System.nanoTime();
				skipFramesInExcessTime(tick);
				updateKeys();
				logica(tick);
				testaColisao();
				paint(g);
				afterTime = System.nanoTime();

				long sleepTime = calculateSleepTime();

				if (sleepTime >= 0)
					sleep(sleepTime);
				else {
					excessTime -= sleepTime;
					overSleepTime = 0L;
					yieldIfNeed();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception during game loop", e);
		} finally {
			running = false;
			System.exit(0);
		}
	}

	// ---------------logica------------//

	/**
	 * Este evento ocorre antes da primeira iteração do ciclo, e apenas uma vez.
	 */
	private void inicializacao() {
		mainWindow = new JFrame("Desenvolvimento de Jogos Digitais");
		mainWindow.setSize(width, height);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setUndecorated(true);
		mainWindow.setIgnoreRepaint(true);
		mainWindow.setPreferredSize(new Dimension(width, height));
		mainWindow.setVisible(true);
		mainWindow.createBufferStrategy(2);
		mainWindow.setFocusable(true);
		mainWindow.requestFocus();

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

		double x = elemento.pos.x;
		double y = elemento.pos.y;

		onUpdate(tick);

		if (elemento.pos.x >= width / 3
				&& elemento.pos.x <= cenarios.get(currentCenario).getPos().width
						- width / 3 - 14)
			cenarios.get(currentCenario).getPos().x -= elemento.pos.x - x;
		if (elemento.pos.y >= height / 3
				&& elemento.pos.y <= cenarios.get(currentCenario).getPos().height
						- height / 3 - 23)
			cenarios.get(currentCenario).getPos().y -= elemento.pos.y - y;

		if (cenarios.get(currentCenario).getPos().x > 0) {
			cenarios.get(currentCenario).getPos().x = 0;
		}
		if (cenarios.get(currentCenario).getPos().y > 0) {
			cenarios.get(currentCenario).getPos().y = 0;
		}
		if (cenarios.get(currentCenario).getPos().x < -cenarios.get(
				currentCenario).getPos().width
				+ width / 3 - 14) {
			cenarios.get(currentCenario).getPos().x = -cenarios.get(
					currentCenario).getPos().width
					+ width / 3 - 14;
		}
		if (cenarios.get(currentCenario).getPos().y < -cenarios.get(
				currentCenario).getPos().height
				+ height / 3 - 23) {
			cenarios.get(currentCenario).getPos().y = -cenarios.get(
					currentCenario).getPos().height
					+ height / 3 - 23;
		}
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

	/**
	 * Tintas os gráficos prestados na tela.
	 */
	private void paint(Graphics2D g) {

		onRender(g);

		Graphics2D g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
		g2d.drawImage(tela, (int) cenarios.get(currentCenario).getPos().x,
				(int) cenarios.get(currentCenario).getPos().y, null);
		onRenderHud(g2d);
		g2d.dispose();
		bufferStrategy.show();
	}

	private void testaColisao() {
		ArrayList<Elemento> element = this.elementos.get(currentCenario);
		ArrayList<Elemento> obstaculo = this.elementos.get("obstaculos");
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

	private void updateKeys() {
		for (Integer keyCode : keyCache.keySet()) {
			if (isJustPressed(keyCode)) {
				keyCache.put(keyCode, KEY_PRESSED);
			}
		}
		for (Integer keyCode : releasedKeys) {
			keyCache.put(keyCode, KEY_RELEASED);
		}
		for (Integer keyCode : pressedKeys) {
			if (isReleased(keyCode)) {
				keyCache.put(keyCode, KEY_JUST_PRESSED);
			} else {
				keyCache.put(keyCode, KEY_PRESSED);
			}
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
		} else {
			System.out.println("cenario ja carregado");
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

	protected void updateElementos(int tick) {
		for (Elemento elemento : elementos.get(currentCenario)) {
			if (elemento.ativo) {
				elemento.mover(this);
				elemento.update(tick);
			} else {
				elementos.get(currentCenario).remove(elemento);
			}
		}
	}

	protected void renderElementos(Graphics2D g) {
		for (Elemento elemento : elementos.get(currentCenario)) {
			if (elemento.visivel) {
				elemento.render(g);
				// g.drawRect((int) elemento.getColisao().getX(), (int)
				// elemento.getColisao().getY(),
				// (int) elemento.getColisao().getWidth(), (int)
				// elemento.getColisao().getHeight());
			}
		}
	}

	public Principal getElementoPrincipal() {
		return (Principal)elemento;
	}
}