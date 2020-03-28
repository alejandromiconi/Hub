package hub.contents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Element;

import hub.commons.Commons;

public class LoadFiles extends Concentra {

	public enum Format { JSON , TEXT };
	
	
	public String moveTo, specs, separator;

	public LoadFiles(Element e) {
		
		super(e);
		
		this.specs = e.getAttribute("specs");
		this.separator = e.getAttribute("separator");
		this.moveTo = e.getAttribute("moveto");
	}
	
	public boolean setContents(Format format) {
		
		boolean success = true;
		
		List<String> files = new ArrayList<String>();

		File[] list = new File(this.props.url).listFiles();
		if (list != null) {
			
			for (File file : list) {

				String name = file.getName();

				boolean found = false;
				
				if (this.specs.isEmpty()) {
					found = true;
				}
				
				else {

					String [] specs = this.specs.split(",");

					for(int k = 0; k<specs.length ; k++) {
						String spec = specs[k].replace(".","\\.").replace("*",".*");
						if (name.matches(spec)) found = true;
					}
				}
				
				if(found) {
					files.add(name);
				}
			}
		}
		
		for(String f : files) {
			
			String path = this.props.url + "/" + f;

			this.getCurrent(true, path);
			
			if (readFile(format)) {

				this.logProcessFile(current.data.length());
				
				if (!this.moveTo.isEmpty()) {
					
					String ext = f.substring(f.lastIndexOf("."));
					String fn = f.substring(0, f.lastIndexOf("."));
	
					String filename = this.moveTo + "/" + this.props.lot + "_" + fn + ext;
					
					new File(this.moveTo).mkdir();
					
					File fi = new File(path);
					fi.renameTo(new File(filename));

					this.logMessage("Moved to " + this.moveTo);
				}
			}
			
			else {

				this.logErrorMessage(lastError);
				
				success = false;
			}
		}
		
		
		return success;
	}
	
	boolean readFile(Format format) {

		// Read file!
		BufferedReader reader;
		
		lastError = "";
		
		try {
		
			reader = new BufferedReader(new FileReader(current.props.url));
			
			switch (format) {
			
			case JSON:
				current.data = new JSONArray(reader.read());
				break;

			case TEXT:

				String line;
				
				boolean first = true;
		    	while ((line = reader.readLine()) != null) {

		    		List<String> tokens = Commons.getTokens(line, separator);
		    		
		    		if (first) {

	    				first = false;

		    			if (current.props.hasTitles) {
		    				current.setNames(tokens);
		    				continue;
		    			}
		    		}

		    		current.addLine(tokens);
				}
				
				break;
			}

			reader.close();
		
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			lastError = e.getMessage();

			return false;
		}

		return true;
	}
}
