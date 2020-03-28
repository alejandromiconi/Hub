package hub.contents;

import java.sql.SQLException;
import org.w3c.dom.Element;

public class Filter extends Concentra {

	String sql;
	
	public Filter(Element e) {
		
		super(e);

		this.sql = e.getTextContent();
	}

	public boolean setContents(Consolida con) {

		sql = sql.replace("{consolida}", con.table);
		
		con.reconnectDb(false);
		
		this.conn = con.conn;
		
		try {
		
			getCurrent(false);
			getDataFromDB(sql);
			conn.close();

		} catch (SQLException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			
			return false;
		}
		
		return true;
	}
}