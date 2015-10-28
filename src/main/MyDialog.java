package main;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class MyDialog extends JDialog {

	private boolean retorno;
	double resposta = 0.0f;
	private JButton[] alternativas;

	private static final long serialVersionUID = 1L;

	public MyDialog(Frame owner, boolean modal, Missao missao,
			ArrayList<PecaGeometrica> arrayList) {
		super(owner, modal);
		alternativas = new JButton[3];
		setSize(600, 400);
		setLocationRelativeTo(null);
		setUndecorated(true);
		setResizable(false);
		setLayout(null);
		JLabel fundo = new JLabel(new ImageIcon(getClass().getClassLoader()
				.getResource("images/fundo.png")));
		fundo.setBounds(0, 0, 600, 400);
		add(fundo);
		String tipo = "";
		String tamanho = "";
		int x = 0;
		for (int i = 0; i < missao.getIdPecaGeometricaRequerida().length; i++) {
			for (PecaGeometrica pecaGeometrica : arrayList) {
				if (pecaGeometrica.getId() == missao
						.getIdPecaGeometricaRequerida()[i]) {
					if (pecaGeometrica instanceof Triangulo) {
						tipo += "Triangulo, ";
						tamanho += pecaGeometrica.getTamanho() + ", ";
					} else {
						tipo += "Quadrado, ";
						tamanho += pecaGeometrica.getTamanho() + ", ";
					}
					resposta += pecaGeometrica.calculaArea();
					JLabel img = new JLabel(new ImageIcon(pecaGeometrica.image));
					img.setBounds(x > 2 ? 200 + x - 3 * 100 : 200 + x * 100,
							100 + (x > 2 ? 100 : 0), 32, 32);
					fundo.add(img);
					x++;
				}
			}
		}
		String s = "Você tem um "
				+ tipo
				+ "medindo "
				+ tamanho
				+ "respectivamente, ambos de altura e de largura.\nquanto é a soma das areas de todas estas peçãs geometricas?";
		if (s.contains("\n")) {
			StringTokenizer linhas = new StringTokenizer(s, "\n");
			int y = 0;
			while (linhas.hasMoreTokens()) {
				JLabel menssagem = new JLabel(linhas.nextToken());
				menssagem.setForeground(Color.WHITE);
				menssagem.setBounds(20, 20 + y * 30, 560, 20);
				fundo.add(menssagem);
				y++;
			}
		}

		Random r = new Random();

		alternativas[0] = new JButton("" + resposta * 2);
		alternativas[1] = new JButton("" + resposta / 2);
		alternativas[2] = new JButton("" + resposta + 2);

		alternativas[r.nextInt(3)].setText("" + resposta);

		for (int i = 0; i < alternativas.length; i++) {
			alternativas[i].setBounds(25 + i * 200, 340, 150, 30);
			fundo.add(alternativas[i]);
		}
		alternativas[0].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (alternativas[0].getText().equalsIgnoreCase("" + resposta)) {
					retorno = true;
					dispose();
				} else {
					retorno = false;
					dispose();
				}
			}
		});
		alternativas[1].addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (alternativas[1].getText().equalsIgnoreCase("" + resposta)) {
					retorno = true;
					dispose();
				} else {
					retorno = false;
					dispose();
				}
			}
		});
		alternativas[2].addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (alternativas[2].getText().equalsIgnoreCase("" + resposta)) {
					retorno = true;
					dispose();
				} else {
					retorno = false;
					dispose();
				}
			}
		});

		setVisible(true);
	}

	public boolean isRetorno() {
		return retorno;
	}

}
