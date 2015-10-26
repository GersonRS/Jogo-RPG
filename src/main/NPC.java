package main;

public class NPC extends Personagem {

	private String menssagem;
	private Missao missao;

	public NPC(int x, int y, int width, int height, int numFrames, String img,
			Missao missao) {
		super(x, y, width, height, numFrames, img);
		direction = 2;
		this.menssagem = "seu nivel ainda � baixo para esta miss�o, \n fale com outro alde�o e suba de nivel.";
		this.missao = missao;
	}

	@Override
	public void mover(Iteracao i) {
	}

	public String getDialogo(Game1 game) {
		if (missao.isAtiva()) {
			if (missao.isConcluida()) {
				return "voce completou esta miss�o, fale com outra pessoa \ntalvez ele tenham alguma miss�o pra voc�.";
			} else {
				if (missao.concluirMissao(game.getElementoPrincipal()
						.getInventario().getPecasgeometricas())) {
					game.getElementoPrincipal().getInventario()
							.add(new Quadrado(0, 0, missao.getRecompenca()));
					game.getElementoPrincipal().maisExp(missao.getExp());
					missao.setConcluida(true);
					return "parabens voc� concluiu a miss�o com sucesso. \nvoc� recebera uma recompen�a";
				} else
					return "voc� ainda n�o completou a miss�o volte quando voc� conseguir \na Pe�a";
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
