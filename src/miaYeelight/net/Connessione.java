package miaYeelight.net;

import miaYeelight.Main;
import miaYeelight.lang.Strings;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Connessione implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String IP_DEFAULT_B_012 = "192.168.1.";
    public static final String IP_DEFAULD_B_3 = "100";

    private transient Socket telnet = null;
    private transient PrintStream out = null;
    private transient Scanner in = null;
    private String ipVarGlobale;
    private boolean stop = false;
    private boolean finitoIlCiclo = true;
    private boolean noCiclo = true;

    private final Main ref;

    public Connessione(Main ref) {
        this.ref = ref;
    }

    public boolean connetti(boolean tentativoIP100) throws IOException {
        DatagramSocket discovery = new DatagramSocket(17000);
        try {
            byte[] payload = "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1982\r\nMAN: \"ssdp:discover\"\r\nST: wifi_bulb".getBytes(StandardCharsets.US_ASCII);
            DatagramPacket request = new DatagramPacket(payload, payload.length, new InetSocketAddress("239.255.255.250", 1982)); // 1982 dal protocollo Yeelight
            discovery.send(request);
            DatagramPacket reply = new DatagramPacket(new byte[512], 512);
            discovery.setSoTimeout(1500);
            discovery.receive(reply);
            String ip = new String(reply.getData(), StandardCharsets.UTF_8);
            discovery.disconnect();
            discovery.close();
            System.out.print(ip);
            ip = ip.substring(ip.indexOf("yeelight://") + 11);
            ip = ip.substring(0, ip.indexOf(':'));
            (telnet = new Socket()).connect(new InetSocketAddress(ip, 55443), 1000); // 5543 dal protocollo Yeelight
            out = new PrintStream(telnet.getOutputStream(), true);
            in = new Scanner(telnet.getInputStream());
            if (telnet.isConnected()) {
                return true;
            }
        } catch (ConnectException | SocketTimeoutException e) {
            e.printStackTrace();
            discovery.close();
        }
        System.out.println(Strings.get("Connessione.5"));

        finitoIlCiclo = false;
        noCiclo = false;
        try { //Tentativo per IP locale 100
            if (tentativoIP100) {
                ref.getPannelloConnessione().setTestoDescrizione(Strings.get("Connessione.10") + IP_DEFAULT_B_012 + IP_DEFAULD_B_3);
                (telnet = new Socket()).connect(new InetSocketAddress(IP_DEFAULT_B_012 + IP_DEFAULD_B_3, 55443), 200);
                System.out.println(Strings.get("Connessione.7"));
                //telnet = new Socket("192.168.1.100", 55443);	//Abbandonata questa modalità perché non permette di impostare un timeout a priori
                out = new PrintStream(telnet.getOutputStream(), true);
                in = new Scanner(telnet.getInputStream());
            } else {
                throw new ConnectException();
            }

        } catch (ConnectException | SocketTimeoutException e) {
            if (tentativoIP100) {
                System.out.println(Strings.get("Connessione.7"));
            }
            ref.getFrame().setVisible(true);
            finitoIlCiclo = false;
            noCiclo = false;
            for (int i = 2; i < 255; i++) {
                try {
                    if (stop) {
                        stop = false;
                        i--;
                        ref.getPannelloConnessione().setTestoDescrizione(Strings.get("Connessione.9") + ipVarGlobale);
                        (telnet = new Socket()).connect(new InetSocketAddress(ipVarGlobale, 55443), 2000);
                        out = new PrintStream(telnet.getOutputStream(), true);
                        in = new Scanner(telnet.getInputStream());
                        break;
                    }
                    ref.getPannelloConnessione().setTestoDescrizione(Strings.get("Connessione.10") + IP_DEFAULT_B_012 + i);
                    (telnet = new Socket()).connect(new InetSocketAddress(IP_DEFAULT_B_012 + i, 55443), 150);
                    out = new PrintStream(telnet.getOutputStream(), true);
                    in = new Scanner(telnet.getInputStream());
                    break;
                } catch (ConnectException | SocketTimeoutException ignored) {
                }
            }
        }
        finitoIlCiclo = true;

        return telnet.isConnected();
    }

    public boolean connettiA(String ip) {
        if (finitoIlCiclo || noCiclo) {
            ref.getPannelloConnessione().setTestoDescrizione(Strings.get("Connessione.9") + ip);
            try {
                (telnet = new Socket()).connect(new InetSocketAddress(ip, 55443));
                out = new PrintStream(telnet.getOutputStream(), true);
                in = new Scanner(telnet.getInputStream());
            } catch (IOException ignored) {
            }
            if (telnet.isConnected()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, Strings.get("Connessione.12"), Strings.get("Connessione.13"), JOptionPane.ERROR_MESSAGE, ref.yee);
                return false;
            }
        } else {
            stop = true;
            ipVarGlobale = ip;
            return false;
        }
    }

    public String[] scaricaProprieta() {
        send("{\"id\":1,\"method\":\"get_prop\",\"params\":[\"power\", \"bright\", \"color_mode\", \"hue\", \"sat\", \"ct\", \"name\"]}");
        String rs;
        do {
            rs = in.nextLine();
        } while (rs.charAt(6) != '1');
        rs = rs.substring(19);
        rs = rs.replace(",\"", "");
        rs = rs.replace("]}", "");
        String[] proprieta = rs.split("\"");
        if (proprieta.length < 7) {
            cambiaNome(Strings.get("AppName"));
            proprieta = new String[]{proprieta[0], proprieta[1], proprieta[2], proprieta[3], proprieta[4], proprieta[5], Strings.get("AppName")};
        }
        return proprieta;
    }

    public void cambiaNome(String nome) {
        send("{\"id\":1,\"method\":\"set_name\",\"params\":[\"" + nome + "\"]}");
    }

    public void accendi() {
        send("{\"id\":0,\"method\":\"set_power\",\"params\":[\"on\"]}");
    }

    public void spegni() {
        send("{\"id\":0,\"method\":\"set_power\",\"params\":[\"off\"]}");
    }

    public void timer(int minuti) {
        send("{\"id\":0,\"method\":\"cron_add\",\"params\":[0," + minuti + "]}");
    }

    public void setBr(int v) {
        send("{\"id\":0,\"method\":\"set_bright\",\"params\":[" + v + "]}");
    }

    public void temperatura(int K) {
        send("{\"id\":0,\"method\":\"set_ct_abx\",\"params\":[" + K + ",\"smooth\",500]}");
    }

    public void setRGB(int r, int g, int b) {
        send("{\"id\":0,\"method\":\"set_rgb\",\"params\":[" + (r * 65536 + g * 256 + b) + "]}");
    }

    public void setHS(int hue, int sat) {
        send("{\"id\":0,\"method\":\"set_hsv\",\"params\":[" + hue + "," + sat + ",\"smooth\",500]}");
    }

    public void animazione(int[][] valori) {
        //Serie di passi tempo, modalità, valore, luminosità
        if (valori == null) {
            return;
        }
        StringBuilder codiceAnimazione = new StringBuilder();
        for (int[] sequenza : valori) {
            codiceAnimazione.append(sequenza[0]);
            codiceAnimazione.append(',');
            codiceAnimazione.append(sequenza[1]);
            codiceAnimazione.append(',');
            codiceAnimazione.append(sequenza[2]);
            codiceAnimazione.append(',');
            codiceAnimazione.append(sequenza[3]);
            codiceAnimazione.append(',');
        }
        if (!codiceAnimazione.toString().isEmpty()) {
            codiceAnimazione = new StringBuilder(codiceAnimazione.substring(0, codiceAnimazione.length() - 1));
        }
        send("{\"id\":0,\"method\":\"start_cf\",\"params\":[0, 1, \"" + codiceAnimazione.toString() + "\"]}");
    }

    private void send(String t) {
        try {
            out.println(t);
        } catch (Exception e) {
            System.out.print(Strings.get("Connessione.41"));
            System.out.println(t);
        }
    }

    public void chiudi() {
        try {
            in.close();
            out.close();
            telnet.close();
            telnet = null;
        } catch (Exception ignored) {
        }
    }

}