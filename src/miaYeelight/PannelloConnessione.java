package miaYeelight;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class PannelloConnessione extends JPanel {
	private static final long serialVersionUID = 1L;
	JLabel desc = new JLabel("");
	
	PannelloConnessione () {
		super();
		setLayout(null);
		JLabel 	intestazione = new JLabel("Connessione alla lampadina");
		JButton connetti = new JButton("Connetti");
		JTextField ip = new JTextField("");
		intestazione.setBounds(10, 0, 510, 40);
		add(intestazione);
		desc.setBounds(10, 50, 510, 40);
		desc.setFont(Main.f2);
		add(desc);
		ip.setText("192.168.1.100");
		ip.setBounds(10, 100, 350, 40);
		add(ip);
		connetti.setBounds(370, 100, 140, 40);
		connetti.setFocusable(false);
		connetti.addActionListener(click -> {
			Main.connettiA(ip.getText());
		});
		add(connetti);
		setBounds(0,0,530,150);
		setVisible(true);
	}
}