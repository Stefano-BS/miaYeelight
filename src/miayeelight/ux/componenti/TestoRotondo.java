package miayeelight.ux.componenti;

import javax.swing.*;
import java.awt.*;

import static miayeelight.ux.schermo.Schermo.d;

public class TestoRotondo extends JTextField {

    public TestoRotondo() {
        super();
        setOpaque(false);
    }

    public TestoRotondo(final String contenuto) {
        super(contenuto);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), d(10), d(10));

        g2.dispose();
        super.paintComponent(g);
    }

}
