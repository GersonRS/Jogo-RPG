package main;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Missao {

	private int nivel;
	private String objetivo;
	private int exp;
	private int idPecaGeometricaRequerida;
	private Point2D pos;
	private String cenario;
	private int recompenca;
	private boolean ativa;
	private boolean concluida;
	private String tipo;

	public Missao(int nivel, String objetivo, int exp,
			int idPecaGeometricaRequerida, String tipo, int recompenca, int x,
			int y, String cenario) {
		this.tipo = tipo;
		this.nivel = nivel;
		this.objetivo = objetivo;
		this.exp = exp;
		this.idPecaGeometricaRequerida = idPecaGeometricaRequerida;
		this.recompenca = recompenca;
		this.ativa = false;
		this.pos = new Point2D.Double(x, y);
		this.cenario = cenario;
	}

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public String getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(String objetivo) {
		this.objetivo = objetivo;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getIdPecaGeometricaRequerida() {
		return idPecaGeometricaRequerida;
	}

	public void setIdPecaGeometricaRequerida(int idPecaGeometricaRequerida) {
		this.idPecaGeometricaRequerida = idPecaGeometricaRequerida;
	}

	public Point2D getPos() {
		return pos;
	}

	public void setPos(Point2D pos) {
		this.pos = pos;
	}

	public int getRecompenca() {
		return recompenca;
	}

	public void setRecompenca(int recompenca) {
		this.recompenca = recompenca;
	}

	public boolean isAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	public boolean isConcluida() {
		return concluida;
	}

	public void setConcluida(boolean concluida) {
		this.concluida = concluida;
	}

	public String getCenario() {
		return cenario;
	}

	public void setCenario(String cenario) {
		this.cenario = cenario;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public boolean concluirMissao(ArrayList<PecaGeometrica> arrayList) {
		for (PecaGeometrica pecaGeometrica : arrayList) {
			if (idPecaGeometricaRequerida == pecaGeometrica.id)
				return true;
		}

		return false;
	}
}
