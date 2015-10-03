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

public class Scenery {

	private Rectangle2D.Double pos;
	private int width, height;
	private int tileWidth;
	private int tileHeight;
	protected String orientation;
	protected String encoding;
	protected String compression;
	private ArrayList<Rectangle2D> objects;
	private HashMap<String, int[][]> camadas;
	private HashMap<String, String> datas;
	private HashMap<String, BufferedImage> layers;
	private BufferedImage image;

	public Scenery(String diretorio) {
		this.datas = new HashMap<String, String>();
		this.camadas = new HashMap<String, int[][]>();
		this.objects = new ArrayList<Rectangle2D>();
		pos = new Rectangle2D.Double(0, 0, 0, 0);
		carregaCenario(diretorio);
		try {
			this.image = ImageIO.read(getClass().getClassLoader().getResource(
					"images/tileset.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		montarMatriz();
		load();
	}

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

	public void load() {
		layers = new HashMap<String, BufferedImage>();
		for (Map.Entry<String, int[][]> entry : camadas.entrySet()) {
			BufferedImage layer = new BufferedImage((int) pos.width,
					(int) pos.height, BufferedImage.TYPE_4BYTE_ABGR);
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
						Rectangle2D.Double r = new Rectangle2D.Double(
								(j * tileWidth), (i * tileHeight), tileWidth,
								tileHeight);
						objects.add(r);

					}

					layers.put(entry.getKey(), layer);
				}
			}
		}
	}

	public static void print(int[][] maze) {
		int height = maze.length;
		int width = maze[0].length;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				System.out.print(maze[i][j] + "  ");
			}
			System.out.println();
		}
	}

	private void carregaCenario(String resource) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"scenerys/" + resource + ".tmx");
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

			this.orientation = docElement.getAttribute("orientation");

			this.width = Integer.parseInt(docElement.getAttribute("width"));
			this.height = Integer.parseInt(docElement.getAttribute("height"));
			this.tileWidth = Integer.parseInt(docElement
					.getAttribute("tilewidth"));
			this.tileHeight = Integer.parseInt(docElement
					.getAttribute("tileheight"));
			NodeList layerNodes = docElement.getElementsByTagName("layer");
			for (int i = 0; i < layerNodes.getLength(); i++) {
				Element current = (Element) layerNodes.item(i);
				String name = current.getAttribute("name");
				Element dataNode = (Element) current.getElementsByTagName(
						"data").item(0);
				Node cdata = dataNode.getFirstChild();
				String data = cdata.getNodeValue().trim();
				this.datas.put(name, data);
				this.encoding = dataNode.getAttribute("encoding");
				this.compression = dataNode.getAttribute("compression");
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

	public void render(Graphics2D g, String name) {
		g.drawImage(layers.get(name), 0, 0, null);
	}

	public void render(Graphics2D g) {
		for (BufferedImage img : layers.values()) {
			g.drawImage(img, 0, 0, null);
		}
	}

	public void renderBase(Graphics2D g) {
		for (String layer : layers.keySet()) {
			if (layer.contains("Base"))
				render(g, layer);
			
		}
	}
	public void renderCima(Graphics2D g) {
		for (String layer : layers.keySet()) {
			if (!layer.contains("Base"))
				render(g, layer);
		}
	}

	public Rectangle2D.Double getPos() {
		return pos;
	}

	public ArrayList<Rectangle2D> getObjects() {
		return objects;
	}
}
