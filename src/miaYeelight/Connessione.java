package miaYeelight;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Connessione {
	static final String IPDefaultB012 = "192.168.1.",
		IPDefauldB3 = "100";
	private Socket telnet = null;
	private PrintStream out = null;
	private Scanner in = null;
	private String ipVarGlobale;
    private boolean stop = false,
    		finitoIlCiclo = true,
    		noCiclo = true;
    
    Main ref;
    
    Connessione (Main ref) {
    	this.ref = ref;
    }
    
    boolean connetti(boolean tentativoIP100) throws IOException {
    	finitoIlCiclo = false; noCiclo = false;
		try { //Tentativo per IP locale 100 
			if (tentativoIP100) {
				ref.pannelloConnessione.desc.setText("In connessione a " + IPDefaultB012 + IPDefauldB3);
				(telnet = new Socket()).connect(new InetSocketAddress(IPDefaultB012 + IPDefauldB3, 55443),1000);
				//telnet = new Socket("192.168.1.100", 55443);	//Abbandonata questa modalità perché non permette di impostare un timeout a priori
	            out = new PrintStream(telnet.getOutputStream(), true);
	            in = new Scanner(telnet.getInputStream());
			}
			else throw new ConnectException();
        } catch (ConnectException | SocketTimeoutException e) {
        	ref.frame.setVisible(true);
        	finitoIlCiclo = false;
        	noCiclo = false;
			for (int i = 2; i<255; i++) {
				try {
					if (stop) {
						stop = false;
						i--;
						ref.pannelloConnessione.desc.setText("Su richiesta, mi connetto a " + ipVarGlobale);
						(telnet = new Socket()).connect(new InetSocketAddress(ipVarGlobale, 55443),2000);
			            out = new PrintStream(telnet.getOutputStream(), true);
			            in = new Scanner(telnet.getInputStream());
			            break;
					}
					ref.pannelloConnessione.desc.setText("In connessione a " + IPDefaultB012 + i);
		        	(telnet = new Socket()).connect(new InetSocketAddress(IPDefaultB012 + i, 55443), 200);
		            out = new PrintStream(telnet.getOutputStream(), true);
		            in = new Scanner(telnet.getInputStream());
		            break;
		        } 
		        catch (ConnectException | SocketTimeoutException ex) {continue;}
			}
        }
		finitoIlCiclo = true;
		if (telnet.isConnected()) return true; else return false;
	}
	
	boolean connettiA(String ip) {
		if (finitoIlCiclo || noCiclo) {
			ref.pannelloConnessione.desc.setText("Su richiesta, mi connetto a " + ip);
			try {
				(telnet = new Socket()).connect(new InetSocketAddress(ip, 55443));
	            out = new PrintStream(telnet.getOutputStream(), true);
	            in = new Scanner(telnet.getInputStream());
	        } catch (IOException e) {}
			if (telnet.isConnected()) return true;
			else {
				JOptionPane.showMessageDialog(null, "Impossibile connettersi a questo indirizzo", "Lampadina non trovata", JOptionPane.ERROR_MESSAGE, ref.yee);
				return false;
			}
		} else {
			stop = true;
			ipVarGlobale = ip;
			return false;
		}
	}
	
	String[] scaricaProprieta() {
		out.println("{\"id\":1,\"method\":\"get_prop\",\"params\":[\"power\", \"bright\", \"color_mode\", \"hue\", \"sat\", \"ct\", \"name\"]}");
		String rs;
		do {
			rs = in.nextLine();
			System.out.println(rs);
		} while (rs.charAt(6) != '1');
		rs = rs.substring(19);
		rs = rs.replace(",\"", "");
		rs = rs.replace("]}", "");
		String[] proprieta = rs.split("\"");
		if (proprieta.length<7) {
			cambiaNome("miaYeelight");
			proprieta = new String[] {proprieta[0], proprieta[1], proprieta[2], proprieta[3], proprieta[4], proprieta[5], "miaYeelight"};
		}
		return proprieta;
	}
	
	
	void cambiaNome(String nome) {out.println("{\"id\":1,\"method\":\"set_name\",\"params\":[\"" + nome + "\"]}");}
	void accendi() {out.println("{\"id\":0,\"method\":\"set_power\",\"params\":[\"on\"]}");}
	void spegni() {out.println("{\"id\":0,\"method\":\"set_power\",\"params\":[\"off\"]}");}
	void timer(int minuti) {out.println("{\"id\":0,\"method\":\"cron_add\",\"params\":[0, " + minuti + "]}");}
	void setBr(int v) {out.println("{\"id\":0,\"method\":\"set_bright\",\"params\":[" + v + "]}");};
	void temperatura(int K) {out.println("{\"id\":0,\"method\":\"set_ct_abx\",\"params\":[" + K + ",\"smooth\",500]}");};
	void setRGB(int r, int g, int b) {out.println("{\"id\":0,\"method\":\"set_rgb\",\"params\":[" + (r*65536+g*256+b) + "]}");};
	void setHS(int hue, int sat) {out.println("{\"id\":0,\"method\":\"set_hsv\",\"params\":[" + hue + "," + sat + ",\"smooth\",500]}");}
	void animazione(int [][] valori) {
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
	
	void chiudi() {
		try {
			in.close();
			out.close();
			telnet.close();
		} catch (Exception e) {}
	}
}