package main;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * informações da classe Scenery
 * 
 * @author Gerson
 */
public class Scenery {

	private Rectangle2D.Double pos;
	private String name;
	private HashMap<Integer, String> destino;
	private int width, height;
	private int tileWidth;
	private int tileHeight;
	private String source;
	private HashMap<String, ArrayList<Elemento>> obstaculos;
	private HashMap<String, int[][]> camadas;
	private HashMap<String, String> datas;
	private HashMap<String, BufferedImage> layers;
	private BufferedImage image;
	private ArrayList<String> layersBase;
	private ArrayList<String> layersSuperficie;

	/**
	 * 
	 * Metodo Construtor da classe Scenery
	 * 
	 * @param diretorio
	 *            Diretorio de onde esta o cenario a ser carregado
	 * @param tileset
	 *            nome do arquivo tileset para este cenario
	 */

	public Scenery(String diretorio) {
		this.datas = new HashMap<String, String>();
		this.camadas = new HashMap<String, int[][]>();
		this.obstaculos = new HashMap<String, ArrayList<Elemento>>();
		this.layersBase = new ArrayList<String>();
		this.layersSuperficie = new ArrayList<String>();
		this.destino = new HashMap<Integer, String>();
		pos = new Rectangle2D.Double();
		carregaCenario(diretorio);
		try {
			this.image = ImageIO.read(getClass().getClassLoader().getResource(
					"images/" + source));
		} catch (IOException e) {
			e.printStackTrace();
		}
		montarMatriz();
		desenhaCamadas();
	}

	/**
	 * Metodo que constroi toda a matriz carregada apartir do arquivo do
	 * diretorio informado
	 * 
	 * @return void
	 */

