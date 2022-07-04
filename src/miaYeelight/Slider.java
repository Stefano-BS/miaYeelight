package miaYeelight;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

class Slider extends BasicSliderUI {
	private static final int TRACK_HEIGHT = Schermo.d(8);
	private static final int TRACK_WIDTH = Schermo.d(8);
	private static final int TRACK_ARC = Schermo.d(8);
	private static final Dimension THUMB_SIZE = new Dimension(Schermo.d(5), Schermo.d(20));
	private final RoundRectangle2D.Float trackShape = new RoundRectangle2D.Float();
	
	private static final Color  cManopolaDefault = Color.red.darker(),
								cBarraDefault = new Color(80, 80 ,80),
								cBordoBarraDefault = new Color(170, 170 ,170),
								cBarraSxDefault = Color.red.darker().darker();
	private Color cManopola = cManopolaDefault, cBarra = cBarraDefault, cBordoBarra = cBordoBarraDefault, cBarraSx = cBarraSxDefault;
	
	public static final int PRESETDEFAULT = 0, PRESETLUM = 1, PRESETCT = 2, PRESETTON = 3, PRESETSAT = 4;
	private int preset = PRESETDEFAULT;
	
	private Slider(final JSlider b) {
	    super(b);
	    b.setBackground(Color.black);
	}
	
	public static JSlider fab() {
		return new JSlider() {
	        private static final long serialVersionUID = 1L;
	        public void updateUI() {setUI(new Slider(this));}
		};
    }
	
	public static JSlider fab(int preset) {
		JSlider fab = new JSlider() {
	        private static final long serialVersionUID = 1L;
	        public void updateUI() {setUI(new Slider(this));}
		};
		((Slider) fab.getUI()).preset = preset;
		return fab;
    }
	
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
	
	protected void calculateThumbLocation() {
	    super.calculateThumbLocation();
	    if (isHorizontal()) thumbRect.y = trackRect.y + (trackRect.height - thumbRect.height) / 2;
	    else thumbRect.x = trackRect.x + (trackRect.width - thumbRect.width) / 2;
	}
	
	protected Dimension getThumbSize() {return THUMB_SIZE;}
	private boolean isHorizontal() {return slider.getOrientation() == JSlider.HORIZONTAL;}
	
	public void paint(final Graphics g, final JComponent c) {
	    //((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    super.paint(g, c);
	}
	
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
	        if (slider.getComponentOrientation().isLeftToRight()) inverted = !inverted;
	        int thumbPos = thumbRect.x + thumbRect.width / 2;
	        if (inverted) g2.clipRect(0, 0, thumbPos, slider.getHeight());
	        else g2.clipRect(thumbPos, 0, slider.getWidth() - thumbPos, slider.getHeight());
	    } else {
	        int thumbPos = thumbRect.y + thumbRect.height / 2;
	        if (inverted) g2.clipRect(0, 0, slider.getHeight(), thumbPos);
	        else g2.clipRect(0, thumbPos, slider.getWidth(), slider.getHeight() - thumbPos);
	    }
	    
	    g2.setColor(coloreTema());
	    g2.fill(trackShape);
	    g2.setClip(clip);
	    trackShape.y -= 1;
	}
	
	public void paintThumb(final Graphics g) {
		if (preset == PRESETDEFAULT) g.setColor(cManopola);
		else g.setColor(coloreTema().brighter());
	    g.fillRoundRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, Schermo.d(5), Schermo.d(5));
	}
	
	private Color coloreTema() {
		if (preset == PRESETDEFAULT) return (cBarraSx);
		double p = (double)thumbRect.x / trackRect.width;
		if (preset == PRESETCT) return new Color(100+(int)(100*(1-p)), 100+(int)(60*p), 50+(int)(100*p));
		if (preset == PRESETLUM) return new Color(60+(int)(p*195), 40+(int)(p*160), 40+(int)(p*160));
		if (preset == PRESETTON) return new Color(
				p<0.17? 255 : p<0.34? (int)(255*5.9*(0.34-p)) : p<0.67? 0 : p<0.83? (int)(255*5.9*(p-0.66)) : 255,
				p<0.16? (int)(255*6*p) : p<0.5? 255 : p<0.66? (int)(255*6*(0.66-p)) : 0,
				p<0.34? 0 : p<0.5? (int)(255*6*(p-0.33)) : p<0.84? 255 : (int)(255*6*(1-p)));
		if (preset == PRESETSAT) return new Color(100+(int)(p*150), 100-(int)(p*100), 100-(int)(p*100));
	    return (cBarraSx);
	}
	
	public void paintFocus(final Graphics g) {}
}
