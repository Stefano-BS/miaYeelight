package miaYeelight;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.*;

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
	static final ImageIcon yee = caricaIcona(); 
	static JFrame frame;
	static JPanel rosso = new JPanel();
	static PannelloConnessione pannelloConnessione;
	static PannelloPrincipale pannello;
	static PannelloAnimazioni pannelloAnimazioni;
	static Socket telnet = null;
    static PrintStream out = null;
    static final Font f = new Font("sans", Font.PLAIN, 18);
    static final Font f2 = new Font("sans", Font.PLAIN, 16);
    static final Color sh1 = new Color(40,40,40,255),
    		bg = new Color(0,0,0,255),
    		trasparente = new Color(0,0,0,0);
    static Color accentColor;
    static Timer aggiornatoreWinAccent;
    static protected boolean stop = false,
    		finitoIlCiclo = false;
    static String ipVarGlobale;
    
	public static void main(String[] args) throws IOException {
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
		
		frame=new JFrame();
		frame.setUndecorated(true);
		frame.setTitle("miaYeelight");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(yee.getImage());
		frame.setBackground(new Color(0,0,0,0));
		creaBarraDelTitolo();
		rosso.setBounds(0, 0, 530, 40);
        frame.getContentPane().add(rosso,BorderLayout.NORTH);
        frame.getContentPane().add(pannelloConnessione = new PannelloConnessione(), BorderLayout.CENTER);
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    	int screenW = (int)(screenSize.getWidth());
    	int screenH = (int)(screenSize.getHeight());
        frame.setBounds(screenW/2-265, screenH/2-pannelloConnessione.getHeight(), 530, rosso.getHeight() + pannelloConnessione.getHeight());
        connetti();
	}
	
	static void connetti() throws IOException {
		try { //Tentativo per IP locale 100 
			pannelloConnessione.desc.setText("In connessione a 192.168.1.100");
			(telnet = new Socket()).connect(new InetSocketAddress("192.168.1.100", 55443),1000);
            //telnet = new Socket("192.168.1.100", 55443);	//Abbandonata questa modalità perché non permette di impostare un timeout a priori
            out = new PrintStream(telnet.getOutputStream(), true);
        } catch (ConnectException | SocketTimeoutException e) {
        	frame.setVisible(true);
			for (int i = 2; i<255; i++) {
				try {
					if (stop) {
						stop = false;
						i--;
						pannelloConnessione.desc.setText("Su richiesta, mi connetto a " + ipVarGlobale);
						(telnet = new Socket()).connect(new InetSocketAddress(ipVarGlobale, 55443),2000);
			            out = new PrintStream(telnet.getOutputStream(), true);
			            break;
					}
					pannelloConnessione.desc.setText("In connessione a 192.168.1." + i);
		        	(telnet = new Socket()).connect(new InetSocketAddress("192.168.1." + i, 55443), 200);
		            out = new PrintStream(telnet.getOutputStream(), true);
		            break;
		        } 
		        catch (ConnectException | SocketTimeoutException ex) {continue;}
			}
        }
		finitoIlCiclo = true;
		if (telnet.isConnected()) {
			tornaStatico();
			frame.setVisible(true); 
		} else JOptionPane.showMessageDialog(null, "<HTML><h2>Impossibile connettersi alla lampadina</h2><br>" +
				"Possibili cause:" +
				"<ul><li>la lampadina non è connessa alla corrente o al wifi</li>" + 
				"<li>la lampadina non è configurata in modalità sviluppatore</li>" + 
				"<li>è fallito il timeout di 200ms, in tal caso riprovare</li>" + 
				"</ul></HTML>", "Errore di connessione", JOptionPane.ERROR_MESSAGE, yee);
	}
	
	static void connettiA(String ip) {
		if (finitoIlCiclo) {
			pannelloConnessione.desc.setText("Su richiesta, mi connetto a " + ip);
			try {
				(telnet = new Socket()).connect(new InetSocketAddress(ip, 55443));
	            out = new PrintStream(telnet.getOutputStream(), true);
	        } catch (IOException e) {}
			if (telnet.isConnected()) tornaStatico(); else JOptionPane.showMessageDialog(null, "Impossibile connettersi a questo indirizzo", "Lampadina non trovata", JOptionPane.ERROR_MESSAGE, yee);
		} else {
			stop = true;
			ipVarGlobale = ip;
		}
	}
	
	static void accendi() {out.println("{\"id\":0,\"method\":\"set_power\",\"params\":[\"on\"]}");}
	static void spegni() {out.println("{\"id\":0,\"method\":\"set_power\",\"params\":[\"off\"]}");}
	static void timer(int minuti) {out.println("{\"id\":0,\"method\":\"cron_add\",\"params\":[0, " + minuti + "]}");}
	static void setBr(int v) {out.println("{\"id\":0,\"method\":\"set_bright\",\"params\":[" + v + "]}");};
	static void temperatura(int K) {out.println("{\"id\":0,\"method\":\"set_ct_abx\",\"params\":[" + K + ",\"smooth\",500]}");};
	static void setRGB(int r, int g, int b) {out.println("{\"id\":0,\"method\":\"set_rgb\",\"params\":[" + (r*65536+g*256+b) + "]}");};
	static void setHS(int hue, int sat) {out.println("{\"id\":0,\"method\":\"set_hsv\",\"params\":[" + hue + "," + sat + ",\"smooth\",500]}");}
	static void animazione(int [][] valori) {
		//Serie di passi tempo, modalità, valore, luminosità
		if (valori == null) return;
		StringBuffer codiceAnimazione = new StringBuffer("");
		for (int [] sequenza : valori) {
			codiceAnimazione.append(sequenza[0]); codiceAnimazione.append(',');
			codiceAnimazione.append(sequenza[1]); codiceAnimazione.append(',');
			codiceAnimazione.append(sequenza[2]); codiceAnimazione.append(',');
			codiceAnimazione.append(sequenza[3]); codiceAnimazione.append(',');
		}
		if (codiceAnimazione.toString() != "") codiceAnimazione = new StringBuffer(codiceAnimazione.substring(0, codiceAnimazione.length()-1));
		out.println("{\"id\":0,\"method\":\"start_cf\",\"params\":[0, 1, \"" + codiceAnimazione.toString() + "\"]}");
	}
	
	static void chiudi() {
		try {
			out.close();
			telnet.close();
		} catch (Exception e) {}
		System.exit(0);
	}
	
	static void creaBarraDelTitolo() {
		JLabel titolo = new JLabel("miaYeelight");
		JButton disconnetti = new JButton("❌");
		JLabel icona = new JLabel();
		rosso.setLayout(null);
        rosso.setBackground(new Color(160,0,0));
        titolo.setBounds(0, 0, 530, 40);
        titolo.setHorizontalAlignment(SwingConstants.CENTER); titolo.setVerticalAlignment(SwingConstants.CENTER);
        rosso.add(titolo);
        titolo.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent click) {
				if (click.getX() > 490) disconnetti.doClick();}
			public void mouseEntered(MouseEvent click) {
				if (click.getX() > 490) disconnetti.setBackground(new Color(90,0,0));}
			public void mouseExited(MouseEvent click) {
				disconnetti.setBackground(new Color(120,0,0));}
			public void mousePressed(MouseEvent click) {
				if (click.getX() > 490) disconnetti.setBackground(new Color(60,0,0));}
			public void mouseReleased(MouseEvent click) {
				if (click.getX() > 490) disconnetti.setBackground(new Color(120,0,0));}
        });
        titolo.addMouseMotionListener(new MouseMotionListener() {
        	public void mouseDragged(MouseEvent d) {
        		if (d.getX() < 490) frame.setBounds(d.getXOnScreen()-frame.getWidth()/2, d.getYOnScreen()-25, frame.getWidth(), frame.getHeight());
			}
			public void mouseMoved(MouseEvent d) {
				if (d.getX() > 490) disconnetti.setBackground(new Color(90,0,0)); else disconnetti.setBackground(new Color(120,0,0));}
		});
        
        disconnetti.setBounds(490, 0, 40, 40);
        disconnetti.addActionListener(click -> Main.chiudi());
        disconnetti.setFocusable(false);
        disconnetti.setBackground(new Color(120,0,0));
        rosso.add(disconnetti);
        icona.setIcon(yee);
        icona.setBounds(0,0,40,40);
        rosso.add(icona);
        rosso.setPreferredSize(new Dimension(530,40));
	}
	
	static void tienitiAggiornataSuWindows() {
		if (aggiornatoreWinAccent == null) {
			pannello.seguiWinAccent.setText("Non seguire più");
			cambiaColoreDaAccent();
			aggiornatoreWinAccent = new Timer();
			aggiornatoreWinAccent.schedule(new TimerTask() {
				public void run() {
					if (accentColor.getRGB() != SystemColor.activeCaption.getRGB()) cambiaColoreDaAccent();
				}
			}, 0, 2000);
		} else {
			aggiornatoreWinAccent.cancel();
			aggiornatoreWinAccent = null;
			pannello.seguiWinAccent.setText("Segui il colore di Windows 10");
		}
	}
	
	static void cambiaColoreDaAccent() {
		accentColor = new Color(SystemColor.activeCaption.getRed(), SystemColor.activeCaption.getGreen(), SystemColor.activeCaption.getBlue());
    	float[] hsbVals = Color.RGBtoHSB(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), new float[3]);
    	pannello.hue.setValue((int)(hsbVals[0]*360));
    	pannello.sat.setValue((int)(hsbVals[1]*70+30));
    	pannello.luminosita.setValue((int)(hsbVals[2]*100));
    	setHS((int)(hsbVals[0]*360), (int)(hsbVals[1]*70+30));
    	try {Thread.sleep(100);} catch (InterruptedException e) {}
    	setBr((int)(hsbVals[2]*100));
	}
	
	static void apriPannelloAnimazioni() {
		if (pannelloAnimazioni == null) pannelloAnimazioni = new PannelloAnimazioni();
		frame.getContentPane().removeAll();
		frame.getContentPane().add(rosso, BorderLayout.NORTH);
		frame.getContentPane().add(pannelloAnimazioni, BorderLayout.CENTER);
		frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloAnimazioni.getHeight());
		frame.revalidate();
	}
	
	static void tornaStatico() {
		if (pannello == null) {
			pannello = new PannelloPrincipale();
			pannello.setBounds(0, 0, 530, pannello.getHeight());
		}
		frame.getContentPane().removeAll();
		frame.getContentPane().add(rosso, BorderLayout.NORTH);
		frame.getContentPane().add(pannello, BorderLayout.CENTER);
		frame.setSize(rosso.getWidth(), rosso.getHeight() + pannello.getHeight());
		frame.revalidate();
	}
	
	static ImageIcon caricaIcona() {
		try {
			return new ImageIcon(ImageIO.read(Main.class.getResource("yee.png")).getScaledInstance(40,40,Image.SCALE_SMOOTH));
		} catch (Exception e) {return null;}
	}
}

class PannelloConnessione extends JPanel {
	private static final long serialVersionUID = 1L;
	JLabel desc = new JLabel("");
	
	PannelloConnessione () {
		super();
		setLayout(null);
		JLabel 	intestazione = new JLabel("Connessione alla lampadina");
		JButton connetti = new JButton("Connetti");
		JTextField ip = new JTextField("");
		intestazione.setBounds(10, 0, 510, 40);
		add(intestazione);
		desc.setBounds(10, 50, 510, 40);
		desc.setFont(Main.f2);
		add(desc);
		ip.setText("192.168.1.100");
		ip.setBounds(10, 100, 350, 40);
		add(ip);
		connetti.setBounds(370, 100, 140, 40);
		connetti.setFocusable(false);
		connetti.addActionListener(click -> {
			Main.connettiA(ip.getText());
		});
		add(connetti);
		setBounds(0,0,530,150);
		setVisible(true);
	}
}