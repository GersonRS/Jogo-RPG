package main;

import java.awt.Dimension;
import java.awt.Graphics2D;
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

public abstract class Game implements Runnable, Iteracao {

	private HashMap<Integer, Integer> keyCache;
	private ArrayList<Integer> pressedKeys;
	private ArrayList<Integer> releasedKeys;

	// --------atributos---------//
	private final int ABOVE = 0;
	private final int RIGHT = 1;
	private final int BELOW = 2;
	private final int LEFT = 3;

	private static final int DEFAULT_UPS = 80;
	private static final int DEFAULT_NO_DELAYS_PER_YIELD = 16;
	private static final int DEFAULT_MAX_FRAME_SKIPS = 5;
	private JFrame mainWindow;
	private BufferStrategy bufferStrategy;
	private BufferedImage tela;
	private int width = 800;
	private int height = 576;

	private long desiredUpdateTime;
	private boolean running;
	private boolean pause;

	private long afterTime;
	private long beforeTime = System.currentTimeMillis();

	private long overSleepTime = 0;
	private long excessTime = 0;

	private int noDelaysPerYield = DEFAULT_NO_DELAYS_PER_YIELD;
	private int maxFrameSkips = DEFAULT_MAX_FRAME_SKIPS;

	private int noDelays = 0;

	private Elemento principal;
	private HashMap<String, ArrayList<Elemento>> elementos;
	private HashMap<String, Scenery> cenarios;
	private String currentCenario;

	// --------construtores----------//
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

	public Game(int ups) {
		this(ups, DEFAULT_MAX_FRAME_SKIPS, DEFAULT_NO_DELAYS_PER_YIELD);
	}

	public Game() {
		this(DEFAULT_UPS);
	}

	// --------metodos------//
	private void sleep(long nanos) throws InterruptedException {
		noDelays = 0;
		long beforeSleep = System.nanoTime();
		Thread.sleep(nanos / 1000000L, (int) (nanos % 1000000L));
		overSleepTime = System.nanoTime() - beforeSleep - nanos;
	}

	private void yieldIfNeed() {
		if (++noDelays == noDelaysPerYield) {
			Thread.yield();
			noDelays = 0;
		}
	}

	private long calculateSleepTime() {
		return desiredUpdateTime - (afterTime - beforeTime) - overSleepTime;
	}

	private void skipFramesInExcessTime(int tick) {
		int skips = 0;
		while ((excessTime > desiredUpdateTime) && (skips < maxFrameSkips)) {
			excessTime -= desiredUpdateTime;
			logica(tick);
			skips++;
		}
	}

