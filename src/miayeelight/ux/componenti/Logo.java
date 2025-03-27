package miayeelight.ux.componenti;


import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Logo extends JPanel {

    public static final Color LOGO_NORMALE = new Color(220, 0, 0);
    public static final Color LOGO_PUNTATORE = new Color(210, 0, 0);
    public static final Color LOGO_PREMUTO = new Color(190, 0, 0);

    private static final double SCALA_BORDO = 0.34;
    private static final List<Double> X = List.of(32.0, 32.0, 12.8, 12.8, 43.0, 43.0, 57.0, 57.0, 88.4, 88.4, 69.2, 69.2, 100.0, 100.0, 50.0, 0.0, 0.0);
    private static final List<Double> Y = List.of(74.0, 60.8, 53.6, 39.2, 52.4, 78.8, 78.8, 52.4, 39.2, 53.6, 60.8, 74.0, 62.0, 20.0, 40.4, 20.0, 62.0);

    public Logo() {
        super(null);
        setBackground(LOGO_NORMALE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        final double oRatio = getWidth() / (100 * (1 + SCALA_BORDO));
        final double vRatio = getHeight() / (100 * (1 + SCALA_BORDO));
        final double oBordo = SCALA_BORDO * oRatio * 50;
        final double vBordo = SCALA_BORDO * oRatio * 50;

        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(X.stream().mapToInt(n -> (int) (n * oRatio + oBordo)).toArray(), Y.stream().mapToInt(n -> (int) (n * vRatio + vBordo)).toArray(), X.size());
    }

}

