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
    private int vPos = 10;
    private final Map<String, JComboBox<String>> impostazioni = new HashMap<>();

    public PannelloImpostazioni(final Main ref) {
        super(null);

        this.ref = ref;

        JLabel intestazione = new JLabel(Strings.get(PannelloImpostazioni.class, "0"));
        intestazione.setFont(ref.carattereGrande);
        intestazione.setBounds(d(10), d(vPos), d(510), d(40));
        vPos += 50;
        add(intestazione);

        Configurazione.getConfCorrente().entrySet().stream().filter(i -> !IMPOSTAZIONI_NON_MODIFICABILI.contains(i.getKey())).forEach(i -> addSetting(i.getKey(), i.getValue(), true));
        Configurazione.getConfCorrente().entrySet().stream().filter(i -> IMPOSTAZIONI_NON_MODIFICABILI.contains(i.getKey())).forEach(i -> addSetting(i.getKey(), i.getValue(), false));

        JButton applica = new JButton(Strings.get(PannelloImpostazioni.class, "1"));
        applica.setBounds(d(10), d(vPos), d(250), d(40));
        applica.setFocusable(false);
        applica.addActionListener(click -> Configurazione.applicaConfigurazione( //
                impostazioni.entrySet().stream().map(impostazione -> Map.entry(impostazione.getKey(), String.valueOf(impostazione.getValue().getSelectedItem()))) //
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
        add(applica);

        JButton chiudi = new JButton(Strings.get(PannelloImpostazioni.class, "2"));
        chiudi.setBounds(d(270), d(vPos), d(250), d(40));
        chiudi.setFocusable(false);
        chiudi.addActionListener(click -> ref.tornaStatico());
        chiudi.setEnabled(true);
        add(chiudi);
        vPos += 50;

        setSize(new Dimension(d(530), d(vPos)));
        setVisible(true);
    }

    private void addSetting(final String nomeImpostazione, final String valoreCorrente, final boolean modificabile) {
        final JLabel nome = new JLabel(Strings.get(PannelloImpostazioni.class, nomeImpostazione));
        nome.setBounds(d(10), d(vPos), d(360), d(40));
        nome.setFont(ref.caratterePiccolo);
        add(nome);

        if (modificabile) {
            final JComboBox<String> valore = new JComboBox<>(Configurazione.getValoriAmmissibili(nomeImpostazione));
            valore.setSelectedItem(" " + valoreCorrente);
            valore.setBounds(d(380), d(vPos), d(140), d(40));
            valore.setAlignmentX(SwingConstants.CENTER);
            valore.setUI(new BasicComboBoxUI() {

                @Override
                protected JButton createArrowButton() {
                    final JButton bottone = new JButton("∨");
                    bottone.setBackground(Color.darkGray);
                    bottone.setForeground(Color.WHITE);
                    bottone.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    return bottone;
                }
            });

            add(valore);
            impostazioni.put(nomeImpostazione, valore);
        } else {
            final JLabel valore = new JLabel(valoreCorrente);
            valore.setBounds(d(350), d(vPos), d(170), d(40));
            valore.setHorizontalAlignment(SwingConstants.CENTER);
            add(valore);
        }

        vPos += 50;
    }

}
