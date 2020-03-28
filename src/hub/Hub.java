package hub;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hub.commons.License;
import hub.commons.Log;
import hub.contents.API;
import hub.contents.Consolida;
import hub.contents.Content;
import hub.contents.Filter;
import hub.contents.LoadDb;
import hub.contents.LoadFiles;
import hub.contents.LoadFiles.Format;
import hub.contents.Mail;
import hub.contents.Page;
import hub.contents.SaveDb;
import hub.contents.SaveFile;

/**
 * @author maico
 * Main!
 */
public class Hub {
	
	public static boolean view = false;
	
	public static void main(String[] args) {
		
		License license = new License();

		for(int i = 0 ; i<args.length ; i++) {

			if (args[i].equals("-l")) {

				int months = Integer.parseInt(args[1]);
				int steps = Integer.parseInt(args[2]);
				
				license.save(months, steps);

				break;
			}
			
			else if (args[i].equals("-v")) {
				view = true;
			}

			else {
				new Hub(args[i]);
			}
		}

		license.view();
	}
	
	public static List<Content> contents;
	public static String nodename;

	public static final String App = "Hub";

	public static final String xmlField = "field";
	public static final String xmlConsolida = "consolida";
	public static final String xmlFilter = "filter";
	
	public static final String xmlLoadFiles = "loadfiles"; // OK
	public static final String xmlLoadJson = "loadjson";
	public static final String xmlLoadDb = "loaddatabase";

	public static final String xmlSaveFile = "savefile";
	public static final String xmlSaveJson = "savejson";
	public static final String xmlSaveDb = "savedatabase";

	public static final String xmlMail = "mail";
	public static final String xmlPage = "page";
	public static final String xmlAPI = "api";
	
	public static String lot;
	
	public boolean success = true;
	//public static List<Content> contents;
		

	public Hub(String path) {
		
		Log.logs = new ArrayList<Log>();

		LocalDateTime dt = LocalDateTime.now();
		lot = dt.toString().replaceAll("[:-]*", "").replace("." , "");
		Log.hub("Start processing lot " + lot);

		
		contents = new ArrayList<Content>();
		
		Consolida consolida = null;

		License license = new License();
		if (license.isExpired()) {
			Log.hub("License expired!");
		}
		
		else 
			
		try {

			File xml = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(xml);
			doc.getDocumentElement().normalize();
	
			Node root = doc.getElementsByTagName("hub").item(0);
			if (root == null) return;
			
			NodeList list = root.getChildNodes();
			
			int steps = 0;
			for ( int i = 0 ; i < list.getLength() ; i++ ) {
				
				Node node = list.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE) continue;
	
				
				steps++;
				
				if (license.steps>0 && steps>license.steps) {
					Log.error(App, "Steps exceed licensed (" + license.steps + ")");
					break;
				}
				
				Element e = (Element) node;

				nodename = e.getNodeName();
				
				switch(nodename.toLowerCase()) {
				
				case xmlLoadFiles:
					new LoadFiles(e).setContents(Format.TEXT);
					break;

				case xmlLoadJson:
					new LoadFiles(e).setContents(Format.JSON);
					break;

				case xmlLoadDb:
					new LoadDb(e).setContents();
					break;

				case xmlPage:
					new Page(e).setContents();
					break;
					
				case xmlAPI:
					new API(e).setContents();
					break;

				case xmlSaveFile:
					new SaveFile(e).process(Format.TEXT);
					break;
				
				case xmlSaveJson:
					new SaveFile(e).process(Format.JSON);
					break;

				case xmlSaveDb:
					new SaveDb(e).process();
					break;

				case xmlConsolida:
					consolida = new Consolida(e);
					consolida.setContents();
					break;
					
				case xmlFilter:
					new Filter(e).setContents(consolida);
					break;

				case xmlMail:
					new Mail(e).process();
					break;
				}
				
				nodename = null;
			}
			
			Page.checkDriver();

	
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			// TODO Auto-generated catch block
			//ex.printStackTrace();
			
			Log.error(App , ex.getMessage());
			
			this.success = false;
		}
		
		Log.hub("End processing");
		Log.print();
		
		if(Hub.view) {
			System.out.print(Log.getLog());
		}
	}
}
