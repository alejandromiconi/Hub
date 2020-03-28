package hub.commons;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import hub.Hub;

public class Log {

	public static final String LogFile = "hub.log";
	
	public static List<Log> logs;
	
	boolean error;
	LocalDateTime dt;
	String node, url, message;
	long line;

	private Log(String url, String message, boolean error) {
		
		this.node = Hub.nodename == null ? Hub.App : Hub.nodename;
		this.dt = LocalDateTime.now();
		this.error = error;
		this.line = logs.size() + 1;
		this.url = url;
		this.message = message;

		logs.add(this);
	}

	public static void hub(String message) {
		new Log(null, message , false);
	}
	
	public static void error(String url, String message) {
		new Log(url, message, true);
	}
	
	public static void warning(String url, String message) {
		new Log(url, message, false);
	}
	
	public static boolean hasErrors() {

		for(Log m : logs) {
			if (m.error) return true;
		}

		return false;
	}
	
	public static String getLog(String end) {
		
		Collections.sort(logs , (a , b) -> a.line<b.line ? -1 : 1);

		String message = "";
		for(Log m : logs) {
			message += m.dt + " " 
					+ ( m.error ? "E" : "I" ) + " "
					+ m.node.toUpperCase() + "* " + ( m.url == null ? "" : m.url + " " ) 
					+ m.message + end;
		}
		
		return message;
	}
	
	public static String getLog() {
		return getLog("\n");
	}
	
	public static void print() {
		
		File log = new File(LogFile);

		try {

			FileWriter fw = new FileWriter(log, true);
			fw.write(getLog());
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
