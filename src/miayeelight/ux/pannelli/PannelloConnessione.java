package miayeelight.ux.pannelli;

import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.net.Connessione;

import javax.swing.*;
import java.io.IOException;
import java.io.Serial;

import static miayeelight.Main.log;
import static miayeelight.ux.schermo.Schermo.d;

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
        JLabel intestazione = new JLabel(Strings.get(PannelloConnessione.class, "1"));
        JButton connetti = new JButton(Strings.get(PannelloConnessione.class, "2"));
        JTextField ip = new JTextField("");
        intestazione.setBounds(d(10), 0, d(510), d(40));
        add(intestazione);
        desc.setBounds(d(10), d(50), d(510), d(40));
        desc.setFont(ref.caratterePiccolo);
        add(desc);
        ip.setHorizontalAlignment(SwingConstants.CENTER);
        ip.setText(Connessione.IP_DEFAULT_B_012 + Connessione.IP_DEFAULT_B_3);
        connetti.setFocusable(false);
        connetti.addActionListener(click -> {
            if (ref.getConnessione().connettiA(ip.getText(), Strings.get(Connessione.class, "2"))) {
                String[] proprieta = ref.getConnessione().scaricaProprieta();
                ref.tornaStatico();
                ref.configuraPannelloPrincipaleConStatoLampada(proprieta);
                ref.schedulaExtListener();
            }
        });
        add(connetti);
        add(ip);

        if (modoAutomatizzato) {
            ip.setBounds(d(10), d(100), d(350), d(40));
            connetti.setBounds(d(370), d(100), d(150), d(40));
        } else {
            JButton avviaScansione = new JButton(Strings.get(PannelloConnessione.class, "4"));
            avviaScansione.setFocusable(false);
            avviaScansione.addActionListener(click -> {
                try {
                    if (ref.getConnessione().connetti(false)) {
                        String[] proprieta = ref.getConnessione().scaricaProprieta();
                        ref.tornaStatico();
                        ref.configuraPannelloPrincipaleConStatoLampada(proprieta);
                        ref.schedulaExtListener();
                    }
                } catch (IOException e) {
                    log(e);
                }
            });
            ip.setBounds(d(10), d(100), d(200), d(40));
            avviaScansione.setBounds(d(370), d(100), d(140), d(40));
            connetti.setBounds(d(220), d(100), d(140), d(40));
            add(avviaScansione);
            avviaScansione.setEnabled(true);
        }

        setBounds(0, 0, d(530), d(150));
        setVisible(true);
    }

}
