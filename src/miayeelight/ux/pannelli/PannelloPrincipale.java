package miayeelight.ux.pannelli;

import miayeelight.Configurazione;
import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.ux.componenti.Slider;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.Serial;

import static miayeelight.Main.log;
import static miayeelight.ux.schermo.Schermo.d;

public class PannelloPrincipale extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JButton seguiWinAccent;
    private final JButton seguiSchermo;
    private final JButton accendi = new JButton(Strings.get(PannelloPrincipale.class, "0"));
    private final JButton winAccent = new JButton(Strings.get(PannelloPrincipale.class, "4"));
    private final JButton animazioni = new JButton(Strings.get(PannelloPrincipale.class, "5"));
    private final JButton timer = new JButton(Strings.get(PannelloPrincipale.class, "1"));
    private final JSlider luminosita = Slider.fab(Slider.PRESETLUM);
    private final JSlider hue = Slider.fab(Slider.PRESETTON);
    private final JSlider sat = Slider.fab(Slider.PRESETSAT);
    private final JSlider temperatura = Slider.fab(Slider.PRESETCT);
    private final JPanel anteprima = new JPanel();
    private final JLabel descLuce;
    private final JLabel descTemp;
    private final JLabel descCol;
    private final Main ref;
    private Timer crovescia = null;
    private boolean modoDiretto = true;
    private boolean ultimaModalita = false; // false: T, true: C
    private boolean win10AccentDisegnati;

    public PannelloPrincipale(Main ref) {
        setLayout(null);
        setBackground(Main.bg);
        this.ref = ref;

        descLuce = new JLabel(Strings.get(PannelloPrincipale.class, "16"));
        descTemp = new JLabel(Strings.get(PannelloPrincipale.class, "18"));
        descCol = new JLabel(Strings.get(PannelloPrincipale.class, "8"));
        seguiWinAccent = new JButton(Strings.get(PannelloPrincipale.class, "9"));
        seguiSchermo = new JButton(Strings.get(PannelloPrincipale.class, "10"));

        int Y = d(10);
        accendi.setBounds(d(10), Y, d(225), d(40));

        final JButton impostazioni = new JButton("🔨");
        impostazioni.setBounds(d(245), Y, d(40), d(40));
        impostazioni.addActionListener(click -> ref.apriPannelloImpostazioni());

        timer.setBounds(d(295), Y, d(225), d(40));

        accendi.addActionListener(click -> {
            if (accendi.getText().equals(Strings.get(PannelloPrincipale.class, "0"))) {
                accendi.setText(Strings.get(PannelloPrincipale.class, "12"));
                ref.getConnessione().accendi();
            } else {
                accendi.setText(Strings.get(PannelloPrincipale.class, "0"));
                ref.getConnessione().spegni();
            }
        });
        Integer[] listaTimer = new Integer[50];
        for (int i = 1; i <= 10; i++) {
            listaTimer[i - 1] = i;
        }
        for (int i = 11; i <= 30; i++) {
            listaTimer[i - 1] = 2 * i - 10;
        }
        for (int i = 31; i <= 40; i++) {
            listaTimer[i - 1] = 5 * i - 100;
        }
        for (int i = 41; i <= 50; i++) {
            listaTimer[i - 1] = 10 * i - 300;
        }
        timer.addActionListener(click -> {
            try {
                int tempo = (Integer) JOptionPane.showInputDialog(ref.getFrame(), Strings.get(PannelloPrincipale.class, "14"), Strings.get(PannelloPrincipale.class, "15"), JOptionPane.QUESTION_MESSAGE, ref.yee, listaTimer, 1);
                ref.getConnessione().timer(tempo);
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
        Y += d(50);

        win10AccentDisegnati = Configurazione.getMostraWin10();
        winAccent.setBounds(d(10), Y, d(250), d(40));
        seguiWinAccent.setBounds(d(270), Y, d(250), d(40));
        winAccent.setFocusable(false);
        seguiWinAccent.setFocusable(false);
        winAccent.addActionListener(click -> ref.cambiaColoreDaAccent());
        seguiWinAccent.addActionListener(click -> ref.tienitiAggiornataSuWindows());
        add(winAccent);
        add(seguiWinAccent);

        if (win10AccentDisegnati) {
            Y += d(50);
        } else {
            winAccent.setVisible(false);
            seguiWinAccent.setVisible(false);
        }

        seguiSchermo.setBounds(d(10), Y, d(250), d(40));
        seguiSchermo.addActionListener(click -> ref.seguiColoreSchermo());
        seguiSchermo.setFocusable(false);
        add(seguiSchermo);

        animazioni.setBounds(d(270), Y, d(250), d(40));
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> ref.apriPannelloAnimazioni());
        add(animazioni);
        Y += d(70);

        descLuce.setBounds(d(10), Y, d(510), d(40));
        descLuce.setOpaque(false);
        add(descLuce);
        Y += d(40);
        luminosita.setBounds(d(10), Y, d(510), d(40));
        luminosita.setBackground(Main.trasparente);
        luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100);
        luminosita.setMinimum(0);
        luminosita.setMajorTickSpacing(10);
        luminosita.setMinorTickSpacing(5);
        luminosita.setPaintTicks(true);
        luminosita.addChangeListener(s -> descLuce.setText(Strings.get(PannelloPrincipale.class, "16") + luminosita.getValue() + "%"));
        luminosita.addChangeListener(s -> aggiornaAnteprima());
        luminosita.addChangeListener(s -> aggiornaAnteprima());
        luminosita.addChangeListener(eventoJSlider(e -> ref.getConnessione().setBr(Math.max(luminosita.getValue(), 1))));
        luminosita.setOpaque(false);
        add(luminosita);
        Y += d(40);
        descTemp.setBounds(d(10), Y, d(510), d(40));
        add(descTemp);
        Y += d(40);
        temperatura.setBounds(d(10), Y, d(510), d(40));
        temperatura.setBackground(Main.trasparente);
        temperatura.setForeground(Color.WHITE);
        temperatura.setOpaque(false);
        temperatura.setMaximum(6500);
        temperatura.setMinimum(1700);
        temperatura.setValue(5000);
        temperatura.setSnapToTicks(true);
        temperatura.setMinorTickSpacing(100);
        temperatura.setPaintTicks(true);
        temperatura.addChangeListener(s -> descTemp.setText(Strings.get(PannelloPrincipale.class, "18") + temperatura.getValue() + "K"));
        temperatura.addChangeListener(s -> ultimaModalita = false);
        temperatura.addChangeListener(s -> aggiornaAnteprima());
        temperatura.addChangeListener(eventoJSlider(e -> ref.getConnessione().temperatura(temperatura.getValue())));
        add(temperatura);
        Y += d(40);
        descCol.setBounds(d(10), Y, d(510), d(40));
        add(descCol);
        Y += d(40);
        hue.setBounds(d(10), Y, d(250), d(40));
        sat.setBounds(d(270), Y, d(250), d(40));
        hue.setBackground(Main.trasparente);
        hue.setForeground(Color.WHITE);
        sat.setBackground(Main.trasparente);
        sat.setForeground(Color.WHITE);
        hue.setMaximum(359);
        hue.setMinimum(0);
        sat.setMaximum(100);
        sat.setMinimum(0);
        hue.setOpaque(false);
        sat.setOpaque(false);
        add(hue);
        add(sat);
        hue.addChangeListener(s -> ultimaModalita = true);
        hue.addChangeListener(s -> aggiornaAnteprima());
        sat.addChangeListener(s -> ultimaModalita = true);
        sat.addChangeListener(s -> aggiornaAnteprima());
        hue.addChangeListener(eventoJSlider(e -> ref.getConnessione().setHS(hue.getValue(), sat.getValue())));
        sat.addChangeListener(eventoJSlider(e -> ref.getConnessione().setHS(hue.getValue(), sat.getValue())));
        Y += d(50);
        anteprima.setBounds(d(10), Y, d(510), d(20));
        anteprima.setFocusable(false);
        aggiornaAnteprima();
        add(anteprima);
        Y += d(30);
        setSize(d(530), Y);
        setVisible(true);
    }

    public void aggiornaAnteprima() {
        if (ultimaModalita) {
            anteprima.setBackground(new Color(Color.HSBtoRGB(((float) hue.getValue()) / 359, ((float) sat.getValue()) / 100, ((float) (luminosita.getValue() > 80 ? 100 : luminosita.getValue() + 20)) / 100)));
        } else {
            anteprima.setBackground(new Color( //
                    (int) (200 * (luminosita.getValue() > 50 ? 1 : 0.5 + (float) luminosita.getValue() / 100)), //
                    (int) (((double) temperatura.getValue() / 37 + 75) * (luminosita.getValue() > 50 ? 1 : 0.5 + (float) luminosita.getValue() / 100)), //
                    (int) ((temperatura.getValue() - 1700) / 18.8f * (luminosita.getValue() > 50 ? 1 : 0.5 + (float) luminosita.getValue() / 100))));
        }
    }

    private ChangeListener eventoJSlider(ActionListener azione) {
        return e -> {
            if (!modoDiretto) {
                return;
            }
            if (crovescia == null) {
                crovescia = new Timer(200, azione);
            } else if (crovescia.isRunning()) {
                crovescia.stop();
                crovescia = new Timer(200, azione);
            }
            crovescia.setRepeats(false);
            crovescia.start();
        };
    }

    public void abilitaControlli(boolean abilita) {
        seguiWinAccent.setEnabled(abilita);
        hue.setEnabled(abilita);
        sat.setEnabled(abilita);
        temperatura.setEnabled(abilita);
        winAccent.setEnabled(abilita);
    }

    public void riscriviEtichette() {
        descLuce.setText(Strings.get(PannelloPrincipale.class, "16") + luminosita.getValue() + "%");
        descTemp.setText(Strings.get(PannelloPrincipale.class, "18") + temperatura.getValue() + "K");
        descCol.setText(Strings.get(PannelloPrincipale.class, "8"));
        accendi.setText(accendi.getText().contains("\uD83D\uDCA1") ? Strings.get(PannelloPrincipale.class, "0") : Strings.get(PannelloPrincipale.class, "12"));
        winAccent.setText(Strings.get(PannelloPrincipale.class, "4"));
        seguiWinAccent.setText(Strings.get(PannelloPrincipale.class, "9"));
        seguiSchermo.setText(Strings.get(PannelloPrincipale.class, "10"));
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
        setBounds(new Rectangle(0, 0, (int) dimensioniPannello.getWidth(), (int) dimensioniPannello.getHeight() + spostamento));
        ref.getFrame().revalidate();
    }

    public boolean getModoDiretto() {
        return modoDiretto;
    }

    public void setModoDiretto(boolean modoDiretto) {
        this.modoDiretto = modoDiretto;
    }

    public JButton getSeguiWinAccent() {
        return seguiWinAccent;
    }

    public JButton getWinAccent() {
        return winAccent;
    }

    public JSlider getHue() {
        return hue;
    }

    public JSlider getSat() {
        return sat;
    }

    public void setUltimaModalita(boolean ultimaModalita) {
        this.ultimaModalita = ultimaModalita;
    }

    public JButton getSeguiSchermo() {
        return seguiSchermo;
    }

    public JSlider getLuminosita() {
        return luminosita;
    }

    public JSlider getTemperatura() {
        return temperatura;
    }

    public JButton getAccendi() {
        return accendi;
    }

    public Timer getCrovescia() {
        return crovescia;
    }

}