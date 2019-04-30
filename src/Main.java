import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

public class Main extends JFrame {
	private static final long serialVersionUID = 0L;
	static JFrame frame;
	static Socket telnet = null;
    static PrintStream out = null;
    static final Font f = new Font("Arial", Font.PLAIN, 18);
    static final Font f2 = new Font("Arial", Font.PLAIN, 16);
    static final Color sh1 = new Color(40,40,40);
    
	public static void main(String[] args) throws IOException {
		try { //Tentativo per IP locale 100 
			(telnet = new Socket()).connect(new InetSocketAddress("192.168.1.100", 55443), 200);
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
		if (!telnet.isConnected()) {
			UIManager.put("OptionPane.messageFont", f2);
			UIManager.put("OptionPane.buttonFont", f2);
			JOptionPane.showMessageDialog(null, "Impossibile connettersi alla lampadina", "Errore di connessione", JOptionPane.ERROR_MESSAGE);
			return;
		}
		frame=new JFrame();
		frame.setUndecorated(true);
		frame.setTitle("miaYeelight");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(new ImageIcon(Main.class.getResource("yee.png")).getImage());
        JPanel pannello = new JPanel();
        pannello.setLayout(null);
        pannello.setBackground(Color.black);
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
        JPanel rosso = new JPanel();
        JLabel 	titolo = new JLabel("miaYeelight"),
        		descLuce = new JLabel("Imposta il valore di luminosità:"),
        		descTemp = new JLabel("Imposta la temperatura in Kelvin:"),
        		descCol = new JLabel("Imposta il colore (tonalità - saturazione):");
        JButton accendi = new JButton("Accendi"),
        		spegni = new JButton("Spegni"),
        		impostaBr = new JButton("Imposta"),
        		impostaC = new JButton("Imposta"),
        		impostaTemp = new JButton("Imposta"),
        		disconnetti = new JButton("Disconnetti");
        /*JTextField r = new JTextField(),
        		g = new JTextField(),
        		b = new JTextField();*/
        JSlider temperatura = new JSlider(),
        		luminosita = new JSlider(),
        		hue = new JSlider(),
        		sat = new JSlider();
        
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
        
        int Y = 0;
        rosso.setLayout(null);
        rosso.setBackground(new Color(160,0,0));
        rosso.setBounds(0, Y, 530, 40);
        pannello.add(rosso);
        titolo.setBounds(0, Y, 530, 40);
        titolo.setHorizontalAlignment(SwingConstants.CENTER); titolo.setVerticalAlignment(SwingConstants.CENTER);
        rosso.add(titolo);
        titolo.addMouseMotionListener(new MouseMotionListener() {
        	public void mouseDragged(MouseEvent d) {
				frame.setBounds(d.getXOnScreen()-frame.getWidth()/2, d.getYOnScreen()-25, frame.getWidth(), frame.getHeight());
			}
			public void mouseMoved(MouseEvent arg0) {}});
        Y += 50;
        accendi.setBounds(10, Y, 250, 40); spegni.setBounds(270, Y, 250, 40);
        accendi.addActionListener(click -> accendi());
        spegni.addActionListener(click -> spegni());
        accendi.setFocusable(false); spegni.setFocusable(false);
        pannello.add(accendi); pannello.add(spegni);
        Y += 50;
        descLuce.setBounds(10, Y, 510, 40);
        pannello.add(descLuce);
        Y += 50;
        luminosita.setBounds(10, Y, 350, 40); impostaBr.setBounds(370, Y, 150, 40);
        luminosita.setBackground(Color.BLACK); luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100); luminosita.setMinimum(1);
        luminosita.addChangeListener(s -> descLuce.setText("Imposta il valore di luminosità: " + luminosita.getValue() + "%"));
        luminosita.addChangeListener(lambdaColore);
        luminosita.addChangeListener(lambdaTemperatura);
        impostaBr.addActionListener(click -> setBr(luminosita.getValue()));
        impostaBr.setFocusable(false);
        pannello.add(impostaBr); pannello.add(luminosita);
        Y += 50;
        descTemp.setBounds(10, Y, 510, 40);
        pannello.add(descTemp);
        Y += 50;
        temperatura.setBounds(10, Y, 350, 40); impostaTemp.setBounds(370, Y, 150, 40);
        temperatura.setBackground(Color.BLACK); temperatura.setForeground(Color.WHITE);
        temperatura.setMaximum(6500); temperatura.setMinimum(1700);
        temperatura.addChangeListener(s -> descTemp.setText("Imposta la temperatura in Kelvin: " + temperatura.getValue() + "K"));
        temperatura.addChangeListener(lambdaTemperatura);
        impostaTemp.addActionListener(click -> temperatura(temperatura.getValue()));
        impostaTemp.setFocusable(false);
        pannello.add(impostaTemp); pannello.add(temperatura);
        Y += 50;
        descCol.setBounds(10, Y, 510, 40);
        pannello.add(descCol);
        Y += 50;
        //r.setBounds(10, Y, 110, 40); g.setBounds(130, Y, 110, 40); b.setBounds(250, Y, 110, 40);
        //r.setHorizontalAlignment(SwingConstants.CENTER); g.setHorizontalAlignment(SwingConstants.CENTER); b.setHorizontalAlignment(SwingConstants.CENTER);
        hue.setBounds(10, Y, 170, 40); sat.setBounds(190, Y, 170, 40);
        hue.setBackground(Color.BLACK); hue.setForeground(Color.WHITE); sat.setBackground(Color.BLACK); sat.setForeground(Color.WHITE);
        hue.setMaximum(359); hue.setMinimum(0);
        sat.setMaximum(100); sat.setMinimum(0);
        impostaC.setBounds(370, Y, 150, 40);
        impostaC.addActionListener(click -> setHS(hue.getValue(), sat.getValue()));
        //impostaC.addActionListener(click -> setRGB(Integer.parseInt(r.getText()),Integer.parseInt(g.getText()),Integer.parseInt(b.getText())));
        impostaC.setFocusable(false);
        pannello.add(hue); pannello.add(sat); pannello.add(impostaC);
        //pannello.add(r); pannello.add(g); pannello.add(b); pannello.add(impostaC);
        hue.addChangeListener(lambdaColore);
        sat.addChangeListener(lambdaColore);
        Y += 50;
        disconnetti.setBounds(10, Y, 510, 40);
        disconnetti.addActionListener(click -> chiudi());
        disconnetti.setFocusable(false);
        pannello.add(disconnetti);
        Y += 50;
        pannello.setBounds(0, 0, 530, Y);
        pannello.setVisible(true);
        frame.getContentPane().add(pannello, BorderLayout.CENTER);
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    	int screenW = (int)(screenSize.getWidth());
    	int screenH = (int)(screenSize.getHeight());
        frame.setBounds(screenW/2-265, screenH/2-Y/2, 530, Y);
        frame.setVisible(true);
        //r.grabFocus();
	}
	
	static void accendi() {out.println("{\"id\":0,\"method\":\"set_power\",\"params\":[\"on\"]}");}
	static void spegni() {out.println("{\"id\":0,\"method\":\"set_power\",\"params\":[\"off\"]}");}
	static void setBr(int v) {out.println("{\"id\":0,\"method\":\"set_bright\",\"params\":[" + v + "]}");};
	static void temperatura(int K) {out.println("{\"id\":0,\"method\":\"set_ct_abx\",\"params\":[" + K + ",\"smooth\",500]}");};
	static void setRGB(int r, int g, int b) {out.println("{\"id\":0,\"method\":\"set_rgb\",\"params\":[" + (r*65536+g*256+b) + "]}");};
	static void setHS(int hue, int sat) {out.println("{\"id\":0,\"method\":\"set_hsv\",\"params\":[" + hue + "," + sat + ",\"smooth\",500]}");}
	
	static void chiudi() {
		try {
			out.close();
			telnet.close();
		} catch (Exception e) {}
		System.exit(0);
	}
}
