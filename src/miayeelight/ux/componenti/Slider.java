package miayeelight.ux.componenti;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.Serial;

import static javax.swing.SwingConstants.HORIZONTAL;
import static miayeelight.ux.schermo.Schermo.coloreDaTemperatura;
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
        final double p = Math.min((double) thumbRect.x / trackRect.width, 1.0);

        g.setColor(switch (preset) {
            case PRESETCT -> coloreDaTemperatura(p * 100, 1);
            case PRESETLUM -> new Color(80 + (int) (p * 175), 80 + (int) (p * 175), 80 + (int) (p * 175));
            case PRESETSAT -> new Color(115 + (int) (p * 140), 115 - (int) (p * 115), 115 - (int) (p * 115));
            case PRESETTON -> Color.getHSBColor((float) p, 0.6f, 1);
            default -> cManopolaDefault;
        });
        g.fillRoundRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, d(5), d(5));
    }

    private Color coloreTema() {
        final double p = Math.min((double) thumbRect.x / trackRect.width, 1.0);

        return switch (preset) {
            case PRESETCT -> coloreDaTemperatura(p * 100, 0.75);
            case PRESETLUM -> new Color(50 + (int) (p * 170), 50 + (int) (p * 170), 50 + (int) (p * 170));
            case PRESETSAT -> new Color(80 + (int) (p * 120), 80 - (int) (p * 80), 80 - (int) (p * 80));
            case PRESETTON -> Color.getHSBColor((float) p, 1, 1);
            default -> cBarraSx;
        };
    }

    @Override
    public void paintFocus(final Graphics g) {
        // Nessun bordo
    }

}
