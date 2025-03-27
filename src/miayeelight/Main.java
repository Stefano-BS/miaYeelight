package miayeelight;

import miayeelight.lang.Strings;
import miayeelight.net.Connessione;
import miayeelight.ux.componenti.BarraTitolo;
import miayeelight.ux.componenti.PulsanteRotondo;
import miayeelight.ux.pannelli.*;
import miayeelight.ux.schermo.TimerColoreSchermo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static miayeelight.Configurazione.LANG;
import static miayeelight.Configurazione.LOG;
import static miayeelight.ux.schermo.Schermo.d;

public class Main implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public final Font caratterePiccolo;
    public final Font carattereMedio;
    public final Font carattereGrande;

    private static final Color sh1 = new Color(40, 40, 40);
    public static final Color bg = new Color(0, 0, 0);
    public static final Color trasparente = new Color(0, 0, 0, 0);
    public static final Color semiTrasparente = new Color(0, 0, 0, 200);
    public static final Color testoDisattivo = new Color(200, 200, 200);
    public final ImageIcon yee;

    private Color accentColor;
    private transient Timer aggiornatoreWinAccent;
    private transient Timer aggiornatoreSchermo;
    private transient Timer extList = new Timer();

    private final JFrame frame;
    private final BarraTitolo rosso;
    private PannelloConnessione pannelloConnessione;
    private PannelloPrincipale pannello;
    private PannelloAnimazioni pannelloAnimazioni;
    private String nomeLampadina;

    public static void main(String[] args) throws IOException {
        new Main(args);
    }

    private Main(final String[] args) throws IOException {
        Configurazione.applicaConfigurazione(args);

        carattereGrande = new Font(Font.SANS_SERIF, Font.PLAIN, d(22));
        carattereMedio = new Font(Font.SANS_SERIF, Font.PLAIN, d(18));
        caratterePiccolo = new Font(Font.SANS_SERIF, Font.PLAIN, d(16));
        Strings.configMessages(Configurazione.get(LANG));

        yee = caricaIcona();
        configuraUIManager();

        nomeLampadina = Strings.get("AppName");
        frame = new JFrame();
        frame.setLayout(null);
        frame.setUndecorated(true);
        frame.setTitle(nomeLampadina);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setBackground(bg);
        frame.getContentPane().setBackground(bg);
        if (yee != null) {
            frame.setIconImage(yee.getImage());
        }
        frame.setBackground(new Color(0, 0, 0, 0));
        rosso = new BarraTitolo(this);
        rosso.setBounds(0, 0, d(530), d(40));
        frame.getContentPane().add(rosso);
        pannelloConnessione = new PannelloConnessione(this, true, Connessione.IP_DEFAULT_B_012 + Connessione.IP_DEFAULT_B_3);
        pannelloConnessione.setBounds(0, d(40), d(530), pannelloConnessione.getHeight());
        frame.getContentPane().add(pannelloConnessione);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - d(265), screenSize.height / 2 - (pannelloConnessione.getHeight() + rosso.getHeight()), d(530), rosso.getHeight() + pannelloConnessione.getHeight());

        Connessione.inizializza(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> Connessione.istanza().chiudi()));

        Connessione.istanza().connetti(true);
        if (Connessione.istanza().isConnesso()) {
            SwingUtilities.invokeLater(() -> {
                final Connessione.StatoLampada stato = Connessione.istanza().ottieniStatoAttuale();
                tornaStatico();
                configuraPannelloPrincipaleConStatoLampada(stato);
                schedulaExtListener();
                frame.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(null, "%s%s%s%s%s</ul></HTML>".formatted(Strings.get(Main.class, "4"), Strings.get(Main.class, "5"), Strings.get(Main.class, "6"), Strings.get(Main.class, "7"), Strings.get(Main.class, "8")), Strings.get(Main.class, "10"), JOptionPane.ERROR_MESSAGE, yee);
        }
    }

    public void configuraPannelloPrincipaleConStatoLampada(final Connessione.StatoLampada stato) {
        if (stato == null || "code".equals(stato.accensione())) {
            return;
        }

        nomeLampadina = stato.nome();
        rosso.setTitolo(nomeLampadina);

        final boolean lampadaAccesa = "on".equals(stato.accensione());
        pannello.getAccendi().setText(Strings.get(PannelloPrincipale.class, lampadaAccesa ? "12" : "0"));
        pannello.abilitaControlli(lampadaAccesa, lampadaAccesa);

        pannello.aggiornaValoriSlider(stato.luma(), stato.temperatura(), stato.hue(), stato.saturazione());
        pannello.aggiornaInterfaccia(stato.modo() != 2);
    }

    public void tornaModoRicerca(final String ipSuggerito) {
        Connessione.istanza().fermaTentativoRiconnessione();
        terminaAggiornatoreWinAccent();
        terminaAggiornatoreColoreSchermo();
        extList.cancel();
        extList = new Timer();
        Connessione.istanza().chiudi();
        nomeLampadina = Strings.get("AppName");
        pannelloConnessione = new PannelloConnessione(this, false, ipSuggerito);
        pannelloConnessione.setBounds(0, d(40), d(530), pannelloConnessione.getHeight());

        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso);
        frame.getContentPane().add(pannelloConnessione);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloConnessione.getHeight());
        frame.repaint();
    }

    public void riconnesso() {
        SwingUtilities.invokeLater(() ->
                Stream.of(frame.getContentPane().getComponents()).filter(PannelloConnessionePersa.class::isInstance).forEach(p -> {
                    frame.getContentPane().remove(p);
                    frame.revalidate();
                    frame.repaint();
                })
        );
    }

    public void mostraDisconnessione() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().add(new PannelloConnessionePersa(this), 0);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void apriPannelloAnimazioni() {
        terminaAggiornatoreWinAccent();
        terminaAggiornatoreColoreSchermo();
        if (pannelloAnimazioni == null) {
            pannelloAnimazioni = new PannelloAnimazioni(this);
            pannelloAnimazioni.setBounds(0, d(40), d(530), pannelloAnimazioni.getHeight());
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso);
        frame.getContentPane().add(pannelloAnimazioni);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloAnimazioni.getHeight());
    }

    public void apriPannelloImpostazioni() {
        final PannelloImpostazioni pannelloImpostazioni = new PannelloImpostazioni(this);
        pannelloImpostazioni.setBounds(0, d(40), d(530), pannelloImpostazioni.getHeight());
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso);
        frame.getContentPane().add(pannelloImpostazioni);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloImpostazioni.getHeight());
        frame.revalidate();
    }

    public void tornaStatico() {
        if (pannello == null) {
            pannello = new PannelloPrincipale(this);
            pannello.setBounds(0, d(40), d(530), pannello.getHeight());
        } else {
            pannello.riscriviEtichette(aggiornatoreWinAccent != null, aggiornatoreSchermo != null);
            pannello.mostraWin10Accent(Configurazione.getMostraWin10());
            if (pannelloAnimazioni != null) {
                pannelloAnimazioni.riscriviEtichette();
            }
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso);
        frame.getContentPane().add(pannello);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannello.getHeight());
        frame.repaint();
    }

    public void schedulaExtListener() {
        extList.schedule(new TimerTask() {
            public void run() {
                if (Connessione.istanza().isConnesso() && Arrays.asList(frame.getContentPane().getComponents()).contains(pannello) && (pannello.getCRovescia() == null || !pannello.getCRovescia().isRunning()) && aggiornatoreSchermo == null) {
                    SwingUtilities.invokeLater(() -> configuraPannelloPrincipaleConStatoLampada(Connessione.istanza().ottieniStatoAttuale()));
                }
            }
        }, 2500, 2697);
    }

    public void tienitiAggiornataSuWindows() {
        if (aggiornatoreWinAccent == null && aggiornatoreSchermo == null) {
            pannello.getSeguiSchermo().setEnabled(false);
            pannello.getSeguiWinAccent().setText(Strings.get(Main.class, "19"));
            cambiaColoreDaAccent();
            aggiornatoreWinAccent = new Timer();
            aggiornatoreWinAccent.schedule(new TimerTask() {

                public void run() {
                    if (accentColor.getRGB() != SystemColor.activeCaption.getRGB() && Connessione.istanza().isConnesso()) {
                        cambiaColoreDaAccent();
                    }
                }

            }, 0, 2000);
        } else {
            terminaAggiornatoreWinAccent();
        }
    }

    public void terminaAggiornatoreWinAccent() {
        if (aggiornatoreWinAccent != null) {
            pannello.getSeguiSchermo().setEnabled(true);
            aggiornatoreWinAccent.cancel();
            aggiornatoreWinAccent = null;
            pannello.getSeguiWinAccent().setText(Strings.get(PannelloPrincipale.class, "9"));
        }
    }

    public void cambiaColoreDaAccent() {
        accentColor = new Color(SystemColor.activeCaption.getRed(), SystemColor.activeCaption.getGreen(), SystemColor.activeCaption.getBlue());
        float[] hsbVals = Color.RGBtoHSB(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), new float[3]);
        pannello.aggiornaValoriSlider((int) (hsbVals[2] * 100), null, (int) (hsbVals[0] * 360), (int) (hsbVals[1] * 70 + 30));
        pannello.aggiornaInterfaccia(null);
        Connessione.istanza().setHS((int) (hsbVals[0] * 360), (int) (hsbVals[1] * 70 + 30));
        Connessione.istanza().setBr((int) (hsbVals[2] * 100));
    }

    public void seguiColoreSchermo() {
        if (aggiornatoreWinAccent == null && aggiornatoreSchermo == null) {
            pannello.abilitaControlli(false, null);
            pannello.getSeguiSchermo().setText(Strings.get(Main.class, "19"));
            aggiornatoreSchermo = new Timer();
            aggiornatoreSchermo.schedule(new TimerColoreSchermo(Configurazione.getAlgoritmo(), pannello, Connessione.istanza()), 0, Configurazione.getIntervalloTimer());
        } else {
            terminaAggiornatoreColoreSchermo();
        }
    }

    public void terminaAggiornatoreColoreSchermo() {
        if (aggiornatoreSchermo != null) {
            pannello.abilitaControlli(true, null);
            aggiornatoreSchermo.cancel();
            aggiornatoreSchermo = null;
            pannello.getSeguiSchermo().setText(Strings.get(PannelloPrincipale.class, "10"));
        }
    }

    private ImageIcon caricaIcona() {
        try {
            return new ImageIcon(ImageIO.read(Objects.requireNonNull(Main.class.getResource("yee.png"))).getScaledInstance(d(40), d(40), Image.SCALE_SMOOTH));
        } catch (Exception e) {
            log(e);
            return null;
        }
    }

    public static void log(final Exception e) {
        switch (Configurazione.get(LOG)) {
            case "console":
                LOGGER.log(Level.WARNING, () -> e.toString() + "\n" + Arrays.stream(e.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n")));
                break;
            case "messaggio":
                JOptionPane.showMessageDialog(null, e.toString() + "\n" + Arrays.stream(e.getStackTrace()).limit(15).map(Object::toString).collect(Collectors.joining("\n")), Strings.get(Main.class, "1"), JOptionPane.ERROR_MESSAGE);
                break;
            default:
                break;
        }
    }

    public static void log(final String log) {
        LOGGER.log(Level.INFO, log);
    }

    private void configuraUIManager() {
        UIManager.put("Button.font", caratterePiccolo);
        UIManager.put("Button.background", sh1);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", sh1.brighter());
        UIManager.put("Button.border", 0);
        UIManager.put("TextField.font", caratterePiccolo);
        UIManager.put("TextField.background", sh1.brighter());
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.border", 0);
        UIManager.put("ComboBox.font", caratterePiccolo);
        UIManager.put("ComboBox.background", sh1);
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", sh1.brighter());
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Label.font", carattereMedio);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Panel.background", Color.black);
        UIManager.put("OptionPane.messageFont", caratterePiccolo);
        UIManager.put("OptionPane.buttonFont", caratterePiccolo);
        UIManager.put("OptionPane.background", Color.black);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("List.foreground", Color.WHITE);
        UIManager.put("List.background", sh1);
        UIManager.put("List.selectionBackground", sh1.brighter());
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("List.font", caratterePiccolo);
        UIManager.put("ScrollBar.background", sh1.brighter().brighter());
        UIManager.put("ButtonUI", PulsanteRotondo.class.getName());
    }

    public JFrame getFrame() {
        return frame;
    }

    public PannelloConnessione getPannelloConnessione() {
        return pannelloConnessione;
    }

    public String getNomeLampadina() {
        return nomeLampadina;
    }

    public void setNomeLampadina(String nomeLampadina) {
        this.nomeLampadina = nomeLampadina;
    }

}
