package miayeelight.ux.pannelli;

import miayeelight.Main;
import miayeelight.lang.Strings;
import miayeelight.net.Connessione;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;

import static miayeelight.Main.semiTrasparente;
import static miayeelight.ux.schermo.Schermo.d;

public class PannelloConnessionePersa extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    public PannelloConnessionePersa(final Main ref) {
        super(null);

        setBackground(semiTrasparente);
        setOpaque(true);
        setFocusable(true);
        setEnabled(true);
        setBounds(0, d(40), d(530), ref.getFrame().getHeight() - d(40));

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                e.consume();
            }

        });

        final JLabel m1 = new JLabel(Strings.get(Connessione.class, "6"));
        m1.setFont(ref.carattereGrande);
        m1.setHorizontalAlignment(SwingConstants.CENTER);

        final JLabel m2 = new JLabel(Strings.get(Connessione.class, "9"));
        m2.setFont(ref.caratterePiccolo);
        m2.setHorizontalAlignment(SwingConstants.CENTER);

        FontMetrics fm = m1.getFontMetrics(m1.getFont());
        final int a1 = (fm.getAscent() - fm.getDescent()) * 2;

        fm = m2.getFontMetrics(m2.getFont());
        final int a2 = (fm.getAscent() - fm.getDescent()) * 2;

        m1.setBounds(0, getHeight() / 2 - a1 - a2 - d(5), d(530), a1);
        m2.setBounds(0, getHeight() / 2 - a2, d(530), a2);

        add(m1);
        add(m2);
    }

}
