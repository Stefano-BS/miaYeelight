package miayeelight.ux.componenti;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.Serial;

import static javax.swing.SwingConstants.HORIZONTAL;
import static miayeelight.ux.schermo.Schermo.d;

public class Slider extends BasicSliderUI {

    private static final int TRACK_HEIGHT = d(8);
    private static final int TRACK_WIDTH = d(8);
    private static final int TRACK_ARC = d(8);
    private static final Dimension THUMB_SIZE = new Dimension(d(5), d(20));
    private final RoundRectangle2D.Float trackShape = new RoundRectangle2D.Float();

    private static final Color cManopolaDefault = Color.red.darker();
    private static final Color cBarraDefault = new Color(80, 80, 80);
    private static final Color cBordoBarraDefault = new Color(170, 170, 170);
    private static final Color cBarraSxDefault = Color.red.darker().darker();

    private static final Color cManopola = cManopolaDefault;
    private static final Color cBarra = cBarraDefault;
    private static final Color cBordoBarra = cBordoBarraDefault;
    private static final Color cBarraSx = cBarraSxDefault;

    public static final int PRESETDEFAULT = 0;
    public static final int PRESETLUM = 1;
    public static final int PRESETCT = 2;
    public static final int PRESETTON = 3;
    public static final int PRESETSAT = 4;
    private int preset = PRESETDEFAULT;

    private Slider(final JSlider b) {
        super(b);
        b.setBackground(Color.black);
    }

    public static JSlider fab(int preset) {
        JSlider fab = new JSlider() {

            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void updateUI() {
                setUI(new Slider(this));
            }
        };
        ((Slider) fab.getUI()).preset = preset;
        return fab;
    }

    @Override
    protected void calculateTrackRect() {
        super.calculateTrackRect();
        if (isHorizontal()) {
            trackRect.y = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2;
            trackRect.height = TRACK_HEIGHT;
        } else {
            trackRect.x = trackRect.x + (trackRect.width - TRACK_WIDTH) / 2;
            trackRect.width = TRACK_WIDTH;
        }
        trackShape.setRoundRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height, TRACK_ARC, TRACK_ARC);
    }

    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();
        if (isHorizontal()) {
            thumbRect.y = trackRect.y + (trackRect.height - thumbRect.height) / 2;
        } else {
            thumbRect.x = trackRect.x + (trackRect.width - thumbRect.width) / 2;
        }
    }

    @Override
    protected Dimension getThumbSize() {
        return THUMB_SIZE;
    }

    private boolean isHorizontal() {
        return slider.getOrientation() == HORIZONTAL;
    }

    @Override
    public void paintTrack(final Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Shape clip = g2.getClip();
        boolean inverted = slider.getInverted();

        // Colora il bordo
        g2.setColor(cBordoBarra);
        g2.fill(trackShape);

        // Colora la barra
        g2.setColor(cBarra);
        g2.setClip(trackShape);
        trackShape.y += 1;
        g2.fill(trackShape);

        // Colora la parte della barra a sinistra della manopola
        if (isHorizontal()) {
            if (slider.getComponentOrientation().isLeftToRight()) {
                inverted = !inverted;
            }
            int thumbPos = thumbRect.x + thumbRect.width / 2;
            if (inverted) {
                g2.clipRect(0, 0, thumbPos, slider.getHeight());
            } else {
                g2.clipRect(thumbPos, 0, slider.getWidth() - thumbPos, slider.getHeight());
            }
        } else {
            int thumbPos = thumbRect.y + thumbRect.height / 2;
            if (inverted) {
                g2.clipRect(0, 0, slider.getHeight(), thumbPos);
            } else {
                g2.clipRect(0, thumbPos, slider.getWidth(), slider.getHeight() - thumbPos);
            }
        }

        g2.setColor(coloreTema());
        g2.fill(trackShape);
        g2.setClip(clip);
        trackShape.y -= 1;
    }

    @Override
    public void paintThumb(final Graphics g) {
        if (preset == PRESETDEFAULT) {
            g.setColor(cManopola);
        } else {
            g.setColor(coloreTema().brighter());
        }
        g.fillRoundRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, d(5), d(5));
    }

    @SuppressWarnings("java:S3358")
    private Color coloreTema() {
        if (preset == PRESETDEFAULT) {
            return (cBarraSx);
        }
        double p = Math.min((double) thumbRect.x / trackRect.width, 1.0);
        if (preset == PRESETCT) {
            return new Color(100 + (int) (100 * (1 - p)), 100 + (int) (60 * p), 50 + (int) (100 * p));
        }
        if (preset == PRESETLUM) {
            return new Color(60 + (int) (p * 195), 40 + (int) (p * 160), 40 + (int) (p * 160));
        }
        if (preset == PRESETTON) {
            return new Color( //
                    p < 0.17 ? 255 : p < 0.34 ? (int) (255 * 5.9 * (0.34 - p)) : p < 0.67 ? 0 : p < 0.83 ? (int) (255 * 5.9 * (p - 0.66)) : 255, //
                    p < 0.16 ? (int) (255 * 6 * p) : p < 0.5 ? 255 : p < 0.66 ? (int) (255 * 6 * (0.66 - p)) : 0, //
                    p < 0.34 ? 0 : p < 0.5 ? (int) (255 * 6 * (p - 0.33)) : p < 0.84 ? 255 : (int) (255 * 6 * (1 - p)));
        }
        if (preset == PRESETSAT) {
            return new Color(100 + (int) (p * 150), 100 - (int) (p * 100), 100 - (int) (p * 100));
        }
        return (cBarraSx);
    }

    @Override
    public void paintFocus(final Graphics g) {
        // Nessun bordo
    }

}
