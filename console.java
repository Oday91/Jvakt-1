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
import java.sql.SQLException;
import java.util.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Timer;
import javax.swing.border.*;

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
	private JButton bu1;
	private JTableHeader header;
	//	    private JScrollPane scrollPane2;
	private consoleDM wD;
	private Boolean swAuto = true;
	//	private Boolean swAuto = false;
	private Boolean swRed = true; 
	private Boolean swDBopen = true; 
	private Boolean swServer = true; 
	private Boolean swDormant = true; 

	//	private  String host = "193.234.149.176";
	private  String jvhost = "127.0.0.1";
	private  String jvport = "1956";
	private  int port = 1956; 
	private  String cmdHst = "java -cp console.jar;postgresql-42.1.3.jar Jvakt.consoleHst";
	private  String cmdSts = "java -cp console.jar;postgresql-42.1.3.jar Jvakt.consoleSts";

	private  int deselectCount = 0; 
	private  int jvconnectCount = 0; 

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

		ImageIcon img = new ImageIcon("console.png");
		setIconImage(img.getImage());

		// get the parameters from the console.properties file
		getProps();
		port = Integer.parseInt(jvport);

		// funktion fr�n Jframe att s�tta rubrik
		setTitle("Jvakt console 2.35  -  F1 = Help");
		//	        setSize(5000, 5000);

		// get the screen size as a java dimension
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// get 2/5 of the height, and 2/3 of the width
		int height = screenSize.height * 1 / 5;
		int width = screenSize.width * 5 / 6;

		// set the jframe height and width
		setPreferredSize(new Dimension(width, height));


		// funktion fr�n Jframe att s�tta f�rg
		setBackground(Color.gray);
		setUndecorated(false);
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

		//		JTableHeader header = table.getTableHeader();
		header = table.getTableHeader();
		header.setBackground(Color.LIGHT_GRAY);
		//		header.setBackground(Color.white);

		bu1 = new JButton();

		System.out.println("screenHeightWidth :" +screenSize.height+" " +screenSize.width);
		if (screenSize.height > 1200) {
			table.setRowHeight(table.getRowHeight()*2);
			header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
		}
		else 
			if (screenSize.height > 1080) {
				table.setRowHeight(table.getRowHeight()*1,5);
				header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			}

		swServer = true;
		try {
			SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer �r tillg�nglig.
			String oSts = jm.open();
			//			System.out.println("#1 "+oSts);
			if (oSts.startsWith("failed")) 	swServer  = false;
			if (oSts.startsWith("DORMANT")) swDormant = true;
			else 							swDormant = false;
			jm.close();
		} 
		catch (IOException e1) {
			swServer = false;
			System.err.println(e1);
			//			System.err.println(e1.getMessage());
		}
		catch (NullPointerException npe2 )   {
			swServer = false;
			System.out.println("-- Rpt Failed --" + npe2);
		}

		//		System.out.println("swServer :" + swServer);

		swDBopen = wD.refreshData(); // kollar om DB �r tillg�nglig
		setBu1Color();

		bu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swAuto = !swAuto;
				swDBopen = wD.refreshData();
				setBu1Color();
			}
		});

		// talar om f�r table att man bara f�r v�lja en rad i taget
		//		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// ber table om referensen till LIstSecectionModel objektet, sparar i rowSM
		ListSelectionModel rowSM = table.getSelectionModel();

		//
		// OBS intern class start---
		// Anv�nder rowSM metod f�r att skapa lyssnare till table f�r att veta vilken rad som v�ljs.
		rowSM.addListSelectionListener(new ListSelectionListener()  {
			// interna classens metod som tar fram vilken rad som valts
			public void valueChanged(ListSelectionEvent e)   {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					System.out.println("No rows are selected.");
				} else {
					int selectedRow = lsm.getMinSelectionIndex();
					System.out.println("Row " + selectedRow + " is now selected.");
					deselectCount = 0;
				}
			}

		}    );
		// OBS intern class end---
		//


		// s�tter automatsortering i tabellerna    
		//	        table.setAutoCreateRowSorter(true);
		// talar om f�r tabellernas datamodellobjekt (wD o wD2) att detta objekt lyssnar; metoden tableChanged
		table.getModel().addTableModelListener(this);

		// s�tter f�rg p� raderna
		consoleCR cr=new consoleCR();

		//		for (int i=0; i <= 8 ; i++ ) {      
		//			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		//		}

		for (int i=0; i <= 7 ; i++ ) {
			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		}


		// skapar nya JScrollPane och l�gger till tabellerna via construktorn. F�r att kunna scrolla tabellerna.

		scrollPane = new JScrollPane(table);
		//		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoResizeMode(JTable. 	AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		//	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumn column = null;
		//		column = table.getColumnModel().getColumn(0);
		//		column.setPreferredWidth(30);
		//		column.setMaxWidth(85);
		//		column = table.getColumnModel().getColumn(1);
		//		column.setPreferredWidth(400);
		//		column.setMaxWidth(1100);
		//		column = table.getColumnModel().getColumn(2);
		//		column.setPreferredWidth(30);
		//		column.setMaxWidth(65);
		//		column = table.getColumnModel().getColumn(3);
		//		column.setPreferredWidth(30);
		//		column.setMaxWidth(65);
		//		column = table.getColumnModel().getColumn(4);
		//		column.setPreferredWidth(255);
		//		column.setMaxWidth(895);
		//		column = table.getColumnModel().getColumn(5);
		//		column.setPreferredWidth(255);
		//		column.setMaxWidth(895);
		//		column = table.getColumnModel().getColumn(6);
		//		column.setPreferredWidth(100);
		//		column.setMaxWidth(420);
		//		column = table.getColumnModel().getColumn(7);
		//		column.setPreferredWidth(900);
		//		column.setMaxWidth(2800);
		//		column = table.getColumnModel().getColumn(8);
		//		column.setPreferredWidth(100);
		//		column.setMaxWidth(950);

		column = table.getColumnModel().getColumn(0);
		column.setPreferredWidth(400);
		column.setMaxWidth(1100);
		column = table.getColumnModel().getColumn(1);
		column.setPreferredWidth(30);
		column.setMaxWidth(65);
		column = table.getColumnModel().getColumn(2);
		column.setPreferredWidth(30);
		column.setMaxWidth(65);
		column = table.getColumnModel().getColumn(3);
		column.setPreferredWidth(255);
		column.setMaxWidth(895);
		column = table.getColumnModel().getColumn(4);
		column.setPreferredWidth(255);
		column.setMaxWidth(895);
		column = table.getColumnModel().getColumn(5);
		column.setPreferredWidth(100);
		column.setMaxWidth(420);
		column = table.getColumnModel().getColumn(6);
		column.setPreferredWidth(900);
		column.setMaxWidth(2800);
		column = table.getColumnModel().getColumn(7);
		column.setPreferredWidth(100);
		column.setMaxWidth(950);

		addKeyBindings();

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// skapar tv� nya JPanel att anv�ndas inuti topPanel, som ocks� �r en JPanel
		//	        usrPanel = new JPanel();
		//	        usrPanel.setLayout(new BorderLayout());
		//	        logPanel = new JPanel();
		//	        logPanel.setLayout(new BorderLayout());
		// talar om f�r de nya JPanels vilka scrollPanes dom ska inneh�lla (scrollPanes inneh�ller tabellerna).
		//	        usrPanel.add(scrollPane, BorderLayout.CENTER);
		topPanel.add(scrollPane, BorderLayout.CENTER);
		// talar om f�r topPanel att den ska inneh�lla tv� JPanelobjekt NORTH och CENTER       
		//	        usrPanel.add(bu1, BorderLayout.NORTH);
		topPanel.add(bu1, BorderLayout.NORTH);
		//	        topPanel.add(usrPanel, BorderLayout.NORTH);
		//	        topPanel.add(logPanel, BorderLayout.CENTER);
		// talar om f�r innevarande object att den lyssnar p� sig sj�lv. (metoderna f�r WindowListener)
		addWindowListener(this);

		Timer timer = new Timer(2500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//	              button.setBackground(flag ? Color.green : Color.yellow);
				//	              flag = !flag;
				if (deselectCount > 10 ) {
					table.getSelectionModel().clearSelection();  // clear selected rows.
					deselectCount = 0;
				}
				deselectCount++;
				if (swAuto) {
					jvconnectCount++;
					if (jvconnectCount > 5 || !swServer) {    // keep the number of connections down because of limitations in Win10
						jvconnectCount = 0;
						try {
							swServer = true;
							SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer �r tillg�nglig.
							String oSts = jm.open();
							//						System.out.println("#1 "+oSts);
							if (oSts.startsWith("failed")) 	swServer  = false;
							if (oSts.startsWith("DORMANT")) swDormant = true;
							else 							swDormant = false;
							jm.close();
						} 
						catch (IOException e1) {
							swServer = false;
							System.err.println(e1);
							//						System.err.println(e1.getMessage());
						}
						catch (NullPointerException npe2 )   {
							swServer = false;
							System.out.println("-- Rpt Failed --" + npe2);
						}
//						System.out.println("swServer 2 : " + swServer);
					}

					swDBopen = wD.refreshData();
					//	            	if (!swDBopen) {
					setBu1Color();
					//	            	}
					//	            	table.repaint();
					//	            	
					if (swRed) scrollPane.setBorder(new LineBorder(Color.RED));
					else scrollPane.setBorder(new LineBorder(Color.CYAN));
					swRed = !swRed;
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					scrollPane.validate();
					scrollPane.repaint();
					//	            	scrollPane.setAutoscrolls(true);
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

					//	            	topPanel.repaint();
					//	            	pack();
					revalidate();
					repaint();	            
				}
			}
		});
		timer.start();

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

	public void setBu1Color()  {
		String txt = "";
		if (swAuto) {
			bu1.setBackground(Color.GRAY);
			//			bu1.setBackground(Color.LIGHT_GRAY);
			txt = "Auto Update ON.";
			//	            bu1.setText("Auto Update ON");
		}
		else {
			bu1.setBackground(Color.yellow);
			txt = "Auto Update OFF.";
			//		              bu1.setText("Auto Update OFF");
		}
		if (!swDBopen) {
			bu1.setBackground(Color.RED);
			//    		if (swAuto) bu1.setText("No connection with DB. Autoupdate ON");
			//    		else 		bu1.setText("No connection with DB. Autoupdate OFF");
			txt = txt + "  No connection with DB. ";

			//    		swAuto = false;
		}
		if (!swServer) {
			bu1.setBackground(Color.RED);
			txt = txt + "  No connection with JvaktServer. ";
		}
		else if (swDormant) txt = txt + "  System DORMANT.";
		else txt = txt +  "  System ACTIVE.";

		bu1.setText(txt);
	}

	private void addKeyBindings() {
		table.getActionMap().put("delRow", delRow());
		table.getActionMap().put("strHst", strHst());
		table.getActionMap().put("strSts", strSts());
		table.getActionMap().put("clearSel", clearSel());
		table.getActionMap().put("increaseH", increaseH());
		table.getActionMap().put("decreaseH", decreaseH());
		table.getActionMap().put("showHelp", showHelp());
		table.getActionMap().put("showLine", showLine());
		table.getActionMap().put("toggleDormant", toggleDormant());
		
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);  // delete key in mac
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "delRow");

		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);  // delete key in win linux
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "delRow");

		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showHelp");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showHelp");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "increaseH");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "decreaseH");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strSts");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showLine");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "toggleDormant");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "delRow");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE , 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "clearSel");

	}  

	private AbstractAction showHelp()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				System.out.println("ShowHelp");
				JOptionPane.showMessageDialog(getContentPane(),
						"F1 : Help \nF3 : Increase font size \nF4 : Decrease font size \nF5 : History \nF6 : Status table \nF7 : Show line \nF8 : Toggle dormant \n\nESC : Unselect \nDEL : delete selected rows.",
						"Jvakt Help",
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
		return save;
	}

	private AbstractAction showLine()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				System.out.println("ShowLine");
				table.editingCanceled(null);
				table.editingStopped(null);
				int[] selectedRow = table.getSelectedRows();

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
						System.out.println("*** Row to show :" + selectedRow[i]);
						Object ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Id"));
						System.out.println(ValueId);
						String id = (String) ValueId;
						if (id == null) continue;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Prio"));
						System.out.println(ValueId);
						int prio = (Integer) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Type"));
						System.out.println(ValueId);
						String type = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("CreDate"));
						System.out.println(ValueId);
						String credate = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("ConDate"));
						System.out.println(ValueId);
						String condate = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Status"));
						System.out.println(ValueId);
						String status = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Body"));
						System.out.println(ValueId);
						String body = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Agent"));
						System.out.println(ValueId);
						String agent = (String) ValueId;
						JOptionPane.showMessageDialog(getContentPane(),
								"- ID (the unique id if the message) -\n"+id+" \n\n" +
										"- Prio (the priority. Below 30 trigger email and SMS text) -\n"+prio +"\n\n" + 
										"- Type (R=repeated, S= scheduled and I=immediate/impromptu) -\n"+type +"\n\n" + 
										"- CreDate (the date it appeared in the console) -\n"+credate +"\n\n" + 
										"- ConDate (the date it updated in the console) -\n"+condate +"\n\n" + 
										"- Status (OK, INFO, TOut or ERR) -\n"+status +"\n\n" + 
										"- Body (any text) -\n"+body +"\n\n" + 
										"- Agent (description of the reporting agent) -\n"+agent  
										,						
										"Jvakt Show line",
										JOptionPane.INFORMATION_MESSAGE);
					}
				} 
				catch (Exception e2) {
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
				table.getSelectionModel().clearSelection();  // clear selected rows.				

			}
		};
		return save;
	}

	private AbstractAction clearSel()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}

	private AbstractAction increaseH()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				if (table.getRowHeight()<100) {
					table.setRowHeight(table.getRowHeight()+1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
			}
		};
		return save;
	}

	private AbstractAction decreaseH()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				//				System.out.println("getRowHeight :" + table.getRowHeight());
				if (table.getRowHeight()>10) {
					table.setRowHeight(table.getRowHeight()-1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
			}
		};
		return save;
	}


	private AbstractAction delRow()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				//	                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				table.editingCanceled(null);
				table.editingStopped(null);
				//				int selectedRow = table.getSelectedRow();
				int[] selectedRow = table.getSelectedRows();

				//				for (int i = 0; i <  selectedRow.length; i++) {
				//					System.out.println("*** Row do delete :" + selectedRow[i]);
				//				}

				//	                 if (selectedRow != -1) {
				//	                     ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
				//	                 }

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
						System.out.println("*** Row do delete :" + selectedRow[i]);
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, port);
						System.out.println(jm.open());
						Object ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Id"));
						System.out.println(ValueId);
						jmsg.setId(ValueId.toString());
						jmsg.setRptsts("OK");
						//						jmsg.setBody("Delete of row from GUI");
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Body"));
						System.out.println(ValueId);
						jmsg.setBody(ValueId.toString());
						//						jmsg.setBody("Delete of row from GUI");
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Prio"));
						System.out.println(ValueId);
						jmsg.setPrio(Integer.parseInt(ValueId.toString()));
						jmsg.setType("D");
						jmsg.setAgent("GUI");
						//						jm.sendMsg(jmsg);
						if (jm.sendMsg(jmsg)) System.out.println("-- Rpt Delivered --");
						else            	  System.out.println("-- Rpt Failed --");
						jm.close();
					}
				} 
				catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}
				catch (Exception e2) {
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}
	
	private AbstractAction toggleDormant()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {

				try {
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, port);
						System.out.println(jm.open());
						jmsg.setId("Jvakt");
						if (swDormant) jmsg.setType("Active");
						else jmsg.setType("Dormant");
						jmsg.setAgent("GUI");
						if (jm.sendMsg(jmsg)) System.out.println("-- Rpt Delivered --");
						else            	  System.out.println("-- Rpt Failed --");
						jm.close();
				} 
				catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}
				catch (Exception e2) {
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
			}
		};
		return save;
	}

	//************
	private AbstractAction strHst()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				System.out.println("-- Start consoleHst: " + cmdHst);

				try {
					//			       Runtime.getRuntime().exec("java -cp \"/Users/septpadm/OneDrive - Perstorp Group/JavaSrc;/Users/septpadm/OneDrive - Perstorp Group/JavaSrc/postgresql-42.1.3.jar\" Jvakt.consoleHst");
					Runtime.getRuntime().exec(cmdHst);
				} catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}

				//			       String[] par = new String[] { "One", "Two", "Three" };
				//			       try {
				//			    	   consoleHst.main(par);
				//			       } catch (IOException e1) {
				//			    	   System.err.println(e1);
				//			    	   System.err.println(e1.getMessage());
				//			       }

				//			       new Thread() {
				//			    	   public void run(){
				//			    		   String[] par = new String[] { "One", "Two", "Three" };
				//			    		   try {
				//			    			   consoleHst.main(par);
				//			    		   } 		catch (IOException e1) {
				//			    			   System.err.println(e1);
				//			    			   System.err.println(e1.getMessage());
				//			    		   }
				//
				//			    	   }
				//			       }.start();

			}
		};
		return save;
	}
	//	************
	//************
	private AbstractAction strSts()  {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)  {
				System.out.println("-- Start consoleSts: " + cmdSts);

				try {
					//			       Runtime.getRuntime().exec("java -cp \"/Users/septpadm/OneDrive - Perstorp Group/JavaSrc;/Users/septpadm/OneDrive - Perstorp Group/JavaSrc/postgresql-42.1.3.jar\" Jvakt.consoleHst");
					Runtime.getRuntime().exec(cmdSts);
				} catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}

			}
		};
		return save;
	}
	//	************


	// windows listeners
	// vi implementerade WindowListener och addade "this" f�r att denna metod skulle anropas vid normalt avslut av Jframe 
	public void windowClosing(WindowEvent e) {
		//skriv userDB
		wD.closeDB();
		System.exit(0);
		// ...och h�r �r det slut i rutan..!!!... 
	}

	void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("console.properties");
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			cmdHst   = prop.getProperty("cmdHst");
			cmdSts   = prop.getProperty("cmdSts");
			input.close();
		} catch (IOException ex) {
			// ex.printStackTrace();
		}    	
	}


	// vi implementerade WindowListener men f�ljande metoder av�nds inte 
	public void windowClosed(WindowEvent e) {    }
	public void windowOpened(WindowEvent e) {    }
	public void windowIconified(WindowEvent e) {    }
	public void windowDeiconified(WindowEvent e) {    }
	public void windowActivated(WindowEvent e) {    }
	public void windowDeactivated(WindowEvent e) {    }

}
