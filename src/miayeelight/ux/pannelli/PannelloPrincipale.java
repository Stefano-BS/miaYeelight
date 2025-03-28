package miayeelight.ux.pannelli;

import miayeelight.Configurazione;
import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.net.Connessione;
import miayeelight.ux.componenti.Slider;
import miayeelight.ux.componenti.TestoRotondo;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static miayeelight.Main.log;
import static miayeelight.Main.testoDisattivo;
import static miayeelight.ux.schermo.FiltroEspressioneRegolare.FILTRO_TEMPI;
import static miayeelight.ux.schermo.Schermo.coloreDaTemperatura;
import static miayeelight.ux.schermo.Schermo.d;

public class PannelloPrincipale extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JButton seguiWinAccent = new JButton(Strings.get(PannelloPrincipale.class, "9"));
    private final JButton seguiSchermo = new JButton(Strings.get(PannelloPrincipale.class, "10"));
    private final JButton accendi = new JButton(Strings.get(PannelloPrincipale.class, "0"));
    private final JButton winAccent = new JButton(Strings.get(PannelloPrincipale.class, "4"));
    private final JButton animazioni = new JButton(Strings.get(PannelloPrincipale.class, "5"));
    private final JButton timer = new JButton(Strings.get(PannelloPrincipale.class, "1"));
    private final JSlider lum = Slider.fab(Slider.PRESETLUM);
    private final JSlider hue = Slider.fab(Slider.PRESETTON);
    private final JSlider sat = Slider.fab(Slider.PRESETSAT);
    private final JSlider temp = Slider.fab(Slider.PRESETCT);
    private final JLabel descLuce = new JLabel("\uD83D\uDD05");
    private final JLabel descTemp = new JLabel("\uD83C\uDF21");
    private final JLabel descHue = new JLabel("\uD83C\uDF08");
    private final JLabel descSat = new JLabel("✴");

    private final JPanel anteprima = new JPanel();

    private final Main ref;

    private Timer cRovescia = null;

    private boolean ultimoModo = false; // false: T, true: C
    private boolean win10AccentDisegnati;

    public PannelloPrincipale(Main ref) {
        super(null);
        setBackground(Main.bg);
        this.ref = ref;

        int y = d(10);
        accendi.setBounds(d(10), y, d(225), d(40));

        final JButton impostazioni = new JButton("🔨");
        impostazioni.setBounds(d(245), y, d(40), d(40));
        impostazioni.addActionListener(click -> SwingUtilities.invokeLater(ref::apriPannelloImpostazioni));

        timer.setBounds(d(295), y, d(225), d(40));

        accendi.addActionListener(click -> SwingUtilities.invokeLater(() -> {
            if (accendi.getText().equals(Strings.get(PannelloPrincipale.class, "0"))) {
                accendi.setText(Strings.get(PannelloPrincipale.class, "12"));
                Connessione.istanza().accendi();
                abilitaControlli(true, true);
            } else {
                accendi.setText(Strings.get(PannelloPrincipale.class, "0"));
                Connessione.istanza().spegni();
                abilitaControlli(false, false);
            }
        }));

        timer.addActionListener(click -> {
            try {
                final JPanel finestra = new JPanel(null);

                final JLabel icona = new JLabel();
                icona.setIcon(ref.yee);
                icona.setBounds(0, 0, d(40), d(40));

                final JLabel domanda = new JLabel(Strings.get(PannelloPrincipale.class, "14"));
                domanda.setFont(ref.caratterePiccolo);
                domanda.setHorizontalAlignment(SwingConstants.HORIZONTAL);

                final TestoRotondo tempo = new TestoRotondo();
                tempo.setHorizontalAlignment(SwingConstants.HORIZONTAL);
                ((AbstractDocument) tempo.getDocument()).setDocumentFilter(FILTRO_TEMPI);
                tempo.setColumns(10);

                final FontMetrics fm = domanda.getFontMetrics(ref.caratterePiccolo);
                final double larghezza = fm.stringWidth(domanda.getText()) * 1.1;

                domanda.setBounds(d(50), 0, (int) larghezza, d(25));
                tempo.setBounds(d(50) + (int) (larghezza / 10), d(35), (int) (larghezza * 0.8), d(30));
                finestra.setPreferredSize(new Dimension(d(50) + (int) larghezza, d(70)));

                finestra.add(icona);
                finestra.add(domanda);
                finestra.add(tempo);

                final int azione = JOptionPane.showOptionDialog(ref.getFrame(), finestra, Strings.get(PannelloPrincipale.class, "15"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

                if (azione == JOptionPane.OK_OPTION && !tempo.getText().isEmpty()) {
                    Connessione.istanza().timer(Integer.parseInt(tempo.getText()));
                }
            } catch (Exception e) {
                log(e);
            }
        });

        accendi.setFocusable(false);
        timer.setFocusable(false);
        impostazioni.setFocusable(false);

        add(accendi);
        add(timer);
        add(impostazioni);
        y += d(50);

        win10AccentDisegnati = Configurazione.getMostraWin10();
        winAccent.setBounds(d(10), y, d(250), d(40));
        seguiWinAccent.setBounds(d(270), y, d(250), d(40));
        winAccent.setFocusable(false);
        seguiWinAccent.setFocusable(false);
        winAccent.addActionListener(click -> SwingUtilities.invokeLater(ref::cambiaColoreDaAccent));
        seguiWinAccent.addActionListener(click -> SwingUtilities.invokeLater(ref::tienitiAggiornataSuWindows));
        add(winAccent);
        add(seguiWinAccent);

        if (win10AccentDisegnati) {
            y += d(50);
        } else {
            winAccent.setVisible(false);
            seguiWinAccent.setVisible(false);
        }

        seguiSchermo.setBounds(d(10), y, d(250), d(40));
        seguiSchermo.addActionListener(click -> SwingUtilities.invokeLater(ref::seguiColoreSchermo));
        seguiSchermo.setFocusable(false);
        add(seguiSchermo);

        animazioni.setBounds(d(270), y, d(250), d(40));
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> SwingUtilities.invokeLater(ref::apriPannelloAnimazioni));
        add(animazioni);
        y += d(50);

        descLuce.setBounds(d(10), y, d(80), d(40));
        add(descLuce);

        lum.setBounds(d(100), y + d(10), d(420), d(30));
        lum.setBackground(Main.bg);
        lum.setForeground(Color.WHITE);
        lum.setMaximum(100);
        lum.setMinimum(0);
        lum.setMajorTickSpacing(10);
        lum.setMinorTickSpacing(5);
        lum.setPaintTicks(true);
        lum.addChangeListener(s -> SwingUtilities.invokeLater(() -> aggiornaInterfaccia(null)));
        lum.addChangeListener(eventoJSlider(e -> Connessione.istanza().setBr(Math.max(lum.getValue(), 1))));
        add(lum);
        y += d(40);

        descTemp.setBounds(d(10), y, d(80), d(40));
        add(descTemp);

        temp.setBounds(d(100), y + d(10), d(420), d(30));
        temp.setBackground(Main.bg);
        temp.setForeground(Color.WHITE);
        temp.setOpaque(false);
        temp.setMaximum(6500);
        temp.setMinimum(1700);
        temp.setValue(5000);
        temp.setSnapToTicks(true);
        temp.setMinorTickSpacing(100);
        temp.addChangeListener(s -> SwingUtilities.invokeLater(() -> aggiornaInterfaccia(false)));
        temp.addChangeListener(eventoJSlider(e -> Connessione.istanza().temperatura(temp.getValue())));
        add(temp);
        y += d(40);

        descHue.setBounds(d(10), y, d(80), d(40));
        add(descHue);

        hue.setBounds(d(100), y + d(10), d(420), d(30));
        hue.setBackground(Main.bg);
        hue.setForeground(Color.WHITE);
        hue.setMaximum(359);
        hue.setMinimum(0);
        hue.setOpaque(false);
        hue.addChangeListener(s -> SwingUtilities.invokeLater(() -> aggiornaInterfaccia(true)));
        hue.addChangeListener(eventoJSlider(e -> Connessione.istanza().setHS(hue.getValue(), sat.getValue())));
        add(hue);
        y += d(40);

        descSat.setBounds(d(10), y, d(80), d(40));
        add(descSat);

        sat.setBounds(d(100), y + d(10), d(420), d(30));
        sat.setBackground(Main.bg);
        sat.setForeground(Color.WHITE);
        sat.setMaximum(100);
        sat.setMinimum(0);
        sat.setOpaque(false);
        sat.addChangeListener(s -> SwingUtilities.invokeLater(() -> aggiornaInterfaccia(true)));
        sat.addChangeListener(eventoJSlider(e -> Connessione.istanza().setHS(hue.getValue(), sat.getValue())));
        add(sat);
        y += d(50);

        anteprima.setBounds(d(10), y, d(510), d(20));
        anteprima.setFocusable(false);
        aggiornaAnteprima();
        add(anteprima);
        y += d(30);

        setSize(d(530), y);
        setVisible(true);
    }

    private void aggiornaAnteprima() {
        if (ultimoModo) {
            anteprima.setBackground(new Color(Color.HSBtoRGB(((float) hue.getValue()) / 359, ((float) sat.getValue()) / 100, ((float) (20 + lum.getValue() * 0.8)) / 100)));
        } else {
            anteprima.setBackground(coloreDaTemperatura((double) (temp.getValue() - 1700) / 48, ((double) lum.getValue()) / 100));
        }
    }

    private ChangeListener eventoJSlider(ActionListener azione) {
        return e -> {
            if (cRovescia == null) {
                cRovescia = new Timer(200, azione);
            } else if (cRovescia.isRunning()) {
                cRovescia.stop();
                cRovescia = new Timer(200, azione);
            }
            cRovescia.setRepeats(false);
            cRovescia.start();
        };
    }

    public void abilitaControlli(boolean abilita, final Boolean lampadaAccesa) {
        seguiWinAccent.setEnabled(abilita);
        winAccent.setEnabled(abilita);
        hue.setEnabled(abilita);
        sat.setEnabled(abilita);
        temp.setEnabled(abilita);

        if (Boolean.FALSE.equals(lampadaAccesa)) {
            Set.of(lum, seguiSchermo, animazioni).forEach(b -> b.setEnabled(false));
            Set.of(seguiSchermo, animazioni).forEach(b -> b.setForeground(testoDisattivo));
        } else if (Boolean.TRUE.equals(lampadaAccesa)) {
            Set.of(lum, seguiSchermo, animazioni).forEach(b -> b.setEnabled(true));
            Set.of(seguiSchermo, animazioni).forEach(b -> b.setForeground(Color.WHITE));
        }
    }

    public void riscriviEtichette(boolean seguiWinAccentInEsecuzione, boolean seguiSchermoInEsecuzione) {
        accendi.setText(accendi.getText().contains("\uD83D\uDCA1") ? Strings.get(PannelloPrincipale.class, "0") : Strings.get(PannelloPrincipale.class, "12"));
        winAccent.setText(Strings.get(PannelloPrincipale.class, "4"));
        seguiWinAccent.setText(seguiWinAccentInEsecuzione ? Strings.get(Main.class, "19") : Strings.get(PannelloPrincipale.class, "9"));
        seguiSchermo.setText(seguiSchermoInEsecuzione ? Strings.get(Main.class, "19") : Strings.get(PannelloPrincipale.class, "10"));
        animazioni.setText(Strings.get(PannelloPrincipale.class, "5"));
        timer.setText(Strings.get(PannelloPrincipale.class, "1"));
    }

    public void mostraWin10Accent(boolean mostraWin10) {
        if (!win10AccentDisegnati && mostraWin10) {
            movimentaComponenti(false);
        }

        if (win10AccentDisegnati && !mostraWin10) {
            movimentaComponenti(true);
        }

        win10AccentDisegnati = mostraWin10;
    }

    private void movimentaComponenti(final boolean sopra) {
        final int spostamento = sopra ? -d(50) : +d(50);
        for (final Component c : getComponents()) {
            if (c.equals(winAccent) || c.equals(seguiWinAccent)) {
                continue;
            }

            final Rectangle dimensioni = c.getBounds();
            if (dimensioni.getY() > d(10)) {
                c.setBounds(new Rectangle((int) dimensioni.getX(), (int) (dimensioni.getY() + spostamento), (int) dimensioni.getWidth(), (int) dimensioni.getHeight()));
            }
        }

        winAccent.setVisible(!sopra);
        seguiWinAccent.setVisible(!sopra);

        final Rectangle dimensioniPannello = getBounds();
        setBounds(new Rectangle(getX(), getY(), (int) dimensioniPannello.getWidth(), (int) dimensioniPannello.getHeight() + spostamento));
        ref.getFrame().revalidate();
    }

    public JButton getSeguiWinAccent() {
        return seguiWinAccent;
    }

    public void aggiornaInterfaccia(final Boolean ultimoModo) {
        if (ultimoModo != null) {
            this.ultimoModo = ultimoModo;

            descSat.setEnabled(ultimoModo);
            descHue.setEnabled(ultimoModo);
            descTemp.setEnabled(!ultimoModo);
        }

        descLuce.setText("\uD83D\uDD05  %d%%".formatted(lum.getValue()));
        descTemp.setText("\uD83C\uDF21  %dK".formatted(temp.getValue()));
        descHue.setText("\uD83C\uDF08  %dº".formatted(hue.getValue()));
        descSat.setText("✴  %d%%".formatted(sat.getValue()));

        aggiornaAnteprima();
    }

    public void aggiornaValoriSlider(final Integer vLum, final Integer vTemp, final Integer vHue, final Integer vSat) {
        final Map<JSlider, Integer> daAggiornare = new HashMap<>();
        if (vLum != null) {
            daAggiornare.put(lum, vLum);
        }

        if (vTemp != null) {
            daAggiornare.put(temp, vTemp);
        }

        if (vHue != null) {
            daAggiornare.put(hue, vHue);
        }

        if (vSat != null) {
            daAggiornare.put(sat, vSat);
        }

        for (final var e : daAggiornare.entrySet()) {
            final var listeners = e.getKey().getChangeListeners();
            Arrays.asList(listeners).forEach(e.getKey()::removeChangeListener);
            e.getKey().setValue(e.getValue());
            Arrays.asList(listeners).forEach(e.getKey()::addChangeListener);
        }
    }

    public JButton getSeguiSchermo() {
        return seguiSchermo;
    }

    public JButton getAccendi() {
        return accendi;
    }

    public Timer getCRovescia() {
        return cRovescia;
    }

}