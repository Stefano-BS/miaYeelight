package miaYeelight;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;

final class PannelloPrincipale extends JPanel {
	private static final long serialVersionUID = 1L;
	JButton seguiWinAccent, accendi = new JButton("💡  Accendi");
    JSlider luminosita = new JSlider(),
    		hue = new JSlider(),
    		sat = new JSlider(),
    		temperatura = new JSlider();
    Timer crovescia = null;
    boolean modoDiretto = true;
	
	PannelloPrincipale(){
		setLayout(null);
        setBackground(Main.bg);
        JLabel 	descLuce = new JLabel("Imposta il valore di luminosità:"),
        		descTemp = new JLabel("Imposta la temperatura in Kelvin:"),
        		descCol = new JLabel("Imposta il colore (tonalità - saturazione):");
        JButton timer = new JButton("⏱  Timer"),
        		//impostaBr = new JButton("Imposta"),
        		impostaC = new JButton("Imposta"),
        		impostaTemp = new JButton("Imposta"),
        		winAccent = new JButton("Applica il colore di Windows 10"),
        		animazioni = new JButton("Usa un'animazione  🎆");
        seguiWinAccent = new JButton("Segui il colore di Windows 10");
        
        ChangeListener 	lambdaColore = s -> {
				        	impostaC.setBackground(new Color(Color.HSBtoRGB(((float)hue.getValue())/359, ((float)sat.getValue())/100, ((float)(luminosita.getValue()>80?100:luminosita.getValue()+20))/100)));
				        	impostaC.setForeground(new Color(Color.HSBtoRGB(((float)((hue.getValue()+180) % 359))/359, ((float)sat.getValue())/100, ((float)((luminosita.getValue()+50) % 100)/100))));
        				},
        				lambdaTemperatura = s -> {
        					impostaTemp.setBackground(new Color((int)(200*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100)),
        														(int)((temperatura.getValue()/37+75)*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100)),
        														(int)((temperatura.getValue()-1700)/18.8f*(luminosita.getValue() > 50 ? 1 : 0.5+(float)luminosita.getValue()/100))));
        														/*(int)((luminosita.getValue()/2+50)*2.3),
        														(int)((luminosita.getValue()/2+50)*2.25) + temperatura.getValue()/260*luminosita.getValue()/100,
        														(int)(((luminosita.getValue()/2+50)*2.55)*((float)(temperatura.getValue()-1700)/4800))));*/
        					impostaTemp.setForeground(luminosita.getValue() > 45? Color.BLACK : Color.WHITE);
        				}
        ;
        
        int Y = 10;
        accendi.setBounds(10, Y, 250, 40); timer.setBounds(270, Y, 250, 40);
        accendi.addActionListener(click -> {
        	if (accendi.getText().equals("💡  Accendi")) {
        		accendi.setText("🕯  Spegni");
        		Main.accendi();
        	} else {
        		accendi.setText("💡  Accendi");
        		Main.spegni();
        	}
        });
        Integer[] listaTimer = new Integer[50];
        for (int i=1; i<=10; i++) listaTimer[i-1]=i;
        for (int i=11; i<=30; i++) listaTimer[i-1]=2*i-10;
        for (int i=31; i<=40; i++) listaTimer[i-1]=5*i-100;
        for (int i=41; i<=50; i++) listaTimer[i-1]=10*i-300;
        timer.addActionListener(click -> {
        	try {
        		int tempo = (Integer)JOptionPane.showInputDialog(Main.frame, "Imposta il tempo (in minuti) tra cui spegnere la lampadina", "Timer di autospegnimento", JOptionPane.QUESTION_MESSAGE, Main.yee, listaTimer, 1);
        		Main.timer(tempo);
        	} catch (Exception e) {}
        });
        accendi.setFocusable(false); timer.setFocusable(false);
        add(accendi); add(timer);
        Y += 50;
        descLuce.setBounds(10, Y, 510, 40);
        descLuce.setOpaque(false);
        add(descLuce);
        Y += 50;
        luminosita.setBounds(10, Y, 510, 40); //impostaBr.setBounds(370, Y, 150, 40);
        luminosita.setBackground(Main.trasparente); luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100); luminosita.setMinimum(1);
        luminosita.addChangeListener(s -> descLuce.setText("Imposta il valore di luminosità: " + luminosita.getValue() + "%"));
        luminosita.addChangeListener(lambdaColore);
        luminosita.addChangeListener(lambdaTemperatura);
        luminosita.addChangeListener(eventoJSlider(e -> Main.setBr(luminosita.getValue())));
        luminosita.setOpaque(false);
        //impostaBr.addActionListener(click -> Main.setBr(luminosita.getValue()));
        //impostaBr.setFocusable(false);
        add(luminosita); //add(impostaBr); 
        Y += 50;
        descTemp.setBounds(10, Y, 510, 40);
        add(descTemp);
        Y += 50;
        temperatura.setBounds(10, Y, 350, 40); impostaTemp.setBounds(370, Y, 150, 40);
        temperatura.setBackground(Main.trasparente); temperatura.setForeground(Color.WHITE);
        temperatura.setOpaque(false);
        temperatura.setMaximum(6500); temperatura.setMinimum(1700);
        temperatura.setValue(5000);
        temperatura.addChangeListener(s -> descTemp.setText("Imposta la temperatura in Kelvin: " + temperatura.getValue() + "K"));
        temperatura.addChangeListener(lambdaTemperatura);
        temperatura.addChangeListener(eventoJSlider(e -> Main.temperatura(temperatura.getValue())));
        impostaTemp.addActionListener(click -> Main.temperatura(temperatura.getValue()));
        impostaTemp.setFocusable(false);
        add(impostaTemp); add(temperatura);
        Y += 50;
        descCol.setBounds(10, Y, 510, 40);
        add(descCol);
        Y += 50;
        hue.setBounds(10, Y, 170, 40); sat.setBounds(190, Y, 170, 40);
        hue.setBackground(Main.trasparente); hue.setForeground(Color.WHITE); sat.setBackground(Main.trasparente); sat.setForeground(Color.WHITE);
        hue.setMaximum(359); hue.setMinimum(0);
        sat.setMaximum(100); sat.setMinimum(0);
        hue.setOpaque(false); sat.setOpaque(false);
        impostaC.setBounds(370, Y, 150, 40);
        impostaC.addActionListener(click -> Main.setHS(hue.getValue(), sat.getValue()));
        impostaC.setFocusable(false);
        add(hue); add(sat); add(impostaC);
        hue.addChangeListener(lambdaColore);
        sat.addChangeListener(lambdaColore);
        hue.addChangeListener(eventoJSlider(e -> Main.setHS(hue.getValue(), sat.getValue())));
        sat.addChangeListener(eventoJSlider(e -> Main.setHS(hue.getValue(), sat.getValue())));
        Y += 50;
        winAccent.setBounds(10, Y, 250, 40);
        seguiWinAccent.setBounds(270, Y, 250, 40);
        winAccent.setFocusable(false); seguiWinAccent.setFocusable(false);
        winAccent.addActionListener(click -> Main.cambiaColoreDaAccent());
        seguiWinAccent.addActionListener(click -> Main.tienitiAggiornataSuWindows());
        add(winAccent); add(seguiWinAccent);
        Y += 50;
        animazioni.setBounds(10,Y,510,40);
        animazioni.setFocusable(false);
        animazioni.addActionListener(click -> Main.apriPannelloAnimazioni());
        add(animazioni);
        Y += 50;
        setSize(530, Y);
        setVisible(true);
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
}