	// -----------loop--------------//
	public void run() {
		running = true;
		Graphics2D g;
		int tick = 0;
		try {
			inicializacao();
			while (running) {
				if (!pause) {
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
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception during game loop", e);
		} finally {
			running = false;
			System.exit(0);
		}
	}

	// ---------------logica------------//

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

		elementos.put("elementos", new ArrayList<Elemento>());

		onLoad();

	}

	private void logica(int tick) {

		double x = principal.pos.x;
		double y = principal.pos.y;
		

		onUpdate(tick);

		if (principal.pos.x >= 400
				&& principal.pos.x <= cenarios.get(currentCenario).getPos().width - 400)
			cenarios.get(currentCenario).getPos().x -= principal.pos.x - x;
		if (principal.pos.y >= 300
				&& principal.pos.y <= cenarios.get(currentCenario).getPos().height - 300)
			cenarios.get(currentCenario).getPos().y -= principal.pos.y - y;

		if (cenarios.get(currentCenario).getPos().x > 0) {
			cenarios.get(currentCenario).getPos().x = 0;
		}
		if (cenarios.get(currentCenario).getPos().y > 0) {
			cenarios.get(currentCenario).getPos().y = 0;
		}
		if (cenarios.get(currentCenario).getPos().x <
				-cenarios.get(currentCenario).getPos().width + mainWindow
					.getWidth()) {
			cenarios.get(currentCenario).getPos().x = -cenarios.get(currentCenario).getPos().width + mainWindow
					.getWidth();
		}
		if (cenarios.get(currentCenario).getPos().y <
				-cenarios.get(currentCenario).getPos().height + mainWindow
				.getHeight()) {
			cenarios.get(currentCenario).getPos().y = -cenarios.get(currentCenario).getPos().height + mainWindow
					.getHeight();
		}
	}
	private void paint(Graphics2D g) {
		onRender(g);
		Graphics2D g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
		g2d.drawImage(tela, (int) cenarios.get(currentCenario).getPos().x,
				(int) cenarios.get(currentCenario).getPos().y, null);
		g2d.dispose();
		bufferStrategy.show();
	}

	private void testaColisao() {
		ArrayList<Elemento> element = this.elementos.get("elementos");
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

	public void keyPressed(KeyEvent e) {
		pressedKeys.add(e.getKeyCode());
	}

	public void keyReleased(KeyEvent e) {
		releasedKeys.add(e.getKeyCode());
	}

	public void keyTyped(KeyEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	abstract protected void onLoad();

	abstract protected void onUpdate(int currentTick);

	abstract protected void onRender(Graphics2D g);

	protected void playSound(String fileName) {
		try {
			Audio.getInstance().audio();
		} catch (IOException ioe) {
		} catch (UnsupportedAudioFileException e) {
		} catch (LineUnavailableException e) {
		}
	}

	protected void addElemento(Elemento e) {
		elementos.get("elementos").add(e);
	}

	protected void addElementoPrincipal(Elemento e) {
		this.principal = e;
		addElemento(e);
	}

	protected void loadCenario(String cenario) {
		if (!cenarios.containsKey(cenario)) {
			Scenery scenery = new Scenery(cenario);
			this.cenarios.put(cenario, scenery);
			this.currentCenario = cenario;
		} else {
			System.out.println("cenario ja carregado");
		}
	}

	protected void currentCenario(String current) {
		Scenery scenery = cenarios.get(current);
		scenery.getPos().x = cenarios.get(currentCenario).getPos().x;
		scenery.getPos().y = cenarios.get(currentCenario).getPos().y;
		tela = new BufferedImage((int) scenery.getPos().width,
				(int) scenery.getPos().height, BufferedImage.TYPE_4BYTE_ABGR);
		ArrayList<Elemento> obstaculos = new ArrayList<Elemento>();
		for (Rectangle2D r : scenery.getObjects()) {
			obstaculos.add(new Obstaculo((int) r.getX(), (int) r.getY(),
					(int) r.getWidth(), (int) r.getHeight()));
		}
		elementos.remove("obstaculos");
		elementos.put("obstaculos", obstaculos);
		this.currentCenario = current;
	}

	protected void renderCenario(Graphics2D g) {
		cenarios.get(currentCenario).render(g);
	}
	protected void renderCenarioBase(Graphics2D g) {
		cenarios.get(currentCenario).renderBase(g);
	}
	protected void renderCenarioCima(Graphics2D g) {
		cenarios.get(currentCenario).renderCima(g);
	}

	protected void renderCenario(Graphics2D g, String camada) {
		cenarios.get(currentCenario).render(g, camada);
	}
	
	protected void updateElementos(int tick){
		for (Elemento elemento : elementos.get("elementos")) {
			if(elemento.ativo){
				elemento.mover(this);
				elemento.update(tick);
			}
		}
	}

	protected void renderElementos(Graphics2D g) {
		for (Elemento elemento : elementos.get("elementos")) {
			if(elemento.visivel){
				elemento.render(g);
//				g.drawRect((int) elemento.getColisao().getX(), (int) elemento.getColisao().getY(),
//						(int) elemento.getColisao().getWidth(), (int) elemento.getColisao().getHeight());
			}
		}
	}
}