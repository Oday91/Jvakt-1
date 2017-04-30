package Jvakt;

	/*
	 * To change this template, choose Tools | Templates
	 * and open the template in the editor.
	 */

	/**
	 *
	 * @author Annika
	 */
	import java.awt.*;
	import java.awt.event.*;
	import javax.swing.event.*;
	import javax.swing.*;
	import javax.swing.table.*;
	import java.io.*;
	import java.util.*;
	import javax.swing.ListSelectionModel;
	import javax.swing.event.ListSelectionEvent;
	import javax.swing.event.ListSelectionListener;

	
	// Extend av jframe f�r att f� tillg�ng till swing metoderna i Jframe. 
	// Jframe �r basen i f�nsterhanteringen.
	//implementerar TableModelListener f�r att anv�nda denna class som lyssnare till Jtables datamodellclass via metoden tableChanged.
	//implementerar WindowListener f�r att anv�nda denna class som lyssnare till Jframe med metoden windowClosing; och d�r t�mma data till filer.
	public class console extends JFrame implements TableModelListener, WindowListener {

	// Skapar diverse variabler
		private JPanel topPanel;
	    private JPanel usrPanel;
	    private JPanel logPanel;
	    private JTable table;
	    private JScrollPane scrollPane;
	    private JScrollPane scrollPane2;
	    private consoleDM wD;

	     /**
	     * @param args the command line arguments
	     */
	    public static void main(String[] args) throws IOException {
	        console mainFrame = new console();  // g�r objekt av innevarande class 
	        mainFrame.pack();                   // kallar p� innevarande class metod pack som �rvts via Jframe 
	        mainFrame.setVisible(true);  	    // kallar p� innevarande class metod setVisible och nu visas f�nster f�r anv�ndaren
	    }   // main st�r nu och "v�ntar" vid slutet tills de andra objekten avslutas.
	    

	    // construktorn som startas i den statiska main metoden.
	    // skapar alla inblandade objekt och kopplar ihop dom.
	    // kallar ocks� p� metoder �rvda fr�n Jframe att s�tta vissa v�rden.
	    public console() throws IOException {
	    	
	    	// funktion fr�n Jframe att s�tta rubrik
	        setTitle("Jvakt consolev 2.0 alpha");
	        //setSize(600, 200);
	    	// funktion fr�n Jframe att s�tta f�rg
	        setBackground(Color.gray);
	    	// skapar ny Jpanel och sparar referensen i topPanel
	        topPanel = new JPanel();
	        // ber�ttar f�r topPanel vilken layout den ska anv�nda genom att skapa ett BorderLayout object utan namn.
	        topPanel.setLayout(new BorderLayout());
	        //topPanel.setLayout(new FlowLayout());
	        // H�mtar Jpanels enkla content hanterare och l�gger dit topPanel i st�llet att hantera resten av objekten
	        getContentPane().add(topPanel);
	        //topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

	        // Skapar datamodel f�r datahanteringen av userDB i table
	        wD = new consoleDM();
	        // skapar en Jtable och l�gger till referensen till wD via Jtables contructor
	        // table kommer att visa userDB
	        table = new JTable(wD);

	        // talar om f�r table att man bara f�r v�lja en rad i taget
	        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        
	        // ber table om referensen till LIstSecectionModel objektet, sparar i rowSM
	        ListSelectionModel rowSM = table.getSelectionModel();
	        
	        //
	        // OBS intern class start---
	        // Anv�nder rowSM metod f�r att skapa lyssnare till table f�r att veta vilken rad som v�ljs.
	            rowSM.addListSelectionListener(new ListSelectionListener()  {
	             // interna classens metod som tar fram vilken rad som valts
	            	public void valueChanged(ListSelectionEvent e)  {
	                // Ignore extra messages.
	                if (e.getValueIsAdjusting())
	                  return;

	                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
	                if (lsm.isSelectionEmpty()) {
	                  System.out.println("No rows are selected.");
	                } else {
	                  int selectedRow = lsm.getMinSelectionIndex();
	                  System.out.println("Row " + selectedRow + " is now selected.");
	                }
	              }
	            	
	            }    );
	        // OBS intern class end---
	        //
	            
	            
	        // s�tter automatsortering i tabellerna    
	        table.setAutoCreateRowSorter(true);
	        // talar om f�r tabellernas datamodellobjekt (wD o wD2) att detta objekt lyssnar; metoden tableChanged
	        table.getModel().addTableModelListener(this);
	        // skapar nya JScrollPane och l�gger till tabellerna via construktorn. F�r att kunna scrolla tabellerna.
	        scrollPane = new JScrollPane(table);

	        // skapar tv� nya JPanel att anv�ndas inuti topPanel, som ocks� �r en JPanel
	        usrPanel = new JPanel();
	        usrPanel.setLayout(new BorderLayout());
	        logPanel = new JPanel();
	        logPanel.setLayout(new BorderLayout());
	        // talar om f�r de nya JPanels vilka scrollPanes dom ska inneh�lla (scrollPanes inneh�ller tabellerna).
	        usrPanel.add(scrollPane, BorderLayout.CENTER);
	        logPanel.add(scrollPane2, BorderLayout.CENTER);
	        // talar om f�r topPanel att den ska inneh�lla tc� JPanelobjekt NORTH och CENTER       
	        topPanel.add(usrPanel, BorderLayout.NORTH);
	        topPanel.add(logPanel, BorderLayout.CENTER);
	        // talar om f�r innevarande object att den lyssnar p� sig sj�lv. (metoderna f�r WindowListener)
	        addWindowListener(this);

	    } // slut construktor
	    
	     
	    // vi implementerade TableModelListener och addade "this" f�r att denna metod skulle anropas vid �nding av v�rde i tabellen
	    // detta anv�ndas bara f�r loggning
	     public void tableChanged(TableModelEvent e)  {
	        int row = e.getFirstRow();
	        int column = e.getColumn();
	        String ls ;
	        TableModel model = (TableModel)e.getSource();
	        String columnName = model.getColumnName(column);
	        String data = (String)model.getValueAt(row, column);
	        ls = "Workout tableChanged " + row + " " + column + " " +  data;
	        System.out.println(ls);
	    }
	 
	    
	   // windows listeners
	   // vi implementerade WindowListener och addade "this" f�r att denna metod skulle anropas vid normalt avslut av Jframe 
	   // v�rdena i tabellerna skrivt till var sin fil
	     public void windowClosing(WindowEvent e) {
	    	//skriv userDB
	    	wD.closeDB();
	    // ...och h�r �r det slut i rutan..!!!... 
	    }
	     
	   // vi implementerade WindowListener men f�ljande metoder av�nds inte 
	    public void windowClosed(WindowEvent e) {    }
	    public void windowOpened(WindowEvent e) {    }
	    public void windowIconified(WindowEvent e) {    }
	    public void windowDeiconified(WindowEvent e) {    }
	    public void windowActivated(WindowEvent e) {    }
	    public void windowDeactivated(WindowEvent e) {    }

}
