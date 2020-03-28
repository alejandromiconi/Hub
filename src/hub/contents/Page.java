package hub.contents;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Page extends Concentra {

	public static final String getText = "text";
	public static final String getName = "name";
	public static final String getId = "id";

	public List<Html> htmls;
	
	
	class Html {
		String type, tag , get , set ;
	}

	static WebDriver driver = null;
	
	public static void checkDriver() {
		if (driver != null) driver.close();
	}
	
	public Page(Element e) {
		
		super(e);
		
		this.htmls = new ArrayList<Html>();
		
		NodeList hs = e.getElementsByTagName("html");
		
		for (int j=0; j<hs.getLength(); j++) {

			Node ph = hs.item(j);
			if (ph.getNodeType() != Node.ELEMENT_NODE) continue;

			Element eh = (Element) ph;

			Html html = new Html();
			html.type = eh.getAttribute("type");
			html.tag = eh.getAttribute("tag");
			html.get = eh.getAttribute("get");
			html.set = eh.getAttribute("set");
			
			this.htmls.add(html);
		}
		
		if (driver == null) {
			System.setProperty("webdriver.chrome.driver","chromedriver.exe");
			driver = new ChromeDriver();
		}
	}
	
	
	
	public boolean setContents() {
		
		try {

			getCurrent(true);
	
			List<WebElement> e = null;
			
			for(Html html : this.htmls) {
	
				switch (html.type) {
				
				case "url":

					driver.get(html.tag);
					break;
				
				case "select":
			
					e = driver.findElements(By.cssSelector(html.tag));
					break;
	
				case "next":
					
					if (e == null);
					
					else if (e.size()>0) {
						e = e.get(0).findElements(By.cssSelector(html.tag));
					}
					
					break;
	
				case "click":
					
					if (!html.tag.isEmpty()) {
						WebElement et = driver.findElement(By.cssSelector(html.tag));
						et.click();
					}
					
					else if (e == null);

					else if (e.size() == 1) e.get(0).click();

					// Provocamos una espera de 1 segundo
					TimeUnit.SECONDS.sleep(1);
					break;

				case "input":
					
					if (e == null) {

						WebElement et = driver.findElement(By.cssSelector(html.tag));
						et.clear();
						
						et.sendKeys(html.set);
						et.sendKeys(Keys.RETURN);
					}
					
					else if (e.size()>0) {

						for(int i = 0 ; i<e.size() ; i++) {
							
							WebElement et = e.get(i).findElement(By.cssSelector(html.tag));
							et.clear();

							et.sendKeys(html.set);
							et.sendKeys(Keys.RETURN);
						}
					}					
					
				case "titles":

					if (e == null);
					
					else if (e.size()>0) {

						for(int i = 0 ; i<e.size() ; i++) {

							List<WebElement> et = e.get(i).findElements(By.cssSelector(html.tag));

							for (int j = 0; j<et.size(); j++) {
								String text = getValue(et.get(j), html);
								if (!text.isEmpty()) current.addName(text);
					        }
						}
					}	
					
					break;
	
				case "columns":

					if (e == null);
					
					else if (e.size()>0) {

						for (int i = 0; i < e.size(); i++) { // Cada columna
							
							WebElement column = e.get(i);
		
							List<WebElement> cr = column.findElements(By.cssSelector(html.tag));

							for (int j = 0; j < cr.size(); j++) {
								current.addLineColumn(j, getValue(cr.get(j), html));
							}
						}
					}
					
					break;
					
				case "rows":
	
					if (e == null);
					
					else if (e.size()>0) {

						for (int i = 0; i < e.size(); i++) { // Son los tr
							
							WebElement tr = e.get(i);
							
							List<WebElement> tds = tr.findElements(By.cssSelector(html.tag)); // tds
							
							List<String> values = new ArrayList<String>();
		
							for (int j = 0; j<tds.size() ; j++) {
								String text = getValue(tds.get(j), html);
								values.add(text);
							}
							
							if(values.size()>0) {
								current.addLine(values);
							}
						}
					}
					
					break;
				}
				
				current.props.url = driver.getCurrentUrl();
			}
		}
		
		catch(Exception e) {
			
			this.logErrorMessage(e.getMessage());
			return false;
		}
		
		
		return true;
	}
	

	public String getValue(WebElement e, Html html) {
		
		String value = "";

		if (html.get == null || html.get.isEmpty())
			html.get = getText;
		
		switch(html.get) {
		
		case getText:
			
			value = e.getText();
			break;
			
	
		default:
			
			value = e.getAttribute(html.get);
			break;
	
		}
		
		return value;
	}
}