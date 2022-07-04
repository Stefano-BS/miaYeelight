package miaYeelight;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;

final class PannelloPrincipale extends JPanel {
	private static final long serialVersionUID = 1L;
	JButton seguiWinAccent, seguiSchermo,
			accendi = new JButton(Strings.get("PannelloPrincipale.0")),
			timer = new JButton(Strings.get("PannelloPrincipale.1")),
    		winAccent = new JButton(Strings.get("PannelloPrincipale.4")),
    		animazioni = new JButton(Strings.get("PannelloPrincipale.5"));
    JSlider luminosita = Slider.fab(Slider.PRESETLUM),
    		hue = Slider.fab(Slider.PRESETTON),
    		sat = Slider.fab(Slider.PRESETSAT),
    		temperatura = Slider.fab(Slider.PRESETCT);
    JPanel anteprima = new JPanel();
    Timer crovescia = null;
    boolean modoDiretto = true;
    boolean ultimaModalita = false; // false: T, true: C
	
	PannelloPrincipale(Main ref){
		setLayout(null);
        setBackground(Main.bg);
        JLabel 	descLuce = new JLabel(Strings.get("PannelloPrincipale.16")),
        		descTemp = new JLabel(Strings.get("PannelloPrincipale.18")),
        		descCol = new JLabel(Strings.get("PannelloPrincipale.8"));
        seguiWinAccent = new JButton(Strings.get("PannelloPrincipale.9"));
        seguiSchermo = new JButton(Strings.get("PannelloPrincipale.10"));
        
        int Y = Schermo.d(10);
        accendi.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40)); timer.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        accendi.addActionListener(click -> {
        	if (accendi.getText().equals(Strings.get("PannelloPrincipale.0"))) {
        		accendi.setText(Strings.get("PannelloPrincipale.12"));
        		ref.connessione.accendi();
        	} else {
        		accendi.setText(Strings.get("PannelloPrincipale.0"));
        		ref.connessione.spegni();
        	}
        });
        Integer[] listaTimer = new Integer[50];
        for (int i=1; i<=10; i++) listaTimer[i-1]=i;
        for (int i=11; i<=30; i++) listaTimer[i-1]=2*i-10;
        for (int i=31; i<=40; i++) listaTimer[i-1]=5*i-100;
        for (int i=41; i<=50; i++) listaTimer[i-1]=10*i-300;
        timer.addActionListener(click -> {
        	try {
        		int tempo = (Integer)JOptionPane.showInputDialog(ref.frame, Strings.get("PannelloPrincipale.14"), Strings.get("PannelloPrincipale.15"), JOptionPane.QUESTION_MESSAGE, ref.yee, listaTimer, 1); 
        		ref.connessione.timer(tempo);
        	} catch (Exception e) {e.printStackTrace();}
        });
        accendi.setFocusable(false); timer.setFocusable(false);
        add(accendi); add(timer);
        Y += Schermo.d(50);
        seguiSchermo.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        seguiSchermo.addActionListener(click -> ref.seguiColoreSchermo());
        seguiSchermo.setFocusable(false);
        add(seguiSchermo);
        Y += Schermo.d(50);
        winAccent.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40));
        seguiWinAccent.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        winAccent.setFocusable(false); seguiWinAccent.setFocusable(false);
        winAccent.addActionListener(click -> ref.cambiaColoreDaAccent());
        seguiWinAccent.addActionListener(click -> ref.tienitiAggiornataSuWindows());
        add(winAccent); add(seguiWinAccent);
        Y += Schermo.d(50);
        animazioni.setBounds(Schermo.d(10),Y,Schermo.d(510),Schermo.d(40));
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> ref.apriPannelloAnimazioni());
        add(animazioni);
        Y += Schermo.d(70);
        descLuce.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        descLuce.setOpaque(false);
        add(descLuce);
        Y += Schermo.d(40);
        luminosita.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        luminosita.setBackground(Main.trasparente); luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100); luminosita.setMinimum(0);
        luminosita.setMajorTickSpacing(10); luminosita.setMinorTickSpacing(5); luminosita.setPaintTicks(true);
        luminosita.addChangeListener(s -> descLuce.setText(Strings.get("PannelloPrincipale.16") + luminosita.getValue() + "%")); 
        luminosita.addChangeListener(s -> aggiornaAnteprima());
        luminosita.addChangeListener(s -> aggiornaAnteprima());
        luminosita.addChangeListener(eventoJSlider(e -> ref.connessione.setBr(Math.max(luminosita.getValue(),1))));
        luminosita.setOpaque(false);
        add(luminosita); 
        Y += Schermo.d(40);
        descTemp.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        add(descTemp);
        Y += Schermo.d(40);
        temperatura.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        temperatura.setBackground(Main.trasparente); temperatura.setForeground(Color.WHITE);
        temperatura.setOpaque(false);
        temperatura.setMaximum(6500); temperatura.setMinimum(1700);
        temperatura.setValue(5000);
        temperatura.setSnapToTicks(true); temperatura.setMinorTickSpacing(100); temperatura.setPaintTicks(true);
        temperatura.addChangeListener(s -> descTemp.setText(Strings.get("PannelloPrincipale.18") + temperatura.getValue() + "K"));
        temperatura.addChangeListener(s -> ultimaModalita = false);
        temperatura.addChangeListener(s -> aggiornaAnteprima());
        temperatura.addChangeListener(eventoJSlider(e -> ref.connessione.temperatura(temperatura.getValue())));
        add(temperatura);
        Y += Schermo.d(40);
        descCol.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(40));
        add(descCol);
        Y += Schermo.d(40);
        hue.setBounds(Schermo.d(10), Y, Schermo.d(250), Schermo.d(40)); sat.setBounds(Schermo.d(270), Y, Schermo.d(250), Schermo.d(40));
        hue.setBackground(Main.trasparente); hue.setForeground(Color.WHITE); sat.setBackground(Main.trasparente); sat.setForeground(Color.WHITE);
        hue.setMaximum(359); hue.setMinimum(0);
        sat.setMaximum(100); sat.setMinimum(0);
        hue.setOpaque(false); sat.setOpaque(false);
        add(hue); add(sat);
        hue.addChangeListener(s -> ultimaModalita = true);
        hue.addChangeListener(s -> aggiornaAnteprima());
        sat.addChangeListener(s -> ultimaModalita = true);
        sat.addChangeListener(s -> aggiornaAnteprima());
        hue.addChangeListener(eventoJSlider(e -> ref.connessione.setHS(hue.getValue(), sat.getValue())));
        sat.addChangeListener(eventoJSlider(e -> ref.connessione.setHS(hue.getValue(), sat.getValue())));
        Y += Schermo.d(50);
        anteprima.setBounds(Schermo.d(10), Y, Schermo.d(510), Schermo.d(20));
        anteprima.setFocusable(false);
        add(anteprima);
        Y += Schermo.d(30);
        setSize(Schermo.d(530), Y);
        setVisible(true);
	}
	
	void aggiornaAnteprima() {
		if (ultimaModalita) anteprima.setBackground(new Color(Color.HSBtoRGB(((float)hue.getValue())/359, ((float)sat.getValue())/100, ((float)(luminosita.getValue()>80?100:luminosita.getValue()+20))/100)));
		else anteprima.setBackground(new Color ((int)(200*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100)),
												(int)((temperatura.getValue()/37+75)*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100)),
												(int)((temperatura.getValue()-1700)/18.8f*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100))));
		;
	}
	
	private ChangeListener eventoJSlider (ActionListener azione) {
		return e -> {
			if (!modoDiretto) return;
        	if (crovescia == null) crovescia = new Timer(200, azione);
        	else if (crovescia.isRunning()) {
        		crovescia.stop();
        		crovescia = new Timer(200, azione);
        	}
        	crovescia.setRepeats(false);
        	crovescia.start();
        };
	}
	
	void abilitaControlli(boolean abilita) {
		seguiWinAccent.setEnabled(abilita);
		hue.setEnabled(abilita);
		sat.setEnabled(abilita);
		temperatura.setEnabled(abilita);
		winAccent.setEnabled(abilita);
	}
}