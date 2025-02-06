package miaYeelight.ux.pannelli;

import miaYeelight.Main;
import miaYeelight.lang.Strings;
import miaYeelight.net.Connessione;
import miaYeelight.ux.schermo.Schermo;

import javax.swing.*;
import java.io.IOException;
import java.io.Serial;

public class PannelloConnessione extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JLabel desc = new JLabel("");

    public void setTestoDescrizione(final String testo) {
        desc.setText(testo);
    }

    public PannelloConnessione(Main ref, boolean modoAutomatizzato) {
        super();
        setLayout(null);
        JLabel intestazione = new JLabel(Strings.get("PannelloConnessione.1"));
        JButton connetti = new JButton(Strings.get("PannelloConnessione.2"));
        JTextField ip = new JTextField("");
        intestazione.setBounds(Schermo.d(10), 0, Schermo.d(510), Schermo.d(40));
        add(intestazione);
        desc.setBounds(Schermo.d(10), Schermo.d(50), Schermo.d(510), Schermo.d(40));
        desc.setFont(ref.f2);
        add(desc);
        ip.setHorizontalAlignment(SwingConstants.CENTER);
        ip.setText(Connessione.IP_DEFAULT_B_012 + Connessione.IP_DEFAULD_B_3);
        connetti.setFocusable(false);
        connetti.addActionListener(click -> {
            if (ref.getConnessione().connettiA(ip.getText())) {
                String[] proprieta = ref.getConnessione().scaricaProprieta();
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
            JButton avviaScansione = new JButton(Strings.get("PannelloConnessione.4"));
            avviaScansione.setFocusable(false);
            avviaScansione.addActionListener(click -> {
                try {
                    ref.getConnessione().connetti(false);
                    String[] proprieta = ref.getConnessione().scaricaProprieta();
                    ref.tornaStatico();
                    ref.configuraPannelloPrincipaleConStatoLampada(proprieta);
                    ref.schedulaExtListener();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ip.setBounds(Schermo.d(10), Schermo.d(100), Schermo.d(200), Schermo.d(40));
            avviaScansione.setBounds(Schermo.d(370), Schermo.d(100), Schermo.d(140), Schermo.d(40));
            connetti.setBounds(Schermo.d(220), Schermo.d(100), Schermo.d(140), Schermo.d(40));
            add(avviaScansione);
            avviaScansione.setEnabled(true);
        }

        setBounds(0, 0, Schermo.d(530), Schermo.d(150));
        setVisible(true);
    }

}
