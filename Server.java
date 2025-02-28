/**
 * This is the core listener. 
 * It will communicate with the agents and update the DB. 
 * @author 		Michael Ekdal
 * @version 	2019-04-29
 */
package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
 
public class Server {
	private static ServerSocket ss;

	/**
	 * The class listens to a port.
	 * For each connection a new thread is created to handle the session.
	 * A single instance of DBupdate is created to handle the DB connection and all updates. 
	 * @param  		port 	The port the program will listen on.
	 * @throws		exeption
	 */
	public static void main(String[] args ) throws Exception  {

		String version = "Server 1.3 # 2019-04-29";
		String jvport   = "1956";

		String config = null;
		File configF;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
 
		// the properties file is read to find which port to open 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("-config file Server: "+configF);
		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvport = prop.getProperty("jvport");
		} catch (IOException ex) {
			// ex.printStackTrace();
		}
		input.close();

		
		// Main loop
		int port = Integer.parseInt(jvport);
		ss = new ServerSocket(port);
		DBupdate dt = new DBupdate( args );
		while( true ) {
			try {
				Socket client = ss.accept();
				ServerThread t = new ServerThread(client, dt);
				t.start(); // Starting a new thread to handle each connection.
			}
			catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
