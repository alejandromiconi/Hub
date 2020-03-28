package hub.contents;

import java.sql.SQLException;

import org.w3c.dom.Element;

public class SaveDb extends Concentra {

	String table ; 
	
	public SaveDb(Element e) {
		
		super(e);
		
		this.table = e.getAttribute("table");
		
		this.connectDb(e); // and truncate
	}

	String lotColumn , list;
	
	public boolean process() {
		// TODO Auto-generated method stub

		try {

			
			this.getCurrent(false);
			this.getConsolidations();
	        
			saveDataToDB(this.table);
	        
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			this.logErrorNotOpen();
			
			return false;
		}
		
		return true;
	}
}
