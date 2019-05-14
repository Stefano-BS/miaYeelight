package miaYeelight;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

final class PannelloPrincipale extends JPanel {
	private static final long serialVersionUID = 1L;
	JButton seguiWinAccent;
    JSlider luminosita = new JSlider(),
    		hue = new JSlider(),
    		sat = new JSlider();
	
	PannelloPrincipale(){
		setLayout(null);
        setBackground(Main.bg);
        JLabel 	descLuce = new JLabel("Imposta il valore di luminosità:"),
        		descTemp = new JLabel("Imposta la temperatura in Kelvin:"),
        		descCol = new JLabel("Imposta il colore (tonalità - saturazione):");
        JButton accendi = new JButton("💡  Accendi"),
        		spegni = new JButton("🕯  Spegni"),
        		timer = new JButton("⏱  Timer"),
        		impostaBr = new JButton("Imposta"),
        		impostaC = new JButton("Imposta"),
        		impostaTemp = new JButton("Imposta"),
        		winAccent = new JButton("Applica il colore di Windows 10"),
        		animazioni = new JButton("Usa un'animazione  🎆");
        seguiWinAccent = new JButton("Segui il colore di Windows 10");
        /*JTextField r = new JTextField(),
        		g = new JTextField(),
        		b = new JTextField();*/
        JSlider temperatura = new JSlider();
        
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
        accendi.setBounds(10, Y, 163, 40); timer.setBounds(183, Y, 163, 40); spegni.setBounds(357, Y, 163, 40);
        accendi.addActionListener(click -> Main.accendi());
        spegni.addActionListener(click -> Main.spegni());
        Integer[] listaTimer = new Integer[50];
        for (int i=1; i<=10; i++) listaTimer[i-1]=i;
        for (int i=11; i<=30; i++) listaTimer[i-1]=2*i-10;
        for (int i=31; i<=40; i++) listaTimer[i-1]=5*i-100;
        for (int i=41; i<=50; i++) listaTimer[i-1]=10*i-300;
        timer.addActionListener(click -> {
        	Main.timer((Integer)JOptionPane.showInputDialog(Main.frame, "Imposta il tempo (in minuti) tra cui spegnere la lampadina", "Timer di autospegnimento", JOptionPane.QUESTION_MESSAGE, Main.yee, listaTimer, 1));
        });
        accendi.setFocusable(false); timer.setFocusable(false); spegni.setFocusable(false);
        add(accendi); add(timer); add(spegni);
        Y += 50;
        descLuce.setBounds(10, Y, 510, 40);
        descLuce.setOpaque(false);
        add(descLuce);
        Y += 50;
        luminosita.setBounds(10, Y, 350, 40); impostaBr.setBounds(370, Y, 150, 40);
        luminosita.setBackground(Main.trasparente); luminosita.setForeground(Color.WHITE);
        luminosita.setMaximum(100); luminosita.setMinimum(1);
        luminosita.addChangeListener(s -> descLuce.setText("Imposta il valore di luminosità: " + luminosita.getValue() + "%"));
        luminosita.addChangeListener(lambdaColore);
        luminosita.addChangeListener(lambdaTemperatura);
        luminosita.setOpaque(false);
        impostaBr.addActionListener(click -> Main.setBr(luminosita.getValue()));
        impostaBr.setFocusable(false);
        add(impostaBr); add(luminosita);
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
        impostaTemp.addActionListener(click -> Main.temperatura(temperatura.getValue()));
        impostaTemp.setFocusable(false);
        add(impostaTemp); add(temperatura);
        Y += 50;
        descCol.setBounds(10, Y, 510, 40);
        add(descCol);
        Y += 50;
        //r.setBounds(10, Y, 110, 40); g.setBounds(130, Y, 110, 40); b.setBounds(250, Y, 110, 40);
        //r.setHorizontalAlignment(SwingConstants.CENTER); g.setHorizontalAlignment(SwingConstants.CENTER); b.setHorizontalAlignment(SwingConstants.CENTER);
        hue.setBounds(10, Y, 170, 40); sat.setBounds(190, Y, 170, 40);
        hue.setBackground(Main.trasparente); hue.setForeground(Color.WHITE); sat.setBackground(Main.trasparente); sat.setForeground(Color.WHITE);
        hue.setMaximum(359); hue.setMinimum(0);
        sat.setMaximum(100); sat.setMinimum(0);
        hue.setOpaque(false); sat.setOpaque(false);
        impostaC.setBounds(370, Y, 150, 40);
        impostaC.addActionListener(click -> Main.setHS(hue.getValue(), sat.getValue()));
        //impostaC.addActionListener(click -> setRGB(Integer.parseInt(r.getText()),Integer.parseInt(g.getText()),Integer.parseInt(b.getText())));
        impostaC.setFocusable(false);
        add(hue); add(sat); add(impostaC);
        //pannello.add(r); pannello.add(g); pannello.add(b); pannello.add(impostaC);
        hue.addChangeListener(lambdaColore);
        sat.addChangeListener(lambdaColore);
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
}