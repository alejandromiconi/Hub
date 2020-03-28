package hub.contents;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Content {

	static final String fieldTypeDate = "date";
	static final String fieldTypeDateTime = "datetime";
	static final String fieldTypeNumber = "number";

	static final String dot = ".";

	Props props;
	JSONArray data;
	private List<String> names; // Es lo que viene en el archivo

	/**
	 * Los contents están capturados directamente sin ningún procesamiento especial
	 * Tienen las propiedades con las cuales fueron capturados, aunque debieran tener
	 * su especificación global
	 * Cuando hago genero un secundario, puedo tener su 
	 * especificación global y particular (columns)
	 */
	protected Content() {
		this.data = new JSONArray();
		this.names = new ArrayList<String>();
	}

	String getName(int i) {

		if (i > maxNames)
			maxNames = i;

		if (i < names.size()) {
			return names.get(i);
		}

		else {
			return "Name" + i;
		}
	}

	int maxNames = -1;

	void addName(String text) {

		this.names.add(text);

		if (maxNames == -1)
			maxNames = 1;
		else
			maxNames++;
	}

	void setNames(List<String> names) {

		this.names = names;
		this.maxNames = names.size();
	}

	@SuppressWarnings("unchecked")
	List<Column> getColumns() {

		List<Column> columns = new ArrayList<Column>();

		if (this.props.columns.size() != 0) {
			return props.columns;
		}

		// Probamos desde los datos

		List<String> names = new ArrayList<String>();

		if (this.data.length() > 0) {

			Iterator<String> keys;
			try {

				keys = this.data.getJSONObject(0).keys();

				while (keys.hasNext()) {
					names.add(keys.next());
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (names.size() == 0) {
			names = this.names;
		}

		for (String name : names) {

			Column column = new Column(name);
			columns.add(column);
		}

		return columns;
	}

	@SuppressWarnings("unchecked")
	JSONObject getSizes() {

		JSONObject sizes = new JSONObject();

		try {

			for (int k = 0; k < this.data.length(); k++) {

				JSONObject o = this.data.getJSONObject(k);

				Iterator<String> keys = o.keys();

				while (keys.hasNext()) {

					String key = keys.next();

					int next = o.getString(key).length();

					if (!sizes.has(key)) {
						sizes.put(key, next);
					}

					else {

						int save = sizes.getInt(key);

						if (next > save) {
							sizes.put(key, next);
						}
					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}

		return sizes;
	}

	void addLine(List<String> values) {

		JSONObject o = new JSONObject();

		try {

			for (int i = 0; i < values.size(); i++) {
				o.put(getName(i), values.get(i));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.data.put(o);
	}

	void addLineColumn(int line, String value) {

		try {

			JSONObject o = this.data.getJSONObject(line);
			o.put(getName(o.length()), value);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	// Suma un contenido al actual
	boolean add(Content from, boolean TBD) {

		if (this == from)
			return true;

		if (this.props.columns.size() == 0) {
			this.props.copy(from.props);
		}
		
		// Columns del destino
		List<Column> columns = this.getColumns(); 
		
		for (int k = 0; k < from.data.length(); k++) {

			JSONObject o;

			try {

				o = from.data.getJSONObject(k);

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();

				continue;
			}

			// Si no tiene especificación, la copia tal cual porque no tiene elementos
			if (columns.size() == 0) {
				this.data.put(o); 
			}

			else {

				JSONObject ob = new JSONObject();
				for (Column column : columns) {

					String value = column.formula;

					try {

						Iterator<String> keys = o.keys();
						while (keys.hasNext()) {

							String key = keys.next();
							
							Object vo = o.get(key);
							value = value.replace(Props.getFormula(key), String.valueOf(vo));
						}

						value = this.props.replace(value);

						switch (column.type) {

						case fieldTypeDate:

							SimpleDateFormat fde = new SimpleDateFormat(from.props.dateformat);
							Date date = fde.parse(value);

							SimpleDateFormat tde = new SimpleDateFormat(props.dateformat);
							value = tde.format(date);
							break;

						case fieldTypeDateTime:

							SimpleDateFormat fdt = new SimpleDateFormat(from.props.datetimeformat);
							Date dt = fdt.parse(value);

							SimpleDateFormat tdt = new SimpleDateFormat(props.datetimeformat);
							value = tdt.format(dt);
							break;

						case fieldTypeNumber:

							if (value.isEmpty())
								value = "0";

							double pf = 0;

							// 1) Llevamos el value a "float" (el dot es el caracter que espera)
							if (from.props.decimalpoint.isEmpty() || from.props.decimalpoint.equals(dot))
								;

							else {
								value = value.replace(dot, "");
								value = value.replace(from.props.decimalpoint, dot);
							}

							pf = Double.parseDouble(value); // Float.parseFloat(value);

							value = String.valueOf(pf);

							// 2) Lo formateamos de dot, a lo que espera la salida
							if (props.decimalpoint.isEmpty() || props.decimalpoint.equals(dot))
								;

							else {
								value = value.replace(dot, props.decimalpoint);
							}

							break;
						}

						ob.put(column.internal, value);

					} catch (ParseException | JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				this.data.put(ob);
			}
		}

		return true;
	}
	
	public boolean isNumeric(String key) {
		return this.getColumnType(key).equals(Content.fieldTypeNumber);
	}
	
	public String getColumnType(String key) {
		
		Optional<Column> column = this.props.columns.stream()
				.filter(x -> x.internal.equals(key)).findFirst();


		if (column.isPresent()) {
			return column.get().type;
		}
		
		return "";
	}



}