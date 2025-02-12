package miayeelight.net;

import miayeelight.Main;
import miayeelight.lang.Strings;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static miayeelight.Main.log;

public class Connessione {

    public static final String IP_DEFAULT_B_012 = "192.168.1.";
    public static final String IP_DEFAULT_B_3 = "100";

    private Socket telnet = null;
    private PrintStream out = null;
    private BufferedReader in = null;

    private Deque<IndirizzoConnessione> codaTentativi;
    private String ultimoIndirizzoConnesso = null;

    private final Main ref;
    private static Connessione istanza;

    private record IndirizzoConnessione(String ip, int tempo) {
    }

    private Connessione(Main ref) {
        this.ref = ref;
    }

    public static void inizializza(Main ref) {
        if (istanza == null) {
            istanza = new Connessione(ref);
        }
    }

    public static Connessione istanza() {
        return istanza;
    }

    public boolean connetti(boolean ciclo) throws IOException {
        if (trovaConMulticast()) {
            return true;
        }
        return ciclo && trovaConRicercaEsaustiva();
    }

    private boolean trovaConMulticast() throws IOException {
        try (final DatagramSocket udpSocket = new DatagramSocket(17000)) {
            final byte[] payload = "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1982\r\nMAN: \"ssdp:discover\"\r\nST: wifi_bulb".getBytes(StandardCharsets.US_ASCII);
            final DatagramPacket richiesta = new DatagramPacket(payload, payload.length, new InetSocketAddress("239.255.255.250", 1982)); // 1982 dal protocollo Yeelight
            udpSocket.send(richiesta);
            final DatagramPacket risposta = new DatagramPacket(new byte[512], 512);
            udpSocket.setSoTimeout(1500);
            udpSocket.receive(risposta);

            String ip = new String(risposta.getData(), StandardCharsets.UTF_8);
            udpSocket.disconnect();

            ip = ip.substring(ip.indexOf("yeelight://") + 11);
            ip = ip.substring(0, ip.indexOf(':'));

            return tentaConnessione(new IndirizzoConnessione(ip, 1000));
        } catch (ConnectException | SocketTimeoutException e) {
            log(Strings.get(Connessione.class, "5"));
            return false;
        }
    }

    private boolean trovaConRicercaEsaustiva() throws IOException {
        ref.getFrame().setVisible(true);
        codaTentativi = new LinkedList<>();
        IntStream.range(2, 255).mapToObj(n -> new IndirizzoConnessione(IP_DEFAULT_B_012 + n, 500)).forEach(codaTentativi::add);

        while (!codaTentativi.isEmpty()) {
            try {
                IndirizzoConnessione tentativo = codaTentativi.pop();

                ref.getPannelloConnessione().setTestoDescrizione(Strings.get(Connessione.class, "1") + tentativo.ip);

                if (tentaConnessione(tentativo)) {
                    codaTentativi = null;
                    return true;
                }
            } catch (ConnectException | SocketTimeoutException ignored) {
                chiudi();
            }
        }

        codaTentativi = null;
        return telnet != null && telnet.isConnected();
    }

    public boolean connettiA(final String ip, final String messaggio) {
        if (codaTentativi == null || codaTentativi.isEmpty()) {
            ref.getPannelloConnessione().setTestoDescrizione(Strings.get(Connessione.class, "8") + ip);
            try {
                if (tentaConnessione(new IndirizzoConnessione(ip, 2000))) {
                    return true;
                }
            } catch (IOException e) {
                chiudi();
            }
            ref.getPannelloConnessione().setTestoDescrizione("");
            JOptionPane.showMessageDialog(null, messaggio, Strings.get(Connessione.class, "3"), JOptionPane.ERROR_MESSAGE, ref.yee);
        } else {
            codaTentativi.addFirst(new IndirizzoConnessione(ip, 2000));
        }
        return false;
    }

    private boolean tentaConnessione(final IndirizzoConnessione i) throws IOException {
        chiudi();
        telnet = new Socket();
        telnet.connect(new InetSocketAddress(i.ip, 55443), i.tempo);
        telnet.setSoTimeout(500);
        out = new PrintStream(telnet.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(telnet.getInputStream(), StandardCharsets.UTF_8));
        ultimoIndirizzoConnesso = i.ip;
        return telnet.isConnected();
    }

