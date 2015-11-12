package game;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import coreBase.Game;
import coreBase.Interacao;
import coreBase.Personagem;

public class NPC extends Personagem {

	private String menssagem;
	private Missao missao;
	private Componente d;
	protected BufferedImage rosto;

	public NPC(int x, int y, int width, int height, int direcao, int numFrames,
			String img, Missao missao) throws IOException {
		super(x, y, width, height, numFrames, img);
		direction = direcao;
		this.menssagem = "seu nivel ainda � baixo para esta miss�o, \n fale com outro alde�o e suba de nivel.";
		this.missao = missao;

		try {
			rosto = ImageIO.read(getClass().getClassLoader().getResource(
					"images/" + img + "Rosto.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mover(Interacao i) {
	}

	public String getDialogo(Game game) {
		Principal p = (Principal) game.getElementoPrincipal();
		if (missao.isAtiva()) {
			if (missao.isConcluida()) {
				return "Eu n�o tenho mais nem um pedido para voc�, fale com outra pessoa \ntalvez ele tenham alguma pedido pra voc�.";
			} else {
				if (missao.concluirMissao(p.getInventario()
						.getPecasgeometricas())) {
					d = new MyDialog(game.getMainWindow(), false, missao, p
							.getInventario().getPecasgeometricas());
					if (d.isRetorno()) {
						missao.setConcluida(true);
					} else {
						return "voc� errou a pergunta volte mais tarde quando voc� souber a resposta.";
					}
					if (missao.getRecompenca() < -100) {
						for (int i = 0; i < missao
								.getIdPecaGeometricaRequerida().length; i++) {
							p.getInventario().remove(
									missao.getIdPecaGeometricaRequerida()[i]);
						}
						return "Obrigado pela sua coopera��o.\n Boa sorte.";
					} else if (missao.getRecompenca() >= -100
							&& missao.getRecompenca() < -50) {
						for (int i = 0; i < missao
								.getIdPecaGeometricaRequerida().length; i++) {
							p.getInventario().remove(
									missao.getIdPecaGeometricaRequerida()[i]);
						}
						p.getInventario().add(
								new Quadrado(0, 0,
										(missao.getRecompenca() + 50) * -1));
						return "parabens voc� concluiu a miss�o com sucesso. \nem troca das pe�as que voc� me deu lhe darei uma recompen�a";
					} else if (missao.getRecompenca() >= -50
							&& missao.getRecompenca() < 0) {
						for (int i = 0; i < missao
								.getIdPecaGeometricaRequerida().length; i++) {
							p.getInventario().remove(
									missao.getIdPecaGeometricaRequerida()[i]);
						}
						p.getInventario()
								.add(new Triangulo(0, 0, missao.getRecompenca()
										* -1));
						return "parabens voc� concluiu a miss�o com sucesso. \nem troca das pe�as que voc� me deu lhe darei uma recompen�a";
					} else if (missao.getRecompenca() > 0
							&& missao.getRecompenca() <= 50) {
						p.getInventario().add(
								new Triangulo(0, 0, missao.getRecompenca()));
						return "parabens voc� concluiu a miss�o com sucesso. \nvoc� recebera uma recompen�a";
					} else if (missao.getRecompenca() > 50
							&& missao.getRecompenca() <= 100) {
						p.getInventario()
								.add(new Quadrado(0, 0,
										missao.getRecompenca() - 50));
						return "parabens voc� concluiu a miss�o com sucesso. \nvoc� recebera uma recompen�a";
					} else if (missao.getRecompenca() > 100) {
						for (int i = 0; i < missao
								.getIdPecaGeometricaRequerida().length; i++) {
							p.getInventario().remove(
									missao.getIdPecaGeometricaRequerida()[i]);
						}
						game.removerObstaculos(missao.getRecompenca() - 100);
						game.configLayerBase(missao.getLayerLiberada());
						return "parabens voc� concluiu a miss�o com sucesso. \nagora voc� poder� passar por novos caminhos";
					}
					p.maisExp(missao.getExp());
					return "muito bem.\n obrigado.";
				} else
					return "voc� ainda n�o completou a miss�o volte quando voc� conseguir \na Pe�a";
			}
		} else if (p.level >= missao.getNivel()) {
			missao.setAtiva(true);
			return missao.getObjetivo();
		} else {
			return menssagem;
		}
	}
}
