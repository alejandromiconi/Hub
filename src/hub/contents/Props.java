package hub.contents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hub.Hub;

public class Props implements Cloneable {

	public String decimalpoint, datetimeformat, dateformat, separator, name, url, lot;
	public boolean hasTitles = false;
	public String id, contentId = "";
	public boolean primary;
	public List<Column> columns;

	@Override
	public Props clone() {

		try {

			final Props result = (Props) super.clone();
			// copy fields that need to be copied here!

			return result;

		} catch (final CloneNotSupportedException ex) {
			throw new AssertionError();
		}
	}

	public Props() {

		this.lot = Hub.lot;
		this.columns = new ArrayList<Column>();
		this.primary = true;
		this.dateformat = "yyyy-MM-dd";
		this.datetimeformat = "yyyy-MM-dd HH:mm:ss";
		this.decimalpoint = Content.dot;
		this.separator = ";";
		this.id = "";
	}

	public Props(Element e) {

		this();

		if (e.hasAttribute("dateformat"))
			this.dateformat = e.getAttribute("dateformat");
		if (e.hasAttribute("datetimeformat"))
			this.datetimeformat = e.getAttribute("datetimeformat");
		if (e.hasAttribute("decimalpoint"))
			this.decimalpoint = e.getAttribute("decimalpoint");
		if (e.hasAttribute("separator"))
			this.separator = e.getAttribute("separator");

		try {

			float sep = Float.parseFloat(this.separator);
			
			char s = (char) sep;
			this.separator = Character.toString(s);

		} catch (NumberFormatException ex) {

		}

		if (e.hasAttribute("hastitles"))
			this.hasTitles = e.getAttribute("hastitles").equals("true");

		this.contentId = e.getAttribute("content");
		this.id = e.getAttribute("id");
		this.name = e.getAttribute("name"); // Solo documental
		this.url = e.getAttribute("url");

		NodeList hs = e.getElementsByTagName("field");

		for (int j = 0; j < hs.getLength(); j++) {

			Node ph = hs.item(j);
			if (ph.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element eh = (Element) ph;

			this.columns.add(new Column(eh));
		}
	}

	public static String getFormula(String name) {
		return "{" + name + "}";
	}

	public static final String tagLotNumber = "tagLotNumber";
	public static final String tagUrl = "tagUrl";
	public static final String tagNow = "tagNow";

	public String replace(String value) {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(this.datetimeformat);
		String dts = sdf.format(cal.getTime());

		value = value.replace(Props.getFormula(tagLotNumber), this.lot);
		value = value.replace(Props.getFormula(tagUrl), this.url);
		value = value.replace(Props.getFormula(tagNow), dts);

		// Chequeamos que no haya quedado nada por resolver
		int f;
		while ((f = value.indexOf("{")) != -1) {

			int t = value.indexOf("}", f);
			if (t == -1)
				t = value.length() - 1;

			value = value.replace(value.substring(f, t + 1), "");
		}

		return value;
	}

	public void copy(Props props) {

		this.columns = props.columns;

		for (Column column : this.columns) {
			column.formula = getFormula(column.internal);
		}
	}
}
