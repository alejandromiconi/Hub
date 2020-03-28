package hub.contents;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

public class Consolida extends Concentra {

	String table;
	
	public Consolida(Element e) {
		
		super(e);

		this.getCurrent(false, "Consolida");
		this.getConsolidations();
		
		this.connectDb(e);
		
		this.table = e.getAttribute("table");
	}

	@SuppressWarnings("unchecked")
	public boolean setContents() {

		try {

			if (this.conn == null) return false;
			
			if (table.isEmpty()) {
				table = "consolida_" + this.props.lot;
			}
			
			PreparedStatement ps = this.conn.prepareStatement("DROP TABLE IF EXISTS " + table);
			ps.executeUpdate();

			
	        // ********************** Definimos las extensiones máximas
	        JSONObject sizes = this.current.getSizes();

	        // ********************** Creamos la tabla
	        String sql = "";
	        
			Iterator<String> keys = sizes.keys();

        	while(keys.hasNext()) {

    			String key = keys.next();

        		String type = "";
        		switch(current.getColumnType(key)) {
        		
        		case Content.fieldTypeDate:

        			type = "DATE";
        			break;
        			
        		case Content.fieldTypeDateTime:
        			
        			type = "DATETIME";
        			break;
        			
        		case Content.fieldTypeNumber:
        			
        			type = "FLOAT";
        			break;
        			
        		default:
        			
            		int ext = sizes.getInt(key);

        			type = "VARCHAR(" + ext + ")";

        			break;
        		
        		
        		}
        		
    			sql += (sql.isEmpty() ? "" : ", ") + key + " " + type + " NULL";
	        }

        	sql = "CREATE TABLE " + table + " ( " + sql + " ) ENGINE = InnoDB";

			ps = this.conn.prepareStatement(sql);
			ps.executeUpdate();
			
			this.logMessage("CREATE TABLE " + table);
			
	        // ********************** Cargamos los datos
			this.saveDataToDB(table);
	        
	        this.conn.close();
	        
		} catch (SQLException | JSONException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			
			this.logErrorMessage(ex.getMessage());
			
			this.conn = null;
			
			return false;
		}
		
		return true;
	}
}