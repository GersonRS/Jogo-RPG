package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

/**
 * Controla o loop principal. Esta classe tentar� atualizar as 
 * l�gicas em uma taxa fixa por segundo (UPS). Isso garante consist�ncia 
 * entre anima��es tanto em computadores de alta e baixa velocidade. 
 * <p> 
 * O n�mero de quadros por segundo desenhar ir� variar de acordo com o 
 * hardware e com o tempo de processamento do jogo. Se o hardware � r�pido 
 * o suficiente um quadro ser� atualizado por l�gicas atualiza��o (isto �, fps == ups). 
 * O tempo de sono entre os quadros ser�o calculados e mantidos automaticamente. 
 * <p> 
 * Se o hardware n�o � r�pido o suficiente, n�o haver� tempo de sono entre os quadros. 
 * Uma vez que esta poderia cair todos os outros segmentos, o ciclo ir� gerar um sinal 
 * de rendimento de cada vez que a contagem � obtida noDelaysPerYield. 
 * <p> 
 * O excesso de tempo para renderizar um quadro ser�o acumulados a cada ciclo. 
 * Quando isso representa um erro grande o suficiente para pular um quadro, o 
 * quadro ser� automaticamente skiped. O n�mero m�ximo de quadros skiped num �nico 
 * passo � dado pelo atributo maxFrameSkips. <p> Para iniciar o MainLoop chamar o 
 * m�todo run (). Por conveni�ncia, a classe <code> Game </ code> � umaclasse abstrata.
 */

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
	private BufferedImage tela, hud, hp, mp;
	private final static Stroke stroke = new BasicStroke(1.5f);
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
     * 		N�mero de atualiza��es desejadas por segundo.
     * @param maxFrameSkips 
     * 		N�mero m�ximo de quadro que pode ser ignorado se o hardware 
     * 		gr�fico n�o � r�pido o suficiente para acompanhar os altos desejados.
     * @param noDelaysPerYield 
     * 		Se o hardware n�o � r�pido o suficiente para permitir uma dellay 
     * 		entre dois quadros , o atraso ser� aplicada neste balc�o, portanto, 
     * 		outros segmentos podem processar suas a��es.
     */
	public Game(int ups, int maxFrameSkips, int noDelaysPerYield) {
		super();

		if (ups < 1)
			throw new IllegalArgumentException(
					"Voc� deve exibir, pelo menos, um quadro por segundo!");

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
     * @param ups N�mero de atualiza��es desejadas por segundo.
     */
	public Game(int ups) {
		this(ups, DEFAULT_MAX_FRAME_SKIPS, DEFAULT_NO_DELAYS_PER_YIELD);
	}

	/**
     * Crie um novo objeto Game e uma atualiza��o por segundo de 80 frames.
     * 
     */
	public Game() {
		this(DEFAULT_UPS);
	}

	/**
     * Dormir a quantidade de tempo determinado. Como o m�todo sleep ()
     * da classe thread n�o � preciso, o overSleepTime ser� calculado.
     * 
     * @param nanos N�mero de nanossegundos para dormir.
     * @throws InterruptedException Se o segmento foi interrompida. 
     */
	private void sleep(long nanos) throws InterruptedException {
		noDelays = 0;
		long beforeSleep = System.nanoTime();
		Thread.sleep(nanos / 1000000L, (int) (nanos % 1000000L));
		overSleepTime = System.nanoTime() - beforeSleep - nanos;
	}

	/**
     * Se o n�mero de quadros sem um atraso � atingido , for�ar a 
     * Thread a ceder, permitindo que outros segmentos para processar .
     */
	private void yieldIfNeed() {
		if (++noDelays == noDelaysPerYield) {
			Thread.yield();
			noDelays = 0;
		}
	}

	/**
     * Calcula o tempo de sono com base no c�lculo do loop anterior. 
     * Para atingir este tempo , o tempo de apresenta��o de imagem 
     * ser�o subtra�dos pelo tempo decorrido no �ltimo c�lculo (afterTime - beforeTime). 
     * Ent�o , se no circuito anterior, houve um tempo oversleep , 
     * este tamb�m ser� subtra�do , de modo que o sistema pode compensar 
     * este prolongamento.
     */
	private long calculateSleepTime() {
		return desiredUpdateTime - (afterTime - beforeTime) - overSleepTime;
	}

	 /**
     * Ir o n�mero de quadros de acordo com o excesso determinado momento. 
     * Isso permite que o jogo seja executado com a mesma velocidade, 
     * mesmo se o computador tem uma taxa de quadros menor do que o necess�rio.
     * O n�mero total de saltos est�o limitados a MAX_FRAME_SKIPS .
     * 
     * @param exceededTime
     * 		O tempo excedido . Se o tempo � suficiente maior para ignorar um 
     * 		ou mais quadros , eles ser�o ignorados.
     * @return O tempo de excesso remanescente , ap�s os pulos.
     */
	private void skipFramesInExcessTime(int tick) {
		int skips = 0;
		while ((excessTime > desiredUpdateTime) && (skips < maxFrameSkips)) {
			excessTime -= desiredUpdateTime;
			logica(tick);
			skips++;
		}
	}

	 /**
     * Executa o loop principal. Este m�todo n�o � thread-safe 
     * e n�o deve ser chamado mais de uma vez .
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
     * Este evento ocorre antes da primeira itera��o do ciclo, e apenas uma vez.
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

		elementos.put("elementos", new ArrayList<Elemento>());

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
					"images/hud.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		onLoad();

	}

	/**
     * O m�todo processLogics atualiza todos os estados jogo de objetos. 
     * O jogo ir� process�-lo do modelo de dados e definir a pr�xima 
     * situa��o de jogo que ir� ser pintado na tela.
     */
	private void logica(int tick) {

		double x = elemento.pos.x;
		double y = elemento.pos.y;

		onUpdate(tick);

		if (elemento.pos.x >= 400
				&& elemento.pos.x <= cenarios.get(currentCenario).getPos().width - 400)
			cenarios.get(currentCenario).getPos().x -= elemento.pos.x - x;
		if (elemento.pos.y >= 300
				&& elemento.pos.y <= cenarios.get(currentCenario).getPos().height - 250)
			cenarios.get(currentCenario).getPos().y -= elemento.pos.y - y;

		if (cenarios.get(currentCenario).getPos().x > 0) {
			cenarios.get(currentCenario).getPos().x = 0;
		}
		if (cenarios.get(currentCenario).getPos().y > 0) {
			cenarios.get(currentCenario).getPos().y = 0;
		}
		if (cenarios.get(currentCenario).getPos().x < -cenarios.get(
				currentCenario).getPos().width
				+ mainWindow.getWidth()) {
			cenarios.get(currentCenario).getPos().x = -cenarios.get(
					currentCenario).getPos().width
					+ mainWindow.getWidth();
		}
		if (cenarios.get(currentCenario).getPos().y < -cenarios.get(
				currentCenario).getPos().height
				+ mainWindow.getHeight()) {
			cenarios.get(currentCenario).getPos().y = -cenarios.get(
					currentCenario).getPos().height
					+ mainWindow.getHeight();
		}
	}

	/**
     * Tintas os gr�ficos prestados na tela.
     */
	private void paint(Graphics2D g) {

		onRender(g);

		Graphics2D g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
		g2d.drawImage(tela, (int) cenarios.get(currentCenario).getPos().x,
				(int) cenarios.get(currentCenario).getPos().y, null);
		g2d.drawImage(
				getRoundImage(
						hp,
						0,
						hp.getHeight()
								- ((((Principal) elemento).getHp() * hp
										.getHeight()) / ((Principal) elemento)
										.getHpMax())), 0,
				mainWindow.getHeight() - hud.getHeight(), null);
		g2d.drawImage(
				getRoundImage(
						mp,
						0,
						mp.getHeight()
								- ((((Principal) elemento).getMp() * mp
										.getHeight()) / ((Principal) elemento)
										.getMpMax())), 690,
				mainWindow.getHeight() - hud.getHeight(), null);
		g2d.drawImage(hud, 0, mainWindow.getHeight() - hud.getHeight(), null);
		g2d.setColor(Color.white);
		g2d.setFont(new Font("", Font.BOLD, 12));
		g2d.drawString(((Principal) elemento).getHp() + " / "
				+ ((Principal) elemento).getHpMax(), 28, 524);
		g2d.drawString(((Principal) elemento).getMp() + " / "
				+ ((Principal) elemento).getMpMax(), 728, 524);
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
		this.elemento = e;
		addElemento(e);
	}

	protected void loadCenario(String cenario, String tileset) {
		if (!cenarios.containsKey(cenario)) {
			Scenery scenery = new Scenery(cenario, tileset);
			this.cenarios.put(cenario, scenery);
			this.currentCenario = cenario;
		} else {
			System.out.println("cenario ja carregado");
		}
	}

	protected void currentCenario(String current) {
		Scenery scenery = cenarios.get(current);
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
		for (Elemento elemento : elementos.get("elementos")) {
			if (elemento.ativo) {
				elemento.mover(this);
				elemento.update(tick);
			}
		}
	}

	protected void renderElementos(Graphics2D g) {
		for (Elemento elemento : elementos.get("elementos")) {
			if (elemento.visivel) {
				elemento.render(g);
				// g.drawRect((int) elemento.getColisao().getX(), (int)
				// elemento.getColisao().getY(),
				// (int) elemento.getColisao().getWidth(), (int)
				// elemento.getColisao().getHeight());
			}
		}
	}

	public Elemento getPrincipal() {
		return elemento;
	}

	public HashMap<String, ArrayList<Elemento>> getElementos() {
		return elementos;
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