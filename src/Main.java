import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.io.*;
import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class Main extends JFrame {
	private static final long serialVersionUID = 0L;
	static JFrame frame;
	static JPanel rosso = new JPanel();
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
    
	public static void main(String[] args) throws IOException {
		connetti();
		if (!telnet.isConnected()) {
			UIManager.put("OptionPane.messageFont", f2);
			UIManager.put("OptionPane.buttonFont", f2);
			UIManager.put("Button.background", Color.white);
			JOptionPane.showMessageDialog(null, "<HTML><h2>Impossibile connettersi alla lampadina</h2><br>" +
					"Possibili cause:" +
					"<ul><li>la lampadina non è connessa alla corrente o al wifi</li>" + 
					"<li>la lampadina non è configurata in modalità sviluppatore</li>" + 
					"<li>è fallito il timeout di 200ms, in tal caso riprovare</li>" + 
					"</ul></HTML>", "Errore di connessione", JOptionPane.ERROR_MESSAGE);
			return;
		}
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
        UIManager.put("OptionPane.messageFont", f2);
		UIManager.put("OptionPane.buttonFont", f2);
		UIManager.put("OptionPane.background", Color.black);
		UIManager.put("Panel.background", Color.black);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		
		frame=new JFrame();
		frame.setUndecorated(true);
		frame.setTitle("miaYeelight");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(new ImageIcon(Main.class.getResource("yee.png")).getImage());
		frame.setBackground(new Color(0,0,0,0));
		creaBarraDelTitolo();
		rosso.setBounds(0, 0, 530, 40);
        frame.getContentPane().add(rosso,BorderLayout.NORTH);
		pannello = new PannelloPrincipale();
		pannello.setBounds(0, 0, 530, pannello.getHeight());
        frame.getContentPane().add(pannello,BorderLayout.CENTER);
        frame.revalidate();
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    	int screenW = (int)(screenSize.getWidth());
    	int screenH = (int)(screenSize.getHeight());
        frame.setBounds(screenW/2-265, screenH/2-pannello.getHeight()/2, 530, rosso.getHeight() + pannello.getHeight());
        frame.setVisible(true);
	}
	
	static void connetti() throws IOException {
		try { //Tentativo per IP locale 100 
			(telnet = new Socket()).connect(new InetSocketAddress("192.168.1.100", 55443));
            //telnet = new Socket("192.168.1.100", 55443);	//Abbandonata questa modalità perché non permette di impostare un timeout a priori
            out = new PrintStream(telnet.getOutputStream(), true);
        } catch (ConnectException | SocketTimeoutException e) {
			for (int i = 2; i<255; i++) {
		        try {
		        	System.out.println("192.168.1." + i);
		        	(telnet = new Socket()).connect(new InetSocketAddress("192.168.1." + i, 55443), 200);
		            out = new PrintStream(telnet.getOutputStream(), true);
		            break;
		        } 
		        catch (ConnectException | SocketTimeoutException ex) {continue;}
			}
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
		frame.getContentPane().removeAll();
		frame.getContentPane().add(rosso, BorderLayout.NORTH);
		frame.getContentPane().add(pannello, BorderLayout.CENTER);
		frame.setSize(rosso.getWidth(), rosso.getHeight() + pannello.getHeight());
		frame.revalidate();
	}
}

final class PannelloPrincipale extends JPanel {
	private static final long serialVersionUID = 1L;
	JButton seguiWinAccent;
    JSlider luminosita = new JSlider(),
    		hue = new JSlider(),
    		sat = new JSlider();
	
	PannelloPrincipale(){
		setLayout(null);
        setBackground(Main.bg);
        JLabel 	descLuce = new JLabel("Imposta il valore di luminosità:"),
        		descTemp = new JLabel("Imposta la temperatura in Kelvin:"),
        		descCol = new JLabel("Imposta il colore (tonalità - saturazione):");
        JButton accendi = new JButton("💡  Accendi"),
        		spegni = new JButton("🕯  Spegni"),
        		timer = new JButton("⏱  Timer"),
        		impostaBr = new JButton("Imposta"),
        		impostaC = new JButton("Imposta"),
        		impostaTemp = new JButton("Imposta"),
        		winAccent = new JButton("Applica il colore di Windows 10"),
        		animazioni = new JButton("Usa un'animazione  🎆");
        seguiWinAccent = new JButton("Segui il colore di Windows 10");
        /*JTextField r = new JTextField(),
        		g = new JTextField(),
        		b = new JTextField();*/
        JSlider temperatura = new JSlider();
        
        ChangeListener 	lambdaColore = s -> {
				        	impostaC.setBackground(new Color(Color.HSBtoRGB(((float)hue.getValue())/359, ((float)sat.getValue())/100, ((float)(luminosita.getValue()>80?100:luminosita.getValue()+20))/100)));
				        	impostaC.setForeground(new Color(Color.HSBtoRGB(((float)((hue.getValue()+180) % 359))/359, ((float)sat.getValue())/100, ((float)((luminosita.getValue()+50) % 100)/100))));
        				},
        				lambdaTemperatura = s -> {
        					impostaTemp.setBackground(new Color((int)(200*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100)),
        														(int)((temperatura.getValue()/37+75)*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100)),
        														(int)((temperatura.getValue()-1700)/18.8f*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100))));
        														/*(int)((luminosita.getValue()/2+50)*2.3),
        														(int)((luminosita.getValue()/2+50)*2.25) + temperatura.getValue()/260*luminosita.getValue()/100,
        														(int)(((luminosita.getValue()/2+50)*2.55)*((float)(temperatura.getValue()-1700)/4800))));*/
        					impostaTemp.setForeground(luminosita.getValue() > 45? Color.BLACK : Color.WHITE);
        				}
        ;
        
        int Y = 10;
        accendi.setBounds(10, Y, 163, 40); timer.setBounds(183, Y, 163, 40); spegni.setBounds(357, Y, 163, 40);
        accendi.addActionListener(click -> Main.accendi());
        spegni.addActionListener(click -> Main.spegni());
        timer.addActionListener(click -> {
        	Main.timer(Integer.parseInt(JOptionPane.showInputDialog(Main.frame, "Imposta il tempo (in minuti) tra cui spegnere la lampadina", "Timer di autospegnimento", JOptionPane.QUESTION_MESSAGE)));
        });
        accendi.setFocusable(false); timer.setFocusable(false); spegni.setFocusable(false);
        add(accendi); add(timer); add(spegni);
        Y += 50;
        descLuce.setBounds(10, Y, 510, 40);
        descLuce.setOpaque(false);
        add(descLuce);
        Y += 50;
        luminosita.setBounds(10, Y, 350, 40); impostaBr.setBounds(370, Y, 150, 40);
        luminosita.setBackground(Main.trasparente); luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100); luminosita.setMinimum(1);
        luminosita.addChangeListener(s -> descLuce.setText("Imposta il valore di luminosità: " + luminosita.getValue() + "%"));
        luminosita.addChangeListener(lambdaColore);
        luminosita.addChangeListener(lambdaTemperatura);
        luminosita.setOpaque(false);
        impostaBr.addActionListener(click -> Main.setBr(luminosita.getValue()));
        impostaBr.setFocusable(false);
        add(impostaBr); add(luminosita);
        Y += 50;
        descTemp.setBounds(10, Y, 510, 40);
        add(descTemp);
        Y += 50;
        temperatura.setBounds(10, Y, 350, 40); impostaTemp.setBounds(370, Y, 150, 40);
        temperatura.setBackground(Main.trasparente); temperatura.setForeground(Color.WHITE);
        temperatura.setOpaque(false);
        temperatura.setMaximum(6500); temperatura.setMinimum(1700);
        temperatura.setValue(5000);
        temperatura.addChangeListener(s -> descTemp.setText("Imposta la temperatura in Kelvin: " + temperatura.getValue() + "K"));
        temperatura.addChangeListener(lambdaTemperatura);
        impostaTemp.addActionListener(click -> Main.temperatura(temperatura.getValue()));
        impostaTemp.setFocusable(false);
        add(impostaTemp); add(temperatura);
        Y += 50;
        descCol.setBounds(10, Y, 510, 40);
        add(descCol);
        Y += 50;
        //r.setBounds(10, Y, 110, 40); g.setBounds(130, Y, 110, 40); b.setBounds(250, Y, 110, 40);
        //r.setHorizontalAlignment(SwingConstants.CENTER); g.setHorizontalAlignment(SwingConstants.CENTER); b.setHorizontalAlignment(SwingConstants.CENTER);
        hue.setBounds(10, Y, 170, 40); sat.setBounds(190, Y, 170, 40);
        hue.setBackground(Main.trasparente); hue.setForeground(Color.WHITE); sat.setBackground(Main.trasparente); sat.setForeground(Color.WHITE);
        hue.setMaximum(359); hue.setMinimum(0);
        sat.setMaximum(100); sat.setMinimum(0);
        hue.setOpaque(false); sat.setOpaque(false);
        impostaC.setBounds(370, Y, 150, 40);
        impostaC.addActionListener(click -> Main.setHS(hue.getValue(), sat.getValue()));
        //impostaC.addActionListener(click -> setRGB(Integer.parseInt(r.getText()),Integer.parseInt(g.getText()),Integer.parseInt(b.getText())));
        impostaC.setFocusable(false);
        add(hue); add(sat); add(impostaC);
        //pannello.add(r); pannello.add(g); pannello.add(b); pannello.add(impostaC);
        hue.addChangeListener(lambdaColore);
        sat.addChangeListener(lambdaColore);
        Y += 50;
        winAccent.setBounds(10, Y, 250, 40);
        seguiWinAccent.setBounds(270, Y, 250, 40);
        winAccent.setFocusable(false); seguiWinAccent.setFocusable(false);
        winAccent.addActionListener(click -> Main.cambiaColoreDaAccent());
        seguiWinAccent.addActionListener(click -> Main.tienitiAggiornataSuWindows());
        add(winAccent); add(seguiWinAccent);
        Y += 50;
        animazioni.setBounds(10,Y,510,40);
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> Main.apriPannelloAnimazioni());
        add(animazioni);
        Y += 50;
        setSize(530, Y);
        setVisible(true);
	}
}