    public String[] scaricaProprieta() {
        String[] proprieta = null;

        String rs = invia("{\"id\":1,\"method\":\"get_prop\",\"params\":[\"power\", \"bright\", \"color_mode\", \"hue\", \"sat\", \"ct\", \"name\"]}");
        while (rs == null || !rs.startsWith("{\"id\":1,")) {
            if (telnet == null || in == null || out == null) {
                return proprieta;
            }
            rs = ricevi();
        }
        rs = rs.substring(19);
        rs = rs.replace(",\"", "");
        rs = rs.replace("]}", "");
        proprieta = rs.split("\"");
        if (proprieta.length < 7) {
            cambiaNome(Strings.get("AppName"));
            proprieta = new String[]{proprieta[0], proprieta[1], proprieta[2], proprieta[3], proprieta[4], proprieta[5], Strings.get("AppName")};
        }
        return proprieta;
    }

    public void cambiaNome(String nome) {
        invia("{\"id\":1,\"method\":\"set_name\",\"params\":[\"" + nome + "\"]}");
    }

    public void accendi() {
        invia("{\"id\":0,\"method\":\"set_power\",\"params\":[\"on\"]}");
    }

    public void spegni() {
        invia("{\"id\":0,\"method\":\"set_power\",\"params\":[\"off\"]}");
    }

    public void timer(int minuti) {
        invia("{\"id\":0,\"method\":\"cron_add\",\"params\":[0," + minuti + "]}");
    }

    public void setBr(int v) {
        invia("{\"id\":0,\"method\":\"set_bright\",\"params\":[" + v + "]}");
    }

    public void temperatura(int kelvin) {
        invia("{\"id\":0,\"method\":\"set_ct_abx\",\"params\":[" + kelvin + ",\"smooth\",500]}");
    }

    @SuppressWarnings("unused")
    public void setRGB(int r, int g, int b) {
        invia("{\"id\":0,\"method\":\"set_rgb\",\"params\":[" + (r * 65536 + g * 256 + b) + "]}");
    }

    public void setHS(int hue, int sat) {
        invia("{\"id\":0,\"method\":\"set_hsv\",\"params\":[" + hue + "," + sat + ",\"smooth\",500]}");
    }

    public void animazione(int[][] valori) {
        //Serie di passi tempo, modalità, valore, luminosità
        if (valori != null) {
            final String codiceAnimazione = Arrays.stream(valori).map(seq -> "%d,%d,%d,%d".formatted(seq[0], seq[1], seq[2], seq[3])).collect(Collectors.joining(","));
            invia("{\"id\":0,\"method\":\"start_cf\",\"params\":[0, 1, \"" + codiceAnimazione + "\"]}");
        }
    }

    private String invia(String t) {
        try {
            if (out == null && impossibileRistabilireConnessione()) {
                ultimoIndirizzoConnesso = null;
                ref.tornaModoRicerca();
                return null;
            }

            out.println(t);
            final String risposta = ricevi();

            if (risposta != null && risposta.contains("client quota exceeded")) {
                chiudi();

                if (impossibileRistabilireConnessione()) {
                    ultimoIndirizzoConnesso = null;
                    ref.tornaModoRicerca();
                    return null;
                }
            }
            return risposta;
        } catch (Exception e) {
            log(e);
            log(Strings.get(Connessione.class, "4") + t);
            return null;
        }
    }

    private String ricevi() {
        try {
            final char[] buffer = new char[512];
            final int length = in.read(buffer);
            if (length > 0) {
                return new String(buffer, 0, length);
            }
        } catch (Exception e) {
            try {
                chiudi();

                if (impossibileRistabilireConnessione()) {
                    ultimoIndirizzoConnesso = null;
                    ref.tornaModoRicerca();
                    return null;
                }
            } catch (IOException ex) {
                log(ex);
            }
        }
        return null;
    }

    private boolean impossibileRistabilireConnessione() throws IOException {
        return !connettiA(ultimoIndirizzoConnesso, Strings.get(Connessione.class, "6")) && !connetti(false);
    }

    public void chiudi() {
        try {
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (telnet != null) {
                telnet.close();
                telnet = null;
            }
        } catch (Exception e) {
            log(e);
        }
    }

}