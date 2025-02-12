package miayeelight.ux.pannelli;

import miayeelight.Configurazione;
import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.net.Connessione;
import miayeelight.ux.componenti.Slider;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.Serial;

import static miayeelight.Main.log;
import static miayeelight.ux.schermo.Schermo.coloreDaTemperatura;
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
    private final JSlider lum = Slider.fab(Slider.PRESETLUM);
    private final JSlider hue = Slider.fab(Slider.PRESETTON);
    private final JSlider sat = Slider.fab(Slider.PRESETSAT);
    private final JSlider temperatura = Slider.fab(Slider.PRESETCT);
    private final JPanel anteprima = new JPanel();
    private final JLabel descLuce;
    private final JLabel descTemp;
    private final JLabel descCol;
    private final Main ref;
    private Timer cRovescia = null;
    private boolean modoDiretto = true;
    private boolean ultimoModo = false; // false: T, true: C
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

        int y = d(10);
        accendi.setBounds(d(10), y, d(225), d(40));

        final JButton impostazioni = new JButton("🔨");
        impostazioni.setBounds(d(245), y, d(40), d(40));
        impostazioni.addActionListener(click -> ref.apriPannelloImpostazioni());

        timer.setBounds(d(295), y, d(225), d(40));

        accendi.addActionListener(click -> {
            if (accendi.getText().equals(Strings.get(PannelloPrincipale.class, "0"))) {
                accendi.setText(Strings.get(PannelloPrincipale.class, "12"));
                Connessione.istanza().accendi();
            } else {
                accendi.setText(Strings.get(PannelloPrincipale.class, "0"));
                Connessione.istanza().spegni();
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
                Object scelta = JOptionPane.showInputDialog(ref.getFrame(), Strings.get(PannelloPrincipale.class, "14"), Strings.get(PannelloPrincipale.class, "15"), JOptionPane.QUESTION_MESSAGE, ref.yee, listaTimer, 1);
                if (scelta instanceof Integer tempo) {
                    Connessione.istanza().timer(tempo);
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
        winAccent.addActionListener(click -> ref.cambiaColoreDaAccent());
        seguiWinAccent.addActionListener(click -> ref.tienitiAggiornataSuWindows());
        add(winAccent);
        add(seguiWinAccent);

        if (win10AccentDisegnati) {
            y += d(50);
        } else {
            winAccent.setVisible(false);
            seguiWinAccent.setVisible(false);
        }

        seguiSchermo.setBounds(d(10), y, d(250), d(40));
        seguiSchermo.addActionListener(click -> ref.seguiColoreSchermo());
        seguiSchermo.setFocusable(false);
        add(seguiSchermo);

        animazioni.setBounds(d(270), y, d(250), d(40));
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> ref.apriPannelloAnimazioni());
        add(animazioni);
        y += d(70);

        descLuce.setBounds(d(10), y, d(510), d(40));
        descLuce.setOpaque(false);
        add(descLuce);
        y += d(40);
        lum.setBounds(d(10), y, d(510), d(40));
        lum.setBackground(Main.trasparente);
        lum.setForeground(Color.WHITE);
        lum.setMaximum(100);
        lum.setMinimum(0);
        lum.setMajorTickSpacing(10);
        lum.setMinorTickSpacing(5);
        lum.setPaintTicks(true);
        lum.addChangeListener(s -> descLuce.setText(Strings.get(PannelloPrincipale.class, "16") + lum.getValue() + "%"));
        lum.addChangeListener(s -> aggiornaAnteprima());
        lum.addChangeListener(s -> aggiornaAnteprima());
        lum.addChangeListener(eventoJSlider(e -> Connessione.istanza().setBr(Math.max(lum.getValue(), 1))));
        lum.setOpaque(false);
        add(lum);
        y += d(40);
        descTemp.setBounds(d(10), y, d(510), d(40));
        add(descTemp);
        y += d(40);
        temperatura.setBounds(d(10), y, d(510), d(40));
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
        temperatura.addChangeListener(s -> ultimoModo = false);
        temperatura.addChangeListener(s -> aggiornaAnteprima());
        temperatura.addChangeListener(eventoJSlider(e -> Connessione.istanza().temperatura(temperatura.getValue())));
        add(temperatura);
        y += d(40);
        descCol.setBounds(d(10), y, d(510), d(40));
        add(descCol);
        y += d(40);
        hue.setBounds(d(10), y, d(250), d(40));
        sat.setBounds(d(270), y, d(250), d(40));
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
        hue.addChangeListener(s -> ultimoModo = true);
        hue.addChangeListener(s -> aggiornaAnteprima());
        sat.addChangeListener(s -> ultimoModo = true);
        sat.addChangeListener(s -> aggiornaAnteprima());
        hue.addChangeListener(eventoJSlider(e -> Connessione.istanza().setHS(hue.getValue(), sat.getValue())));
        sat.addChangeListener(eventoJSlider(e -> Connessione.istanza().setHS(hue.getValue(), sat.getValue())));
        y += d(50);
        anteprima.setBounds(d(10), y, d(510), d(20));
        anteprima.setFocusable(false);
        aggiornaAnteprima();
        add(anteprima);
        y += d(30);
        setSize(d(530), y);
        setVisible(true);
    }

    public void aggiornaAnteprima() {
        if (ultimoModo) {
            anteprima.setBackground(new Color(Color.HSBtoRGB(((float) hue.getValue()) / 359, ((float) sat.getValue()) / 100, ((float) (lum.getValue() > 80 ? 100 : lum.getValue() + 20)) / 100)));
        } else {
            anteprima.setBackground(coloreDaTemperatura((double) (temperatura.getValue() - 1700) / 48, ((double) lum.getValue()) / 100));
        }
    }

    private ChangeListener eventoJSlider(ActionListener azione) {
        return e -> {
            if (!modoDiretto) {
                return;
            }
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

    public void abilitaControlli(boolean abilita) {
        seguiWinAccent.setEnabled(abilita);
        hue.setEnabled(abilita);
        sat.setEnabled(abilita);
        temperatura.setEnabled(abilita);
        winAccent.setEnabled(abilita);
    }

    public void riscriviEtichette() {
        descLuce.setText(Strings.get(PannelloPrincipale.class, "16") + lum.getValue() + "%");
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

    public JSlider getHue() {
        return hue;
    }

    public JSlider getSat() {
        return sat;
    }

    public void setUltimoModo(boolean ultimoModo) {
        this.ultimoModo = ultimoModo;
    }

    public JButton getSeguiSchermo() {
        return seguiSchermo;
    }

    public JSlider getLum() {
        return lum;
    }

    public JSlider getTemperatura() {
        return temperatura;
    }

    public JButton getAccendi() {
        return accendi;
    }

    public Timer getCRovescia() {
        return cRovescia;
    }

}