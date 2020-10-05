package Jvakt;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class monHttps {

	static boolean state = false;
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static long ts;
	static boolean swFound;
	static boolean swSingle = false;
	static boolean swShow = false;
	static boolean swExpire = false;
	static String expire;
	static String host;
	static String hosturl;
	static String tabbar = "                                                                                               ";
	static InetAddress inet;
	static String version = "monHttps (2020-09-10)";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 443 ;
	static String agent = null;
	static String webfile = "";
	static String webcontent = "400";
	static Date now;

	static String config = null;
	static File configF;

	public static void main(String[] args) throws UnknownHostException, IOException, Exception {

		String[] tab = new String [1];
		//		String tdat;
		String s;
		File[] listf;
		DirFilter df;
		File dir = new File(".");
		if (config != null ) dir = new File(config);
		String suf = ".csv";
		String pos = "monHttps-";
		boolean swRun = false;
		now = new Date();


		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("File names must contain monHttps- and end with .csv. e.g. monHttps-01.csv ");
			System.out.println("Row in the file example: ");
			System.out.println("wikipedia;sv.wikipedia.org;443;/wiki/Portal:Huvudsida;wikipedia;descriptive text");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run    \tTo actually update the status on the server side."+
					"\n-host   \tCheck a single host." +
					"\n-port   \tDefault is 443." +
					"\n-web    \tlike /index.html" +
					"\n-webcontent \tstring in the response to check for." +
					"\n-show   \tShow the response from the server."
					);

			System.exit(4);
		}

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}
		};

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			//			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-port")) wport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-web")) webfile = args[++i];
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-show")) swShow = true;
			if (args[i].equalsIgnoreCase("-host")) { swSingle = true; host = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-webcontent")) webcontent = args[++i];
		}
		if (config != null ) dir = new File(config);
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");


		System.out.println("\n"+now+" *** Jvakt "+version+" ***\n");
		if (swShow)	System.out.println(" config file: "+configF);

		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");
		if (swShow)	{
			System.out.println(" Dir : "+dir);
			System.out.println(" Suf : "+suf);
			System.out.println(" Pos : "+pos);
			System.out.println(" Host: "+host+"\n");
		}

		if (swSingle) {
			checkHttp();
		} else {

			//			if (pos != null) df = new DirFilter(suf, pos);
			//			else             df = new DirFilter(suf);

			df = new DirFilter(suf, pos);

			listf = dir.listFiles(df);

			if (swShow)	System.out.println("-- Number of files found:"+ listf.length);

			for (int i = 0; i < listf.length; i++) {

				if (swShow)	System.out.println("-- Checking: "+listf[i]+"\n");

				BufferedReader in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					// splittar rad från fil
					tab = s.split(";" , 6);
					t_id = tab[0];
					host = tab[1];
					wport = Integer.parseInt(tab[2]);
					webfile = tab[3];
					webcontent = tab[4];
					t_desc = tab[5];
					state = false;

					checkHttp();

					if (swRun)  {
						if (state) 	sendSTS(true);
						else 		sendSTS(false);
					}

				}
				in.close();
			}
		}
	}

	public static boolean checkHttp() {
		// First set the default cookie manager.
		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		// connect to port
		try {
			hosturl ="https://"+host+":"+wport+webfile;
			if (swShow)	System.out.println("-- URL    : https://"+host+":"+wport+webfile);
			if (swShow)	System.out.println("-- OK text: " +webcontent);
			//			System.setProperty("https.protocols", "SSLv3");
			URL url = new URL("https://"+host+":"+wport+webfile); 
			//			URLConnection con = url.openConnection();  // new
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			if (swShow)	System.out.println("-- OK connection");
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			//			BufferedReader httpin = new BufferedReader(
			//					new InputStreamReader(url.openStream()));

			con.connect();

			swExpire = false;
			Certificate[] certs = con.getServerCertificates();
			for(Certificate c:certs){
				//System.out.println(c.getType());
				//System.out.println(c.toString());
				X509Certificate xc = (X509Certificate)c; // we should really check the type before doing this typecast..
				String dn = xc.getSubjectDN().getName();
				Date expiresOn= xc.getNotAfter();
				Date now = new Date();
				long days = (expiresOn.getTime()-now.getTime())/(1000*60*60*24);
				if (days < 10) { 
					expire = " ** Warning ** Certificate expires soon - "+expiresOn+" - "+dn ;
					if (swShow)	System.out.println(expire);
					swExpire = true;
				}
				if (swShow)	System.out.println(dn+"\n The certificate expires on "+expiresOn+".\n "+days+" days to go");
			}			


			if (swShow)	System.out.println("-- OK get in-stream");
			BufferedReader httpin = new BufferedReader(
					new InputStreamReader(con.getInputStream()));

			String inputLine;
			if (swShow)	System.out.println("-- start read lines");
			while ((inputLine = httpin.readLine()) != null  && !state) {
				if (swShow)	System.out.println(inputLine);
				if (inputLine.toLowerCase().indexOf(webcontent.toLowerCase()) >= 0) {
					state = true;
					if (swShow)	System.out.println("-- OK text found: "+ webcontent );
				}
			}
			httpin.close();



		} 
		catch (Exception e) { System.out.println(e); state = false;   }
		//		catch (UnknownHostException e) { System.out.println(e); state = false;   }
		//		catch (Exception e) { 
		//			System.out.println(e);
		//			if (e.toString().indexOf("403") > 0) state = true;
		//			else state = false;
		//		}

		//		try { Thread.currentThread(); Thread.sleep(1000); } catch (Exception e) {} ;

		if (t_desc == null) t_desc = " ";
		if (hosturl.length()>85) hosturl=hosturl.substring(0,85);
		hosturl = hosturl + tabbar.substring(0,85-hosturl.length());
		
		if (swExpire) {
			t_desc = expire +" - "+ t_desc;
			state = false;
		}

		if (state) {System.out.println(new Date()+" -- Connection successful - "+hosturl+t_desc); return true; }
		else 	   {System.out.println(new Date()+" -- Connection failed     - "+hosturl+t_desc); return false; }

		//		if (state) {System.out.println("Connection successful"); return true; }
		//		else 	   {System.out.println("Connection failed"); return false; }
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		if (swShow)	System.out.println(jm.open());
		else jm.open();
		jmsg.setId(t_id+"-monHttps-"+host);
		if (STS) jmsg.setRptsts("OK");
		else jmsg.setRptsts("ERR");
		if (swExpire) t_desc = expire +" - "+ t_desc;
		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		//		jm.sendMsg(jmsg);
		if (jm.sendMsg(jmsg));
		else            	  System.out.println("-- Rpt Failed --");
		jm.close();
	}

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			port = Integer.parseInt(jvport);
			if (swShow)	System.out.println(" jvport : " + jvport + "\n jvhost : "+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			if (swShow)	System.out.println(" Inet : "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}

}
