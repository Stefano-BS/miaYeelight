package miaYeelight.ux.pannelli;

import miaYeelight.Main;
import miaYeelight.ux.componenti.Slider;
import miaYeelight.ux.schermo.Schermo;
import miaYeelight.lang.Strings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class PannelloAnimazioni extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private ArrayList<JTextField> tempi = new ArrayList<>();
    private ArrayList<JSlider[]> colori = new ArrayList<>();
    private ArrayList<JButton> aggiuntori = new ArrayList<>();
    private ArrayList<JButton> rimovitori = new ArrayList<>();
    private final JButton avviaAnimazione = new JButton(Strings.get("PannelloAnimazioni.6"));
    private final JButton torna = new JButton(Strings.get("PannelloAnimazioni.7"));
    private AnteprimaAnimazione anteprima;
    private int Y = Schermo.d(10);

    transient DocumentListener listenerCambioTempi = new DocumentListener() {
        public void changedUpdate(DocumentEvent a) {
            aggiorna(a);
        }

        public void insertUpdate(DocumentEvent a) {
            aggiorna(a);
        }

        public void removeUpdate(DocumentEvent a) {
            aggiorna(a);
        }

        private void aggiorna(DocumentEvent a) {
            try {
                int val = Integer.parseInt(a.getDocument().getText(0, a.getDocument().getLength()));
                if (val < 50) {
                    JOptionPane.showMessageDialog(ref.getFrame(), Strings.get("PannelloAnimazioni.8"), Strings.get("PannelloAnimazioni.9"), JOptionPane.WARNING_MESSAGE);
                }
                aggiornaAnteprima();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(ref.getFrame(), Strings.get("PannelloAnimazioni.10"), Strings.get("PannelloAnimazioni.9"), JOptionPane.WARNING_MESSAGE);
            } catch (BadLocationException e) {
                //
            }
        }
    };

    Main ref;

    public PannelloAnimazioni(Main ref) {
        super();
        this.ref = ref;
        setLayout(null);
        setBackground(Main.bg);
        JButton caricaAnimazione = new JButton(Strings.get("PannelloAnimazioni.4"));
        caricaAnimazione.setFocusable(false);
        JButton salvaAnimazione = new JButton(Strings.get("PannelloAnimazioni.5"));
        salvaAnimazione.setFocusable(false);
        caricaAnimazione.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        caricaAnimazione.addActionListener(this::caricaAnimazione);
        add(caricaAnimazione);
        salvaAnimazione.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        salvaAnimazione.addActionListener(click -> {
            FileDialog picker = new FileDialog((Frame) null);
            picker.setTitle(Strings.get("PannelloAnimazioni.16"));
            picker.setMode(FileDialog.SAVE);
            picker.setFile(Strings.get("PannelloAnimazioni.13"));
            picker.setVisible(true);
            try {
                DataOutputStream f = new DataOutputStream(new FileOutputStream(picker.getDirectory() + picker.getFile()));
                for (int i = 0; i < tempi.size(); i++) {
                    f.writeInt(Integer.parseInt(tempi.get(i).getText()));
                    f.writeInt(colori.get(i)[0].getValue());
                    f.writeInt(colori.get(i)[1].getValue());
                    f.writeInt(colori.get(i)[2].getValue());
                }
                f.flush();
                f.close();
            } catch (IOException e) {
                //
            }
        });
        add(salvaAnimazione);
        Y += Schermo.d(50);
        JLabel[] intestazione = {new JLabel(Strings.get("PannelloAnimazioni.0")), new JLabel(Strings.get("PannelloAnimazioni.1")), new JLabel(Strings.get("PannelloAnimazioni.2")), new JLabel(Strings.get("PannelloAnimazioni.3"))};
        intestazione[0].setBounds(Schermo.d(10), Y, Schermo.d(80), Schermo.d(40));
        intestazione[0].setHorizontalAlignment(SwingConstants.CENTER);
        intestazione[1].setBounds(Schermo.d(80), Y, Schermo.d(80), Schermo.d(40));
        intestazione[1].setHorizontalAlignment(SwingConstants.CENTER);
        intestazione[2].setBounds(Schermo.d(210), Y, Schermo.d(80), Schermo.d(40));
        intestazione[2].setHorizontalAlignment(SwingConstants.CENTER);
        intestazione[3].setBounds(Schermo.d(320), Y, Schermo.d(80), Schermo.d(40));
        intestazione[3].setHorizontalAlignment(SwingConstants.CENTER);
        add(intestazione[0]);
        add(intestazione[1]);
        add(intestazione[2]);
        add(intestazione[3]);
        Y += Schermo.d(50);
        tempi.add(new JTextField());
        tempi.get(0).setText("1000");
        colori.add(new JSlider[]{Slider.fab(Slider.PRESETLUM), Slider.fab(Slider.PRESETTON), Slider.fab(Slider.PRESETSAT)});
        aggiuntori.add(new JButton("➕"));
        rimovitori.add(new JButton("➖"));
        add(tempi.get(0));
        add(colori.get(0)[0]);
        add(colori.get(0)[1]);
        add(colori.get(0)[2]);
        add(aggiuntori.get(0));
        add(rimovitori.get(0));
        tempi.get(0).getDocument().addDocumentListener(listenerCambioTempi);
        colori.get(0)[0].addChangeListener(sl -> aggiornaAnteprima());
        colori.get(0)[1].addChangeListener(sl -> aggiornaAnteprima());
        colori.get(0)[2].addChangeListener(sl -> aggiornaAnteprima());
        disegna();
        anteprima = new AnteprimaAnimazione(ottieniValori());
        anteprima.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        add(anteprima);
        Y += Schermo.d(50);
        avviaAnimazione.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        avviaAnimazione.addActionListener(click -> {
            int[][] valori = new int[tempi.size()][4];
            for (int i = 0; i < valori.length; i++) {
                valori[i][0] = Integer.parseInt(tempi.get(i).getText());
                //Spegni la lampada se la luminosità scelta è zero
                valori[i][1] = 1;
                Color coloreScelto = new Color(Color.HSBtoRGB(((float) colori.get(i)[1].getValue()) / 359, ((float) colori.get(i)[2].getValue()) / 100, ((float) colori.get(i)[0].getValue()) / 100));
                valori[i][2] = coloreScelto.getRed() * 65536 + coloreScelto.getGreen() * 256 + coloreScelto.getBlue();
                valori[i][3] = colori.get(i)[0].getValue();
            }
            ref.getConnessione().animazione(valori);
        });
        torna.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        torna.setFocusable(false);
        avviaAnimazione.setFocusable(false);
        torna.addActionListener(click -> ref.tornaStatico());
        Y += Schermo.d(50);
        add(avviaAnimazione);
        add(torna);
        setSize(new Dimension(Schermo.d(530), Y));
        setPreferredSize(new Dimension(Schermo.d(530), Y));
        setVisible(true);
    }

    private void caricaAnimazione(final ActionEvent actionEvent) {
        FileDialog picker = new FileDialog((Frame) null);
        picker.setTitle(Strings.get("PannelloAnimazioni.12"));
        picker.setMode(FileDialog.LOAD);
        picker.setVisible(true);
        try {
            final DataInputStream f = new DataInputStream(new FileInputStream(picker.getDirectory() + picker.getFile()));
            creaUiDaAnimazioneImportata(f);
            f.close();
            ridisegna();
            aggiornaAnteprima();
        } catch (IOException ignored) {
            //
        }
    }

    private void creaUiDaAnimazioneImportata(final DataInputStream f) {
        try {
            tempi.forEach(this::remove);
            aggiuntori.forEach(this::remove);
            rimovitori.forEach(this::remove);

            for (JSlider[] jl : colori) {
                remove(jl[0]);
                remove(jl[1]);
                remove(jl[2]);
            }

            tempi = new ArrayList<>();
            colori = new ArrayList<>();
            aggiuntori = new ArrayList<>();
            rimovitori = new ArrayList<>();
            int i = 0;
            while (f.available() >= 16) {
                tempi.add(new JTextField());
                tempi.get(i).setText("" + f.readInt());
                colori.add(new JSlider[]{Slider.fab(Slider.PRESETLUM), Slider.fab(Slider.PRESETTON), Slider.fab(Slider.PRESETSAT)});
                colori.get(i)[0].setMinimum(0);
                colori.get(i)[0].setMaximum(100);
                colori.get(i)[1].setMinimum(0);
                colori.get(i)[1].setMaximum(359);
                colori.get(i)[2].setMinimum(0);
                colori.get(i)[2].setMaximum(100);
                colori.get(i)[0].setValue(f.readInt());
                colori.get(i)[1].setValue(f.readInt());
                colori.get(i)[2].setValue(f.readInt());
                tempi.get(i).getDocument().addDocumentListener(listenerCambioTempi);
                colori.get(i)[0].setOpaque(false);
                colori.get(i)[1].setOpaque(false);
                colori.get(i)[1].setOpaque(false);
                colori.get(i)[0].addChangeListener(sl -> aggiornaAnteprima());
                colori.get(i)[1].addChangeListener(sl -> aggiornaAnteprima());
                colori.get(i)[2].addChangeListener(sl -> aggiornaAnteprima());
                aggiuntori.add(new JButton("➕"));
                rimovitori.add(new JButton("➖"));
                add(tempi.get(i));
                add(colori.get(i)[0]);
                add(colori.get(i)[1]);
                add(colori.get(i)[2]);
                add(aggiuntori.get(i));
                add(rimovitori.get(i));
                i++;
            }
        } catch (IOException ignored) {
            //
        }
    }

    private void disegna() {
        for (int i = 0; i < tempi.size(); i++) {
            tempi.get(i).setBounds(Schermo.d(10), Y, Schermo.d(80), Schermo.d(40));
            tempi.get(i).setHorizontalAlignment(SwingConstants.CENTER);
            colori.get(i)[0].setBounds(Schermo.d(100), Y, Schermo.d(100), Schermo.d(40));
            colori.get(i)[0].setMinimum(0);
            colori.get(i)[0].setMaximum(100);
            colori.get(i)[1].setBounds(Schermo.d(210), Y, Schermo.d(100), Schermo.d(40));
            colori.get(i)[1].setMinimum(0);
            colori.get(i)[1].setMaximum(359);
            colori.get(i)[2].setBounds(Schermo.d(320), Y, Schermo.d(100), Schermo.d(40));
            colori.get(i)[2].setMinimum(0);
            colori.get(i)[2].setMaximum(100);
            colori.get(i)[0].setBackground(Main.trasparente);
            colori.get(i)[1].setBackground(Main.trasparente);
            colori.get(i)[2].setBackground(Main.trasparente);
            aggiuntori.get(i).setBounds(Schermo.d(430), Y, Schermo.d(40), Schermo.d(40));
            rimovitori.get(i).setBounds(Schermo.d(480), Y, Schermo.d(40), Schermo.d(40));
            //Rimuovi il listener associato precedentemente perché potrebbe contenere un indice ora errato
            if (aggiuntori.get(i).getActionListeners().length != 0) {
                aggiuntori.get(i).removeActionListener(aggiuntori.get(i).getActionListeners()[0]);
            }
            aggiuntori.get(i).addActionListener(new LambdaAnimazione(true, i));

            if (rimovitori.get(i).getActionListeners().length != 0) {
                rimovitori.get(i).removeActionListener(rimovitori.get(i).getActionListeners()[0]);
            }
            rimovitori.get(i).addActionListener(new LambdaAnimazione(false, i));

            aggiuntori.get(i).setFocusable(false);
            rimovitori.get(i).setFocusable(false);
            Y += Schermo.d(50);
        }
    }

    void ridisegna() {
        Y = Schermo.d(100);
        disegna();
        if (anteprima != null) {
            anteprima.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
            Y += Schermo.d(50);
        }
        avviaAnimazione.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        torna.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        Y += Schermo.d(50);
        setSize(new Dimension(Schermo.d(530), Y));
        setPreferredSize(new Dimension(Schermo.d(530), Y));
        ref.getFrame().setSize(Schermo.d(530), Schermo.d(40) + Y);
        ref.getFrame().revalidate();
    }

    void aggiornaAnteprima() {
        if (anteprima == null) {
            return;
        }
        int vecchiaY = anteprima.getY();
        remove(anteprima);
        anteprima = new AnteprimaAnimazione(ottieniValori());
        anteprima.setBounds(Schermo.d(10), vecchiaY, Schermo.d(510), Schermo.d(40));
        add(anteprima);
        repaint();
    }

    int[][] ottieniValori() {
        int[][] valori = new int[tempi.size()][4];
        for (int i = 0; i < tempi.size(); i++) {
            valori[i][0] = Integer.parseInt(tempi.get(i).getText());
            valori[i][1] = colori.get(i)[0].getValue();
            valori[i][2] = colori.get(i)[1].getValue();
            valori[i][3] = colori.get(i)[2].getValue();
        }
        return valori;
    }

    private class LambdaAnimazione implements ActionListener {
        boolean tipo;
        int riga;

        LambdaAnimazione(boolean tipologia, int riga) {
            tipo = tipologia;
            this.riga = riga;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (tipo) {
                tempi.add(riga, new JTextField());
                tempi.get(riga).setText("1000");
                colori.add(riga, new JSlider[]{Slider.fab(Slider.PRESETLUM), Slider.fab(Slider.PRESETTON), Slider.fab(Slider.PRESETSAT)});
                aggiuntori.add(riga, new JButton("➕"));
                rimovitori.add(riga, new JButton("➖"));
                add(tempi.get(riga));
                add(colori.get(riga)[0]);
                add(colori.get(riga)[1]);
                add(colori.get(riga)[2]);
                add(aggiuntori.get(riga));
                add(rimovitori.get(riga));
                tempi.get(riga).getDocument().addDocumentListener(listenerCambioTempi);
                colori.get(riga)[0].setOpaque(false);
                colori.get(riga)[1].setOpaque(false);
                colori.get(riga)[1].setOpaque(false);
                colori.get(riga)[0].addChangeListener(sl -> aggiornaAnteprima());
                colori.get(riga)[1].addChangeListener(sl -> aggiornaAnteprima());
                colori.get(riga)[2].addChangeListener(sl -> aggiornaAnteprima());
            } else {
                if (tempi.size() == 1) {
                    return;
                }
                remove(tempi.get(riga));
                remove(colori.get(riga)[0]);
                remove(colori.get(riga)[1]);
                remove(colori.get(riga)[2]);
                remove(aggiuntori.get(riga));
                remove(rimovitori.get(riga));
                tempi.remove(riga);
                colori.remove(riga);
                aggiuntori.remove(riga);
                rimovitori.remove(riga);
            }

            ridisegna();
            aggiornaAnteprima();
        }
    }

    private static class AnteprimaAnimazione extends JPanel {

        @Serial
        private static final long serialVersionUID = 1L;

        private final int[][] v;

        AnteprimaAnimazione(int[][] valori) {
            v = valori;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (v == null || v.length == 0) {
                return;
            }

            int tempo = 0;
            int X = 0;
            int xPost = 0;

            for (int[] i : v) {
                tempo += i[0];
            }

            for (int s = 0; s < v.length; s++) {
                xPost += (int) ((float) (v[s][0]) / tempo * Schermo.d(510));
                float hue1 = v[s][2];
                float sat1 = v[s][3];
                float br1 = v[s][1];

                float hue2;
                float sat2;
                float br2;
                if (s == v.length - 1) {
                    hue2 = v[0][2];
                    sat2 = v[0][3];
                    br2 = v[0][1];
                } else {
                    hue2 = v[s + 1][2];
                    sat2 = v[s + 1][3];
                    br2 = v[s + 1][1];
                }

                for (int x = X; x <= xPost; x++) {
                    float p = (float) (x - X) / (xPost - X); //percentuale gradiente attuale
                    g.setColor(Color.getHSBColor((hue2 > hue1 ? (hue2 - hue1 < 180 ? (hue1 * (1 - p) + hue2 * p) : (hue1 - (hue1 + 360 - hue2) * p > 0 ? hue1 - (hue1 + 360 - hue2) * p : 360 + hue1 - (hue1 + 360 - hue2) * p)) : (hue1 - hue2 < 180 ? (hue1 * (1 - p) + hue2 * p) : (hue1 + (hue2 + 360 - hue1) * p > 359 ? hue1 + (hue2 + 360 - hue1) * p - 360 : hue1 + (hue2 + 360 - hue1) * p))) / 360, (sat1 * (1 - p) + sat2 * p) / 100, (br1 * (1 - p) + br2 * p) / 100));

                    g.drawLine(x, 0, x, Schermo.d(40));
                }
                X = xPost;
            }
        }
    }

}