final class PannelloAnimazioni extends JPanel {
	private static final long serialVersionUID = 1L;
	private ArrayList<JTextField> tempi = new ArrayList<>();
	private ArrayList<JSlider[]> colori = new ArrayList<>();
	private ArrayList<JButton> aggiuntori = new ArrayList<>();
	private ArrayList<JButton> rimovitori = new ArrayList<>();
	private JLabel[] intestazione = {new JLabel("Durata"), new JLabel("Luce"), new JLabel("Tonalità"), new JLabel("Saturazione")};
	private JButton caricaAnimazione = new JButton("📦  Carica animazione"), salvaAnimazione = new JButton("💾  Salva questa animazione"),
		avviaAnimazione = new JButton("▶  Avvia animazione"), torna = new JButton ("⬅  Modalità statica");
	private AnteprimaAnimazione anteprima;
	int Y = 10;
	DocumentListener ListenerCambioTempi = new DocumentListener() {
		public void changedUpdate(DocumentEvent a) {aggiorna(a);}
		public void insertUpdate(DocumentEvent a) {aggiorna(a);}
		public void removeUpdate(DocumentEvent a) {aggiorna(a);}
		private void aggiorna(DocumentEvent a) {
			try {
				int val = Integer.parseInt(a.getDocument().getText(0, a.getDocument().getLength()));
				if (val<50) JOptionPane.showMessageDialog(Main.frame, "La lampadina non è in grado di riprodurre colori in meno di 50ms", "Errore di compilazione", JOptionPane.WARNING_MESSAGE);
				aggiornaAnteprima();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(Main.frame, "Inserire un numero intero", "Errore di compilazione", JOptionPane.WARNING_MESSAGE);
			} catch (BadLocationException e) {}
		}
	};
	
