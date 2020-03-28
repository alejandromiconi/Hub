package hub.contents;

import org.w3c.dom.Element;

import hub.contents.LoadFiles.Format;

public class SaveFile extends Concentra {
	
	String filename; //, decimalpoint;
	
	public String getFileName() {
		
		return filename;
	}

	public SaveFile(Element e) {

		super(e);
		
		this.filename = e.getAttribute("filename");
		
	}

	public boolean process(Format format) {

		this.getCurrent(false, filename);
		this.filename = current.props.replace(this.filename);
		current.props.url = this.filename;
		
		this.getConsolidations();
		
		switch(format) {
		
		case JSON:

			this.saveJsonFile(this.filename);
			break;

		case TEXT:

			this.saveTextFile(this.filename);
			break;

		}

		this.logProcessFile(current.data.length());
		return true;
	}
}