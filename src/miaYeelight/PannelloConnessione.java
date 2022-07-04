package miaYeelight;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class PannelloConnessione extends JPanel {
	private static final long serialVersionUID = 1L;
	JLabel desc = new JLabel("");

	PannelloConnessione (Main ref, boolean modoAutomatizzato) {
		super();
		setLayout(null);
		JLabel 	intestazione = new JLabel(Strings.get("PannelloConnessione.1"));
		JButton connetti = new JButton(Strings.get("PannelloConnessione.2"));
		JTextField ip = new JTextField("");
		intestazione.setBounds(Schermo.d(10), 0, Schermo.d(510), Schermo.d(40));
		add(intestazione);
		desc.setBounds(Schermo.d(10), Schermo.d(50), Schermo.d(510), Schermo.d(40));
		desc.setFont(Main.f2);
		add(desc);
		ip.setHorizontalAlignment(SwingConstants.CENTER);
		ip.setText(Connessione.IPDefaultB012 + Connessione.IPDefauldB3);
		connetti.setFocusable(false);
		connetti.addActionListener(click -> {
			if (ref.connessione.connettiA(ip.getText())) {
				String[] proprieta = ref.connessione.scaricaProprieta();
				ref.tornaStatico();
				ref.configuraPannelloPrincipaleConStatoLampada(proprieta);
				ref.schedulaExtListener();
			}
		});
		add(connetti);
		add(ip);

		if (modoAutomatizzato) {
			ip.setBounds(Schermo.d(10), Schermo.d(100), Schermo.d(350), Schermo.d(40));
			connetti.setBounds(Schermo.d(370), Schermo.d(100), Schermo.d(150), Schermo.d(40));
		} else {
			JButton avviaScansione = new JButton (Strings.get("PannelloConnessione.4"));
			avviaScansione.setFocusable(false);
			avviaScansione.addActionListener(click -> {
				try {
					ref.connessione.connetti(false);
					String[] proprieta = ref.connessione.scaricaProprieta();
					ref.tornaStatico();
					ref.configuraPannelloPrincipaleConStatoLampada(proprieta);
					ref.schedulaExtListener();
				} 
				catch (IOException e) {e.printStackTrace();}
			});
			ip.setBounds(Schermo.d(10), Schermo.d(100), Schermo.d(200), Schermo.d(40));
			avviaScansione.setBounds(Schermo.d(370), Schermo.d(100), Schermo.d(140), Schermo.d(40));
			connetti.setBounds(Schermo.d(220), Schermo.d(100), Schermo.d(140), Schermo.d(40));
			add(avviaScansione);
			avviaScansione.setEnabled(true);
		}

		setBounds(0,0,Schermo.d(530),Schermo.d(150));
		setVisible(true);
	}
}
