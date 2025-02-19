package miayeelight.ux.componenti;

import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.net.Connessione;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serial;
import java.util.Arrays;

import static miayeelight.ux.schermo.Schermo.d;

public class BarraTitolo extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Color X = new Color(140, 0, 0);
    public static final Color X_PUNTATORE = new Color(120, 0, 0);
    public static final Color X_PREMUTA = new Color(100, 0, 0);

    private final JLabel titolo;
    private final JButton disconnetti = new JButton("❌");
    private final Main ref;

    private int lkpX = 0;
    private int lkpY = 0;

    public BarraTitolo(final Main ref, final ImageIcon yee) {
        this.ref = ref;

        setLayout(null);
        setBackground(new Color(160, 0, 0));

        titolo = new JLabel(ref.getNomeLampadina());
        titolo.setBounds(0, 0, d(530), d(40));
        titolo.setHorizontalAlignment(SwingConstants.CENTER);
        titolo.setVerticalAlignment(SwingConstants.CENTER);
        titolo.addMouseListener(new GestoreClickBarraTitolo());
        titolo.addMouseMotionListener(new GestoreMovimentoBarraTitolo());
        add(titolo);

        disconnetti.setBounds(d(490), 0, d(40), d(40));
        disconnetti.setUI(new PulsanteRotondo(PulsanteRotondo.TipoArrotondamento.NESSUNO));
        disconnetti.addActionListener(click -> System.exit(0));
        disconnetti.setFocusable(false);
        disconnetti.setBackground(X);
        add(disconnetti);

        final JLabel icona = new JLabel();
        icona.setIcon(yee);
        icona.setBounds(0, 0, d(40), d(40));
        add(icona);

        setPreferredSize(new Dimension(d(530), d(40)));
    }

    public void setTitolo(final String titolo) {
        this.titolo.setText(titolo);
    }

    private class GestoreClickBarraTitolo implements MouseListener {

        public void mouseClicked(MouseEvent click) {
            if (click.getX() > d(490)) {
                disconnetti.doClick();
            } else if (click.getX() > d(40)) {
                final String nome = JOptionPane.showInputDialog(Strings.get(Main.class, "16"), ref.getNomeLampadina());
                if (nome != null && !nome.isEmpty()) {
                    Connessione.istanza().cambiaNome(nome);
                    ref.setNomeLampadina(nome);
                    titolo.setText(nome);
                }
            } else if (!Arrays.asList(ref.getFrame().getContentPane().getComponents()).contains(ref.getPannelloConnessione())) {
                ref.tornaModoRicerca();
            }
        }

        public void mouseEntered(MouseEvent click) {
            if (click.getX() > d(490)) {
                disconnetti.setBackground(X_PUNTATORE);
            }
        }

        public void mouseExited(MouseEvent click) {
            disconnetti.setBackground(X);
            lkpX = 0;
            lkpY = 0;
            titolo.setText(ref.getNomeLampadina());
        }

        public void mousePressed(MouseEvent click) {
            if (click.getX() > d(490)) {
                disconnetti.setBackground(X_PREMUTA);
            }
        }

        public void mouseReleased(MouseEvent click) {
            if (click.getX() > d(490)) {
                disconnetti.setBackground(X);
            }
            lkpX = 0;
            lkpY = 0;
        }
    }

    private class GestoreMovimentoBarraTitolo implements MouseMotionListener {

        public void mouseDragged(MouseEvent d) {
            if (d.getX() < d(490)) {
                int nuovaX = d.getXOnScreen();
                int nuovaY = d.getYOnScreen();
                if (lkpX != 0 || lkpY != 0) {
                    int xInc = nuovaX - lkpX;
                    int yInc = nuovaY - lkpY;
                    ref.getFrame().setLocation(nuovaX - d.getX() + xInc, nuovaY - d.getY() + yInc);
                }
                lkpX = nuovaX;
                lkpY = nuovaY;
            }
        }

        public void mouseMoved(MouseEvent d) {
            if (d.getX() > d(490)) {
                disconnetti.setBackground(X_PUNTATORE);
            } else {
                disconnetti.setBackground(X);
            }
            if (d.getX() <= d(40) && !Arrays.asList(ref.getFrame().getContentPane().getComponents()).contains(ref.getPannelloConnessione())) {
                titolo.setText(Strings.get(Main.class, "18"));
            } else {
                titolo.setText(ref.getNomeLampadina());
            }
        }
    }

}