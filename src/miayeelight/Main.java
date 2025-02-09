package miayeelight;

import miayeelight.lang.Strings;
import miayeelight.net.Connessione;
import miayeelight.ux.componenti.BarraTitolo;
import miayeelight.ux.pannelli.PannelloAnimazioni;
import miayeelight.ux.pannelli.PannelloConnessione;
import miayeelight.ux.pannelli.PannelloImpostazioni;
import miayeelight.ux.pannelli.PannelloPrincipale;
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

    private static final Color sh1 = new Color(40, 40, 40, 255);
    public static final Color bg = new Color(0, 0, 0, 255);
    public static final Color trasparente = new Color(0, 0, 0, 0);
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

    private Connessione connessione = null;

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
        frame.setUndecorated(true);
        frame.setTitle(nomeLampadina);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        if (yee != null) {
            frame.setIconImage(yee.getImage());
        }
        frame.setBackground(new Color(0, 0, 0, 0));
        rosso = new BarraTitolo(this, yee);
        rosso.setBounds(0, 0, d(530), d(40));
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        pannelloConnessione = new PannelloConnessione(this, true);
        frame.getContentPane().add(pannelloConnessione, BorderLayout.CENTER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - d(265), screenSize.height / 2 - (pannelloConnessione.getHeight() + rosso.getHeight()), d(530), rosso.getHeight() + pannelloConnessione.getHeight());

        connessione = new Connessione(this);
        if (connessione.connetti(true)) {
            final String[] proprieta = connessione.scaricaProprieta();
            tornaStatico();
            configuraPannelloPrincipaleConStatoLampada(proprieta);
            schedulaExtListener();
            frame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "%s%s%s%s%s</ul></HTML>".formatted(Strings.get(Main.class, "4"), Strings.get(Main.class, "5"), Strings.get(Main.class, "6"), Strings.get(Main.class, "7"), Strings.get(Main.class, "8")), Strings.get(Main.class, "10"), JOptionPane.ERROR_MESSAGE, yee);
        }
    }

    public void configuraPannelloPrincipaleConStatoLampada(final String[] statoIniziale) {
        if (statoIniziale == null) {
            return;
        }
        if (statoIniziale[0].equals("code")) {
            return;
        }
        pannello.setModoDiretto(false);
        if (statoIniziale[0].equals("on")) {
            pannello.getAccendi().setText(Strings.get(PannelloPrincipale.class, "12"));
        }
        pannello.getLuminosita().setValue(Integer.parseInt(statoIniziale[1]));
        pannello.getTemperatura().setValue(Integer.parseInt(statoIniziale[5]));
        pannello.getHue().setValue(Integer.parseInt(statoIniziale[3]));
        pannello.getSat().setValue(Integer.parseInt(statoIniziale[4]));
        nomeLampadina = statoIniziale[6];
        rosso.setTitolo(nomeLampadina);
        pannello.setUltimaModalita(!statoIniziale[2].equals("2"));
        pannello.aggiornaAnteprima();
        pannello.setModoDiretto(true);
    }

    public void tornaModalitaRicerca() {
        terminaAggiornatoreWinAccent();
        extList.cancel();
        extList = new Timer();
        connessione.chiudi();
        connessione = new Connessione(this);
        nomeLampadina = Strings.get("AppName");
        pannelloConnessione = new PannelloConnessione(this, false);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        frame.getContentPane().add(pannelloConnessione, BorderLayout.CENTER);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloConnessione.getHeight());
    }

    public void apriPannelloAnimazioni() {
        terminaAggiornatoreWinAccent();
        terminaAggiornatoreColoreSchermo();
        if (pannelloAnimazioni == null) {
            pannelloAnimazioni = new PannelloAnimazioni(this);
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        frame.getContentPane().add(pannelloAnimazioni, BorderLayout.CENTER);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloAnimazioni.getHeight());
    }

    public void apriPannelloImpostazioni() {
        PannelloImpostazioni pannelloImpostazioni;
        pannelloImpostazioni = new PannelloImpostazioni(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        frame.getContentPane().add(pannelloImpostazioni, BorderLayout.CENTER);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannelloImpostazioni.getHeight());
        frame.revalidate();
    }

    public void tornaStatico() {
        if (pannello == null) {
            pannello = new PannelloPrincipale(this);
            pannello.setBounds(0, 0, pannello.getWidth(), pannello.getHeight());
        } else {
            pannello.riscriviEtichette();
            pannello.mostraWin10Accent(Configurazione.getMostraWin10());
            if (pannelloAnimazioni != null) {
                pannelloAnimazioni.riscriviEtichette();
            }
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        frame.getContentPane().add(pannello, BorderLayout.CENTER);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannello.getHeight());
        frame.repaint();
    }

    public void schedulaExtListener() {
        extList.schedule(new TimerTask() {
            public void run() {
                if (Arrays.asList(frame.getContentPane().getComponents()).contains(pannello) && (pannello.getCrovescia() == null || !pannello.getCrovescia().isRunning()) && aggiornatoreSchermo == null) {
                    boolean modoPrima = pannello.getModoDiretto();
                    pannello.setModoDiretto(false);
                    configuraPannelloPrincipaleConStatoLampada(connessione.scaricaProprieta());
                    pannello.setModoDiretto(modoPrima);
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
                    if (accentColor.getRGB() != SystemColor.activeCaption.getRGB()) {
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
        boolean modoPrima = pannello.getModoDiretto();
        pannello.setModoDiretto(false);
        pannello.getHue().setValue((int) (hsbVals[0] * 360));
        pannello.getSat().setValue((int) (hsbVals[1] * 70 + 30));
        pannello.getLuminosita().setValue((int) (hsbVals[2] * 100));
        pannello.setModoDiretto(modoPrima);
        connessione.setHS((int) (hsbVals[0] * 360), (int) (hsbVals[1] * 70 + 30));
        connessione.setBr((int) (hsbVals[2] * 100));
    }

    public void seguiColoreSchermo() {
        if (aggiornatoreWinAccent == null && aggiornatoreSchermo == null) {
            pannello.abilitaControlli(false);
            pannello.getSeguiSchermo().setText(Strings.get(Main.class, "19"));
            aggiornatoreSchermo = new Timer();
            aggiornatoreSchermo.schedule(new TimerColoreSchermo(Configurazione.getAlgoritmo(), pannello, connessione), 0, Configurazione.getIntervalloTimer());
        } else {
            terminaAggiornatoreColoreSchermo();
        }
    }

    public void terminaAggiornatoreColoreSchermo() {
        if (aggiornatoreSchermo != null) {
            pannello.abilitaControlli(true);
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
    }

    public JFrame getFrame() {
        return frame;
    }

    public PannelloConnessione getPannelloConnessione() {
        return pannelloConnessione;
    }

    public Connessione getConnessione() {
        return connessione;
    }

    public String getNomeLampadina() {
        return nomeLampadina;
    }

    public void setNomeLampadina(String nomeLampadina) {
        this.nomeLampadina = nomeLampadina;
    }

}