	PannelloAnimazioni () {
		super();
		setLayout(null);
		setBackground(Main.bg);
		caricaAnimazione.setFocusable(false); salvaAnimazione.setFocusable(false);
		caricaAnimazione.setBounds(10, Y, 250, 40); 
		caricaAnimazione.addActionListener(click -> {
			FileDialog picker = new java.awt.FileDialog((java.awt.Frame) null);
			picker.setTitle("Scegli l'animazione da caricare");
			picker.setMode(FileDialog.LOAD);
			picker.setVisible(true);
			try {
				DataInputStream f = new DataInputStream(new FileInputStream(picker.getDirectory() + picker.getFile()));
				try {
					for (JTextField jt : tempi) remove(jt);
					for (JSlider[] jl : colori) {remove(jl[0]);remove(jl[1]);remove(jl[2]);}
					for (JButton jb : aggiuntori) remove(jb);
					for (JButton jb : rimovitori) remove(jb);
					tempi = new ArrayList<>();
					colori = new ArrayList<>();
					aggiuntori = new ArrayList<>();
					rimovitori = new ArrayList<>();
					int i = 0;
					while (f.available()>=16) {
						tempi.add(new JTextField());
						tempi.get(i).setText("" + f.readInt());
						colori.add(new JSlider[] {new JSlider(), new JSlider(), new JSlider()});
						colori.get(i)[0].setMinimum(0); colori.get(i)[0].setMaximum(100);
						colori.get(i)[1].setMinimum(0); colori.get(i)[1].setMaximum(359);
						colori.get(i)[2].setMinimum(0); colori.get(i)[2].setMaximum(100);
						colori.get(i)[0].setValue(f.readInt()); colori.get(i)[1].setValue(f.readInt()); colori.get(i)[2].setValue(f.readInt());
						tempi.get(i).getDocument().addDocumentListener(ListenerCambioTempi);
						colori.get(i)[0].setOpaque(false); colori.get(i)[1].setOpaque(false); colori.get(i)[1].setOpaque(false);
						colori.get(i)[0].addChangeListener(sl -> aggiornaAnteprima());
						colori.get(i)[1].addChangeListener(sl -> aggiornaAnteprima());
						colori.get(i)[2].addChangeListener(sl -> aggiornaAnteprima());
						aggiuntori.add(new JButton("➕"));
						rimovitori.add(new JButton("➖"));
						add(tempi.get(i));
						add(colori.get(i)[0]); add(colori.get(i)[1]); add(colori.get(i)[2]);
						add(aggiuntori.get(i)); add(rimovitori.get(i));
						System.out.println("Letta riga " + i);
						i++;
					}
				} catch (IOException e) {}
				f.close();
				ridisegna();
				aggiornaAnteprima();
			} catch (IOException e) {e.printStackTrace();}
		});
		add(caricaAnimazione);
		salvaAnimazione.setBounds(270, Y, 250, 40);
		salvaAnimazione.addActionListener(click -> {
			FileDialog picker = new java.awt.FileDialog((java.awt.Frame) null);
			picker.setTitle("Scegli dove salvare");
			picker.setMode(FileDialog.SAVE);
			picker.setFile("animazione.yan");
			picker.setVisible(true);
			try {DataOutputStream f = new DataOutputStream(new FileOutputStream(picker.getDirectory() + picker.getFile()));
				for (int i=0; i<tempi.size(); i++)	{
					f.writeInt(Integer.parseInt(tempi.get(i).getText()));
					f.writeInt(colori.get(i)[0].getValue());
					f.writeInt(colori.get(i)[1].getValue());
					f.writeInt(colori.get(i)[2].getValue());
				}
				f.flush();
				f.close();
			} catch (IOException e) {e.printStackTrace();}
		});
		add(salvaAnimazione);
		Y += 50;
		intestazione[0].setBounds(10, Y, 80, 40); intestazione[0].setHorizontalAlignment(SwingConstants.CENTER);
		intestazione[1].setBounds(100, Y, 100, 40); intestazione[1].setHorizontalAlignment(SwingConstants.CENTER);
		intestazione[2].setBounds(210, Y, 100, 40); intestazione[2].setHorizontalAlignment(SwingConstants.CENTER);
		intestazione[3].setBounds(320, Y, 100, 40); intestazione[3].setHorizontalAlignment(SwingConstants.CENTER);
		add(intestazione[0]); add(intestazione[1]); add(intestazione[2]); add(intestazione[3]);
		Y += 50;
		tempi.add(new JTextField());
		tempi.get(0).setText("1000");
		colori.add(new JSlider[] {new JSlider(), new JSlider(), new JSlider()});
		aggiuntori.add(new JButton("➕"));
		rimovitori.add(new JButton("➖"));
		add(tempi.get(0));
		add(colori.get(0)[0]); add(colori.get(0)[1]); add(colori.get(0)[2]);
		add(aggiuntori.get(0)); add(rimovitori.get(0));
		tempi.get(0).getDocument().addDocumentListener(ListenerCambioTempi);
		colori.get(0)[0].addChangeListener(sl -> aggiornaAnteprima());
		colori.get(0)[1].addChangeListener(sl -> aggiornaAnteprima());
		colori.get(0)[2].addChangeListener(sl -> aggiornaAnteprima());
		disegna();
		anteprima = new AnteprimaAnimazione(ottieniValori());
		anteprima.setBounds(10, Y, 510, 40);
		add(anteprima);
		Y += 50;
		avviaAnimazione.setBounds(10, Y, 250, 40);
		avviaAnimazione.addActionListener(click -> {
			int[][] valori = new int[tempi.size()][4];
			for (int i=0; i<valori.length; i++) {
				valori[i][0] = Integer.parseInt(tempi.get(i).getText());
				//Spegni la lampada se la luminosità scelta è zero
				valori[i][1] = 1;
				Color coloreScelto = new Color(Color.HSBtoRGB(
						((float)colori.get(i)[1].getValue())/359,
						((float)colori.get(i)[2].getValue())/100,
						((float)colori.get(i)[0].getValue())/100));
				valori[i][2] = coloreScelto.getRed()*65536 + coloreScelto.getGreen()*256 + coloreScelto.getBlue();
				valori[i][3] = colori.get(i)[0].getValue();
			}
			Main.animazione(valori);
		});
		torna.setBounds(270, Y, 250, 40);
		torna.setFocusable(false); avviaAnimazione.setFocusable(false);
		torna.addActionListener(click -> Main.tornaStatico());
		Y += 50;
		add(avviaAnimazione); add(torna);
		setSize(new Dimension(530,Y));
		setPreferredSize(new Dimension(530,Y));
		setVisible(true);
	}
	
