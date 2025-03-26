package miayeelight;

import miayeelight.lang.Strings;
import miayeelight.ux.schermo.Schermo;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.List;

import static miayeelight.ux.schermo.TimerColoreSchermo.ALGORITMO_FOTO;
import static miayeelight.ux.schermo.TimerColoreSchermo.ALGORITMO_PUNTI;

public class Configurazione implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TIMER_INT = "timerInt";
    public static final String RATIO = "ratio";
    public static final String COLORE_W_10 = "coloreW10";
    public static final String ALGO = "algo";
    public static final String LANG = "lang";
    public static final String SIGMA = "sigma";
    public static final String LOG = "log";

    private static final Map<String, String> CONF_BASE = Map.of( //
            LANG, Locale.getDefault().getLanguage(), //
            ALGO, "foto", //
            COLORE_W_10, "no", //
            RATIO, "auto", //
            TIMER_INT, "350",  //
            SIGMA, "8", //
            LOG, "no"
    );

    private static final Map<String, String[]> VALORI_AMMISSIBILI = Map.of( //
            LANG, new String[]{" it", " pt", " es", " fr", " en"}, //
            ALGO, new String[]{" foto", " pts"}, //
            COLORE_W_10, new String[]{" no", " si"}, //
            TIMER_INT, new String[]{" 160", " 250", " 350", " 500", " 800", " 1500"}, //
            SIGMA, new String[]{" 1", " 3", " 5", " 8", " 11", " 15", " 30"}, //
            LOG, new String[]{" no", " messaggio", " console"}
    );

    public static final Set<String> IMPOSTAZIONI_NON_MODIFICABILI = Set.of(RATIO);

    private static final Map<String, String> confCorrente = new HashMap<>(CONF_BASE);

    public static Map<String, String> getConfCorrente() {
        return confCorrente;
    }

    public static String get(final String parametro) {
        return confCorrente.get(parametro);
    }

    public static int getAlgoritmo() {
        return "pnt".equals(confCorrente.get(ALGO)) ? ALGORITMO_PUNTI : ALGORITMO_FOTO;
    }

    public static boolean getMostraWin10() {
        return !"no".equals(Configurazione.get(COLORE_W_10));
    }

    public static long getIntervalloTimer() {
        try {
            return Long.parseLong(confCorrente.get(TIMER_INT));
        } catch (NumberFormatException e) {
            return Long.parseLong(CONF_BASE.get(TIMER_INT));
        }
    }

    public static void applicaConfigurazione(final String[] parametri) {
        if (parametri != null) {
            final List<Map.Entry<String, String>> mappaParametri = Arrays.stream(parametri) //
                    .filter(p -> p.contains(":")).map(parametro -> parametro.split(":")) //
                    .map(parametroDiviso -> Map.entry(parametroDiviso[0], parametroDiviso[1])).toList();

            mappaParametri.stream().filter(parametro -> RATIO.equals(parametro.getKey())).findFirst().ifPresent(parametro -> {
                if ("auto".equals(parametro.getValue().trim())) {
                    Schermo.setRatio(((double) Toolkit.getDefaultToolkit().getScreenResolution()) / 100);
                } else {
                    Schermo.setRatio(Double.parseDouble(parametro.getValue().trim()));
                }
            });

            mappaParametri.stream().filter(parametro -> CONF_BASE.containsKey(parametro.getKey())).forEach(parametro -> confCorrente.put(parametro.getKey(), parametro.getValue().trim()));
        }
    }

    public static void applicaConfigurazione(final Map<String, String> parametri) {
        final Map<String, String> paramSanificati = new HashMap<>();
        parametri.forEach((key, value) -> paramSanificati.put(key, value.trim()));

        if (!Objects.equals(confCorrente.get(LANG), paramSanificati.get(LANG))) {
            Strings.configMessages(paramSanificati.get(LANG));
        }

        confCorrente.putAll(paramSanificati);
    }

    public static String[] getValoriAmmissibili(String nomeImpostazione) {
        return VALORI_AMMISSIBILI.get(nomeImpostazione);
    }

}