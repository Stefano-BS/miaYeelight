package miayeelight.ux.schermo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.io.Serializable;

public class Schermo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Dimension DIMENSIONI = Toolkit.getDefaultToolkit().getScreenSize();
    private static Color avg = new Color(0, 0, 0);
    private static double ratio = ((double) Toolkit.getDefaultToolkit().getScreenResolution()) / 100;

    public static void setRatio(double ratio) {
        Schermo.ratio = ratio;
    }

    public static int d(int dim) {
        return (int) (dim * ratio);
    }

    public static Color ottieniMedia(final double peso) {
        final BufferedImage image;
        try {
            image = new Robot().createScreenCapture(new Rectangle(DIMENSIONI));
        } catch (AWTException e) {
            return avg;
        }

        long sommaR = 0;
        long sommaG = 0;
        long sommaB = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                sommaR += (pixel >> 16) & 0xFF;
                sommaG += (pixel >> 8) & 0xFF;
                sommaB += pixel & 0xFF;
            }
        }

        final int pixelCount = image.getHeight() * image.getWidth();
        final int r = (int) (avg.getRed() * (1 - peso) + ((double) sommaR / pixelCount) * peso);
        final int g = (int) (avg.getGreen() * (1 - peso) + ((double) sommaG / pixelCount) * peso);
        final int b = (int) (avg.getBlue() * (1 - peso) + ((double) sommaB / pixelCount) * peso);
        avg = new Color(r, g, b);
        return avg;
    }

    public static Color ottieniMedia(int punti, double peso, double varianza) {
        int r = 0;
        int g = 0;
        int b = 0;

        if (varianza != 0) {
            punti *= (int) (1 + (Math.random() - 0.5) * varianza);
        }

        final int NPX = DIMENSIONI.width * DIMENSIONI.height;
        final int passo = (int) Math.floor(((double) NPX) / punti);

        try {
            Robot robot = new Robot();

            for (int i = 0; i <= NPX; i += passo) {
                final Color color = robot.getPixelColor(i % DIMENSIONI.width, Math.floorDiv(i, DIMENSIONI.width));
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
            }
            r /= punti;
            g /= punti;
            b /= punti;
        } catch (AWTException e) {
            return avg;
        }

        r = (int) (avg.getRed() * (1 - peso) + r * peso);
        g = (int) (avg.getGreen() * (1 - peso) + g * peso);
        b = (int) (avg.getBlue() * (1 - peso) + b * peso);
        avg = new Color(r, g, b);
        return avg;
    }

}