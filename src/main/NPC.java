package main;

public class NPC extends Personagem {

	private String menssagem;
	private Missao missao;

	public NPC(int x, int y, int width, int height, int direcao, int numFrames,
			String img, Missao missao) {
		super(x, y, width, height, numFrames, img);
		direction = direcao;
		this.menssagem = "seu nivel ainda é baixo para esta missão, \n fale com outro aldeão e suba de nivel.";
		this.missao = missao;
	}

	@Override
	public void mover(Iteracao i) {
	}

	public String getDialogo(Game1 game) {
		if (missao.isAtiva()) {
			if (missao.isConcluida()) {
				return "voce já completou esta missão, fale com outra pessoa \ntalvez ele tenham alguma missão pra você.";
			} else {
				if (missao.concluirMissao(game.getElementoPrincipal()
						.getInventario().getPecasgeometricas())) {
					missao.setConcluida(true);
					if (missao.getRecompenca() < 0) {
						for (int i = 0; i < missao
								.getIdPecaGeometricaRequerida().length; i++) {
							game.getElementoPrincipal()
									.getInventario()
									.remove(missao
											.getIdPecaGeometricaRequerida()[i]);
						}
						return "Obrigado pela sua cooperação.\n Boa sorte.";
					} else if (missao.getRecompenca() > 0
							&& missao.getRecompenca() <= 50) {
						game.getElementoPrincipal()
								.getInventario()
								.add(new Triangulo(0, 0, missao.getRecompenca()));
						return "parabens você concluiu a missão com sucesso. \nvocê recebera uma recompença";
					} else if (missao.getRecompenca() > 50
							&& missao.getRecompenca() <= 100) {
						game.getElementoPrincipal()
								.getInventario()
								.add(new Quadrado(0, 0,
										missao.getRecompenca() - 50));
						return "parabens você concluiu a missão com sucesso. \nvocê recebera uma recompença";
					} else if (missao.getRecompenca() > 100) {
						for (int i = 0; i < missao
								.getIdPecaGeometricaRequerida().length; i++) {
							game.getElementoPrincipal()
									.getInventario()
									.remove(missao
											.getIdPecaGeometricaRequerida()[i]);
						}
						game.removerObstaculos(missao.getRecompenca()-100);
						game.configLayerBase(missao.getLayerLiberada());
						return "parabens você concluiu a missão com sucesso. \nagora você poderá passar por novos caminhos";
					}
					game.getElementoPrincipal().maisExp(missao.getExp());
					return "muito bem.\n obrigado.";
				} else
					return "você ainda não completou a missão volte quando você conseguir \na Peça";
			}
		} else if (game.getElementoPrincipal().level >= missao.getNivel()) {
			missao.setAtiva(true);
			return missao.getObjetivo();
		} else {
			return menssagem;
		}
	}
}
