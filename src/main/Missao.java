package main;

import java.util.ArrayList;

public class Missao {

	private int nivel;
	private String objetivo;
	private int exp;
	private int[] idPecaGeometricaRequerida;
	private int recompenca;
	private boolean ativa;
	private boolean concluida;
	private String layerLiberada;

	public Missao(int nivel, String objetivo, int exp, int[] ids,
			int recompenca, String layerLiberada) {
		this.nivel = nivel;
		this.objetivo = objetivo;
		this.exp = exp;
		this.idPecaGeometricaRequerida = ids;
		this.recompenca = recompenca;
		this.layerLiberada = layerLiberada;
		this.ativa = false;
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

	public int[] getIdPecaGeometricaRequerida() {
		return idPecaGeometricaRequerida;
	}

	public void setIdPecaGeometricaRequerida(int[] idPecaGeometricaRequerida) {
		this.idPecaGeometricaRequerida = idPecaGeometricaRequerida;
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

	public String getLayerLiberada() {
		return layerLiberada;
	}

	public void setLayerLiberada(String layerLiberada) {
		this.layerLiberada = layerLiberada;
	}

	public boolean concluirMissao(ArrayList<PecaGeometrica> arrayList) {
		int acertos = 0;
		for (int i = 0; i < idPecaGeometricaRequerida.length; i++) {
			for (PecaGeometrica pecaGeometrica : arrayList) {
				if (idPecaGeometricaRequerida[i] == pecaGeometrica.id) {
					acertos++;
				}
			}
		}
		if (acertos == idPecaGeometricaRequerida.length)
			return true;
		else
			return false;
	}
}
