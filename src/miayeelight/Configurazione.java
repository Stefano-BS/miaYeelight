package miayeelight;

import miayeelight.lang.Strings;
import miayeelight.ux.schermo.Schermo;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.*;

import static miayeelight.ux.schermo.TimerColoreSchermo.ALGORITMO_FOTO;
import static miayeelight.ux.schermo.TimerColoreSchermo.ALGORITMO_PUNTI;

public class Configurazione implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Map<String, String> CONF_BASE = Map.of( //
            "lang", Locale.getDefault().getLanguage(), //
            "algo", "foto", //
            "coloreW10", "no", //
            "ratio", "auto", //
            "timerInt", "350"
    );

    private static final Map<String, String[]> VALORI_AMMISSIBILI = Map.of( //
            "lang", new String[]{" it", " pt", " es", " fr", " en"}, //
            "algo", new String[]{" foto", " pts"}, //
            "coloreW10", new String[]{" no", " si"}, //
            "timerInt", new String[]{" 100", " 150", " 220", " 350", " 500", " 800", " 1500"}
    );

    public static final Set<String> IMPOSTAZIONI_NON_MODIFICABILI = Set.of("ratio");

    private static final Map<String, String> confCorrente = new HashMap<>(CONF_BASE);

    public static Map<String, String> getConfCorrente() {
        return confCorrente;
    }

    public static String get(final String parametro) {
        return confCorrente.get(parametro);
    }

    public static int getAlgoritmo() {
        return "pnt".equals(confCorrente.get("algo")) ? ALGORITMO_PUNTI : ALGORITMO_FOTO;
    }

    public static boolean getMostraWin10() {
        return !"no".equals(Configurazione.get("coloreW10"));
    }

    public static long getIntervalloTimer() {
        try {
            return Long.parseLong(confCorrente.get("timerInt"));
        } catch (NumberFormatException e) {
            return Long.parseLong(CONF_BASE.get("timetInt"));
        }
    }

    public static void applicaConfigurazione(final String[] parametri) {
        if (parametri != null) {
            final List<Map.Entry<String, String>> mappaParametri = Arrays.stream(parametri) //
                    .filter(p -> p.contains(":")).map(parametro -> parametro.split(":")) //
                    .map(parametroDiviso -> Map.entry(parametroDiviso[0], parametroDiviso[1])).toList();

            mappaParametri.stream().filter(parametro -> "ratio".equals(parametro.getKey())).findFirst().ifPresent(parametro -> {
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
        parametri.forEach((key, value) -> parametri.put(key, value.trim()));
        
        if (!Objects.equals(confCorrente.get("lang"), parametri.get("lang"))) {
            Strings.configMessages(parametri.get("lang"));
        }

        confCorrente.putAll(parametri);
    }

    public static String[] getValoriAmmissibili(String nomeImpostazione) {
        return VALORI_AMMISSIBILI.get(nomeImpostazione);
    }

}