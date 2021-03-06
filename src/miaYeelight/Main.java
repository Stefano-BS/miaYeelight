package miaYeelight;

import java.io.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Main extends JFrame {
	private static final long serialVersionUID = 0L;
	static final Font f = new Font("sans", Font.PLAIN, 18);
    static final Font f2 = new Font("sans", Font.PLAIN, 16);
    static final Color sh1 = new Color(40,40,40,255),
    		bg = new Color(0,0,0,255),
    		trasparente = new Color(0,0,0,0);
	final ImageIcon yee = caricaIcona();
	
	Color accentColor;
	Timer aggiornatoreWinAccent;
	Timer extList = new Timer();
	int lkpX = 0, lkpY = 0;
	
	JFrame frame;
	JPanel rosso = new JPanel();
	JLabel titolo;
	PannelloConnessione pannelloConnessione;
	PannelloPrincipale pannello;
	PannelloAnimazioni pannelloAnimazioni;
	String nomeLampadina = "miaYeelight";
	
	Connessione connessione = null;
    
	public static void main(String[] args) throws IOException {
		configuraUIManager();
		new Main();
	}
	
	Main() throws IOException {
		frame = new JFrame();
		frame.setUndecorated(true);
		frame.setTitle("miaYeelight");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(yee.getImage());
		frame.setBackground(new Color(0,0,0,0));
		creaBarraDelTitolo();
		rosso.setBounds(0, 0, 530, 40);
        frame.getContentPane().add(rosso,BorderLayout.NORTH);
        frame.getContentPane().add(pannelloConnessione = new PannelloConnessione(this, true), BorderLayout.CENTER);
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    	int screenW = (int)(screenSize.getWidth());
    	int screenH = (int)(screenSize.getHeight());
        frame.setBounds(screenW/2-265, screenH/2-pannelloConnessione.getHeight(), 530, rosso.getHeight() + pannelloConnessione.getHeight());
        
        connessione = new Connessione(this);
        if (connessione.connetti(true)) {
        	String[] proprieta = connessione.scaricaProprieta();
        	tornaStatico();
			configuraPannelloPrincipaleConStatoLampada(proprieta);
			schedulaExtListener();
			frame.setVisible(true);
        } else JOptionPane.showMessageDialog(null, "<HTML><h2>Impossibile connettersi alla lampadina</h2><br>" +
				"Possibili cause:" +
				"<ul><li>la lampadina non è connessa alla corrente o al wifi</li>" + 
				"<li>la lampadina non è configurata in modalità sviluppatore</li>" + 
				"<li>è fallito il timeout di 200ms, in tal caso riprovare</li>" + 
				"</ul></HTML>", "Errore di connessione", JOptionPane.ERROR_MESSAGE, yee);
	}
	
	void configuraPannelloPrincipaleConStatoLampada(String [] statoIniziale) {
        pannello.modoDiretto = false;
        if (statoIniziale[0].equals("on")) pannello.accendi.setText("🕯  Spegni");
        pannello.luminosita.setValue(Integer.parseInt(statoIniziale[1]));
        pannello.temperatura.setValue(Integer.parseInt(statoIniziale[5]));
        pannello.hue.setValue(Integer.parseInt(statoIniziale[3]));
        pannello.sat.setValue(Integer.parseInt(statoIniziale[4]));
        nomeLampadina = statoIniziale[6];
        titolo.setText(nomeLampadina);
        pannello.modoDiretto = true;
	}
	
	
	void tornaModalitaRicerca() {
		terminaAggiornatoreWinAccent();
		extList.cancel();
		extList = new Timer();
		connessione.chiudi();
		connessione = new Connessione(this);
		nomeLampadina = "miaYeelight";
		frame.getContentPane().removeAll();
		frame.getContentPane().add(rosso, BorderLayout.NORTH);
		frame.getContentPane().add(pannelloConnessione = new PannelloConnessione(this, false), BorderLayout.CENTER);
        frame.setSize(530, rosso.getHeight() + pannelloConnessione.getHeight());
        frame.revalidate();
	}
	
	void apriPannelloAnimazioni() {
		terminaAggiornatoreWinAccent();
		if (pannelloAnimazioni == null) pannelloAnimazioni = new PannelloAnimazioni(this);
		frame.getContentPane().removeAll();
		frame.getContentPane().add(rosso, BorderLayout.NORTH);
		frame.getContentPane().add(pannelloAnimazioni, BorderLayout.CENTER);
		frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloAnimazioni.getHeight());
		frame.revalidate();
	}
	
	void tornaStatico() {
		if (pannello == null) {
			pannello = new PannelloPrincipale(this);
			pannello.setBounds(0, 0, 530, pannello.getHeight());
		}
		frame.getContentPane().removeAll();
		frame.getContentPane().add(rosso, BorderLayout.NORTH);
		frame.getContentPane().add(pannello, BorderLayout.CENTER);
		frame.setSize(rosso.getWidth(), rosso.getHeight() + pannello.getHeight());
		frame.revalidate();
	}
	
	void creaBarraDelTitolo() {
		titolo = new JLabel(nomeLampadina);
		JButton disconnetti = new JButton("❌");
		JLabel icona = new JLabel();
		rosso.setLayout(null);
        rosso.setBackground(new Color(160,0,0));
        titolo.setBounds(0, 0, 530, 40);
        titolo.setHorizontalAlignment(SwingConstants.CENTER); titolo.setVerticalAlignment(SwingConstants.CENTER);
        rosso.add(titolo);
        titolo.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent click) {
				if (click.getX() > 490) disconnetti.doClick();
				else if (click.getX() > 40) {
					String nome = JOptionPane.showInputDialog("Scegli il nuovo nome di questa lampadina", nomeLampadina);
					if (nome != null && nome != "") {
						connessione.cambiaNome(nome);
						nomeLampadina = nome;
						titolo.setText(nomeLampadina);
					}
				}
				else if (!Arrays.asList(frame.getContentPane().getComponents()).contains(pannelloConnessione)) tornaModalitaRicerca();
			}
			public void mouseEntered(MouseEvent click) {
				if (click.getX() > 490) disconnetti.setBackground(new Color(90,0,0));}
			public void mouseExited(MouseEvent click) {
				disconnetti.setBackground(new Color(120,0,0));
				lkpX = 0; lkpY = 0;
				titolo.setText(nomeLampadina);}
			public void mousePressed(MouseEvent click) {
				if (click.getX() > 490) disconnetti.setBackground(new Color(60,0,0));}
			public void mouseReleased(MouseEvent click) {
				if (click.getX() > 490) disconnetti.setBackground(new Color(120,0,0));
				lkpX = 0; lkpY = 0;
			}
        });
        titolo.addMouseMotionListener(new MouseMotionListener() {
        	public void mouseDragged(MouseEvent d) {
        		if (d.getX() < 490) {
        			int nuovaX = d.getXOnScreen(), nuovaY = d.getYOnScreen();
    				if (lkpX !=0 || lkpY!=0) {
	        			int xInc = nuovaX-lkpX,
	        				yInc = nuovaY-lkpY;
	        			frame.setLocation(nuovaX-d.getX()+xInc, nuovaY-d.getY()+yInc);
    				}
    				lkpX = nuovaX; lkpY = nuovaY;
    			}
			}
			public void mouseMoved(MouseEvent d) {
				if (d.getX() > 490) disconnetti.setBackground(new Color(90,0,0));
				else disconnetti.setBackground(new Color(120,0,0));
				if (d.getX() <= 40 && !Arrays.asList(frame.getContentPane().getComponents()).contains(pannelloConnessione)) titolo.setText("Cambia lampadina ...");
				else titolo.setText(nomeLampadina);
			}
		});
        
        disconnetti.setBounds(490, 0, 40, 40);
        disconnetti.addActionListener(click -> {
        	connessione.chiudi();
        	System.exit(0);
        });
        disconnetti.setFocusable(false);
        disconnetti.setBackground(new Color(120,0,0));
        rosso.add(disconnetti);
        icona.setIcon(yee);
        icona.setBounds(0,0,40,40);
        rosso.add(icona);
        rosso.setPreferredSize(new Dimension(530,40));
	}
	
	void schedulaExtListener() {
		extList.schedule(new TimerTask() {
			public void run() {
				if (Arrays.asList(frame.getContentPane().getComponents()).contains(pannello)) {
					boolean modoPrima = pannello.modoDiretto;
					pannello.modoDiretto = false;
					configuraPannelloPrincipaleConStatoLampada(connessione.scaricaProprieta());
					pannello.modoDiretto = modoPrima;
				}
			}
		}, 2500, 2697);
	}
	
	void tienitiAggiornataSuWindows() {
		if (aggiornatoreWinAccent == null) {
			pannello.seguiWinAccent.setText("⏸ Non seguire più");
			cambiaColoreDaAccent();
			aggiornatoreWinAccent = new Timer();
			aggiornatoreWinAccent.schedule(new TimerTask() {
				public void run() {
					if (accentColor.getRGB() != SystemColor.activeCaption.getRGB()) cambiaColoreDaAccent();
				}
			}, 0, 2000);
		} else terminaAggiornatoreWinAccent();
	}
	
	void terminaAggiornatoreWinAccent() {
		if (aggiornatoreWinAccent != null) {
			aggiornatoreWinAccent.cancel();
			aggiornatoreWinAccent = null;
			pannello.seguiWinAccent.setText("Segui il colore di Windows 10");
		}
	}
	
	void cambiaColoreDaAccent() {
		accentColor = new Color(SystemColor.activeCaption.getRed(), SystemColor.activeCaption.getGreen(), SystemColor.activeCaption.getBlue());
    	float[] hsbVals = Color.RGBtoHSB(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), new float[3]);
    	boolean modoPrima = pannello.modoDiretto;
    	pannello.modoDiretto = false;
    	pannello.hue.setValue((int)(hsbVals[0]*360));
    	pannello.sat.setValue((int)(hsbVals[1]*70+30));
    	pannello.luminosita.setValue((int)(hsbVals[2]*100));
    	pannello.modoDiretto = modoPrima;
    	connessione.setHS((int)(hsbVals[0]*360), (int)(hsbVals[1]*70+30));
    	try {Thread.sleep(100);} catch (InterruptedException e) {}
    	connessione.setBr((int)(hsbVals[2]*100));
	}
	
	
	ImageIcon caricaIcona() {
		try {
			return new ImageIcon(ImageIO.read(Main.class.getResource("yee.png")).getScaledInstance(40,40,Image.SCALE_SMOOTH));
		} catch (Exception e) {return null;}
	}
	
	static void configuraUIManager() {
    	//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		UIManager.put("Button.font", f2);
        UIManager.put("Button.background", sh1);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", sh1.brighter());
        UIManager.put("Button.border", 0);
        UIManager.put("TextField.font", f2);
        UIManager.put("TextField.background", sh1.brighter());
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.border", 0);
        UIManager.put("Label.font", f);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Panel.background", Color.black);
        UIManager.put("OptionPane.messageFont", f2);
		UIManager.put("OptionPane.buttonFont", f2);
		UIManager.put("OptionPane.background", Color.black);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		UIManager.put("List.foreground", Color.WHITE);
		UIManager.put("List.background", sh1);
		UIManager.put("List.selectionBackground", sh1.brighter());
		UIManager.put("List.selectionForeground", Color.WHITE);
		UIManager.put("List.font", f2);
		UIManager.put("ScrollBar.background", sh1.brighter().brighter());
    }
}