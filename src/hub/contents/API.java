package hub.contents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

public class API extends Concentra {

	String method, authorization;
	
	public API(Element e) {
		
		super(e);
		
		//this.url = e.getAttribute("url");
		this.method = e.getAttribute("method");
		this.authorization = e.getAttribute("authorization");
		
	}
	
	public boolean setContents() {

		int records = 0;
		
		try {
			
	        URL url = new URL(this.props.url);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        
	        if (!this.authorization.isEmpty()) {
	        	conn.addRequestProperty("Authorization", this.authorization);
	        }
	        
	        conn.setRequestMethod(this.method);
	        //conn.setRequestProperty("Accept", "application/json");
	        
	        if (conn.getResponseCode() != 200) {

	        	String m = "Failed : HTTP Error code : " + conn.getResponseCode();
	        	this.logErrorMessage(m);
	        	return false;
	        }
	        
	        
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());
	        BufferedReader br = new BufferedReader(in);

	        String output;
	        
	        
	        this.getCurrent(true);
	        
	        boolean first = true;
	        while ((output = br.readLine()) != null) {

	        	// Es un array!
	        	if (output.startsWith("[")) {

	        		JSONArray a = new JSONArray(output);
	    
	        		for(int i = 0; i<a.length(); i++) {
	        			JSONObject o = a.getJSONObject(i);
	        			addLine(this.current, o, first);
	        			
	        			first = false;
	        			records++;
	        		}
	        	}
	        	
	        	else {

	        		JSONObject o = new JSONObject(output);
	        		addLine(this.current, o, first);

	        		first = false;
	        		
	        		records++;
	        	}
	        }

	        conn.disconnect();
	        
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			this.logErrorMessage(e.getMessage());
			
			return false;
		}


		this.logProcessFile(records);
		
		return true;
	        
	}
	
	void addLine(Content content, JSONObject o, boolean first) {

    	@SuppressWarnings("unchecked")
		Iterator<String> keys = o.keys();

    	List<String> values = new ArrayList<String>();
    	while(keys.hasNext()) {

			String key = keys.next();

			if (first) {
				content.addName(key);
        	}

        	String value = "";

        	try {

	        	Object ob = o.get(key);
	
	        	if (ob instanceof String) {
	        		
	        		value = o.getString(key);
	        	}
	        	
	        	else {
	        		
	        		value = String.valueOf(ob);
	        		
	        	}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				this.logErrorMessage("Field " + key + " " + e.getMessage());
				
			}
        	
			values.add(value);
		}
    	
    	content.addLine(values);
	}
}