	void disegna() {
		for (int i=0; i<tempi.size(); i++) {
			tempi.get(i).setBounds(10, Y, 80, 40);
			tempi.get(i).setHorizontalAlignment(SwingConstants.CENTER);
			colori.get(i)[0].setBounds(100, Y, 100, 40);
			colori.get(i)[0].setMinimum(0); colori.get(i)[0].setMaximum(100);
			colori.get(i)[1].setBounds(210, Y, 100, 40);
			colori.get(i)[1].setMinimum(0); colori.get(i)[1].setMaximum(359);
			colori.get(i)[2].setBounds(320, Y, 100, 40);
			colori.get(i)[2].setMinimum(0); colori.get(i)[2].setMaximum(100);
			colori.get(i)[0].setBackground(Main.trasparente); colori.get(i)[1].setBackground(Main.trasparente); colori.get(i)[2].setBackground(Main.trasparente);
			aggiuntori.get(i).setBounds(430, Y, 40, 40);
			rimovitori.get(i).setBounds(480, Y, 40, 40);
			//Rimuovi il listener associato precedentemente perché potrebbe contenere un indice ora errato
			if (aggiuntori.get(i).getActionListeners().length != 0)
				aggiuntori.get(i).removeActionListener(aggiuntori.get(i).getActionListeners()[0]);
			aggiuntori.get(i).addActionListener(new lambdaAnimazione(true, i));
			
			if (rimovitori.get(i).getActionListeners().length != 0)
				rimovitori.get(i).removeActionListener(rimovitori.get(i).getActionListeners()[0]);
			rimovitori.get(i).addActionListener(new lambdaAnimazione(false, i));
			
			aggiuntori.get(i).setFocusable(false);
			rimovitori.get(i).setFocusable(false);
			Y += 50;
		}
	}
	
