package miayeelight.ux.componenti;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.io.Serial;

import static javax.swing.SwingConstants.HORIZONTAL;
import static miayeelight.ux.schermo.Schermo.coloreDaTemperatura;
import static miayeelight.ux.schermo.Schermo.d;

public class Slider extends BasicSliderUI {

    private static final int TRACK_HEIGHT = d(8);
    private static final int TRACK_WIDTH = d(8);
    private static final int TRACK_ARC = d(8);
    private static final Dimension THUMB_SIZE = new Dimension(d(5), d(20));
    private final Rectangle trackShape = new Rectangle();

    private static final Color cManopolaDefault = Color.red.darker();
    private static final Color cBarra = new Color(80, 80, 80);
    private static final Color cBordoBarra = new Color(170, 170, 170);
    private static final Color cBarraSx = Color.red.darker().darker();

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
        if (slider.getOrientation() == HORIZONTAL) {
            trackShape.setBounds(trackRect.x, trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2, trackRect.width, TRACK_HEIGHT);
        } else {
            trackShape.setBounds(trackRect.x + (trackRect.width - TRACK_WIDTH) / 2, trackRect.y, TRACK_WIDTH, trackRect.height);
        }
    }

    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();
        if (slider.getOrientation() == HORIZONTAL) {
            thumbRect.y = trackRect.y + (trackRect.height - thumbRect.height) / 2;
        } else {
            thumbRect.x = trackRect.x + (trackRect.width - thumbRect.width) / 2;
        }
    }

    @Override
    protected Dimension getThumbSize() {
        return THUMB_SIZE;
    }

    @Override
    public void paintTrack(final Graphics g) {
        final Shape clip = g.getClip();
        g.setClip(null);

        // Colora il bordo
        g.setColor(cBordoBarra);
        g.fillRoundRect(trackShape.x, trackShape.y, trackShape.width, trackShape.height, TRACK_ARC, TRACK_ARC);

        // Colora la barra
        g.setColor(cBarra);
        g.fillRoundRect(trackShape.x, trackShape.y + 1, trackShape.width, trackShape.height, TRACK_ARC, TRACK_ARC);

        // Colora la parte della barra a sinistra della manopola
        g.setColor(coloreTema());
        if (slider.getOrientation() == HORIZONTAL) {
            final boolean inverted = slider.getComponentOrientation().isLeftToRight() != slider.getInverted();
            final int thumbPos = thumbRect.x + thumbRect.width / 2;

            if (inverted) {
                g.fillRoundRect(trackShape.x, trackShape.y, thumbPos, trackShape.height, TRACK_ARC, TRACK_ARC);
            } else {
                g.fillRoundRect(thumbPos, trackShape.y, trackShape.width - thumbPos, trackShape.height, TRACK_ARC, TRACK_ARC);
            }
        } else {
            final boolean inverted = slider.getInverted();
            final int thumbPos = thumbRect.y + thumbRect.height / 2;

            if (inverted) {
                g.fillRoundRect(trackShape.x, trackShape.y, trackShape.width, thumbPos, TRACK_ARC, TRACK_ARC);
            } else {
                g.fillRoundRect(trackShape.x, thumbPos, trackShape.width, trackShape.height - thumbPos, TRACK_ARC, TRACK_ARC);
            }
        }

        g.setClip(clip);
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
