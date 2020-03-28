/**
* <h1>Hello, World!</h1>
* The HelloWorld program implements an application that
* simply displays "Hello World!" to the standard output.
* <p>
* Giving proper comments in your program makes it more
* user friendly and it is assumed as a high quality code.
* 
*
* @author  Zara Ali
* @version 1.0
* @since   2014-03-31 
*/


package hub.contents;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import hub.Hub;
import hub.commons.Log;

/**
 * @author maico
 *
 */
/**
 * @author maico
 *
 */
public abstract class Concentra {
	
	Props props;

	protected Concentra() {
		this.props = new Props();
	}
	
	protected Concentra(Element e) {
		
		this();
		
		this.props = new Props(e);
	}

	
	Content current;
	String lastError;
	
	
	protected Content getCurrent(boolean primary) {

		this.current = new Content();
		current.props = this.props.clone();
		current.props.primary = primary;
		Hub.contents.add(current);
		
		return this.current;
	}

	protected Content getCurrent(boolean primary, String url) {
		
		getCurrent(primary);
		current.props.url = url;
		return current;
	}

	
	/**
	 * Consolida los contenidos respetando los formatos de origen y destino
	 * Debemos venir con el current ya definido!
	 * Si hay está nominado, toma el nominado, sino toma el CONSOLIDA, sino toma los primarios
	 * @return
	 */
	protected void getConsolidations() {

		if (this.props.contentId.isEmpty()) {
				
			// Levanta todos los PRIMARY
			for(Content content : Hub.contents) {
				if (content.props.primary) {
					current.add(content, false);
				}
			}			
		}
		
		// Nominados, levanta las columns del mismo primary
		else {
		
			Optional<Content> content = Hub.contents.stream()
					.filter(x -> x.props.id.equals(this.props.contentId)).findFirst();
			
			if (content.isPresent()) {
				current.add(content.get(), true);
			}
		}
	}
	
	Connection conn;
	
	String url, forname, username, password, truncate; 
	protected boolean connectDb(Element e) {
		
		forname = e.getAttribute("forname");
		username = e.getAttribute("username");
		password = e.getAttribute("password");
		url = e.getAttribute("url");
		truncate = e.getAttribute("truncate");

		return reconnectDb(!truncate.isEmpty());
	}

	protected boolean reconnectDb(boolean trunc) {
		
		try {

			Class.forName(forname);
			
	        // specify url, username, pasword - make sure these are valid 
			this.conn = DriverManager.getConnection(url, username, password);
		
	        if (trunc) {
	
	        	this.truncate = this.props.replace(this.truncate);
	        	
				PreparedStatement ps = conn.prepareStatement(truncate);
				ps.executeUpdate();
				
	        	this.logTruncate(truncate);
	        }
			
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
			this.logErrorMessage(e1.getMessage());
			
			return false;
		}
		
		return true;
	}
	
	
	protected void getDataFromDB(String select) {
		
		try {

			Statement st = conn.createStatement();

			ResultSet rs = st.executeQuery(select);

			ResultSetMetaData rsm = rs.getMetaData();
	
			List<String> names = new ArrayList<String>();
			for (int i = 0; i < rsm.getColumnCount(); i++) {
				names.add(rsm.getColumnName(i + 1));
			}
	
			current.setNames(names);
			
			while (rs.next()) {
	
				JSONObject o = new JSONObject();
				
				for (int i = 0; i < rsm.getColumnCount(); i++) {
	
					int f = i + 1;
					
					o.put(rsm.getColumnName(f), rs.getObject(f));
				}
				
				current.data.put(o);
			}

		} catch (SQLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveJsonFile(String filename) {

		FileWriter fw;

		try {

			fw = new FileWriter(filename, true);
			fw.write(current.data.toString());
			fw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveTextFile(String filename) {
		
		FileWriter fw;
		String text;

		try {
		
			fw = new FileWriter(filename, true);

			for (int k = 0 ; k < current.data.length() ; k++) {

				JSONObject o;

				o = current.data.getJSONObject(k);

				Iterator<String> keys = o.keys();
	
				if (k == 0) {
	
					text = "";
		        	while(keys.hasNext()) {
		    			String key = keys.next();
						text += (text.isEmpty() ? "" : this.props.separator) + key;
		        	}
	
		        	fw.write(text + "\n");
				}
	
				keys = o.keys();
	
				text = "";
	        	while(keys.hasNext()) {
	
	    			String key = keys.next();
	    			Object vo = o.get(key);
	    			
	    			String value = String.valueOf(vo);


	    			String format = "\"" + value + "\"";

	    			if (current.isNumeric(key)) {
	    				format = value;
	    			}
	    			
	    			
					text += (text.isEmpty() ? "" : this.props.separator) + format;
	        	}
	
				fw.write(text + "\n");
			}

			fw.close();

		} catch (IOException | JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		
	}


	@SuppressWarnings("unchecked")
	protected boolean saveDataToDB(String table) {
		
		String columns = "";
		String records = "";
		
		long process = 0;
		
		for (int k = 0 ; k < current.data.length() ; k++) {

			lastError = "";

			
			try {
				

				JSONObject o = current.data.getJSONObject(k);
				
				Iterator<String> keys = o.keys();
	
				if (k == 0) {
					
		        	while(keys.hasNext()) {
		    			String key = keys.next();
		    			columns += ( columns.isEmpty() ? "" : " , ") + key;
		        	}
				}
		
				keys = o.keys();
	
				String record = "";
	        	while(keys.hasNext()) {
	
	    			String key = keys.next();
	    			String value = o.getString(key);
					record += ( record.isEmpty() ? "" : ", ") + ( value == null ? "NULL" : "'" + value + "'" );
	        	}
	        	
				records += ( records.isEmpty() ? "" : ", ") + " ( " + record + " ) ";
					
				if (k % 100==0 || k == current.data.length()-1) {
	
					String sql = "insert into " + table + " (" + columns + ") values " + records;
	
					PreparedStatement ps;
					
						ps = this.conn.prepareStatement(sql);
						ps.executeUpdate();
	
	
					records = "";
				}
				
				process++;

			} catch (SQLException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				lastError = e.getMessage();
			}
		}

		
		if (!lastError.isEmpty()) {
			this.logErrorMessage(lastError);
		}
		
		this.logProcessFile(process);

		return true;
	}

	String getUrl() {
		String url = current == null ? this.props.url : current.props.url;
		if (url.isEmpty()) url = "N/A";
		return url;
	}
	
	// Errors!
	protected void logErrorNotOpen() {
		Log.error(getUrl(), "Opening file");
	}

	protected void logErrorMessage(String message) {
		Log.error(getUrl(), message);
	}

	// Information!
	protected void logMessage(String message) {
		Log.warning(getUrl(), message);
	}
	
	protected void logProcessFile(long records) {
		Log.warning(getUrl(), "File processed with " + records + " records");
	}
	
	protected void logTruncate(String truncate) {
		Log.warning(getUrl(), "Truncate with " + truncate);
	}
}