	void ridisegna() {
		Y = 100;
		disegna();
		if (anteprima != null) {
			anteprima.setBounds(10, Y, 510, 40);
			Y += 50;
		}
		avviaAnimazione.setBounds(10, Y, 250, 40);
		torna.setBounds(270, Y, 250, 40);
		Y += 50;
		setSize(new Dimension(530,Y));
		setPreferredSize(new Dimension(530,Y));
		Main.frame.setSize(530, 40 + Y);
		Main.frame.revalidate();
	}
	
	void aggiornaAnteprima() {
		if (anteprima == null) return;
		int vecchiaY = anteprima.getY();
		remove(anteprima);
		anteprima = new AnteprimaAnimazione(ottieniValori());
		anteprima.setBounds(10, vecchiaY, 510, 40);
		add(anteprima);
		repaint();
	}
	
	int[][] ottieniValori(){
		int [][] valori = new int[tempi.size()][4];
		for (int i=0; i<tempi.size(); i++) {
			valori[i][0] = Integer.parseInt(tempi.get(i).getText());
			valori[i][1] = colori.get(i)[0].getValue();
			valori[i][2] = colori.get(i)[1].getValue();
			valori[i][3] = colori.get(i)[2].getValue();
		}
		return valori;
	}
	
	private class lambdaAnimazione implements ActionListener {
		boolean tipo;
		int riga;
		lambdaAnimazione(boolean tipologia, int riga) {
			tipo = tipologia; this.riga = riga;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			if (tipo) {
				tempi.add(riga, new JTextField());
				tempi.get(riga).setText("1000");
				colori.add(riga, new JSlider[] {new JSlider(), new JSlider(), new JSlider()});
				aggiuntori.add(riga, new JButton("➕"));
				rimovitori.add(riga, new JButton("➖"));
				add(tempi.get(riga));
				add(colori.get(riga)[0]); add(colori.get(riga)[1]); add(colori.get(riga)[2]);
				add(aggiuntori.get(riga)); add(rimovitori.get(riga));
				tempi.get(riga).getDocument().addDocumentListener(ListenerCambioTempi);
				colori.get(riga)[0].setOpaque(false); colori.get(riga)[1].setOpaque(false); colori.get(riga)[1].setOpaque(false);
				colori.get(riga)[0].addChangeListener(sl -> aggiornaAnteprima());
				colori.get(riga)[1].addChangeListener(sl -> aggiornaAnteprima());
				colori.get(riga)[2].addChangeListener(sl -> aggiornaAnteprima());
				ridisegna();
				aggiornaAnteprima();
			} else {
				if (tempi.size() == 1) return;
				remove(tempi.get(riga));
				remove(colori.get(riga)[0]); remove(colori.get(riga)[1]); remove(colori.get(riga)[2]);
				remove(aggiuntori.get(riga)); remove(rimovitori.get(riga));
				tempi.remove(riga);
				colori.remove(riga);
				aggiuntori.remove(riga);
				rimovitori.remove(riga);
				ridisegna();
				aggiornaAnteprima();
			}
		}
	}
	
