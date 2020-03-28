package hub.contents;

import org.w3c.dom.Element;

public class Column {

	public String name , internal , formula, type;

	public Column(String name) {
		
		this.name = name;
		this.internal = name.replace(" ", "_");
		this.formula = Props.getFormula(this.internal);
	}
	
	public Column(Element e) {
		
		this.name = e.getAttribute("name");
		this.internal = this.name.replace(" ", "_");
		this.formula = e.getAttribute("formula");
		this.type = e.getAttribute("type");
	}
}
