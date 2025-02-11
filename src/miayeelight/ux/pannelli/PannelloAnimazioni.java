package miayeelight.ux.pannelli;

import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.ux.componenti.Slider;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static miayeelight.Main.log;
import static miayeelight.ux.schermo.Schermo.d;

public class PannelloAnimazioni extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final FiltroEspressioneRegolare FILTRO_TEMPI = new FiltroEspressioneRegolare("^[0-9]*$");

    private ArrayList<JTextField> tempi = new ArrayList<>();
    private ArrayList<JSlider[]> colori = new ArrayList<>();
    private ArrayList<JButton> aggiuntori = new ArrayList<>();
    private ArrayList<JButton> rimovitori = new ArrayList<>();
    private final JButton avviaAnimazione = new JButton(Strings.get(PannelloAnimazioni.class, "6"));
    private final JButton torna = new JButton(Strings.get(PannelloAnimazioni.class, "7"));
    private final JButton caricaAnimazione = new JButton(Strings.get(PannelloAnimazioni.class, "4"));
    private final JButton salvaAnimazione = new JButton(Strings.get(PannelloAnimazioni.class, "5"));
    private final JLabel[] intestazione = {new JLabel(Strings.get(PannelloAnimazioni.class, "0")), new JLabel(Strings.get(PannelloAnimazioni.class, "1")), new JLabel(Strings.get(PannelloAnimazioni.class, "2")), new JLabel(Strings.get(PannelloAnimazioni.class, "3"))};
    private AnteprimaAnimazione anteprima;
    private int vPos = d(10);
    private final Main ref;

    private final transient DocumentListener listenerCambioTempi = new DocumentListener() {
        public void changedUpdate(DocumentEvent a) {
            aggiornaAnteprima();
        }

        public void insertUpdate(DocumentEvent a) {
            aggiornaAnteprima();
        }

        public void removeUpdate(DocumentEvent a) {
            aggiornaAnteprima();
        }
    };

    public PannelloAnimazioni(Main ref) {
        super();
        this.ref = ref;
        setLayout(null);
        setBackground(Main.bg);
        caricaAnimazione.setFocusable(false);
        salvaAnimazione.setFocusable(false);
        caricaAnimazione.setBounds(d(10), vPos, d(250), d(40));
        caricaAnimazione.addActionListener(this::caricaAnimazione);
        add(caricaAnimazione);
        salvaAnimazione.setBounds(d(270), vPos, d(250), d(40));
        salvaAnimazione.addActionListener(click -> salvaAnimazione());
        add(salvaAnimazione);
        vPos += d(50);
        intestazione[0].setBounds(d(10), vPos, d(80), d(40));
        intestazione[0].setHorizontalAlignment(SwingConstants.CENTER);
        intestazione[1].setBounds(d(80), vPos, d(80), d(40));
        intestazione[1].setHorizontalAlignment(SwingConstants.CENTER);
        intestazione[2].setBounds(d(210), vPos, d(80), d(40));
        intestazione[2].setHorizontalAlignment(SwingConstants.CENTER);
        intestazione[3].setBounds(d(320), vPos, d(80), d(40));
        intestazione[3].setHorizontalAlignment(SwingConstants.CENTER);
        add(intestazione[0]);
        add(intestazione[1]);
        add(intestazione[2]);
        add(intestazione[3]);
        vPos += d(50);
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
        ((AbstractDocument) tempi.get(0).getDocument()).setDocumentFilter(FILTRO_TEMPI);
        tempi.get(0).getDocument().addDocumentListener(listenerCambioTempi);
        colori.get(0)[0].addChangeListener(sl -> aggiornaAnteprima());
        colori.get(0)[1].addChangeListener(sl -> aggiornaAnteprima());
        colori.get(0)[2].addChangeListener(sl -> aggiornaAnteprima());
        disegna();
        anteprima = new AnteprimaAnimazione(ottieniValori());
        anteprima.setBounds(d(10), vPos, d(510), d(40));
        add(anteprima);
        vPos += d(50);
        avviaAnimazione.setBounds(d(10), vPos, d(250), d(40));
        avviaAnimazione.addActionListener(click -> {
            if (tempi.stream().anyMatch(t -> t.getText().isEmpty() || Integer.parseInt(t.getText()) < 50)) {
                JOptionPane.showMessageDialog(ref.getFrame(), Strings.get(PannelloAnimazioni.class, "8"), Strings.get(PannelloAnimazioni.class, "9"), JOptionPane.WARNING_MESSAGE);
                return;
            }

            int[][] valori = new int[tempi.size()][4];
            for (int i = 0; i < valori.length; i++) {
                final Color coloreScelto = new Color(Color.HSBtoRGB(((float) colori.get(i)[1].getValue()) / 359, ((float) colori.get(i)[2].getValue()) / 100, ((float) colori.get(i)[0].getValue()) / 100));

                valori[i][0] = Integer.parseInt(tempi.get(i).getText());
                valori[i][1] = 1;
                valori[i][2] = coloreScelto.getRed() * 65536 + coloreScelto.getGreen() * 256 + coloreScelto.getBlue();
                valori[i][3] = colori.get(i)[0].getValue();
            }
            ref.getConnessione().animazione(valori);
        });
        torna.setBounds(d(270), vPos, d(250), d(40));
        torna.setFocusable(false);
        avviaAnimazione.setFocusable(false);
        torna.addActionListener(click -> ref.tornaStatico());
        vPos += d(50);
        add(avviaAnimazione);
        add(torna);
        setSize(new Dimension(d(530), vPos));
        setPreferredSize(new Dimension(d(530), vPos));
        setVisible(true);
    }

    public void riscriviEtichette() {
        avviaAnimazione.setText(Strings.get(PannelloAnimazioni.class, "6"));
        torna.setText(Strings.get(PannelloAnimazioni.class, "7"));
        caricaAnimazione.setText(Strings.get(PannelloAnimazioni.class, "4"));
        salvaAnimazione.setText(Strings.get(PannelloAnimazioni.class, "5"));
        intestazione[0].setText(Strings.get(PannelloAnimazioni.class, "0"));
        intestazione[1].setText(Strings.get(PannelloAnimazioni.class, "1"));
        intestazione[2].setText(Strings.get(PannelloAnimazioni.class, "2"));
        intestazione[3].setText(Strings.get(PannelloAnimazioni.class, "3"));
    }

    private void salvaAnimazione() {
        final FileDialog selettoreFile = new FileDialog((Frame) null);
        selettoreFile.setTitle(Strings.get(PannelloAnimazioni.class, "16"));
        selettoreFile.setMode(FileDialog.SAVE);
        selettoreFile.setFile(Strings.get(PannelloAnimazioni.class, "13"));
        selettoreFile.setVisible(true);

        try (final DataOutputStream f = new DataOutputStream(new FileOutputStream(selettoreFile.getDirectory() + selettoreFile.getFile()))) {
            for (int i = 0; i < tempi.size(); i++) {
                f.writeInt(Integer.parseInt(tempi.get(i).getText()));
                f.writeInt(colori.get(i)[0].getValue());
                f.writeInt(colori.get(i)[1].getValue());
                f.writeInt(colori.get(i)[2].getValue());
            }
            f.flush();
        } catch (IOException e) {
            log(e);
        }
    }

    private void caricaAnimazione(final ActionEvent ignored) {
        FileDialog picker = new FileDialog((Frame) null);
        picker.setTitle(Strings.get(PannelloAnimazioni.class, "12"));
        picker.setMode(FileDialog.LOAD);
        picker.setVisible(true);

        try (final DataInputStream f = new DataInputStream(new FileInputStream(picker.getDirectory() + picker.getFile()))) {
            creaUiDaAnimazioneImportata(f);
            ridisegna();
            aggiornaAnteprima();
        } catch (IOException e) {
            log(e);
        }
    }

    private void creaUiDaAnimazioneImportata(final DataInputStream f) throws IOException {
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
            ((AbstractDocument) tempi.get(i).getDocument()).setDocumentFilter(FILTRO_TEMPI);
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

    }

    private void disegna() {
        for (int i = 0; i < tempi.size(); i++) {
            tempi.get(i).setBounds(d(10), vPos, d(80), d(40));
            tempi.get(i).setHorizontalAlignment(SwingConstants.CENTER);
            colori.get(i)[0].setBounds(d(100), vPos, d(100), d(40));
            colori.get(i)[0].setMinimum(0);
            colori.get(i)[0].setMaximum(100);
            colori.get(i)[1].setBounds(d(210), vPos, d(100), d(40));
            colori.get(i)[1].setMinimum(0);
            colori.get(i)[1].setMaximum(359);
            colori.get(i)[2].setBounds(d(320), vPos, d(100), d(40));
            colori.get(i)[2].setMinimum(0);
            colori.get(i)[2].setMaximum(100);
            colori.get(i)[0].setBackground(Main.trasparente);
            colori.get(i)[1].setBackground(Main.trasparente);
            colori.get(i)[2].setBackground(Main.trasparente);
            aggiuntori.get(i).setBounds(d(430), vPos, d(40), d(40));
            rimovitori.get(i).setBounds(d(480), vPos, d(40), d(40));
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
            vPos += d(50);
        }
    }

    private void ridisegna() {
        vPos = d(100);
        disegna();
        if (anteprima != null) {
            anteprima.setBounds(d(10), vPos, d(510), d(40));
            vPos += d(50);
        }
        avviaAnimazione.setBounds(d(10), vPos, d(250), d(40));
        torna.setBounds(d(270), vPos, d(250), d(40));
        vPos += d(50);
        setSize(new Dimension(d(530), vPos));
        setPreferredSize(new Dimension(d(530), vPos));
        ref.getFrame().setSize(d(530), d(40) + vPos);
        ref.getFrame().revalidate();
    }

    private void aggiornaAnteprima() {
        if (anteprima == null) {
            return;
        }
        int vecchiaY = anteprima.getY();
        remove(anteprima);
        anteprima = new AnteprimaAnimazione(ottieniValori());
        anteprima.setBounds(d(10), vecchiaY, d(510), d(40));
        add(anteprima);
        repaint();
    }

    private int[][] ottieniValori() {
        int[][] valori = new int[tempi.size()][4];
        for (int i = 0; i < tempi.size(); i++) {
            final String tempoInserito = tempi.get(i).getText();
            valori[i][0] = tempoInserito.isEmpty() ? 1 : Integer.parseInt(tempoInserito);
            valori[i][1] = colori.get(i)[0].getValue();
            valori[i][2] = colori.get(i)[1].getValue();
            valori[i][3] = colori.get(i)[2].getValue();
        }
        return valori;
    }

    private class LambdaAnimazione implements ActionListener {
        final boolean tipo;
        final int riga;

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
                ((AbstractDocument) tempi.get(riga).getDocument()).setDocumentFilter(FILTRO_TEMPI);
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

            int tempoTotale = Arrays.stream(v).mapToInt(t -> t[0]).sum();
            float orizPos = 0;

            for (int s = 0; s < v.length; s++) {
                final float xPost = Math.round(orizPos + ((float) v[s][0] / tempoTotale * d(510)));

                final int coloreIniziale = Color.getHSBColor((float) v[s][2] / 360, (float) v[s][3] / 100, (float) v[s][1] / 100).getRGB();
                final int[] finale = v[(s + 1) % v.length];
                final int coloreFinale = Color.getHSBColor((float) finale[2] / 360, (float) finale[3] / 100, (float) finale[1] / 100).getRGB();

                final int rossoIniziale = coloreIniziale >> 16 & 0xFF;
                final int verdeIniziale = coloreIniziale >> 8 & 0xFF;
                final int bluIniziale = coloreIniziale & 0xFF;
                final int rossoFinale = coloreFinale >> 16 & 0xFF;
                final int verdeFinale = coloreFinale >> 8 & 0xFF;
                final int bluFinale = coloreFinale & 0xFF;

                for (int x = (int) orizPos; x < (int) xPost; x++) {
                    float p = (x - orizPos) / (xPost - orizPos);
                    final int rosso = (int) (rossoIniziale * (1 - p) + rossoFinale * p);
                    final int verde = (int) (verdeIniziale * (1 - p) + verdeFinale * p);
                    final int blu = (int) (bluIniziale * (1 - p) + bluFinale * p);

                    g.setColor(new Color(rosso, verde, blu));
                    g.fillRect(x, 0, 1, d(40));
                }

                orizPos = xPost;
            }
        }

    }

    private static class FiltroEspressioneRegolare extends DocumentFilter {

        private final Pattern pattern;

        public FiltroEspressioneRegolare(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public void insertString(final FilterBypass fb, int offset, final String string, final AttributeSet attr) throws BadLocationException {
            final StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, string);

            if (pattern.matcher(sb.toString()).matches()) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(final FilterBypass fb, int offset, int length, final String text, final AttributeSet attrs) throws BadLocationException {
            final StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text);

            if (pattern.matcher(sb.toString()).matches()) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        @Override
        public void remove(final FilterBypass fb, int offset, int length) throws BadLocationException {
            final StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.delete(offset, offset + length);

            if (pattern.matcher(sb.toString()).matches()) {
                super.remove(fb, offset, length);
            }
        }
    }

}