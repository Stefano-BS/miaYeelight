package miayeelight.ux.schermo;

import miayeelight.Configurazione;
import miayeelight.net.Connessione;
import miayeelight.ux.pannelli.PannelloPrincipale;

import java.awt.*;
import java.util.TimerTask;

import static miayeelight.Configurazione.SIGMA;

public class TimerColoreSchermo extends TimerTask {

    public static final int ALGORITMO_PUNTI = 0;
    public static final int ALGORITMO_FOTO = 1;

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

        if (!connessione.isConnesso()) {
            return;
        }

        if (algoritmo == ALGORITMO_PUNTI) {
            final Color c = Schermo.ottieniMedia(campione == 2 ? 50 : 150, campione == 2 ? 0.25 : 0.4, 0.15);
            final float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[3]);
            tonalita = (int) (hsbVals[0] * 360);
            saturazioneAggiustata = (int) (hsbVals[1] * 70 + 30);
        } else {
            final Color c = Schermo.ottieniMedia((double) campione / 15);
            final float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[3]);
            tonalita = (int) (hsbVals[0] * 360);
            saturazioneAggiustata = sigmoide(hsbVals[1], Integer.parseInt(Configurazione.get(SIGMA)));
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

    private int sigmoide(final float h, final int k) {
        final double sigmaNonNormalizzata = (1 / (1 + Math.exp(-k * (h - 0.15))));
        final double sigmoideMinVal = 1 / (1 + Math.exp(-k * -0.15));
        final double sigmoideMaxVal = 1 / (1 + Math.exp(-k * (1 - 0.15)));
        return (int) (100 * ((sigmaNonNormalizzata - sigmoideMinVal) / (sigmoideMaxVal - sigmoideMinVal)));
    }

}