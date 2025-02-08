package miayeelight;

import miayeelight.lang.Strings;
import miayeelight.net.Connessione;
import miayeelight.ux.pannelli.PannelloAnimazioni;
import miayeelight.ux.pannelli.PannelloConnessione;
import miayeelight.ux.pannelli.PannelloImpostazioni;
import miayeelight.ux.pannelli.PannelloPrincipale;
import miayeelight.ux.schermo.TimerColoreSchermo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static miayeelight.ux.schermo.Schermo.d;

public class Main implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public final Font f2;
    private static final Color sh1 = new Color(40, 40, 40, 255);
    public static final Color bg = new Color(0, 0, 0, 255);
    public static final Color trasparente = new Color(0, 0, 0, 0);
    public final ImageIcon yee;

    private Color accentColor;
    private transient Timer aggiornatoreWinAccent;
    private transient Timer aggiornatoreSchermo;
    private transient Timer extList = new Timer();
    private int lkpX = 0;
    private int lkpY = 0;

    private final JFrame frame;
    private final JPanel rosso = new JPanel();
    private PannelloConnessione pannelloConnessione;
    private JLabel titolo;
    private PannelloPrincipale pannello;
    private PannelloAnimazioni pannelloAnimazioni;
    private String nomeLampadina;

    private Connessione connessione = null;

    public static void main(String[] args) throws IOException {
        new Main(args);
    }

    private Main(final String[] args) throws IOException {
        Configurazione.applicaConfigurazione(args);
        Strings.configMessages(Configurazione.get("lang"));

        f2 = new Font("sans", Font.PLAIN, d(16));
        yee = caricaIcona();
        configuraUIManager();

        nomeLampadina = Strings.get("AppName");
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setTitle(nomeLampadina);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setIconImage(yee.getImage());
        frame.setBackground(new Color(0, 0, 0, 0));
        creaBarraDelTitolo();
        rosso.setBounds(0, 0, d(530), d(40));
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        pannelloConnessione = new PannelloConnessione(this, true);
        frame.getContentPane().add(pannelloConnessione, BorderLayout.CENTER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenW = (int) (screenSize.getWidth());
        int screenH = (int) (screenSize.getHeight());
        frame.setBounds(screenW / 2 - 265, screenH / 2 - pannelloConnessione.getHeight(), rosso.getWidth(), rosso.getHeight() + pannelloConnessione.getHeight());

        connessione = new Connessione(this);
        if (connessione.connetti()) {
            String[] proprieta = connessione.scaricaProprieta();
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
        titolo.setText(nomeLampadina);
        pannello.setUltimaModalita(!statoIniziale[2].equals("2"));
        pannello.aggiornaAnteprima();
        pannello.setModoDiretto(true);
    }

    private void tornaModalitaRicerca() {
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
    }

    public void tornaStatico() {
        if (pannello == null) {
            pannello = new PannelloPrincipale(this);
            pannello.setBounds(0, 0, pannello.getWidth(), pannello.getHeight());
        } else {
            pannello.riscriviEtichette();
            pannello.mostraWin10Accent(Configurazione.getMostraWin10());
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(rosso, BorderLayout.NORTH);
        frame.getContentPane().add(pannello, BorderLayout.CENTER);
        frame.setSize(rosso.getWidth(), rosso.getHeight() + pannello.getHeight());
    }

    void creaBarraDelTitolo() {
        titolo = new JLabel(nomeLampadina);
        JButton disconnetti = new JButton("❌");
        JLabel icona = new JLabel();
        rosso.setLayout(null);
        rosso.setBackground(new Color(160, 0, 0));
        titolo.setBounds(0, 0, d(530), d(40));
        titolo.setHorizontalAlignment(SwingConstants.CENTER);
        titolo.setVerticalAlignment(SwingConstants.CENTER);
        rosso.add(titolo);
        titolo.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent click) {
                if (click.getX() > d(490)) {
                    disconnetti.doClick();
                } else if (click.getX() > d(40)) {
                    String nome = JOptionPane.showInputDialog(Strings.get(Main.class, "16"), nomeLampadina);
                    if (nome != null && !nome.isEmpty()) {
                        connessione.cambiaNome(nome);
                        nomeLampadina = nome;
                        titolo.setText(nomeLampadina);
                    }
                } else if (!Arrays.asList(frame.getContentPane().getComponents()).contains(pannelloConnessione)) {
                    tornaModalitaRicerca();
                }
            }

            public void mouseEntered(MouseEvent click) {
                if (click.getX() > d(490)) {
                    disconnetti.setBackground(new Color(90, 0, 0));
                }
            }

            public void mouseExited(MouseEvent click) {
                disconnetti.setBackground(new Color(120, 0, 0));
                lkpX = 0;
                lkpY = 0;
                titolo.setText(nomeLampadina);
            }

            public void mousePressed(MouseEvent click) {
                if (click.getX() > d(490)) {
                    disconnetti.setBackground(new Color(60, 0, 0));
                }
            }

            public void mouseReleased(MouseEvent click) {
                if (click.getX() > d(490)) {
                    disconnetti.setBackground(new Color(120, 0, 0));
                }
                lkpX = 0;
                lkpY = 0;
            }
        });

        titolo.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent d) {
                if (d.getX() < d(490)) {
                    int nuovaX = d.getXOnScreen();
                    int nuovaY = d.getYOnScreen();
                    if (lkpX != 0 || lkpY != 0) {
                        int xInc = nuovaX - lkpX;
                        int yInc = nuovaY - lkpY;
                        frame.setLocation(nuovaX - d.getX() + xInc, nuovaY - d.getY() + yInc);
                    }
                    lkpX = nuovaX;
                    lkpY = nuovaY;
                }
            }

            public void mouseMoved(MouseEvent d) {
                if (d.getX() > d(490)) {
                    disconnetti.setBackground(new Color(90, 0, 0));
                } else {
                    disconnetti.setBackground(new Color(120, 0, 0));
                }
                if (d.getX() <= d(40) && !Arrays.asList(frame.getContentPane().getComponents()).contains(pannelloConnessione)) {
                    titolo.setText(Strings.get(Main.class, "18"));
                } else {
                    titolo.setText(nomeLampadina);
                }
            }
        });

        disconnetti.setBounds(d(490), 0, d(40), d(40));
        disconnetti.addActionListener(click -> {
            connessione.chiudi();
            System.exit(0);
        });
        disconnetti.setFocusable(false);
        disconnetti.setBackground(new Color(120, 0, 0));
        rosso.add(disconnetti);
        icona.setIcon(yee);
        icona.setBounds(0, 0, d(40), d(40));
        rosso.add(icona);
        rosso.setPreferredSize(new Dimension(d(530), d(40)));
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
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log(e);
        }
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

    ImageIcon caricaIcona() {
        try {
            return new ImageIcon(ImageIO.read(Objects.requireNonNull(Main.class.getResource("yee.png"))).getScaledInstance(d(40), d(40), Image.SCALE_SMOOTH));
        } catch (Exception e) {
            log(e);
            return null;
        }
    }

    public static void log(final Exception e) {
        e.printStackTrace();
    }

    public static void log(final String log) {
        System.out.println(log);
    }

    private void configuraUIManager() {
        final Font f = new Font("sans", Font.PLAIN, d(18));

        UIManager.put("Button.font", f2);
        UIManager.put("Button.background", sh1);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", sh1.brighter());
        UIManager.put("Button.border", 0);
        UIManager.put("TextField.font", f2);
        UIManager.put("TextField.background", sh1.brighter());
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.border", 0);
        UIManager.put("ComboBox.font", f2);
        UIManager.put("ComboBox.background", sh1);
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", sh1.brighter());
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
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

    public JFrame getFrame() {
        return frame;
    }

    public PannelloConnessione getPannelloConnessione() {
        return pannelloConnessione;
    }

    public Connessione getConnessione() {
        return connessione;
    }

}
