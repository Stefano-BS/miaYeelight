package miayeelight.ux.schermo;

import miayeelight.net.Connessione;
import miayeelight.ux.pannelli.PannelloPrincipale;

import java.awt.*;
import java.util.TimerTask;

public class TimerColoreSchermo extends TimerTask {

    public static final int ALGORITMO_PUNTI = 0;
    public static final int ALGORITMO_FOTO = 1;

    private static final double SIGMOIDE_MIN_VAL = 1 / (1 + Math.exp(-7 * -0.15));
    private static final double SIGMOIDE_MAX_VAL = 1 / (1 + Math.exp(-7 * (1 - 0.15)));

    private final int passoMinimo;
    private final int passoMassimo;
    private final int algoritmo;
    private final PannelloPrincipale pannello;
    private final Connessione connessione;

    private int campione;
    private int ultimaSaturazioneInviata = 0;
    private int ultimaTonalitaInviata = 0;

    public TimerColoreSchermo(final int algoritmo, final PannelloPrincipale pannello, final Connessione connessione) {
        this.pannello = pannello;
        this.connessione = connessione;
        this.algoritmo = algoritmo;
        passoMinimo = algoritmo == ALGORITMO_FOTO ? 6 : 0;
        passoMassimo = algoritmo == ALGORITMO_FOTO ? 8 : 2;
        campione = passoMinimo;
    }

    @Override
    public void run() {
        final int tonalita;
        final int saturazioneAggiustata;

        if (algoritmo == ALGORITMO_PUNTI) {
            final Color c = Schermo.ottieniMedia(campione == 2 ? 50 : 150, campione == 2 ? 0.25 : 0.4, 0.15);
            final float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[3]);
            tonalita = (int) (hsbVals[0] * 360);
            saturazioneAggiustata = (int) (hsbVals[1] * 70 + 30);
        } else {
            final Color c = Schermo.ottieniMedia((double) campione / 15);
            final float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[3]);
            tonalita = (int) (hsbVals[0] * 360);
            saturazioneAggiustata = sigmoide(hsbVals[1]);
        }

        final boolean modoPrima = pannello.getModoDiretto();
        pannello.setModoDiretto(false);
        pannello.getHue().setValue(tonalita);
        pannello.getSat().setValue(saturazioneAggiustata);
        pannello.setModoDiretto(modoPrima);

        campione++;
        if (campione > passoMassimo) {
            campione = passoMassimo;

            if (Math.abs(tonalita - ultimaTonalitaInviata) > 5 || Math.abs(saturazioneAggiustata - ultimaSaturazioneInviata) > 2) {
                campione = passoMinimo;
                ultimaSaturazioneInviata = saturazioneAggiustata;
                ultimaTonalitaInviata = tonalita;
                connessione.setHS(ultimaTonalitaInviata, ultimaSaturazioneInviata);
            }
        }
    }

    private int sigmoide(final float h) {
        final double sigmaNonNormalizzata = (1 / (1 + Math.exp(-7 * (h - 0.15))));
        return (int) (100 * ((sigmaNonNormalizzata - SIGMOIDE_MIN_VAL) / (SIGMOIDE_MAX_VAL - SIGMOIDE_MIN_VAL)));
    }

}