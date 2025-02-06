package miaYeelight.ux.pannelli;

import miaYeelight.Main;
import miaYeelight.ux.componenti.Slider;
import miaYeelight.ux.schermo.Schermo;
import miaYeelight.lang.Strings;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.Serial;

public class PannelloPrincipale extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JButton seguiWinAccent;
    private final JButton seguiSchermo;
    private final JButton accendi = new JButton(Strings.get("PannelloPrincipale.0"));
    private final JButton winAccent = new JButton(Strings.get("PannelloPrincipale.4"));
    private final JSlider luminosita = Slider.fab(Slider.PRESETLUM);
    private final JSlider hue = Slider.fab(Slider.PRESETTON);
    private final JSlider sat = Slider.fab(Slider.PRESETSAT);
    private final JSlider temperatura = Slider.fab(Slider.PRESETCT);
    private final JPanel anteprima = new JPanel();
    private Timer crovescia = null;
    private boolean modoDiretto = true;
    private boolean ultimaModalita = false; // false: T, true: C

    public PannelloPrincipale(Main ref) {
        setLayout(null);
        setBackground(Main.bg);
        JLabel descLuce = new JLabel(Strings.get("PannelloPrincipale.16"));
        JLabel descTemp = new JLabel(Strings.get("PannelloPrincipale.18"));
        JLabel descCol = new JLabel(Strings.get("PannelloPrincipale.8"));
        seguiWinAccent = new JButton(Strings.get("PannelloPrincipale.9"));
        seguiSchermo = new JButton(Strings.get("PannelloPrincipale.10"));

        int Y = Schermo.d(10);
        accendi.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        final JButton timer = new JButton(Strings.get("PannelloPrincipale.1"));
        timer.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        accendi.addActionListener(click -> {
            if (accendi.getText().equals(Strings.get("PannelloPrincipale.0"))) {
                accendi.setText(Strings.get("PannelloPrincipale.12"));
                ref.getConnessione().accendi();
            } else {
                accendi.setText(Strings.get("PannelloPrincipale.0"));
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
                int tempo = (Integer) JOptionPane.showInputDialog(ref.getFrame(), Strings.get("PannelloPrincipale.14"), Strings.get("PannelloPrincipale.15"), JOptionPane.QUESTION_MESSAGE, ref.yee, listaTimer, 1);
                ref.getConnessione().timer(tempo);
            } catch (Exception ignored) {
                //
            }
        });

        accendi.setFocusable(false);
        timer.setFocusable(false);
        add(accendi);
        add(timer);
        Y += Schermo.d(50);
        seguiSchermo.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        seguiSchermo.addActionListener(click -> ref.seguiColoreSchermo());
        seguiSchermo.setFocusable(false);
        add(seguiSchermo);
        Y += Schermo.d(50);
        winAccent.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        seguiWinAccent.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        winAccent.setFocusable(false);
        seguiWinAccent.setFocusable(false);
        winAccent.addActionListener(click -> ref.cambiaColoreDaAccent());
        seguiWinAccent.addActionListener(click -> ref.tienitiAggiornataSuWindows());
        add(winAccent);
        add(seguiWinAccent);
        Y += Schermo.d(50);
        final JButton animazioni = new JButton(Strings.get("PannelloPrincipale.5"));
        animazioni.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> ref.apriPannelloAnimazioni());
        add(animazioni);
        Y += Schermo.d(70);
        descLuce.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        descLuce.setOpaque(false);
        add(descLuce);
        Y += Schermo.d(40);
        luminosita.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        luminosita.setBackground(Main.trasparente);
        luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100);
        luminosita.setMinimum(0);
        luminosita.setMajorTickSpacing(10);
        luminosita.setMinorTickSpacing(5);
        luminosita.setPaintTicks(true);
        luminosita.addChangeListener(s -> descLuce.setText(Strings.get("PannelloPrincipale.16") + luminosita.getValue() + "%"));
        luminosita.addChangeListener(s -> aggiornaAnteprima());
        luminosita.addChangeListener(s -> aggiornaAnteprima());
        luminosita.addChangeListener(eventoJSlider(e -> ref.getConnessione().setBr(Math.max(luminosita.getValue(), 1))));
        luminosita.setOpaque(false);
        add(luminosita);
        Y += Schermo.d(40);
        descTemp.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        add(descTemp);
        Y += Schermo.d(40);
        temperatura.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        temperatura.setBackground(Main.trasparente);
        temperatura.setForeground(Color.WHITE);
        temperatura.setOpaque(false);
        temperatura.setMaximum(6500);
        temperatura.setMinimum(1700);
        temperatura.setValue(5000);
        temperatura.setSnapToTicks(true);
        temperatura.setMinorTickSpacing(100);
        temperatura.setPaintTicks(true);
        temperatura.addChangeListener(s -> descTemp.setText(Strings.get("PannelloPrincipale.18") + temperatura.getValue() + "K"));
        temperatura.addChangeListener(s -> ultimaModalita = false);
        temperatura.addChangeListener(s -> aggiornaAnteprima());
        temperatura.addChangeListener(eventoJSlider(e -> ref.getConnessione().temperatura(temperatura.getValue())));
        add(temperatura);
        Y += Schermo.d(40);
        descCol.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        add(descCol);
        Y += Schermo.d(40);
        hue.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        sat.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
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
        Y += Schermo.d(50);
        anteprima.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(20));
        anteprima.setFocusable(false);
        add(anteprima);
        Y += Schermo.d(30);
        setSize(Schermo.d(530), Y);
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