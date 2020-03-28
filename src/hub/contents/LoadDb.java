package hub.contents;

import java.sql.SQLException;

import org.w3c.dom.Element;

public class LoadDb extends Concentra {


	String select ; 
	
	public LoadDb(Element e) {
		
		super(e);
		
		//this.truncate = e.getAttribute("truncate");
		this.select = e.getAttribute("select");
		
		this.connectDb(e);
	}
	
	public boolean setContents() {

		try {
			
			this.getDataFromDB(this.select);
			this.conn.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
