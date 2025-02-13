package miayeelight.ux.componenti;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import static miayeelight.ux.schermo.Schermo.d;

public class PulsanteRotondo extends BasicButtonUI {

    public enum TipoArrotondamento {
        NESSUNO, LIMITATO, TOTALE
    }

    private final TipoArrotondamento arrotondamento;
    private Color sfondo;

    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent c) {
        return new PulsanteRotondo();
    }

    public PulsanteRotondo(TipoArrotondamento arrotondamento) {
        this.arrotondamento = arrotondamento;
    }

    public PulsanteRotondo() {
        this(TipoArrotondamento.LIMITATO);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setOpaque(false);
        sfondo = c.getBackground();

        if (c instanceof AbstractButton pulsante) {
            pulsante.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    ricolora(sfondo.brighter());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ricolora(sfondo);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    ricolora(sfondo);
                }

                private void ricolora(final Color colore) {
                    pulsante.setBackground(colore);
                    pulsante.repaint();
                }

            });
        }
    }

    @Override
    public void paint(Graphics g, JComponent componente) {
        final AbstractButton pulsante = (AbstractButton) componente;
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(pulsante.getModel().isPressed() ? sfondo.darker() : pulsante.getBackground());

        final int arcoOrizzontale = switch (arrotondamento) {
            case NESSUNO -> 0;
            case LIMITATO -> d(10);
            case TOTALE -> pulsante.getWidth();
        };

        final int arcoVerticale = switch (arrotondamento) {
            case NESSUNO -> 0;
            case LIMITATO -> d(10);
            case TOTALE -> pulsante.getHeight();
        };

        g2.fill(new RoundRectangle2D.Float(0, 0, pulsante.getWidth(), pulsante.getHeight(), arcoOrizzontale, arcoVerticale));

        final FontMetrics fm = g2.getFontMetrics();
        int textX = (pulsante.getWidth() - fm.stringWidth(pulsante.getText())) / 2;
        int textY = (pulsante.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(pulsante.getForeground());
        g2.drawString(pulsante.getText(), textX, textY);
    }

}
