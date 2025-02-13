package miayeelight.ux.pannelli;

import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.net.Connessione;
import miayeelight.ux.componenti.TestoRotondo;

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
        int y = 0;

        final JLabel intestazione = new JLabel(Strings.get(PannelloConnessione.class, "1"));
        final JButton connetti = new JButton(Strings.get(PannelloConnessione.class, "2"));

        intestazione.setBounds(d(10), y, d(510), d(40));
        add(intestazione);
        y += d(50);

        desc.setBounds(d(10), y, d(510), d(40));
        desc.setFont(ref.caratterePiccolo);
        add(desc);
        y += d(50);

        final JTextField ip = new TestoRotondo("");
        ip.setHorizontalAlignment(SwingConstants.CENTER);
        ip.setText(Connessione.IP_DEFAULT_B_012 + Connessione.IP_DEFAULT_B_3);
        connetti.setFocusable(false);
        connetti.addActionListener(click -> {
            if (Connessione.istanza().connettiA(ip.getText(), Strings.get(Connessione.class, "2"))) {
                final Connessione.StatoLampada stato = Connessione.istanza().ottieniStatoAttuale();
                ref.tornaStatico();
                ref.configuraPannelloPrincipaleConStatoLampada(stato);
                ref.schedulaExtListener();
            }
        });
        add(connetti);
        add(ip);

        if (modoAutomatizzato) {
            ip.setBounds(d(10), y, d(350), d(40));
            connetti.setBounds(d(370), y, d(150), d(40));
        } else {
            JButton avviaScansione = new JButton(Strings.get(PannelloConnessione.class, "4"));
            avviaScansione.setFocusable(false);
            avviaScansione.addActionListener(click -> {
                try {
                    if (Connessione.istanza().connetti(false)) {
                        final Connessione.StatoLampada stato = Connessione.istanza().ottieniStatoAttuale();
                        ref.tornaStatico();
                        ref.configuraPannelloPrincipaleConStatoLampada(stato);
                        ref.schedulaExtListener();
                    }
                } catch (IOException e) {
                    log(e);
                }
            });
            ip.setBounds(d(10), y, d(200), d(40));
            connetti.setBounds(d(220), y, d(145), d(40));
            avviaScansione.setBounds(d(375), y, d(145), d(40));
            add(avviaScansione);
            avviaScansione.setEnabled(true);
        }

        setBounds(0, 0, d(530), d(150));
        setVisible(true);
    }

}