	private void montarMatriz() {
		try {
			for (Map.Entry<String, String> entry : datas.entrySet()) {
				if (entry.getValue() != null && entry.getValue().length() > 0) {
					int camada[][] = new int[height][width];
					StringTokenizer linhas = new StringTokenizer(
							entry.getValue(), "\n");
					int i = 0;
					while (linhas.hasMoreTokens()) {
						StringTokenizer colunas = new StringTokenizer(
								linhas.nextToken(), ",");
						int j = 0;
						while (colunas.hasMoreTokens()) {
							camada[i][j] = Integer
									.parseInt(colunas.nextToken());
							j++;
						}
						i++;
					}
					camadas.put(entry.getKey(), camada);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Desenha as camadas que compõe o cenário
	 * 
	 * @return void
	 */

	private void desenhaCamadas() {
		layers = new HashMap<String, BufferedImage>();
		for (Map.Entry<String, int[][]> entry : camadas.entrySet()) {
			BufferedImage layer = new BufferedImage((int) pos.width,
					(int) pos.height, BufferedImage.TYPE_4BYTE_ABGR);
			ArrayList<Elemento> obs = new ArrayList<Elemento>();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int tile = (entry.getValue()[i][j] != 0) ? (entry
							.getValue()[i][j] - 1) : 0;
					int tileRow = (tile / 8) | 0;
					int tileCol = (tile % 8) | 0;
					layer.getGraphics().drawImage(image, (j * tileWidth),
							(i * tileHeight), (j * tileWidth) + tileWidth,
							(i * tileHeight) + tileHeight,
							(tileCol * tileWidth), (tileRow * tileHeight),
							(tileCol * tileWidth) + tileWidth,
							(tileRow * tileHeight) + tileHeight, null);
					if (entry.getKey().equalsIgnoreCase("obstaculos")
							&& tile != 0) {
						Obstaculo o = new Obstaculo((j * tileWidth),
								(i * tileHeight), tileWidth, tileHeight, tile);
						obs.add(o);
					} else if (entry.getKey().equalsIgnoreCase("in")
							&& tile != 0) {
						Obstaculo o = new Obstaculo((j * tileWidth),
								(i * tileHeight), tileWidth, tileHeight, tile);
						obs.add(o);
					} else if (entry.getKey().equalsIgnoreCase("out")
							&& tile != 0) {
						Obstaculo o = new Obstaculo((j * tileWidth),
								(i * tileHeight), tileWidth, tileHeight, tile);
						obs.add(o);
					}

				}
			}
			layers.put(entry.getKey(), layer);
			if (entry.getKey().equalsIgnoreCase("obstaculos"))
				obstaculos.put(entry.getKey(), obs);
			else if (entry.getKey().equalsIgnoreCase("in"))
				obstaculos.put(entry.getKey(), obs);
			else if (entry.getKey().equalsIgnoreCase("out"))
				obstaculos.put(entry.getKey(), obs);
		}
		image = null;
		System.gc();
	}

	/**
	 * Carrega o cenário a partir de um diretorio
	 * 
	 * @param diretorio
	 *            Diretorio de onde esta o cenario
	 * 
	 * @return void
	 */

	private void carregaCenario(String diretorio) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"scenerys/" + diretorio + ".tmx");
		this.name = diretorio;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					return new InputSource(
							new ByteArrayInputStream(new byte[0]));
				}
			});

			Document doc = builder.parse(is);
			Element docElement = doc.getDocumentElement();

			this.width = Integer.parseInt(docElement.getAttribute("width"));
			this.height = Integer.parseInt(docElement.getAttribute("height"));
			this.tileWidth = Integer.parseInt(docElement
					.getAttribute("tilewidth"));
			this.tileHeight = Integer.parseInt(docElement
					.getAttribute("tileheight"));

			NodeList tileNodes = docElement.getElementsByTagName("tileset");
			Element currente = (Element) tileNodes.item(0);
			Element imageNode = (Element) currente
					.getElementsByTagName("image").item(0);
			this.source = imageNode.getAttribute("source");

			NodeList layerNodes = docElement.getElementsByTagName("layer");
			for (int i = 0; i < layerNodes.getLength(); i++) {
				Element current = (Element) layerNodes.item(i);
				String name = current.getAttribute("name");
				Element dataNode = (Element) current.getElementsByTagName(
						"data").item(0);
				Node cdata = dataNode.getFirstChild();
				String data = cdata.getNodeValue().trim();
				this.datas.put(name, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (width * tileWidth < 800 && height * tileHeight < 576) {
				System.out.println("erro");
			}
			pos.width = width * tileWidth;
			pos.height = height * tileHeight;
		}

	}

	public String getName() {
		return name;
	}

	/**
	 * Metodo que desenha o uma camada especificada por parametro
	 * 
	 * @param g
	 *            Graphics onde a camada será desenhado
	 * @param name
	 *            Nome da camada a ser desenhada
	 * 
	 * @return void
	 */
	public void render(Graphics2D g, String name) {
		g.drawImage(layers.get(name), 0, 0, null);
	}

	/**
	 * Metodo que desenha todas as camadas de uma vez
	 * 
	 * @param g
	 *            Graphics onde todas as camadas seram desenhadas
	 * 
	 * @return void
	 */
	public void render(Graphics2D g) {
		for (BufferedImage img : layers.values()) {
			g.drawImage(img, 0, 0, null);
		}
	}

	/**
	 * Metodo que desenha as camadas bases
	 * 
	 * @param g
	 *            Graphics onde as camadas bases seram desenhadas
	 * 
	 * @return void
	 */
	public void renderBase(Graphics2D g) {
		for (String string : layersBase) {
			render(g, string);
		}
	}

	/**
	 * Metodo que desenha as camadas da superficie
	 * 
	 * @param g
	 *            Graphics onde as camadas da superficie seram desenhadas
	 * 
	 * @return void
	 */
	public void renderSuperficie(Graphics2D g) {
		for (String string : layersSuperficie) {
			render(g, string);
		}
	}

	/**
	 * Metodo para a a definição das camadas bases
	 * 
	 * @param s
	 *            Nome da camada a ser definida como camada base
	 * 
	 * @return void
	 */
	public void configLayerBase(String s) {
		layersBase.add(s);
	}

	/**
	 * Metodo para a a definição das camadas da superficie
	 * 
	 * @param s
	 *            Nome da camada a ser definida como camada da superficie
	 * 
	 * @return void
	 */
	public void configLayerSuperficie(String s) {
		layersSuperficie.add(s);
	}

	/**
	 * get do posicionamento do cenário
	 * 
	 * @return Rectangle2D.Double
	 */
	public Rectangle2D.Double getPos() {
		return pos;
	}

	/**
	 * get dos obstaculos que tem no cenrário
	 * 
	 * @return ArrayList<Obstaculo>
	 */
	public ArrayList<Elemento> getObstaculos() {
		return obstaculos.get("obstaculos");
	}

	/**
	 * get dos lugares onde o personagem se teleporta
	 * 
	 * @return ArrayList<Obstaculo>
	 */
	public ArrayList<Elemento> getIn() {
		return obstaculos.get("in");
	}

	/**
	 * get dos lugares onde o personagem se teleporta para outros cenarios
	 * 
	 * @return ArrayList<Obstaculo>
	 */
	public ArrayList<Elemento> getOut() {
		return obstaculos.get("out");
	}

	/**
	 * Metodo que adiciona um cenario destino
	 * 
	 * @param cenario
	 *            Nome do cenario destino
	 * @param local
	 *            numero do local para o qual o personagem ira se teleportar
	 * 
	 * @return void
	 */
	public void addTeleport(String cenario, int local) {
		destino.put(local, cenario);
	}

	public String getDestino(int local) {
		if (destino.containsKey(local)) {
			return destino.get(local);
		}
		return "";
	}

}