	class AnteprimaAnimazione extends JPanel {
		private static final long serialVersionUID = 1L;
		private int [][] v;
		AnteprimaAnimazione(int [][] valori) {v = valori;}
		
		protected void paintComponent (Graphics g) {
			if (v == null || v.length == 0) return;
			int tempo = 0, X=0, xPost=0;
			for (int[] i : v) tempo += i[0];
			for (int s=0; s<v.length; s++) {
				xPost += (int)((float)(v[s][0])/tempo*510);
				float hue1=v[s][2], sat1=v[s][3], br1=v[s][1], hue2, sat2, br2;
				if (s==v.length-1) {hue2=v[0][2]; sat2=v[0][3]; br2=v[0][1];}
				else {hue2=v[s+1][2]; sat2=v[s+1][3]; br2=v[s+1][1];}
				//System.out.println("Segmento " + s + " - Hue1: " + hue1 + " Hue2: " + hue2);
				//System.out.println("Da: " + X + " A: " + xPost + " Tempo: " + tempo + " Durata: " + v[s][0]);
				//System.out.println(hue1 + " " + sat1 + " " + br1);
				for (int x=X; x<=xPost; x++) {
					float p = (float)(x-X)/(xPost-X);//percentuale gradiente attuale
					g.setColor(Color.getHSBColor(
							(hue2>hue1? 
								(hue2-hue1<180?
									(hue1*(1-p)+hue2*p):
									(hue1-(hue1+360-hue2)*p >0?
										hue1-(hue1+360-hue2)*p:
										360+hue1-(hue1+360-hue2)*p))
								:(hue1-hue2<180?
									(hue1*(1-p)+hue2*p):
									(hue1+(hue2+360-hue1)*p>359?
										hue1+(hue2+360-hue1)*p-360:
										hue1+(hue2+360-hue1)*p))
							)/360,
							(sat1*(1-p)+sat2*p)/100,
							(br1*(1-p)+br2*p)/100));
					//System.out.println("R" + g.getColor().getRed() + "G" + g.getColor().getGreen() + "B" + g.getColor().getBlue());
					g.drawLine(x,0,x,40);
				}
				X = xPost;
			}
		}
	}
}