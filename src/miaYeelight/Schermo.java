package miaYeelight;

import java.awt.Color;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.AWTException;

class Schermo {
	static Color avg = new Color(0,0,0);
	
    public static Color ottieniMedia(int punti, double peso, double varianza) {
    	int r = 0, g = 0, b = 0;
    	if (varianza!=0) punti *= 1+(Math.random()-0.5)*varianza;
    	int w = Toolkit.getDefaultToolkit().getScreenSize().width;
    	int npx = w*Toolkit.getDefaultToolkit().getScreenSize().height;
    	int passo = (int)Math.floor(((double)npx)/punti);
        
    	try {
            Robot robot = new Robot();
            
            for (int i=0; i<=npx; i+=passo) {
	            Color color = robot.getPixelColor(i%w, Math.floorDiv(i, w));
	            r += color.getRed();
	            g += color.getGreen();
	            b += color.getBlue();
            }
            r /= punti;
            g /= punti;
            b /= punti;
        } 
        catch (AWTException e) {return avg;}
        
        r = (int) (avg.getRed()*(1-peso)+r*peso);
        g = (int) (avg.getGreen()*(1-peso)+g*peso);
        b = (int) (avg.getBlue()*(1-peso)+b*peso);
        avg = new Color(r, g, b);
        return avg;
    }
}