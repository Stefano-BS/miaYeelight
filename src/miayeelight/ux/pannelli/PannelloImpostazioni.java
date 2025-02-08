package miayeelight.ux.pannelli;

import miayeelight.Configurazione;
import miayeelight.Main;
import miayeelight.lang.Strings;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static miayeelight.Configurazione.IMPOSTAZIONI_NON_MODIFICABILI;
import static miayeelight.ux.schermo.Schermo.d;

public class PannelloImpostazioni extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Main ref;
    private int Y = 10;
    private final Map<String, JComboBox<String>> impostazioni = new HashMap<>();

    public PannelloImpostazioni(final Main ref) {
        super();
        setLayout(null);

        this.ref = ref;

        JLabel intestazione = new JLabel(Strings.get(PannelloImpostazioni.class, "0"));
        intestazione.setBounds(d(10), d(Y), d(510), d(40));
        Y += 50;
        add(intestazione);

        Configurazione.getConfCorrente().entrySet().stream().filter(i -> !IMPOSTAZIONI_NON_MODIFICABILI.contains(i.getKey())).forEach(i -> addSetting(i.getKey(), i.getValue(), true));
        Configurazione.getConfCorrente().entrySet().stream().filter(i -> IMPOSTAZIONI_NON_MODIFICABILI.contains(i.getKey())).forEach(i -> addSetting(i.getKey(), i.getValue(), false));

        JButton applica = new JButton(Strings.get(PannelloImpostazioni.class, "1"));
        applica.setBounds(d(10), d(Y), d(250), d(40));
        applica.setFocusable(false);
        applica.addActionListener(click -> Configurazione.applicaConfigurazione( //
                impostazioni.entrySet().stream().map(impostazione -> Map.entry(impostazione.getKey(), String.valueOf(impostazione.getValue().getSelectedItem()))) //
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
        add(applica);

        JButton chiudi = new JButton(Strings.get(PannelloImpostazioni.class, "2"));
        chiudi.setBounds(d(270), d(Y), d(250), d(40));
        chiudi.setFocusable(false);
        chiudi.addActionListener(click -> ref.tornaStatico());
        chiudi.setEnabled(true);
        add(chiudi);
        Y += 50;

        setSize(new Dimension(d(530), d(Y)));
        setPreferredSize(new Dimension(d(530), d(Y)));
        setVisible(true);
    }

    private void addSetting(final String nomeImpostazione, final String valoreCorrente, final boolean modificabile) {
        final JLabel nome = new JLabel(Strings.get(PannelloImpostazioni.class, nomeImpostazione));
        nome.setBounds(d(10), d(Y), d(360), d(40));
        nome.setFont(ref.f2);
        add(nome);

        if (modificabile) {
            final JComboBox<String> valore = new JComboBox<>(Configurazione.getValoriAmmissibili(nomeImpostazione));
            valore.setSelectedItem(" " + valoreCorrente);
            valore.setBounds(d(380), d(Y), d(140), d(40));
            valore.setAlignmentX(SwingConstants.CENTER);
            valore.setUI(new BasicComboBoxUI() {

                @Override
                protected JButton createArrowButton() {
                    final JButton bottone = new JButton("∨");
                    bottone.setBackground(new Color(70, 70, 70));
                    bottone.setForeground(Color.WHITE);
                    bottone.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    return bottone;
                }
            });

            add(valore);
            impostazioni.put(nomeImpostazione, valore);
        } else {
            final JLabel valore = new JLabel(valoreCorrente);
            valore.setBounds(d(350), d(Y), d(170), d(40));
            valore.setHorizontalAlignment(SwingConstants.CENTER);
            add(valore);
        }

        Y += 50;
    }

}
