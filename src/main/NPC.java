package main;

public class NPC extends Personagem {

	private String menssagem;
	private Missao missao;

	public NPC(int x, int y, int width, int height, int numFrames, String img,
			Missao missao) {
		super(x, y, width, height, numFrames, img);
		direction = 2;
		this.menssagem = "seu nivel ainda é baixo para esta missão, \n fale com outro aldeão e suba de nivel.";
		this.missao = missao;
	}

	@Override
	public void mover(Iteracao i) {
	}

	public String getDialogo(Game1 game) {
		if (missao.isAtiva()) {
			if (missao.isConcluida()) {
				return "voce completou esta missão, fale com outra pessoa \ntalvez ele tenham alguma missão pra você.";
			} else {
				if (missao.concluirMissao(game.getElementoPrincipal()
						.getInventario().getPecasgeometricas())) {
					game.getElementoPrincipal().getInventario()
							.add(new Quadrado(0, 0, missao.getRecompenca()));
					game.getElementoPrincipal().maisExp(missao.getExp());
					missao.setConcluida(true);
					return "parabens você concluiu a missão com sucesso. \nvocê recebera uma recompença";
				} else
					return "você ainda não completou a missão volte quando você conseguir \na Peça";
			}
		} else if (game.getElementoPrincipal().level >= missao.getNivel()) {
			missao.setAtiva(true);
			if (!missao.getTipo().equalsIgnoreCase("")) {
				game.addElemento(
						missao.getCenario(),
						missao.getTipo().equalsIgnoreCase("quadrado") ? new Quadrado(
								(int) missao.getPos().getX(), (int) missao
										.getPos().getY(), missao
										.getIdPecaGeometricaRequerida())
								: new Triangulo((int) missao.getPos().getX(),
										(int) missao.getPos().getY(), missao
												.getIdPecaGeometricaRequerida()));
			}
			return missao.getObjetivo();
		} else {
			return menssagem;
		}
	}
}
