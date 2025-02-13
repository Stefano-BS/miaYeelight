package miayeelight.net;

import miayeelight.Configurazione;
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
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static miayeelight.Main.log;

public class Connessione {

    public static final String IP_DEFAULT_B_012 = "192.168.1.";
    public static final String IP_DEFAULT_B_3 = "100";

    public static final int TIMEOUT_UDP = 1500;
    public static final int TIMEOUT_TCP = 500;

    private ExecutorService esecutore;
    private final ConcurrentHashMap<CategoriaEseguibile, Future<?>> codaEsecuzione = new ConcurrentHashMap<>();

    private enum CategoriaEseguibile {
        LETTURA_STATO, SCRITTURA_NOME, SCRITTURA_ACCENSIONE, SCRITTURA_TIMER, SCRITTURA_STATO
    }

    public record StatoLampada(String accensione, String nome, //
                               Integer luma, Integer temperatura, Integer hue, Integer saturazione, Integer modo) {
    }

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
            final DatagramPacket richiesta = new DatagramPacket(payload, payload.length, new InetSocketAddress("239.255.255.250", 1982));
            udpSocket.send(richiesta);
            final DatagramPacket risposta = new DatagramPacket(new byte[512], 512);
            udpSocket.setSoTimeout(TIMEOUT_UDP);
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
        IntStream.range(2, 255).mapToObj(n -> new IndirizzoConnessione(IP_DEFAULT_B_012 + n, TIMEOUT_TCP)).forEach(codaTentativi::add);

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
            if (messaggio != null) {
                JOptionPane.showMessageDialog(null, messaggio, Strings.get(Connessione.class, "3"), JOptionPane.ERROR_MESSAGE, ref.yee);
            }
        } else {
            codaTentativi.addFirst(new IndirizzoConnessione(ip, 2000));
        }
        return false;
    }

    private boolean tentaConnessione(final IndirizzoConnessione i) throws IOException {
        chiudi();

        esecutore = Executors.newSingleThreadExecutor();

        telnet = new Socket();
        telnet.connect(new InetSocketAddress(i.ip, 55443), i.tempo);
        telnet.setSoTimeout(TIMEOUT_TCP);
        out = new PrintStream(telnet.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(telnet.getInputStream(), StandardCharsets.UTF_8));
        ultimoIndirizzoConnesso = i.ip;
        return telnet.isConnected();
    }

    public StatoLampada ottieniStatoAttuale() {
        return leggiStato().map(rs -> {
            final int inizio = rs.indexOf("[") + 1;
            final int fine = rs.indexOf("]");

            if (inizio > 0 && fine > 0) {
                final String[] prop = rs.substring(inizio, fine).replace("\"", "").split(",");

                if (prop.length == 7) {
                    return new StatoLampada(prop[0], prop[6], Integer.parseInt(prop[1]), Integer.parseInt(prop[5]), Integer.parseInt(prop[3]), Integer.parseInt(prop[4]), Integer.parseInt(prop[2]));
                }
            }

            return null;
        }).orElse(null);
    }

    @SuppressWarnings("java:S2142")
    private Optional<String> leggiStato() {
        try {
            final String comando = "{\"id\":%d,\"method\":\"get_prop\",\"params\":[\"power\", \"bright\", \"color_mode\", \"hue\", \"sat\", \"ct\", \"name\"]}".formatted(CategoriaEseguibile.LETTURA_STATO.ordinal());
            String rs = pianifica(CategoriaEseguibile.LETTURA_STATO, () -> invia(comando, true)).get(500, TimeUnit.MILLISECONDS);

            while (rs == null || !rs.startsWith("{\"id\":%d,".formatted(CategoriaEseguibile.LETTURA_STATO.ordinal()))) {
                if (telnet == null || in == null || out == null) {
                    return Optional.empty();
                }
                rs = pianifica(CategoriaEseguibile.LETTURA_STATO, () -> ricevi(true)).get(500, TimeUnit.MILLISECONDS);
            }
            return Optional.of(rs);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log(e);
            return Optional.empty();
        }
    }

    public void cambiaNome(String nome) {
        final String comando = "{\"id\":%d,\"method\":\"set_name\",\"params\":[\"%s\"]}".formatted(CategoriaEseguibile.SCRITTURA_NOME.ordinal(), nome);
        pianifica(CategoriaEseguibile.SCRITTURA_NOME, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void accendi() {
        final String comando = "{\"id\":%d,\"method\":\"set_power\",\"params\":[\"on\"]}".formatted(CategoriaEseguibile.SCRITTURA_ACCENSIONE.ordinal());
        pianifica(CategoriaEseguibile.SCRITTURA_ACCENSIONE, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void spegni() {
        final String comando = "{\"id\":%d,\"method\":\"set_power\",\"params\":[\"off\"]}".formatted(CategoriaEseguibile.SCRITTURA_ACCENSIONE.ordinal());
        pianifica(CategoriaEseguibile.SCRITTURA_ACCENSIONE, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void timer(int minuti) {
        final String comando = "{\"id\":%d,\"method\":\"cron_add\",\"params\":[0,%d]}".formatted(CategoriaEseguibile.SCRITTURA_TIMER.ordinal(), minuti);
        pianifica(CategoriaEseguibile.SCRITTURA_TIMER, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void setBr(int v) {
        final String comando = "{\"id\":%d,\"method\":\"set_bright\",\"params\":[%d]}".formatted(CategoriaEseguibile.SCRITTURA_STATO.ordinal(), v);
        pianifica(CategoriaEseguibile.SCRITTURA_STATO, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void temperatura(int kelvin) {
        final String comando = "{\"id\":%d,\"method\":\"set_ct_abx\",\"params\":[%d,\"smooth\",500]}".formatted(CategoriaEseguibile.SCRITTURA_STATO.ordinal(), kelvin);
        pianifica(CategoriaEseguibile.SCRITTURA_STATO, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    @SuppressWarnings("unused")
    public void setRGB(int r, int g, int b) {
        final String comando = "{\"id\":%d,\"method\":\"set_rgb\",\"params\":[%d]}".formatted(CategoriaEseguibile.SCRITTURA_STATO.ordinal(), r * 65536 + g * 256 + b);
        pianifica(CategoriaEseguibile.SCRITTURA_STATO, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void setHS(int hue, int sat) {
        final String comando = "{\"id\":%d,\"method\":\"set_hsv\",\"params\":[%d,%d,\"smooth\",500]}".formatted(CategoriaEseguibile.SCRITTURA_STATO.ordinal(), hue, sat);
        pianifica(CategoriaEseguibile.SCRITTURA_STATO, () -> invia(comando, Configurazione.isResetAggressivo()));
    }

    public void animazione(int[][] valori) {
        if (valori != null) {
            final String codiceAnimazione = Arrays.stream(valori).map(seq -> "%d,%d,%d,%d".formatted(seq[0], seq[1], seq[2], seq[3])).collect(Collectors.joining(","));
            final String comando = "{\"id\":%d,\"method\":\"start_cf\",\"params\":[0, 1, \"%s\"]}".formatted(CategoriaEseguibile.SCRITTURA_STATO.ordinal(), codiceAnimazione);
            pianifica(CategoriaEseguibile.SCRITTURA_STATO, () -> invia(comando, Configurazione.isResetAggressivo()));
        }
    }

    private <T> Future<T> pianifica(final CategoriaEseguibile categoria, final Callable<T> eseguibile) {
        if (esecutore == null || esecutore.isShutdown() || esecutore.isTerminated()) {
            final CompletableFuture<T> futureVuoto = new CompletableFuture<>();
            futureVuoto.complete(null);
            return futureVuoto;
        }

        final Future<T> future = esecutore.submit(eseguibile);
        final Future<?> pianificazioneAttuale = codaEsecuzione.put(categoria, future);

        if (pianificazioneAttuale != null && !pianificazioneAttuale.isDone() && !pianificazioneAttuale.isCancelled()) {
            pianificazioneAttuale.cancel(false);
        }

        return future;
    }

    private String invia(final String t, boolean rispostaNecessaria) {
        try {
            if (out == null && tentaRiconnessione()) {
                return null;
            }

            out.println(t);
            return ricevi(rispostaNecessaria);
        } catch (Exception e) {
            log(e);
            log(Strings.get(Connessione.class, "4") + t);
            return null;
        }
    }

    private String ricevi(boolean rispostaNecessaria) {
        try {
            final char[] buffer = new char[512];
            final int length = in.read(buffer);

            final String risposta = length > 0 ? new String(buffer, 0, length) : null;
            if (risposta != null && risposta.contains("client quota exceeded")) {
                chiudi();

                if (tentaRiconnessione()) {
                    return null;
                }
            }
            return risposta;
        } catch (Exception e) {
            try {
                if (rispostaNecessaria) {
                    chiudi();

                    tentaRiconnessione();
                }
            } catch (IOException ex) {
                log(ex);
            }
        }
        return null;
    }

    private boolean tentaRiconnessione() throws IOException {
        final boolean nonConnesso = !connettiA(ultimoIndirizzoConnesso, null) && !connetti(false);

        if (nonConnesso) {
            ultimoIndirizzoConnesso = null;
            ref.tornaModoRicerca();
            JOptionPane.showMessageDialog(null, Strings.get(Connessione.class, "6"), Strings.get(Connessione.class, "3"), JOptionPane.ERROR_MESSAGE, ref.yee);
        }

        return nonConnesso;
    }

    public void chiudi() {
        if (esecutore != null) {
            codaEsecuzione.clear();
            esecutore.shutdown();

            try {
                if (!esecutore.awaitTermination(1, TimeUnit.SECONDS)) {
                    esecutore.shutdownNow();
                }
            } catch (InterruptedException e) {
                esecutore.shutdownNow();
                Thread.currentThread().interrupt();
            }

            esecutore = null;
        }

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