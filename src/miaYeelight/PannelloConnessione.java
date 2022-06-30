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
		intestazione.setBounds(10, 0, 510, 40);
		add(intestazione);
		desc.setBounds(10, 50, 510, 40);
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
			ip.setBounds(10, 100, 350, 40);
			connetti.setBounds(370, 100, 140, 40);
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
			ip.setBounds(10, 100, 200, 40);
			avviaScansione.setBounds(370, 100, 140, 40);
			connetti.setBounds(220, 100, 140, 40);
			add(avviaScansione);
			avviaScansione.setEnabled(true); ///// !
		}

		setBounds(0,0,530,150);
		setVisible(true);
	}
}
